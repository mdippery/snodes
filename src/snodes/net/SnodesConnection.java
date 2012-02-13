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

import snodes.crypto.EncryptionKey;
import snodes.crypto.Key;
import snodes.crypto.Passkey;
import snodes.util.Base64;

import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Creates a new connection to another SFXP node. A trivial use of this connection
 * API for outgoing connections might follow this pattern:<p>
 *
 * <pre>
 * // Create a connection to 192.168.0.2
 * SnodesConnection conn = new SnodesConnection("192.168.0.2");
 * // Authenticate the connection
 * conn.authenticate("mypasskey");
 * // Connect
 * conn.connect();
 *
 * // After a confirmation packet is received
 * conn.authorize(mySessionID, myEncryptKey);
 *
 * // Do stuff with the connection
 *
 * // Close connection
 * conn.disconnect();
 * </pre>
 *
 * Alternatively, new connections may be received from {@link SnodesServer}. Such
 * connections have already been set up, but must be authenticated. A trivial
 * class which authorizes all incoming connections may look like this:
 *
 * <pre>
 * // Create a listener for new connections
 * ServerListener sl = new ServerListener() {
 *     public void processConnection(SnodesConnection conn) {
 *         // Automatically authenticate/authorize all incoming connections
 *         conn.authenticate(myGenericPasskey);
 *         conn.authorize(myID++, myGenericEncryptionKey);
 *         conn.accept();
 *     }
 * };
 * </pre>
 *
 * A <tt>SnodesConnection</tt> can be reused between connections. In other words, a
 * connection may be created and connected, then disconnected, then reconnected.
 * However, a connection need only be authenticated once; once authenticated, it
 * can be connected and reconnected as many times as desired.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @author Michael Schoonmaker
 * @version 0.1
 */
public class SnodesConnection
{
	/** The class logger. */
	private static final Logger logger = Logger.getLogger("snodes.net");
	
	/** A flag to indicate that the ID number has not been sent. */
	private static final int NO_ID = -1;
	
	/** The remote host. */
	private InetAddress host;
	/** The passkey used to connect to the designated remote host. */
	private Passkey passkey;
	/** The key used to encrypt the session. */
	private EncryptionKey encryptKey;
	/** The connection's session ID number. */
	private int id;
	/** The set of file transfers used by this connection. */
	private Map<String, FileTransfer> transfers;
	/** The current chat with the remote host. */
	private Chat chat;
	/** The current status of the connection. */
	private Status status;
	/** The connection's packet listeners. */
	private Set<ListenerWrapper> listeners;
	
	/**
	 * Creates a new connection to the specified host. The host can be specified
	 * as an IP address (e.g., <tt>192.168.0.2</tt>), or as a fully-qualified
	 * hostname (e.g., <tt>example.com</tt>).<p>
	 *
	 * The connection does not connect automatically; to connect, you must call
	 * {@link #connect} after creating a connection.
	 *
	 * @param host
	 *     The remote host.
	 * @throws UnknownHostException
	 *     If the host is invalid.
	 */
	public SnodesConnection(String host) throws UnknownHostException
	{
		this.host = InetAddress.getByName(host);
		
		this.passkey = null;
		this.encryptKey = null;
		this.id = NO_ID;
		this.transfers = new HashMap<String, FileTransfer>();
		this.chat = null;
		this.status = Status.NEW;
		this.listeners = new HashSet<ListenerWrapper>();
	}
	
	/**
	 * Authenticates the connection using the given passkey. This is the passkey
	 * used to connect to the host specified by {@link #getHost}.
	 *
	 * @param passkey
	 *     The passkey used to connect to the connection's remote host.
	 */
	public void authenticate(String passkey)
	{
		this.passkey = new Passkey(passkey);
	}
	
	/**
	 * Establishes a connection to the remote host.<p>
	 *
	 * This is merely a connection <em>request</em>, and could be denied or ignored
	 * by the remote host.
	 *
	 * @throws IOException
	 *     If a connection cannot be established.
	 */
	@GuardedBy("this")
	public synchronized void connect() throws IOException
	{
		Packet packet = new Packet(Packet.Type.Connect);
		packet.putProperty("Passkey", passkey.toString());
		sendPacket(packet);
		status = Status.CONNECTING;
	}
	
