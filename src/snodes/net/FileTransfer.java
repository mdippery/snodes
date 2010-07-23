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

import snodes.util.Base64;

import java.io.IOException;
import java.util.zip.Checksum;
import java.util.zip.CRC32;


/**
 * A file transfer to a remote host.
 *
 * Transfers are created using {@link SnodesConnection#createTransfer}, and are
 * created for both outgoing transfers (<em>uploads</em> to another host) and
 * incoming transfers (<em>downloads</em> from another host to the local machine).<p>
 *
 * A use of the file transfer manager might be as follows:<p>
 *
 * <pre>
 * // Upload a file to another host
 * FileTransfer up = conn.createTransfer("myfile.txt");
 * up.send(bytes, 1, 144);
 *
 * // Request a file from another host
 * FileTransfer down = conn.createTransfer("otherfile.txt");
 * down.request();
 * </pre>
 *
 * <tt>FileTransfer</tt> objects are often created in response to file requests.
 * For example, to implement a listener to listen for file requests and create a
 * new file transfer object in response, you might do this:<p>
 *
 * <pre>
 * PacketListener ftCreator = new PacketListener() {
 *     public void processPacket(SnodesConnection conn, Packet packet) {
 *         if (packet.getType() == Packet.Type.RequestFile) {
 *             String file = (String) packet.getProperty("ShareName");
 *             FileTransfer ft = conn.createTransfer(file);
 *             processFileRequest(ft);
 *         }
 *     }
 * };
 * </pre>
 *
 * <em>The above is very important to note.</em> As of v0.1, the
 * <tt>SnodesConnection</tt> class has no way to return already-created
 * <tt>FileTransfer</tt> objects to its listeners, so the listeners (or delegates
 * of the listeners) must perform this task.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 * @see SnodesConnection#createTransfer
 * @see FileTransfer#send
 * @see FileTransfer#request
 * @see PacketListener
 * @see SnodesConnection#addListener
 */
public class FileTransfer
{
	/** The transfer's owner. */
	private SnodesConnection owner;
	/** The name of the file being transferred. */
	private String filename;
	
	/**
	 * Creates a new file transfer.
	 *
	 * @param owner
	 *     The owner of the transfer.
	 * @param filename
	 *     The name of the file being transferred.
	 */
	FileTransfer(SnodesConnection owner, String filename)
	{
		this.owner = owner;
		this.filename = filename;
	}
	
	/**
	 * Sends a file segment.<p>
	 *
	 * <em>The byte array is automatically converted to a Base64 representation and
	 * gzipped before being sent.</em> When incoming <tt>FileTransfer</tt> packets
	 * are received, bear in mind that the data is a Base64 string that is
	 * compressed using gzip. Thus, when receiving a file, you will have to do
	 * the following:<p>
	 *
	 * <pre>
	 * String data = (String) packet.getProperty("Data");
	 * byte[] bytes = Base64.decode(data);
	 * </pre>
	 *
	 * This will work fine, since {@link snodes.util.Base64} can automatically
	 * detect and decompress gzipped data.
	 *
	 * @param bytes
	 *     The data for the file segment.
	 * @param seg
	 *     The (sequential) ID of the segment.
	 * @param size
	 *     The <em>total</em> size of the whole file, in bytes.
	 * @throws IOException
	 *     If the file cannot be sent due to a disk or network error.
	 * @see Base64
	 * @see Base64#decode(String)
	 * @see Base64#GZIP
	 */
	public void send(byte[] bytes, int seg, long size) throws IOException
	{
		String data = Base64.encodeBytes(bytes, Base64.GZIP);
		Checksum crc = new CRC32();
		long hash = 0; // hash is 32 bits, but it is an *unsigned* int
		
		crc.update(bytes, 0, bytes.length);
		hash = crc.getValue();
		
		Packet packet = new Packet(Packet.Type.TransferFile);
		packet.putProperty("Id", new Integer(owner.getID()));
		packet.putProperty("ShareName", filename);
		packet.putProperty("TotalSize", new Long(size));
		packet.putProperty("SegmentSize", new Integer(bytes.length));
		packet.putProperty("Segment", new Integer(seg));
		packet.putProperty("Hash", new Long(hash));
		packet.putProperty("Data", data);
		owner.sendPacket(packet);
	}
	
	/**
	 * Requests that the specified file be sent. This does not guarantee that
	 * the file <em>will</em> be sent by the remote host; it is merely a
	 * request.
	 *
	 * @throws IOException
	 *     If the request cannot be sent due to a network error.
	 * @see #getFilename
	 */
	public void request() throws IOException
	{
		Packet packet = new Packet(Packet.Type.RequestFile);
		packet.putProperty("Id", new Integer(owner.getID()));
		packet.putProperty("ShareName", filename);
		owner.sendPacket(packet);
	}
	
	/**
	 * Requests that a specific file segment be re-sent. This is used if the
	 * packet carrying a segment was dropped.
	 *
	 * @param seg
	 *     The file segment ID.
	 * @throws IOException
	 *     If the request cannot be sent due to a network error.
	 * @see #getFilename
	 */
	public void request(int seg) throws IOException
	{
		Packet packet = new Packet(Packet.Type.RequestAgain);
		packet.putProperty("Id", new Integer(owner.getID()));
		packet.putProperty("ShareName", filename);
		packet.putProperty("Segment", new Integer(seg));
		owner.sendPacket(packet);
	}
	
	/**
	 * Cancels the file transfer request.
	 *
	 * @throws IOException
	 *     If the cancel request cannot be made due to a network error.
	 */
	public void cancel() throws IOException
	{
		Packet packet = new Packet(Packet.Type.CancelTransfer);
		packet.putProperty("Id", new Integer(owner.getID()));
		packet.putProperty("ShareName", filename);
		owner.sendPacket(packet);
	}
	
	/**
	 * Returns the name of the file being transferred.
	 *
	 * @return
	 *     The filename.
	 */
	public String getFilename()
	{
		return filename;
	}
	
	/**
	 * Returns the transfer's owner connection.
	 *
	 * @return
	 *     The transfer's owner connection.
	 */
	public SnodesConnection getConnection()
	{
		return owner;
	}
	
	/**
	 * Returns a string representation of the file transfer, which may look
	 * like the following:<p>
	 *
	 * <pre>
	 * FileTransfer@192.168.0.2 &lt;myfile.txt&gt;
	 * </pre>
	 *
	 * @return
	 *     A string representation of the transfer object.
	 */
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder("FileTransfer");
		buf.append("@").append(owner.getHost().getHostAddress());
		buf.append(" <").append(filename).append(">");
		return new String(buf);
	}
}
