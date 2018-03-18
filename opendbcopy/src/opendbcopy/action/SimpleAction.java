/*
 * Copyright (C) 2004 Anthony Smith
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * ----------------------------------------------------------------------------
 * TITLE $Id$
 * ---------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.action;

import opendbcopy.config.XMLTags;
import opendbcopy.controller.MainController;
import opendbcopy.gui.FrameMain;
import org.apache.log4j.Level;
import org.jdom2.Element;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class SimpleAction extends AbstractAction {
    private FrameMain frame;
    private MainController controller;
    private Element operation;

    /**
     * Creates a new SimpleAction object.
     *
     * @param command    DOCUMENT ME!
     * @param name       DOCUMENT ME!
     * @param imageIcon  DOCUMENT ME!
     * @param frame      DOCUMENT ME!
     * @param controller DOCUMENT ME!
     */
    public SimpleAction(String command,
                        String name,
                        ImageIcon imageIcon,
                        FrameMain frame,
                        MainController controller) {
        putValue(AbstractAction.NAME, name);
        putValue(AbstractAction.ACTION_COMMAND_KEY, command);

        if (imageIcon != null) {
            putValue(AbstractAction.SMALL_ICON, imageIcon);
        }

        this.operation = new Element(XMLTags.OPERATION);
        this.operation.setAttribute(XMLTags.NAME, command);

        this.frame = frame;
        this.controller = controller;

        this.setEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            controller.execute(operation);
        } catch (Exception e) {
            frame.postException(e, Level.ERROR);
        }
    }
}
