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
package de.cismet.cismap.commons.gui.layerwidget;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface ActiveLayerModelStore {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  alm  DOCUMENT ME!
     */
    void setActiveLayerModel(ActiveLayerModel alm);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ActiveLayerModel getActiveLayerModel();
}
