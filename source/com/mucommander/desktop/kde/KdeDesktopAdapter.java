/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.desktop.kde;

import com.mucommander.AppLogger;
import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.DesktopManager;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
abstract class KdeDesktopAdapter extends DefaultDesktopAdapter {
    private static final String FILE_MANAGER_NAME = "Konqueror";
    private static final String FILE_OPENER       = "kfmclient exec $f";
    private static final String URL_OPENER        = "kfmclient openURL $f";

    /** Multi-click interval, cached to avoid polling the value every time {@link #getMultiClickInterval()} is called */
    private int multiClickInterval;

    /** Key to the double-click interval value in the KDE configuration */
    private String DOUBLE_CLICK_CONFIG_KEY = "DoubleClickInterval";

    public abstract boolean isAvailable();

    public void init(boolean install) throws DesktopInitialisationException {
        // Initialises trash management.
        DesktopManager.setTrashProvider(new KDETrashProvider());

        // Registers KDE specific commands.
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  FILE_OPENER, Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   URL_OPENER,  Command.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER, Command.SYSTEM_COMMAND, FILE_MANAGER_NAME));
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}

        // Multi-click interval retrieval
        try {
            String value = KdeConfig.getValue(DOUBLE_CLICK_CONFIG_KEY);
            if(value==null)
                multiClickInterval = super.getMultiClickInterval();

            multiClickInterval = Integer.parseInt(value);
        }
        catch(Exception e) {
            AppLogger.fine("Error while retrieving double-click interval from gconftool", e);
            multiClickInterval = super.getMultiClickInterval();
        }
    }

    /**
     * Returns the <code>DoubleClickInterval</code> KDE configuration value.
     * If the returned value is not defined or could not be retrieved, the value of
     * {@link DefaultDesktopAdapter#getMultiClickInterval()} is returned.<br/>
     * The value is retrieved on initialization and never updated thereafter.
     * <p>
     * Note under Java 1.6 or below, the returned value does not match the one used by Java for generating multi-clicks
     * (see {@link DefaultDesktopAdapter#getMultiClickInterval()}, as Java uses the multi-click speed declared in
     * X Window's configuration, not in KDE's. See <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5076635">
     * Java Bug 5076635</a> for more information.
     * </p>
     * @return the <code>DoubleClickInterval</code> KDE configuration value.
     */
    public int getMultiClickInterval() {
        return multiClickInterval;
    }
}
