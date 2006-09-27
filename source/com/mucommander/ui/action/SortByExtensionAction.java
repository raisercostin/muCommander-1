package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action sorts the currently active FileTable by extension.
 * If the table is already sorted by extension, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortByExtensionAction extends MucoAction {

    public SortByExtensionAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().sortBy(FileTable.EXTENSION);
    }
}
