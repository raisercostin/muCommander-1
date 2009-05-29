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

package com.mucommander.ui.main.commandbar;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.KeyStroke;

import com.mucommander.Debug;
import com.mucommander.RuntimeConstants;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.ui.text.KeyStrokeUtils;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

/**
 * 
 * @author Arik Hadas
 */
public class CommandBarWriter extends CommandBarIO {

	CommandBarWriter() throws IOException {
		Class[] commandBarActions = CommandBarAttributes.getActions();
		Class[] commandBarAlterativeActions = CommandBarAttributes.getAlternateActions();
		KeyStroke commandBarModifier = CommandBarAttributes.getModifier();
		
		BackupOutputStream bos = null;

		try {
			bos = new BackupOutputStream(getDescriptionFile());
			new Writer(bos).write(commandBarActions, commandBarAlterativeActions, commandBarModifier);
			isCommandBarChanged = false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bos.close();
		}
	}
	
	private static class Writer {
		private XmlWriter writer = null;
		
		private Writer(OutputStream stream) throws IOException {
    		this.writer = new XmlWriter(stream);
    	}
		
		private void write(Class[] actions, Class[] alternativeActions, KeyStroke modifier) throws IOException {
			try {
				XmlAttributes rootElementAttributes = new XmlAttributes();
				rootElementAttributes.add(MODIFIER_ATTRIBUTE, KeyStrokeUtils.getKeyStrokeRepresentation(modifier));
				rootElementAttributes.add(VERSION_ATTRIBUTE, RuntimeConstants.VERSION);

    			writer.startElement(ROOT_ELEMENT, rootElementAttributes, true);

    			int nbCommandBarActions = actions.length;
    			for (int i=0; i<nbCommandBarActions; ++i)
    				write(actions[i], alternativeActions[i]);

    		} finally {
    			writer.endElement(ROOT_ELEMENT);
    		}
		}
		
		private void write(Class action, Class alternativeAction) throws IOException {
			XmlAttributes attributes = new XmlAttributes();
			attributes.add(ACTION_ATTRIBUTE, action.getCanonicalName());
			if (alternativeAction != null)
				attributes.add(ALT_ACTION_ATTRIBUTE, alternativeAction.getCanonicalName());
			
			if (Debug.ON)
    			Debug.trace(" Writing button: action = "  + attributes.getValue(ACTION_ATTRIBUTE) + ", alt_action = " + attributes.getValue(ALT_ACTION_ATTRIBUTE));
			
			writer.writeStandAloneElement(BUTTON_ELEMENT, attributes);
		}
	}
}
