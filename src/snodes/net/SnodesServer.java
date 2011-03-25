/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Dippery <mdippery@bucknell.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package snodes.net;

import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles incoming connections via the Snodes protocol.<p>
 *
 * A typical use of <tt>SnodesServer</tt> looks like this:<p>
 *
 * <pre>
 * // Get the server instance
 * SnodesServer server = SnodesServer.getInstance();
 * // Add listeners and connection managers
 * server.addListener(myListener);
 * server.setConnectionManager(myManager);
 * // Start the server
 * server.start();
 * </pre>
 *
 * The server runs in its own thread, and will be shut down automatically when
 * the Java VM exits. There is no way to manually stop or restart the server.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class SnodesServer
{
	/** The class logger. */
	private static final Logger logger = Logger.getLogger("snodes.net");
	
	/** The port on which the server will listen for packets. */
	public static final int PORT = 8010;
	
	/** The theoretical max size of a UDP datagram. */
	static final int UDP_MAX = 65507;
	/** The maximum size of a UDP datagram in most UDP implementations. */
	static final int UDP_REAL_MAX = 8192;
	/** The maximum size of a UDP datagram sent via the Snodes protocol. */
	static final int UDP_SNODES_MAX = UDP_REAL_MAX;
	
	/** The singleton instance of the server. */
	private static SnodesServer singleton = null;
	
	/** The thread on which the server runs. */
	private Thread thread;
	/** The object responsible for tracking connections. */
	private ConnectionManager connectionManager;
	
	/** Creates a new instance of <tt>SnodesServer</tt>. */
	private SnodesServer()
	{
		connectionManager = null;
		
		Runnable runner = new Runnable() {
			public void run() {
				listen();
			}
		};
		
		thread = new Thread(runner, "Server Thread");
		thread.setUncaughtExceptionHandler(new DefaultExceptionHandler());
		thread.setDaemon(true); // Quit when Java VM exits
	}
	
	/**
	 * Gets the application-wide instance of <tt>SnodesServer</tt>.
	 *
	 * @return
	 *     The singleton instance of <tt>SnodesServer</tt>.
	 */
	@GuardedBy("this")
	public static synchronized SnodesServer getInstance()
	{
		if (singleton == null) singleton = new SnodesServer();
		return singleton;
	}
	
	/**
	 * Starts the server.<p>
	 *
	 * The server runs on its own thread and will terminate when the JVM exits.
	 *
	 * @throws IllegalStateException
	 *     If the server is already running.
	 */
	public void start() throws IllegalStateException
	{
		if (thread.getState() == Thread.State.NEW) {
			thread.start();
		} else {
			throw new IllegalStateException("Server already running");
		}
	}
	
	/** Parses the packets from the network stream. */
	private void listen()
	{
		try {
			DatagramSocket socket = new DatagramSocket(PORT);
		
			logger.info("Starting server: OK");
		
			while (!thread.isInterrupted()) {
				byte[] data = new byte[UDP_MAX];
				
				final DatagramPacket dgram = new DatagramPacket(data, UDP_MAX);
				
				try {
					socket.receive(dgram);
					
					// Run the packet parser in an independent thread; this way,
					// if the parser crashes (as it sometimes does), it won't
					// take down the whole server with it.
					Runnable parseRunner = new Runnable() {
						public void run() {
							processDatagram(dgram);
						}
					};
					Thread parser = new Thread(parseRunner, "Packet Parser Thread");
					parser.setUncaughtExceptionHandler(new DefaultExceptionHandler());
					
					parser.start();
				} catch (IOException e) {
					logger.warning("Server error: " + e.getMessage());
				}
				
				Thread.yield(); // Let other threads execute
			}
			
			logger.info("Stopping server: OK");
		} catch (SocketException e) {
			logger.log(Level.SEVERE, "Starting server: FAILED", e);
		}
	}
	
	/**
	 * Processes an incoming datagram.
	 *
	 * @param dgram
	 *     The datagram.
	 */
	private void processDatagram(DatagramPacket dgram)
	{
		InetAddress host = dgram.getAddress();
		byte[] data = dgram.getData();
		int len = dgram.getLength();
		Packet packet = null;
		
		SnodesConnection conn = connectionManager.getConnection(host); // null if not validated
		if (conn != null) {
			packet = Packet.fromBytes(data, len, conn.getKey());
			processPacket(packet, host);
		}
	}
	
	/**
	 * Processes incoming packets.
	 *
	 * @param packet
	 *     The packet.
	 * @param host
	 *     The host that sent the packet.
	 */
	private void processPacket(Packet packet, InetAddress host)
	{
		SnodesConnection conn = connectionManager.getConnection(host);
		if (conn != null) {
			conn.processPacket(packet);
		} else {
			logger.warning("No destination for packet: " + host);
		}
	}
	
	/**
	 * Sets the object responsible for tracking connections. This object will be
	 * queried for connection information.<p>
	 *
	 * Only one manager may be set at any given time.
	 *
	 * @param manager
	 *     The connection manager.
	 */
	public void setConnectionManager(ConnectionManager manager)
	{
		connectionManager = manager;
	}
	
	
	/** Default exception handler for uncaught exceptions. */
	private static class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler
	{
		public void uncaughtException(Thread t, Throwable e)
		{
			String msg = t.getName() + " crashed";
			logger.log(Level.SEVERE, msg, e);
		}
	}
}
