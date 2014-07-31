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

import snodes.Controller;
import snodes.fs.RootShares;
import snodes.net.Packet;
import snodes.net.Packet.Type;
import snodes.net.SnodesConnection;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;


/**
 * Controller for the graphical user interface (GUI).
 *
 * @author Michael Schoonmaker
 * @author <a href="mailto:michael@monkey-robot.com">Michael Dippery</a>
 * @version 0.1
 */
public class GUIController extends Controller {
	
	/** The class logger. */
	private static final Logger logger = Logger.getLogger("snodes.gui");
	
	/** The window. */
	private JFrame frame;
	/** The main text area. This is for text feedback to the user.*/
	private IOPanel output;
	/** The connections panel. */
	private ConnectionsPanel connectionsPanel;
	/** The tabbed main section */
	private JTabbedPane tabs;
	/** The main split pane containing the tabs and the connections. */
	private JSplitPane splitPane;
	/** The preference manager. */
	private Preferences prefs;
	/** The singleton instance. */
	private static GUIController singleton = null;
	
	// Mac users expect the menu bar to be at the top of the screen, not in the
	// window, so enable that setting. (This is ignored on non-Mac systems).
	static {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Spaghetti Nodes");
		System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
	}
	
	/**
	 * Returns the singleton instance, and creates it if neccessary.
	 *
	 * @return
	 *	The singleton GUIController instance.
	 */
	public static GUIController getInstance() {
		if (singleton == null)
			singleton = new GUIController();
		return singleton;
	}

