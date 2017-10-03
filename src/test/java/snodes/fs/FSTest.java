//
//  fstest.java
//  snodes
//
//  Created by Christopher Shake on 10/26/07.
//

import snodes.fs.*;

import java.io.*;
import java.nio.*;
import java.util.*;

public class FSTest {

	private FSTest() {}
	
    public static void main(String[] args) {
	    try{
			// Create shares for local movies
			RootShares inst = RootShares.getInstance();
			inst.clearShares();
			inst.addFolder("Movies","/Users/Chris/Movies");
			FileList lister = new FileList("Osiris");
			lister.createTree(inst.getFolderList());
			File datadir = new File("data");
			if(!datadir.exists()){
				datadir.mkdir();
			}
			BufferedWriter fileout = new BufferedWriter( new FileWriter("data/filelist.xml"));
			fileout.write(lister.toXML());
			fileout.close();
			
			
			// Read in a shared file and copy it
			FileRead freadinst=FileRead.getInstance();
			String filetocopy = "Movies/Gnuplot-4.0.0.dmg";
			int segsize = freadinst.getSegmentSize();
			int numsegs = freadinst.fileSegments(filetocopy);
			System.out.println("filetocopy = "+filetocopy);
			System.out.println("segsize = "+segsize);
			System.out.println("numsegs = "+numsegs);
			
			String endfile = "Gnuplot-4.0.0.dmg";
			FileWrite fwriteinst=FileWrite.getInstance();
			fwriteinst.readyFile(endfile,segsize,numsegs);
			int curseg=0;
			
			for(int i=0; i<numsegs; i++){
				if(Math.random() >= 0.95){
					System.out.println("dropping segment "+i);
				} else {
					fwriteinst.writeSegment(endfile,freadinst.readSegment(filetocopy,i),i);
				}
			}
			
			System.out.println("going through and checking that file was completely written");
			while(!fwriteinst.isFileDone(endfile)){
				curseg = fwriteinst.nextSegmentNeeded(endfile);
				
				fwriteinst.writeSegment(endfile,freadinst.readSegment(filetocopy,curseg),curseg);
				
				System.out.println(curseg+"/"+numsegs);
			}
			
			
		} catch (IOException e) {
			System.err.println("Error: "+e.getMessage());
		}
	}
}