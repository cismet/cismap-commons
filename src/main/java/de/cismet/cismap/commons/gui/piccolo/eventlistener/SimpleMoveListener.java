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

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.Highlightable;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.AddHandleDialog;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleDeleteAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.CismetThreadPool;

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

    //~ Instance fields --------------------------------------------------------

    Highlightable highlighted = null;
    Object handleHighlightingStuff = new Object();

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private MappingComponent mc;
    private PFeature underlyingObject = null;
    private int positionInArray = 0;
    private double xCoord = -1.0d;
    private double yCoord = -1.0d;
    private float handleX = Float.MIN_VALUE;
    private float handleY = Float.MIN_VALUE;
    private PHandle newPointHandle = null;
    private PFeature pf = null;
    private PLocator l = null;
    private PNode pointerAnnotation = new PNode();
    private PPath snapRect = PPath.createRectangle(0.0f, 0.0f, 20.0f, 20.0f);
    private boolean annotationNodeVisible = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimpleMoveListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public SimpleMoveListener(final MappingComponent mc) {
        super();
        this.mc = mc;
        mc.getCamera().addChild(pointerAnnotation);
        mc.getCamera().addChild(snapRect);
        snapRect.setStroke(null);
        snapRect.setTransparency(0.2f);
        snapRect.setVisible(false);
        pointerAnnotation.setVisible(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final PInputEvent event) {
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    try {
                        if (mc.getInteractionMode().equals(MappingComponent.SELECT)
                                    && mc.getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)) {
                            if ((mc.getFeatureCollection() instanceof DefaultFeatureCollection)
                                        && (((DefaultFeatureCollection)mc.getFeatureCollection()).getSelectedFeatures()
                                            .size() == 1)) {
                                if ((l == null) || (newPointHandle == null)) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("create newPointHandle and Locator"); // NOI18N
                                    }
                                    l = new PLocator() {

                                            @Override
                                            public double locateX() {
                                                return handleX;
                                            }
                                            @Override
                                            public double locateY() {
                                                return handleY;
                                            }
                                        };
                                    newPointHandle = new PHandle(l, mc) {

                                            @Override
                                            public void handleClicked(final PInputEvent e) {
                                                SimpleMoveListener.this.mouseClicked(e);
                                                // super.handleClicked(pInputEvent);
                                            }
                                        };
                                    newPointHandle.setPaint(new Color(255, 0, 0, 150));
                                }
                                final Collection sel = ((DefaultFeatureCollection)mc.getFeatureCollection())
                                            .getSelectedFeatures();
                                final Point2D trigger = event.getPosition();
                                final Point2D[] neighbours = getNearestNeighbours(trigger, sel);

                                final Point2D p0 = neighbours[0];
                                final Point2D p1 = neighbours[1];
                                if ((p0 != null) && (p1 != null)) {
                                    final Point2D erg = StaticGeometryFunctions.createPointOnLine(p0, p1, trigger);
//                            Point2D erg;
//                            // CTRL-Taste gedrückt
//                            if (event.getModifiers() == InputEvent.CTRL_MASK) {
//                                // Handle zwischen dem linken und rechten Nachbar zentrieren
//                                Double centerX = (neighbours[0].getX() + neighbours[1].getX()) / (double) 2;
//                                Double centerY = (neighbours[0].getY() + neighbours[1].getY()) / (double) 2;
//                                erg = new Point2D.Double(centerX, centerY);
//                            } else { // CTRL-Taste nicht gedrückt
//                                // Handle folgt auf der Linie der Maus
//                                erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);
//                            }
                                    handleX = (float)erg.getX();
                                    handleY = (float)erg.getY();
                                    boolean found = false;
                                    for (final Object o : mc.getHandleLayer().getChildrenReference()) {
                                        if ((o instanceof PHandle) && ((PHandle)o == newPointHandle)) {
                                            found = true;
                                        }
                                    }
                                    if (!found) {
                                        // EventQueue.invokeLater(new Runnable() {
                                        // public void run() {
                                        mc.getHandleLayer().addChild(newPointHandle);
                                        log.info("tempor\u00E4res Handle eingef\u00FCgt"); // NOI18N
                                        // }
                                        // });
                                    }
                                    newPointHandle.relocateHandle();
                                }
                            }
                        }
                        xCoord = mc.getWtst().getSourceX(event.getPosition().getX() - mc.getClip_offset_x());
                        yCoord = mc.getWtst().getSourceY(event.getPosition().getY() - mc.getClip_offset_y());

                        refreshPointerAnnotation(event);

                        underlyingObject = null;
                        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                                event,
                                new Class[] { PFeature.class });
                        if (o instanceof PFeature) {
                            underlyingObject = (PFeature)o;
                        }
                        postCoordinateChanged();
                        try {
                            mc.getSnapHandleLayer().removeAllChildren();
                        } catch (Exception e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Fehler beim entfernen der SnappingVisualisierung", e); // NOI18N
                            }
                        }

                        if (mc.isVisualizeSnappingEnabled()) {
                            final Point2D nearestPoint = PFeatureTools.getNearestPointInArea(
                                    mc,
                                    event.getCanvasPosition());
                            if (nearestPoint != null) {
                                mc.getCamera().viewToLocal(nearestPoint);
                                final PPath show = PPath.createEllipse((float)(nearestPoint.getX() - 3),
                                        (float)(nearestPoint.getY() - 3),
                                        (float)(6),
                                        (float)(6));
                                show.setPaint(new Color(0, 0, 0));
                                mc.getSnapHandleLayer().addChild(show);
                            }
                            if (mc.isVisualizeSnappingRectEnabled()) {
                                snapRect.setVisible(true);
                                snapRect.setPathToRectangle((int)event.getCanvasPosition().getX()
                                            - (mc.getSnappingRectSize() / 2),
                                    (int)event.getCanvasPosition().getY()
                                            - (mc.getSnappingRectSize() / 2),
                                    mc.getSnappingRectSize(),
                                    mc.getSnappingRectSize());
                            } else {
                                snapRect.setVisible(false);
                            }
                        }
                    } catch (Exception e) {
                        log.info("Fehler beim Moven \u00FCber die Karte ", e); // NOI18N
                    }
                    handleHighlightingStuff(event);
                }
            };
        // t.setPriority(Thread.NORM_PRIORITY);
        // t.start();
        // CismetThreadPool.execute(t);
        // Workaround für Issue 0001202 (http://bugs.cismet.de/mantis/view.php?id=1202)
        EventQueue.invokeLater(t);
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
                final PFeature pfeature = (PFeature)mc.getPFeatureHM().get(feature);
                final Geometry geometry = pfeature.getFeature().getGeometry();
                if ((geometry instanceof Polygon) || (geometry instanceof LineString)
                            || (geometry instanceof MultiPolygon)) {
                    for (int i = 0; i < (pfeature.getXp().length - 1); i++) {
                        final Point2D tmpStart = new Point2D.Double(pfeature.getXp()[i], pfeature.getYp()[i]);
                        final Point2D tmpEnd = new Point2D.Double(pfeature.getXp()[i + 1], pfeature.getYp()[i + 1]);
                        final double tmpDist = StaticGeometryFunctions.distanceToLine(tmpStart, tmpEnd, trigger);
                        if (tmpDist < dist) {
                            dist = tmpDist;
                            start = tmpStart;
                            end = tmpEnd;
                            this.pf = pfeature;
                            this.positionInArray = i + 1;
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
                    new Class[] { PFeature.class, PHandle.class });
            try {
                final PNode n = (PNode)o;
                setMouseCursorAccordingToMode(n);
                if (n instanceof Highlightable) {
                    if (highlighted != null) {
                        highlighted.setHighlighting(false);
                    }
                    ((Highlightable)n).setHighlighting(true);
                    highlighted = (Highlightable)n;
                } else {
                    if (highlighted != null) {
                        highlighted.setHighlighting(false);
                    }
                }
            } catch (Exception e) {
                log.warn("Fehler beim Highlighten", e); // NOI18N
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
            c = mc.getCursor(mc.getHandleInteractionMode());
        } else {
            c = mc.getCursor(mc.getInteractionMode());
        }
        if ((c != null) && (mc.getCursor() != c)) {
            mc.setCursor(c);
        }
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        try {
            if ((event.getClickCount() == 2) && mc.getInteractionMode().equals(MappingComponent.SELECT)
                        && mc.getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)
                        && ((pf != null) && pf.isSelected())) {
                // Selektiertes Feature holen
                final Collection sel = ((DefaultFeatureCollection)mc.getFeatureCollection()).getSelectedFeatures();

                // markiertes Handel auf der Linie holen
                final Point2D newPoint = new Point2D.Float(handleX, handleY);
                final Point2D[] neighbours = getNearestNeighbours(newPoint, sel);

                final Point2D p0 = neighbours[0];
                final Point2D p1 = neighbours[1];
                if ((p0 != null) && (p1 != null)) {
                    // CTRL-Taste beim Klicken gedrückt
                    if (event.getModifiers() == (InputEvent.CTRL_MASK + InputEvent.BUTTON1_MASK)) {
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
                        } else {
                            // Nachbar "0" und "1" liegen genau übereinander
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
                        if (log.isDebugEnabled()) {
                            log.debug("distanceLeft: " + distanceLeft); // NOI18N
                        }

                        // Gesamt-Abstand berechnen
                        final Double distanceTotal = leftNeighbour.distance(rightNeighbour);
                        if (log.isDebugEnabled()) {
                            log.debug("distanceTotal: " + distanceTotal); // NOI18N
                        }

                        // MainFrame holen
                        final Frame frame = StaticSwingTools.getParentFrame(mc);

                        // Dialog modal aufrufen und Werte übergeben
                        final AddHandleDialog dialog = new AddHandleDialog(frame, true, distanceTotal);
                        dialog.setDistanceToLeft(distanceLeft);

                        // Dialog zentrieren und sichtbar machen
                        dialog.setLocationRelativeTo(frame);
                        dialog.setVisible(true);

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
// =====================================================================================
                if (mc.isSnappingEnabled()) { // Snapping Modus
                    // Features suchen bei denen der zukünftige neue
                    // Punkt auf einer identischen Linie sitzt

                    // Alle Objekte durchlaufen
                    for (final Feature feature : mc.getFeatureCollection().getAllFeatures()) {
                        // Collection erzeugen (wird von getNearestNeighbours erwartet)
                        final LinkedList<Feature> featureCollection = new LinkedList<Feature>();
                        // und aktuelles Feature hinzufügen
                        featureCollection.add(feature);

                        // die 2 Nachbarpunkte ermitteln, dessen Linie dem zukünftigen neuen Punkt am Nächsten ist
                        final Point2D[] tmpneighbours = getNearestNeighbours(new Point2D.Float(handleX, handleY),
                                featureCollection);

                        // sind die 2 Nachbarpunkte identisch mit den 2 Nachbarn des neuen Punktes => identische Linie
                        final Point2D t0 = tmpneighbours[0];
                        final Point2D t1 = tmpneighbours[1];
                        if ((t0 != null) && (t1 != null)) {
                            if ((t0.equals(neighbours[0]) && t1.equals(neighbours[1]))
                                        || (t0.equals(neighbours[1])
                                            && t1.equals(neighbours[0]))) {
                                // Punkt dem jeweiligen Feature hinzufügen
                                addPoint((PFeature)mc.getPFeatureHM().get(feature), handleX, handleY);
                            }
                        }
                    }
                } else { // kein Snapping Modus
                    // einfach nur den Punkt hinzufügen (wie vorher auch)
                    addPoint(pf, handleX, handleY);
                }
// =====================================================================================
                mc.getMemRedo().clear();
                resetAfterClick();
            }
        } catch (Exception ex) {
            log.error("Fehler beim Anlegen von neuer Koordinate und Handle", ex); // NOI18N
        }
        super.mouseClicked(event);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pf       DOCUMENT ME!
     * @param  handleX  DOCUMENT ME!
     * @param  handleY  DOCUMENT ME!
     */
    private void addPoint(final PFeature pf, final float handleX, final float handleY) {
        log.info("neues Handle einf\u00FCgen: Anzahl vorher:" + pf.getCoordArr().length); // NOI18N
        pf.setXp(pf.insertCoordinate(positionInArray, pf.getXp(), handleX));
        if (log.isDebugEnabled()) {
            log.debug("Pos=" + positionInArray + ", HandleX=" + handleX);                 // NOI18N
        }
        for (final float f : pf.getXp()) {
            if (log.isDebugEnabled()) {
                log.debug("X=" + f);                                                      // NOI18N
            }
        }
        pf.setYp(pf.insertCoordinate(positionInArray, pf.getYp(), handleY));
        if (log.isDebugEnabled()) {
            log.debug("Pos=" + positionInArray + ", HandleY=" + handleY);                 // NOI18N
        }
        for (final float f : pf.getYp()) {
            if (log.isDebugEnabled()) {
                log.debug("Y=" + f);                                                      // NOI18N
            }
        }
        final Coordinate c = new Coordinate(mc.getWtst().getSourceX(handleX), mc.getWtst().getSourceY(handleY));
        pf.setCoordArr(pf.insertCoordinate(positionInArray, pf.getCoordArr(), c));
        pf.syncGeometry();
        log.info("neues Handle einf\u00FCge: Anzahl nachher:" + pf.getCoordArr().length); // NOI18N
        pf.setPathToPolyline(pf.getXp(), pf.getYp());
        final Vector v = new Vector();
        v.add(pf.getFeature());
        ((DefaultFeatureCollection)mc.getFeatureCollection()).fireFeaturesChanged(v);
        mc.getMemUndo().addAction(new HandleDeleteAction(mc, pf.getFeature(), positionInArray, c, handleX, handleY));
    }

    /**
     * Setzt den Listener nach einem erfolgreichen Einf\u00FCgen eines neuen Punkts wieder auf den Anfangszustand
     * zur\u00FCck.
     */
    private void resetAfterClick() {
//        ((DefaultFeatureCollection) mc.getFeatureCollection()).unselectAll();
        newPointHandle = null;
        handleX = Float.MIN_VALUE;
        handleY = Float.MIN_VALUE;
        positionInArray = 0;
        pf = null;
    }

    @Override
    public void mouseDragged(final PInputEvent event) {
        super.mouseDragged(event);
        refreshPointerAnnotation(event);
    }

    @Override
    public void mouseEntered(final PInputEvent event) {
        super.mouseEntered(event);
        pointerAnnotation.setVisible(true && annotationNodeVisible);
    }

    @Override
    public void mouseExited(final PInputEvent event) {
        super.mouseExited(event);
        pointerAnnotation.setVisible(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void refreshPointerAnnotation(final PInputEvent event) {
        if (annotationNodeVisible) {
            pointerAnnotation.setVisible(true);
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
        return mc.getCurrentOGCScale();
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
        if (this.pointerAnnotation != null) {
            mc.getCamera().removeChild(this.pointerAnnotation);
        }
        this.pointerAnnotation = pointerAnnotation;
        mc.getCamera().addChild(pointerAnnotation);
        pointerAnnotation.setVisible(false);
    }
}
