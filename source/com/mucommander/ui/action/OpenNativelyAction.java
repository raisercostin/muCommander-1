package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.MainFrame;

/**
 * This action opens the currently selected file or folder with native file associations.
 *
 * @author Maxence Bernard
 */
public class OpenNativelyAction extends MucoAction {

    public OpenNativelyAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        AbstractFile selectedFile = mainFrame.getLastActiveTable().getSelectedFile(true);

        if(selectedFile==null)
            return;

        // Tries to execute file
        PlatformManager.open(selectedFile);
    }
}
