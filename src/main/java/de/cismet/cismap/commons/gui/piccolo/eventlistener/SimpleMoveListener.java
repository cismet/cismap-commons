/*
 * SimpleMoveListener.java
 *
 * Created on 10. M\u00E4rz 2005, 10:10
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.Highlightable;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleDeleteAction;
import de.cismet.cismap.commons.tools.PFeatureTools;
import de.cismet.math.geometry.StaticGeometryFunctions;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;

/**
 *
 * @author hell/nh
 */
public class SimpleMoveListener extends PBasicInputEventHandler {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String COORDINATES_CHANGED = "COORDINATES_CHANGED";
    Highlightable highlighted = null;
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
    Object handleHighlightingStuff = new Object();

    /**
     * Creates a new instance of SimpleMoveListener
     */
    public SimpleMoveListener(MappingComponent mc) {
        super();
        this.mc = mc;
        mc.getCamera().addChild(pointerAnnotation);
        mc.getCamera().addChild(snapRect);
        snapRect.setStroke(null);
        snapRect.setTransparency(0.2f);
        snapRect.setVisible(false);
        pointerAnnotation.setVisible(false);
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        Thread t = new Thread() {
            
            @Override
            public void run() {
                try {
                    if (mc.getInteractionMode().equals(MappingComponent.SELECT) && 
                            mc.getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)) {
                        if (mc.getFeatureCollection() instanceof DefaultFeatureCollection &&
                                ((DefaultFeatureCollection) mc.getFeatureCollection()).getSelectedFeatures().size() == 1) {
                            if (l == null || newPointHandle == null) {
                                log.debug("newPointHandle und Locator erstellen");
                                l = new PLocator() {
                                    public double locateX() {return handleX;}
                                    public double locateY() {return handleY;}
                                };
                                newPointHandle = new PHandle(l, mc) {
                                    @Override
                                    public void handleClicked(PInputEvent e) {
                                        SimpleMoveListener.this.mouseClicked(e);
    //                                    super.handleClicked(pInputEvent);
                                    }
                                };
                                newPointHandle.setPaint(new Color(255, 0, 0, 150));
                            }
                            Collection sel = ((DefaultFeatureCollection) mc.getFeatureCollection()).getSelectedFeatures();
                            Point2D trigger = event.getPosition();
                            Point2D[] neighbours = getNearestNeighbours(trigger, sel);

                            Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);
                            handleX = (float) erg.getX();
                            handleY = (float) erg.getY();
                            boolean found = false;
                            for (Object o : mc.getHandleLayer().getChildrenReference()) {
                                if (o instanceof PHandle && (PHandle) o == newPointHandle) {
                                    found = true;
                                }
                            }
                            if (!found) {
                                EventQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        mc.getHandleLayer().addChild(newPointHandle);
                                        log.info("tempor\u00E4res Handle eingef\u00FCgt");
                                    }
                                });
                            }
                            newPointHandle.relocateHandle();
                        }
                    }
                    xCoord = mc.getWtst().getSourceX(event.getPosition().getX() - mc.getClip_offset_x());
                    yCoord = mc.getWtst().getSourceY(event.getPosition().getY() - mc.getClip_offset_y());

                    refreshPointerAnnotation(event);

                    underlyingObject = null;
                    Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[]{PFeature.class});
                    if (o instanceof PFeature) {
                        underlyingObject = (PFeature) o;
                    }
                    postCoordinateChanged();
                    try {
                    mc.getSnapHandleLayer().removeAllChildren();
                    }
                    catch (Exception e){
                        log.debug("Fehler beim entfernen der SnappingVisualisierung",e);
                    }

                    if (mc.isVisualizeSnappingEnabled()) {
                        Point2D nearestPoint = PFeatureTools.getNearestPointInArea(mc, event.getCanvasPosition());
                        if (nearestPoint != null) {
                            mc.getCamera().viewToLocal(nearestPoint);
                            PPath show = PPath.createEllipse((float) (nearestPoint.getX() - 3), (float) (nearestPoint.getY() - 3), (float) (6), (float) (6));
                            show.setPaint(new Color(0, 0, 0));
                            mc.getSnapHandleLayer().addChild(show);
                        }
                        if (mc.isVisualizeSnappingRectEnabled()) {
                            snapRect.setVisible(true);
                            snapRect.setPathToRectangle((int) event.getCanvasPosition().getX() - mc.getSnappingRectSize() / 2,
                                    (int) event.getCanvasPosition().getY() - mc.getSnappingRectSize() / 2,
                                    mc.getSnappingRectSize(), mc.getSnappingRectSize());
                        } else {
                            snapRect.setVisible(false);
                        }
                    }
                } catch (Exception e) {
                    log.info("Fehler beim Moven \u00FCber die Karte ", e);
                }
                handleHighlightingStuff(event);
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * Sucht in der \u00FCbergebenen PFeature-Collection nach den zwei der Mausposition
     * am n\u00E4chsten gelegenen Eckpunkte EINES PFeatures.
     * @param trigger Mausposition
     * @param sel Collection aller selektierter PFeatures
     * @return Point2D-Array mit den zwei gefundenen Punkten
     */
    private Point2D[] getNearestNeighbours(Point2D trigger, Collection sel) {
        Point2D start = null;
        Point2D end = null;
        double dist = Double.POSITIVE_INFINITY;
        for (Object o : sel) {
            if (o instanceof Feature) {
                PFeature pfeature = (PFeature) mc.getPFeatureHM().get(o);
                if (pfeature.getFeature().getGeometry() instanceof Polygon ||
                        pfeature.getFeature().getGeometry() instanceof LineString) {
                    for (int i = 0; i < pfeature.getXp().length-1; i++) {
                        Point2D tmpStart = new Point2D.Double(pfeature.getXp()[i], pfeature.getYp()[i]);
                        Point2D tmpEnd = new Point2D.Double(pfeature.getXp()[i+1], pfeature.getYp()[i+1]);
                        double tmpDist = StaticGeometryFunctions.distanceToLine(tmpStart, tmpEnd, trigger);
                        if (tmpDist < dist) {
                            dist = tmpDist;
                            start = tmpStart;
                            end = tmpEnd;
                            this.pf = pfeature;
                            this.positionInArray = i+1;
                        }
                    }
                }
            }
        }
        Point2D[] erg = {start, end};
        return erg;
    }
    
    private void handleHighlightingStuff(final PInputEvent event) {
        synchronized (handleHighlightingStuff) {
            Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[]{PFeature.class, PHandle.class});
            try {
                final PNode n = (PNode) o;
                setMouseCursorAccordingToMode(n);
                if (n instanceof Highlightable) {
                    if (highlighted != null) {
                        highlighted.setHighlighting(false);
                    }
                    ((Highlightable) n).setHighlighting(true);
                    highlighted = (Highlightable) n;
                } else {
                    if (highlighted != null) {
                        highlighted.setHighlighting(false);
                    }
                }
            } catch (Exception e) {
                log.warn("Fehler beim Highlighten", e);
            }
        }
    }

    private void postCoordinateChanged() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(COORDINATES_CHANGED, this);
    }

    /**
     * Falls die \u00FCbergebene PNode ein PHandle ist wir der Cursor entsprechend
     * dem in der MappingComponent gesetzen Modus gesetzt.
     * @param n PNode-Instanz
     */
    private void setMouseCursorAccordingToMode(PNode n) {
        Cursor c = null;
        if (n instanceof PHandle) {
            c = mc.getCursor(mc.getHandleInteractionMode());
        } else {
            c = mc.getCursor(mc.getInteractionMode());
        }
        if (c != null && mc.getCursor() != c) {
            mc.setCursor(c);
        }
    }

    @Override
    public void mouseClicked(PInputEvent event) {
        try {
            if (event.getClickCount() == 2
                    && mc.getInteractionMode().equals(MappingComponent.SELECT)
                    && mc.getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE) 
                    && (pf != null && pf.isSelected())) {
                log.info("neues Handle einf\u00FCgen: Anzahl vorher:" + pf.getCoordArr().length);
                pf.setXp(pf.insertCoordinate(positionInArray, pf.getXp(),handleX));
                log.debug("Pos="+positionInArray+", HandleX="+handleX);
                for (float f : pf.getXp()) {
                    log.debug("X="+f);
                }
                pf.setYp(pf.insertCoordinate(positionInArray, pf.getYp(),handleY));
                log.debug("Pos="+positionInArray+", HandleX="+handleY);
                for (float f : pf.getYp()) {
                    log.debug("Y="+f);
                }
                Coordinate c = new Coordinate(mc.getWtst().getSourceX(handleX), mc.getWtst().getSourceY(handleY));
                pf.setCoordArr(pf.insertCoordinate(positionInArray, pf.getCoordArr(), c));
                pf.syncGeometry();
                log.info("neues Handle einf\u00FCge: Anzahl nachher:" + pf.getCoordArr().length);
                pf.setPathToPolyline(pf.getXp(), pf.getYp());
                Vector v = new Vector();
                v.add(pf.getFeature());
                ((DefaultFeatureCollection) mc.getFeatureCollection()).fireFeaturesChanged(v);
                mc.getMemUndo().addAction(new HandleDeleteAction(mc, pf.getFeature(), positionInArray, c, handleX, handleY));
                mc.getMemRedo().clear();
                resetAfterClick();
            }
        } catch (Exception ex) {
            log.error("Fehler beim Anlegen von neuer Koordinate und Handle", ex);
        }
        super.mouseClicked(event);
    }
    
    /**
     * Setzt den Listener nach einem erfolgreichen Einf\u00FCgen eines neuen Punkts
     * wieder auf den Anfangszustand zur\u00FCck.
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
    public void mouseDragged(PInputEvent event) {
        super.mouseDragged(event);
        refreshPointerAnnotation(event);
    }

    @Override
    public void mouseEntered(PInputEvent event) {
        super.mouseEntered(event);
        pointerAnnotation.setVisible(true && annotationNodeVisible);
    }

    @Override
    public void mouseExited(PInputEvent event) {
        super.mouseExited(event);
        pointerAnnotation.setVisible(false);
    }

    private void refreshPointerAnnotation(PInputEvent event) {
        if (annotationNodeVisible) {
            pointerAnnotation.setVisible(true);
            pointerAnnotation.setOffset(event.getCanvasPosition().getX() + 20.0d, event.getCanvasPosition().getY() + 20.0d);
        }
    }

// <editor-fold defaultstate="collapsed" desc="Setters & Getters">
    public double getXCoord() {
        return xCoord;
    }

    public double getYCoord() {
        return yCoord;
    }

    public double getCurrentOGCScale() {
        return mc.getCurrentOGCScale();
    }

    public PFeature getUnderlyingPFeature() {
        return underlyingObject;
    }

    public boolean isAnnotationNodeVisible() {
        return annotationNodeVisible;
    }

    public void setAnnotationNodeVisible(boolean annotationNodeVisible) {
        this.annotationNodeVisible = annotationNodeVisible;
    }

    public PNode getPointerAnnotation() {
        return pointerAnnotation;
    }

    public void setPointerAnnotation(PNode pointerAnnotation) {
        if (this.pointerAnnotation != null) {
            mc.getCamera().removeChild(this.pointerAnnotation);
        }
        this.pointerAnnotation = pointerAnnotation;
        mc.getCamera().addChild(pointerAnnotation);
        pointerAnnotation.setVisible(false);
    }
// </editor-fold>
}
