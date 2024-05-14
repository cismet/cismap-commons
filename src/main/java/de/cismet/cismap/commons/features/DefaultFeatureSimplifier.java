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

import com.vividsolutions.jts.geom.Geometry;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DefaultFeatureSimplifier implements FeatureSimplifier {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry simplify(final Feature feature) {
        final Geometry geom = feature.getGeometry();

        return geom;
    }
}
