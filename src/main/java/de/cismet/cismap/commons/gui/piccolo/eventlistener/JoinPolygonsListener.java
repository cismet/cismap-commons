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
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class JoinPolygonsListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FEATURE_JOIN_REQUEST_NOTIFICATION = "FEATURE_JOIN_REQUEST_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature featureRequestedForJoin = null;
    int modifier = -1;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class },
                true);
        modifier = pInputEvent.getModifiers();
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            featureRequestedForJoin = (PFeature)(o);
            postFeatureJoinRequest();
        } else {
            featureRequestedForJoin = null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postFeatureJoinRequest() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(FEATURE_JOIN_REQUEST_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureRequestedForJoin() {
        return featureRequestedForJoin;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getModifier() {
        return modifier;
    }
}
