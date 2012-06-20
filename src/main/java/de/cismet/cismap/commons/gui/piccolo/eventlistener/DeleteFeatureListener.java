/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DeleteFeatureListener.java
 *
 * Created on 20. April 2005, 11:22
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import java.util.Collection;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureCreateAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class DeleteFeatureListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DeleteFeatureListener.class);
    public static final String FEATURE_DELETE_REQUEST_NOTIFICATION = "FEATURE_DELETE_REQUEST_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature featureRequestedForDeletion = null;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mappingComponent = (MappingComponent)pInputEvent.getComponent();
            if (pInputEvent.isAltDown()) {
                final Collection selectedFeatures = mappingComponent.getFeatureCollection().getSelectedFeatures();
                if (selectedFeatures.size() == 1) {
                    final PFeature pFeature = mappingComponent.getPFeatureHM()
                                .get((Feature)selectedFeatures.toArray()[0]);
                    if ((pFeature != null) && (pFeature.getFeature().getGeometry() instanceof MultiPolygon)) {
                        if (pFeature.getNumOfEntities() == 1) {
                            deletePFeature(pFeature, mappingComponent);
                        } else {
                            final Coordinate mouseCoord = new Coordinate(
                                    mappingComponent.getWtst().getSourceX(
                                        pInputEvent.getPosition().getX()
                                                - mappingComponent.getClip_offset_x()),
                                    mappingComponent.getWtst().getSourceY(
                                        pInputEvent.getPosition().getY()
                                                - mappingComponent.getClip_offset_y()));
                            final int selectedEntityPosition = pFeature.getEntityUnderCoordinate(mouseCoord);
                            if (selectedEntityPosition >= 0) {
                                pFeature.removeEntity(selectedEntityPosition);
                            } else {
                                pFeature.removeHoleUnderCoordinate(mouseCoord);
                            }
                        }
                    }
                } else {
                    final PFeature pFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                            pInputEvent,
                            new Class[] { PFeature.class });
                    if (pFeature != null) {
                        mappingComponent.getFeatureCollection().select(pFeature.getFeature());
                    }
                }
            } else {
                final PFeature pFeatureUnderPointer = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                        pInputEvent,
                        new Class[] { PFeature.class });
                if (pFeatureUnderPointer != null) {
                    deletePFeature(pFeatureUnderPointer, mappingComponent);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pFeature          DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     */
    private void deletePFeature(final PFeature pFeature, final MappingComponent mappingComponent) {
        if (pFeature.getFeature().isEditable() && pFeature.getFeature().canBeSelected()) {
            featureRequestedForDeletion = (PFeature)pFeature.clone();
            mappingComponent.getFeatureCollection().removeFeature(pFeature.getFeature());
            mappingComponent.getMemUndo().addAction(new FeatureCreateAction(mappingComponent, pFeature.getFeature()));
            mappingComponent.getMemRedo().clear();
            postFeatureDeleteRequest();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postFeatureDeleteRequest() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(FEATURE_DELETE_REQUEST_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureRequestedForDeletion() {
        return featureRequestedForDeletion;
    }
}
