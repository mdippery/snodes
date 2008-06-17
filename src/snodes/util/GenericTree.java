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

package snodes.util;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * A generic tree. The tree is not ordered in any special way, beyond the
 * inherent hierarchical ordering of its data, and each node in the tree can
 * have any number of children.<p>
 *
 * <tt>GenericTree</tt> is perfect to use as a base class for more specialized
 * tree structures, such as binary search trees or red-black trees.<p>
 *
 * <tt>GenericTree</tt> is not inherently threadsafe.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
@NotThreadSafe
public class GenericTree<T>
{
	/** The data contained in this node. */
	protected T object;
	/** The node's children. This object will be empty if there are no children. */
	private List<GenericTree<T>> children;
	
	/**
	 * Creates a new tree containing the specified piece of data. The new tree
	 * has no children.
	 *
	 * @param object
	 *     The object contained in this node.
	 */
	public GenericTree(T object)
	{
		this.object = object;
		this.children = new ArrayList<GenericTree<T>>();
	}
	
	/**
	 * Returns an iterator for the children of this node.
	 *
	 * @return
	 *     An iterator for iterating through all of this node's children.
	 *     If there are no children, the iterator will be empty.
	 */
	public Iterator<GenericTree<T>> getChildren()
	{
		return children.iterator();
	}
	
	/**
	 * Returns <tt>true</tt> if this node is allowed to have children.
	 *
	 * @return
	 *     <tt>true</tt> if this node can have children.
	 */
	public boolean allowsChildren()
	{
		return true;
	}
	
	/**
	 * Gets the child at the specified index.
	 *
	 * @return
	 *    The child at the specified index.
	 * @throws IndexOutOfBoundsException
	 *    If there is no child at the given index.
	 */
	public GenericTree<T> getChildAt(int i) throws IndexOutOfBoundsException
	{
		return children.get(i);
	}
	
	/**
	 * Returns the number of children.
	 *
	 * @return
	 *     The number of this node's children.
	 */
	public int getChildCount()
	{
		return children.size();
	}
	
	/**
	 * Returns the index of the specified child. If <tt>child</tt> is not
	 * a child of this node, then a <tt>NoSuchElementException</tt> is
	 * thrown.
	 *
	 * @return
	 *     The index of the specified child.
	 * @throws NoSuchElementException
	 *     If the specified node is not a child of this node.
	 */
	public int indexOf(GenericTree<T> child) throws NoSuchElementException
	{
		if (child == null) throw new NoSuchElementException("null");
		
		int idx = children.indexOf(child);
		if (idx > -1) {
			return idx;
		} else {
			throw new NoSuchElementException(child.toString());
		}
	}
	
	/**
	 * Returns the data object contained in this node.
	 *
	 * @return
	 *     This node's data object. The object may be <tt>null</tt>.
	 */
	public T getObject()
	{
		return object;
	}
	
	/**
	 * Sets this node's data object.
	 *
	 * @param object
	 *     The data object.
	 */
	public void setObject(T object)
	{
		this.object = object;
	}
	
	/**
	 * Adds the specified node to the tree. The node will be a child of this
	 * node and reside in the first available slot.
	 *
	 * @param node
	 *     The node (or tree) to add as a child of this one.
	 */
	public void insert(GenericTree<T> node)
	{
		children.add(node);
	}
	
	/**
	 * Adds the specified node to the tree at the desired index. The greatest
	 * index is equal to <tt>children.size()</tt>; any greater index will
	 * result in the node being added to the last available slot. An index
	 * less than 0 will result in the node being added to the first slot.
	 *
	 * @param node
	 *     The node (or tree) to add to this tree.
	 * @param index
	 *     The location in the list of children where this tree should be added.
	 */
	public void insert(GenericTree<T> node, int index)
	{
		int idx = index;
		if (index > children.size()) {
			idx = children.size();
		} else if (index < 0) {
			idx = 0;
		}
		children.add(idx, node);
	}
	
	/**
	 * Returns <tt>true</tt> if the node has no children.
	 *
	 * @return
	 *     <tt>true</tt> if the node is a leaf node.
	 */
	public boolean isLeaf()
	{
		return (children.size() == 0);
	}
	
	/**
	 * Returns <tt>true</tt> if the specified node is a child of this node.
	 *
	 * @param node
	 *     The node.
	 * @return
	 *     <tt>true</tt> if <tt>node</tt> is a child of this node.
	 */
	public boolean hasChild(GenericTree<T> node)
	{
		return children.contains(node);
	}
	
	/**
	 * Returns the string representation of the node's data object.
	 *
	 * @return
	 *     The string representation of the node's data object. This is the
	 *     same as calling <tt>String.valueOf({@link #object})</tt>.
	 */
	@Override
	public String toString()
	{
		return String.valueOf(object);
	}
}
