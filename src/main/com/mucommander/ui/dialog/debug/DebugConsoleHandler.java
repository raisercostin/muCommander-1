/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.debug;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.OutputStreamAppender;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreferences;

/**
 * This <code>java.util.logging</code> <code>Handler</code> collects the last log messages that were published by
 * the different muCommander loggers, so they can be displayed at any time in the {@link DebugConsoleDialog}.
 * Log records are kept in memory as a sliding window. The number of log records is controlled by the
 * {@link MuPreferences#LOG_BUFFER_SIZE} configuration variable: the more records, the more memory is used.
 *
 * @see DebugConsoleDialog
 * @see MuPreferences#LOG_BUFFER_SIZE
 * @author Maxence Bernard
 */
public class DebugConsoleHandler extends AppenderBase<ILoggingEvent> {

    /** Maximum number of log records to keep in memory */
    private int bufferSize;

    /** Contains the last LogRecord instances. */
    private List<ILoggingEvent> logRecords;

    /** Singleton instance of DebugConsoleHandler */
    private final static DebugConsoleHandler INSTANCE = new DebugConsoleHandler();

    /**
     * Creates a new <code>DebugConsoleHandler</code>. This constructor is automatically by
     * <code>java.util.logging</code> when it is configured and should never be called directly.
     */
    private DebugConsoleHandler() {
        // TODO: re-implement this.
        //setFormatter(new SingleLineFormatter());

        bufferSize = MuConfigurations.getPreferences().getVariable(MuPreferences.LOG_BUFFER_SIZE, MuPreferences.DEFAULT_LOG_BUFFER_SIZE);
        logRecords = new LinkedList<ILoggingEvent>();
    }

    /**
     * Returns a singleton instance of {@link DebugConsoleHandler}.
     *
     * @return a singleton instance of {@link DebugConsoleHandler}.
     */
    public static DebugConsoleHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the last records that were collected by this handler.
     *
     * @return the last records that were collected by this handler.
     */
    public synchronized ILoggingEvent[] getLogRecords() {
    	ILoggingEvent[] records = new ILoggingEvent[0];
    	records = logRecords.toArray(records);

    	return records;
    }


    ////////////////////////////
    // Appender implementation //
    ////////////////////////////

    @Override
    protected void append(ILoggingEvent record) {
		if(logRecords.size()== bufferSize)
            logRecords.remove(0);

        logRecords.add(record);
	}
}
