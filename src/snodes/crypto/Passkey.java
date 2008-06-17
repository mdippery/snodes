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

import java.io.UnsupportedEncodingException;


/**
 * A text-based key.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class Passkey implements Key
{
	/** The passkey. */
	private String key;
	
	/**
	 * Creates a new passkey from the specified string.
	 *
	 * @param key
	 *     The passkey.
	 */
	public Passkey(String key)
	{
		this.key = key;
	}
	
	/**
	 * Returns a byte representation of the key.<p>
	 *
	 * The returned key will be 32 bytes long. If it is not already 32 bytes
	 * long, it will be padded to be so. If it is more than 32 bytes long, it
	 * will be truncated.
	 *
	 * @return
	 *     A 32-byte representation of the key.
	 */
	public byte[] toByteArray()
	{
		try {
			byte[] oldKey = key.getBytes("UTF-8");
			byte[] newKey = new byte[32];
			
			for (int i = 0; i < 32; i++) {
				newKey[i] = oldKey[i % oldKey.length];
			}
			
			return newKey;
		} catch (UnsupportedEncodingException e) {
			assert false : "UTF-8 is not a valid encoding.";
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the key.
	 *
	 * @return
	 *     The key.
	 */
	@Override
	public String toString()
	{
		return key;
	}
}
