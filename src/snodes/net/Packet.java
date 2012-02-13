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

import snodes.crypto.Key;

import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


/**
 * A wrapper for a stream of data sent between two nodes. The data contained
 * within a <tt>Packet</tt> object is a high-level representation of the
 * datagrams sent between two nodes.<p>
 *
 * The general design of <tt>Packet</tt> is as a container for a dictionary of
 * packet data. The packet retains info on the source or destination of the
 * packet and the type of packet, and also wraps a dictionary of the actual
 * data for the packet. The exact structure of the dictionary differs depending
 * upon the packet type.<p>
 *
 * This class is loosely modeled after the Cocoa class <tt>NSNotification</tt>.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class Packet
{
	/** The class's logger. */
	private static final Logger logger = Logger.getLogger("snodes.net");
	
	/** The current protocol version number. */
	public static final String VERSION = "SFXP/1.0";
	
	/** The encoding used by the packet. */
	public static final String ENCODING = "UTF-8";
	
	/** The packet's type. */
	private Type type;
	/** A dictionary mapping keys to values. */
	private Map<String, Object> info;
	
	/**
	 * Creates a new packet with an empty set of properties.
	 *
	 * @param type
	 *     The type of packet.
	 */
	Packet(Type type)
	{
		this.type = type;
		this.info = null;
	}
	
	/**
	 * Creates a new packet object from the byte array. Essentially this constructor
	 * acts as a decoder for the bytes of an encoded packet.
	 *
	 * @param bytes
	 *     The byte array.
	 * @param len
	 *     The length of the data array.
	 * @param key
	 *     The key used to decrypt the packet.
	 * @throws IllegalArgumentException
	 *     If the packet is in an invalid format.
	 */
	static Packet fromBytes(byte[] bytes, int len, Key key) throws IllegalArgumentException
	{
		byte[] data = decrypt(bytes, len, key);
		logger.fine("Creating packet with key: " + key);
		return parse(data, len);
	}
	
	/**
	 * Parses the <em>unencrypted</em> packet data.
	 *
	 * @param bytes
	 *     The unencrypted packet data.
	 * @param len
	 *     The length of the packet data.
	 * @throws IllegalArgumentException
	 *     If the packet is in an invalid format.
	 */
	private static Packet parse(byte[] bytes, int len) throws IllegalArgumentException
	{
		Packet packet = new Packet(null);
		
		logger.finest("Parsing " + len + " bytes...");
		
		try {
			String str = new String(bytes, 0, len, ENCODING);
			BufferedReader in = new BufferedReader(new StringReader(str));
			String line = null;
			
			while ((line = in.readLine()) != null) {
				if (packet.type == null) { // First line, since packet type not set
					
					int firstSpace = line.indexOf(' ');
					if (firstSpace > -1) {
						String typeStr = line.substring(0, firstSpace);
						
						try {
							packet.type = Type.valueOf(typeStr);
						} catch (IllegalArgumentException e) {
							throw new RuntimeException(e);
						}
					} else {
						// Probably the wrong decryption key was used. But sometimes
						// just the version part of the line is cut off, so try to
						// convert to a packet type anyway. This means we ignore
						// the version. This is a hack but will work for now. - mpd
						try {
							logger.info("Attempting to parse packet line again");
							packet.type = Type.valueOf(line);
						} catch (IllegalArgumentException e) {
							throw new IllegalArgumentException(line);
						}
					}
				} else if (!line.trim().equals("")) { // Skip empty lines
					String prop = null;
					String value = null;
					int colon = line.indexOf(':');
					
					prop = line.substring(0, colon);
					value = line.substring(colon+1).trim();
					
					if (value.charAt(0) == '\"') { // Strings are quoted
						value = value.substring(1, value.length()-1);
						packet.putProperty(prop, new String(value));
					} else { // If it's not a string, it can only be an int or a long
						try {
							int last = value.length()-1;
							
							if (value.charAt(last) == 'L') {
								value = value.substring(0, last);
								packet.putProperty(prop, new Long(value));
							} else {
								packet.putProperty(prop, new Integer(value));
							}
						} catch (NumberFormatException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
			return packet;
		} catch (UnsupportedEncodingException e) {
			assert false : (ENCODING + " is not a valid encoding.");
			throw new RuntimeException(e);
		} catch (IOException e) {
			// Don't want to throw an I/O exception here, because that's
			// highly unlikely to happen with a StringReader; just throw a
			// RuntimeException instead.
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converts the packet into a sequence of bytes that can be transmitted over
	 * a network connection. The packet is encrypted with the given key.
	 *
	 * @param key
	 *     The key used to encrypt the packet.
	 * @return
	 *     The packet's byte representation.
	 * @throws IllegalArgumentException
	 *     If <tt>key</tt> is too short.
	 */
	byte[] toByteArray(Key key) throws IllegalArgumentException
	{
		if (key == null) throw new IllegalArgumentException("null key");
		
		try {
			byte[] raw = toString().getBytes(ENCODING);
			byte[] encrypted = null;
			
			encrypted = encrypt(raw, key);
			
			if (encrypted.length <= SnodesServer.UDP_MAX) {
				return encrypted;
			} else {
				throw new RuntimeException("Packet length " + encrypted.length);
			}
		} catch (UnsupportedEncodingException e) {
			assert false : (ENCODING + " is not a valid encoding.");
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Encrypts the packet using the given key.
	 *
	 * @param bytes
	 *     The packet data.
	 * @param key
	 *     The packet key.
	 * @return
	 *     The encrypted packet data.
	 * @throws IllegalArgumentException
	 *     If <tt>key</tt> is too short.
	 */
	private static byte[] encrypt(byte[] bytes, Key key) throws IllegalArgumentException
	{
		logger.fine("Encrypting packet with key " + key);
		
		KeyParameter fishkey = new KeyParameter(key.toByteArray());
		TwofishEngine twofish = new TwofishEngine();
		int blocksize = twofish.getBlockSize();
		int blocks = (int) Math.ceil((double) bytes.length / (double) blocksize);
		
		twofish.init(true, fishkey);                   // true = encrypt
		byte[] encrypted = new byte[blocks*blocksize]; // Leave room for padding
		
		for (int i = 0; i < blocks; i++) {
			byte[] block = new byte[blocksize];
			int offset = i * blocksize;
			int written = 0;
			
			// Pad if not enough bytes -- otherwise, exception is thrown
			if (bytes.length-offset < blocksize) {
				BlockCipherPadding pad = new PKCS7Padding();
				int padLen = bytes.length - offset;
				byte[] write = new byte[blocksize];
				int padding = 0;
				
				System.arraycopy(bytes, offset, write, 0, padLen);
				
				pad.init(new SecureRandom());
				padding = pad.addPadding(write, padLen);
				
				written = twofish.processBlock(write, 0, block, 0);
			} else {
				written = twofish.processBlock(bytes, offset, block, 0);
			}
			
			System.arraycopy(block, 0, encrypted, offset, written);
		}
		
		return encrypted;
	}
	
	/**
	 * Decrypts the packet using the given key.
	 *
	 * @param bytes
	 *     The packet data.
	 * @param len
	 *     The length of the data array.
	 * @param key
	 *     The key previously used to encrypt the data.
	 * @return
	 *     The decrypted packet data.
	 */
	private static byte[] decrypt(byte[] bytes, int len, Key key)
	{
		logger.finest("Initial decrypt size: " + len + " bytes");
		
		KeyParameter fishkey = new KeyParameter(key.toByteArray());
		TwofishEngine twofish = new TwofishEngine();
		byte[] decrypted = new byte[len];
		int blocksize = twofish.getBlockSize();
		int blocks = (int) Math.ceil((double) len / (double) blocksize);
		
		assert len % blocksize == 0 : "len is not a multiple of blocksize";
		twofish.init(false, fishkey); // false = decrypt
		
		//logger.finest("Decrypting " + len + " bytes (" + blocks + " blocks)");
		for (int i = 0; i < blocks; i++) {
			byte[] block = new byte[blocksize];
			int offset = i * blocksize;
			twofish.processBlock(bytes, offset, block, 0);
			
			System.arraycopy(block, 0, decrypted, offset, blocksize);
		}
		
		return decrypted;
	}
	
	/**
	 * Returns a string representation of the packet.
	 *
	 * @return
	 *     The string representation of the packet.
	 */
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		
		buf.append(type).append(" ").append(VERSION).append("\n");
		
		if (info != null) {
			Iterator<String> keyIter = info.keySet().iterator();
			
			while (keyIter.hasNext()) {
				String key = keyIter.next();
				Object value = info.get(key);
				
				buf.append(key).append(": ");
				if (value != null) {
					if (value instanceof String) { // Strings get quoted
						buf.append("\"").append(value).append("\"");
					} else {
						buf.append(value);
						if (value instanceof Long) {
							buf.append('L');
						}
					}
				}
				buf.append("\n");
			}
		}
		
		return new String(buf).trim();
	}
	
	/**
	 * Gets the type of the packet.
	 *
	 * @return
	 *     The type of the packet.
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Adds an attribute to the packet.
	 *
	 * @param key
	 *   The name of the attribute.
	 * @param value
	 *   The value of the attribute.
	 * @return
	 *   The packet.
	 * @throws IllegalArgumentException
	 *   If <tt>key</tt> or <tt>value</tt> is <tt>null</tt>.
	 */
	public Packet putProperty(String key, Object value)
	{
		if (key == null) {
			throw new IllegalArgumentException("'key' may not be null");
		}
		if (value == null) {
			throw new IllegalArgumentException("'value' may not be null");
		}
		
		if (info == null) {
			info = new HashMap<String,Object>();
		}
		
		info.put(key, value);
		return this;
	}
	
	/**
	 * Returns the specified property from the packet.
	 *
	 * @param key
	 *     The property.
	 * @return
	 *     The value for the given property, or <tt>null</tt> if the given
	 *     property is not defined.
	 */
	public Object getProperty(String key)
	{
		if (info != null) {
			return info.get(key);
		} else {
			return null;
		}
	}
	
	
	/**
	 * The type of the packet. The packet types are described in the SFXP
	 * specification.<p>
	 *
	 * The name of packet types map directly to the <tt>Type</tt> name, so use
	 * {@link #valueOf} to convert from a <tt>String</tt> object
	 * directly to a <tt>Type</tt> value.
	 */
	public enum Type
	{
		/** Initiates a connection between two nodes. */
		Connect,
		/** Accepts of a previous connection request. */
		AcceptConnection,
		/** Closes a connection. */
		CloseConnection,
		/** Sends a file to a remote host. */
		TransferFile,
		/** Requests a file. */
		RequestFile,
		/** Confirms that a node has a file and can send it. */
		ConfirmFileRequest,
		/** Requests that a file segment be resent. */
		RequestAgain,
		/** Cancels a file transfer. */
		CancelTransfer,
		/** A simple chat packet. */
		//TODO - implement chat after nodes are properly connected.
		ChatMessage
	}
}
