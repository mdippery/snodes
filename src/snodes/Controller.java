/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2008 Michael Dippery <michael@monkey-robot.com>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
package snodes;

import snodes.gui.GUIController;
import snodes.net.ConnectionManager;
import snodes.net.PacketListener;
import snodes.net.PacketFilter;
import snodes.net.Packet;
import snodes.net.SnodesConnection;
import snodes.util.Base64;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for application controllers.
 *
 * @author Michael Schoonmaker
 * @author <a href="mailto:michael@monkey-robot.com">Michael Dippery</a>
 * @version 0.1
 */
public abstract class Controller implements PacketListener, PacketFilter, ConnectionManager
{
	/** The class logger. */
	private static final Logger logger = Logger.getLogger("snodes");
	
	/** The name of the program. */
	public final static String NAME = "Spaghetti Nodes";
	/** The version number of the program. */
	public final static String VERSION = "0.2a1";
	
	/** The set of all current connections. */
	// This can be a set; SnodesConnection.equals() is based on session ID
	//private Set<SnodesConnection> connections;
	/** The set of all attempted connections. */
	// Should be renamed once it fulfills the proper responsibility.
	private Map<InetAddress, SnodesConnection> connectionMap;
	/** The set of all acceptable hosts. */
	//private Set<InetAddress> validHosts;
	/** The random number generator for session IDs. */
	private Random rng;
	
	/**
	 * Creates a new controller instance. This program does some basic setup
	 * for controllers; concrete subclasses should be sure to call this
	 * constructor.
	 */
	public Controller() {
		connectionMap = new HashMap<InetAddress, SnodesConnection>();
		rng = new Random();
	}
	
	/**
	 * Prints an object.
	 *
	 * @param inObject
	 *     The object to print.
	 */
	public abstract void print(Object inObject);
	
	/**
	 * Prints an object and automatically appends a line feed.
	 *
	 * @param inObject
	 *     The object to print.
	 */
	public abstract void println(Object inObject);
	
	/**
	 * Handles an incoming packet.
	 *
	 * @param conn
	 *     The connection on which the packet event occurred.
	 * @param packet
	 *     The packet.
	 */
	public void processPacket(SnodesConnection conn, Packet packet) {
		logger.info("Received packet " + packet.getType() + " from " + conn);

		switch (packet.getType()) {
			case Connect:
			{
				byte[] encryptionKey = new byte[32];
				int sessionID = Math.abs(rng.nextInt());
				
				logger.info("processPacket() received connection attempt: " + conn);

				// Generate a random encryption key
				try {
					SecureRandom srng = SecureRandom.getInstance("SHA1PRNG");
					srng.nextBytes(encryptionKey);
					logger.finer("Created encryption key with secure RNG");
				} catch (NoSuchAlgorithmException e) {
					logger.log(Level.WARNING, "Invalid RNG algorithm", e);
					logger.warning("Using insecure RNG algorithm");
					new Random().nextBytes(encryptionKey);
				}

				// Connection should have already been authenticated.
				conn.authorize(sessionID, encryptionKey);

				try {
					conn.accept();
					logger.finer("Added connection (" + sessionID + "): " + conn);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Cannot establish connection", e);
				}

				} break;
				case AcceptConnection:
				{
					logger.finer("Searching for attempt: " + conn + "...");
					if (connectionMap.get(conn.getHost()) != null) {
						logger.finer("Found attempt: " + conn);

						Integer sessionIDObj = null;
						int sessionID = -1;
						byte[] encryptKey = new byte[32];

						connectionMap.remove(conn.getHost());
						logger.finer("Removed attempt: " + conn);

						new Random().nextBytes(encryptKey);

						try {
							sessionIDObj = (Integer) packet.getProperty("Id");
							assert sessionIDObj != null : "Packet Id is null";
							sessionID = sessionIDObj.intValue();
							
							String base64key = (String) packet.getProperty("EncryptKey");
							byte[] newkey = Base64.decode(base64key);
							conn.authorize(sessionID, newkey);
							conn.accept();
							logger.finer("Added connection (" + sessionID + "): " + conn);
							logger.info("Accepted connection: " + conn);
						} catch (IOException ex) {
							logger.log(Level.SEVERE, null, ex);
						} catch (IllegalStateException ex) {
							logger.log(Level.SEVERE, null, ex);
						} catch (ClassCastException e) {
							logger.log(Level.SEVERE, "Cannot get session ID", e);
						}
					} else {
						logger.severe("No attempted connection: " + conn);
					}
				}
				break;
			}
		}
	
