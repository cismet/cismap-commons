/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleMoveListener.java
 *
 * Created on 10. M\u00E4rz 2005, 10:10
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import java.util.Collection;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.Highlightable;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.RequestForNonreflectingFeature;
import de.cismet.cismap.commons.features.RequestForUnaddableHandles;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.AddHandleDialog;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell/nh
 * @version  $Revision$, $Date$
 */
public class SimpleMoveListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String COORDINATES_CHANGED = "COORDINATES_CHANGED"; // NOI18N
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SimpleMoveListener.class);
    private static Color COLOR_ADD_HANDLE = new Color(255, 0, 0, 150);
    private static Color COLOR_REFLECT_HANDLE = new Color(205, 133, 0, 150);

    //~ Instance fields --------------------------------------------------------

    Highlightable highlighted = null;
    Object handleHighlightingStuff = new Object();
    private final MappingComponent mappingComponent;
    private PFeature underlyingObject = null;
    private int entityPosition;
    private int ringPosition;
    private int coordPosition = 0;
    private double xCoord = -1.0d;
    private double yCoord = -1.0d;
    private float handleX = Float.MIN_VALUE;
    private float handleY = Float.MIN_VALUE;
    private PHandle newPointHandle = null;
    private PFeature pFeature = null;
    private PLocator locator = null;
    private PNode pointerAnnotation = new PNode();
    private PPath snapRect = PPath.createRectangle(0.0f, 0.0f, 20.0f, 20.0f);
    private boolean annotationNodeVisible = false;
    private double underlyingObjectHalo = 0.0d;
    private boolean deepSeekEnabled = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimpleMoveListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public SimpleMoveListener(final MappingComponent mc) {
        super();
        this.mappingComponent = mc;
        mc.getCamera().addChild(pointerAnnotation);
        mc.getCamera().addChild(snapRect);
        snapRect.setStroke(null);
        snapRect.setTransparency(0.2f);
        snapRect.setVisible(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final PInputEvent event) {
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    try {
                        underlyingObject = null;
                        final Object o;
                        if (underlyingObjectHalo > 0.0d) {
                            o = PFeatureTools.getFirstValidObjectUnderPointer2(
                                    event,
                                    new Class[] { PFeature.class },
                                    underlyingObjectHalo);
                        } else {
                            o = PFeatureTools.getFirstValidObjectUnderPointer(
                                    event,
                                    new Class[] { PFeature.class },
                                    deepSeekEnabled);
                        }
                        if (o instanceof PFeature) {
                            underlyingObject = (PFeature)o;
                        }
                        if (underlyingObject != null) {
                            if (mappingComponent.getInteractionMode().equals(MappingComponent.SELECT)
                                        && ((!(underlyingObject.getFeature() instanceof RequestForUnaddableHandles)
                                                && mappingComponent.getHandleInteractionMode().equals(
                                                    MappingComponent.ADD_HANDLE))
                                            || (!(underlyingObject.getFeature()
                                                    instanceof RequestForNonreflectingFeature)
                                                && mappingComponent.getHandleInteractionMode().equals(
                                                    MappingComponent.REFLECT_POLYGON)))) {
                                if ((mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection)
//                                  no one knows, why the following condition was there
//                                            && (((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
//                                                .getSelectedFeatures().size() == 1)
                                ) {
                                    if (!newPointHandleExists()) {
                                        createNewPointHandle();
                                    }
                                    if (!(underlyingObject.getFeature() instanceof RequestForUnaddableHandles)
                                                && mappingComponent.getHandleInteractionMode().equals(
                                                    MappingComponent.ADD_HANDLE)) {
                                        newPointHandle.setPaint(COLOR_ADD_HANDLE);
                                    } else if (!(underlyingObject.getFeature()
                                                    instanceof RequestForNonreflectingFeature)
                                                && mappingComponent.getHandleInteractionMode().equals(
                                                    MappingComponent.REFLECT_POLYGON)) {
                                        newPointHandle.setPaint(COLOR_REFLECT_HANDLE);
                                    }

                                    if (event.isAltDown()) {
                                        handleX = (float)event.getPosition().getX();
                                        handleY = (float)event.getPosition().getY();
                                    } else {
                                        final Collection sel =
                                            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                                    .getSelectedFeatures();
                                        final Point2D trigger = event.getPosition();
                                        final Point2D[] neighbours = getNearestNeighbours(trigger, sel);

                                        final Point2D p0 = neighbours[0];
                                        final Point2D p1 = neighbours[1];
                                        if ((p0 != null) && (p1 != null)) {
                                            if (event.isShiftDown()) {
                                                final Point2D p0i1 = new Point2D.Double(p0.getX(), p1.getY());
                                                final Point2D p1i0 = new Point2D.Double(p1.getX(), p0.getY());
                                                final Line2D lOrig = new Line2D.Double(p0, p1);
                                                final Line2D lInversed = new Line2D.Double(p0i1, p1i0);
                                                final Point2D erg = StaticGeometryFunctions.createIntersectionPoint(
                                                        lOrig,
                                                        lInversed);
                                                handleX = (float)erg.getX();
                                                handleY = (float)erg.getY();
                                            } else {
                                                final Point2D erg = StaticGeometryFunctions.createPointOnLine(
                                                        p0,
                                                        p1,
                                                        trigger);
                                                handleX = (float)erg.getX();
                                                handleY = (float)erg.getY();
                                            }
                                        }
                                        boolean found = false;
                                        for (final Object po : mappingComponent.getHandleLayer().getChildrenReference()) {
                                            if ((po instanceof PHandle) && ((PHandle)po == newPointHandle)) {
                                                found = true;
                                            }
                                        }
                                        if (!found) {
                                            // EventQueue.invokeLater(new Runnable() {
                                            // public void run() {
                                            mappingComponent.getHandleLayer().addChild(newPointHandle);
                                            LOG.info("tempor\u00E4res Handle eingef\u00FCgt"); // NOI18N
                                            // }
                                            // });
                                        }
                                        newPointHandle.relocateHandle();
                                    }
                                }
                            }
                        }
                        final WorldToScreenTransform wtst = mappingComponent.getWtst();

                        if (wtst == null) {
                            return;
                        }
                        xCoord = wtst.getSourceX(event.getPosition().getX() - mappingComponent.getClip_offset_x());
                        yCoord = wtst.getSourceY(event.getPosition().getY() - mappingComponent.getClip_offset_y());

                        refreshPointerAnnotation(event);

                        postCoordinateChanged();
                        try {
                            mappingComponent.getSnapHandleLayer().removeAllChildren();
                        } catch (Exception e) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Fehler beim entfernen der SnappingVisualisierung", e); // NOI18N
                            }
                        }

                        if (mappingComponent.isVisualizeSnappingEnabled()) {
                            final PFeatureTools.SnappedPoint snappedPoint = PFeatureTools.getNearestPointInArea(
                                    mappingComponent,
                                    event.getCanvasPosition(),
                                    true,
                                    null);
                            if (!PFeatureTools.SnappedPoint.SnappedOn.NOTHING.equals(
                                            snappedPoint.getSnappedOn())) {
                                final Point2D nearestPoint = snappedPoint.getPoint();
                                mappingComponent.getCamera().viewToLocal(nearestPoint);
                                final PPath show = PPath.createEllipse((float)(nearestPoint.getX() - 3),
                                        (float)(nearestPoint.getY() - 3),
                                        (float)(6),
                                        (float)(6));
                                switch (snappedPoint.getSnappedOn()) {
                                    case POINT: {
                                        show.setPaint(Color.BLACK);
                                    }
                                    break;
                                    case LINE: {
                                        show.setPaint(Color.GRAY);
                                    }
                                    break;
                                }
                                mappingComponent.getSnapHandleLayer().addChild(show);
                            }
                            if (mappingComponent.isVisualizeSnappingRectEnabled()) {
                                snapRect.setVisible(true);
                                snapRect.setPathToRectangle((int)event.getCanvasPosition().getX()
                                            - (mappingComponent.getSnappingRectSize() / 2),
                                    (int)event.getCanvasPosition().getY()
                                            - (mappingComponent.getSnappingRectSize() / 2),
                                    mappingComponent.getSnappingRectSize(),
                                    mappingComponent.getSnappingRectSize());
                            } else {
                                snapRect.setVisible(false);
                            }
                        }
                    } catch (Exception e) {
                        LOG.info("Fehler beim Moven \u00FCber die Karte ", e); // NOI18N
                    }
                    handleHighlightingStuff(event);
                }
            };
        EventQueue.invokeLater(t);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  deepSeekEnabled  DOCUMENT ME!
     */
    public void setDeepSeekEnabled(final boolean deepSeekEnabled) {
        this.deepSeekEnabled = deepSeekEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean newPointHandleExists() {
        return (locator != null) && (newPointHandle != null);
    }

    /**
     * DOCUMENT ME!
     */
    private void createNewPointHandle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create newPointHandle and Locator"); // NOI18N
        }
        locator = new PLocator() {

                @Override
                public double locateX() {
                    return handleX;
                }

                @Override
                public double locateY() {
                    return handleY;
                }
            };
        newPointHandle = new PHandle(locator, mappingComponent) {

                @Override
                public void handleClicked(final PInputEvent e) {
                    SimpleMoveListener.this.mouseClicked(e);
                    // super.handleClicked(pInputEvent);
                }
            };
    }

    /**
     * Sucht in der \u00FCbergebenen PFeature-Collection nach den zwei der Mausposition am n\u00E4chsten gelegenen
     * Eckpunkte EINES PFeatures.
     *
     * @param   trigger  Mausposition
     * @param   sel      Collection aller selektierter PFeatures
     *
     * @return  Point2D-Array mit den zwei gefundenen Punkten
     */
    private Point2D[] getNearestNeighbours(final Point2D trigger, final Collection sel) {
        Point2D start = null;
        Point2D end = null;
        double dist = Double.POSITIVE_INFINITY;
        for (final Object o : sel) {
            if (o instanceof Feature) {
                final Feature feature = (Feature)o;
                final PFeature pfeature = (PFeature)mappingComponent.getPFeatureHM().get(feature);
                if (pfeature != null) {
                    final Geometry geometry = pfeature.getFeature().getGeometry();
                    if ((geometry instanceof Polygon) || (geometry instanceof LineString)
                                || (geometry instanceof MultiPolygon)) {
                        for (int entityIndex = 0; entityIndex < pfeature.getNumOfEntities(); entityIndex++) {
                            for (int ringIndex = 0; ringIndex < pfeature.getNumOfRings(entityIndex); ringIndex++) {
                                final float[] xp = pfeature.getXp(entityIndex, ringIndex);
                                final float[] yp = pfeature.getYp(entityIndex, ringIndex);
                                for (int i = 0; i < (xp.length - 1); i++) {
                                    final Point2D tmpStart = new Point2D.Double(xp[i], yp[i]);
                                    final Point2D tmpEnd = new Point2D.Double(xp[i + 1], yp[i + 1]);
                                    final double tmpDist = StaticGeometryFunctions.distanceToLine(
                                            tmpStart,
                                            tmpEnd,
                                            trigger);
                                    if (tmpDist < dist) {
                                        dist = tmpDist;
                                        start = tmpStart;
                                        end = tmpEnd;
                                        this.pFeature = pfeature;
                                        this.entityPosition = entityIndex;
                                        this.ringPosition = ringIndex;
                                        this.coordPosition = i + 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        final Point2D[] erg = { start, end };
        return erg;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void handleHighlightingStuff(final PInputEvent event) {
        synchronized (handleHighlightingStuff) {
            final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                    event,
                    new Class[] { PFeature.class, PHandle.class },
                    deepSeekEnabled);
            try {
                final PNode n = (PNode)o;
                setMouseCursorAccordingToMode(n);

                if (CismapBroker.getInstance().isHighlightFeatureOnMouseOver()) {
                    if (n instanceof Highlightable) {
                        if (highlighted != null) {
                            highlighted.setHighlighting(false);
                        }
                        ((Highlightable)n).setHighlighting(true);
                        highlighted = (Highlightable)n;
                    } else if (highlighted != null) {
                        highlighted.setHighlighting(false);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Fehler beim Highlighten", e); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postCoordinateChanged() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(COORDINATES_CHANGED, this);
    }

    /**
     * Falls die \u00FCbergebene PNode ein PHandle ist wir der Cursor entsprechend dem in der MappingComponent gesetzen
     * Modus gesetzt.
     *
     * @param  n  PNode-Instanz
     */
    private void setMouseCursorAccordingToMode(final PNode n) {
        Cursor c = null;
        if (n instanceof PHandle) {
            c = mappingComponent.getCursor(mappingComponent.getHandleInteractionMode());
        } else {
            c = mappingComponent.getCursor(mappingComponent.getInteractionMode());
        }
        if ((c != null) && (mappingComponent.getCursor() != c)) {
            mappingComponent.setCursor(c);
        }
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        try {
            if ((event.getClickCount() == 2) && mappingComponent.getInteractionMode().equals(MappingComponent.SELECT)
                        && ((pFeature != null) && pFeature.isSelected())) {
                // Selektiertes Feature holen
                final Collection sel = ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                            .getSelectedFeatures();

                if (!(pFeature.getFeature() instanceof RequestForUnaddableHandles)
                            && mappingComponent.getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)) {
                    // markiertes Handel auf der Linie holen
                    final Point2D newPoint = new Point2D.Float(handleX, handleY);
                    final Point2D[] neighbours = getNearestNeighbours(newPoint, sel);

                    final Point2D p0 = neighbours[0];
                    final Point2D p1 = neighbours[1];
                    if ((p0 != null) && (p1 != null)) {
                        // CTRL-Taste beim Klicken gedrückt
                        if (event.isControlDown()) {
                            // welcher Nachbar ist weiter Links/Rechts?
                            Point2D leftNeighbour;
                            Point2D rightNeighbour;
                            if (p0.getX() < p1.getX()) {
                                // Nachbar "0" ist weiter Links
                                leftNeighbour = p0;
                                rightNeighbour = p1;
                            } else if (p1.getX() < p0.getX()) {
                                // Nachbar "1" ist weiter Links
                                leftNeighbour = p1;
                                rightNeighbour = p0;
                            } else // Nachbar "0" und "1" liegen genau übereinander
                            {
                                if (p0.getY() <= p1.getY()) {
                                    // Nachbar "0" ist weiter oben (wird als weiter Links interpretiert)
                                    leftNeighbour = p0;
                                    rightNeighbour = p1;
                                } else {
                                    // Nachbar "1" ist weiter oben (wird als weiter Links interpretiert)
                                    leftNeighbour = p1;
                                    rightNeighbour = p0;
                                }
                            }

                            // Abstand zum linken Nachbar berechnen
                            Double distanceLeft = leftNeighbour.distance(newPoint);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("distanceLeft: " + distanceLeft); // NOI18N
                            }

                            // Gesamt-Abstand berechnen
                            final Double distanceTotal = leftNeighbour.distance(rightNeighbour);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("distanceTotal: " + distanceTotal); // NOI18N
                            }

                            // Dialog modal aufrufen und Werte übergeben
                            final AddHandleDialog dialog = AddHandleDialog.getInstance();
                            dialog.setDistanceTotal(distanceTotal);
                            dialog.setDistanceToLeft(distanceLeft);

                            // Dialog zentrieren und sichtbar machen
                            StaticSwingTools.showDialog(dialog);

                            // wenn der Dialog mit OK geschlossen wurde
                            if (dialog.getReturnStatus() == AddHandleDialog.STATUS_OK) {
                                // ist der gewählte Punkt näher an Links
                                if (dialog.getDistanceToLeft() < dialog.getDistanceToRight()) {
                                    // Abstand von Links aus berechnen
                                    distanceLeft = dialog.getDistanceToLeft();
                                } else { // ist der gewählte Punkt nächer an rechts
                                    // Abstand von Rechts aus berechnen
                                    distanceLeft = distanceTotal - dialog.getDistanceToRight();
                                }
                                // Abstand kann nicht größer als Gesamt-Abstand sein
                                distanceLeft = (distanceLeft > distanceTotal) ? distanceTotal : distanceLeft;
                                // ist die Gesamtlänge ungleich 0 (division durch null verhindern)
                                if (distanceTotal != 0) {
                                    // Handle-Koordinaten anhand des Abstands berechnen
                                    handleX = (float)(leftNeighbour.getX()
                                                    + ((rightNeighbour.getX() - leftNeighbour.getX())
                                                        * (distanceLeft / distanceTotal)));
                                    handleY = (float)(leftNeighbour.getY()
                                                    + ((rightNeighbour.getY() - leftNeighbour.getY())
                                                        * (distanceLeft / distanceTotal)));
                                } else { // ist die Gesamtlänge 0
                                    // Handle-Koordinaten sind gleich die des linken Nachbarn
                                    handleX = (float)leftNeighbour.getX();
                                    handleY = (float)leftNeighbour.getY();
                                }
                            } else { // wenn der Dialog nicht mit OK geschlossen wurde
                                // nichts tun
                                super.mouseClicked(event);
                                return;
                            }
                        }
                    }

                    pFeature.insertCoordinate(entityPosition, ringPosition, coordPosition, handleX, handleY);
                } else if (!(pFeature.getFeature() instanceof RequestForNonreflectingFeature)
                            && mappingComponent.getHandleInteractionMode().equals(MappingComponent.REFLECT_POLYGON)) {
                    reflectFeature(pFeature, entityPosition, ringPosition, coordPosition - 1, coordPosition);
                }

                mappingComponent.getMemRedo().clear();
                resetAfterClick();
            }
        } catch (Exception ex) {
            LOG.error("Fehler beim Anlegen von neuer Koordinate und Handle", ex); // NOI18N
        }
        super.mouseClicked(event);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pf                  DOCUMENT ME!
     * @param  entityPosition      DOCUMENT ME!
     * @param  ringPosition        DOCUMENT ME!
     * @param  leftCoordPosition   DOCUMENT ME!
     * @param  rightCoordPosition  DOCUMENT ME!
     */
    private void reflectFeature(final PFeature pf,
            final int entityPosition,
            final int ringPosition,
            final int leftCoordPosition,
            final int rightCoordPosition) {
        final Geometry origGeom = pf.getFeature().getGeometry();
        final Coordinate[] coordArr = pf.getCoordArr(entityPosition, ringPosition);
        final Geometry reflectGeom = AffineTransformation.reflectionInstance(
                    coordArr[leftCoordPosition].x,
                    coordArr[leftCoordPosition].y,
                    coordArr[rightCoordPosition].x,
                    coordArr[rightCoordPosition].y)
                    .transform(origGeom);

        final PureNewFeature reflectFeature = new PureNewFeature(reflectGeom);

        final PureNewFeature.geomTypes geomType;
        if (pf.getFeature() instanceof PureNewFeature) {
            final PureNewFeature origPureNewFeature = (PureNewFeature)pf.getFeature();
            geomType = origPureNewFeature.getGeometryType();
        } else if (reflectGeom instanceof MultiPolygon) {
            geomType = PureNewFeature.geomTypes.MULTIPOLYGON;
        } else if (reflectGeom instanceof Polygon) {
            geomType = PureNewFeature.geomTypes.POLYGON;
        } else if (reflectGeom instanceof LineString) {
            geomType = PureNewFeature.geomTypes.LINESTRING;
        } else if (reflectGeom instanceof Point) {
            geomType = PureNewFeature.geomTypes.POINT;
        } else {
            geomType = PureNewFeature.geomTypes.UNKNOWN;
        }

        reflectFeature.setGeometryType(geomType);
        reflectFeature.setEditable(true);

        mappingComponent.getFeatureCollection().addFeature(reflectFeature);
        mappingComponent.getFeatureCollection().holdFeature(reflectFeature);
    }

    /**
     * Setzt den Listener nach einem erfolgreichen Einf\u00FCgen eines neuen Punkts wieder auf den Anfangszustand
     * zur\u00FCck.
     */
    private void resetAfterClick() {
        newPointHandle = null;
        handleX = Float.MIN_VALUE;
        handleY = Float.MIN_VALUE;
        coordPosition = 0;
        pFeature = null;
    }

    @Override
    public void mouseDragged(final PInputEvent event) {
        super.mouseDragged(event);
        refreshPointerAnnotation(event);
    }

    @Override
    public void mouseEntered(final PInputEvent event) {
        super.mouseEntered(event);
        if (pointerAnnotation != null) {
            refreshPointerAnnotation(event);
            pointerAnnotation.setVisible(annotationNodeVisible);
        }
    }

    @Override
    public void mouseExited(final PInputEvent event) {
        super.mouseExited(event);
        if (pointerAnnotation != null) {
            pointerAnnotation.setVisible(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void refreshPointerAnnotation(final PInputEvent event) {
        if (pointerAnnotation != null) {
            pointerAnnotation.setOffset(event.getCanvasPosition().getX() + 20.0d,
                event.getCanvasPosition().getY()
                        + 20.0d);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getXCoord() {
        return xCoord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getYCoord() {
        return yCoord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getCurrentOGCScale() {
        return mappingComponent.getCurrentOGCScale();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getUnderlyingPFeature() {
        return underlyingObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAnnotationNodeVisible() {
        return annotationNodeVisible;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  annotationNodeVisible  DOCUMENT ME!
     */
    public void setAnnotationNodeVisible(final boolean annotationNodeVisible) {
        this.annotationNodeVisible = annotationNodeVisible;
        if (this.pointerAnnotation != null) {
            try {
                mappingComponent.getCamera().removeChild(this.pointerAnnotation);
            } catch (final Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no child to remove", ex);
                }
            }
            if (annotationNodeVisible) {
                mappingComponent.getCamera().addChild(pointerAnnotation);
                pointerAnnotation.setVisible(true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PNode getPointerAnnotation() {
        return pointerAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointerAnnotation  DOCUMENT ME!
     */
    public void setPointerAnnotation(final PNode pointerAnnotation) {
        setAnnotationNodeVisible(false);
        this.pointerAnnotation = pointerAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  underlyingObjectHalo  DOCUMENT ME!
     */
    public void setUnderlyingObjectHalo(final double underlyingObjectHalo) {
        this.underlyingObjectHalo = underlyingObjectHalo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getUnderlyingObjectHalo() {
        return underlyingObjectHalo;
    }
}
