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
 
package snodes;

import snodes.gui.GUIController;
import snodes.net.SnodesServer;
import snodes.util.logging.ConsoleFormatter;
import snodes.util.logging.FileFormatter;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The main executable class of the program. It provides an executable
 * <tt>main</tt> method for the program.
 *
 * @author Michael Schoonmaker
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public final class ControllerMain {
    
    /** The default logging level. */
    private static final Level DEFAULT_LEVEL = Level.FINE;
    /** The system's log object. */
    private static final Logger logger = Logger.getLogger("snodes");
    /** The path to the log file. */
    private static final String LOG_PATH = "snodes.log";
    
    /**
     * The main executable method of the program.<p>
     *
     * The following command-line arguments are accepted:<p>
     *
     * <pre>
     * -h     Print a help message and exit
     * -q     Don't print debugging info to a log file
     * -d     Print all debug messages, and delete old log files
     * </pre>
     *
     * @param args
     *     The command line arguments.
     */
    public static void main(String[] args)
    {
        Controller controller = null;
        SnodesServer server = SnodesServer.getInstance();
        Level level = DEFAULT_LEVEL;
        boolean logToConsole = false;
        
        Getopt g = new Getopt("snodes", args, "hqdV");
        int c = -1;
        
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'h':
                showHelp();
                break;
            case 'q':
                logToConsole = true;
                level = Level.INFO;
                break;
            case 'd':
                logToConsole = true;
                level = Level.ALL;
                //deleteLog();
                break;
            case 'V':
                System.out.println(Controller.NAME + " " + Controller.VERSION);
                System.exit(0);
                break;
            case '?':
                // Getopt already printed an error
                break;
            default:
                logger.warning("Getopt returned: " + c);
                break;
            }
        }
        
        if (!logToConsole) {
            try {
                initFileLogger(level);
            } catch (IOException e) {
                initConsoleLogger(level);
            }
        } else {
            initConsoleLogger(level);
        }
        
        logger.info("Configured Snodes. Starting program.");
        
        controller = GUIController.getInstance();
        server.setConnectionManager(controller);
        server.start();
    }
    
    private static void showHelp()
    {
        System.err.println(
            Controller.NAME + "\n" +
            "  -h    Print help message and exit\n" +
            "  -q    Print debugging info to console\n" +
            "  -d    Print all debug messages, and delete old log files"
        );
        System.exit(0);
    }
    
    private static void initConsoleLogger(Level level)
    {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new ConsoleFormatter());
        ch.setLevel(level);
        logger.addHandler(ch);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
    }
    
    private static void initFileLogger(Level level) throws IOException
    {
        FileHandler fh = new FileHandler(LOG_PATH, true); // true = append
        ConsoleHandler ch = new ConsoleHandler();
        fh.setFormatter(new FileFormatter());
        ch.setFormatter(new ConsoleFormatter());
        ch.setLevel(Level.WARNING); // Also print important messages to console
        logger.addHandler(fh);
        logger.addHandler(ch);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
    }
    
    private static void deleteLog()
    {
        new File(LOG_PATH).delete();
    }
    
    // Don't create instances of this class
    private ControllerMain() {}
}
