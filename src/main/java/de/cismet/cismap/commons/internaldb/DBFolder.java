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
public class DBFolder extends DBEntry {

    //~ Instance fields --------------------------------------------------------

    private List<DBEntry> children = new ArrayList<DBEntry>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DbFolder object.
     *
     * @param  name  DOCUMENT ME!
     */
    public DBFolder(final String name) {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the children
     */
    public List<DBEntry> getChildren() {
        return children;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  child  the children to set
     */
    public void addChildren(final DBEntry child) {
        this.children.add(child);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  child  the children to set
     * @param  index  DOCUMENT ME!
     */
    public void addChildren(final DBEntry child, final int index) {
        this.children.add((index > children.size()) ? children.size() : index, child);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   folder  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean contains(final DBEntry folder) {
        return children.contains(folder);
    }
}
