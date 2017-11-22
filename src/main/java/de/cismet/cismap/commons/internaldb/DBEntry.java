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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DBEntry {

    //~ Instance fields --------------------------------------------------------

    private String name;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBEntry object.
     *
     * @param  name  DOCUMENT ME!
     */
    public DBEntry(final String name) {
        this.name = name;
    }

    //~ Methods ----------------------------------------------------------------

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

    @Override
    public boolean equals(final Object obj) {
        return getClass().equals(obj.getClass()) && name.equals(((DBEntry)obj).name);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFolderName() {
        int end = 0;

        if (name.indexOf("->") != -1) {
            end = name.lastIndexOf("->");
        }

        if (end == 0) {
            return "";
        } else {
            return name.substring(0, end);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getHash() {
        int start = 0;

        if (name.lastIndexOf("_") != -1) {
            start = name.lastIndexOf("_") + 1;
        }

        if (start == 0) {
            return "";
        } else {
            return name.substring(start);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getNameWithoutFolder() {
        int start = 0;

        if (name.indexOf("->") != -1) {
            start = name.lastIndexOf("->") + 2;
        }

        return name.substring(start);
    }

    @Override
    public String toString() {
        int start = 0;
        int end = name.length();

        if (name.indexOf("->") != -1) {
            start = name.lastIndexOf("->") + 2;
        }
        if (!(this instanceof DBFolder)) {
            if ((name.lastIndexOf("_") != -1) && ((name.length() - name.lastIndexOf("_")) == 33)) {
                end = name.lastIndexOf("_");
            }
        }

        return name.substring(start, end);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (71 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        return hash;
    }
}
