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

package snodes.util.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * A formatter that logs only the log level and message. This is deal for use
 * in consoles.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class ConsoleFormatter extends Formatter
{
	/** Creates a new formatter. */
	public ConsoleFormatter()
	{
		super();
	}
	
	/**
	 * Formats a message for logging purposes. The message "Debug message",
	 * with the level INFO, would look like the following:<p>
	 *
	 * <pre>
	 * INFO: Debug message
	 * </pre>
	 *
	 * @param record
	 *     The log record.
	 * @return
	 *     The formatted message.
	 */
	@Override
	public String format(LogRecord record)
	{
		Throwable t = record.getThrown();
		String level = String.format("[%-7s]", record.getLevel().toString());
		String msg = record.getMessage();
		
		if (t != null) {
			return String.format("%s %s: %s%n", level, msg, t.toString());
		} else {
			return String.format("%s %s%n", level, msg);
		}
	}
}
