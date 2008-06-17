/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2008 Michael Dippery <mdippery@bucknell.edu>
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

package snodes.crypto;

import java.util.Arrays;


/**
 * A byte-based key used in symmetric ciphers.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class EncryptionKey implements Key
{
	/** The key. */
	private byte[] key;
	
	/**
	 * Creates a new encryption key.
	 *
	 * @param bytes
	 *     The key's bytes.
	 */
	public EncryptionKey(byte[] bytes)
	{
		key = bytes;
	}
	
	/**
	 * Returns a byte representation of the key.
	 *
	 * @return
	 *     A byte representation of the key.
	 */
	public byte[] toByteArray()
	{
		return key;
	}
	
	/**
	 * Returns a string representation of the key. The string returned is
	 * identical to the one as returned by <tt>Arrays.toString(this.toByteArray())</tt>.
	 *
	 * @return
	 *     A string representation of the key's bytes.
	 */
	@Override
	public String toString()
	{
		return Arrays.toString(key);
	}
}
