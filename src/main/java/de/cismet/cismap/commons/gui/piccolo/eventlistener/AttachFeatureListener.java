/*
 * DragFeatureListener.java
 *
 * Created on 20. April 2005, 14:43
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

/**
 *
 * @author hell
 */
public class AttachFeatureListener extends PBasicInputEventHandler {
    public static final String ATTACH_FEATURE_NOTIFICATION = "ATTACH_FEATURE_NOTIFICATION";//NOI18N
    PFeature featureToAttach = null;

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            featureToAttach = (PFeature) (o);
            postFeatureAttachRequest();
        } else {
            featureToAttach = null;
        }
    }

    private void postFeatureAttachRequest() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(ATTACH_FEATURE_NOTIFICATION, this);
    }

    public PFeature getFeatureToAttach() {
        return featureToAttach;
    }
}
