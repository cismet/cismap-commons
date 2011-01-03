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

import java.awt.geom.Point2D;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SplitPolygonListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SPLIT_FINISHED = "SPLIT_FINISHED";       // NOI18N
    public static final String SELECTION_CHANGED = "SELECTION_CHANGED"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    MappingComponent mc;
    PFeature p = null;
    private boolean inProgress;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SplitPolygonListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public SplitPolygonListener(final MappingComponent mc) {
        super();
        this.mc = mc;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (log.isDebugEnabled()) {
            log.debug("mouseClicked()"); // NOI18N
        }
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            p = (PFeature)(o);
            if (p.isSelected() == false) {
                mc.getFeatureCollection().select(p.getFeature());
            } else if (p.inSplitProgress()) {
                Point2D point = null;
                if (mc.isSnappingEnabled()) {
                    point = PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition());
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                if (p.inSplitProgress()) {
                    p.getSplitPoints().add(point);
                    updateSplitLine(null);
                }
            } else {
                postClickDetected();
            }
        } else {
            p = null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postClickDetected() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SPLIT_FINISHED, this);
    }

    /**
     * DOCUMENT ME!
     */
    private void postSelectionChanged() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SELECTION_CHANGED, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureClickedOn() {
        return p;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSelectedPFeature() {
        return p;
    }

//TODO
    @Override
    public void mouseMoved(final edu.umd.cs.piccolo.event.PInputEvent event) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[] { PFeature.class });
        p = (PFeature)o;
        if ((p == null) && (mc.getFeatureCollection().getSelectedFeatures().size() == 1)) {
            p = (PFeature)mc.getPFeatureHM().get(mc.getFeatureCollection().getSelectedFeatures().toArray()[0]);
            // p=mc.getSelectedNode();
        }
        if ((p != null) && p.inSplitProgress()) {
            if (log.isDebugEnabled()) {
                log.debug("want to draw line"); // NOI18N
            }
            Point2D point = null;
            if (mc.isSnappingEnabled()) {
                point = PFeatureTools.getNearestPointInArea(mc, event.getCanvasPosition());
            }
            if (point == null) {
                point = event.getPosition();
            }
            updateSplitLine((point));
        }
        super.mouseMoved(event);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastPoint  DOCUMENT ME!
     */
    private void updateSplitLine(final Point2D lastPoint) {
        final Point2D[] pa = getPoints(lastPoint);
        if (log.isDebugEnabled()) {
            log.debug("getSplitLine()" + p.getSplitLine()); // NOI18N
        }
        p.getSplitLine().setPathToPolyline(pa);
        p.getSplitLine().repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lastPoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point2D[] getPoints(final Point2D lastPoint) {
        int plus;
        boolean movin = false;
        if (lastPoint != null) {
            plus = 1;
            movin = true;
        } else {
            plus = 0;
            movin = false;
        }
        final Point2D[] pa = new Point2D[p.getSplitPoints().size() + plus];
        for (int i = 0; i < p.getSplitPoints().size(); ++i) {
            pa[i] = (Point2D)(p.getSplitPoints().get(i));
        }

        if (movin) {
            pa[p.getSplitPoints().size()] = lastPoint;
            // pa[p.getSplitPoints().size()+1]=p.getFirstSplitHandle();
        } else {
            // pa[p.getSplitPoints().size()]=p.getFirstSplitHandle();
        }
        return pa;
    }
}
