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
package de.cismet.cismap.commons.gui.featureinfopanel;

import java.util.EventListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureInfoPanelListener extends EventListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    void featureSaved(FeatureInfoPanelEvent evt);

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    void dispose(FeatureInfoPanelEvent evt);
}
