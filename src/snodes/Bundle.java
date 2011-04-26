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

package snodes;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Provides a way to interact with resources in JAR files.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class Bundle
{
	/** The class loader for this bundle. */
	private ClassLoader loader;
	
	/**
	 * Creates a new instance of <tt>Bundle</tt>.
	 *
	 * @param loader
	 *     The bundle's (or program's) class loader.
	 */
	private Bundle(ClassLoader loader)
	{
		this.loader = loader;
	}
	
	/**
	 * Gets the main bundle for the program. This bundle can be used to load
	 * shared resources, such as images, for the program's use.
	 *
	 * @return
	 *     The main bundle for the Spaghetti Nodes program.
	 */
	public static Bundle getMainBundle()
	{
		return new Bundle(ClassLoader.getSystemClassLoader());
	}
	
	/**
	 * Returns the URL of the specified image file.
	 *
	 * @param image
	 *     The name of the image file, including the file extension.
	 * @return
	 *     The image file's URL.
	 * @throws FileNotFoundException
	 *     If the image file does not exist in the Spaghetti Nodes JAR.
	 */
	public URL loadImage(String image) throws FileNotFoundException
	{
		URL imageURL = loader.getResource("resources/images/" + image);
		if (imageURL != null) {
			return imageURL;
		} else {
			throw new FileNotFoundException(image);
		}
	}
}
