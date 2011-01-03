/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleClickDetectionListener.java
 *
 * Created on 28. April 2005, 16:51
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class SimpleClickDetectionListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String CLICK_DETECTED = "CLICK_DETECTED"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature p = null;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            p = (PFeature)(o);
            postClickDetected();
        } else {
            p = null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postClickDetected() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(CLICK_DETECTED, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureClickedOn() {
        return p;
    }
}
