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
package opendbcopy.io;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class ImportFromXML {
    /**
     * DOCUMENT ME!
     *
     * @param pathFilename DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws FileNotFoundException    DOCUMENT ME!
     * @throws IOException              DOCUMENT ME!
     * @throws JDOMException            DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Document importFile(String pathFilename) throws IOException, JDOMException {
        if ((pathFilename == null) || (pathFilename.length() == 0)) {
            throw new IllegalArgumentException("Missing pathFilename");
        }

        Document doc;

        SAXBuilder parser = new SAXBuilder();
        doc = parser.build(FileHandling.getFile(pathFilename));

        return doc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws FileNotFoundException    DOCUMENT ME!
     * @throws IOException              DOCUMENT ME!
     * @throws JDOMException            DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Document importFile(File file) throws IOException, JDOMException {
        if (file == null) {
            throw new IllegalArgumentException("Missing file");
        }

        Document doc;

        SAXBuilder parser = new SAXBuilder();
        doc = parser.build(file);

        return doc;
    }
}
