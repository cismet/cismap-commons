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
package de.cismet.cismap.commons.gui.piccolo;

import java.util.Comparator;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureWithId;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PFeatureComparable implements Comparator<Feature> {

    //~ Methods ----------------------------------------------------------------

    @Override
    public int compare(final Feature o1, final Feature o2) {
        // if tester and PFeature are FeatureWithId-objects
        if ((o1 instanceof FeatureWithId) && (o2 instanceof FeatureWithId)) {
            final Integer id1 = ((FeatureWithId)o1).getId();
            final Integer id2 = ((FeatureWithId)o2).getId();
            if ((id1 != -1) && (id2 != -1)) {
                return id1.compareTo(id2);
            } else {
                return o1.getGeometry().compareTo(o2.getGeometry());
            }
        } else { // no FeatureWithId, compare geometries
            return o1.getGeometry().compareTo(o2.getGeometry());
        }
    }
}
