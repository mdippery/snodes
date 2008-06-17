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

import java.net.InetAddress;


/**
 * Responsible for tracking connections for the program. The manager creates a
 * map of connections to hosts, which allows classes (such as {@link SnodesServer})
 * to query the manager for connection information.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @author Michael Schoonmaker
 * @version 0.1
 */
public interface ConnectionManager
{
	/**
	 * Returns the connection object for the given address, or <tt>null</tt> if
	 * no such connection has been established.
	 *
	 * @param host
	 *     The address.
	 * @return
	 *     The connection for the host, or <tt>null</tt> if none exists.
	 */
	SnodesConnection getConnection(InetAddress host);
}
