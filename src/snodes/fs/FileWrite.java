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

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides methods for reassembling file chunks into cohesive files.
 *
 * @author Chris Shake
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
@ThreadSafe
public class FileWrite {
	/** The singleton instance of the class. */
	private static FileWrite singleton = null;
	
	/** The downloads directory. */
	private File saveDir;
	/** The list of current file handles. */
	private Map<File,FileInfo> currentHandles;
	
	/**
	 * Creates new instance of <tt>FileWrite</tt>.
	 *
	 * This constructor is private and cannot be called outside of the class.
	 * Other classes should use {@link #getInstance} to get instances of this
	 * class instead.
	 */
	private FileWrite() {
		// Get save directory from some utility
		saveDir = PathManager.getManager().getDataDirectory();
		currentHandles = new ConcurrentHashMap<File, FileInfo>();
	}
	
	/**
	 * Returns the shared instance of this class.
	 *
	 * @return
	 *     An instance of <tt>FileWrite</tt>.
	 */
	@GuardedBy("this")
	public static synchronized FileWrite getInstance()
	{
		if (singleton == null) singleton = new FileWrite();
		return singleton;
	}
	
	/**
	 * Sets up the instance to accept a new incoming file
	 *
	 * @param fileName
	 *     The name of the file.
	 * @param segSize
	 *     The size of a file segment.
	 * @param numSegments
	 *     The number of segments the file will be broken into.
	 * @throws IOException
	 *     If an I/O error occurs.
	 */
	public void readyFile(String fileName, int segSize, int numSegments) throws IOException
	{
		File saveFile = new File(saveDir, fileName);
		saveFile.createNewFile(); // This currently doesn't allow resuming incomplete files
		FileInfo saveInfo = new FileInfo(fileName, numSegments, segSize);
		currentHandles.put(saveFile,saveInfo);
	}
	
	/**
	 * Returns next segment needed
	 * If file is done, it will return -1 to indicate none are needed.
	 *
	 * @param fileName
	 *		incoming file name
	 */
	public int nextSegmentNeeded(String fileName){
		File save = new File(saveDir, fileName);
		FileInfo saveInfo = currentHandles.get(save);
		
		if(saveInfo.fileDone()){
			return -1;
		}
		
		boolean[] segStatus = saveInfo.segmentStatusArray();
		int numSegs = saveInfo.numberOfSegments();
		
		for(int i=0; i<numSegs; i++){
			if(!segStatus[i]){
				return i;
			}
		}
		return -2;
	}
	
	/**
	 * Check if a file is fully written
	 *
	 * @param fileName
	 *		incoming file name
	 */
	public boolean isFileDone(String fileName){
		File save = new File(saveDir, fileName);
		FileInfo saveInfo = currentHandles.get(save);
		return saveInfo.fileDone();
	}
	
	/**
	 * Writes a chunk of incoming data to a file.
	 *
	 * @param fileName
	 *     The place to save incoming file.
	 * @param data
	 *     The byte stream to be written.
	 * @param segmentNum
	 *     The segment number.
	 * @throws IOException
	 *     If an I/O error occurs.
	 */
	public void writeSegment(String fileName, byte[] data, int segmentNum) throws IOException {
		File save = new File(saveDir, fileName);
		FileInfo saveInfo =	currentHandles.get(save);
		
		if (saveInfo == null) {
			throw new IOException("File not initialized for writing");
		}
		
		if (saveInfo.segmentStatus(segmentNum)) {
			throw new IOException("Current segment already written");
		}
		
		if (data.length != saveInfo.fileSegmentSize() && segmentNum < saveInfo.numberOfSegments()-1) {
			throw new IOException("Segment is wrong size");
		}
		
		long offset = (long) segmentNum * saveInfo.fileSegmentSize();
		RandomAccessFile fout = new RandomAccessFile(save,"rw");
		
		fout.seek(offset);
		fout.write(data);
		saveInfo.segmentWritten(segmentNum);
		fout.close();
	}
	
	/**
	 * Creates a clone of this instance. Clones of <tt>FileWrite</tt> cannot
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
