/*
 * MeasurementListener.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 23. M\u00E4rz 2006, 15:25
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Vector;
import edu.umd.cs.piccolox.event.PNotificationCenter;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class MeasurementListener extends PBasicInputEventHandler {
    public static final String LENGTH_CHANGED = "LENGTH_CHANGED";//NOI18N
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected Point2D startPoint;
    protected PPath tempFeature;
    protected MappingComponent mc;
    protected boolean inProgress;
    private Vector points;
    private SimpleMoveListener moveListener;
    private double measuredLength = 0;

    /** Creates a new instance of CreatePolygonFeatureListener */
    public MeasurementListener(MappingComponent mc) {
        this.mc = mc;
        moveListener = (SimpleMoveListener) mc.getInputListener(MappingComponent.MOTION);
    }

    @Override
    public void mouseMoved(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");//NOI18N
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
//    public void mouseDragged(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
//         mouseMoved(pInputEvent);
//    }
    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (pInputEvent.getButton() == 1) { //Linke Maustaste: TODO: konnte die piccolo Konstanten nicht finden
            if (pInputEvent.getClickCount() == 1) {
                Point2D point = null;
                if (mc.isSnappingEnabled()) {
                    point = PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition());
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                if (!inProgress) {
                    //Polygon erzeugen
                    tempFeature = new PPath();
                    points = new Vector();
                    FixedWidthStroke fws = new FixedWidthStroke();
                    fws.setMultiplyer(3f);
                    tempFeature.setStroke(fws);
                    //tempFeature.setPaint(getFillingColor());
                    mc.getTmpFeatureLayer().removeAllChildren();
                    mc.getTmpFeatureLayer().addChild(tempFeature);
                    //Ersten Punkt anlegen
                    startPoint = point;
                    points.add(startPoint);
                    inProgress = true;
                } else {
                    //Zus\u00E4tzlichen Punkt anlegen
                    points.add(point);
                    updatePolygon(null);
                }
            } else if (pInputEvent.getClickCount() == 2) {
                //Anlegen des neuen PFeatures
                mc.getTmpFeatureLayer().removeAllChildren();
                measuredLength = getLength(getPoints(null));
                postLength();
                //PFeature newFeature=new PFeature(getPoints(null),mc.getWtst(),mc.getClip_offset_x(),mc.getClip_offset_y());
                //newFeature.setViewer(mc);
                //mc.getFeatureLayer().addChild(newFeature);
                inProgress = false;
            }
        }
    }

    private double getLength(Point2D[] canvasPoints) {
        Coordinate[] coordArr = new Coordinate[canvasPoints.length];
        float[] xp = new float[canvasPoints.length];
        float[] yp = new float[canvasPoints.length];
        for (int i = 0; i < canvasPoints.length; ++i) {
            xp[i] = (float) (canvasPoints[i].getX());
            yp[i] = (float) (canvasPoints[i].getY());
            coordArr[i] = new Coordinate(mc.getWtst().getSourceX(xp[i] - mc.getClip_offset_x()), mc.getWtst().getSourceY(yp[i] - mc.getClip_offset_y()));
        }
        CoordinateSequence cs = new PackedCoordinateSequenceFactory().create(coordArr);
        LineString ls = new LineString(cs, new GeometryFactory());
        double l = ls.getLength();
        return l;
    }

    protected Color getFillingColor() {
        return new Color(1f, 0f, 0f, 0.5f);
    }

    protected void updatePolygon(Point2D lastPoint) {
        Point2D[] p = getPoints(lastPoint);
        tempFeature.setPathToPolyline(p);
        tempFeature.repaint();
        measuredLength = getLength(p);
        postLength();
    }

    protected Point2D[] getPoints(Point2D lastPoint) {
        int plus;
        boolean movin = false;
        if (lastPoint != null) {
            plus = 1;
            movin = true;
        } else {
            plus = 0;
            movin = false;
        }
        Point2D[] p = new Point2D[points.size() + plus];
        for (int i = 0; i < points.size(); ++i) {
            p[i] = (Point2D) (points.get(i));
        }
        if (movin) {
            p[points.size()] = lastPoint;
        }
        return p;
    }

    protected void postLength() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(LENGTH_CHANGED, this);
    }

    public double getMeasuredLength() {
        return measuredLength;
    }

    public void setMeasuredLength(double measuredLength) {
        this.measuredLength = measuredLength;
    }
}