	/**
	 * Authorizes a connection by setting a session ID and an encryption key.
	 *
	 * The connection should be {@link #authenticate authenticated} before
	 * authorization.
	 *
	 * @param id
	 *     The session ID number.
	 * @param key
	 *     The key used to encrypt the session.
	 */
	@GuardedBy("this")
	public synchronized void authorize(int id, byte[] key)
	{
		logger.fine("Authorizing connection");
		
		this.id = id;
		this.encryptKey = new EncryptionKey(key);
		//this.status = Status.AUTHORIZED;
	}
	
	/**
	 * Accepts the connection. This is in response to a connection request.<p>
	 *
	 * The connection should be {@link #authenticate authenticated} and
	 * {@link #authorize authorized} first.<p>
	 *
	 * Essentially this method corresponds to an <tt>AcceptConnection</tt> packet.
	 *
	 * @throws IOException
	 *     If the connection cannot be authorized.
	 * @throws IllegalStateException
	 *     If the connection has not been {@link #authorize authorized}.
	 */
	@GuardedBy("this")
	public synchronized void accept() throws IOException, IllegalStateException
	{
		if (encryptKey == null) throw new IllegalStateException("EncryptKey is null");
		
		String base64key = Base64.encodeBytes(encryptKey.toByteArray()); // Not gzipped!
		Packet packet = new Packet(Packet.Type.AcceptConnection);
		packet.putProperty("Passkey", passkey.toString());
		packet.putProperty("Id", new Integer(id));
		packet.putProperty("EncryptKey", base64key);
		sendPacket(packet);
	}
	
	/** Closes the connection to the remote host. */
	@GuardedBy("this")
	public synchronized void disconnect()
	{
		try {
			Packet packet = new Packet(Packet.Type.CloseConnection);
			sendPacket(packet);
		} catch (IOException e) {
			logger.log(Level.INFO, "Cannot send disconnect packet", e);
		} finally {
			status = Status.NEW;
		}
	}
	
	/**
	 * Creates a new transfer to the remote host. If the file is already being
	 * transferred, the existing file transfer object will be returned.
	 *
	 * @param file
	 *     The name of the shared file to transfer.
	 */
	public FileTransfer createTransfer(String file)
	{
		FileTransfer transfer = transfers.get(file);
		if (transfer == null) {
			transfer = new FileTransfer(this, file);
			transfers.put(file, transfer);
		}
		return transfer;
	}
	
	/**
	 * Creates a new chat with the remote host. If a chat already exists, that
	 * chat object is returned.
	 *
	 * @return
	 *     A chat to the remote host.
	 */
	public Chat createChat()
	{
		if (chat == null) chat = new Chat(this);
		return chat;
	}
	
	/**
	 * Registers a packet listener. A packet listener will be notified of all
	 * packet events that occur on this connection.<p>
	 *
	 * Packet listeners can be added for a number of reasons, and offer a way to
	 * interact with the connection. For example, if you want to be alerted every
	 * time this connection receives a file request, you might use this code:<p>
	 *
	 * <pre>
	 * PacketListener pl = new PacketListener() {
	 *     public void processPacket(SnodesConnection conn, Packet packet) {
	 *         System.err.println("Received file request from " + conn.getHost());
	 *     }
	 * };
	 * PacketFilter pf = new PacketFilter() {
	 *     public boolean accept(Packet.Type type) {
	 *         if (type == Packet.Type.RequestFile) {
	 *             return true;
	 *         } else {
	 *             return false;
	 *         }
	 *     }
	 * };
	 * conn.addListener(pl, pf);
	 * </pre>
	 *
	 * Packet listeners are often used to be notified of file transfers, too:<p>
	 *
	 * <pre>
	 * PacketListener pl = new PacketListener() {
	 *     public void processPacket(SnodesConnection conn, Packet packet) {
	 *         System.err.println("Received file from " + conn.getHost());
	 *     }
	 * };
	 * PacketFilter pf = new PacketFilter() {
	 *     public boolean accept(Packet.Type type) {
	 *         if (type == Packet.Type.TransferFile) {
	 *             return true;
	 *         } else {
	 *             return false;
	 *         }
	 *     }
	 * };
	 * conn.addListener(pfl, pff);
	 * </pre>
	 *
	 * Packet listeners are notified on the same thread as the connection, so
	 * listeners should do their work as soon as possible, or spawn another thread
	 * to do the work.<p>
	 *
	 * <em>There is no way to add a listener to a {@link FileTransfer}</em>, so
	 * observers interested in file transfers must add listeners to this connection
	 * instead.
	 *
	 * @param listener
	 *     The packet listener.
	 * @param filter
	 *     A filter to apply to this listener, which allows only certain types
	 *     of packets.
	 */
	public void addListener(PacketListener listener, PacketFilter filter)
	{
		if (listener == null) return;
		
		listeners.add(new ListenerWrapper(listener, filter));
	}
	
