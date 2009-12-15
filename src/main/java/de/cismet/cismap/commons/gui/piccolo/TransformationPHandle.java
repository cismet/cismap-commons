/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleAddAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleDeleteAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleMoveAction;
import de.cismet.cismap.commons.tools.PFeatureTools;
import de.cismet.math.geometry.StaticGeometryFunctions;
import de.cismet.tools.StaticDecimalTools;
import de.cismet.tools.collections.MultiMap;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author jruiz
 */
public class TransformationPHandle extends PHandle {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PText leftInfo;
    private PText rightInfo;
    private MultiMap glueCoordinates = new MultiMap();
    private Point2D vetoPoint = null;
    private PFeature pfeature;
    private int positionInArray;
    private float startX, startY, endX, endY;
    
    public TransformationPHandle(final PFeature pfeature, final int positionInArray) {
        super(new PLocator() {

            @Override
            public double locateX() {
                try {
                    return pfeature.getXp()[positionInArray];
                } catch (Exception ex) {
                    return -1;
                }
            }

            @Override
            public double locateY() {
                try {
                    return pfeature.getYp()[positionInArray];
                } catch (Exception ex) {
                    return -1;
                }
            }
        }, pfeature.getViewer());
        
        this.pfeature = pfeature;
        this.positionInArray = positionInArray;
    }

    private int getLeftNeighbourIndex(int index) {
        if (index == 0) {
            return pfeature.getXp().length - 2;
        } else if (positionInArray == pfeature.getXp().length - 1) {
            return positionInArray - 1;
        } else {
            return index - 1;
        }
    }

    private int getRightNeighbourIndex(int index) {
        if (index == 0) {
            return positionInArray + 1;
        } else if (positionInArray == pfeature.getXp().length - 1) {
            return 1;
        } else {
            return positionInArray + 1;
        }
    }

    @Override
    public void dragHandle(PDimension aLocalDimension, PInputEvent pInputEvent) {
        if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
            SimpleMoveListener moveListener = (SimpleMoveListener) pfeature.getViewer().getInputListener(MappingComponent.MOTION);
            if (moveListener != null) {
                moveListener.mouseMoved(pInputEvent);
            } else {
                log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
            }

            if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE) ||
                    pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE)) {
                //localToParent(aLocalDimension);
//                     if (viewer.isFeatureDebugging()) log.debug("vorher.DRAG.aLocalDimension:"+aLocalDimension);
                pfeature.getViewer().getCamera().localToView(aLocalDimension);
//                     if (viewer.isFeatureDebugging()) log.debug("nachher.DRAG.aLocalDimension:"+aLocalDimension);

                float newX = pfeature.getXp()[positionInArray] + (float) aLocalDimension.getWidth();
                float newY = pfeature.getYp()[positionInArray] + (float) aLocalDimension.getHeight();

                // if CTRL DOWN
                if (pInputEvent.isLeftMouseButton() && pInputEvent.isControlDown()) {
                    Point2D trigger = pInputEvent.getCanvasPosition();
                    trigger = pfeature.getViewer().getCamera().localToView(trigger);
                    Point2D lineStart = new Point2D.Double();
                    Point2D lineEnd = new Point2D.Double();

                    int lineStartIndex = getLeftNeighbourIndex(positionInArray);
                    int lineEndIndex = getRightNeighbourIndex(positionInArray);

//                        if (positionInArray==0) {
//                            lineStartIndex=getXp().length-2;
//                            lineEndIndex=positionInArray+1;
//                        } else if (positionInArray==getXp().length-1){
//                            lineStartIndex=positionInArray-1;
//                            lineEndIndex=1;
//                        } else {
//                            lineStartIndex=positionInArray-1;
//                            lineEndIndex=positionInArray+1;
//                        }

                    lineStart.setLocation(pfeature.getXp()[lineStartIndex], pfeature.getYp()[lineStartIndex]);
                    lineEnd.setLocation(pfeature.getXp()[lineEndIndex], pfeature.getYp()[lineEndIndex]);
                    Point2D erg = StaticGeometryFunctions.createPointOnLine(lineStart, lineEnd, trigger);
                    newX = (float) erg.getX();
                    newY = (float) erg.getY();
                }

                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("Width,Height:" + (float) aLocalDimension.getWidth() + "," + (float) aLocalDimension.getHeight());
                }
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("Width,Height:" + aLocalDimension.getWidth() + "," + aLocalDimension.getHeight());
                }
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("alter.Wert:" + pfeature.getXp()[positionInArray] + "," + pfeature.getYp()[positionInArray]);
                }
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("neuer.Wert:" + newX + "," + newY);
                }
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("Berechnung:" + (float) ((double) pfeature.getXp()[positionInArray] * 10000 + (double) aLocalDimension.getWidth() * 10000) / 10000);
                }
                if (pfeature.getViewer().isSnappingEnabled()) {
//                        Point2D snapPoint=PFeatureTools.getNearestPointInArea(viewer,aEvent.getCanvasPosition(),
//                                new Point2D.Float(getXp()[positionInArray],getYp()[positionInArray]) );
                    Point2D snapPoint = PFeatureTools.getNearestPointInArea(pfeature.getViewer(), pInputEvent.getCanvasPosition());
                    if (snapPoint != null) {
                        newX = (float) snapPoint.getX();
                        newY = (float) snapPoint.getY();
                    }
                }

                // umgekehrte Bewegung fuer Undo speichern
                endX = newX;
                endY = newY;

                updateGeometryPoints(positionInArray, newX, newY);

                //pfeature.syncGeometry();
                relocateHandle();

                if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                    Set<PFeature> pFeatureSet = glueCoordinates.keySet();
                    for (PFeature gluePFeature : pFeatureSet) {
                        if (gluePFeature.getFeature().isEditable()) {
                            Collection coordinates = (Collection) glueCoordinates.get(gluePFeature);
                            if (coordinates != null) {
                                for (Object o : coordinates) {
                                    int oIndex = (Integer) o;
                                    gluePFeature.moveCoordinateToNewPiccoloPosition(oIndex, newX, newY);
                                    //   gluePFeature.syncGeometry();
                                    if (pfeature.getViewer().isFeatureDebugging()) {
                                        log.debug("moveCoordinateToNewPiccoloPosition " + gluePFeature);
                                    }
                                }
                            }
                        }
                    }
