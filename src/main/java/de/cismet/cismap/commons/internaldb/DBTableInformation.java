/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.internaldb;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DBTableInformation {

    //~ Instance fields --------------------------------------------------------

    private String databasePath;
    private String databaseTable;
    private String name;
    private boolean folder = false;
    private final List<DBTableInformation> children = new ArrayList<DBTableInformation>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBTableInformation object.
     *
     * @param  name           DOCUMENT ME!
     * @param  databasePath   DOCUMENT ME!
     * @param  databaseTable  DOCUMENT ME!
     * @param  folder         DOCUMENT ME!
     */
    public DBTableInformation(final String name,
            final String databasePath,
            final String databaseTable,
            final boolean folder) {
        this.databasePath = databasePath;
        this.databaseTable = databaseTable;
        this.name = name;
        this.folder = folder;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the databasePath
     */
    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  databasePath  the databasePath to set
     */
    public void setDatabasePath(final String databasePath) {
        this.databasePath = databasePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the databaseTable
     */
    public String getDatabaseTable() {
        return databaseTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  databaseTable  the databaseTable to set
     */
    public void setDatabaseTable(final String databaseTable) {
        this.databaseTable = databaseTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the name
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the folder
     */
    public boolean isFolder() {
        return folder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  folder  the folder to set
     */
    public void setFolder(final boolean folder) {
        this.folder = folder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the children
     */
    public List<DBTableInformation> getChildren() {
        return children;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  child  DOCUMENT ME!
     */
    public void addChild(final DBTableInformation child) {
        children.add(child);
    }
}
