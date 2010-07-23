/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2008 Chris Shake <cshake@gmail.com>
 * Copyright (c) 2007-2008 Michael Dippery <mdippery@bucknell.edu>
 * Copyright (c) 2007-2008 Chris Kenna <ckenna@bucknell.edu>
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

import net.jcip.annotations.GuardedBy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Maintains the list of shared folders.<p>
 *
 * Since this class interacts intimately with the file system, it is implemented
 * as a singleton class to avoid synchronization issues. Thus, there is only ever
 * one instance of it created. Since there is no public constructor, other classes
 * should use {@link #getInstance} to get the singleton instance of the class.<p>
 *
 * It seems that the <tt>sharedFolders</tt> and cache file objects are what
 * would be the cause of any synchronization problems in a multi-threaded
 * application of this class; therefore, they have been protected with intrinsic
 * locks. Be sure that when you need to acquire both locks, you acquire the lock on
 * the cache <em>first</em>, and <em>then</em> the lock on <tt>sharedFolders</tt>,
 * or the possibility for deadlock arrises!
 *
 * @author Chris Shake
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @author Christopher Kenna
 * @version 0.1
 */
public class RootShares {
	private static final Logger logger = Logger.getLogger("snodes.fs");
	
	/** The single shared instance of the class. */
	private static RootShares singleton = null;
	
	/** The map of aliases to corresponding paths. */
	@GuardedBy("sharedFoldersLock")
	private Map<String, String> sharedFolders;
	/** A cached immutable map of the shared folders. */
	private Map<String, String> unmodifiableSharedFolders;
	/** The location of the cache file. */
	@GuardedBy("cacheFileLock")
	private File cacheFile;
	/** Cache file lock. */
	private Object cacheFileLock;
	/** Hash map lock. */
	private Object sharedFoldersLock;
	
	/**
	 * Returns the shared instance of this class. <tt>RootShares</tt> is
	 * implemented as a singleton class, primarily so it can easily control
	 * access to the data file it uses to store shared folder paths. There is
	 * no public constructor, so classes should use this method to get instances
	 * of <tt>RootShares</tt>, instead of creating their own objects.
	 *
	 * @return An instance of <tt>RootShares</tt>.
	 */
	@GuardedBy("this")
	public static synchronized RootShares getInstance()
	{
		if (singleton == null) singleton = new RootShares();
		return singleton;
	}
	
	/**
	 * Creates a new instance of <tt>RootShares</tt>. This constructor checks
	 * for a pre-existing cache of shared files and loads previous shares,
	 * or creates new blank shares.<p>
	 *
	 * This constructor is private and cannot be called outside of the class.
	 * Other classes should use {@link #getInstance} to get instances of this
	 * class instead.
	 */
	protected RootShares() {
		cacheFile = new File(PathManager.getManager().getDataDirectory(), "rootdatafile.dat");
		cacheFileLock = new Object();
		sharedFoldersLock = new Object();
		unmodifiableSharedFolders = null;
		if(fileExists()) {
			readRootFolders();
		} else {
			synchronized(sharedFoldersLock){
				sharedFolders = new HashMap<String, String>();
			}
			writeRootFolders();
		}
	}
	
	/**
	 * Adds a new folder to the shares.
	 *
	 * @param alias
	 *     The folder's alias.
	 * @param path
	 *     The path to the folder.
	 * @throws IllegalArgumentException
	 *     If <tt>alias</tt> is already the name of a shared folder, is already 
	 * associated with another share, or <tt>alias</tt> already exists.
	 */
	// I don't see a reason to distinguish in the way we were. If there is one,
	//		feel free to put IOException back...
	// TODO: Implement folder validation
	public void addFolder(String alias, String path) throws IllegalArgumentException {		
		if(path.endsWith("/") || path.endsWith("\\")) {
			path = path.substring(0,path.length()-1);
		} // quick hack to take off trailing slashes, should do something better later
		
		synchronized(sharedFoldersLock){
			for(final String key : sharedFolders.keySet()){
				String temppath = sharedFolders.get(key);
				if (temppath.contains(path)) {
					throw new IllegalArgumentException(alias + " is already shared as part of " + temppath);
				} else if (alias.equals(key)) {
					throw new IllegalArgumentException(alias + " already exists");
				}
			}
			sharedFolders.put(alias,path);
		}
		
		writeRootFolders();
	}
	
	/**
	 * Takes a folder out of the shares.
	 *
	 * @return <tt>true</tt> if successful, <tt>false</tt> if something fails.
	 */
	public boolean removeFolder(String alias) {
		synchronized(sharedFoldersLock){
			Set<String> keys = sharedFolders.keySet();
			String temppath = null;

			if(alias == null || keys.isEmpty())
				return false;
			
			for(final String key: keys){
				temppath=sharedFolders.get(key);
				if(alias.equals(key) || alias.equals(temppath)) {
					if(sharedFolders.remove(key) == null){
						System.err.println("snodes Error: " + alias + " couldn't be removed");
					} else {
						writeRootFolders();
						return true;
					}
				}
			}
			logger.warning("Couldn't find '" + alias + "' in shares");
			return false;
		}
	}
	
	/** Clears all the shares. */
	public void clearShares() {
		synchronized(sharedFoldersLock){
			sharedFolders.clear();
		}
		
		writeRootFolders();
	}
	
	/**
	 * Returns a read-only copy of the shared folder list. To modify the list,
	 * use {@link #addFolder(String,String)} and {@link #removeFolder(String)}.
	 *
	 * @return
	 *     A read-only copy of the shared folder list.
	 */
	public Map<String, String> getFolderList() {
		if (unmodifiableSharedFolders == null) {
			unmodifiableSharedFolders = Collections.unmodifiableMap(sharedFolders);
		}
		return unmodifiableSharedFolders;
	}
	
	/**
	 * Checks if a cache file exists for shared folders.
	 *
	 * @return <tt>true</tt> if the file exists.
	 */
	private boolean fileExists() {
		return cacheFile.exists();
	}
	
	/**
	 * Loads the cache file of shared folders into memory.<p>
	 *
	 * Aliases and pathnames are stored in the format:<p>
	 *
	 * <blockquote><p><code>~/path/to/file<i>\t</i>alias</code></p></blockquote>
	 */
	private void readRootFolders() {
		BufferedReader in = null;
		String line = null;
		
		if (sharedFolders == null) {
			synchronized(sharedFoldersLock){
				sharedFolders = new HashMap<String, String>();
			}
		}
		
		try {
			synchronized(cacheFileLock){
				in = new BufferedReader(new FileReader(cacheFile));
				
				synchronized(sharedFoldersLock){
					while ((line = in.readLine()) != null) {
						String[] pathParts = line.split("\t");
						String path = pathParts[0];
						String alias = pathParts[1];
						
						addFolder(alias, path);
						//sharedFolders.put(alias, path);
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "Could not open cache file", e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error reading cache file", e);
		}
	}
	
	/**
	 * Writes the current copy of the shared folder list to a file.<p>
	 *
	 * Aliases and pathnames are stored in the format:<p>
	 *
	 * <blockquote><p><code>~/path/to/file\talias</code></p></blockquote>
	 */
	private void writeRootFolders() {
		BufferedWriter out = null;
		
		synchronized(cacheFileLock){
			try {
				out = new BufferedWriter(new FileWriter(cacheFile));
				
				synchronized(sharedFoldersLock){
					for (final String key : sharedFolders.keySet()) {
						out.write(sharedFolders.get(key) + "\t" + key + "\n");
					}
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Error writing cache file", e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// Ignore.
					}
				}
			}
		} // end sync. bracket
	}
	
	/**
	 * Returns a string representation of the shared folders list. This is
	 * a listing of each item in the list.
	 *
	 * @return A string representation of the folders list.
	 */
	@Override
	public String toString()
	{
		final String NL = System.getProperty("line.separator");
		StringBuilder buf = new StringBuilder();
		String retStr = null;
		buf.append("--- Shares ---").append(NL);
		
		synchronized (sharedFoldersLock) {
			Set<String> keys = sharedFolders.keySet();
			
			for (final String key : keys) {
				buf.append(key).append(" : ");
				buf.append(sharedFolders.get(key)).append(NL);
			}
		}
		
		// Trim off the trailing new line
		retStr = new String(buf);
		return retStr.substring(0, retStr.length()-1);
	}
	
	/**
	 * Creates a clone of this instance. Clones of <tt>RootShares</tt> cannot
	 * be created, so this method <em>always</em> throws an exception. To get
	 * instances of this class, use {@link #getInstance}.
	 *
	 * @return A copy of this instance.
	 * @throws CloneNotSupportedException If cloning is not supported. This
	 *	   class does not support cloning, so this method always throws a
	 *	   <tt>CloneNotSupportedException</tt>.
	 */
	protected Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException("Singleton class");
	}
}
