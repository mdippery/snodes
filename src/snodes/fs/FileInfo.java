/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2008 Chris Shake <cshake@gmail.com>
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

/**
 * Keeps all the info needed about incoming files.
 *
 * @author Chris Shake
 * @version 0.1
 */
public class FileInfo {

	private String fileName;
	private int segmentSize;
	private int numSegments;
	private boolean[] segmentStatus;

	public FileInfo(String name, int segments, int segsize){
		fileName = new String(name);
		numSegments = segments;
		segmentSize = segsize;
		segmentStatus = new boolean[numSegments]; // defaults to false
	}
	
	public String toString(){
		return fileName;
	}
	
	public int fileSegmentSize(){
		return segmentSize;
	}
	
	public int numberOfSegments(){
		return numSegments;
	}
	
	public boolean[] segmentStatusArray(){
		return segmentStatus;
	}
	
	public void segmentWritten(int segnum){
		segmentStatus[segnum] = true;
	}
	
	public boolean segmentStatus(int segnum){
		return segmentStatus[segnum];
	}
	
	public boolean fileDone(){
		for(int i=0; i<numSegments; i++){
			if(!segmentStatus[i]){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the index of the first incomplete segment.
	 *
	 * @return
	 *     The index of the first incomplete segment, or <tt>-1</tt> if all
	 *     segments are complete.
	 */
	public int firstMissingSegment(){
		for(int i=0; i<numSegments; i++){
			if(!segmentStatus[i]){
				return i;
			}
		}
		return -1;
	}
}
