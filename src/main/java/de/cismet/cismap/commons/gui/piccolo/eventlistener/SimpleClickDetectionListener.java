/*
 * SimpleClickDetectionListener.java
 *
 * Created on 28. April 2005, 16:51
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
public class SimpleClickDetectionListener extends PBasicInputEventHandler {
    public static final String CLICK_DETECTED = "CLICK_DETECTED";//NOI18N
    PFeature p = null;

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            p = (PFeature) (o);
            postClickDetected();
        } else {
            p = null;
        }
    }

    private void postClickDetected() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(CLICK_DETECTED, this);
    }

    public PFeature getFeatureClickedOn() {
        return p;
    }
}
