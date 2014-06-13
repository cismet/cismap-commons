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
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * Is used to restore removed holes from (multi)polygons.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureAddHoleAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private Feature f;
    private MappingComponent mc;
    private LineString hole;
    private int position;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureAddHoleAction instance.
     *
     * @param  mc        the MappingComponent, the parent polygon is contained in
     * @param  f         the feature, the hole should be added to
     * @param  position  the postion of the hole in the polygon
     * @param  hole      the new hole
     */
    public FeatureAddHoleAction(final MappingComponent mc, final Feature f, final int position, final LineString hole) {
        this.hole = hole;
        this.mc = mc;
        this.f = f;
        this.position = position;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Adds the hole to the feature.
     */
    @Override
    public void doAction() {
        final PFeature pf = mc.getPFeatureHM().get(f);

        if (pf != null) {
            pf.addHoleToEntity(position, hole);
        }
    }

    /**
     * delivers the description of the action as string.
     *
     * @return  a description of the action
     */
    @Override
    public String info() {
        return org.openide.util.NbBundle.getMessage(
                FeatureCreateAction.class,
                "FeatureAddHoleAction.info().return"); // NOI18N
    }

    /**
     * Delivers the inverse action.
     *
     * @return  the inverse operation
     */
    @Override
    public CustomAction getInverse() {
        return new FeatureRemoveHoleAction(mc, f, position, hole);
    }

    @Override
    public boolean featureConcerned(final Feature feature) {
        return (f != null) && f.equals(feature);
    }
}
