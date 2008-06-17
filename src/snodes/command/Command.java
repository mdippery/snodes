/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
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

package snodes.command;

/**
 * Interface to allow for proper handling by {@link snodes.gui.GUIController#actionPerformed}.<p>
 *
 * In order to be properly handled, use as follows:<p>
 *
 * <ol>
 *   <li>Create a new class, and implement the <tt>Command</tt> interface.</li>
 *   <li>
 *     <p>Place the class in the proper package:</p>
 *     <blockquote>
 *       The command should be of the form <tt>&lt;Type&gt;.&lt;Command&gt;</tt>, so
 *       "System.Quit" corresponds to the <tt>Quit</tt> class within the
 *       <tt>snodes.command.System package.</tt>
 *     </blockquote>
 *   </li>
 *   <li>Place any neccessary instantiation code in the constructor.</li>
 *   <li>Place the code to be run in {@link #run run}.</li>
 * </ol>
 *
 * <em><strong>IMPORTANT NOTE:</strong> The command may contain more than
 * <tt>&lt;Type&gt;.&lt;Command&gt</tt>;&mdash;for example, "Connection.Add.&lt;address&gt;".
 * Only <tt>Connection.Add</tt> will be used to parse the command, but the full
 * command will be passed to run. Use the string to parse the argument.</em>
 *
 * @author Michael Schoonmaker
 * @version 0.1
 */
public interface Command {
	/**
	 * The method to be run by the child class. The constructor is not used to run this
	 * code to ensure the class is intended as a command. Otherwise, any class could be
	 * passed as an <tt>ActionEvent</tt> command.
	 *
	 * @param actionCommand
	 *	The command passed to the ActionEvent. See note in class desctription.
	 */
	public void run(String actionCommand);
}