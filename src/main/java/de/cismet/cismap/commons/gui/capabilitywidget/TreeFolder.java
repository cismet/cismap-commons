/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.capabilitywidget;

import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TreeFolder extends ArrayList<Object> {

    //~ Instance fields --------------------------------------------------------

    private String name;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TreeFolder object.
     *
     * @param  name  DOCUMENT ME!
     */
    public TreeFolder(final String name) {
        this.name = name;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TreeFolder) {
            return ((TreeFolder)obj).name.equals(name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (37 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        return hash;
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
}
