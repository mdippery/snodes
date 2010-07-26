/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * TestPathUtils
 * Spaghetti Nodes Unit Testing
 * Author: Michael Dippery <mdippery@bucknell.edu>
 */

package snodes.fs;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestPathManager
{
	// Tests PathUtils.getDataDirectory to make sure it always returns the same name
	@Test
	public void testDataDirectory()
	{
		File path1 = PathManager.getManager().getDataDirectory();
		File path2 = PathManager.getManager().getDataDirectory();
		
		assertNotNull("path1 is null", path1);
		assertNotNull("path2 is null", path2);
		assertEquals("paths are not equal", path1, path2);
		//System.out.println("Data directory is " + path1);
	}
}
