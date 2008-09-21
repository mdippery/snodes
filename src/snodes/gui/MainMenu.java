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

import snodes.Bundle;
import snodes.Controller;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
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
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class MainMenu extends JMenuBar {
	
	/** The class logger. */
	private static final Logger logger = Logger.getLogger("snodes.gui");
	
	/** The mask for action-related keystrokes. */
	private static int ACTION_MASK = ActionEvent.CTRL_MASK;
	/** The global listener used by the menu structure. */
	private static ActionListener listener;
	// On Mac OS X, this key should be the command key, not the option key
	static {
		if (System.getProperty("os.name").startsWith("Mac OS")) {
			ACTION_MASK = ActionEvent.META_MASK;
		}
	}
	
	/** Creates the main menu for the SpaghettiNodes program. */
	public MainMenu(ActionListener listener) {
		super();
		
		this.listener = listener;
		
	    //This is where the menu will be built.
	    JMenu menu = new JMenu("Menu");
	    menu.setMnemonic(KeyEvent.VK_M);
	    menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
	    add(menu);

		menu.add(createItem("Show IP Address", null, "Displays your IP address.",
			"System.Address", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_A, ACTION_MASK | ActionEvent.SHIFT_MASK)));
		
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
				logger.warning("Cannot load Quit image: " + e.getMessage());
			} finally {
				menu.add(createItem("Quit", icon, label, command, mnemonic, key));
			}
		}
	}
	
	/**
	 * Creates a new JMenuItem with the given parameters.
	 *
	 * @param name
	 *	The name of the menu item.
	 * @param icon
	 *	The icon to be used alongside the name.
	 * @param description
	 *	The tooltip description of the menu control.
	 * @param command
	 *	The actual command passed to the ActionListener.
	 * @param monic
	 *	The mnemonic used to access the item once the Menu has focus.
	 * @param accel
	 *	The hotkey Accelerator used to access the item from outside the Menu.
	 *
	 * @return
	 *	The created JMenuItem.
	 */
	private JMenuItem createItem(String name, Icon icon, String description, String command, int monic, KeyStroke accel) {
		JMenuItem menuItem = new JMenuItem(name, icon);
		menuItem.setMnemonic(monic);
	    menuItem.getAccessibleContext().setAccessibleDescription(description);
	    if(listener != null) {
		    menuItem.addActionListener(listener);
		    if(command != null)
		    	menuItem.setActionCommand(command);
		    else
		    	menuItem.setActionCommand(name);
	    }
	    if(accel != null) {
		    menuItem.setAccelerator(accel);
	    }
	    return menuItem;
	}
}
