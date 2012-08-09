/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
import java.util.Stack;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.*;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.EllipsePHandle;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.RectangleFromLineDialog;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureDeleteAction;
import de.cismet.cismap.commons.interaction.CismapBroker;
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
    private MappingComponent mappingComponent;
    private boolean inProgress;
    private int numOfEllipseEdges = DEFAULT_NUMOF_ELLIPSE_EDGES;

    private ArrayList<Point2D> points;
    private Stack<Point2D> undoPoints;
    private SimpleMoveListener moveListener;
    private String mode = POLYGON;
    private Class<? extends PureNewFeature> geometryFeatureClass = null;
    private PureNewFeature currentFeature = null;

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
        this.mappingComponent = mc;
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
        return mappingComponent;
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
    public void mouseMoved(final PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            LOG.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden."); // NOI18N
        }
        if (inProgress) {                                                           // && (!isInMode(POINT))) {
            Point2D point = null;
            if (mappingComponent.isSnappingEnabled()) {
                point = PFeatureTools.getNearestPointInArea(mappingComponent, pInputEvent.getCanvasPosition());
            }
            if (point == null) {
                point = pInputEvent.getPosition();
            }
            updatePolygon(point);
        }
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (mappingComponent.isReadOnly()) {
            ((DefaultFeatureCollection)(mappingComponent.getFeatureCollection())).removeFeaturesByInstance(
                PureNewFeature.class);
        }
        if (isInMode(POINT)) {
            if (pInputEvent.isLeftMouseButton()) {
                Point2D point = null;
                if (mappingComponent.isSnappingEnabled()) {
                    point = PFeatureTools.getNearestPointInArea(mappingComponent, pInputEvent.getCanvasPosition());
                }
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                points = new ArrayList<Point2D>();
                points.add(point);
                readyForFinishing();
            }
        } else if (isInMode(RECTANGLE)) {
            if (!inProgress) {
                initTempFeature();
                startPoint = pInputEvent.getPosition();
            }
        } else if (isInMode(ELLIPSE)) {
            if (!inProgress) {
                initTempFeature();
                startPoint = pInputEvent.getPosition();
            }
        } else if (isInMode(RECTANGLE_FROM_LINE)) {
            // snappingpoint ermitteln falls snapping enabled
            final Point2D snappingPoint = (mappingComponent.isSnappingEnabled())
                ? PFeatureTools.getNearestPointInArea(mappingComponent, pInputEvent.getCanvasPosition()) : null;
            // wenn snappingpoint vorhanden, dann den nehmen, ansonsten normalen punkt unter der maus ermitteln
            final Point2D point = (snappingPoint != null) ? snappingPoint : pInputEvent.getPosition();

            if (pInputEvent.isLeftMouseButton()) {
                if (!inProgress) {
                    inProgress = true;
                    initTempFeature();
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
                    final Frame parentFrame = StaticSwingTools.getParentFrame(mappingComponent);
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
                        readyForFinishing();
                        inProgress = false;
                    } else {
                        // abbrechen
                        mappingComponent.getTmpFeatureLayer().removeChild(tempFeature);
                        inProgress = false;
                    }
                }
            } else if (pInputEvent.isRightMouseButton()) {
                // abbrechen
                if (tempFeature != null) {
                    mappingComponent.getTmpFeatureLayer().removeChild(tempFeature);
                }
                inProgress = false;
            }
        } else if (isInMode(POLYGON) || isInMode(LINESTRING)) {
            if (pInputEvent.getClickCount() == 1) {
                if (pInputEvent.isLeftMouseButton()) {
                    Point2D point = null;
                    undoPoints.clear();
                    if (mappingComponent.isSnappingEnabled()) {
                        point = PFeatureTools.getNearestPointInArea(mappingComponent, pInputEvent.getCanvasPosition());
                    }
                    if (point == null) {
                        point = pInputEvent.getPosition();
                    }

                    if (!inProgress) {
                        inProgress = true;
                        initTempFeature();

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
                } else if (pInputEvent.isRightMouseButton()) {
                    // abbrechen
                    if (tempFeature != null) {
                        mappingComponent.getTmpFeatureLayer().removeChild(tempFeature);
                    }
                    inProgress = false;
                }
            } else if (pInputEvent.getClickCount() == 2) {
                if (isInMode(POLYGON)) {
                    if (points.size() == 2) { // bei polygonen mit nur 2 punkten
                                              // wird eine boundingbox angelegt
                        final Point2D pointA = points.get(0);
                        final Point2D pointB = points.get(1);
                        points.remove(pointB);
                        points.add(new Point2D.Double(pointA.getX(), pointB.getY()));
                        points.add(pointB);
                        points.add(new Point2D.Double(pointB.getX(), pointA.getY()));
                        points.add(pointA);
                    } else if (points.size() < 2) {
                        return;               // ignorieren, da weniger als 2 Punkte nicht ausreichen
                    }
                }
                readyForFinishing();
                inProgress = false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void readyForFinishing() {
        try {
            createCurrentPureNewFeature(null);
            finishGeometry(currentFeature);
        } catch (Throwable t) {
            LOG.error("Error during the creation of the geometry", t); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
// protected PureNewFeature getCurrentPureNewFeature() {
// return getCurrentPureNewFeature(null);
// }

    protected PureNewFeature getCurrentPureNewFeature() {
        return currentFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastPoint  DOCUMENT ME!
     */
    protected void createCurrentPureNewFeature(final Point2D lastPoint) {
        try {
            final Point2D[] finalPoints = getPoints(lastPoint);
            PureNewFeature.geomTypes geomType = PureNewFeature.geomTypes.UNKNOWN;
            if (isInMode(ELLIPSE)) {
                geomType = PureNewFeature.geomTypes.ELLIPSE;
            } else if (isInMode(LINESTRING)) {
                geomType = PureNewFeature.geomTypes.LINESTRING;
            } else if (isInMode(POINT)) {
                geomType = PureNewFeature.geomTypes.POINT;
            } else if (isInMode(POLYGON)) {
                geomType = PureNewFeature.geomTypes.POLYGON;
            } else if (isInMode(RECTANGLE)) {
                geomType = PureNewFeature.geomTypes.POLYGON;
            } else if (isInMode(RECTANGLE_FROM_LINE)) {
                geomType = PureNewFeature.geomTypes.POLYGON;
            }

            final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
            final Constructor<? extends PureNewFeature> constructor = geometryFeatureClass.getConstructor(
                    Point2D[].class,
                    WorldToScreenTransform.class);
            final PureNewFeature pureNewFeature = constructor.newInstance(finalPoints, mappingComponent.getWtst());
            pureNewFeature.setGeometryType(geomType);
            pureNewFeature.getGeometry().setSRID(currentSrid);
            final Geometry geom = CrsTransformer.transformToGivenCrs(pureNewFeature.getGeometry(),
                    mappingComponent.getMappingModel().getSrs().getCode());
            pureNewFeature.setGeometry(geom);

            currentFeature = pureNewFeature;
        } catch (Throwable throwable) {
            LOG.error("Error during the creation of the geometry", throwable); // NOI18N
            currentFeature = null;
        }
    }

    @Override
    public void mouseReleased(final PInputEvent arg0) {
        super.mouseReleased(arg0);
        if (inProgress) {
            if (isInMode(RECTANGLE) || isInMode(ELLIPSE)) {
                readyForFinishing();
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
                    mappingComponent.getTmpFeatureLayer().removeAllChildren();
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

            initTempFeature();

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
        mappingComponent.getMemUndo().addAction(new FeatureDeleteAction(m, f));
        mappingComponent.getMemRedo().clear();
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
        createCurrentPureNewFeature(lastPoint);
        final ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(currentFeature);
        ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).fireFeaturesChanged(features);

        tempFeature.setPathToPolyline(getPoints(lastPoint));
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
        try {
            final boolean movin;
            int plus;
            if (lastPoint != null) {
                plus = 2;
                movin = true;
            } else {
                plus = 1;
                movin = false;
            }

            if (!isInMode(POLYGON)) {
                plus--;
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
                if (isInMode(POLYGON)) {
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
                createAction(mappingComponent, (PureNewFeature)curFeature);
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
        mappingComponent.getTmpFeatureLayer().removeAllChildren();
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
     * @return  DOCUMENT ME!
     */
    protected PPath createNewTempFeature() {
        final PPath newTempFeaturePath = new PPath();
        newTempFeaturePath.setStroke(new FixedWidthStroke());
        if (!isInMode(LINESTRING)) {
            final Color fillingColor = getFillingColor();
            newTempFeaturePath.setStrokePaint(fillingColor.darker());
            newTempFeaturePath.setPaint(fillingColor);
        }
        return newTempFeaturePath;
    }

    /**
     * DOCUMENT ME!
     */
    private void initTempFeature() {
        tempFeature = createNewTempFeature();
        mappingComponent.getTmpFeatureLayer().addChild(tempFeature);
    }
}
