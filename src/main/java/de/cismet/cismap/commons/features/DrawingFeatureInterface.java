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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface DrawingFeatureInterface {

    //~ Instance fields --------------------------------------------------------

    List<AbstractNewFeature.geomTypes> TYPE_ORDER = new ArrayList<AbstractNewFeature.geomTypes>(Arrays.asList(
                AbstractNewFeature.geomTypes.TEXT,
                AbstractNewFeature.geomTypes.POINT,
                AbstractNewFeature.geomTypes.LINESTRING,
                AbstractNewFeature.geomTypes.POLYGON));

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getTypeOrder();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    AbstractNewFeature.geomTypes getGeometryType();
}
