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

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * Formats log messages for entry into a log file.
 *
 * @author <a href="mailto:mdippery@bucknell.edu">Michael Dippery</a>
 * @version 0.1
 */
public class FileFormatter extends Formatter
{
	/** Creates a new formatter. */
	public FileFormatter()
	{
		super();
	}
	
	/**
	 * Formats a message for logging purposes. The message "Debug message",
	 * with the level INFO, would look like the following:<p>
	 *
	 * <pre>
	 * 2008-01-18 12:26:59.051 Snodes [INFO]: Debug message
	 * </pre>
	 *
	 * Stack trace information is also prevented in the case of an exception.
	 *
	 * @param record
	 *     The log record.
	 * @return
	 *     The formatted message.
	 */
	@Override
	public String format(LogRecord record)
	{
		final String LF = System.getProperty("line.separator");
		
		Throwable t = record.getThrown();
		String tStr = null;
		String str = null;
		
		// If there's an exception associated with the record, print the stack trace
		if (t != null) {
			StringBuilder tbuf = new StringBuilder();
			
			tbuf.append(t).append(LF);
			
			for (final StackTraceElement el : t.getStackTrace()) {
				tbuf.append("\tat ").append(el).append(LF);
			}

			tStr = new String(tbuf);
		}
		
		str = String.format(
			"%1$tF %1$tT [%2$-7s]: %3$s%n",
			new Date(), record.getLevel().toString(), record.getMessage()
		);
		if (tStr != null) {
			str = str.concat(tStr);
		}
		
		return str;
	}
}
