/*
 * DeleteFeatureListener.java
 *
 * Created on 20. April 2005, 11:22
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureCreateAction;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

/**
 *
 * @author hell
 */
public class DeleteFeatureListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String FEATURE_DELETE_REQUEST_NOTIFICATION = "FEATURE_DELETE_REQUEST_NOTIFICATION";
    PFeature featureRequestedForDeletion = null;

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            MappingComponent mc = (MappingComponent) pInputEvent.getComponent();
            Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
            if (o instanceof PFeature && o != null && ((PFeature) o).getFeature().isEditable() && ((PFeature) o).getFeature().canBeSelected()) {
                super.mouseClicked(pInputEvent);
                PFeature pf = (PFeature) o;
                featureRequestedForDeletion = (PFeature) pf.clone();
                mc.getFeatureCollection().removeFeature(pf.getFeature());
                mc.getMemUndo().addAction(new FeatureCreateAction(mc, pf.getFeature()));
                mc.getMemRedo().clear();
                postFeatureDeleteRequest();
            } else {
                featureRequestedForDeletion = null;
            }
        }
    }

    private void postFeatureDeleteRequest() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(FEATURE_DELETE_REQUEST_NOTIFICATION, this);
    }

    public PFeature getFeatureRequestedForDeletion() {
        return featureRequestedForDeletion;
    }
}
