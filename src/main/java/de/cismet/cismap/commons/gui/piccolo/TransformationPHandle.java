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
import java.awt.EventQueue;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.InvalidPolygonTooltip;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleAddAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleDeleteAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.HandleMoveAction;
import de.cismet.cismap.commons.interaction.CismapBroker;
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

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TransformationPHandle.class);

    //~ Instance fields --------------------------------------------------------

    private PText leftInfo;
    private PText rightInfo;
    private MultiMap glueCoordinates = new MultiMap();
    private final PFeature pfeature;
    private final int entityPosition;
    private final int ringPosition;
    private int coordPosition;
    private float startX;
    private float startY;

    private int leftNeighbourIndex;
    private int rightNeighbourIndex;
    private Point2D leftNeighbourPoint;
    private Point2D rightNeighbourPoint;
    private Coordinate leftNeighbourCoordinate;
    private Coordinate rightNeighbourCoordinate;
    private Coordinate[] backupCoordArr;
    private InvalidPolygonTooltip polygonTooltip = new InvalidPolygonTooltip();

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
                    } catch (final Exception ex) {
                        return -1;
                    }
                }

                @Override
                public double locateY() {
                    try {
                        return pfeature.getYp(entityPosition, ringPosition)[coordPosition];
                    } catch (final Exception ex) {
                        return -1;
                    }
                }
            }, pfeature.getViewer());

        this.pfeature = pfeature;
        this.entityPosition = entityPosition;
        this.ringPosition = ringPosition;
        this.coordPosition = coordPosition;
        polygonTooltip.setVisible(false);
        addChild(polygonTooltip);
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
            return pfeature.getCoordArr(entityPosition, ringPosition).length - 2;
        } else if (coordPosition == (pfeature.getCoordArr(entityPosition, ringPosition).length - 1)) {
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
        } else if (coordPosition == (pfeature.getCoordArr(entityPosition, ringPosition).length - 1)) {
            return 1;
        } else {
            return coordPosition + 1;
        }
    }

    @Override
    public void dragHandle(final PDimension aLocalDimension, final PInputEvent pInputEvent) {
        try {
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)
                        && !pfeature.getViewer().getInteractionMode().equals(MappingComponent.MOVE_POLYGON)) {
                final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                            .getInputListener(MappingComponent.MOTION);
                if (moveListener != null) {
                    moveListener.mouseMoved(pInputEvent);
                } else {
                    LOG.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
                }

                if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)
                            || pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE)) {
                    // neue HandlePosition berechnen

                    float currentX;
                    float currentY;

                    // CTRL DOWN => an der Linie kleben
                    if (pInputEvent.isLeftMouseButton() && pInputEvent.isControlDown()) {
                        final Point2D trigger = pInputEvent.getCanvasPosition();

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

                        final Point2D erg = StaticGeometryFunctions.createPointOnLine(
                                leftNeighbourPoint,
                                rightNeighbourPoint,
                                trigger);

                        final Point2D ergPoint = pfeature.getViewer().getCamera().localToView(erg);
                        currentX = (float)ergPoint.getX();
                        currentY = (float)ergPoint.getY();
                    } else {
                        // an der Maus
                        currentX = (float)pInputEvent.getPosition().getX();
                        currentY = (float)pInputEvent.getPosition().getY();

                        // snapping ?
                        if (pfeature.getViewer().isSnappingEnabled()) {
                            final boolean vertexRequired = pfeature.getViewer().isSnappingOnLineEnabled();
                            final Point2D snapPoint = PFeatureTools.getNearestPointInArea(
                                    pfeature.getViewer(),
                                    pInputEvent.getCanvasPosition(),
                                    vertexRequired,
                                    true);
                            if (snapPoint != null) {
                                currentX = (float)snapPoint.getX();
                                currentY = (float)snapPoint.getY();
                            }
                        }
                    }

                    updateGeometryPoints(currentX, currentY);
                    // pfeature.syncGeometry();
                    relocateHandle();

                    // anzeigen von fehler bei ungültigen operationen bei (multi)-polygone
                    if (((pfeature.getFeature().getGeometry() instanceof MultiPolygon)
                                    || (pfeature.getFeature().getGeometry() instanceof Polygon))
                                && !pfeature.isValid(entityPosition, ringPosition)) {
                        final boolean creatingHole = ringPosition > 0;

                        polygonTooltip.setOffset(
                            pInputEvent.getCanvasPosition().getX()
                                    + 20.0d,
                            pInputEvent.getCanvasPosition().getY()
                                    + 20.0d);
                        if (creatingHole) {
                            polygonTooltip.setMode(InvalidPolygonTooltip.Mode.HOLE_ERROR);
                        } else {
                            polygonTooltip.setMode(InvalidPolygonTooltip.Mode.ENTITY_ERROR);
                        }
                        polygonTooltip.setVisible(true);
                    } else {
                        polygonTooltip.setVisible(false);
                    }

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
                                            currentX,
                                            currentY);
                                        // gluePFeature.syncGeometry();
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
                    final Coordinate coordinate = coordArr[coordPosition];

                    final double leftDistance = coordinate.distance(leftNeighbourCoordinate);
                    final double rightDistance = coordinate.distance(rightNeighbourCoordinate);

                    leftInfo.setText(StaticDecimalTools.round(leftDistance));
                    rightInfo.setText(StaticDecimalTools.round(rightDistance));
                }
            }
        } catch (final Throwable t) {
            LOG.error("Error in dragHandle.", t);
        }
        super.dragHandle(aLocalDimension, pInputEvent);
    }

    /**
     * Override this method to get notified when the handle starts to get dragged.
     *
     * @param  aLocalPoint  DOCUMENT ME!
     * @param  aEvent       DOCUMENT ME!
     */
    @Override
    public void startHandleDrag(final Point2D aLocalPoint, final PInputEvent aEvent) {
        try {
            final Point2D startPoint = PFeatureTools.getNearestPointInArea(
                    pfeature.getViewer(),
                    aEvent.getCanvasPosition(),
                    false,
                    false);
            CismapBroker.getInstance().setSnappingVetoPoint(startPoint);
            CismapBroker.getInstance().setSnappingVetoFeature(pfeature);
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.MOVE_POLYGON)) {
                final Coordinate[] coordArr = pfeature.getCoordArr(entityPosition, ringPosition);
                final float[] xp = pfeature.getXp(entityPosition, ringPosition);
                final float[] yp = pfeature.getYp(entityPosition, ringPosition);

                backupCoordArr = new Coordinate[coordArr.length];
                System.arraycopy(coordArr, 0, backupCoordArr, 0, backupCoordArr.length);

                leftNeighbourIndex = getLeftNeighbourIndex(coordPosition);
                rightNeighbourIndex = getRightNeighbourIndex(coordPosition);
                leftNeighbourCoordinate = coordArr[leftNeighbourIndex];
                rightNeighbourCoordinate = coordArr[rightNeighbourIndex];
                leftNeighbourPoint = new Point2D.Double(xp[leftNeighbourIndex], yp[leftNeighbourIndex]);
                rightNeighbourPoint = new Point2D.Double(xp[rightNeighbourIndex], yp[rightNeighbourIndex]);

                if ((pfeature.getFeature() instanceof AbstractNewFeature)
                            && ((((AbstractNewFeature)pfeature.getFeature()).getGeometryType()
                                    == AbstractNewFeature.geomTypes.RECTANGLE)
                                || (((AbstractNewFeature)pfeature.getFeature()).getGeometryType()
                                    == AbstractNewFeature.geomTypes.ELLIPSE))) {
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
                } else {
                    if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)) {
                        // Infonodes (Entfernung) anlegen
                        final Point2D leftInfoPoint = pfeature.getViewer().getCamera().viewToLocal(leftNeighbourPoint);
                        final Point2D rightInfoPoint = pfeature.getViewer()
                                    .getCamera()
                                    .viewToLocal(rightNeighbourPoint);

                        leftInfo = new PText();
                        leftInfo.setPaint(new Color(255, 255, 255, 100));
                        rightInfo = new PText();
                        rightInfo.setPaint(new Color(255, 255, 255, 100));
                        leftInfo.setX(leftInfoPoint.getX() + 6);
                        leftInfo.setY(leftInfoPoint.getY() - 6);
                        rightInfo.setX(rightInfoPoint.getX() + 6);
                        rightInfo.setY(rightInfoPoint.getY() - 6);
                        leftInfo.setVisible(true);
                        rightInfo.setVisible(true);
                        addChild(leftInfo);
                        addChild(rightInfo);

                        // Glue: IdentischePunkte mitverschieben
                        if (pfeature.getViewer().isInGlueIdenticalPointsMode()) {
                            // Features suchen die identische Punkte haben
                            glueCoordinates = pfeature.checkforGlueCoords(entityPosition, ringPosition, coordPosition);
                            LOG.info("checkforGlueCoords() aufgerufen und " + glueCoordinates.keySet().size()
                                        + " gefunden");
                        }
                        startX = xp[coordPosition];
                        startY = yp[coordPosition];
                    }
                }
            }
        } catch (final Throwable t) {
            LOG.error("Error in startHandleDrag.", t);
        }
        super.startHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void endHandleDrag(final java.awt.geom.Point2D aLocalPoint, final PInputEvent aEvent) {
        try {
            // polygonTooltip.setVisible(false);
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.SPLIT_POLYGON)
                        && !pfeature.getViewer().getInteractionMode().equals(MappingComponent.MOVE_POLYGON)) {
                // rückgängig machen ungültiger operationen bei (multi)-polygone
                if (((pfeature.getFeature().getGeometry() instanceof MultiPolygon)
                                || (pfeature.getFeature().getGeometry() instanceof Polygon))
                            && !pfeature.isValid(entityPosition, ringPosition)) {
                    updateGeometryPoints(startX, startY);
                    // pfeature.syncGeometry();
                    relocateHandle();
                }

                if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
                    pfeature.syncGeometry();
                    final Collection<Feature> features = new ArrayList<Feature>();
                    features.add(pfeature.getFeature());
                    ((DefaultFeatureCollection)pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(
                        features);
                } else {
                    pfeature.getViewer().getFeatureCollection().reconsiderFeature(pfeature.getFeature());
                }

                // remove the veto objects
                CismapBroker.getInstance().setSnappingVetoFeature(null);
                CismapBroker.getInstance().setSnappingVetoPoint(null);

                // linke und rechte info entfernen
                removeChild(leftInfo);
                removeChild(rightInfo);
                leftInfo = null;
                rightInfo = null;

                if (((pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE))
                                && (Math.abs(startX - getLocator().locateX()) > 0.001d))
                            || (Math.abs(startY - getLocator().locateY()) > 0.001d)) {
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
                                            // int oIndex = (Integer) o;
                                            // gluePFeature.moveCoordinateToNewPiccoloPosition(oIndex, newX, newY);
                                            // gluePFeature.syncGeometry();
                                            if (pfeature.getViewer().isFeatureDebugging()) {
                                                if (LOG.isDebugEnabled()) {
                                                    LOG.debug("PFeature synced:" + gluePFeature);
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
                                    (float)getLocator().locateX(),
                                    (float)getLocator().locateY(),
                                    isGluedAction));
                    pfeature.getViewer().getMemRedo().clear();
                }
            }
        } catch (final Throwable t) {
            LOG.error("Error in endHandleDrag.", t);
        }
        super.endHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void handleClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        try {
            if (!pfeature.getViewer().getInteractionMode().equals(MappingComponent.MOVE_POLYGON)) {
                if (pfeature.getViewer().isFeatureDebugging()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Handle clicked");
                    }
                }
                final float[] xp = pfeature.getXp(entityPosition, ringPosition);
                final float[] yp = pfeature.getYp(entityPosition, ringPosition);

                if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.REMOVE_HANDLE)) {
                    final Coordinate[] coordArr = pfeature.getCoordArr(entityPosition, ringPosition);
                    final Coordinate[] newCoordArr = new Coordinate[coordArr.length - 1];
                    System.arraycopy(coordArr, 0, newCoordArr, 0, coordPosition);
                    System.arraycopy(
                        coordArr,
                        coordPosition
                                + 1,
                        newCoordArr,
                        coordPosition,
                        newCoordArr.length
                                - coordPosition);
                    if ((pfeature.getFeature().getGeometry() instanceof Polygon)
                                || (pfeature.getFeature().getGeometry() instanceof MultiPolygon)) {
                        newCoordArr[newCoordArr.length - 1] = newCoordArr[0];
                    }
                    if (pfeature.isValidWithThisCoordinates(entityPosition, ringPosition, newCoordArr)) {
                        ((PHandle)(pInputEvent.getPickedNode())).removeHandle();
                        polygonTooltip.setVisible(false);
                    } else {
                        polygonTooltip.setOffset(
                            pInputEvent.getCanvasPosition().getX()
                                    + 20.0d,
                            pInputEvent.getCanvasPosition().getY()
                                    + 20.0d);
                        if (ringPosition > 0) {
                            showInvalidPolygonTooltip(InvalidPolygonTooltip.Mode.HOLE_ERROR);
                        } else {
                            showInvalidPolygonTooltip(InvalidPolygonTooltip.Mode.ENTITY_ERROR);
                        }
                    }
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
                    if (pfeature.getFeature().getGeometry() instanceof Polygon) {
                        pfeature.addSplitHandle(((PHandle)(pInputEvent.getPickedNode())));
                    } else if (pfeature.getFeature().getGeometry() instanceof LineString) {
                        if ((coordPosition > 0) && (coordPosition < (xp.length - 1))) {
                            pfeature.addSplitHandle(((PHandle)(pInputEvent.getPickedNode())));
                        }
                    }
                }
                if (pfeature.getViewer().isFeatureDebugging()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Ende von handleClicked() getFeature().getGeometry().getCoordinates().length:"
                                    + pfeature.getFeature().getGeometry().getCoordinates().length);
                    }
                }
                // viewer.getFeatureCollection().reconsiderFeature(getFeature());
            }
        } catch (final Throwable t) {
            LOG.error("Error in handleClicked.", t);
        }
        super.handleClicked(pInputEvent);
    }

    @Override
    public void mouseMovedNotInDragOperation(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                    .getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            LOG.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
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
        if (pfeature.getFeature() instanceof AbstractNewFeature) {
            final AbstractNewFeature.geomTypes geomType = ((AbstractNewFeature)pfeature.getFeature()).getGeometryType();

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
                            xp[posPrevious],
                            newY);
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posNext,
                            newX,
                            yp[posNext]);
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posOpposed,
                            xp[posPrevious],
                            yp[posNext]);
                    } else {
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posPrevious,
                            newX,
                            yp[posPrevious]);
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posNext,
                            xp[posNext],
                            newY);
                        pfeature.moveCoordinateToNewPiccoloPosition(
                            entityPosition,
                            ringPosition,
                            posOpposed,
                            xp[posNext],
                            yp[posPrevious]);
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

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    private void showInvalidPolygonTooltip(final InvalidPolygonTooltip.Mode mode) {
        new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    polygonTooltip.setMode(mode);
                    polygonTooltip.setVisible(true);
                    Thread.sleep(2000);
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    polygonTooltip.setVisible(false);
                }
            }.execute();
    }
}