	/**
	 * Returns the status of the connection.
	 *
	 * @return
	 *     The connection's status.
	 */
	public Status getStatus()
	{
		return status;
	}
	
	/**
	 * Returns the remote host associated with this connection.
	 *
	 * @return
	 *     The host to which this connection is connected.
	 */
	public InetAddress getHost()
	{
		return host;
	}
	
	/**
	 * Returns the connection's session ID number.
	 *
	 * @return
	 *     The connection's session ID number.
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * Returns the connection's <em>current</em> encryption key.
	 *
	 * @return
	 *     The connection's encryption key.
	 */
	public Key getKey()
	{
		return (status == Status.AUTHORIZED ? encryptKey : passkey);
	}
	
	/**
	 * Returns <tt>true</tt> if this object is equal to <tt>obj</tt>.<p>
	 *
	 * This object is equal to other <tt>SnodesConnection</tt> objects with the
	 * same ID number.
	 *
	 * @param obj
	 *     The other object.
	 * @return
	 *     <tt>true</tt> if the objects are equal.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		} else if (obj == null || obj.getClass() != getClass()) {
			return false;
		} else {
			SnodesConnection conn = (SnodesConnection) obj;
			return id == conn.id;
		}
	}
	
	/**
	 * Returns this object's hash code.
	 *
	 * @return
	 *     The object's hash code.
	 */
	@Override
	public int hashCode()
	{
		return 31 * 7 + id;
	}
	
	/**
	 * Returns a string representation of the connection. Such a representation
	 * contains the class name and remote host, in this format:<p>
	 *
	 * <pre>
	 * SnodesConnection@192.168.0.2
	 * </pre>
	 *
	 * @return
	 *     A string representation of the connection object.
	 */
	@Override
	public String toString()
	{
		return "SnodesConnection@" + host.getHostAddress();
	}
	
	/**
	 * Processes incoming packets.
	 *
	 * @param packet
	 *     The packet.
	 */
	void processPacket(Packet packet)
	{	
		for (final ListenerWrapper lw : listeners) {
			if (lw.filter.accept(packet.getType())) {
				lw.listener.processPacket(this, packet);
			}
		}
		
		// If connection accepted, set flag to AUTHORIZED
		if (packet.getType() == Packet.Type.AcceptConnection) {
			status = Status.AUTHORIZED;
		}
	}
	
	/**
	 * Sends a packet to the remote host via the writer's output stream.
	 *
	 * @param packet
	 *     The packet to send.
	 * @throws IOException
	 *     If an I/O error occurs while sending the packet.
	 */
	void sendPacket(Packet packet) throws IOException
	{
		if (packet == null) return;
		
		logger.fine("Attempting to send packet " + packet.getType() + "...");
		logger.finest("Packet is:\n" + packet);
		
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket dgram = null;
		byte[] bytes = null;
		
		switch (packet.getType()) {
		case Connect:
		case AcceptConnection:
			bytes = packet.toByteArray(passkey);
			break;
		default:
			bytes = packet.toByteArray(encryptKey);
			break;
		}
		
		dgram = new DatagramPacket(bytes, bytes.length, host, SnodesServer.PORT);
		socket.send(dgram);
	}
	
	
	/** Indicates the status of the connection. */
	public enum Status
	{
		/** The connection is new and no connection attempt has been made. */
		NEW,
		/** A Connection packet has been sent, but no confirmation has been received. */
		CONNECTING,
		/** An AcceptConnection packet has been received. Future packets should be
		 *  encrypted and decrypted using the 256-bit session key. */
		AUTHORIZED
	}
	
	
	/* A wrapper for packet listeners and filters. */
	private static class ListenerWrapper
	{
		private final PacketListener listener;
		private final PacketFilter filter;
		
		private ListenerWrapper(PacketListener listener, PacketFilter filter)
		{
			this.listener = listener;
			this.filter = filter;
		}
	}
}
