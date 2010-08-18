/*
 * CreateGeometryListener.java
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
 * Created on 29. Mai 2006, 11:55
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.EllipsePHandle;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.RectangleFromLineDialog;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureDeleteAction;
import de.cismet.cismap.commons.tools.PFeatureTools;
import de.cismet.tools.gui.StaticSwingTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import java.awt.Color;
import java.awt.Frame;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CreateGeometryListener extends PBasicInputEventHandler implements FeatureCollectionListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected Point2D startPoint;
    protected PPath tempFeature;
    protected MappingComponent mc;
    protected boolean inProgress;
    private Vector<Point2D> points;
    private Stack<Point2D> undoPoints;
    private int numOfEllipseEdges;
    private SimpleMoveListener moveListener;
    private String mode = POLYGON;
    public static final String LINESTRING = "LINESTRING";//NOI18N
    public static final String POINT = "POINT";//NOI18N
    public static final String POLYGON = "POLYGON";//NOI18N
    public static final String RECTANGLE = "BOUNDING_BOX";//NOI18N
    public static final String RECTANGLE_FROM_LINE = "RECTANGLE_FROM_LINE";//NOI18N
    public static final String ELLIPSE = "ELLIPSE";//NOI18N
    private static final int NUMOF_ELLIPSE_EDGES = 36;
    private Class<? extends PureNewFeature> geometryFeatureClass = null;
    public static final String GEOMETRY_CREATED_NOTIFICATION = "GEOMETRY_CREATED_NOTIFICATION";//NOI18N

    /** Creates a new instance of CreateGeometryListener */
    protected CreateGeometryListener(MappingComponent mc, Class geometryFeatureClass) {
        setGeometryFeatureClass(geometryFeatureClass);
        this.mc = mc;
        moveListener = (SimpleMoveListener) mc.getInputListener(MappingComponent.MOTION);
        undoPoints = new Stack<Point2D>();
        //srichter: fehlerpotential! this referenz eines nicht fertig initialisieren Objekts wieder nach aussen geliefert!
        //loesungsvorschlag: createInstance-methode, welche den aufruf nach dem erzeugen ausfuehrt.
        mc.getFeatureCollection().addFeatureCollectionListener(this);
    }

    public CreateGeometryListener(MappingComponent mc) {
        this(mc, Feature.class);
    }

    public void setMode(String m) throws IllegalArgumentException {
        if (m.equals(LINESTRING) || m.equals(POINT) || m.equals(POLYGON) || m.equals(ELLIPSE) || m.equals(RECTANGLE) || m.equals(RECTANGLE_FROM_LINE)) {
            this.mode = m;
//            mc.getTmpFeatureLayer().removeAllChildren();
//            inProgress = false;
        } else {
            throw new IllegalArgumentException("Mode:" + m + " is not a valid Mode in this Listener.");//NOI18N
        }
    }

    @Override
    public void mouseMoved(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");//NOI18N
        }
        if (inProgress) {// && (!isInMode(POINT))) {
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

    @Override
    public void mousePressed(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (mc.isReadOnly()) {
            ((DefaultFeatureCollection) (mc.getFeatureCollection())).removeFeaturesByInstance(Feature.class);
        }
        if (isInMode(POINT)) {
            if (pInputEvent.isLeftMouseButton()) {
                Point2D point = null;
                if (mc.isSnappingEnabled()) {
                    point = PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition());
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                points = new Vector<Point2D>();
                points.add(point);
                createPureNewFeature(PureNewFeature.geomTypes.POINT);
            }
        } else if (isInMode(RECTANGLE)) {
            if (!inProgress) {
                initTempFeature(true);
                startPoint = pInputEvent.getPosition();
            }
        } else if (isInMode(ELLIPSE)) {
            if (!inProgress) {
                initTempFeature(true);
                startPoint = pInputEvent.getPosition();
            }
        } else if (isInMode(RECTANGLE_FROM_LINE)) {
            if (!inProgress) {
                inProgress = true;
                initTempFeature(true);
                startPoint = pInputEvent.getPosition();
                points = new Vector<Point2D>(5);
                points.add(startPoint);
            } else {
                Point2D stopPoint = pInputEvent.getPosition();
                points.add(stopPoint);
                points.add(stopPoint);
                points.add(startPoint);
                points.add(startPoint);

                double distance = startPoint.distance(stopPoint);
                Frame frame = StaticSwingTools.getParentFrame(mc);

                final RectangleFromLineDialog dialog = new RectangleFromLineDialog(frame, true, distance);
                dialog.addWidthChangedListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent ce) {
                        double height = dialog.getRectangleWidth();
                        boolean isLefty = dialog.isLefty();

                        Point2D startPoint = points.get(0);
                        Point2D stopPoint = points.get(1);

                        double deltaX = stopPoint.getX() - startPoint.getX();
                        double deltaY = stopPoint.getY() - startPoint.getY();

                        double alpha = Math.atan2(deltaY, deltaX);
                        double alpha90 = alpha + Math.toRadians((isLefty) ? -90 : 90);

                        double x = Math.cos(alpha90) * height;
                        double y = Math.sin(alpha90) * height;

                        points.set(2, new Point2D.Double(x + stopPoint.getX(), y + stopPoint.getY()));
                        points.set(3, new Point2D.Double(x + startPoint.getX(), y + startPoint.getY()));

                        updatePolygon(null);

                    }

                });
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == RectangleFromLineDialog.STATUS_OK) {
                    createPureNewFeature(PureNewFeature.geomTypes.POLYGON);
                } else {
                    mc.getTmpFeatureLayer().removeChild(tempFeature);
                }
                inProgress = false;
            }
        } else if (isInMode(POLYGON) || isInMode(LINESTRING)) {
            if (pInputEvent.getClickCount() == 1) {
                Point2D point = null;
                undoPoints.clear();
                if (mc.isSnappingEnabled()) {
                    point = PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition());
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }

                if (!inProgress) {
                    inProgress = true;

                    if (isInMode(POLYGON)) {
                        initTempFeature(true);
                    } else {
                        initTempFeature(false);
                    }

                    //Polygon erzeugen
                    points = new Vector<Point2D>();
                    //Ersten Punkt anlegen
                    startPoint = point;
                    points.add(startPoint);
                } else {
                    //Zus\u00E4tzlichen Punkt anlegen
                    points.add(point);
                    updatePolygon(null);
                }
            } else if (pInputEvent.getClickCount() == 2) {
                if (isInMode(POLYGON)) {
                    createPureNewFeature(PureNewFeature.geomTypes.POLYGON);
                } else if (isInMode(LINESTRING)) {
                    createPureNewFeature(PureNewFeature.geomTypes.LINESTRING);
                }
                inProgress = false;
            }
        }
    }

    private void createPureNewFeature(PureNewFeature.geomTypes geomType) {
        try {
            Constructor<? extends PureNewFeature> c = geometryFeatureClass.getConstructor(Point2D[].class, WorldToScreenTransform.class);
            Point2D[] p = getFinalPoints(null);
            log.debug("point count:" + p.length + " (" + Arrays.deepToString(p) + ")");//NOI18N
            PureNewFeature pnf = c.newInstance(p, mc.getWtst());
            pnf.setGeometryType(geomType);
            finishGeometry(pnf);
        } catch (Throwable t) {
            log.error("Error during the creation of the geometry", t);//NOI18N
        }
    }

    @Override
    public void mouseReleased(PInputEvent arg0) {
        super.mouseReleased(arg0);
        if (inProgress) {
            if (isInMode(RECTANGLE)) {
                createPureNewFeature(PureNewFeature.geomTypes.RECTANGLE);
                inProgress = false;
            } else if (isInMode(ELLIPSE)) {
                createPureNewFeature(PureNewFeature.geomTypes.ELLIPSE);
                inProgress = false;
            }
        }
    }

    @Override
    public void keyPressed(edu.umd.cs.piccolo.event.PInputEvent event) {
        if (inProgress) {
            if (!event.isControlDown() && points.size() > 0) { // Strg nicht gedr\u00FCckt
                undoPoints.add(points.get(points.size() - 1));
                points.remove(points.size() - 1);
                // keine Punkte mehr vorhanden? Stoppe erstellen
                if (points.isEmpty()) {
                    startPoint = null;
                    mc.getTmpFeatureLayer().removeAllChildren();
                    inProgress = false;
                }
                log.debug("Backspace gedr\u00FCckt: letzter eingef\u00FCgter Punkt gel\u00F6scht.");//NOI18N
                updatePolygon(null);
            } else if (event.isControlDown()) { // Strg gedr\u00FCckt
                if (!undoPoints.isEmpty()) {
                    points.add(undoPoints.pop());
                    log.debug("Backspace + STRG gedr\u00FCckt: letzter gel\u00F6schter Punkt wiederhergestellt.");//NOI18N
                    updatePolygon(null);
                }
            }
        } else if (!inProgress && points.isEmpty() && event.isControlDown()) {
            log.debug("Versuche Polygon und Startpunkt wiederherzustellen");//NOI18N

            initTempFeature(true);

            //Ersten Punkt anlegen
            startPoint = undoPoints.pop();
            points.add(startPoint);
            inProgress = true;
        }
    }

    private void createAction(MappingComponent m, PureNewFeature f) {
        mc.getMemUndo().addAction(new FeatureDeleteAction(m, f));
        mc.getMemRedo().clear();
    }

    protected Color getFillingColor() {
        return new Color(1f, 1f, 1f, 0.4f);
    }

    protected void updatePolygon(Point2D lastPoint) {
        Point2D[] p = getPoints(lastPoint);
        Feature pnf = null;
        try {
            Constructor c = geometryFeatureClass.getConstructor(Point2D[].class, WorldToScreenTransform.class);
            pnf = (Feature) c.newInstance(p, mc.getWtst());
        } catch (Throwable t) {
            log.error("Error during the creation of a geometry", t);//NOI18N
        }
        //pnf=new PureNewFeature(p,mc.getWtst());
        ArrayList<Feature> v = new ArrayList<Feature>();
        v.add(pnf);
        log.debug("added:" + pnf);//NOI18N
        ((DefaultFeatureCollection) mc.getFeatureCollection()).fireFeaturesChanged(v);
        tempFeature.setPathToPolyline(p);
        tempFeature.repaint();
    }

    protected Point2D[] getFinalPoints(Point2D lastPoint) {
        return getPoints(true, lastPoint);
    }

    protected Point2D[] getPoints(Point2D lastPoint) {
        return getPoints(false, lastPoint);
    }

    protected Point2D[] getPoints(boolean isFinal, Point2D lastPoint) {
        int plus;
        boolean movin = false;
        try {
            if (lastPoint != null) {

                plus = 2;
                movin = true;
            } else {
                plus = 1;
                movin = false;
            }

            if (!isInMode(POLYGON) || (isInMode(POLYGON) && points.size() == 2 && !movin)) {
                plus--;
            }
            if (isFinal && isInMode(POLYGON) && points.size() == 2 && !movin) { //bei polygonen mit nur 2 punkten wird eine boundingbox angelegt
                Point2D[] p = new Point2D[5];
                p[0] = points.get(0);
                p[2] = points.get(1);
                p[1] = new Point2D.Double(p[0].getX(), p[2].getY());
                p[3] = new Point2D.Double(p[2].getX(), p[0].getY());
                p[4] = p[0];
                return p;
            }
            Point2D[] p = new Point2D[points.size() + plus];
            for (int i = 0; i < points.size(); ++i) {
                p[i] = points.get(i);
            }

            if (movin) {
                log.debug("movin");//NOI18N
                p[points.size()] = lastPoint;
                if (isInMode(POLYGON)) {
                    //close it
                    p[points.size() + 1] = startPoint;
                }
            } else {
                log.debug("not movin");//NOI18N
                if (points.size() > 2 && isInMode(POLYGON)) {
                    //close it
                    p[points.size()] = startPoint;
                }
            }
            return p;
        } catch (Exception e) {
            log.warn("Error in getPoints()", e);//NOI18N
            return new Point2D[0];
        }
    }

    public boolean isInMode(String mode) {
        return (this.mode.equals(mode));
    }

    public String getMode() {
        return mode;
    }

    public Class getGeometryFeatureClass() {
        return geometryFeatureClass;
    }

    public void setGeometryFeatureClass(Class<? extends PureNewFeature> geometryFeatureClass) {
        this.geometryFeatureClass = geometryFeatureClass;
    }

    private void postGeometryCreatedNotificaton(final PureNewFeature newFeature) {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(GEOMETRY_CREATED_NOTIFICATION, newFeature);
    }

    @Override
    public void allFeaturesRemoved(FeatureCollectionEvent fce) {
    }

    @Override
    public void mouseDragged(PInputEvent pInputEvent) {
        super.mouseDragged(pInputEvent);

        if (isInMode(RECTANGLE)) {
            inProgress = true;

            // 4 Punkte, und der erste Punkt nochmal als letzter Punkt
            points = new Vector<Point2D>(5);
            points.add(startPoint);
            points.add(new Point2D.Double(startPoint.getX(), pInputEvent.getPosition().getY()));
            points.add(pInputEvent.getPosition());
            points.add(new Point2D.Double(pInputEvent.getPosition().getX(), startPoint.getY()));
            points.add(startPoint);

            updatePolygon(null);
        } else if (isInMode(ELLIPSE)) {
            inProgress = true;
            Point2D dragPoint = pInputEvent.getPosition();
            log.debug("pInputEvent.getModifiers() = " + pInputEvent.getModifiers());//NOI18N

            double a = startPoint.getX() - dragPoint.getX();
            double b = startPoint.getY() - dragPoint.getY();
            double startX = startPoint.getX();
            double startY = startPoint.getY();

            Coordinate[] coordArr = EllipsePHandle.createEllipseCoordinates(
                    getNumOfEllipseEdges(),
                    a,
                    b,
                    pInputEvent.isControlDown(),
                    pInputEvent.isShiftDown());

            points = new Vector<Point2D>(coordArr.length);
            for (int i = 0; i < coordArr.length; i++) {
                points.add(new Point2D.Double(startX - coordArr[i].x, startY - coordArr[i].y));
            }

            updatePolygon(null);
        }
    }

    @Override
    public void featureCollectionChanged() {
    }

    @Override
    public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
    }

    @Override
    public void featureSelectionChanged(FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresAdded(FeatureCollectionEvent fce) {
        log.debug("Features added to map");//NOI18N
        for (Feature curFeature : fce.getEventFeatures()) {
            if (curFeature instanceof PureNewFeature) {
                log.debug("Added Feature is PureNewFeature. PostingGeometryCreateNotification");//NOI18N
                postGeometryCreatedNotificaton((PureNewFeature) curFeature);
                createAction(mc, (PureNewFeature) curFeature);
            }
        }
    }

    @Override
    public void featuresChanged(FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(FeatureCollectionEvent fce) {
    }

    protected void finishGeometry(final PureNewFeature newFeature) {
        mc.getTmpFeatureLayer().removeAllChildren();
    }

    public void setNumOfEllipseEdges(int numOfEllipseEdges) {
        if (numOfEllipseEdges <= 2) {
            numOfEllipseEdges = NUMOF_ELLIPSE_EDGES;
        }
        this.numOfEllipseEdges = numOfEllipseEdges;
    }

    public int getNumOfEllipseEdges() {
        return numOfEllipseEdges;
    }

    private void initTempFeature(boolean filled) {
        tempFeature = new PPath();
        tempFeature.setStroke(new FixedWidthStroke());
        if (filled) {
            Color fillingColor = getFillingColor();
            tempFeature.setStrokePaint(fillingColor.darker());
            tempFeature.setPaint(fillingColor);
        }
        mc.getTmpFeatureLayer().addChild(tempFeature);
    }
}
