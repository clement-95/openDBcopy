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
package opendbcopy.plugin.script;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.io.Writer;

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.database.typeinfo.TypeInfo;
import opendbcopy.plugin.model.database.typeinfo.TypeInfoHelper;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.sql.Helper;

import opendbcopy.util.IntHashMap;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class InsertScriptPlugin extends DynamicPluginThread {
    private DatabaseModel model;
    private Connection    connSource;
    private Statement     stmSource;
    private ResultSet     rs;
    private StringBuffer  sbScript;
    private String        path = "";
    private String        newLine = "";
    private String        database = "";
    private String        identifierQuoteStringOut = "";
    private List          processTables = null;
    private boolean       show_qualified_table_name = false;
    private int           counterRecords = 0;
    private int           counterTables = 0;

    /**
     * Creates a new InsertScriptPlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public InsertScriptPlugin(MainController controller,
                              Model          baseModel) throws PluginException {
        super(controller, baseModel);
        this.model = (DatabaseModel) baseModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected void setUp() throws PluginException {
        try {
            newLine = System.getProperty("line.separator");

            // read the plugin configuration
            Element conf = model.getConf();
            path                          = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE);
            show_qualified_table_name     = Boolean.valueOf(conf.getChild("show_qualified_table_name").getAttributeValue(XMLTags.VALUE)).booleanValue();

            if (model.getDbMode() == model.DUAL_MODE) {
                identifierQuoteStringOut     = model.getDestinationMetadata().getChild(XMLTags.IDENTIFIER_QUOTE_STRING).getAttributeValue(XMLTags.VALUE);
                database                     = model.getDestinationDb().getName();
            } else {
                identifierQuoteStringOut     = model.getSourceMetadata().getChild(XMLTags.IDENTIFIER_QUOTE_STRING).getAttributeValue(XMLTags.VALUE);
                database                     = model.getSourceDb().getName();
            }

            // get connection
            connSource = DBConnection.getConnection(model.getSourceConnection());

            // extract the tables to dump
            if (model.getDbMode() == model.DUAL_MODE) {
                processTables = model.getDestinationTablesToProcessOrdered();
            } else {
                processTables = model.getSourceTablesToProcessOrdered();
            }

            // now set the number of tables that need to be copied
            model.setLengthProgressTable(processTables.size());
        } catch (Exception e) {
            throw new PluginException(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public void execute() throws PluginException {
        try {
            stmSource = connSource.createStatement();

            String   sourceTableName = "";
            String   destinationTableName = "";
            String   qualifiedTableName = "";
            String   tableName = "";
            String   fileName = "";
            String   selectStm = "";
            Iterator itProcessTables = processTables.iterator();

            while (itProcessTables.hasNext() && !isInterrupted()) {
                Element tableProcess = (Element) itProcessTables.next();
                List    processColumns = null;
                counterRecords = 0;

                if (model.getDbMode() == model.DUAL_MODE) {
                    sourceTableName          = tableProcess.getAttributeValue(XMLTags.SOURCE_DB);
                    destinationTableName     = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);
                    processColumns           = model.getMappingColumnsToProcessByDestinationTable(destinationTableName);
                    fileName                 = path + counterTables + "_" + destinationTableName + ".sql";
                    selectStm                = Helper.getSelectStatement(model, sourceTableName, XMLTags.SOURCE_DB, processColumns);

                    if (show_qualified_table_name) {
                        qualifiedTableName = model.getQualifiedDestinationTableName(destinationTableName);
                    } else {
                        qualifiedTableName = destinationTableName;
                    }

                    tableName = destinationTableName;
                } else {
                    sourceTableName     = tableProcess.getAttributeValue(XMLTags.NAME);
                    processColumns      = model.getSourceColumnsToProcess(sourceTableName);
                    fileName            = path + counterTables + "_" + sourceTableName + ".sql";
                    selectStm           = Helper.getSelectStatement(model, sourceTableName, XMLTags.NAME, processColumns);

                    if (show_qualified_table_name) {
                        qualifiedTableName = model.getQualifiedSourceTableName(sourceTableName);
                    } else {
                        qualifiedTableName = sourceTableName;
                    }

                    tableName = sourceTableName;
                }

                // Reading number of records for progress bar
                model.setLengthProgressRecord(Helper.getNumberOfRecordsFiltered(stmSource, model, XMLTags.SOURCE_DB, sourceTableName));
                model.setCurrentProgressRecord(counterRecords);

                model.setProgressMessage("Reading " + qualifiedTableName + " ...");

                logger.info("Reading " + qualifiedTableName + " ...");

                // initalise stringBuffer sbScript and script header
                initScriptHeader(qualifiedTableName, counterTables);

                ResultSet srcResult = stmSource.executeQuery(selectStm);

                genInserts(srcResult, tableName, qualifiedTableName, processColumns, sbScript);

                srcResult.close();

                Writer.write(sbScript, fileName);

                model.setCurrentProgressTable(++counterTables);

                logger.info(counterRecords + " records written to file " + fileName);
            }

            if (!isInterrupted()) {
                logger.info(counterTables + " table(s) processed");
            }
        } catch (SQLException sqle) {
            throw new PluginException(sqle);
        } catch (Exception e1) {
            try {
                DBConnection.closeConnection(connSource);
            } catch (CloseConnectionException e2) {
                // bad luck ... don't worry
            }

            throw new PluginException(e1.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param srcResult DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     * @param qualifiedTableName DOCUMENT ME!
     * @param processColumns DOCUMENT ME!
     * @param sbScript DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private void genInserts(ResultSet    srcResult,
                            String       tableName,
                            String       qualifiedTableName,
                            List         processColumns,
                            StringBuffer sbScript) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, SQLException {
        Iterator     itProcessColumns = processColumns.iterator();
        StringBuffer sbColumnNames = new StringBuffer();

        int          nbrCols = 1;
        IntHashMap   colTypeInfo = new IntHashMap();

        while (itProcessColumns.hasNext()) {
            Element column = (Element) itProcessColumns.next();

            if (model.getDbMode() == model.DUAL_MODE) {
                column = model.getDestinationColumn(tableName, column.getAttributeValue(XMLTags.DESTINATION_DB));
            }

            sbColumnNames.append(column.getAttributeValue(XMLTags.NAME));

            colTypeInfo.put(nbrCols, new TypeInfo(model.getTypeInfoByTypeName(column.getAttributeValue(XMLTags.TYPE_NAME), database)));

            if (nbrCols < processColumns.size()) {
                sbColumnNames.append(", ");
                nbrCols++;
            }
        }

        String in = null;

        while (srcResult.next() && !isInterrupted()) {
            sbScript.append("insert into ");
            sbScript.append(qualifiedTableName);
            sbScript.append(" (" + sbColumnNames.toString() + ")");
            sbScript.append(" values (");

            // read columns and append them
            for (int i = 1; i < (nbrCols + 1); i++) {
                in = srcResult.getString(i);

                if (srcResult.wasNull()) {
                    sbScript.append("null");
                } else {
                    sbScript.append(TypeInfoHelper.getFormattedString(in, (TypeInfo) colTypeInfo.get(i), identifierQuoteStringOut));
                }

                if (i < (nbrCols)) {
                    sbScript.append(", ");
                }
            }

            sbScript.append(");");
            sbScript.append(newLine);

            model.setCurrentProgressRecord(++counterRecords);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     * @param processOrder DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void initScriptHeader(String tableName,
                                  int    processOrder) throws Exception {
        sbScript = new StringBuffer();

        sbScript.append("#############################################################################" + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("# Script for table " + tableName + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("# Load this script as #" + processOrder + " if there are referential integrity constraints set" + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("# generated on " + new Date().toString() + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("#############################################################################" + newLine);
    }
}
