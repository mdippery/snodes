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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;


/**
 * Provides methods for breaking up files into chunks in preparation for
 * transmission.
 *
 * @author Chris Shake
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
@ThreadSafe
public class FileRead {
	private static final Logger logger = Logger.getLogger("snodes.fs");
	
	/** The default size of a file segment. */
	public static final int DEFAULT_SEGMENT_SIZE = 131072;  // 128k
	/** The maximum segment size, in bytes. */
	public static final int MAX_SEGMENT_SIZE = 1048576; // 1M
	
	/** The singleton instance of the class. */
	private static FileRead singleton = null;
	
	/** The maximum size of a segment. */
	@GuardedBy("segSizeLock")
	private int maxSegmentSize;
	/** A lock for {@link #maxSegmentSize}. */
	private Object segSizeLock;
	/** A map of files to random access files. */
	private Map<File, RandomAccessFile> fileMap;
	
	/**
	 * Creates new instance of <tt>FileRead</tt>
	 *
	 * This constructor is private and cannot be called outside of the class.
	 * Other classes should use {@link #getInstance} to get instances of this
	 * class instead.
	 *
	 */
	private FileRead() {
		fileMap = new HashMap<File, RandomAccessFile>();
		segSizeLock = new Object();
		maxSegmentSize = DEFAULT_SEGMENT_SIZE;

		registerFileCloseHook();
	}
	
	/**
	 * Returns the shared instance of this class.
	 *
	 * @return
	 *     An instance of <tt>FileRead</tt>.
	 */
	public static synchronized FileRead getInstance()
	{
		if (singleton == null) singleton = new FileRead();
		return singleton;
	}
	
	/**
	 * Registers a hook with the Java runtime that ensures that any files in
	 * the set of files gets closed.
	 */
	private void registerFileCloseHook()
	{
		// This probably isn't strictly necessary, since the Java VM should
		// close all open files when it exists, but it's not bad to do it
		// explicitly anyway.
		Runnable runner = new Runnable() {
			public void run() {
				closeFiles();
			}
		};
		
		Runtime.getRuntime().addShutdownHook(new Thread(runner, "File Close Hook"));
	}
	
	/** Closes any files in the file map. */
	private void closeFiles()
	{
		logger.finer("Closing random access files...");
		
		Iterator<File> iter = fileMap.keySet().iterator();
		while (iter.hasNext()) {
			File key = iter.next();
			RandomAccessFile file = fileMap.get(key);
			try {
				file.close();
			} catch (IOException e) {
				// Ignore.
			} finally {
				logger.fine("Closed random access file: " + key);
				fileMap.remove(key);
			}
		}
		
		logger.finer("Random access files closed.");
	}
	
	/**
	 * Closes a random-access file.<p>
	 *
	 * Files should be closed once they have been read completely.
	 *
	 * @param fileName
	 *     The name of the file to close.
	 * @see #readSegment
	 */
	public void closeFile(String fileName)
	{
		try {
			File keyFile = realPath(fileName);
			RandomAccessFile file = fileMap.get(keyFile);
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					// Ignore.
				} finally {
					logger.fine("Closed random access file: " + keyFile);
					fileMap.remove(keyFile);
				}
			}
		} catch (FileNotFoundException e) {
			// Ignore. There's nothing to close if the file path is invalid.
		}
	}
	
	/**
	 * Calculates the number of file segments it will take to transmit the
	 * entire file.
	 *
	 * @param fileName
	 *     String path of file to read
	 * @return
	 *     Number of segments that will be read. 0 if error.
	 * @throws FileNotFoundException
	 *     If the file cannot be found.
	 */
	public int fileSegments(String fileName) throws FileNotFoundException {
		File f = realPath(fileName);
		
		if (f == null) throw new FileNotFoundException(fileName);
		
		long size = f.length();
		return (int) Math.ceil((double)size/maxSegmentSize);
	}
	
	/**
	 * Returns specified segment of a file.
	 *
	 * @param fileName
	 *     String path of file
	 * @param segmentNumber
	 *     The segment number.
	 * @return
	 *     A byte array containing the file contents for that segment.
	 * @throws IOException
	 *     If an I/O error occurs while reading the file.
	 * @throws FileNotFoundException
	 *     If <tt>fileName</tt> does not exist, is
	 *     not readable, or is not a shared file.
	 * @throws EOFException
	 *     If the end of the file has been reached. If this exception is thrown,
	 *     the caller should probably close the file with a call to
	 *     {@link #closeFile}.
	 * @see #closeFile
	 */
	public byte[] readSegment(String fileName, int segmentNumber)
		throws IOException, FileNotFoundException, EOFException
	{
		synchronized (segSizeLock) {
			File path = realPath(fileName);
			RandomAccessFile file = fileMap.get(path);
			if (file == null) {
				// We have to open a RandomAccessFile object to get random
				// access to file contents, but this can conceivably leave a
				// lot of open file handles laying around. I have a quick hack
				// to close file handles with closeFile(), but we should try
				// to come up with a way to do this automagically -- otherwise
				// we might end up with A LOT of open file handles (maybe even
				// too many for the Java VM to handle).
				
				file = new RandomAccessFile(path, "r");
				fileMap.put(path, file);
			}
		
			byte[] contents = new byte[maxSegmentSize];
			long offset = segmentNumber * maxSegmentSize;
			int bytesRead = -1;
		
			file.seek(offset);
			bytesRead = file.read(contents);
		
			if (bytesRead == -1) {
				throw new EOFException("End of file reached");
			} else if (bytesRead == 0) {
				throw new IOException("0 bytes read");
			}
		
			if (bytesRead != maxSegmentSize) {
				byte[] returncontents = new byte[bytesRead];
			
				for(int i = 0; i < bytesRead; i++){
					returncontents[i] = contents[i];
				}
			
				contents = returncontents;
			}
		
			return contents;
		}
	}
	
	/**
	 * Returns maximum file segment size in bytes.
	 *
	 * @return
	 *     The maximum file segment size.
	 */
	public int getSegmentSize() {
		return maxSegmentSize;
	}
	
	/**
	 * Sets the maximum file segment size.
	 *
	 * @param size
	 *     The maximum segment size, in bytes. Segments larger than
	 *     {@value MAX_SEGMENT_SIZE} are not allowed.
	 * @return
	 *     The maximum segment size after the change.
	 */
	public int setSegmentSize(int size){
		synchronized (segSizeLock) {
			if (size > MAX_SEGMENT_SIZE) {
				maxSegmentSize = size;
			} else {
				logger.warning("segment size cannot be set to " + size);
			}
			return maxSegmentSize;
		}
	}
	
	/**
	 * Returns the real file pointed to by the filelist path string
	 *
	 * @param sharePath
	 *     path to the file from FileList
	 * @return
	 *     File handle to actual file.
	 * @throws FileNotFoundException
	 *     If the file cannot be found or is not a shared file.
	 */
	private File realPath(String sharePath) throws FileNotFoundException {
		final String FSEP = System.getProperty("file.separator");
		
		File fp = null;
		RootShares roots = RootShares.getInstance();
		Map<String,String> rootsMap = roots.getFolderList();
		Set<String> keys = rootsMap.keySet();
		String[] pathParts = sharePath.split(FSEP);
				
		if(!keys.contains(pathParts[0])){
			throw new FileNotFoundException("Provided path isn't in a shared directory: "+sharePath);
		}
		String path = "";
		for(final String part : pathParts){
			if(path.equals("")){
				path = rootsMap.get(part);
				if (path == null){
					throw new FileNotFoundException("Cannot find '"+part+"' in the shares.");
				}
			} else {
				path += FSEP + part;
			}
		}
		return new File(path);
	}
	
	/**
	 * Creates a clone of this instance. Clones of <tt>FileRead</tt> cannot
	 * be created, so this method <em>always</em> throws an exception. To get
	 * instances of this class, use {@link #getInstance}.
	 *
	 * @return
	 *     A copy of this instance.
	 * @throws CloneNotSupportedException
	 *     If cloning is not supported. This class does not support cloning,
	 *     so this method always throws a <tt>CloneNotSupportedException</tt>.
	 */
	protected Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException("Singleton class");
	}
}
