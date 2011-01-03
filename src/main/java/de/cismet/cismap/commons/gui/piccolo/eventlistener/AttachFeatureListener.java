/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DragFeatureListener.java
 *
 * Created on 20. April 2005, 14:43
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import java.util.Vector;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class AttachFeatureListener extends RectangleRubberBandListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ATTACH_FEATURE_NOTIFICATION = "ATTACH_FEATURE_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PFeature featureToAttach = null;
    private MappingComponent mc = null;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class },
                30.5d);
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            featureToAttach = (PFeature)(o);
            postFeatureAttachRequest();
        } else {
            featureToAttach = null;
        }
    }

    @Override
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        if (event.getButton() == 1) { // linke Maustaste
            // Mouseevent muss von einer MappingComponent gefeuert werden
            if (event.getComponent() instanceof MappingComponent) {
                mc = (MappingComponent)event.getComponent();
                mc.getHandleLayer().removeAllChildren();
                if (log.isDebugEnabled()) {
                    // Hole alle PFeatures die das Markierviereck schneiden
                    // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                    log.debug("Markierviereck = (X=" + rectangle.getBounds().getX() + ",Y="
                                + rectangle.getBounds().getY() // NOI18N
                                + ",W=" + rectangle.getBounds().getWidth() + ",H=" + rectangle.getBounds().getHeight()
                                + ")");                        // NOI18N
                }

                final PFeature[] pfArr = PFeatureTools.getPFeaturesInArea(mc, rectangle.getBounds());

                if (pfArr.length == 1) {
                    featureToAttach = pfArr[0];
                    postFeatureAttachRequest();
                } else {
                    JOptionPane.showMessageDialog(
                        mc,
                        "Bitte nur ein Objekt zum zuordnen auswählen.",
                        "Mehr als ein Objekt markiert.",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
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
