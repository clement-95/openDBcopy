/*
 * Copyright (C) 2003 Anthony Smith
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
package opendbcopy.gui;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;


/**
 * Copyright 2002 Sun Microsystems, Imc. All rights reserved. SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms. A convenience
 * implementation of FileFilter that filters out all files except for those type extensions that it knows about. Extensions are of the type ".foo",
 * which is typically found on Windows and Unix boxes, but not on Macinthosh. Case is ignored. Example - create a new filter that filerts out all
 * files but gif and jpg image files: JFileChooser chooser = new JFileChooser(); ExampleFileFilter filter = new ExampleFileFilter( new String{"gif",
 * "jpg"}, "JPEG & GIF Images") chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
 *
 * @author Jeff Dinkins
 * @version 1.20 02/06/02
 */
public class ExampleFileFilter extends FileFilter {
    private Hashtable filters = null;
    private String    description = null;
    private String    fullDescription = null;
    private boolean   useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all files are accepted.
     */
    public ExampleFileFilter() {
        this.filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts files with the given extension. Example: new ExampleFileFilter("jpg");
     *
     * @param extension DOCUMENT ME!
     */
    public ExampleFileFilter(String extension) {
        this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type. Example: new ExampleFileFilter("jpg", "JPEG Image Images"); Note that the "." before
     * the extension is not needed. If provided, it will be ignored.
     *
     * @param extension DOCUMENT ME!
     * @param description DOCUMENT ME!
     */
    public ExampleFileFilter(String extension,
                             String description) {
        this();

        if (extension != null) {
            addExtension(extension);
        }

        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Creates a file filter from the given string array. Example: new ExampleFileFilter(String {"gif", "jpg"}); Note that the "." before the
     * extension is not needed adn will be ignored.
     *
     * @param filters DOCUMENT ME!
     */
    public ExampleFileFilter(String[] filters) {
        this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description. Example: new ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG
     * Images"); Note that the "." before the extension is not needed and will be ignored.
     *
     * @param filters DOCUMENT ME!
     * @param description DOCUMENT ME!
     */
    public ExampleFileFilter(String[] filters,
                             String   description) {
        this();

        for (int i = 0; i < filters.length; i++) {
            // add filters one by one
            addExtension(filters[i]);
        }

        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Return true if this file should be shown in the directory pane, false if it shouldn't. Files that begin with "." are ignored.
     *
     * @param f DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);

            if ((extension != null) && (filters.get(getExtension(f)) != null)) {
                return true;
            }

            ;
        }

        return false;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @param f DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int    i = filename.lastIndexOf('.');

            if ((i > 0) && (i < (filename.length() - 1))) {
                return filename.substring(i + 1).toLowerCase();
            }

            ;
        }

        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against. For example: the following code will create a filter that filters out all files except
     * those that end in ".jpg" and ".tif": ExampleFileFilter filter = new ExampleFileFilter(); filter.addExtension("jpg");
     * filter.addExtension("tif"); Note that the "." before the extension is not needed and will be ignored.
     *
     * @param extension DOCUMENT ME!
     */
    public void addExtension(String extension) {
        if (filters == null) {
            filters = new Hashtable(5);
        }

        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }

    /**
     * Returns the human readable description of this filter. For example: "JPEG and GIF Image Files (.jpg, .gif)"
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        if (fullDescription == null) {
            if ((description == null) || isExtensionListInDescription()) {
                fullDescription = (description == null) ? "(" : (description + " (");

                // build the description from the extension list
                Enumeration extensions = filters.keys();

                if (extensions != null) {
                    fullDescription += ("." + (String) extensions.nextElement());

                    while (extensions.hasMoreElements()) {
                        fullDescription += (", " + (String) extensions.nextElement());
                    }
                }

                fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }

        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For example: filter.setDescription("Gif and JPG Images");
     *
     * @param description DOCUMENT ME!
     */
    public void setDescription(String description) {
        this.description     = description;
        fullDescription      = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should show up in the human readable description. Only relevent if a description was
     * provided in the constructor or using setDescription();
     *
     * @param b DOCUMENT ME!
     */
    public void setExtensionListInDescription(boolean b) {
        useExtensionsInDescription     = b;
        fullDescription                = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should show up in the human readable description. Only relevent if a description was
     * provided in the constructor or using setDescription();
     *
     * @return DOCUMENT ME!
     */
    public boolean isExtensionListInDescription() {
        return useExtensionsInDescription;
    }
}
