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

import de.cismet.cismap.commons.LayerConfig;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerTransferable {

    //~ Instance fields --------------------------------------------------------

    private boolean folder;
    private Object data;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerTransferable object.
     *
     * @param  data  DOCUMENT ME!
     */
    public CidsLayerTransferable(final LayerConfig data) {
        this.data = data;
        folder = false;
    }

    /**
     * Creates a new CidsLayerTransferable object.
     *
     * @param  data  DOCUMENT ME!
     */
    public CidsLayerTransferable(final TreeFolder data) {
        this.data = data;
        folder = true;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFolder() {
        return folder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LayerConfig getLayerConfig() {
        if (!folder) {
            return (LayerConfig)data;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreeFolder getFolder() {
        if (folder) {
            return (TreeFolder)data;
        } else {
            return null;
        }
    }
}
