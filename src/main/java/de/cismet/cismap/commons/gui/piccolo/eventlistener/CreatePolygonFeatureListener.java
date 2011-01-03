/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CreatePolygonFeatureListener.java
 *
 * Created on 19. April 2005, 09:33
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Vector;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class CreatePolygonFeatureListener extends PBasicInputEventHandler {

    //~ Instance fields --------------------------------------------------------

    protected Point2D startPoint;
    protected PPath tempFeature;
    protected MappingComponent mc;
    protected boolean inProgress;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Vector points;
    private SimpleMoveListener moveListener;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CreatePolygonFeatureListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public CreatePolygonFeatureListener(final MappingComponent mc) {
        this.mc = mc;
        moveListener = (SimpleMoveListener)mc.getInputListener(MappingComponent.MOTION);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden."); // NOI18N
        }

        if (inProgress) {
            Point2D point = null;
            if (mc.isSnappingEnabled()) {
                point = PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition());
            }
            if (point == null) {
                point = pInputEvent.getPosition();
            }
            updatePolygon(point);
        }
    }

//    public void mousePressed(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
//        mouseClicked(pInputEvent);
//    }
//
//    public void mouseDragged(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
//         mouseMoved(pInputEvent);
//    }

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (pInputEvent.getButton() == 1) { // Linke Maustaste: TODO: konnte die piccolo Konstanten nicht finden
            if (pInputEvent.getClickCount() == 1) {
                Point2D point = null;
                if (mc.isSnappingEnabled()) {
                    point = PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition());
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                if (!inProgress) {
                    // Polygon erzeugen
                    tempFeature = new PPath();
                    points = new Vector();
                    tempFeature.setStroke(new FixedWidthStroke());
                    tempFeature.setPaint(getFillingColor());
                    mc.getTmpFeatureLayer().removeAllChildren();
                    mc.getTmpFeatureLayer().addChild(tempFeature);
                    // Ersten Punkt anlegen
                    startPoint = point;
                    points.add(startPoint);
                    inProgress = true;
                } else {
                    // Zus\u00E4tzlichen Punkt anlegen
                    points.add(point);
                    updatePolygon(null);
                }
            } else if (pInputEvent.getClickCount() == 2) {
                // Anlegen des neuen PFeatures
                mc.getTmpFeatureLayer().removeAllChildren();
                final PFeature newFeature = new PFeature(getPoints(null),
                        mc.getWtst(),
                        mc.getClip_offset_x(),
                        mc.getClip_offset_y(),
                        mc);
                newFeature.setViewer(mc);
                mc.getFeatureLayer().addChild(newFeature);
                inProgress = false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Color getFillingColor() {
        return new Color(1f, 0f, 0f, 0.5f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastPoint  DOCUMENT ME!
     */
    protected void updatePolygon(final Point2D lastPoint) {
        final Point2D[] p = getPoints(lastPoint);
        tempFeature.setPathToPolyline(p);
        tempFeature.repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lastPoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Point2D[] getPoints(final Point2D lastPoint) {
        int plus;
        boolean movin = false;
        if (lastPoint != null) {
            plus = 2;
            movin = true;
        } else {
            plus = 1;
            movin = false;
        }
        final Point2D[] p = new Point2D[points.size() + plus];
        for (int i = 0; i < points.size(); ++i) {
            p[i] = (Point2D)(points.get(i));
        }
        if (movin) {
            p[points.size()] = lastPoint;
            p[points.size() + 1] = startPoint;
        } else {
            p[points.size()] = startPoint;
        }
        return p;
    }
}
