/*
 * Copyright (c) 2007-2008 Michael Schoonmaker <michael.r.schoonmaker@gmail.com>
 * Copyright (c) 2007-2008 Michael Dippery <michael@monkey-robot.com>
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

import snodes.Bundle;
import snodes.Controller;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


/**
 * The main menu for the GUIController.
 *
 * @author Michael Schoonmaker
 * @author <a href="mailto:michael@monkey-robot.com">Michael Dippery</a>
 * @version 0.1
 */
public class MainMenu extends JMenuBar {
	/** The class logger. */
	private static final Logger logger = Logger.getLogger("snodes.gui");
	
	/** The mask for action-related keystrokes. */
	private static int ACTION_MASK = ActionEvent.CTRL_MASK;

	// On Mac OS X, this key should be the command key, not the option key
	static {
		if (System.getProperty("os.name").startsWith("Mac OS")) {
			ACTION_MASK = ActionEvent.META_MASK;
		}
	}
	
	/** Creates the main menu for the SpaghettiNodes program. */
	public MainMenu() {
		super();
		
	    //This is where the menu will be built.
	    JMenu menu = new JMenu("Menu");
	    menu.setMnemonic(KeyEvent.VK_M);
	    menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
	    add(menu);
	
		JMenuItem showIPItem = new JMenuItem("Show IP Address");
		showIPItem.setMnemonic(KeyEvent.VK_A);
		showIPItem.getAccessibleContext().setAccessibleDescription("Displays your IP address");
		showIPItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ACTION_MASK | ActionEvent.SHIFT_MASK));
		showIPItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					GUIController.getInstance().println("Your IP address is " + InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException exc) {
					logger.log(Level.WARNING, "Could not get host", exc);
				}
			}
		});
		menu.add(showIPItem);
		
		// OS X adds its Quit item to the app menu automatically, so don't add if on OS X
		if (!"Mac OS X".equals(System.getProperty("os.name"))) {
			Icon icon = null;
			KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Q, ACTION_MASK);
			String label = "Quit " + Controller.NAME;
			String command = "System.Quit";
			int mnemonic = KeyEvent.VK_Q;
			
			try {
				icon = new ImageIcon(Bundle.getMainBundle().loadImage("skull.png"));
			} catch (FileNotFoundException e) {
				logger.log(Level.WARNING, "Cannot load Quit image", e);
			} finally {
				//menu.add(createItem("Quit", icon, label, command, mnemonic, key));
				JMenuItem quitItem = new JMenuItem("Quit", icon);
				quitItem.setMnemonic(mnemonic);
				quitItem.setAccelerator(key);
				quitItem.getAccessibleContext().setAccessibleDescription(label);
				quitItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						GUIController.getInstance().savePrefsAndExit();
					}
				});
				menu.add(quitItem);
			}
		}
	}
}