//                        if (viewer.isFeatureDebugging()) log.debug("glueIdenticalPoints==true");
//                        List<PFeatureCoordinatePosition> l =pfeature.getViewer().getPFeaturesByCoordinates(oldCoordinate);
//                        if (l!=null) {
//                            if (viewer.isFeatureDebugging()) log.debug("l.size():"+l.size())   ;
//                            for (PFeatureCoordinatePosition pc:l){
//
//                                if (pc.getPFeature()!=PFeature.this) {
//                                    if (viewer.isFeatureDebugging()) log.debug("GLUE");
//                                    //set the x and y value separately, because we don't want to create a flat copy and we don't want to clone
//                                    pc.getPFeature().getXp()[pc.getPosition()]=getXp()[positionInArray];
//                                    pc.getPFeature().getYp()[pc.getPosition()]=getYp()[positionInArray];
//                                    pc.getPFeature().getCoordArr()[pc.getPosition()].x=pfeature.getCoordArr()[positionInArray].x;
//                                    pc.getPFeature().getCoordArr()[pc.getPosition()].y=pfeature.getCoordArr()[positionInArray].y;
//                                    pc.getPFeature().setPathToPolyline(pc.getPFeature().getXp(), pc.getPFeature().getYp());
//                                    pc.getPFeature().syncGeometry();
//                                    pc.getPFeature().doGeometry(pc.getPFeature().getFeature().getGeometry());
//                                    pc.getPFeature().getViewer().reconsiderFeature(pc.getPFeature().getFeature());
//                                } else {
//                                    if (viewer.isFeatureDebugging()) log.debug("Same Object no GLUE");
//                                }
//                            }
//                        }
                }

                //Abst\u00E4nde zu den Nachbarn
                int lefty = getLeftNeighbourIndex(positionInArray);
                int righty = getRightNeighbourIndex(positionInArray);
                Coordinate lc = pfeature.getCoordArr()[lefty];
                Coordinate rc = pfeature.getCoordArr()[righty];
                Coordinate thisC = pfeature.getCoordArr()[positionInArray];

                double leftDistance = thisC.distance(lc);
                double rightDistance = thisC.distance(rc);

                leftInfo.setText("" + StaticDecimalTools.round(leftDistance));
                rightInfo.setText("" + StaticDecimalTools.round(rightDistance));

                Point2D lp = new Point2D.Double();
                Point2D rp = new Point2D.Double();
                lp.setLocation(pfeature.getXp()[lefty], pfeature.getYp()[lefty]);
                rp.setLocation(pfeature.getXp()[righty], pfeature.getYp()[righty]);
                lp = pfeature.getViewer().getCamera().viewToLocal(lp);
                rp = pfeature.getViewer().getCamera().viewToLocal(rp);
                leftInfo.setX(lp.getX());
                leftInfo.setY(lp.getY());
                rightInfo.setX(rp.getX());
                rightInfo.setY(rp.getY());
            }
        }
    }

    /**
     * Override this method to get notified when the handle starts to get dragged.
     */
    @Override
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
        if (
                ((PureNewFeature)pfeature.getFeature()).getGeometryType() == PureNewFeature.geomTypes.RECTANGLE ||
                ((PureNewFeature)pfeature.getFeature()).getGeometryType() == PureNewFeature.geomTypes.ELLIPSE
        ) {
            Collection selArr = pfeature.getViewer().getFeatureCollection().getSelectedFeatures();
            for (Object o : selArr) {
                PFeature pf = (PFeature) (pfeature.getViewer().getPFeatureHM().get(o));
                if (pf != null && pf.getInfoNode() != null) {
                    pf.getInfoNode().setVisible(false);
                }
            }

            pfeature.getViewer().getHandleLayer().removeAllChildren();
            pfeature.getViewer().getHandleLayer().addChild(this);
            /*if (pivotHandle != null) {
                pfeature.getViewer().getHandleLayer().addChild(pivotHandle);
            }*/

            super.startHandleDrag(aLocalPoint, aEvent);

        } else {
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
                //Infonodes (Entfernung) anlegen
                leftInfo = new PText();
                leftInfo.setPaint(new Color(255, 255, 255, 100));
                rightInfo = new PText();
                rightInfo.setPaint(new Color(255, 255, 255, 100));
                addChild(leftInfo);
                addChild(rightInfo);
                leftInfo.setVisible(true);
                rightInfo.setVisible(true);

                //Glue: IdentischePunkte mitverschieben
                if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                    //Features suchen die identische Punkte haben
                    glueCoordinates = pfeature.checkforGlueCoords(positionInArray);
                    log.info("checkforGlueCoords() aufgerufen und " + glueCoordinates.keySet().size() + " gefunden");
                }
                vetoPoint = aLocalPoint;
                startX = pfeature.getXp()[positionInArray];
                startY = pfeature.getYp()[positionInArray];
                endX = startX;
                endY = startY;

                super.startHandleDrag(aLocalPoint, aEvent);
            }
        }
    }

    @Override
    public void endHandleDrag(java.awt.geom.Point2D aLocalPoint, PInputEvent aEvent) {
        if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
            if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
                pfeature.syncGeometry();
                Vector v = new Vector();
                v.add(pfeature.getFeature());
                ((DefaultFeatureCollection) pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
            } else {
                pfeature.getViewer().getFeatureCollection().reconsiderFeature(pfeature.getFeature());
            }

            removeChild(leftInfo);
            removeChild(rightInfo);
            leftInfo = null;
            rightInfo = null;

            if ((pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE)) &&
                    (Math.abs(startX - endX) > 0.001d) || (Math.abs(startY - endY) > 0.001d)) {
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("neue MoveAction erzeugen");
                }
                boolean isGluedAction = false;
                if (glueCoordinates.size() != 0) {
                    isGluedAction = true;
                    Vector v = new Vector();
                    if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                        Set<PFeature> pFeatureSet = glueCoordinates.keySet();
                        for (PFeature gluePFeature : pFeatureSet) {
                            if (gluePFeature.getFeature().isEditable()) {
                                v.add(gluePFeature.getFeature());
                                Collection coordinates = (Collection) glueCoordinates.get(gluePFeature);
                                if (coordinates != null) {
                                    for (Object o : coordinates) {
//                                                int oIndex = (Integer) o;
//                                                gluePFeature.moveCoordinateToNewPiccoloPosition(oIndex, newX, newY);
//                                                gluePFeature.syncGeometry();
                                        if (pfeature.getViewer().isFeatureDebugging()) {
                                            log.debug("PFeature synced:" + gluePFeature);
                                        }
                                    }
                                }
                            }
                        }
                        ((DefaultFeatureCollection) pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
                    }
                }
                pfeature.getViewer().getMemUndo().addAction(new HandleMoveAction(positionInArray, pfeature, startX, startY, endX, endY, isGluedAction));
                pfeature.getViewer().getMemRedo().clear();
            }
        }
        super.endHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void handleClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pfeature.getViewer().isFeatureDebugging()) {
            log.debug("Handle clicked");
        }
        if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.REMOVE_HANDLE)) {
            pfeature.getViewer().getMemUndo().addAction(new HandleAddAction(pfeature.getViewer(), pfeature.getFeature(), positionInArray, pfeature.getCoordArr()[positionInArray], pfeature.getXp()[positionInArray], pfeature.getYp()[positionInArray]));
            ((PHandle) (pInputEvent.getPickedNode())).removeHandle();
        } else if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)) {
            pfeature.getViewer().getMemUndo().addAction(new HandleDeleteAction(pfeature.getViewer(), pfeature.getFeature(), positionInArray, pfeature.getCoordArr()[positionInArray], pfeature.getXp()[positionInArray], pfeature.getYp()[positionInArray]));
            ((PHandle) (pInputEvent.getPickedNode())).duplicateHandle();
        } else if (pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
            pfeature.addSplitHandle(((PHandle) (pInputEvent.getPickedNode())));
        }
        if (pfeature.getViewer().isFeatureDebugging()) {
            log.debug("Ende von handleClicked() getFeature().getGeometry().getCoordinates().length:" + pfeature.getFeature().getGeometry().getCoordinates().length);
        }
        //viewer.getFeatureCollection().reconsiderFeature(getFeature());
    }

    @Override
    public void mouseMovedNotInDragOperation(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        SimpleMoveListener moveListener = (SimpleMoveListener) pfeature.getViewer().getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
    }

    @Override
    public void removeHandle() {
        //log.error("--- :"+getCoordArr());
        if (pfeature.getXp().length > 4 && pfeature.getFeature().getGeometry() instanceof Polygon ||
                pfeature.getXp().length > 1 && pfeature.getFeature().getGeometry() instanceof LineString) { //DANGER und Linien ???
            pfeature.setXp(pfeature.removeCoordinateFromOutside(positionInArray, pfeature.getXp()));
            pfeature.setYp(pfeature.removeCoordinateFromOutside(positionInArray, pfeature.getYp()));
            log.debug("---vorher:" + pfeature.getCoordArr().length);
            pfeature.setCoordArr(pfeature.removeCoordinateFromOutside(positionInArray, pfeature.getCoordArr()));
            pfeature.syncGeometry();
            log.debug("---nachher:" + pfeature.getCoordArr().length);
            pfeature.setPathToPolyline(pfeature.getXp(), pfeature.getYp());
            //Jetzt sind allerdings alle Locator noch falsch und das handle existiert noch
            //handleLayer.removeChild(this);
            //Das w\u00E4re zwar optimal (Performance) korrigiert allerdings nicht die falschen
            //Locator
            Vector v = new Vector();
            v.add(pfeature.getFeature());
            ((DefaultFeatureCollection) pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
            //deswegen (langsam aber funzt):
            if (isSelected()) {
                pfeature.getViewer().showHandles(false);
            }
        }
    }

    @Override
    public void duplicateHandle() {
        log.info("duplicate Handle : anzahl der Handles vorher:" + pfeature.getCoordArr().length);
        pfeature.setXp(duplicateCoordinate(positionInArray, pfeature.getXp()));
        pfeature.setYp(duplicateCoordinate(positionInArray, pfeature.getYp()));
        pfeature.setCoordArr(duplicateCoordinate(positionInArray, pfeature.getCoordArr()));
        pfeature.syncGeometry();
        log.info("duplicate Handle : anzahl der Handles nachher:" + pfeature.getCoordArr().length);
        pfeature.setPathToPolyline(pfeature.getXp(), pfeature.getYp());
        pfeature.getViewer().showHandles(false);
        Vector v = new Vector();
        v.add(pfeature.getFeature());
        ((DefaultFeatureCollection) pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
    }

    /**
     * Dupliziert eine Koordinate.
     * @param position Position der zu duplizierenden Koordinate
     * @param original Koordinatenarray der Geometrie
     * @return Koordinatenarray mit dupliziertem Punkt
     */
    private Coordinate[] duplicateCoordinate(int position, Coordinate[] original) {
        if (pfeature.getFeature().getGeometry() instanceof Polygon && original != null && original.length - 1 > position ||
                pfeature.getFeature().getGeometry() instanceof LineString && original != null && original.length > position && original.length > 2) {
            Coordinate[] newCoordinates = new Coordinate[original.length + 1];
            //vorher
            for (int i = 0; i <= position; ++i) {
                newCoordinates[i] = original[i];
            }

            //zu entferndes Element duplizieren, hier muss geklont werden
            newCoordinates[position + 1] = (Coordinate) (original[position].clone());

            //nachher
            for (int i = position + 1; i < original.length; ++i) {
                newCoordinates[i + 1] = original[i];
            }

            //Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist
            if (position == 0 && pfeature.getFeature().getGeometry() instanceof Polygon) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            }
            pfeature.syncGeometry();
            return newCoordinates;
        } else {
            if (original != null) {
                return original;
            } else {
                return null;
            }
        }
    }

    /**
     * Dupliziert einen Punkt.
     * @param position Position des zu duplizierenden Punkts
     * @param original PCanvas-Punktarray der Geometrie
     * @return PCanvas-Punktarray, mit dupliziertem Punkt
     */
    private float[] duplicateCoordinate(int position, float[] original) {
        if (pfeature.getFeature().getGeometry() instanceof Polygon && original != null && original.length - 1 > position ||
                pfeature.getFeature().getGeometry() instanceof LineString && original != null && original.length > position && original.length > 2) {
            float[] newCoordinates = new float[original.length + 1];
            //vorher
            for (int i = 0; i <= position; ++i) {
                newCoordinates[i] = original[i];
            }

            //nachher
            for (int i = position; i < original.length; ++i) {
                newCoordinates[i + 1] = original[i];
            }

            //Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist
            if (position == 0 && pfeature.getFeature().getGeometry() instanceof Polygon) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            }
            return newCoordinates;
        } else {
            if (original != null) {
                return original;
            } else {
                return null;
            }
        }
    }

    public void updateGeometryPoints(int positionInArray, float newX, float newY) {
        switch (((PureNewFeature)pfeature.getFeature()).getGeometryType()) {
            case RECTANGLE:
                // letzter Punkt ist gleich erster Punkt. Wir arbeiten lieber mit dem ersten
                if (positionInArray == 4) {
                    positionInArray = 0;
                }

                int posOpposed = (positionInArray + 2) % 4;
                int posPrevious = (positionInArray + 3) % 4;
                int posNext = (positionInArray + 1) % 4;

//                Point2D currentPoint = new Point2D.Double(pfeature.getXp()[positionInArray], pfeature.getYp()[positionInArray]);
                Point2D previousPoint = new Point2D.Double(pfeature.getXp()[posPrevious], pfeature.getYp()[posPrevious]);
                Point2D nextPoint = new Point2D.Double(pfeature.getXp()[posNext], pfeature.getYp()[posNext]);
//                Point2D opposedPoint = new Point2D.Double(pfeature.getXp()[posOpposed], pfeature.getYp()[posOpposed]);

//                double distancePrevious = StaticGeometryFunctions.distanceToLine(opposedPoint, previousPoint, currentPoint);
//                double distanceNext = StaticGeometryFunctions.distanceToLine(opposedPoint, nextPoint, currentPoint);
//
//                double currentAngle = Math.toRadians(Math.toDegrees(angle) - (positionInArray * 90) % 360);
//                double previousAngle = Math.toRadians(180 - Math.toDegrees(currentAngle));
//                double nextAngle = Math.toRadians(Math.toDegrees(previousAngle) + 90);
//
//                Point2D.Double pN = new Point2D.Double();
//                pN.x = opposedPoint.getX() + distancePrevious * Math.cos(nextAngle);
//                pN.y = opposedPoint.getY() + distancePrevious * Math.sin(nextAngle);
//
//                Point2D.Double pP = new Point2D.Double();
//                pP.x = opposedPoint.getX() + distanceNext * Math.cos(previousAngle);
//                pP.y = opposedPoint.getY() + distanceNext * Math.sin(previousAngle);
//
//                log.debug("angle: " + Math.toDegrees(currentAngle));
//                log.debug("distance Next: " + distanceNext);
//                log.debug("distance Previous: " + distancePrevious);


                // selektierten punkt verschieben
                pfeature.moveCoordinateToNewPiccoloPosition(positionInArray, newX, newY);
//                pfeature.moveCoordinateToNewPiccoloPosition(posPrevious, (float)pP.getX(), (float)pP.getY());
//                pfeature.moveCoordinateToNewPiccoloPosition(posNext, (float)pN.getX(), (float)pN.getY());
                if (positionInArray % 2 == 0) {
                    pfeature.moveCoordinateToNewPiccoloPosition(posPrevious, newX, (float)previousPoint.getY());
                    pfeature.moveCoordinateToNewPiccoloPosition(posNext, (float)nextPoint.getX(), newY);
                    pfeature.moveCoordinateToNewPiccoloPosition(posOpposed, (float)nextPoint.getX(), (float)previousPoint.getY());
                } else {
                    pfeature.moveCoordinateToNewPiccoloPosition(posPrevious, (float)previousPoint.getX(),  newY);
                    pfeature.moveCoordinateToNewPiccoloPosition(posNext, newX, (float)nextPoint.getY());
                    pfeature.moveCoordinateToNewPiccoloPosition(posOpposed, (float)previousPoint.getX(), (float)nextPoint.getY());
                }

                // letzter Punkt ist gleich erster Punkt
                pfeature.getXp()[4] = pfeature.getXp()[0];
                pfeature.getYp()[4] = pfeature.getYp()[0];

                // Pfad auf Punkte setzen
                pfeature.setPathToPolyline(pfeature.getXp(), pfeature.getYp());
                break;
            case ELLIPSE:
                // wird vom EllipseHandle transformiert
                break;
            case LINESTRING:
                pfeature.moveCoordinateToNewPiccoloPosition(positionInArray, newX, newY);
                break;
            case POLYGON:
                pfeature.moveCoordinateToNewPiccoloPosition(positionInArray, newX, newY);
                break;
            case POINT:
                pfeature.moveCoordinateToNewPiccoloPosition(positionInArray, newX, newY);
                break;
        }
//        pfeature.getCoordArr()[positionInArray].x = wtst.getSourceX(pfeature.getXp()[positionInArray] - x_offset);
//        pfeature.getCoordArr()[positionInArray].y = wtst.getSourceY(pfeature.getYp()[positionInArray] - y_offset);
//        if (positionInArray == 0 && pfeature.getFeature().getGeometry() instanceof Polygon) {
//            pfeature.getXp()[pfeature.getXp().length - 1] = pfeature.getXp()[0];
//            pfeature.getYp()[pfeature.getYp().length - 1] = pfeature.getYp()[0];
//            //Originalgeometrie ver\u00E4ndern
//            // hin :wtst.getDestX(coordArr[i].x)+x_offset)
//            pfeature.getCoordArr()[pfeature.getXp().length - 1].x = wtst.getSourceX(pfeature.getXp()[positionInArray] - x_offset);
//            pfeature.getCoordArr()[pfeature.getXp().length - 1].y = wtst.getSourceY(pfeature.getYp()[positionInArray] - y_offset);
//        }
    }

}
