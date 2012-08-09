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

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Frame;
import java.awt.geom.Point2D;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.*;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.EllipsePHandle;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.RectangleFromLineDialog;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureDeleteAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CreateGeometryListener extends PBasicInputEventHandler implements FeatureCollectionListener,
    CreateGeometryListenerInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateGeometryListener.class);
    public static final String GEOMETRY_CREATED_NOTIFICATION = "GEOMETRY_CREATED_NOTIFICATION"; // NOI18N

    protected static final int DEFAULT_NUMOF_ELLIPSE_EDGES = 36;

    //~ Instance fields --------------------------------------------------------

    private Point2D startPoint;
    private PPath tempFeature;
    private MappingComponent mc;
    private boolean inProgress;
    private int numOfEllipseEdges = DEFAULT_NUMOF_ELLIPSE_EDGES;

    private ArrayList<Point2D> points;
    private Stack<Point2D> undoPoints;
    private SimpleMoveListener moveListener;
    private String mode = POLYGON;
    private Class<? extends PureNewFeature> geometryFeatureClass = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public CreateGeometryListener(final MappingComponent mc) {
        this(mc, Feature.class);
    }

    /**
     * Creates a new instance of CreateGeometryListener.
     *
     * @param  mc                    DOCUMENT ME!
     * @param  geometryFeatureClass  DOCUMENT ME!
     */
    protected CreateGeometryListener(final MappingComponent mc, final Class geometryFeatureClass) {
        setGeometryFeatureClass(geometryFeatureClass);
        this.mc = mc;
        moveListener = (SimpleMoveListener)mc.getInputListener(MappingComponent.MOTION);
        undoPoints = new Stack<Point2D>();
        // srichter: fehlerpotential! this referenz eines nicht fertig initialisieren Objekts wieder nach aussen
        // geliefert! loesungsvorschlag: createInstance-methode, welche den aufruf nach dem erzeugen ausfuehrt.
        mc.getFeatureCollection().addFeatureCollectionListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected MappingComponent getMappingComponent() {
        return mc;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean isInProgress() {
        return inProgress;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @Override
    public void setMode(final String mode) throws IllegalArgumentException {
        if (modeEquals(LINESTRING) || modeEquals(POINT) || modeEquals(POLYGON) || modeEquals(ELLIPSE)
                    || modeEquals(RECTANGLE)
                    || modeEquals(RECTANGLE_FROM_LINE)) {
            if (!modeEquals(mode)) {
                reset();
                this.mode = mode;
            }
        } else {
            throw new IllegalArgumentException("Mode:" + mode + " is not a valid Mode in this Listener."); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean modeEquals(final String mode) {
        return ((mode == null) ? (this.mode == null) : mode.equals(this.mode));
    }

    /**
     * DOCUMENT ME!
     */
    protected void reset() {
        mc.getTmpFeatureLayer().removeAllChildren();
        inProgress = false;
    }

    @Override
    public void mouseMoved(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            LOG.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden."); // NOI18N
        }
        if (inProgress) {                                                           // && (!isInMode(POINT))) {
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
    public void mousePressed(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (mc.isReadOnly()) {
            ((DefaultFeatureCollection)(mc.getFeatureCollection())).removeFeaturesByInstance(PureNewFeature.class);
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
                points = new ArrayList<Point2D>();
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
            // snappingpoint ermitteln falls snapping enabled
            final Point2D snappingPoint = (mc.isSnappingEnabled())
                ? PFeatureTools.getNearestPointInArea(mc, pInputEvent.getCanvasPosition()) : null;
            // wenn snappingpoint vorhanden, dann den nehmen, ansonsten normalen punkt unter der maus ermitteln
            final Point2D point = (snappingPoint != null) ? snappingPoint : pInputEvent.getPosition();

            if (pInputEvent.isLeftMouseButton()) {
                if (!inProgress) {
                    inProgress = true;
                    initTempFeature(true);
                    startPoint = point;
                    points = new ArrayList<Point2D>(5);
                    points.add(startPoint);
                } else {
                    final Point2D stopPoint = point;

                    // erst einmal nur "flaches" Rechteck erzeugen (eine Linie, aber mit 4 Punkten)
                    points.add(stopPoint);
                    points.add(stopPoint);
                    points.add(startPoint);
                    points.add(startPoint);

                    // Länge ermitteln
                    final double length = startPoint.distance(stopPoint);

                    // Dialog erzeugen
                    final Frame parentFrame = StaticSwingTools.getParentFrame(mc);
                    final RectangleFromLineDialog dialog = new RectangleFromLineDialog(parentFrame, true, length);

                    // in der Karte dynamisch auf Eingaben im Dialog reagieren
                    dialog.addWidthChangedListener(new ChangeListener() {

                            @Override
                            public void stateChanged(final ChangeEvent ce) {
                                final double height = dialog.getRectangleWidth();
                                final boolean isLefty = dialog.isLefty();

                                final Point2D startPoint = points.get(0);
                                final Point2D stopPoint = points.get(1);

                                final double deltaX = stopPoint.getX() - startPoint.getX();
                                final double deltaY = stopPoint.getY() - startPoint.getY();

                                final double alpha = Math.atan2(deltaY, deltaX);
                                final double alpha90 = alpha + Math.toRadians((isLefty) ? -90 : 90);

                                final double x = Math.cos(alpha90) * height;
                                final double y = Math.sin(alpha90) * height;

                                points.set(2, new Point2D.Double(x + stopPoint.getX(), y + stopPoint.getY()));
                                points.set(3, new Point2D.Double(x + startPoint.getX(), y + startPoint.getY()));

                                updatePolygon(null);
                            }
                        });

                    // Dialog mittig anzeigen
                    StaticSwingTools.showDialog(dialog);

                    // Ergebnis des Dialogs auswerten
                    if (dialog.getReturnStatus() == RectangleFromLineDialog.STATUS_OK) {
                        // fertig
                        createPureNewFeature(PureNewFeature.geomTypes.POLYGON);
                        inProgress = false;
                    } else {
                        // abbrechen
                        mc.getTmpFeatureLayer().removeChild(tempFeature);
                        inProgress = false;
                    }
                }
            } else if (pInputEvent.isRightMouseButton()) {
                // abbrechen
                if (tempFeature != null) {
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

                    // Polygon erzeugen
                    points = new ArrayList<Point2D>();
                    // Ersten Punkt anlegen
                    startPoint = point;
                    points.add(startPoint);
                } else {
                    // Zus\u00E4tzlichen Punkt anlegen
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

    /**
     * DOCUMENT ME!
     *
     * @param  geomType  DOCUMENT ME!
     */
    private void createPureNewFeature(final PureNewFeature.geomTypes geomType) {
        try {
            final Constructor<? extends PureNewFeature> c = geometryFeatureClass.getConstructor(
                    Point2D[].class,
                    WorldToScreenTransform.class);
            final Point2D[] p = getFinalPoints(null);
            if (LOG.isDebugEnabled()) {
                LOG.debug("point count:" + p.length + " (" + Arrays.deepToString(p) + ")"); // NOI18N
            }
            final PureNewFeature pnf = c.newInstance(p, mc.getWtst());
            pnf.setGeometryType(geomType);
            finishGeometry(pnf);
        } catch (Throwable t) {
            LOG.error("Error during the creation of the geometry", t);                      // NOI18N
        }
    }

    @Override
    public void mouseReleased(final PInputEvent arg0) {
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
    public void keyPressed(final edu.umd.cs.piccolo.event.PInputEvent event) {
        if (inProgress) {
            if (!event.isControlDown() && (points.size() > 0)) { // Strg nicht gedr\u00FCckt
                undoPoints.add(points.get(points.size() - 1));
                points.remove(points.size() - 1);
                // keine Punkte mehr vorhanden? Stoppe erstellen
                if (points.isEmpty()) {
                    startPoint = null;
                    mc.getTmpFeatureLayer().removeAllChildren();
                    inProgress = false;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Backspace gedr\u00FCckt: letzter eingef\u00FCgter Punkt gel\u00F6scht.");               // NOI18N
                }
                updatePolygon(null);
            } else if (event.isControlDown()) {                                                                        // Strg gedr\u00FCckt
                if (!undoPoints.isEmpty()) {
                    points.add(undoPoints.pop());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Backspace + STRG gedr\u00FCckt: letzter gel\u00F6schter Punkt wiederhergestellt."); // NOI18N
                    }
                    updatePolygon(null);
                }
            }
        } else if (!inProgress && points.isEmpty() && event.isControlDown()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Versuche Polygon und Startpunkt wiederherzustellen");                                       // NOI18N
            }

            initTempFeature(true);

            // Ersten Punkt anlegen
            startPoint = undoPoints.pop();
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
        return new Color(1f, 1f, 1f, 0.4f);
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
        } catch (Throwable t) {
            LOG.error("Error during the creation of a geometry", t); // NOI18N
        }
        // pnf=new PureNewFeature(p,mc.getWtst());
        final ArrayList<Feature> v = new ArrayList<Feature>();
        v.add(pnf);
        if (LOG.isDebugEnabled()) {
            LOG.debug("added:" + pnf); // NOI18N
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("movin"); // NOI18N
                }
                p[points.size()] = lastPoint;
                if (isInMode(POLYGON)) {
                    // close it
                    p[points.size() + 1] = startPoint;
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("not movin"); // NOI18N
                }
                if ((points.size() > 2) && isInMode(POLYGON)) {
                    // close it
                    p[points.size()] = startPoint;
                }
            }
            return p;
        } catch (Exception e) {
            LOG.warn("Error in getPoints()", e); // NOI18N
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
    @Override
    public boolean isInMode(final String mode) {
        return (this.mode.equals(mode));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getMode() {
        return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Class getGeometryFeatureClass() {
        return geometryFeatureClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geometryFeatureClass  DOCUMENT ME!
     */
    @Override
    public final void setGeometryFeatureClass(final Class<? extends PureNewFeature> geometryFeatureClass) {
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

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void mouseDragged(final PInputEvent pInputEvent) {
        super.mouseDragged(pInputEvent);

        if (startPoint == null) {
            startPoint = pInputEvent.getPosition();
        }

        if (isInMode(RECTANGLE)) {
            inProgress = true;

            // 4 Punkte, und der erste Punkt nochmal als letzter Punkt
            points = new ArrayList<Point2D>(5);
            points.add(startPoint);
            points.add(new Point2D.Double(startPoint.getX(), pInputEvent.getPosition().getY()));
            points.add(pInputEvent.getPosition());
            points.add(new Point2D.Double(pInputEvent.getPosition().getX(), startPoint.getY()));
            points.add(startPoint);

            updatePolygon(null);
        } else if (isInMode(ELLIPSE)) {
            inProgress = true;
            final Point2D dragPoint = pInputEvent.getPosition();
            if (LOG.isDebugEnabled()) {
                LOG.debug("pInputEvent.getModifiers() = " + pInputEvent.getModifiers()); // NOI18N
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

            points = new ArrayList<Point2D>(coordArr.length);
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
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Features added to map");                                                      // NOI18N
        }
        for (final Feature curFeature : fce.getEventFeatures()) {
            if (curFeature instanceof PureNewFeature) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Added Feature is PureNewFeature. PostingGeometryCreateNotification"); // NOI18N
                }
                postGeometryCreatedNotificaton((PureNewFeature)curFeature);
                createAction(mc, (PureNewFeature)curFeature);
            }
        }
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    protected void finishGeometry(final PureNewFeature newFeature) {
        mc.getTmpFeatureLayer().removeAllChildren();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  numOfEllipseEdges  DOCUMENT ME!
     */
    @Override
    public void setNumOfEllipseEdges(int numOfEllipseEdges) {
        if (numOfEllipseEdges <= 2) {
            numOfEllipseEdges = DEFAULT_NUMOF_ELLIPSE_EDGES;
        }
        this.numOfEllipseEdges = numOfEllipseEdges;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getNumOfEllipseEdges() {
        return numOfEllipseEdges;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  filled  DOCUMENT ME!
     */
    private void initTempFeature(final boolean filled) {
        tempFeature = new PPath();
        tempFeature.setStroke(new FixedWidthStroke());
        if (filled) {
            final Color fillingColor = getFillingColor();
            tempFeature.setStrokePaint(fillingColor.darker());
            tempFeature.setPaint(fillingColor);
        }
        mc.getTmpFeatureLayer().addChild(tempFeature);
    }
}
