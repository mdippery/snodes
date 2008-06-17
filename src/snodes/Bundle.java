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
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Enumeration;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

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

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     * 
     * @param pckgname
     *            the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    public static List<Class> getClassesForPackage(String pckgname) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        ArrayList<File> directories = new ArrayList<File>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = pckgname.replace('.', '/');
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(path);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }
 
        ArrayList<Class> classes = new ArrayList<Class>();
        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }
}
