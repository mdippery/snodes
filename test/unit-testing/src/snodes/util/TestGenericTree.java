/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * TestGenericTree
 * Spaghetti Nodes Unit Testing
 * Author: Michael Dippery <mdippery@bucknell.edu>
 */

package snodes.util;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class TestGenericTree
{
	private GenericTree<String> tree;
	
	@Before
	public void initTree()
	{
		GenericTree<String> tree0 = new GenericTree<String>("Child 0");
		GenericTree<String> tree1 = new GenericTree<String>("Child 1");
		GenericTree<String> tree00 = new GenericTree<String>("Child 00");
		tree0.insert(tree00);
		
		tree = new GenericTree<String>("Root");
		tree.insert(tree0);
		tree.insert(tree1);
	}
	
	// Tests getting children
	@Test
	public void testGetChildren()
	{
		Iterator<GenericTree<String>> iter = tree.getChildren();
		boolean gotChildren = false;
		
		//System.out.println(tree);
		while (iter.hasNext()) {
			GenericTree<String> subtree = iter.next();
			//System.out.println(subtree);
			gotChildren = true;
		}
		
		assertTrue("no children", gotChildren);
	}
	
	// Tests getting a specific child
	@Test
	public void testGetChildAt()
	{
		GenericTree<String> child1 = tree.getChildAt(1);
		assertTrue("childAt(1) != child1", child1.getObject().equals("Child 1"));
	}
	
	// Tests counting children
	@Test
	public void testChildCount()
	{
		assertTrue("childCount != 2", tree.getChildCount() == 2);
	}
	
	// Tests returning the index of a tree
	@Test
	public void testIndexOf()
	{
		GenericTree<String> child1 = new GenericTree<String>("Child 1");
		tree.insert(child1, 1);	
		int child1idx = tree.indexOf(child1);
		assertTrue("child index != 1", child1idx == 1);
	}
	
	// Tests getting the object
	@Test
	public void testGetObject()
	{
		assertTrue(tree.getObject().equals("Root"));
	}
	
	// Tests setting an object
	@Test
	public void testSetObject()
	{
		GenericTree<String> root = new GenericTree<String>("root");
		//System.out.println(root);
		root.setObject("not root");
		assertTrue("object not changed", root.getObject().equals("not root"));
	}
	
	// Test inserting a tree
	@Test
	public void testInsert()
	{
		GenericTree<String> inserted = new GenericTree<String>("inserted");
		GenericTree<String> first = new GenericTree<String>("first");
		tree.insert(inserted);
		tree.indexOf(inserted);
		tree.insert(first, 0);
		assertTrue("first index != 0", tree.indexOf(first) == 0);
	}
	
	// Test leafs
	@Test
	public void testIsLeaf()
	{
		GenericTree<String> leaf = new GenericTree<String>("Leaf");
		tree.insert(leaf);
		assertFalse("Root is leaf", tree.isLeaf());
		assertTrue("Leaf is leaf", leaf.isLeaf());
	}
	
	// Test has child
	@Test
	public void testHasChild()
	{
		GenericTree<String> child = new GenericTree<String>("Child");
		tree.insert(child);
		assertTrue("child not a child", tree.hasChild(child));
	}
}
