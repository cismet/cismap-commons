/*
 * SplitPolygonListener.java
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
 * Created on 27. September 2005, 16:47
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import java.awt.geom.Point2D;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SplitPolygonListener extends PBasicInputEventHandler {
    private boolean inProgress;
    public static final String SPLIT_FINISHED = "SPLIT_FINISHED";
    public static final String SELECTION_CHANGED = "SELECTION_CHANGED";
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    MappingComponent mc;
    PFeature p = null;
    
    public SplitPolygonListener(MappingComponent mc) {
        super();
        this.mc = mc;
    }
    
    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        log.debug("mouseClicked()");
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            p = (PFeature) (o);
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

    private void postClickDetected() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SPLIT_FINISHED, this);
    }

    private void postSelectionChanged() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SELECTION_CHANGED, this);
    }

    public PFeature getFeatureClickedOn() {
        return p;
    }

    public PFeature getSelectedPFeature() {
        return p;
    }

//TODO
    @Override
    public void mouseMoved(edu.umd.cs.piccolo.event.PInputEvent event) {
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[]{PFeature.class});
        p = (PFeature) o;
        if (p == null && mc.getFeatureCollection().getSelectedFeatures().size() == 1) {
            p = (PFeature) mc.getPFeatureHM().get(mc.getFeatureCollection().getSelectedFeatures().toArray()[0]);
        //p=mc.getSelectedNode();
        }
        if (p != null && p.inSplitProgress()) {
            log.debug("will Linie zeichnen");
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

    private void updateSplitLine(Point2D lastPoint) {
        Point2D[] pa = getPoints(lastPoint);
        log.debug("getSplitLine()" + p.getSplitLine());
        p.getSplitLine().setPathToPolyline(pa);
        p.getSplitLine().repaint();
    }

    private Point2D[] getPoints(Point2D lastPoint) {
        int plus;
        boolean movin = false;
        if (lastPoint != null) {
            plus = 1;
            movin = true;
        } else {
            plus = 0;
            movin = false;
        }
        Point2D[] pa = new Point2D[p.getSplitPoints().size() + plus];
        for (int i = 0; i < p.getSplitPoints().size(); ++i) {
            pa[i] = (Point2D) (p.getSplitPoints().get(i));
        }
        
        if (movin) {
            pa[p.getSplitPoints().size()] = lastPoint;
        //pa[p.getSplitPoints().size()+1]=p.getFirstSplitHandle();
        } else {
        //pa[p.getSplitPoints().size()]=p.getFirstSplitHandle();
        }
        return pa;
    }
}
