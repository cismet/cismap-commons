/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
public class AttachFeatureListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ATTACH_FEATURE_NOTIFICATION = "ATTACH_FEATURE_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature featureToAttach = null;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            featureToAttach = (PFeature)(o);
            postFeatureAttachRequest();
        } else {
            featureToAttach = null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postFeatureAttachRequest() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(ATTACH_FEATURE_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureToAttach() {
        return featureToAttach;
    }
}
