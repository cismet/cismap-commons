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
package de.cismet.cismap.commons.gui.attributetable;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureCreator {

    //~ Instance fields --------------------------------------------------------

    String SIMPLE_GEOMETRY_LISTENER_KEY = "SimpleGeometryCreater";

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mc       DOCUMENT ME!
     * @param  feature  DOCUMENT ME!
     */
    void createFeature(MappingComponent mc, FeatureServiceFeature feature);
}
