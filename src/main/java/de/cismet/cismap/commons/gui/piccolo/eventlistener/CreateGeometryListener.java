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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.Point2D;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import de.cismet.cismap.commons.tools.NewTextDialog;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.StaticDecimalTools;

import de.cismet.tools.gui.StaticSwingTools;

import static de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface.POLYGON;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CreateGeometryListener extends PBasicInputEventHandler implements CreateGeometryListenerInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateGeometryListener.class);
    public static final String GEOMETRY_CREATED_NOTIFICATION = "GEOMETRY_CREATED_NOTIFICATION"; // NOI18N

    protected static final int DEFAULT_NUMOF_ELLIPSE_EDGES = 36;

    //~ Instance fields --------------------------------------------------------

    protected MappingComponent mappingComponent;
    protected PInputEvent finishingEvent = null;
    protected String mode = POLYGON;

    protected ArrayList<Point2D> points;
    protected Map<Point2D, Coordinate> snappedCoordinates = new HashMap<Point2D, Coordinate>();

    private boolean showCurrentLength = false;
    private PText currentLength;
    private Point2D startPoint;
    private PPath tempFeature;
    private boolean inProgress;
    private int numOfEllipseEdges = DEFAULT_NUMOF_ELLIPSE_EDGES;
    private Stack<Point2D> undoPoints;
    private Class<? extends AbstractNewFeature> geometryFeatureClass = null;
    private AbstractNewFeature currentFeature = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateGeometryListener object.
     */
    public CreateGeometryListener() {
        this(null, Feature.class);
    }

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
        undoPoints = new Stack<Point2D>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the showCurrentLength
     */
    public boolean isShowCurrentLength() {
        return showCurrentLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  showCurrentLength  the showCurrentLength to set
     */
    public void setShowCurrentLength(final boolean showCurrentLength) {
        this.showCurrentLength = showCurrentLength;
    }

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
                    || modeEquals(RECTANGLE_FROM_LINE) || modeEquals(TEXT)) {
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
        if (mappingComponent != null) {
            mappingComponent.getTmpFeatureLayer().removeAllChildren();
        }
        currentFeature = null;
        inProgress = false;
    }

    @Override
    public void mouseMoved(final PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);

        if (inProgress) { // && (!isInMode(POINT))) {
            final Point2D point = PFeatureTools.getNearestPointInArea(
                        mappingComponent,
                        pInputEvent.getCanvasPosition(),
                        true,
                        null)
                        .getPoint();
            updatePolygon(point);

            if (showCurrentLength && isInMode(LINESTRING)) {
                if (currentLength == null) {
                    currentLength = new PText();
                    currentLength.setVisible(true);
                    if (currentLength != null) {
                        mappingComponent.getTmpFeatureLayer().addChild(currentLength);
                    }
                }
                final Point2D leftInfoPoint = pInputEvent.getPosition();
                int fontSize = (int)(mappingComponent.getScaleDenominator() / 3700 * 12);
                if (fontSize < 1) {
                    fontSize = 1;
                }
                final Font f = new Font("sansserif", Font.PLAIN, fontSize);
                currentLength.setFont(f);
                currentLength.setTextPaint(new Color(100, 100, 0));
                currentLength.setPaint(new Color(255, 255, 255));
                currentLength.setX(leftInfoPoint.getX() + (12 / mappingComponent.getCamera().getViewScale()));
                currentLength.setY(leftInfoPoint.getY() - (12 / mappingComponent.getCamera().getViewScale()));

                if ((currentFeature != null) && (currentFeature.getGeometry() != null)) {
                    currentLength.setText(StaticDecimalTools.round(currentFeature.getGeometry().getLength()));
                }
            }
        }
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (mappingComponent.isReadOnly()) {
            ((DefaultFeatureCollection)(mappingComponent.getFeatureCollection())).removeFeaturesByInstance(
                PureNewFeature.class);
        }

        if (isInMode(TEXT)) {
            if (pInputEvent.isLeftMouseButton()) {
                Point2D point = null;
                if (point == null) {
                    point = pInputEvent.getPosition();
                }
                points = new ArrayList<>();
                snappedCoordinates.clear();
                points.add(point);
                readyForFinishing(pInputEvent);
            }
        } else if (isInMode(POINT)) {
            if (pInputEvent.isLeftMouseButton()) {
                snappedCoordinates.clear();
                final PFeatureTools.SnappedPoint snappedPoint = PFeatureTools.getNearestPointInArea(
                        mappingComponent,
                        pInputEvent.getCanvasPosition(),
                        true,
                        null);
                if ((MappingComponent.SnappingMode.POINT.equals(mappingComponent.getSnappingMode())
                                || MappingComponent.SnappingMode.BOTH.equals(mappingComponent.getSnappingMode()))
                            && !PFeatureTools.SnappedPoint.SnappedOn.NOTHING.equals(snappedPoint.getSnappedOn())) {
                    final Coordinate coord = PFeatureTools.getNearestCoordinateInArea(
                            mappingComponent,
                            pInputEvent.getCanvasPosition(),
                            true,
                            null);
                    if (coord != null) {
                        snappedCoordinates.put(snappedPoint.getPoint(), coord);
                    }
                }
                points = new ArrayList<>();
                points.add(snappedPoint.getPoint());
                readyForFinishing(pInputEvent);
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
            final PFeatureTools.SnappedPoint snappedPoint = PFeatureTools.getNearestPointInArea(
                    mappingComponent,
                    pInputEvent.getCanvasPosition(),
                    true,
                    null);
            // wenn snappingpoint vorhanden, dann den nehmen, ansonsten normalen punkt unter der maus ermitteln
            final Point2D point = (snappedPoint != null) ? snappedPoint.getPoint() : pInputEvent.getPosition();

            if (pInputEvent.isLeftMouseButton()) {
                if (!inProgress) {
                    inProgress = true;
                    initTempFeature();
                    startPoint = point;
                    points = new ArrayList<Point2D>(5);
                    snappedCoordinates.clear();
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
                        readyForFinishing(pInputEvent);
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
            if (pInputEvent.isLeftMouseButton()) {
                if (pInputEvent.getClickCount() == 1) {
                    Point2D point = null;
                    undoPoints.clear();
                    if (!inProgress) {
                        snappedCoordinates.clear();
                    }
                    if (mappingComponent.isSnappingEnabled()) {
                        point = PFeatureTools.getNearestPointInArea(
                                    mappingComponent,
                                    pInputEvent.getCanvasPosition(),
                                    true,
                                    null).getPoint();
                        if ((point != null)
                                    && (MappingComponent.SnappingMode.POINT.equals(mappingComponent.getSnappingMode())
                                        || MappingComponent.SnappingMode.BOTH.equals(
                                            mappingComponent.getSnappingMode()))) {
                            final Coordinate coord = PFeatureTools.getNearestCoordinateInArea(
                                    mappingComponent,
                                    pInputEvent.getCanvasPosition(),
                                    true,
                                    null);
                            if (coord != null) {
                                snappedCoordinates.put(point, coord);
                            }
                        }
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
                } else if (pInputEvent.getClickCount() == 2) {
                    if (isInMode(POLYGON)) {
                        if (points.size() == 2) { // bei polygonen mit nur 2 punkten
                                                  // wird eine boundingbox angelegt
                            //
                            // Rectangle QuickSnap
                            //
                            // because of this code one is able to create a rectangular polygon with a click and a
                            // following doubleclick
                            //
                            // .....P1--------------P2 .....|               | .....|               | .....|
                            // | .....P3--------------P4
                            //
                            //

                            final Point2D pointP1 = points.get(0);
                            final Point2D pointP4 = points.get(1);
                            points.remove(pointP4);

                            // P2
                            points.add(new Point2D.Double(pointP1.getX(), pointP4.getY()));

                            // P4
                            points.add(pointP4);

                            // P3
                            points.add(new Point2D.Double(pointP4.getX(), pointP1.getY()));

                            // No need to add P1 to close the polygon, because the polygon part of the code will care
                            // about that
                        } else if (points.size() < 2) {
                            return; // ignorieren, da weniger als 2 Punkte nicht ausreichen
                        }
                    }
                    readyForFinishing(pInputEvent);
                }
            } else if (pInputEvent.isRightMouseButton()) {
                if (inProgress) {
                    if ((points.size() < 2) || (pInputEvent.getClickCount() == 2)) {
                        // abbrechen
                        if (tempFeature != null) {
                            mappingComponent.getTmpFeatureLayer().removeChild(tempFeature);
                        }
                        inProgress = false;
                    } else {
                        points.remove(points.size() - 1);
                        final Point2D point = PFeatureTools.getNearestPointInArea(
                                    mappingComponent,
                                    pInputEvent.getCanvasPosition(),
                                    true,
                                    null)
                                    .getPoint();
                        updatePolygon(point);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void readyForFinishing(final PInputEvent event) {
        try {
            finishingEvent = event;
            if ((currentFeature == null) || ((currentFeature != null) && currentFeature.getGeometry().isValid())) {
                createCurrentNewFeature(null);
                finishGeometry(currentFeature);
                inProgress = false;
            } else {
                reset();
            }
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

    protected AbstractNewFeature getCurrentNewFeature() {
        return currentFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastPoint  DOCUMENT ME!
     */
    protected void createCurrentNewFeature(final Point2D lastPoint) {
        try {
            final Point2D[] finalPoints = getPoints(lastPoint);
            AbstractNewFeature.geomTypes geomType = AbstractNewFeature.geomTypes.UNKNOWN;
            if (isInMode(ELLIPSE)) {
                geomType = AbstractNewFeature.geomTypes.ELLIPSE;
            } else if (isInMode(LINESTRING)) {
                geomType = AbstractNewFeature.geomTypes.LINESTRING;
            } else if (isInMode(POINT)) {
                geomType = AbstractNewFeature.geomTypes.POINT;
            } else if (isInMode(POLYGON)) {
                geomType = AbstractNewFeature.geomTypes.POLYGON;
            } else if (isInMode(RECTANGLE)) {
                geomType = AbstractNewFeature.geomTypes.POLYGON;
            } else if (isInMode(RECTANGLE_FROM_LINE)) {
                geomType = AbstractNewFeature.geomTypes.POLYGON;
            } else if (isInMode(TEXT)) {
                geomType = AbstractNewFeature.geomTypes.TEXT;
            }

            final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
            final Constructor<? extends AbstractNewFeature> constructor = geometryFeatureClass.getConstructor(
                    Point2D[].class,
                    WorldToScreenTransform.class);
            AbstractNewFeature newFeature = constructor.newInstance(finalPoints, mappingComponent.getWtst());
            newFeature.setGeometryType(geomType);
            newFeature.getGeometry().setSRID(currentSrid);
            Geometry geom = newFeature.getGeometry();

            if (finalPoints != null) {
                boolean coordinatesChanged = false;

                for (int i = 0; i < finalPoints.length; ++i) {
                    final Coordinate coord = snappedCoordinates.get(finalPoints[i]);
                    if (coord != null) {
                        if ((geom.getCoordinates()[i].x != coord.x) || (geom.getCoordinates()[i].y != coord.y)) {
                            geom.getCoordinates()[i].x = coord.x;
                            geom.getCoordinates()[i].y = coord.y;
                            coordinatesChanged = true;
                        }
                    }

                    if (coordinatesChanged) {
                        geom.geometryChanged();
                    }
                }
            }

            geom = CrsTransformer.transformToGivenCrs(geom,
                    mappingComponent.getMappingModel().getSrs().getCode());

            if (isInMode(LINESTRING) && geom.getGeometryType().equals("Polygon")) {
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        currentSrid);
                geom = factory.createLineString(((Polygon)geom).getExteriorRing().getCoordinates());
            }

            newFeature.setGeometry(geom);

            if (isInMode(TEXT)) {
                final AbstractNewFeature f = newFeature;

                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            createTextNode(f);
                        }
                    });

                newFeature = null;
            }

            currentFeature = newFeature;
        } catch (Throwable throwable) {
            LOG.error("Error during the creation of the geometry", throwable); // NOI18N
            currentFeature = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    private void createTextNode(final AbstractNewFeature newFeature) {
        final NewTextDialog dialog = new NewTextDialog(StaticSwingTools.getParentFrame(mappingComponent), false);
        dialog.setRunWhenFinish(new Runnable() {

                @Override
                public void run() {
                    if (dialog.isConfirmed()) {
                        newFeature.setName(dialog.getText());

                        if (!dialog.isAutoScaleEnabled()) {
                            final int fontSize = (int)(mappingComponent.getScaleDenominator() / 3700 * 12);
                            Font f = dialog.getFont();

                            if (f == null) {
                                f = new Font("sansserif", Font.PLAIN, fontSize);
                            }
                            newFeature.setPrimaryAnnotationFont(f);
                        } else {
                            Font f = dialog.getFont();
                            if (f == null) {
                                final int fontSize = (int)(mappingComponent.getScaleDenominator() / 3700 * 12);
                                f = new Font("sansserif", Font.PLAIN, fontSize);
                            } else {
                                final int fontSize = (int)(mappingComponent.getScaleDenominator() / 3700 * f.getSize());
                                f = f.deriveFont(fontSize);
                            }
                            newFeature.setPrimaryAnnotationFont(f);
                        }
                        newFeature.setAutoScale(dialog.isAutoScaleEnabled());

                        if (dialog.isHaloEnabled()) {
                            newFeature.setPrimaryAnnotationHalo(Color.WHITE);
                        }
                        finishGeometry(newFeature);
                    }
                }
            });

        final Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        dialog.setLocation((int)(mouseLocation.getX() + 10), (int)mouseLocation.getY());
        dialog.setVisible(true);
    }

    @Override
    public void mouseReleased(final PInputEvent arg0) {
        super.mouseReleased(arg0);
        if (inProgress) {
            if (isInMode(RECTANGLE) || isInMode(ELLIPSE)) {
                readyForFinishing(arg0);
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
        } else if (!inProgress && points != null && points.isEmpty() && event.isControlDown()) {
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
    private void createAction(final MappingComponent m, final AbstractNewFeature f) {
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
        createCurrentNewFeature(lastPoint);
        if (!(currentFeature instanceof SearchFeature)) {
            final ArrayList<Feature> features = new ArrayList<Feature>();
            features.add(currentFeature);
            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).fireFeaturesChanged(features);
        }
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
    protected Point2D[] getPoints(Point2D lastPoint) {
        try {
            final boolean movin;
            int plus;
            if (lastPoint == points.get(points.size() - 1)) {
                lastPoint = null;
            }
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
    public final void setGeometryFeatureClass(final Class<? extends AbstractNewFeature> geometryFeatureClass) {
        this.geometryFeatureClass = geometryFeatureClass;
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
            snappedCoordinates.clear();
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
            snappedCoordinates.clear();
            for (int i = 0; i < coordArr.length; i++) {
                points.add(new Point2D.Double(startX - coordArr[i].x, startY - coordArr[i].y));
            }

            updatePolygon(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    protected void finishGeometry(final AbstractNewFeature newFeature) {
        mappingComponent.getTmpFeatureLayer().removeAllChildren();
        currentLength = null;
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

    /**
     * DOCUMENT ME!
     *
     * @param  mappingComponent  the mappingComponent to set
     */
    public void setMappingComponent(final MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }
}
