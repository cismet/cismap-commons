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
package de.cismet.cismap.commons.gui.attributetable;

import java.util.EventObject;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureCreatedEvent extends EventObject {

    //~ Instance fields --------------------------------------------------------

    FeatureServiceFeature feature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureCreatedEvent object.
     *
     * @param  source   DOCUMENT ME!
     * @param  feature  DOCUMENT ME!
     */
    public FeatureCreatedEvent(final Object source, final FeatureServiceFeature feature) {
        super(source);
        this.feature = feature;
    }
}
