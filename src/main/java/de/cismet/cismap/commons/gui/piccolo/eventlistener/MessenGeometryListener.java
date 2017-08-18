/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.lang.reflect.Constructor;

import java.util.Arrays;
import java.util.List;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.EllipsePHandle;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureDeleteAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.collections.TypeSafeCollections;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public class MessenGeometryListener extends PBasicInputEventHandler implements FeatureCollectionListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String LINESTRING = "LINESTRING";
    public static final String POINT = "POINT";
    public static final String POLYGON = "POLYGON";
    public static final String RECTANGLE = "BOUNDING_BOX";
    public static final String ELLIPSE = "ELLIPSE";
    private static final int NUMOF_ELLIPSE_EDGES = 36;
    public static final String GEOMETRY_CREATED_NOTIFICATION = "GEOMETRY_CREATED_NOTIFICATION";
    private static final Color PAINT_COLOR = new Color(255, 0, 255, 45);

    //~ Instance fields --------------------------------------------------------

    protected Point2D startPoint;
    protected PPath tempFeature;
    protected MappingComponent mc;
    protected boolean inProgress;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final List<Point2D> points = TypeSafeCollections.newArrayList();
    private final List<Point2D> undoPoints = TypeSafeCollections.newArrayList();
    private int numOfEllipseEdges;
    private String mode = POLYGON;
    private Class<? extends PureNewFeature> geometryFeatureClass = null;
    private final PBasicInputEventHandler zoomDelegate;
    private Feature latestCreation = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessenGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public MessenGeometryListener(final MappingComponent mc) {
        this(mc, PureNewFeature.class);
    }

    /**
     * Creates a new instance of CreateGeometryListener.
     *
     * @param  mc                    DOCUMENT ME!
     * @param  geometryFeatureClass  DOCUMENT ME!
     */
    protected MessenGeometryListener(final MappingComponent mc, final Class geometryFeatureClass) {
        setGeometryFeatureClass(geometryFeatureClass);
        zoomDelegate = new RubberBandZoomListener();
        this.mc = mc;
        undoPoints.clear();
        // srichter: fehlerpotential! this referenz eines nicht fertig initialisieren Objekts wieder nach aussen
        // geliefert! loesungsvorschlag: createInstance-methode, welche den aufruf nach dem erzeugen ausfuehrt.
        mc.getFeatureCollection().addFeatureCollectionListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   m  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void setMode(final String m) throws IllegalArgumentException {
        if (m.equals(LINESTRING) || m.equals(POINT) || m.equals(POLYGON) || m.equals(ELLIPSE) || m.equals(RECTANGLE)) {
            this.mode = m;
            mc.getTmpFeatureLayer().removeAllChildren();
            inProgress = false;
        } else {
            throw new IllegalArgumentException("Mode:" + m + " is not a valid Mode in this Listener.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mouseMoved(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (inProgress) { // && (!isInMode(POINT))) {
            Point2D point = null;
            if (mc.isSnappingEnabled()) {
                final boolean vertexRequired = mc.isSnappingOnLineEnabled();
                point = PFeatureTools.getNearestPointInArea(
                        mc,
                        pInputEvent.getCanvasPosition(),
                        vertexRequired,
                        true);
            }
            if (point == null) {
                point = pInputEvent.getPosition();
            }
            updatePolygon(point);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mousePressed(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (mc.isReadOnly()) {
            ((DefaultFeatureCollection)(mc.getFeatureCollection())).removeFeaturesByInstance(Feature.class);
        }
        if (isInMode(POINT)) {
            if (pInputEvent.isLeftMouseButton()) {
                Point2D point = null;
                if (mc.isSnappingEnabled()) {
                    final boolean vertexRequired = mc.isSnappingOnLineEnabled();
                    point = PFeatureTools.getNearestPointInArea(
                            mc,
                            pInputEvent.getCanvasPosition(),
                            vertexRequired,
                            true);
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                try {
                    final Constructor<? extends PureNewFeature> c = geometryFeatureClass.getConstructor(
                            Point2D.class,
                            WorldToScreenTransform.class);
                    final PureNewFeature pnf = c.newInstance(point, mc.getWtst());
                    applyCurrentStyle(pnf);
                    pnf.setGeometryType(PureNewFeature.geomTypes.POINT);
                    finishGeometry(pnf);
                } catch (Throwable t) {
                    log.error("Fehler beim Erzeugen der Geometrie: " + geometryFeatureClass, t);
                }
            }
        } else if (isInMode(RECTANGLE)) {
            if (!inProgress) {
                tempFeature = initTempFeature(true);
                mc.getTmpFeatureLayer().addChild(tempFeature);

                startPoint = pInputEvent.getPosition();
            }
        } else if (isInMode(ELLIPSE)) {
            if (!inProgress) {
                tempFeature = initTempFeature(true);
                mc.getTmpFeatureLayer().addChild(tempFeature);

                startPoint = pInputEvent.getPosition();
            }
        } else if (isInMode(POLYGON) || isInMode(LINESTRING)) {
            if (pInputEvent.getClickCount() == 1) {
                Point2D point = null;
                undoPoints.clear();
                if (mc.isSnappingEnabled()) {
                    final boolean vertexRequired = mc.isSnappingOnLineEnabled();
                    point = PFeatureTools.getNearestPointInArea(
                            mc,
                            pInputEvent.getCanvasPosition(),
                            vertexRequired,
                            true);
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                if (!inProgress) {
                    if (isInMode(POLYGON)) {
                        tempFeature = initTempFeature(true);
                    } else {
                        tempFeature = initTempFeature(false);
                    }
                    mc.getTmpFeatureLayer().addChild(tempFeature);

                    // Polygon erzeugen
                    points.clear();
                    // Ersten Punkt anlegen
                    startPoint = point;
                    points.add(startPoint);
                    if (latestCreation != null) {
                        mc.getFeatureCollection().removeFeature(latestCreation);
                    }
                    inProgress = true;
                } else {
                    // Zus\u00E4tzlichen Punkt anlegen
                    points.add(point);
                    updatePolygon(null);
                }
            } else if (pInputEvent.getClickCount() == 2) {
                // Anlegen des neuen PFeatures
                try {
                    final Constructor<? extends PureNewFeature> c = geometryFeatureClass.getConstructor(
                            Point2D[].class,
                            WorldToScreenTransform.class);
                    final Point2D[] p = getFinalPoints(null);
                    if (log.isDebugEnabled()) {
                        log.debug("Anzahl Punkte:" + p.length + " (" + Arrays.deepToString(p) + ")");
                    }

                    final PureNewFeature pnf = c.newInstance(p, mc.getWtst());
                    applyCurrentStyle(pnf);
                    if (isInMode(POLYGON)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POLYGON);
                    } else if (isInMode(LINESTRING)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.LINESTRING);
                    }
                    finishGeometry(pnf);
                } catch (Throwable t) {
                    log.error("Fehler beim Erzeugen der Geometrie: " + geometryFeatureClass, t);
                }
                inProgress = false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  arg0  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final PInputEvent arg0) {
        super.mouseReleased(arg0);
        if (isInMode(RECTANGLE) || isInMode(ELLIPSE)) {
            if (inProgress) {
                try {
                    final Constructor<? extends PureNewFeature> c = geometryFeatureClass.getConstructor(
                            Point2D[].class,
                            WorldToScreenTransform.class);
                    final Point2D[] p = getFinalPoints(null);
                    if (log.isDebugEnabled()) {
                        log.debug("Anzahl Punkte:" + p.length + " (" + Arrays.deepToString(p) + ")");
                    }
                    final PureNewFeature pnf = c.newInstance(p, mc.getWtst());
                    applyCurrentStyle(pnf);
                    if (isInMode(RECTANGLE)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.RECTANGLE);
                    } else {
                        pnf.setGeometryType(PureNewFeature.geomTypes.ELLIPSE);
                    }
                    finishGeometry(pnf);
                } catch (Throwable ex) {
                    log.error("", ex);
                }
                inProgress = false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void keyPressed(final edu.umd.cs.piccolo.event.PInputEvent event) {
        if (inProgress) {
            if (!event.isControlDown() && (points.size() > 0)) { // Strg nicht gedr\u00FCckt
                undoPoints.add(points.get(points.size() - 1));
                points.remove(points.size() - 1);
                // keine Punkte mehr vorhanden? Stoppe erstellen
                if (points.size() == 0) {
                    startPoint = null;
                    mc.getTmpFeatureLayer().removeAllChildren();
                    inProgress = false;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Backspace gedr\u00FCckt: letzter eingef\u00FCgter Punkt gel\u00F6scht.");
                }
                updatePolygon(null);
            } else if (event.isControlDown()) { // Strg gedr\u00FCckt
                if (!undoPoints.isEmpty()) {
                    points.add(undoPoints.remove(undoPoints.size() - 1));
                    if (log.isDebugEnabled()) {
                        log.debug("Backspace + STRG gedr\u00FCckt: letzter gel\u00F6schter Punkt wiederhergestellt.");
                    }
                    updatePolygon(null);
                }
            }
        } else if (!inProgress && points.isEmpty() && event.isControlDown()) {
            if (log.isDebugEnabled()) {
                log.debug("Versuche Polygon und Startpunkt wiederherzustellen");
            }

            tempFeature = initTempFeature(true);
            mc.getTmpFeatureLayer().addChild(tempFeature);

            // Ersten Punkt anlegen
            startPoint = undoPoints.remove(undoPoints.size() - 1);
            points.add(startPoint);
            inProgress = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  m  DOCUMENT ME!
     * @param  f  DOCUMENT ME!
     */
    private void createAction(final MappingComponent m, final PureNewFeature f) {
        mc.getMemUndo().addAction(new FeatureDeleteAction(m, f));
        mc.getMemRedo().clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Color getFillingColor() {
        return PAINT_COLOR;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  toProcess  DOCUMENT ME!
     */
    private void applyCurrentStyle(final Feature toProcess) {
        if (toProcess instanceof StyledFeature) {
            final StyledFeature sf = (StyledFeature)toProcess;
            sf.setFillingPaint(PAINT_COLOR);
            sf.setLinePaint(PAINT_COLOR.darker());
            sf.setTransparency((PAINT_COLOR.getTransparency() / 255.0f));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastPoint  DOCUMENT ME!
     */
    protected void updatePolygon(final Point2D lastPoint) {
        final Point2D[] p = getPoints(lastPoint);
        Feature pnf = null;
        try {
            final Constructor c = geometryFeatureClass.getConstructor(Point2D[].class, WorldToScreenTransform.class);
            pnf = (Feature)c.newInstance(p, mc.getWtst());
            applyCurrentStyle(pnf);
        } catch (Throwable t) {
            log.error("Fehler beim Erzeugen der Geometrie", t);
        }
        // pnf=new PureNewFeature(p,mc.getWtst());
        final List<Feature> v = TypeSafeCollections.newArrayList(1);
        v.add(pnf);
        if (log.isDebugEnabled()) {
            log.debug("hinzugefügt:" + pnf);
        }
        ((DefaultFeatureCollection)mc.getFeatureCollection()).fireFeaturesChanged(v);
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
    protected Point2D[] getFinalPoints(final Point2D lastPoint) {
        return getPoints(true, lastPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lastPoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Point2D[] getPoints(final Point2D lastPoint) {
        return getPoints(false, lastPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFinal    DOCUMENT ME!
     * @param   lastPoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Point2D[] getPoints(final boolean isFinal, final Point2D lastPoint) {
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

            if (!isInMode(POLYGON) || (isInMode(POLYGON) && (points.size() == 2) && !movin)) {
                plus--;
            }
            if (isFinal && isInMode(POLYGON) && (points.size() == 2) && !movin) { // bei polygonen mit nur 2 punkten
                                                                                  // wird eine boundingbox angelegt
                final Point2D[] p = new Point2D[5];
                p[0] = points.get(0);
                p[2] = points.get(1);
                p[1] = new Point2D.Double(p[0].getX(), p[2].getY());
                p[3] = new Point2D.Double(p[2].getX(), p[0].getY());
                p[4] = p[0];
                return p;
            }
            final Point2D[] p = new Point2D[points.size() + plus];
            for (int i = 0; i < points.size(); ++i) {
                p[i] = points.get(i);
            }

            if (movin) {
                if (log.isDebugEnabled()) {
                    log.debug("movin");
                }
                p[points.size()] = lastPoint;
                if (isInMode(POLYGON)) {
                    // close it
                    p[points.size() + 1] = startPoint;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("not movin");
                }
                if ((points.size() > 2) && isInMode(POLYGON)) {
                    // close it
                    p[points.size()] = startPoint;
                }
            }
            return p;
        } catch (Exception e) {
            log.warn("Fehler in getPoints()", e);
            return new Point2D[0];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isInMode(final String mode) {
        return (this.mode.equals(mode));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMode() {
        return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Class getGeometryFeatureClass() {
        return geometryFeatureClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geometryFeatureClass  DOCUMENT ME!
     */
    public void setGeometryFeatureClass(final Class<? extends PureNewFeature> geometryFeatureClass) {
        this.geometryFeatureClass = geometryFeatureClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    private void postGeometryCreatedNotificaton(final PureNewFeature newFeature) {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(GEOMETRY_CREATED_NOTIFICATION, newFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mouseDragged(final PInputEvent pInputEvent) {
        super.mouseDragged(pInputEvent);

        if (isInMode(RECTANGLE)) {
            inProgress = true;

            // 4 Punkte, und der erste Punkt nochmal als letzter Punkt
            points.clear();
            points.add(startPoint);
            points.add(new Point2D.Double(startPoint.getX(), pInputEvent.getPosition().getY()));
            points.add(pInputEvent.getPosition());
            points.add(new Point2D.Double(pInputEvent.getPosition().getX(), startPoint.getY()));
            points.add(startPoint);

            updatePolygon(null);
        } else if (isInMode(ELLIPSE)) {
            inProgress = true;
            final Point2D dragPoint = pInputEvent.getPosition();
            if (log.isDebugEnabled()) {
                log.debug("pInputEvent.getModifiers() = " + pInputEvent.getModifiers());
            }

            final double a = startPoint.getX() - dragPoint.getX();
            final double b = startPoint.getY() - dragPoint.getY();
            final double startX = startPoint.getX();
            final double startY = startPoint.getY();

            final Coordinate[] coordArr = EllipsePHandle.createEllipseCoordinates(
                    getNumOfEllipseEdges(),
                    a,
                    b,
                    pInputEvent.isControlDown(),
                    pInputEvent.isShiftDown());

            points.clear();
            for (int i = 0; i < coordArr.length; i++) {
                points.add(new Point2D.Double(startX - coordArr[i].x, startY - coordArr[i].y));
            }

            updatePolygon(null);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void featureCollectionChanged() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        if (log.isDebugEnabled()) {
            log.debug("Features added to map");
        }
        for (final Feature curFeature : fce.getEventFeatures()) {
            if (curFeature instanceof PureNewFeature) {
                if (log.isDebugEnabled()) {
                    log.debug("Added Feature is PureNewFeature. PostingGeometryCreateNotification");
                }
                postGeometryCreatedNotificaton((PureNewFeature)curFeature);
                createAction(mc, (PureNewFeature)curFeature);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    protected void finishGeometry(final PureNewFeature newFeature) {
        latestCreation = newFeature;
        mc.getTmpFeatureLayer().removeAllChildren();
        newFeature.setEditable(true);
        mc.getFeatureCollection().addFeature(newFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  numOfEllipseEdges  DOCUMENT ME!
     */
    public void setNumOfEllipseEdges(int numOfEllipseEdges) {
        if (numOfEllipseEdges <= 2) {
            numOfEllipseEdges = NUMOF_ELLIPSE_EDGES;
        }
        this.numOfEllipseEdges = numOfEllipseEdges;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getNumOfEllipseEdges() {
        return numOfEllipseEdges;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   filled  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PPath initTempFeature(final boolean filled) {
        tempFeature = new PPath();
        tempFeature.setStroke(new FixedWidthStroke());
        final Color fillingColor = getFillingColor();
        tempFeature.setStrokePaint(fillingColor.darker());
        if (filled) {
            tempFeature.setPaint(fillingColor);
        } else {
            tempFeature.setPaint(null);
        }
        return tempFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    @Override
    public void mouseWheelRotated(final PInputEvent evt) {
        // delegate zoom event
        zoomDelegate.mouseWheelRotated(evt);
        // trigger full repaint
        mouseMoved(evt);
    }
}
