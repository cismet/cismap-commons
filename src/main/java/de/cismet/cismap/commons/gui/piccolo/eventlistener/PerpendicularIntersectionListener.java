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

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell/nh
 * @version  $Revision$, $Date$
 */
public class PerpendicularIntersectionListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            PerpendicularIntersectionListener.class);
    private static final Color COLOR_PERPENDICULAR_HANDLE = new Color(205, 133, 0, 150);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static enum Stage {

        //~ Enum constants -----------------------------------------------------

        SELECT_FEATURE, CREATE_PERPENDICULAR, ADD_HANDLES
    }

    //~ Instance fields --------------------------------------------------------

    private final MappingComponent mappingComponent;

    private LineSegment perpendicularSegment = null;
    private Coordinate perpendicularCoordinate;

    private PPath perpendicularPathLine = null;
    private PHandle perpendicularHandle = null;
    private PLocator locator = null;
    private Feature selectedFeature = null;
    private Collection<AddHandle> addHandles = new ArrayList<AddHandle>();
    private Collection<PHandle> featureHandles = new ArrayList<PHandle>();

    private Stage stage = Stage.SELECT_FEATURE;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PerpendicularIntersectionListener object.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PerpendicularIntersectionListener(final MappingComponent mappingComponent) {
        super();
        this.mappingComponent = mappingComponent;

        perpendicularPathLine = new PPath();
        locator = new PLocator() {

                @Override
                public double locateX() {
                    if (perpendicularCoordinate != null) {
                        return perpendicularCoordinate.x;
                    } else {
                        return Double.MIN_VALUE;
                    }
                }

                @Override
                public double locateY() {
                    if (perpendicularCoordinate != null) {
                        return perpendicularCoordinate.y;
                    } else {
                        return Double.MIN_VALUE;
                    }
                }
            };
        perpendicularHandle = new PHandle(locator, mappingComponent) {

                @Override
                public void handleClicked(final PInputEvent e) {
                    finishCreatePerpendicular();
                }
            };
        perpendicularHandle.setPaint(COLOR_PERPENDICULAR_HANDLE);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void createFeatureHandles(final Feature feature) {
        createFeatureHandles(feature, stage == Stage.ADD_HANDLES);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature             DOCUMENT ME!
     * @param  isInAddHandleStage  DOCUMENT ME!
     */
    private void createFeatureHandles(final Feature feature, final boolean isInAddHandleStage) {
        createFeatureHandles(Arrays.asList(new Feature[] { feature }), isInAddHandleStage);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  col                 DOCUMENT ME!
     * @param  isInAddHandleStage  DOCUMENT ME!
     */
    private synchronized void createFeatureHandles(final Collection col, final boolean isInAddHandleStage) {
        if (isInAddHandleStage) {
            addHandles.clear();
        }
        featureHandles.clear();
        for (final Object obj : col) {
            if ((obj instanceof Feature) && ((Feature)obj).isEditable()) {
                final Feature feature = (Feature)obj;
                final PFeature pFeature = (PFeature)mappingComponent.getPFeatureHM().get(feature);

                if (pFeature != null) {
                    final Geometry geometry = pFeature.getFeature().getGeometry();
                    if ((geometry instanceof Polygon) || (geometry instanceof LineString)
                                || (geometry instanceof MultiPolygon)) {
                        for (int entityIndex = 0; entityIndex < pFeature.getNumOfEntities(); entityIndex++) {
                            for (int ringIndex = 0; ringIndex < pFeature.getNumOfRings(entityIndex); ringIndex++) {
                                final float[] xp = pFeature.getXp(entityIndex, ringIndex);
                                final float[] yp = pFeature.getYp(entityIndex, ringIndex);

                                // for (int coordPosition = 0; coordPosition < (xp.length - 1); coordPosition++) {
                                // WARNING ! iterating backwards for important reason !!!
                                // If we iterate forwards, the insertion of multiple points
                                // of the same ring will fail later !
                                // This is because after the insertion of the first coordinate
                                // all following insertion have to be shift to one later coordinate.
                                // So it is far easier to iterate backwards to avoid this.
                                for (int coordPosition = xp.length - 1; coordPosition > 0; coordPosition--) {
                                    final LineSegment segment = new LineSegment(
                                            xp[coordPosition - 1],
                                            yp[coordPosition - 1],
                                            xp[coordPosition],
                                            yp[coordPosition]);
                                    featureHandles.add(new PHandle(new PLocator() {

                                                @Override
                                                public double locateX() {
                                                    return segment.p0.x;
                                                }

                                                @Override
                                                public double locateY() {
                                                    return segment.p0.y;
                                                }
                                            }, mappingComponent));
                                    if (isInAddHandleStage) {
                                        final Coordinate intersectionCoord = segment.intersection(perpendicularSegment);

                                        if (intersectionCoord != null) {
                                            // pFeature arbeitet nur mit float genauigkeit
                                            final Coordinate floatSegmentStartCoord = new Coordinate((float)
                                                    segment.p0.x,
                                                    (float)segment.p0.y);
                                            final Coordinate floatSegmentEndCoord = new Coordinate((float)segment.p1.x,
                                                    (float)segment.p1.y);

                                            if (!(floatSegmentStartCoord.equals2D(intersectionCoord)
                                                            || floatSegmentEndCoord.equals2D(intersectionCoord))) {
                                                final AddHandle addHandle = new AddHandle(
                                                        pFeature,
                                                        entityIndex,
                                                        ringIndex,
                                                        coordPosition,
                                                        intersectionCoord,
                                                        segment);
                                                addHandles.add(addHandle);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void createAddHandles() {
        createFeatureHandles(mappingComponent.getFeatureCollection().getAllFeatures(), true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private synchronized void recalculatePerpendicular(final PInputEvent event) {
        perpendicularSegment = null;
        perpendicularCoordinate = null;

        if (selectedFeature != null) {
            final Point2D triggerPoint = event.getPosition();
            final Coordinate triggerCoord = new Coordinate(triggerPoint.getX(), triggerPoint.getY());
            final LineSegment nearestSegment = getNearestSegment(triggerCoord, selectedFeature);

            if (nearestSegment != null) {
                final int snappingDistance = mappingComponent.getSnappingRectSize() / 2;

                if (mappingComponent.isSnappingEnabled()) {
                    final Point2D localTriggerPoint = mappingComponent.getCamera()
                                .viewToLocal((Point2D)triggerPoint.clone());
                    final Point2D localNearestSegmentStartPoint = mappingComponent.getCamera()
                                .viewToLocal(new Point2D.Double(nearestSegment.p0.x, nearestSegment.p0.y));
                    final Point2D localNearestSegmentEndPoint = mappingComponent.getCamera()
                                .viewToLocal(new Point2D.Double(nearestSegment.p1.x, nearestSegment.p1.y));

                    if (localTriggerPoint.distance(localNearestSegmentStartPoint) < snappingDistance) {
                        perpendicularCoordinate = nearestSegment.p0;
                    } else if (localTriggerPoint.distance(localNearestSegmentEndPoint) < snappingDistance) {
                        perpendicularCoordinate = nearestSegment.p1;
                    }
                }

                if (perpendicularCoordinate == null) {
                    perpendicularCoordinate = nearestSegment.closestPoint(triggerCoord);
                }

                // minimum bounds is viewbounds
                final PBounds bounds = new PBounds(mappingComponent.getCamera().getViewBounds());

                // extend bounds to bounds of all features in featurecollection
                final List<Feature> allFeatures = mappingComponent.getFeatureCollection().getAllFeatures();
                for (final Feature feature : allFeatures) {
                    final PFeature pfeature = (PFeature)mappingComponent.getPFeatureHM().get(feature);
                    bounds.add(pfeature.getBounds());
                }

                // create all 4 bounds lines
                final LineSegment top = new LineSegment(
                        bounds.getMinX(),
                        bounds.getMinY(),
                        bounds.getMaxX(),
                        bounds.getMinY());
                final LineSegment bottom = new LineSegment(
                        bounds.getMinX(),
                        bounds.getMaxY(),
                        bounds.getMaxX(),
                        bounds.getMaxY());
                final LineSegment left = new LineSegment(
                        bounds.getMinX(),
                        bounds.getMinY(),
                        bounds.getMinX(),
                        bounds.getMaxY());
                final LineSegment right = new LineSegment(
                        bounds.getMaxX(),
                        bounds.getMinY(),
                        bounds.getMaxX(),
                        bounds.getMaxY());

                // the diagonal of the bounds is the longest possible line
                // that can cross the boundslines
                final double maxLength = Math.sqrt(
                        Math.pow(bounds.getWidth(), 2)
                                * Math.pow(bounds.getHeight(), 2));

                // create left and right perpendicular of the segment
                final LineSegment leftyPerpendicular = leftyPerpendicular(
                        nearestSegment,
                        perpendicularCoordinate,
                        maxLength);
                final LineSegment rightyPerpendicular = rightyPerpendicular(
                        nearestSegment,
                        perpendicularCoordinate,
                        maxLength);

                // check for each bounds line if it intersects with the perpendiculars
                final Collection<LineSegment> boundLines = new ArrayList<LineSegment>();
                boundLines.add(top);
                boundLines.add(bottom);
                boundLines.add(left);
                boundLines.add(right);

                Coordinate leftyIntersection = null;
                Coordinate rightyIntersection = null;

                final Iterator<LineSegment> boundsIterator = boundLines.iterator();
                while (boundsIterator.hasNext()
                            && ((leftyIntersection == null) || (rightyIntersection == null))) {
                    final LineSegment boundsLine = boundsIterator.next();
                    if (leftyIntersection == null) {
                        leftyIntersection = boundsLine.intersection(leftyPerpendicular);
                    }
                    if (rightyIntersection == null) {
                        rightyIntersection = boundsLine.intersection(rightyPerpendicular);
                    }
                }

                // if both intersection points were found (should be the case everytime)
                // then the perpendicular line is determined by the 2 points
                if ((leftyIntersection != null) && (rightyIntersection != null)) {
                    perpendicularSegment = new LineSegment(leftyIntersection, rightyIntersection);
                }
            }
        }
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        super.mouseMoved(event);
        try {
            if (stage == Stage.CREATE_PERPENDICULAR) {
                new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            recalculatePerpendicular(event);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                            } catch (final Exception ex) {
                                LOG.warn(ex, ex);
                            }
                            refreshHandles();
                            relocatePerpendicularHandle();
                        }
                    }.execute();
            }
        } catch (final Exception ex) {
            LOG.info("Exception in mouseMoved", ex);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void relocatePerpendicularHandle() {
        if (mappingComponent.getHandleLayer().getChildrenReference().contains(perpendicularPathLine)) {
            if (perpendicularSegment != null) {
                final Point2D localPerpendicularSegmentStartPoint = mappingComponent.getCamera()
                            .viewToLocal(new Point2D.Double(perpendicularSegment.p0.x, perpendicularSegment.p0.y));
                final Point2D localPerpendicularSegmentEndPoint = mappingComponent.getCamera()
                            .viewToLocal(new Point2D.Double(perpendicularSegment.p1.x, perpendicularSegment.p1.y));
                perpendicularPathLine.setPathToPolyline(
                    new Point2D[] {
                        localPerpendicularSegmentStartPoint,
                        localPerpendicularSegmentEndPoint
                    });
            }
        }
        if (mappingComponent.getHandleLayer().getChildrenReference().contains(perpendicularHandle)) {
            if (perpendicularCoordinate != null) {
                perpendicularHandle.relocateHandle();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     * @param  pPaths   DOCUMENT ME!
     */
    private void setPPathVisible(final boolean visible, final PPath... pPaths) {
        for (final PPath pPath : pPaths) {
            final boolean found = mappingComponent.getHandleLayer().getChildrenReference().contains(pPath);
            if (visible) {
                if (!found) {
                    mappingComponent.getHandleLayer().addChild(pPath);
                }
            } else {
                if (found) {
                    mappingComponent.getHandleLayer().removeChild(pPath);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    private void setPerpendicularHandleVisible(final boolean visible) {
        setPPathVisible(visible, perpendicularHandle);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    private void setFeatureHandlesVisible(final boolean visible) {
        setPPathVisible(visible, featureHandles.toArray(new PPath[0]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    private void setAddHandlesVisible(final boolean visible) {
        setPPathVisible(visible, addHandles.toArray(new PPath[0]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    private void setPerpendicularLineVisible(final boolean visible) {
        setPPathVisible(visible, perpendicularPathLine);
    }

    /**
     * DOCUMENT ME!
     */
    public void init() {
        if ((mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection)
                    && (((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).getSelectedFeatures()
                        .size() == 1)) {
            final Feature old = selectedFeature;
            selectedFeature =
                ((Collection<Feature>)((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                            .getSelectedFeatures()).toArray(new Feature[0])[0];
            if (old != selectedFeature) {
                perpendicularSegment = null;
                perpendicularCoordinate = null;
                stage = Stage.CREATE_PERPENDICULAR;
            }
        } else {
            perpendicularSegment = null;
            perpendicularCoordinate = null;
            selectedFeature = null;
            stage = Stage.SELECT_FEATURE;
        }
        new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    if (selectedFeature == null) {
                        featureHandles.clear();
                        addHandles.clear();
                    } else {
                        createFeatureHandles(selectedFeature);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (final Exception ex) {
                        LOG.warn(ex, ex);
                    }
                    refreshHandles();
                }
            }.execute();
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshHandles() {
        mappingComponent.getHandleLayer().removeAllChildren();
        setPerpendicularLineVisible((perpendicularSegment != null) && (stage != Stage.SELECT_FEATURE));
        setFeatureHandlesVisible(stage != Stage.SELECT_FEATURE);
        setAddHandlesVisible(stage == Stage.ADD_HANDLES);
        setPerpendicularHandleVisible((perpendicularCoordinate != null) && (stage == Stage.CREATE_PERPENDICULAR));
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);
        if (event.isRightMouseButton()) {
            mappingComponent.getFeatureCollection().unselectAll();
            stage = Stage.SELECT_FEATURE;
            refreshHandles();
        } else if (event.isLeftMouseButton()) {
            if (stage == Stage.SELECT_FEATURE) {
                final PFeature pFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                        event,
                        new Class[] { PFeature.class });
                if (pFeature != null) {
                    selectedFeature = pFeature.getFeature();
                    mappingComponent.getFeatureCollection().select(selectedFeature);
                    new SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() throws Exception {
                                stage = Stage.CREATE_PERPENDICULAR;
                                recalculatePerpendicular(event);
                                createFeatureHandles(selectedFeature);
                                return null;
                            }

                            @Override
                            protected void done() {
                                try {
                                    get();
                                } catch (final Exception ex) {
                                    LOG.warn(ex, ex);
                                }
                                refreshHandles();
                                relocatePerpendicularHandle();
                            }
                        }.execute();
                }
            } else if (stage == Stage.CREATE_PERPENDICULAR) {
                finishCreatePerpendicular();
            } else if (stage == Stage.ADD_HANDLES) {
                if (event.getClickCount() == 2) {
                    finishAddHandles();
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void finishCreatePerpendicular() {
        new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    stage = Stage.ADD_HANDLES;
                    createAddHandles();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (final Exception ex) {
                        LOG.warn(ex, ex);
                    }
                    refreshHandles();
                }
            }.execute();
    }

    /**
     * DOCUMENT ME!
     */
    private void finishAddHandles() {
        new SwingWorker<Boolean, Void>() {

                @Override
                protected Boolean doInBackground() throws Exception {
                    stage = Stage.CREATE_PERPENDICULAR;
                    for (final AddHandle addHandle : addHandles) {
                        if (addHandle.isSelected()) {
                            addHandle.insertCoordinate();
                        }
                    }
                    createFeatureHandles(selectedFeature);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (final Exception ex) {
                        LOG.warn(ex, ex);
                    }
                    refreshHandles();
                }
            }.execute();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   segment             DOCUMENT ME!
     * @param   perpendicularStart  DOCUMENT ME!
     * @param   length              DOCUMENT ME!
     * @param   isLefty             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LineSegment perpendicular(final LineSegment segment,
            final Coordinate perpendicularStart,
            final double length,
            final boolean isLefty) {
        final double deltaX = segment.p1.x - segment.p0.x;
        final double deltaY = segment.p1.y - segment.p0.y;

        final double alpha = Math.atan2(deltaY, deltaX);
        final double alpha90 = alpha + Math.toRadians((isLefty) ? -90 : 90);

        final double x = Math.cos(alpha90) * length;
        final double y = Math.sin(alpha90) * length;

        final Coordinate perpendicularEnd = new Coordinate(x + perpendicularStart.x, y + perpendicularStart.y);

        return new LineSegment(perpendicularStart, perpendicularEnd);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   segment             DOCUMENT ME!
     * @param   perpendicularStart  DOCUMENT ME!
     * @param   length              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LineSegment leftyPerpendicular(final LineSegment segment,
            final Coordinate perpendicularStart,
            final double length) {
        return perpendicular(segment, perpendicularStart, length, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   segment             DOCUMENT ME!
     * @param   perpendicularStart  DOCUMENT ME!
     * @param   length              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LineSegment rightyPerpendicular(final LineSegment segment,
            final Coordinate perpendicularStart,
            final double length) {
        return perpendicular(segment, perpendicularStart, length, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   trigger  DOCUMENT ME!
     * @param   feature  sel DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */

    private LineSegment getNearestSegment(final Coordinate trigger, final Feature feature) {
        LineSegment segment = null;
        double dist = Double.POSITIVE_INFINITY;
        final PFeature pfeature = (PFeature)mappingComponent.getPFeatureHM().get(feature);
        if (pfeature != null) {
            final Geometry geometry = pfeature.getFeature().getGeometry();
            if ((geometry instanceof Polygon) || (geometry instanceof LineString)
                        || (geometry instanceof MultiPolygon)) {
                for (int entityIndex = 0; entityIndex < pfeature.getNumOfEntities(); entityIndex++) {
                    for (int ringIndex = 0; ringIndex < pfeature.getNumOfRings(entityIndex); ringIndex++) {
                        final float[] xp = pfeature.getXp(entityIndex, ringIndex);
                        final float[] yp = pfeature.getYp(entityIndex, ringIndex);
                        for (int coordIndex = xp.length - 1; coordIndex > 0; coordIndex--) {
                            final LineSegment tmpSegment = new LineSegment(
                                    xp[coordIndex - 1],
                                    yp[coordIndex - 1],
                                    xp[coordIndex],
                                    yp[coordIndex]);
                            final double tmpDist = tmpSegment.distance(trigger);
                            if (tmpDist < dist) {
                                dist = tmpDist;
                                segment = tmpSegment;
                            }
                        }
                    }
                }
            }
        }
        return segment;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class AddHandle extends PHandle {

        //~ Instance fields ----------------------------------------------------

        private final PFeature pFeature;
        private final int entityPosition;
        private final int ringPosition;
        private final int coordPosition;
        private final Coordinate coordinate;
        private final LineSegment segment;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AddHandle object.
         *
         * @param  pFeature        DOCUMENT ME!
         * @param  entityPosition  DOCUMENT ME!
         * @param  ringPosition    DOCUMENT ME!
         * @param  coordPosition   DOCUMENT ME!
         * @param  coordinate      DOCUMENT ME!
         * @param  segment         DOCUMENT ME!
         */
        public AddHandle(final PFeature pFeature,
                final int entityPosition,
                final int ringPosition,
                final int coordPosition,
                final Coordinate coordinate,
                final LineSegment segment) {
            super(new PLocator() {

                    @Override
                    public double locateX() {
                        return coordinate.x;
                    }

                    @Override
                    public double locateY() {
                        return coordinate.y;
                    }
                }, mappingComponent);

            this.pFeature = pFeature;
            this.entityPosition = entityPosition;
            this.ringPosition = ringPosition;
            this.coordPosition = coordPosition;
            this.coordinate = coordinate;
            this.segment = segment;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Color getDefaultColor() {
            return COLOR_PERPENDICULAR_HANDLE;
        }

        @Override
        public void handleClicked(final PInputEvent pInputEvent) {
            if (pInputEvent.getClickCount() == 2) {
                finishAddHandles();
            } else {
                if (pInputEvent.isLeftMouseButton()) {
                    setSelected(!isSelected());
                }
            }
        }

        /**
         * DOCUMENT ME!
         */
        public void insertCoordinate() {
            final float handleX = (float)coordinate.x;
            final float handleY = (float)coordinate.y;
            pFeature.insertCoordinate(entityPosition, ringPosition, coordPosition, handleX, handleY);
        }
    }
}
