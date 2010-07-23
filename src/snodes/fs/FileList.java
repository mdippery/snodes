/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2008 Chris Shake <cshake@gmail.com>
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

import snodes.util.GenericTree;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Maintains a list of shared files.
 *
 * @author Chris Shake
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class FileList {
	private static final Logger logger = Logger.getLogger("snodes.fs");
	
	private GenericTree<File> root;
	private File shareName;
	private Object treeLock;
	private Map<String,String> sharedDirs;
	
	/**
	 * Creates a new instance of <tt>FileList</tt>.
	 *
	 * @param name The root of the shared file list.
	 */
	public FileList(String name) {
		shareName = new File(name);
		root = new GenericTree<File>(shareName);
		treeLock = new Object();
	}
	
	/**
	 * Creates the file list tree.
	 *
	 * @param shares The list of file shares.
	 */
	public void createTree(Map<String,String> shares) {
		if (shares == null) {
			logger.warning("Cannot create tree: shares is null");
		}
		
		// Read in file structures
		Iterator<String> it = shares.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String path=shares.get(key);
			createShare(key,path);
		}
		sharedDirs = shares;
	}
	
	/**
	 * Adds a shared folder to the file listing.
	 *
	 * @param alias The folder's alias.
	 * @param path The full path to the folder.
	 */
	private void createShare(String alias, String path) {
		File shareRoot = new File(path);
		if (shareRoot.isDirectory()){
			addChildren(root,shareRoot);
		} else {
			logger.warning("'" + path + "' is not a valid folder to share");
		}
	}
	
	/**
	 * A helper method that recursively adds directories and files to the tree.
	 *
	 * @param node The current node.
	 * @param file The directory.
	 */
	private void addChildren(GenericTree<File> node, File file) {
		if(file.exists()){
			if(file.isFile()){
				// return case
				GenericTree<File> tempTreeNode = new GenericTree<File>(file);
				synchronized (treeLock) {
					node.insert(tempTreeNode);
				}
				return;
			} else if(file.isDirectory()) {
				// recurse entire directory
				GenericTree<File> tempTreeNode = new GenericTree<File>(file);
				synchronized (treeLock) {
					node.insert(tempTreeNode);
				}
				File[] dirContents = file.listFiles();
				for(final File subfile : dirContents) {
					if(!subfile.isHidden()){
						addChildren(tempTreeNode,subfile);
					}
				}
			} else {
				assert false : (file + " is neither a file nor a directory");
			}
		} else {
			logger.warning("Unable to add '" + file + "' to the tree");
		}
	}
	
	/**
	 * Returns the XML representation of the file listing.
	 *
	 * @return An XML representation of the file listing.
	 */
	@Override
	public String toString()
	{
		return toXML();
	}
	
	/**
	 * Returns an XML representation of the file listing. The returned string
	 * can then be written to a file for storage.<p>
	 *
	 * The returned XML is designed for readability and thus has proper spacing,
	 * tabs, and so forth.
	 *
	 * @return An XML representation of the file listing.
	 */
	public String toXML()
	{
		StringBuffer buf = new StringBuffer();
		toXML(root, buf, 0, "", "");    // XXX mpd - Not sure if the last two parameters are right
		return new String(buf);
	}
	
	/**
	 * A helper method for {@link #toXML()} that recursively builds an XML
	 * string representation of the file listing.
	 *
	 * @param node The current node.
	 * @param buf The <tt>StringBuffer</tt> to which the string is written.
	 * @param lvl The current indentation level.
	 * @param alias The base share alias for hte current branches
	 * @param path The path to the base of the share for current branches
	 */
	private void toXML(GenericTree<File> node, StringBuffer buf, int lvl, String alias, String path)
	{
		if (node == null) return;
		
		boolean base = false;
		if (alias == "" || path == "") base = true;
		
		final String NL = System.getProperty("line.separator");
		File fnode = node.getObject();
		String indent = new String();
		
		for (int i = 0; i < lvl; i++) {
			indent += "    ";
		}
		
		if (fnode.isFile()) {
			// Write <File>
			buf.append(indent);
			buf.append("<File name=\"");
			buf.append(fnode.getName());
			buf.append("\" path=\"");
			buf.append(fnode.getParent().replace(path,alias));
			buf.append("\" size=\"");
			buf.append(fnode.length());
			buf.append("\" />").append(NL);
		} else {
			Iterator<GenericTree<File>> children = null;
			
			// Write <Directory>
			assert fnode.isDirectory() : (fnode + " is neither a file nor a directory");
			buf.append(indent);
			buf.append("<Directory name=\"");
			buf.append(fnode.getName());
			if(!base) {
				buf.append("\" path=\"");
				if(!fnode.toString().equals(path)){
					buf.append(fnode.getParent().replace(path,alias));
				}
			} else {
				buf.append("\" path=\"");
				// XXX mpd - The first node can give a parent of null, so just print
				// an empty string in such a case. This is a ugly hack and
				// an ugly way of outputting XML, but it works for now.
				if (fnode.getParent() != null) {
					buf.append(fnode.getParent());
				} else {
					buf.append("");
				}
			}
			buf.append("\" >");
			buf.append(NL);
			
			// Write each <File>
			children = node.getChildren();
			while (children.hasNext()) {
				if(base) {
					GenericTree<File> sharefile = children.next();
					Set<String> keys = sharedDirs.keySet();
					String sharepath = sharefile.toString();
					String sharealias = "";
					String temppath = "";
					for(final String key: keys){
						temppath = sharedDirs.get(key);
						if(temppath.equals(sharepath)){
							sharealias=key;
						}
					}
					toXML(sharefile, buf, lvl+1, sharealias, sharepath);
				} else {
					toXML(children.next(), buf, lvl+1, alias, path);
				}
			}
			
			// Write </Directory>
			buf.append(indent);
			buf.append("</Directory>");
			buf.append(NL);
		}
	}
}
