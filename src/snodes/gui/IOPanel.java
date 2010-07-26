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

package snodes.gui;

import snodes.fs.RootShares;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * A text output panel for the Spaghetti Nodes program.
 *
 * @author Michael Schoonmaker
 * @author Michael Dippery
 * @version 0.1
 */
public class IOPanel extends JPanel {
	private static final Logger logger = Logger.getLogger("snodes.gui");
	
	/** The main text area. */
	private JTextArea output;
	/** The text entry field. */
	private JTextField entryField;
	/** The text entry button. */
	private JButton entrySubmit;
	
	/** Creates a new main panel. */
	public IOPanel() {
		super(new BorderLayout());
		
		//Create the text area used for output.	 Request
		//enough space for 5 rows and 30 columns.
		output = new JTextArea(5, 30);
		output.setEditable(false);
		
		entryField = new JTextField(60);

		entrySubmit = new JButton();
		entrySubmit.setText("Submit");
		entrySubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCommand(entryField.getText());
			}
		});

		JPanel entryPanel = new JPanel(new BorderLayout());
		entryPanel.add(entryField, BorderLayout.CENTER);
		entryPanel.add(entrySubmit, BorderLayout.LINE_END);

		add(new JScrollPane(output), BorderLayout.CENTER);
		add(entryPanel, BorderLayout.PAGE_END);
	}
	
	/**
	 * Prints an object to the main text area.
	 *
	 * @param inObject
	 *     The object to print.
	 */
	public void print(Object inObject) {
		output.append(inObject.toString());
		output.setCaretPosition(output.getDocument().getLength());
	}
	
	/**
	 * Prints an object to the main text area and appends a line feed.
	 *
	 * @param inObject
	 *     The object to print.
	 */
	public void println(Object inObject) {
		output.append(inObject.toString() + "\n");
		output.setCaretPosition(output.getDocument().getLength());
	}
	
	public void handleCommand(String command)
	{
		GUIController gui = GUIController.getInstance();
		if (command.equals("Connection.Add")) {
			gui.promptForNewConnection();
		} else if (command.equals("Connection.Manage")) {
			gui.println("Connection management not implemented yet");
		} else if (command.equals("Connection.Remove")) {
			gui.promptToDeleteConnection();
		} else if (command.equals("Search.Private")) {
			gui.println("Searching not implemented yet");
		} else if (command.equals("Search.Public")) {
			gui.println("Searching not implemented yet");
		} else if (command.equals("Shares.Add")) {
			gui.promptToAddShare();
		} else if (command.equals("Shares.Manage")) {
			gui.println("Shares management not implemented yet");
		} else if (command.equals("Shares.Print")) {
			gui.println(RootShares.getInstance().toString());
		} else if (command.equals("Shares.Remove")) {
			gui.promptToRemoveShare();
		} else if (command.equals("System.Address")) {
			try {
				gui.println("Your IP address is " + InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException exc) {
				logger.log(Level.WARNING, "Could not get host", exc);
			}
		} else if (command.equals("System.Help")) {
			gui.println("RTFM");
		} else if (command.equals("System.Quit")) {
			gui.savePrefsAndExit();
		} else if (command.equals("System.RefreshUI")) {
			gui.refreshAll();
		} else {
			gui.println("Invalid command: " + command);
		}
	}
}
