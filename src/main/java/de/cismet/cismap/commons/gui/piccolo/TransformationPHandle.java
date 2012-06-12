/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class TransformationPHandle extends PHandle {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PText leftInfo;
    private PText rightInfo;
    private MultiMap glueCoordinates = new MultiMap();
    private Point2D vetoPoint = null;
    private PFeature pfeature;
    private int entityPosition;
    private int ringPosition;
    private int coordPosition;
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TransformationPHandle object.
     *
     * @param  pfeature        DOCUMENT ME!
     * @param  entityPosition  DOCUMENT ME!
     * @param  ringPosition    DOCUMENT ME!
     * @param  coordPosition   DOCUMENT ME!
     */
    public TransformationPHandle(final PFeature pfeature,
            final int entityPosition,
            final int ringPosition,
            final int coordPosition) {
        super(new PLocator() {

                @Override
                public double locateX() {
                    try {
                        return pfeature.getXp(entityPosition, ringPosition)[coordPosition];
                    } catch (Exception ex) {
                        return -1;
                    }
                }

                @Override
                public double locateY() {
                    try {
                        return pfeature.getYp(entityPosition, ringPosition)[coordPosition];
                    } catch (Exception ex) {
                        return -1;
                    }
                }
            }, pfeature.getViewer());

        this.pfeature = pfeature;
        this.entityPosition = entityPosition;
        this.ringPosition = ringPosition;
        this.coordPosition = coordPosition;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getLeftNeighbourIndex(final int index) {
        if (index == 0) {
            return pfeature.getXp(entityPosition, ringPosition).length - 2;
        } else if (coordPosition == (pfeature.getXp(entityPosition, ringPosition).length - 1)) {
            return coordPosition - 1;
        } else {
            return index - 1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getRightNeighbourIndex(final int index) {
        if (index == 0) {
            return coordPosition + 1;
        } else if (coordPosition == (pfeature.getXp(entityPosition, ringPosition).length - 1)) {
            return 1;
        } else {
            return coordPosition + 1;
        }
    }

    @Override
    public void dragHandle(final PDimension aLocalDimension, final PInputEvent pInputEvent) {
        try {
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
                final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                            .getInputListener(MappingComponent.MOTION);
                if (moveListener != null) {
                    moveListener.mouseMoved(pInputEvent);
                } else {
                    log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
                }

                if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)
                            || pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE)) {
                    // localToParent(aLocalDimension);
// if (viewer.isFeatureDebugging()) log.debug("vorher.DRAG.aLocalDimension:"+aLocalDimension);
                    pfeature.getViewer().getCamera().localToView(aLocalDimension);
//                     if (viewer.isFeatureDebugging()) log.debug("nachher.DRAG.aLocalDimension:"+aLocalDimension);

                    final float[] xp = pfeature.getXp(entityPosition, ringPosition);
                    final float[] yp = pfeature.getYp(entityPosition, ringPosition);

                    float newX = xp[coordPosition] + (float)aLocalDimension.getWidth();
                    float newY = yp[coordPosition] + (float)aLocalDimension.getHeight();

                    // if CTRL DOWN
                    if (pInputEvent.isLeftMouseButton() && pInputEvent.isControlDown()) {
                        Point2D trigger = pInputEvent.getCanvasPosition();
                        trigger = pfeature.getViewer().getCamera().localToView(trigger);
                        final Point2D lineStart = new Point2D.Double();
                        final Point2D lineEnd = new Point2D.Double();

                        final int lineStartIndex = getLeftNeighbourIndex(coordPosition);
                        final int lineEndIndex = getRightNeighbourIndex(coordPosition);

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

                        lineStart.setLocation(xp[lineStartIndex], yp[lineStartIndex]);
                        lineEnd.setLocation(xp[lineEndIndex], yp[lineEndIndex]);
                        final Point2D erg = StaticGeometryFunctions.createPointOnLine(lineStart, lineEnd, trigger);
                        newX = (float)erg.getX();
                        newY = (float)erg.getY();
                    }

                    if (pfeature.getViewer().isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Width,Height:" + (float)aLocalDimension.getWidth() + ","
                                        + (float)aLocalDimension.getHeight());
                        }
                    }
                    if (pfeature.getViewer().isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Width,Height:" + aLocalDimension.getWidth() + "," + aLocalDimension.getHeight());
                        }
                    }
                    if (pfeature.getViewer().isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("alter.Wert:" + xp[coordPosition] + "," + yp[coordPosition]);
                        }
                    }
                    if (pfeature.getViewer().isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("neuer.Wert:" + newX + "," + newY);
                        }
                    }
                    if (pfeature.getViewer().isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Berechnung:"
                                        + ((float)(((double)xp[coordPosition] * 10000)
                                                + ((double)aLocalDimension.getWidth() * 10000)) / 10000));
                        }
                    }
                    if (pfeature.getViewer().isSnappingEnabled()) {
//                        Point2D snapPoint=PFeatureTools.getNearestPointInArea(viewer,aEvent.getCanvasPosition(),
//                                new Point2D.Float(getXp()[positionInArray],getYp()[positionInArray]) );
                        final Point2D snapPoint = PFeatureTools.getNearestPointInArea(pfeature.getViewer(),
                                pInputEvent.getCanvasPosition());
                        if (snapPoint != null) {
                            newX = (float)snapPoint.getX();
                            newY = (float)snapPoint.getY();
                        }
                    }

                    // umgekehrte Bewegung fuer Undo speichern
                    endX = newX;
                    endY = newY;

                    updateGeometryPoints(newX, newY);

                    // pfeature.syncGeometry();
                    relocateHandle();

                    if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                        final Set<PFeature> pFeatureSet = glueCoordinates.keySet();
                        for (final PFeature gluePFeature : pFeatureSet) {
                            if (gluePFeature.getFeature().isEditable()) {
                                final Collection coordinates = (Collection)glueCoordinates.get(gluePFeature);
                                if (coordinates != null) {
                                    for (final Object o : coordinates) {
                                        final int oIndex = (Integer)o;
                                        gluePFeature.moveCoordinateToNewPiccoloPosition(
                                            entityPosition,
                                            ringPosition,
                                            oIndex,
                                            newX,
                                            newY);
                                        // gluePFeature.syncGeometry();
                                        if (pfeature.getViewer().isFeatureDebugging()) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("moveCoordinateToNewPiccoloPosition " + gluePFeature);
                                            }
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

                    // Abst\u00E4nde zu den Nachbarn
                    final Coordinate[] coordArr = pfeature.getCoordArr(entityPosition, ringPosition);
                    final int lefty = getLeftNeighbourIndex(coordPosition);
                    final int righty = getRightNeighbourIndex(coordPosition);
                    final Coordinate lc = coordArr[lefty];
                    final Coordinate rc = coordArr[righty];
                    final Coordinate thisC = coordArr[coordPosition];

                    final double leftDistance = thisC.distance(lc);
                    final double rightDistance = thisC.distance(rc);

                    leftInfo.setText("" + StaticDecimalTools.round(leftDistance));
                    rightInfo.setText("" + StaticDecimalTools.round(rightDistance));

                    Point2D lp = new Point2D.Double();
                    Point2D rp = new Point2D.Double();
                    lp.setLocation(xp[lefty], yp[lefty]);
                    rp.setLocation(xp[righty], yp[righty]);
                    lp = pfeature.getViewer().getCamera().viewToLocal(lp);
                    rp = pfeature.getViewer().getCamera().viewToLocal(rp);
                    leftInfo.setX(lp.getX());
                    leftInfo.setY(lp.getY());
                    rightInfo.setX(rp.getX());
                    rightInfo.setY(rp.getY());
                }
            }
        } catch (Throwable t) {
            log.error("Error in dragHandle.", t);
        }
    }

    /**
     * Override this method to get notified when the handle starts to get dragged.
     *
     * @param  aLocalPoint  DOCUMENT ME!
     * @param  aEvent       DOCUMENT ME!
     */
    @Override
    public void startHandleDrag(final Point2D aLocalPoint, final PInputEvent aEvent) {
        if ((pfeature.getFeature() instanceof PureNewFeature)
                    && ((((PureNewFeature)pfeature.getFeature()).getGeometryType()
                            == PureNewFeature.geomTypes.RECTANGLE)
                        || (((PureNewFeature)pfeature.getFeature()).getGeometryType()
                            == PureNewFeature.geomTypes.ELLIPSE))) {
            final Collection selArr = pfeature.getViewer().getFeatureCollection().getSelectedFeatures();
            for (final Object o : selArr) {
                final PFeature pf = (PFeature)(pfeature.getViewer().getPFeatureHM().get(o));
                if ((pf != null) && (pf.getInfoNode() != null)) {
                    pf.getInfoNode().setVisible(false);
                }
            }

            pfeature.getViewer().getHandleLayer().removeAllChildren();
            pfeature.getViewer().getHandleLayer().addChild(this);
            /*if (pivotHandle != null) {
             * pfeature.getViewer().getHandleLayer().addChild(pivotHandle);}*/

            super.startHandleDrag(aLocalPoint, aEvent);
        } else {
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
                // Infonodes (Entfernung) anlegen
                leftInfo = new PText();
                leftInfo.setPaint(new Color(255, 255, 255, 100));
                rightInfo = new PText();
                rightInfo.setPaint(new Color(255, 255, 255, 100));
                addChild(leftInfo);
                addChild(rightInfo);
                leftInfo.setVisible(true);
                rightInfo.setVisible(true);

                // Glue: IdentischePunkte mitverschieben
                if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                    // Features suchen die identische Punkte haben
                    glueCoordinates = pfeature.checkforGlueCoords(entityPosition, ringPosition, coordPosition);
                    log.info("checkforGlueCoords() aufgerufen und " + glueCoordinates.keySet().size() + " gefunden");
                }
                vetoPoint = aLocalPoint;
                final float[] xp = pfeature.getXp(entityPosition, ringPosition);
                final float[] yp = pfeature.getYp(entityPosition, ringPosition);
                startX = xp[coordPosition];
                startY = yp[coordPosition];
                endX = startX;
                endY = startY;

                super.startHandleDrag(aLocalPoint, aEvent);
            }
        }
    }

    @Override
    public void endHandleDrag(final java.awt.geom.Point2D aLocalPoint, final PInputEvent aEvent) {
        if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
            if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
                pfeature.syncGeometry();
                final Collection<Feature> featuresures = new ArrayList<Feature>();
                featuresures.add(pfeature.getFeature());
                ((DefaultFeatureCollection)pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(
                    featuresures);
            } else {
                pfeature.getViewer().getFeatureCollection().reconsiderFeature(pfeature.getFeature());
            }

            removeChild(leftInfo);
            removeChild(rightInfo);
            leftInfo = null;
            rightInfo = null;

            if (((pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE))
                            && (Math.abs(startX - endX) > 0.001d))
                        || (Math.abs(startY - endY) > 0.001d)) {
                if (pfeature.getViewer().isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("neue MoveAction erzeugen");
                    }
                }
                boolean isGluedAction = false;
                if (glueCoordinates.size() != 0) {
                    isGluedAction = true;
                    final Collection<Feature> features = new ArrayList<Feature>();
                    if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                        final Set<PFeature> pFeatureSet = glueCoordinates.keySet();
                        for (final PFeature gluePFeature : pFeatureSet) {
                            if (gluePFeature.getFeature().isEditable()) {
                                features.add(gluePFeature.getFeature());
                                final Collection coordinates = (Collection)glueCoordinates.get(gluePFeature);
                                if (coordinates != null) {
                                    for (final Object o : coordinates) {
//                                                int oIndex = (Integer) o;
//                                                gluePFeature.moveCoordinateToNewPiccoloPosition(oIndex, newX, newY);
//                                                gluePFeature.syncGeometry();
                                        if (pfeature.getViewer().isFeatureDebugging()) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("PFeature synced:" + gluePFeature);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ((DefaultFeatureCollection)pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(
                            features);
                    }
                }

                pfeature.getViewer()
                        .getMemUndo()
                        .addAction(new HandleMoveAction(
                                entityPosition,
                                ringPosition,
                                coordPosition,
                                pfeature,
                                startX,
                                startY,
                                endX,
                                endY,
                                isGluedAction));
                pfeature.getViewer().getMemRedo().clear();
            }
        }
        super.endHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void handleClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pfeature.getViewer().isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("Handle clicked");
            }
        }
        final float[] xp = pfeature.getXp(entityPosition, ringPosition);
        final float[] yp = pfeature.getYp(entityPosition, ringPosition);

        if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.REMOVE_HANDLE)) {
            pfeature.getViewer()
                    .getMemUndo()
                    .addAction(new HandleAddAction(
                            pfeature.getViewer(),
                            pfeature.getFeature(),
                            entityPosition,
                            ringPosition,
                            coordPosition,
                            xp[coordPosition],
                            yp[coordPosition]));
            ((PHandle)(pInputEvent.getPickedNode())).removeHandle();
        } else if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)) {
            pfeature.getViewer()
                    .getMemUndo()
                    .addAction(new HandleDeleteAction(
                            pfeature.getViewer(),
                            pfeature.getFeature(),
                            entityPosition,
                            ringPosition,
                            coordPosition,
                            xp[coordPosition],
                            yp[coordPosition]));
            ((PHandle)(pInputEvent.getPickedNode())).duplicateHandle();
        } else if (pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
            pfeature.addSplitHandle(((PHandle)(pInputEvent.getPickedNode())));
        }
        if (pfeature.getViewer().isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("Ende von handleClicked() getFeature().getGeometry().getCoordinates().length:"
                            + pfeature.getFeature().getGeometry().getCoordinates().length);
            }
        }
        // viewer.getFeatureCollection().reconsiderFeature(getFeature());
    }

    @Override
    public void mouseMovedNotInDragOperation(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                    .getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
    }

    @Override
    public void removeHandle() {
        final float[] xp = pfeature.getXp(entityPosition, ringPosition);
        if (((xp.length > 4)
                        && ((pfeature.getFeature().getGeometry() instanceof Polygon)
                            || (pfeature.getFeature().getGeometry() instanceof MultiPolygon)))
                    || ((xp.length > 1)
                        && (pfeature.getFeature().getGeometry() instanceof LineString))) { // DANGER und Linien ???
            pfeature.removeCoordinate(entityPosition, ringPosition, coordPosition);
            // deswegen (langsam aber funzt):
            if (isSelected()) {
                pfeature.getViewer().showHandles(false);
            }
        }
    }

    @Override
    public void duplicateHandle() {
        pfeature.duplicateCoordinate(entityPosition, ringPosition, coordPosition);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newX  DOCUMENT ME!
     * @param  newY  DOCUMENT ME!
     */
    private void updateGeometryPoints(final float newX, final float newY) {
        if (pfeature.getFeature() instanceof PureNewFeature) {
            final PureNewFeature.geomTypes geomType = ((PureNewFeature)pfeature.getFeature()).getGeometryType();

            switch (geomType) {
                case RECTANGLE: {
                    // letzter Punkt ist gleich erster Punkt. Wir arbeiten lieber mit dem ersten
                    if (coordPosition == 4) {
                        coordPosition = 0;
                    }

                    final int posOpposed = (coordPosition + 2) % 4;
                    final int posPrevious = (coordPosition + 3) % 4;
                    final int posNext = (coordPosition + 1) % 4;

                    final float[] xp = pfeature.getXp(entityPosition, ringPosition);
                    final float[] yp = pfeature.getYp(entityPosition, ringPosition);
                    final Point2D previousPoint = new Point2D.Double(xp[posPrevious], yp[posPrevious]);
                    final Point2D nextPoint = new Point2D.Double(xp[posNext], yp[posNext]);

                    // selektierten punkt verschieben
                    pfeature.moveCoordinateToNewPiccoloPosition(
                        entityPosition,
                        ringPosition,
                        coordPosition,
                        newX,
                        newY);
                    if ((coordPosition % 2) == 0) {
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posPrevious,
                            (float)previousPoint.getX(),
                            newY);
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posNext,
                            newX,
                            (float)nextPoint.getY());
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posOpposed,
                            (float)previousPoint.getX(),
                            (float)nextPoint.getY());
                    } else {
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posPrevious,
                            newX,
                            (float)previousPoint.getY());
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posNext,
                            (float)nextPoint.getX(),
                            newY);
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posOpposed,
                            (float)nextPoint.getX(),
                            (float)previousPoint.getY());
                    }

                    // letzter Punkt ist gleich erster Punkt
                    pfeature.moveCoordinateToNewPiccoloPosition(entityPosition, ringPosition, 4, xp[0], yp[0]);

                    pfeature.updatePath();
                    break;
                }
                case ELLIPSE: {
                    // wird vom EllipseHandle transformiert
                    break;
                }
                // POINT,LINESTRING,POLYGON, UNKNOWN
                default: {
                    pfeature.moveCoordinateToNewPiccoloPosition(
                        entityPosition,
                        ringPosition,
                        coordPosition,
                        newX,
                        newY);
                }
            }
        } else {
            pfeature.moveCoordinateToNewPiccoloPosition(entityPosition, ringPosition, coordPosition, newX, newY);
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
