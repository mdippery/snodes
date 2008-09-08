/*
 * Copyright (c) 2008 Michael Dippery <mpd@cs.wm.edu>
 *
 * @MIT LICENSE@
 */

package snodes;

import java.util.HashMap;
import java.util.Iterator;


/**
 * An implementation of GNU's Getopt library in Java.<p>
 *
 * There are a few differences between this implementation and GNU's C
 * implementation; this implementation more closely resembles the
 * implementation found in the Python standard library.
 *
 * @author Michael Dippery
 * @version 0.2
 */
public class Args implements Iterable<String>
{
    /** The list of all arguments. */
    private HashMap<String, OptionWrapper> args;

    /**
     * Creates a new argument object.
     *
     * @param shortArgs
     *   A string describing the "short" arguments. The string should consist
     *   of each argument accepted. If the argument takes a value, you should
     *   append a colon. For example, to accept the arguments <tt>-v -o
     *   out</tt>, you should pass in the string <tt>"vo:"</tt>. If there are
     *   no short arguments, pass <tt>null</tt>.
     *
     * @param longArgs
     *   A list of possible long arguments. The string should consist of
     *   the full name of the argument. If the argument takes a value,
     *   append an equals ("=") sign. For example, to take the arguments
     *   "--verbose, --out=outfile", pass "verbose, out=". If there are no
     *   long arguments, pass <tt>null</tt>.
     *
     * @throws IllegalArgumentException
     *   If an invalid (non-letter) argument is passed.
     */
    public Args(String shortArgs, String[] longArgs)
        throws IllegalArgumentException
    {
        int size = 0;
        size += (shortArgs != null) ? shortArgs.length() : 0;
        size += (longArgs != null) ? longArgs.length : 0;
        args = new HashMap<String, OptionWrapper>();

        processShortArgs(shortArgs);
        processLongArgs(longArgs);
    }

    /**
     * Processes the short options.
     *
     * @param sargs
     *   The list of arguments.
     *
     * @throws IllegalArgumentException
     *   If any argument is invalid.
     *
     */
    private void processShortArgs(String sargs)
    {
        if (sargs == null) return;

        for (int i = 0; i < sargs.length(); i++) {
            char arg = sargs.charAt(i);

            if (!Character.isLetter(arg)) {
                throw new IllegalArgumentException(sargs + ": " + arg
                                                   + " is not a valid "
                                                   + "argument");
            }

            String k = String.valueOf(arg);
            OptionWrapper v = new OptionWrapper();
            if ((i+1) < sargs.length() && sargs.charAt(i+1) == ':') {
                v.required = true;
            }
            args.put(k, v);
        }
    }

    /**
     * Process the long options.
     *
     * @param largs
     *   The arguments.
     */
    private void processLongArgs(String[] largs)
    {
        if (largs == null) return;

        int i = 0;
        for (i = 0; i < largs.length; i++) {
            String k = largs[i];
            OptionWrapper v = new OptionWrapper();
            if (largs[i].endsWith("=")) v.required = true;
            args.put(k, v);
        }
    }

    /**
     * Processes the given arguments.<p>
     *
     * After processing the arguments, you may walk through a loop to return
     * any valid argument and argument value passed to the program.
     *
     * @param opts
     *   The command-line arguments passed to the program.
     *
     * @throws IllegalArgumentException
     *   If an invalid or unrecognized option is encountered.
     */
    public void parse(String[] opts)
    {
        for (String opt : opts) {
            // Process
        }
    }

    /**
     * Returns a object that iterates through each argument.
     *
     * @return
     *   An iterator.
     */
    public Iterator<String> iterator()
    {
        return args.keySet().iterator();
    }


    /** A mapping of argument value requirements to values. */
    private static class OptionWrapper
    {
        /** A flag specifying whether an option is required. */
        private boolean required;
        /** The value of the option, or <tt>null</tt>. */
        private String val;

        /** Creates a new, empty wrapper object. */
        private OptionWrapper()
        {
            this.required = false;
            this.val = null;
        }
    }
}
