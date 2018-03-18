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
package opendbcopy.gui.database;

import info.clearthought.layout.TableLayout;
import opendbcopy.config.GUI;
import opendbcopy.config.XMLTags;
import opendbcopy.controller.MainController;
import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.PluginGui;
import opendbcopy.plugin.model.database.DatabaseModel;
import org.jdom2.Element;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Observable;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelFilter extends DynamicPanel {
    private DatabaseModel model;
    private GridLayout gridLayout = new GridLayout();
    private JPanel panelStringFilter = new JPanel();
    private JCheckBox checkBoxTrim = new JCheckBox();
    private JCheckBox checkBoxRemoveMultipleIntermediateSpaces = new JCheckBox();
    private JCheckBox checkBoxSetNull = new JCheckBox();

    /**
     * Creates a new PanelConfiguration object.
     *
     * @param controller         DOCUMENT ME!
     * @param workingMode        DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public PanelFilter(MainController controller,
                       PluginGui workingMode,
                       Boolean registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);
        model = (DatabaseModel) super.model;
        guiInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o   DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object obj) {
        try {
            // check for string filters
            Iterator itStringFilters = model.getStringFilters().iterator();

            while (itStringFilters.hasNext()) {
                Element stringFilter = (Element) itStringFilters.next();

                if ((stringFilter.getAttributeValue(XMLTags.NAME).compareTo(XMLTags.TRIM) == 0) && (stringFilter.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                    checkBoxTrim.setSelected(true);
                }

                if ((stringFilter.getAttributeValue(XMLTags.NAME).compareTo(XMLTags.REMOVE_INTERMEDIATE_WHITESPACES) == 0) && (stringFilter.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                    checkBoxRemoveMultipleIntermediateSpaces.setSelected(true);
                }

                if ((stringFilter.getAttributeValue(XMLTags.NAME).compareTo(XMLTags.SET_NULL) == 0) && (stringFilter.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                    checkBoxSetNull.setSelected(true);
                }
            }
        } catch (Exception e) {
            postException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        double[][] size = {
                {GUI.B, GUI.F, GUI.B}, // Columns
                {GUI.B, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.B}
        }; // Rows

        this.setLayout(new TableLayout(size));
        this.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.BLACK, 1), " String Filters "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        checkBoxTrim.setText(rm.getString("text.filter.trim"));
        checkBoxTrim.addActionListener(new PanelFilter_checkBoxTrim_actionAdapter(this));

        checkBoxRemoveMultipleIntermediateSpaces.setText(rm.getString("text.filter.removeMultipleIntermediateWhitespaces"));
        checkBoxRemoveMultipleIntermediateSpaces.addActionListener(new PanelFilter_checkBoxRemoveMultipleIntermediateSpaces_actionAdapter(this));

        checkBoxSetNull.setText(rm.getString("text.filter.setNull"));
        checkBoxSetNull.addActionListener(new PanelFilter_checkBoxSetNull_actionAdapter(this));

        this.add(checkBoxTrim, "1, 1");
        this.add(checkBoxRemoveMultipleIntermediateSpaces, "1, 3");
        this.add(checkBoxSetNull, "1, 5");
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxTrim_actionPerformed(ActionEvent e) {
        try {
            Element filter = model.getStringFilter(XMLTags.TRIM);

            if (filter != null) {
                if (checkBoxTrim.isSelected()) {
                    filter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    filter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxRemoveMultipleIntermediateSpaces_actionPerformed(ActionEvent e) {
        try {
            Element filter = model.getStringFilter(XMLTags.REMOVE_INTERMEDIATE_WHITESPACES);

            if (filter != null) {
                if (checkBoxRemoveMultipleIntermediateSpaces.isSelected()) {
                    filter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    filter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxSetNull_actionPerformed(ActionEvent e) {
        try {
            Element filter = model.getStringFilter(XMLTags.SET_NULL);

            if (filter != null) {
                if (checkBoxSetNull.isSelected()) {
                    filter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    filter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            postException(ex);
        }
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelFilter_checkBoxTrim_actionAdapter implements java.awt.event.ActionListener {
    PanelFilter adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelFilter_checkBoxTrim_actionAdapter(PanelFilter adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxTrim_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelFilter_checkBoxRemoveMultipleIntermediateSpaces_actionAdapter implements java.awt.event.ActionListener {
    PanelFilter adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelFilter_checkBoxRemoveMultipleIntermediateSpaces_actionAdapter(PanelFilter adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxRemoveMultipleIntermediateSpaces_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelFilter_checkBoxSetNull_actionAdapter implements java.awt.event.ActionListener {
    PanelFilter adaptee;

    /**
     * Creates a new PanelFilter_checkBoxSetNull_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelFilter_checkBoxSetNull_actionAdapter(PanelFilter adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxSetNull_actionPerformed(e);
    }
}
