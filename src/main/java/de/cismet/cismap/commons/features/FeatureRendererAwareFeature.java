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
package de.cismet.cismap.commons.features;

/**
 * Should be implemented by all features, which can contain feature renderer.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureRendererAwareFeature {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureRenderer getFeatureRenderer();
}