	/**
	 * Returns <tt>true</tt> if the listener should accept the specified packet
	 * type.
	 *
	 * @return
	 *     <tt>true</tt> if the packet should be accepted.
	 */
	public boolean accept(Packet.Type type) {
		//TODO: Update as more of the design is implemented.
		switch(type) {
		/*
		 *	From Packet: Initiates a connection between two nodes. 
		 *	Our Interest: Need to refresh the UI.
		 */
		case Connect:
		
		/*
		 *	From Packet: Accepts of a previous connection request.
		 *	Our Interest: None. Here if one arises.
		 */
		case AcceptConnection:
		
		/*
		 *	From Packet: Closes a connection.
		 *	Our Interest: Need to refresh the UI again...
		 */
		case CloseConnection:
		
		/*
		 *	From Packet: Sends a file to a remote host.
		 *	Our Interest: None. We started it. Here if one arises.
		 */
		//case TransferFile:
		
		/*
		 *	From Packet: Requests a file.
		 *	Our Interest: None. Here when one aries.
		 */
		//case RequestFile:
		
		/*
		 *	From Packet: Confirms that a node has a file and can send it.
		 *	Our Interest: None. Here when one arises.
		 */
		//case ConfirmFileRequest:
		
		/*
		 *	From Packet: Requests that a file segment be resent.
		 *	Our Interest: None. Here when one arises.
		 */
		//case RequestAgain:
		
		/*
		 *	From Packet: Cancels a file transfer.
		 *	Our Interest: None. Here when one arises.
		 */
		//case CancelTransfer:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Establishes the given connection.
	 *
	 * @param conn
	 *     The connection.
	 */
	public void connect(SnodesConnection conn)
	{
		connectionMap.put(conn.getHost(), conn);
		logger.finer("Adding connection attempt: " + conn);
		try {
			conn.addListener(this, this);
			conn.connect();
			logger.fine("Connected to " + conn);
		} catch (IOException e) {
			logger.warning("Cannot connect to " + conn.getHost());
		}
	}
	
	/**
	 * Returns an array of connections.
	 *
	 * @return
	 *     A map of session IDs to connections.
	 */
	public SnodesConnection[] getConnections() {
		return connectionMap.values().toArray(new SnodesConnection[0]);
	}
	
	/**
	 * Returns a connection object for the given host, or <tt>null</tt> if such
	 * a connection does not exist.
	 *
	 * @return
	 *     The connection object for the given host, or <tt>null</tt> if none exists.
	 */
	public SnodesConnection getConnection(InetAddress host) {
		return connectionMap.get(host);
	}
	
	/**
	 * Validates the given host.
	 *
	 * @param host
	 *     The address to validate.
	 */
	public void validateHost(InetAddress host) {
		try {
			connectionMap.put(host, new SnodesConnection(host.getHostName()));
			logger.info("Validating host: " + host);
		} catch (UnknownHostException ex) {
			logger.log(Level.SEVERE, "Unknown Host: ", ex);
		}
	}

	public void invalidateHost(InetAddress host) {
		connectionMap.remove(host);
	}

	public boolean isValidHost(InetAddress address) {
		return connectionMap.get(address) != null;
	}

	/**
	 * Composed method for {@link #validateHost}, {@link #getConnection}.
	 *
	 * @param address
	 *     The IP address or hostname of the connection to
	 *     be added.
	 */
	public SnodesConnection addConnection(String address) {
		try {
			InetAddress host = InetAddress.getByName(address);
			validateHost(host);
			SnodesConnection conn = getConnection(host);
			logger.fine("addConnection() returning " + conn);
			return conn;
		} catch (UnknownHostException ex) {
			logger.log(Level.SEVERE, "Unknown Host:", ex);
		}
		return null;
	}
}
