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

package snodes.command.Search;

import snodes.command.Command;
import snodes.gui.GUIController;
import snodes.net.SnodesConnection;

import javax.swing.JOptionPane;

/**
 * The Search.Public command class.
 *
 * @author Michael Schoonmaker
 * @version 0.1
 */
public class Public implements Command {
	/**
	 * Creates the instance. No instantiation needed.
	 *
	 */
	public Public() {}
	
	/**
	 * Instantiates a new public search via dialog box.
	 *
	 * @param actionCommand
	 *	The full ActionEvent command.
	 */
	public void run(String actionCommand) {
	}
}
