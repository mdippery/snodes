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

package snodes.gui;

import snodes.net.SnodesConnection;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * The panel containing all of the current connections,
 * as well as a "Add Connection" button.
 *
 * @author Michael Schoonmaker
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class ConnectionsPanel extends JPanel {
	
	/** The class's logger. */
	private static final Logger logger = Logger.getLogger("snodes.gui");
	
	/** The panel containing all of the current connections. */
	private JPanel connections;
	/** The shared ActionListener. */
	private ActionListener listener;
	
	/** Creates a new connections panel. */
	public ConnectionsPanel(ActionListener listener) {
		super(new BorderLayout());

		this.listener = listener;

		connections = new JPanel();
		connections.setLayout(new BoxLayout(connections, BoxLayout.Y_AXIS));
		
		JButton newConnectionButton = new JButton();
		newConnectionButton.setText("Add Connection");
		newConnectionButton.setActionCommand("Connection.Add");
		newConnectionButton.addActionListener(listener);
		
		add(connections, BorderLayout.PAGE_START);
		add(newConnectionButton, BorderLayout.PAGE_END);
	}
	
	/**
	 * Refreshes the ConnectionsPanel to properly reflect changes in 
	 * the SnodesConnection(s) extant.
	 */
	public void refreshConnections() {
		connections.removeAll();
		
		for (final SnodesConnection conn : GUIController.getInstance().getConnections()) {
			logger.finest("Adding connection button: " + conn.getHost());
			JButton addition = new JButton(conn.getHost().getHostAddress());
			addition.setActionCommand("Connection.Manage." + conn.getHost().getHostAddress());
			addition.addActionListener(listener);
			connections.add(addition);
		}
	}
}
