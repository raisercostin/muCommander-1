package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action refreshes the currently active FolderPanel (refreshes the content of the folder).
 *
 * @author Maxence Bernard
 */
public class RefreshAction extends MucoAction {

    public RefreshAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        // Refresh current folder in a separate thread
        mainFrame.getLastActiveTable().getFolderPanel().tryRefreshCurrentFolder();
    }
}
