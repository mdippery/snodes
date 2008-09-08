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
                i++;
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

        for (String arg : largs) {
            OptionWrapper v = new OptionWrapper();
            if (arg.endsWith("=")) v.required = true;
            args.put(arg, v);
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
    public void parse(String[] opts) throws IllegalArgumentException
    {
        for (int i = 0; i < opts.length; ) {
            String opt = opts[i];
            if (opt.startsWith("-")) {
                i = processArg(opts, i);
            } else {
                throw new IllegalArgumentException(opt);
            }
        }
    }

    /**
     * Processes a single argument.
     *
     * @param opts
     *   The list of options.
     *
     * @param idx
     *   The current index into the array.
     *
     * @return
     *   The new index.
     *
     * @throws IllegalArgumentException
     *   If the argument is invalid or unrecognized.
     */
    private int processArg(String[] opts, int idx)
        throws IllegalArgumentException
    {
        String arg = opts[idx];
        int newIdx = idx+1;
        if (arg.contains(arg)) {
            boolean longArg = false;
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                longArg = true;
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
                longArg = false;
            }

            OptionWrapper w = args.get(arg);
            if (w.required) {
                if (longArg) {
                    String[] cl = arg.split("=");
                    if (cl.length == 2) {
                        w.val = cl[1].substring(1);
                    } else {
                        throw new IllegalArgumentException(arg + " requires "
                                                           + "a value");
                    }
                } else {
                    if (!opts[idx+1].startsWith("-")) {
                        w.val = opts[idx+1];
                        newIdx++;
                    } else {
                        throw new IllegalArgumentException(arg + " requires a "
                                                           + "value");
                    }
                }
            }

            return newIdx;
        } else {
            throw new IllegalArgumentException(arg);
        }
    }

    /**
     * Gets the value for an option.
     *
     * @param opt
     *   The option
     *
     * @return
     *   The value for that option, or <tt>null</tt> if no value was
     *   specified.
     *
     * @throws IllegalArgumentException
     *   If <tt>opt</tt> is an invalid or unrecognized option.
     */
    public String getValue(String opt) throws IllegalArgumentException
    {
        if (args.containsKey(opt)) {
            return args.get(opt).val;
        } else {
            throw new IllegalArgumentException(opt);
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
