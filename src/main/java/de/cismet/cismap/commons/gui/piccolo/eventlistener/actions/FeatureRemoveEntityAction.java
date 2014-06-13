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

import com.vividsolutions.jts.geom.Polygon;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * Is used to remove added polygons from multipolygons
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureRemoveEntityAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private Feature f;
    private MappingComponent mc;
    private Polygon entity;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureRemoveEntityAction instance
     *
     * @param  mc      the MappingComponent, the parent polygon is contained in
     * @param  f       the feature, the polygone should be removed from
     * @param  entity  the new polygon
     */
    public FeatureRemoveEntityAction(final MappingComponent mc, final Feature f, final Polygon entity) {
        this.entity = entity;
        this.mc = mc;
        this.f = f;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Removes the entity
     */
    @Override
    public void doAction() {
        final PFeature pf = mc.getPFeatureHM().get(f);

        if (pf != null) {
            final int entryPosition = pf.getEntityPositionUnderPoint(entity.getExteriorRing().getStartPoint());
            pf.removeEntity(entryPosition);
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
                "FeatureRemoveEntityAction.info().return"); // NOI18N
    }

    /**
     * Delivers the inverse action.
     *
     * @return  the inverse operation
     */
    @Override
    public CustomAction getInverse() {
        return new FeatureAddEntityAction(mc, f, entity);
    }

    @Override
    public boolean featureConcerned(Feature feature) {
        return f != null && f.equals(feature);
    }
}
