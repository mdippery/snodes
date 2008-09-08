/*
 * Copyright (c) 2008 Michael Dippery <mpd@cs.wm.edu>
 *
 * @MIT LICENSE@
 */

package snodes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


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
    /** The class logger. */
    private static final Logger logger = Logger.getLogger("snodes");

    /** The list of all arguments. */
    private String[] args;
    /** The list of all values. */
    private String[] vals;
    /** The list of arguments that require values. */
    private boolean[] requireVals;

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

        if (shortArgs != null) size += shortArgs.length();
        if (longArgs != null) size += longArgs.length;
        args = new String[size];
        vals = new String[size];
        Arrays.fill(vals, null);
        requireVals = new boolean[size];
        Arrays.fill(requireVals, false);

        int idx = processShortArgs(shortArgs, 0);
        idx = processLongArgs(longArgs, idx);
        if (idx < size) {
            logger.info("Resizing args list from " + size + " to " + idx);
            String[] newArgs = new String[idx];
            System.arraycopy(args, 0, newArgs, 0, idx);
            args = newArgs;
        }
    }

    /**
     * Processes the short options.
     *
     * @param sargs
     *   The list of arguments.
     *
     * @param idx
     *   The current index into the <tt>args</tt> instance variable.
     *
     * @return
     *   The new index into the <tt>args</tt> instance variable.
     *
     * @throws IllegalArgumentException
     *   If any argument is invalid.
     *
     */
    private int processShortArgs(String sargs, int idx)
    {
        if (sargs == null) return idx;

        int i = 0;
        for (i = 0; i < sargs.length(); i++) {
            char arg = sargs.charAt(i);

            if (!Character.isLetter(arg)) {
                throw new IllegalArgumentException(sargs + ": " + arg
                                                   + " is not a valid "
                                                   + "argument");
            }

            args[idx+i] = String.valueOf(arg);
            if ((i+1) < sargs.length() && sargs.charAt(i+1) == ':') {
                requireVals[idx+i+1] = true;
                i++;
            }
        }

        return idx+i;
    }

    /**
     * Process the long options.
     *
     * @param largs
     *   The arguments.
     *
     * @param idx
     *   The current index into the <tt>args</tt> array.
     *
     * @return
     *   The new index into the <tt>args</tt> array.
     */
    private int processLongArgs(String[] largs, int idx)
    {
        if (largs == null) return idx;

        int i = 0;
        for (i = 0; i < largs.length; i++) {
            args[idx+i] = largs[i];
            if (largs[i].endsWith("=")) {
                requireVals[idx+i] = true;
            }
        }

        return idx+i;
    }

    /**
     * Returns a object that iterates through each argument.
     *
     * @return
     *   An iterator.
     */
    public Iterator<String> iterator()
    {
        return new ArgsIterator(this);
    }


    /** An iterator for an <tt>Args</tt> object. */
    private static class ArgsIterator implements Iterator<String>
    {
        /** The iterator's owner. */
        private Args obj;
        /** The current index into the owner's data structures. */
        private int cur;

        /**
         * Creates a new iterator.
         *
         * @param obj
         *   The parent object.
         */
        private ArgsIterator(Args obj)
        {
            this.obj = obj;
            this.cur = 0;
        }

        /**
         * Returns <tt>true</tt> if the iterator contains more elements.
         *
         * @return
         *   <tt>true</tt> if the iterator contains more elements.
         */
        public boolean hasNext()
        {
            return cur < obj.args.length;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return
         *   The next element in the iteration.
         * @throws NoSuchElementException
         *   If the iteration contains no more elements.
         */
        public String next() throws NoSuchElementException
        {
            try {
                return obj.args[cur++];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        /**
         * Removes an item from the underlying collection.
         *
         * @throws UnsupportedOperationException
         *   If the operation is not supported by this iterator.
         *   <tt>ArgsIterator</tt> does not support this operation.
         */
        public void remove() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }
}
