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


/**
 * Processes incoming packets.<p>
 *
 * A trivial listener that merely prints connection info to the screen every time
 * a packet arrives might be implemented using the following:<p>
 *
 * <pre>
 * PacketListener pl = new PacketListener() {
 *     public void processPacket(SnodesConnection conn, Packet packet) {
 *         System.err.println("Packet: " + packet.getType());
 *         System.err.println("Connection: " + conn);
 *         System.err.println();
 *     }
 * };
 * </pre>
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public interface PacketListener
{
	/**
	 * Handles an incoming packet.
	 *
	 * @param conn
	 *     The connection on which the packet event occurred.
	 * @param packet
	 *     The packet.
	 */
	void processPacket(SnodesConnection conn, Packet packet);
}
