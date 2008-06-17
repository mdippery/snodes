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

package snodes.fs;

import java.io.File;
import java.io.IOException;
import java.awt.Image;
import java.awt.Toolkit;


/**
 * A set of utilities for interacting with the standard directory structure
 * in a platform-independent way. This class provides a number of static
 * methods to finding standard paths to application data directories, temporary
 * file storage locations, and preference files.<p>
 *
 * <tt>PathManager</tt> is a singleton class; use {@link #getManager} to get
 * instances of this class.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @author Michael Schoonmaker
 * @version 0.1
 */
public final class PathManager
{
	/** The singleton instance of the class. */
	private static PathManager singleton = null;
	
	/** The cached data directory. */
	private File dataDirectory;
	
	/** Creates a new <tt>PathManager</tt>. */
	private PathManager()
	{
		dataDirectory = null;
	}
	
	/**
	 * Gets the singleton instance of the path manager.
	 *
	 * @return The path manager.
	 */
	public static synchronized PathManager getManager()
	{
		if (singleton == null) singleton = new PathManager();
		return singleton;
	}
	
	/**
	 * Returns a file reference representing a path to a directory in which
	 * application data, such as cache files, can be stored.<p>
	 *
	 * The returned file object can be prepended to a data file, like so:<p>
	 *
	 * <pre>
	 * File dataDir = PathUtils.getDataDirectory();
	 * File data = new File(dataDir, "data.dat");
	 * </pre>
	 *
	 * @return A path to a directory in which application data can be stored.
	 */
	public File getDataDirectory()
	{
		if (dataDirectory == null) {
			final String OS = System.getProperty("os.name");
			final String HOME = System.getProperty("user.home");
			final String FSEP = System.getProperty("file.separator");
			final String TEST = "test.txt";
		
			File tmpFile = null;
			String path = HOME;
			boolean createdTestFile = false;
		
			// The goal is to get the path to a part of the file system in which
			// application data can be stored. Start first by constructing the
			// path to standard locations.
			if (OS.equals("Mac OS X")) {
				path += FSEP + "Library";
				path += FSEP + "Application Support";
				path += FSEP + "Spaghetti Nodes";
			} else if (OS.startsWith("Windows")) {
				path += FSEP + "Application Data";
				path += FSEP + "Spaghetti Nodes";
			} else {
				// Most other platforms are likely just UNIX-like platforms.
				// Warning: This isn't true of Mac OS 9 and below, but is anyone
				// actually using that platform anymore?
				path += FSEP + ".snodes";
			}
		
			// We should now have a directory in which to store app data. Check to
			// make sure it exists; if it doesn't, create it.
			tmpFile = new File(path);
			if (!tmpFile.exists()) {
				boolean success = tmpFile.mkdir();
				if (!success) {
					System.err.println("Could not create directory: " + tmpFile);
					// Fall back to using the local directory
					path = System.getProperty("user.dir");
				}
			}
		
			// At this point, we should have created a directory to store our
			// application data. Let's test if we have read/write access.
			tmpFile = new File(path, TEST);
			try {
				createdTestFile = tmpFile.createNewFile();
			} catch (IOException e) {
				createdTestFile = false;
			}
		
			if (!createdTestFile) {
				System.err.println("Cannot write to directory: " + path);
				// Fall back to using local directory
				path = System.getProperty("user.dir");
			} else {
				// Delete the temporary test file
				tmpFile.delete();
			}
		
			dataDirectory = new File(path);
		}
		
		return dataDirectory;
	}
	
	/**
	 * Returns the application's plugins directory. Any plugin stored in this
	 * directory will be loaded at startup.
	 *
	 * @return The application plugin directory.
	 */
	public File getPluginDirectory()
	{
		return new File(getDataDirectory(), "Plugins");
	}
}
