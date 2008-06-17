/* -*-    indent-tabs-mode:t; tab-width:4; c-basic-offset:4    -*- */
/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2007 Michael Dippery <mdippery@bucknell.edu>
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

package snodes.command.Connection;

import snodes.command.Command;
import snodes.gui.GUIController;
import snodes.net.SnodesConnection;

import java.util.logging.Logger;


/**
 * The Connection.Add command class.
 *
 * @author Michael Schoonmaker
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class Add implements Command {

	/** The class's logger. */
	private static final Logger logger = Logger.getLogger("snodes.command.Connection");

	/**
	 * Creates the instance. No instantiation needed.
	 *
	 */
	public Add() {}

	/**
	 * Creates a new connection via dialog box.
	 *
	 * @param actionCommand
	 *     The full ActionEvent command.
	 */
	public void run(String actionCommand) {
		String address = GUIController.getInstance().showTextDialog("Enter the user's IP Address:", "IP Address");
		String passkey = GUIController.getInstance().showTextDialog("Enter the secret:", "Secret");
		
		if (address != null && passkey != null) { // Did the user cancel the dialog?
			SnodesConnection conn = GUIController.getInstance().addConnection(address);
			
			conn.authenticate(passkey);
			logger.finest("Connection.Add creating connection: " + conn);
			GUIController.getInstance().connect(conn);
			GUIController.getInstance().refreshAll();
		}
	}
}
