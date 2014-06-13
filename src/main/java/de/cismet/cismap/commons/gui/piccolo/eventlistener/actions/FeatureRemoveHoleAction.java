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

import com.vividsolutions.jts.geom.LineString;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * Is used to remove added holes from (multi)polygons
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureRemoveHoleAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private Feature f;
    private MappingComponent mc;
    private LineString hole;
    private int position;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureRemoveHoleAction instance
     *
     * @param  mc      the MappingComponent, the parent polygon is contained in
     * @param  f       the feature, the hole should be removed from
     * @param  postion the postion of the hole in the polygon
     * @param  hole    the hole to remove
     */
    public FeatureRemoveHoleAction(final MappingComponent mc,
            final Feature f,
            final int position,
            final LineString hole) {
        this.hole = hole;
        this.mc = mc;
        this.f = f;
        this.position = position;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Removes the hole
     */
    @Override
    public void doAction() {
        f.equals(f);
        final PFeature pf = mc.getPFeatureHM().get(f);

        if (pf != null) {
            pf.removeHoleUnderPoint(hole.getStartPoint());
        }
    }

    /**
     * delivers the description of the action as string
     *
     * @return  a description of the action
     */
    @Override
    public String info() {
        return org.openide.util.NbBundle.getMessage(
                FeatureCreateAction.class,
                "FeatureRemoveHoleAction.info().return"); // NOI18N
    }

    /**
     * Delivers the inverse action.
     *
     * @return  the inverse operation
     */
    @Override
    public CustomAction getInverse() {
        return new FeatureAddHoleAction(mc, f, position, hole);
    }

    @Override
    public boolean featureConcerned(Feature feature) {
        return f != null && f.equals(feature);
    }
}
