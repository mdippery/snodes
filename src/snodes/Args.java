/*
 * Copyright (c) 2008 Michael Dippery <mpd@cs.wm.edu>
 *
 * @MIT LICENSE@
 */

package snodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
public class Args implements Iterable<Map.Entry<String, String>>
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
                // TODO: Handle "compacted" arguments; e.g, if
                // the program accepted `-v -d', allow user to
                // pass argument like '-dv'.
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
        String arg = opts[idx].substring(1);	// Trim off dash
        int newIdx = idx+1;
        if (args.containsKey(arg)) {
            boolean longArg = false;
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                longArg = true;
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
                longArg = false;
            }

            OptionWrapper w = args.get(arg);
            assert w != null : ("args contains '" + arg + "'!");
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
            w.present = true;

            return newIdx;
        } else {
            throw new IllegalArgumentException(arg);
        }
    }

    /**
     * Returns a object that iterates through each argument.
     *
     * @return
     *   An iterator.
     */
    public Iterator<Map.Entry<String, String>> iterator()
    {
        return new ArgsIterator(this);
    }


    /** A mapping of argument value requirements to values. */
    private static class OptionWrapper
    {
        /** A flag specifying whether an option is required. */
        private boolean required;
        /** The value of the option, or <tt>null</tt>. */
        private String val;
        /** A flag specifying whether the user specified the option. */
        private boolean present;

        /** Creates a new, empty wrapper object. */
        private OptionWrapper()
        {
            this.required = false;
            this.val = null;
            this.present = false;
        }
    }


    /** The <tt>Args</tt> object iterator. */
    private static class ArgsIterator
        implements Iterator<Map.Entry<String, String>>
    {
        /** The owner object. */
        private Args obj;
        /** The list of all "present" keys. */
        private List<String> present;
        /** The wrapped iterator. */
        private Iterator<String> it;

        /**
         * Creates a new iterator.
         *
         * @param obj
         *   The owner instance.
         */
        public ArgsIterator(Args obj)
        {
            this.obj = obj;
            this.present = new ArrayList<String>();

            for (String arg : obj.args.keySet()) {
                OptionWrapper w = obj.args.get(arg);
                assert w != null : "args should contain w";
                if (w.present) present.add(arg);
            }

            this.it = this.present.iterator();
        }

        /**
         * Returns <tt>true</tt> if there are more elements in the collection.
         *
         * @return
         *   <tt>true</tt> if there are more elements.
         */
        public boolean hasNext()
        {
            return it.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return
         *   A 2-tuple consisting of the argument and any value associated
         *   with the argument.
         *
         * @throws NoSuchElementException
         *   If there are no more elements in the iteration.
         */
        public Map.Entry<String, String> next()
        {
            String next = it.next();
            OptionWrapper w = obj.args.get(next);
            assert w != null : "args should contain w";
            assert w.present : "w should be present";
            return new ArgTuple(next, w.val);
        }

        /**
         * Removes the last item returned by {@link next} from the underlying
         * collection.
         *
         * @throws UnsupportedOperationException
         *   This operation is not supported.
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    /** A 2-tuple consisting of (arg, val). */
    public static class ArgTuple implements Map.Entry<String, String>
    {
        /** The argument. */
        public final String arg;
        /** The value. */
        public final String val;

        /**
         * Creates a new tuple.
         *
         * @param arg
         *   The name of the argument.
         * @param val
         *   The value of the argument.
         */
        private ArgTuple(String arg, String val)
        {
            this.arg = arg;
            this.val = val;
        }

        /**
         * Returns the name of the argument.
         *
         * @return
         *   The argument.
         */
        public String getKey()
        {
            return arg;
        }

        /**
         * Returns the value associated with the argument.
         *
         * @return
         *   The argument's value.
         */
        public String getValue()
        {
            return val;
        }

        /**
         * Replaces the value with a new value.
         *
         * @param value
         *   The new value.
         *
         * @return
         *   The old value.
         *
         * @throws UnsupportedOperationException
         *   This operation is not supported.
         */
        public String setValue(String value)
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Determines if this object is equal to another object.
         *
         * @param o
         *   The other object.
         *
         * @return
         *   <tt>true</tt> if the other object is a map that maps to the
         *   same thing.
         */
        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            } else if (o == null || o.getClass() != getClass()) {
                return false;
            } else {
                ArgTuple at = (ArgTuple) o;
                return (arg.equals(at.arg) && (val.equals(at.val)));
            }
        }

        /**
         * Returns the hash code for the tuple.
         *
         * @return
         *   The hash code.
         */
        @Override
        public int hashCode()
        {
            int argHash = (arg != null ? arg.hashCode() : 0);
            int valHash = (val != null ? val.hashCode() : 0);
            return argHash ^ valHash;
        }
    }
}