	/** Throws a CloneNotSupportedException, seeing as this is a singleton class. */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("GUIController is a singleton class. Clone not supported.");
	}
	
	/** Creates a new GUI controller. This sets up the default interface. */
	private GUIController() {
		super();
		
		logger.fine("Setting up GUI");
		
		prefs = Preferences.userNodeForPackage(GUIController.class);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			// Ignore.
		}
		
		frame = new JFrame(Controller.NAME);
		// Setup default close operation - save window preferences.
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				savePrefsAndExit();
			}
		});

		//Perform the actual layout.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		connectionsPanel = new ConnectionsPanel();
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		output = new IOPanel();
		addTab("Text Output", null, output, "The text output");

		splitPane.setOneTouchExpandable(true);
		splitPane.setLeftComponent(tabs);
		splitPane.setRightComponent(connectionsPanel);
		splitPane.setDividerSize(4);
		splitPane.setDividerLocation(prefs.getInt("Divider Location", 650));
		
		frame.setPreferredSize(new Dimension(prefs.getInt("XSize", 800), prefs.getInt("YSize", 600)));
		frame.add(splitPane);
		frame.pack();
		frame.setLocation(prefs.getInt("XLocation", 20), prefs.getInt("YLocation", 20));
		frame.setExtendedState(prefs.getInt("WindowState", 0));
		frame.setJMenuBar(new MainMenu());
		frame.setVisible(true);
		
		// Register a hook to save the window position when quit via the app menu.
		// This is in Mac OSX only.
		if (System.getProperty("os.name").startsWith("Mac OS")) {
			Runnable runner = new Runnable() {
				public void run() {
					savePrefs();
				}
			};
			
			Runtime.getRuntime().addShutdownHook(new Thread(runner, "Window Prefs Hook"));
		}
		
		//Finally, print the welcome message.
		println("Welcome to " + Controller.NAME + "\n---------------------------------\n");
	}
	
	/**
	 * Prints the object to the main panel.
	 *
	 * @param inObject
	 *     The object to print.
	 */
	public void print(Object inObject) {
		output.print(inObject);
	}
	
	/**
	 * Prints the object to the main panel and appends a line feed.
	 *
	 * @param inObject
	 *     The object to print.
	 */
	public void println(Object inObject) {
		output.println(inObject);
	}
	
	/**
	 * Refreshes the entire UI.
	 *
	 */
	public void refreshAll() {
		savePrefs();
		connectionsPanel.refreshConnections();
		frame.pack();
	}
	
	/**
	 * Saves any relevant preferences - particularly at the time of
	 *	closing.
	 *
	 */
	public void savePrefs()
	{
		int state = frame.getExtendedState();
		prefs.putInt("WindowState", state);
		
		if ((state & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
			if ((state & JFrame.MAXIMIZED_VERT) != JFrame.MAXIMIZED_VERT) {
				prefs.putInt("YSize", (int)frame.getSize().getHeight());
				prefs.putInt("YLocation", (int)frame.getLocation().getY());
			}
			
			if ((state & JFrame.MAXIMIZED_HORIZ) != JFrame.MAXIMIZED_HORIZ) {
				prefs.putInt("XSize", (int)frame.getSize().getWidth());
				prefs.putInt("XLocation", (int)frame.getLocation().getX());
			}
		}
		
		prefs.putInt("Divider Location", (int)splitPane.getDividerLocation());
		
		try {
			prefs.sync();
			logger.finer("Saved window preferences");
		} catch(BackingStoreException e) {
			logger.log(Level.SEVERE, "Cannot save preferences", e);
		}
	}
	
	/**
	 * Saves the preferences and then terminates the Java VM.
	 *
	 */
	public void savePrefsAndExit() {
		savePrefs();
		logger.fine("Exiting Snodes via GUIController.savePrefsAndExit()");
		System.exit(0);
	}
	
	/**
	 * Returns the GUIController's main JFrame, so that calling objects can create Dialogs.
	 *
	 * @return
	 *	JFrame of the window.
	 *
	 */
	public JFrame getMainFrame() {
		return frame;
	}
	
	/**
	 * Adds a tab to the MainPanel.
	 *
	 * @return
	 *	The tab's ID.
	 * @param title
	 *	The tab's title.
	 * @param icon
	 *	The ImageIcon to use as the tab's icon.
	 * @param tabPanel
	 *	The JPanel to add as a tab.
	 * @param tip
	 *	The mouseover tooltip to be associated with the new tab.
	 *
	 */
	public int addTab(String title, ImageIcon icon, JPanel tabPanel, String tip) {
		tabs.addTab(title, icon, tabPanel, tip);
		return tabs.getTabCount() - 1;
	}
	
	/**
	 * Removes a tab.
	 *
	 * @param id
	 *	The id of the tab to be removed.
	 *
	 */
	public void removeTab(int id) {
		tabs.removeTabAt(id);
	}

	/**
	 * Displays a String via dialog box.
	 *
	 * @param label
	 *	The label within the dialog.
	 * @param title
	 *	The dialog window's title.
	 */
	public void showMessageDialog(String label, String title) {
		JOptionPane.showMessageDialog(frame, label, title, JOptionPane.PLAIN_MESSAGE, null);
	}

	/**
	 * Prompts the user for a String via dialog box.
	 *
	 * @param label
	 *	The label within the dialog of the prompt.
	 * @param title
	 *	The dialog window's title.
	 * @return
	 *	The entered String.
	 */
	public String showTextDialog(String label, String title) {
		return JOptionPane.showInputDialog(frame, label, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	/**
	 * Prompts the user for an Object from an array of Objects via dialog box.
	 *
	 * @param label
	 *	The label within the dialog of the prompt.
	 * @param title
	 *	The dialog window's title.
	 * @param pickFrom
	 *	The array that the user is to select from.
	 * @return
	 *	The selected Object.
	 */
	public Object showSelectionDialog(String label, String title, Object[] pickFrom) {
		return JOptionPane.showInputDialog(frame, title, label, JOptionPane.PLAIN_MESSAGE, null, pickFrom, null);
	}
	
	void promptForNewConnection()
	{
		String address = showTextDialog("Enter the user's IP Address:", "IP Address");
		String passkey = showTextDialog("Enter the secret:", "Secret");
		
		if (address != null && passkey != null) { // Did the user cancel the dialog?
			SnodesConnection conn = GUIController.getInstance().addConnection(address);
			
			conn.authenticate(passkey);
			logger.finest("Connection.Add creating connection: " + conn);
			connect(conn);
			refreshAll();
		}
	}
	
	void promptToDeleteConnection()
	{
		SnodesConnection[] conns = GUIController.getInstance().getConnections();
		SnodesConnection conn = (SnodesConnection) showSelectionDialog(
			"Select the connection to remove",
			"Remove Connection",
			conns
		);
		invalidateHost(conn.getHost());
	}
	
	void promptToAddShare()
	{
		String alias = showTextDialog("Enter the alias for the share:", "Alias");
		if (alias != null && !"".equals(alias)) {
			String path = showTextDialog("Enter the path:", "Path");
			try {
				if (path != "" && path != null) {
					RootShares.getInstance().addFolder(alias, path);
				}
			} catch(IllegalArgumentException e) {
				logger.log(Level.SEVERE, "IllegalArgumentException", e);
			}
		}
	}
	
	void promptToRemoveShare()
	{
		if (RootShares.getInstance().getFolderList().isEmpty()) {
			showMessageDialog("No shares to remove", "Remove Share");
			return;
		}
		String alias = showSelectionDialog(
			"Select the share to remove",
			"Remove Share",
			RootShares.getInstance().getFolderList().keySet().toArray()
		).toString();
		if (alias != null && !"".equals(alias)) {
			RootShares.getInstance().removeFolder(alias);
		}
	}
	
	@Override
	public boolean accept(Type type) {
		return (type == Packet.Type.ChatMessage) || super.accept(type);
	}

	@Override
	public void processPacket(SnodesConnection conn, Packet packet) {
		println("Received: " + packet.toString() + "From " + conn.toString());
		super.processPacket(conn, packet);
		refreshAll();
	}
}
