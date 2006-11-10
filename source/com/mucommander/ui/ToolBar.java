
package com.mucommander.ui;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileToolkit;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MucoAction;
import com.mucommander.ui.comp.button.RolloverButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements ConfigurationListener, MouseListener {

    private MainFrame mainFrame;

    /** Default toolbar descriptor filename */
    private final static String DEFAULT_TOOLBAR_FILENAME = "toolbar.xml";
    /** Path to the toolbar descriptor resource file within the application JAR file */
    private final static String TOOLBAR_RESOURCE_PATH = "/"+DEFAULT_TOOLBAR_FILENAME;

    /** Toolbar descriptor file used when calling {@link #loadDescriptionFile()} */
    private static AbstractFile toolbarDescriptorFile = FileFactory.getFile(PlatformManager.getPreferencesFolder().getAbsolutePath()+"/"+DEFAULT_TOOLBAR_FILENAME);

    /** Dimension of button separators */
    private final static Dimension SEPARATOR_DIMENSION = new Dimension(10, 16);

    /** Name of the configuration variable that holds the toolbar's icon scale */
    public final static String TOOLBAR_ICON_SCALE_CONF_VAR = "prefs.toolbar.icon_scale";


    /** Current icon scale value */
    private static float scaleFactor = ConfigurationManager.getVariableFloat(TOOLBAR_ICON_SCALE_CONF_VAR, 1.0f);

    /** Command bar actions: Class instances or null to signify a separator */
    private static Class actions[];


    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link DEFAULT_TOOLBAR_FILENAME} within the preferences folder.
     *
     * @param filePath path to the toolbar descriptor file
     */
    public static void setDescriptionFile(String filePath) {
        AbstractFile file = FileFactory.getFile(filePath);
        if(file!=null)
            toolbarDescriptorFile = file;
    }

    /**
     * Parses the XML file describing the toolbar's buttons and associated actions.
     * If the file doesn't exist yet, it is copied from the default resource file within the JAR.
     *
     * <p>This method must be called before instanciating ToolBar for the first time.
     */
    public static void loadDescriptionFile() {
        // If the given file doesn't exist, copy the default one in the JAR file
        if(!toolbarDescriptorFile.exists()) {
            try {
                if(Debug.ON) Debug.trace("copying "+TOOLBAR_RESOURCE_PATH+" resource to "+toolbarDescriptorFile);

                FileToolkit.copyResource(TOOLBAR_RESOURCE_PATH, toolbarDescriptorFile);
            }
            catch(IOException e) {
                System.out.println("Error: unable to copy "+TOOLBAR_RESOURCE_PATH+" resource to "+toolbarDescriptorFile+": "+e);
                return;
            }
        }

        new ToolBarReader();
    }


    /**
     * Creates a new toolbar and attaches it to the given frame.
     */
    public ToolBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Decoration properties
        setBorderPainted(false);
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        // Listen to mouse events in order to popup a menu when toolbar is right-clicked
        addMouseListener(this);

        // Listen to configuration changes to reload toolbar buttons when icon size has changed
        ConfigurationManager.addConfigurationListener(this);

        // Create buttons and add them to this toolbar
        int nbActions = actions.length;
        for(int i=0; i<nbActions; i++) {
            Class actionClass = actions[i];
            if(actionClass==null)
                addSeparator(SEPARATOR_DIMENSION);
            else
                addButton(ActionManager.getActionInstance(actionClass, mainFrame));
        }
    }


    /**
     * Adds a button to this toolbar using the given action.
     */
    private void addButton(MucoAction action) {
        JButton button = new RolloverButton(action);

        // Remove label
        button.setText(null);

        // Add tooltip using the action's label and accelerator
        String toolTipText = action.getLabel();
        String acceleratorText = action.getAcceleratorText();
        if(acceleratorText!=null)
            toolTipText += " ("+acceleratorText+")";
        button.setToolTipText(toolTipText);

        // Scale icon if scale factor is different from 1.0
        if(scaleFactor!=1.0f)
            button.setIcon(IconManager.getScaledIcon(action.getIcon(), scaleFactor));

        add(button);
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Rescale buttons icon
        if (var.equals(TOOLBAR_ICON_SCALE_CONF_VAR)) {
            scaleFactor = event.getFloatValue();
            Component components[] = getComponents();
            int nbComponents = components.length;

            for(int i=0; i<nbComponents; i++) {
                if(components[i] instanceof JButton) {
                    JButton button = (JButton)components[i];
                    // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
                    button.setIcon(IconManager.getScaledIcon(((MucoAction)button.getAction()).getIcon(), scaleFactor));
                }
            }
        }

        return true;
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();

        // Right clicking on the toolbar brings up a popup menu
        if(source == this) {
            int modifiers = e.getModifiers();
            if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {
                //			if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleToolBarAction.class, mainFrame));
                popupMenu.show(this, e.getX(), e.getY());
                popupMenu.setVisible(true);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }


    /**
     * This class parses the XML file describing the toolbar's buttons and associated actions.
     *
     * @author Maxence Bernard
     */
    private static class ToolBarReader implements ContentHandler {

        /** Temporarily used for XML parsing */
        private Vector actionsV;


        /**
         * Starts parsing the XML description file.
         */
        private ToolBarReader() {
            InputStream in = null;
            try {
                in = new BackupInputStream(toolbarDescriptorFile);
                new Parser().parse(in, this, "UTF-8");
            }
            catch(Exception e) {
                // Report error to the standard output
                System.out.println("Exception thrown while parsing Toolbar XML file "+TOOLBAR_RESOURCE_PATH+": "+e);
            }
            finally {
                if(in!=null)
                    try { in.close(); }
                    catch(IOException e) {}
            }
        }

        ////////////////////////////
        // ContentHandler methods //
        ////////////////////////////

        public void startDocument() throws Exception {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(TOOLBAR_RESOURCE_PATH+" parsing started");

            actionsV = new Vector();
        }

        public void endDocument() throws Exception {
            int nbActions = actionsV.size();
            actions = new Class[nbActions];
            actionsV.toArray(actions);
            actionsV = null;

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(TOOLBAR_RESOURCE_PATH+" parsing finished");
        }

        public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
            if(name.equals("button")) {
                String actionClassName = (String)attValues.get("action");
                try {
                    actionsV.add(Class.forName(actionClassName));
                }
                catch(Exception e) {
                    System.out.println("Error in "+TOOLBAR_RESOURCE_PATH+": action class "+actionClassName+" not found: "+e);
                }
            }
            else if(name.equals("separator")) {
                actionsV.add(null);
            }
        }

        public void endElement(String uri, String name) throws Exception {
        }

        public void characters(String s) throws Exception {
        }
    }
}
