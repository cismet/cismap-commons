/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.tools;

import Sirius.util.collections.MultiMap;

import com.vividsolutions.jts.algorithm.CentroidPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.*;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.ParentNodeIsAPFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PFeatureTools {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PFeatureTools.class); // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PNode getFirstValidObjectUnderPointer2(final PInputEvent pInputEvent,
            final Class[] validClasses) {
        return getFirstValidObjectUnderPointer2(pInputEvent, validClasses, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   halo          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PNode getFirstValidObjectUnderPointer2(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
        final List<PNode> allValids = (List<PNode>)getFirstObjectsUnderPointer(pInputEvent, validClasses, halo);
        if (allValids.isEmpty()) {
            return null;
        }
        return allValids.get(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   halo          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PNode> getFirstObjectsUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
        return getValidObjectsUnderPointer(pInputEvent, validClasses, halo, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   halo          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PNode> getValidObjectsUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
        return getValidObjectsUnderPointer(pInputEvent, validClasses, halo, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent          mc DOCUMENT ME!
     * @param   validClasses         DOCUMENT ME!
     * @param   halo                 DOCUMENT ME!
     * @param   stopAfterFirstValid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PNode> getValidObjectsUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo,
            final boolean stopAfterFirstValid) {
        final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();
        final WorldToScreenTransform wtst = mc.getWtst();

        final int srs = CrsTransformer.extractSridFromCrs(mc.getMappingModel().getSrs().getCode());
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srs);
        final double x1 = wtst.getWorldX(pInputEvent.getPosition().getX());
        final double y1 = wtst.getWorldY(pInputEvent.getPosition().getY());
        final Geometry point;
        if (halo > 0) {
            point = gf.createPoint(new Coordinate(x1, y1)).buffer(halo * mc.getScaleDenominator());
        } else {
            point = gf.createPoint(new Coordinate(x1, y1));
        }

        final List<PNode> pNodes = new ArrayList<PNode>(findIntersectingPFeatures(mc.getFeatureLayer(), point));

        for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
            final PNode pNode = mc.getMapServiceLayer().getChild(i);
            if (pNode instanceof PLayer) {
                pNodes.addAll(findIntersectingPFeatures(pNode, point));
            }
        }

        final LinkedList<PNode> allValidPNodes = new LinkedList<PNode>();
        for (final PNode pNode : pNodes) {
            for (int i = 0; i < validClasses.length; ++i) {
                if ((pNode != null) && validClasses[i].isAssignableFrom(pNode.getClass())
                            && (pNode.getParent() != null)
                            && pNode.getParent().getVisible() && pNode.getVisible()) {
                    allValidPNodes.add(pNode);
                    if (stopAfterFirstValid) {
                        break;
                    }
                }
            }
        }
        return allValidPNodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc      DOCUMENT ME!
     * @param   bounds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PFeature[] getPFeaturesInArea(final MappingComponent mc, final PBounds bounds) {
        final WorldToScreenTransform wtst = mc.getWtst();
        final Geometry bBox = getGeometryFromPBounds(bounds, wtst, mc.getMappingModel().getSrs().getCode());

        return getPFeaturesInArea(mc, bBox);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc    DOCUMENT ME!
     * @param   geom  bounds DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PFeature[] getPFeaturesInArea(final MappingComponent mc, final Geometry geom) {
        final List<PFeature> pFeatures = findIntersectingPFeatures(mc.getFeatureLayer(), geom);

        for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
            final PNode p = mc.getMapServiceLayer().getChild(i);
            if (p instanceof PLayer) {
                pFeatures.addAll(findIntersectingPFeatures(mc.getMapServiceLayer().getChild(i), geom));
            }
        }

        final Collection<PFeature> vRet = new ArrayList<PFeature>(pFeatures.size());
        for (final PFeature pf : pFeatures) {
            if (pf.isSnappable()) {
                vRet.add(pf);
            }
        }
        return vRet.toArray(new PFeature[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bounds  a PBounds object, that should contain screen coordinates. (See the class WorldToScreenTransform)
     * @param   wtst    DOCUMENT ME!
     * @param   crs     DOCUMENT ME!
     *
     * @return  a Geometry object that represents the given PBounds object.
     */
    public static Geometry getGeometryFromPBounds(final PBounds bounds,
            final WorldToScreenTransform wtst,
            final String crs) {
        final int srs = CrsTransformer.extractSridFromCrs(crs);
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srs);
        final double x1 = wtst.getWorldX(bounds.x);
        final double x2 = wtst.getWorldX(bounds.x + bounds.width);
        final double y1 = wtst.getWorldY(bounds.y);
        final double y2 = wtst.getWorldY(bounds.y + bounds.height);
        final Coordinate[] polyCords = new Coordinate[5];

        polyCords[0] = new Coordinate(x1, y1);
        polyCords[1] = new Coordinate(x1, y2);
        polyCords[2] = new Coordinate(x2, y2);
        polyCords[3] = new Coordinate(x2, y1);
        polyCords[4] = new Coordinate(x1, y1);

        return gf.createPolygon(gf.createLinearRing(polyCords), null);
    }

    /**
     * This should be used instead of the findIntersectingNodes method of the PNode class. The differences between this
     * methods and the findIntersectingNodes method are, that this method only finds PFeature objects and this method
     * works properly.
     *
     * @param   node      The node, the PFeatures should be find in
     * @param   geometry  the search geometry
     *
     * @return  DOCUMENT ME!
     */
    public static List<PFeature> findIntersectingPFeatures(final PNode node, final Geometry geometry) {
        final List<PFeature> pFeatures = new ArrayList<PFeature>();
        final String srs = CrsTransformer.createCrsFromSrid(geometry.getSRID());

        for (int index = 0; index < node.getChildrenCount(); index++) {
            final PNode pNode = node.getChild(index);
            if (pNode instanceof PFeature) {
                final PFeature pFeature = (PFeature)pNode;
                Geometry featureGeometry = pFeature.getFeature().getGeometry();

                if (featureGeometry.getSRID() != geometry.getSRID()) {
                    featureGeometry = CrsTransformer.transformToGivenCrs(featureGeometry, srs);
                }

                if (intersects(featureGeometry, geometry)) {
                    pFeatures.add(pFeature);
                }
            } else {
                pFeatures.addAll(findIntersectingPFeatures(pNode, geometry));
            }
        }
        return pFeatures;
    }

    /**
     * Determines whether g1 and g2 intersects. Contrary to the intersects method of the Geometry class, this method
     * does also support GeometryCollections.
     *
     * @param   g1  DOCUMENT ME!
     * @param   g2  DOCUMENT ME!
     *
     * @return  true, iff g1 intersects g2
     */
    private static boolean intersects(Geometry g1, Geometry g2) {
        if (g2 instanceof GeometryCollection) {
            final Geometry tmp = g1;
            g1 = g2;
            g2 = tmp;
        }

        if ((g1 instanceof GeometryCollection) && (g2 instanceof GeometryCollection)) {
            final GeometryCollection gc1 = (GeometryCollection)g1;
            final GeometryCollection gc2 = (GeometryCollection)g2;

            for (int i = 0; i < gc1.getNumGeometries(); ++i) {
                for (int n = 0; n < gc2.getNumGeometries(); ++n) {
                    final Geometry geomEntry1 = gc1.getGeometryN(i);
                    final Geometry geomEntry2 = gc2.getGeometryN(n);

                    if (intersects(geomEntry1, geomEntry2)) {
                        return true;
                    }
                }
            }

            return false;
        } else if (g1 instanceof GeometryCollection) {
            final GeometryCollection gc = (GeometryCollection)g1;

            for (int i = 0; i < gc.getNumGeometries(); ++i) {
                final Geometry geomEntry = gc.getGeometryN(i);

                if (intersects(geomEntry, g2)) {
                    return true;
                }
            }

            return false;
        } else {
            return g1.intersects(g2);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc      DOCUMENT ME!
     * @param   bounds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D[] getPointsInArea(final MappingComponent mc, final PBounds bounds) {
        final PFeature[] features = getPFeaturesInArea(mc, bounds);
        final Collection<Point2D> points = new ArrayList<Point2D>();
        if (features == null) {
            return null;
        }
        for (final PFeature pfeature : features) {
            for (int entityIndex = 0; entityIndex < pfeature.getNumOfEntities(); entityIndex++) {
                for (int ringIndex = 0; ringIndex < pfeature.getNumOfRings(entityIndex); ringIndex++) {
                    final float[] xp = pfeature.getXp(entityIndex, ringIndex);
                    final float[] yp = pfeature.getYp(entityIndex, ringIndex);
                    for (int position = 0; position < xp.length; position++) {
                        if (bounds.contains(xp[position], yp[position])) {
                            points.add(new Point2D.Float(xp[position], yp[position]));
                        }
                    }
                }
            }
        }
        return points.toArray(new Point2D[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc          DOCUMENT ME!
     * @param   bounds      DOCUMENT ME!
     * @param   myPosition  DOCUMENT ME!
     * @param   vetoPoint   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Point2D getNearestPointInArea(final MappingComponent mc,
            final PBounds bounds,
            final Point2D myPosition,
            final Point2D vetoPoint) {
        final Point2D[] points = getPointsInArea(mc, bounds);
        double distance = -1;
        Point2D nearestPoint = null;
        for (int i = 0; i < points.length; ++i) {
            if ((vetoPoint != null) && points[i].equals(vetoPoint)) {
                return null;
            }
            final double distanceCheck = myPosition.distanceSq(points[i]);
            if ((distance < 0) || (distanceCheck < distance)) {
                nearestPoint = points[i];
                distance = distanceCheck;
            }
        }
        return nearestPoint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc              DOCUMENT ME!
     * @param   canvasPosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D getNearestPointInArea(final MappingComponent mc, final Point2D canvasPosition) {
        return getNearestPointInArea(mc, canvasPosition, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc                   DOCUMENT ME!
     * @param   canvasPosition       DOCUMENT ME!
     * @param   vertexRequired       DOCUMENT ME!
     * @param   considerVetoObjects  veto objects are objects, which should be ignored from the snapping mechanism. This
     *                               can be the currently modifying feature.
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D getNearestPointInArea(final MappingComponent mc,
            final Point2D canvasPosition,
            final boolean vertexRequired,
            final boolean considerVetoObjects) {
        final Point2D vetoPoint = (considerVetoObjects ? CismapBroker.getInstance().getSnappingVetoPoint() : null);

        if (!vertexRequired) {
            return getNearestPointInArea(mc, canvasPosition, vetoPoint);
        } else {
            final PFeature vetoFeature = (considerVetoObjects ? CismapBroker.getInstance().getSnappingVetoFeature()
                                                              : null);

            return getNearestPointInAreaNoVertexRequired(
                    mc,
                    canvasPosition,
                    vetoPoint,
                    vetoFeature);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc              DOCUMENT ME!
     * @param   canvasPosition  DOCUMENT ME!
     * @param   vetoPoint       DOCUMENT ME!
     * @param   vetoFeature     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D getNearestPointInAreaNoVertexRequired(final MappingComponent mc,
            final Point2D canvasPosition,
            final Point2D vetoPoint,
            final PFeature vetoFeature) {
        final Rectangle2D area = new Rectangle((int)canvasPosition.getX() - (mc.getSnappingRectSize() / 2),
                (int)canvasPosition.getY()
                        - (mc.getSnappingRectSize() / 2),
                mc.getSnappingRectSize(),
                mc.getSnappingRectSize());
        final Rectangle2D d2d = mc.getCamera().localToView(new PBounds(area));
        final Point2D myPosition = mc.getCamera().localToView(canvasPosition);
        final PBounds bounds = new PBounds(d2d);

        final Point2D[] points = getPointsInAreaNoVertexRequired(mc, bounds, myPosition, vetoFeature);
        double distance = -1;
        Point2D nearestPoint = null;
        for (int i = 0; i < points.length; ++i) {
            final double distanceCheck = myPosition.distanceSq(points[i]);
            if (((vetoPoint == null) || !vetoPoint.equals(points[i]))
                        && ((distance < 0) || (distanceCheck < distance))) {
                nearestPoint = points[i];
                distance = distanceCheck;
            }
        }
        return nearestPoint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc               DOCUMENT ME!
     * @param   bounds           DOCUMENT ME!
     * @param   currentPosition  DOCUMENT ME!
     * @param   vetoFeature      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D[] getPointsInAreaNoVertexRequired(final MappingComponent mc,
            final PBounds bounds,
            final Point2D currentPosition,
            final PFeature vetoFeature) {
        final PFeature[] features = getPFeaturesInArea(mc, bounds);
        final Collection<Point2D> points = new ArrayList<Point2D>();
        if (features == null) {
            return null;
        }
        final Coordinate c = new Coordinate(currentPosition.getX(), currentPosition.getY());
        for (final PFeature pfeature : features) {
            if (!pfeature.equals(vetoFeature)) {
                final LineSegment seg = getNearestSegment(c, pfeature);
                final Coordinate point = seg.closestPoint(c);
                if (bounds.contains(point.x, point.y)) {
                    points.add(new Point2D.Float((float)point.x, (float)point.y));
                }
            }
        }
        return points.toArray(new Point2D[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   trigger   DOCUMENT ME!
     * @param   pfeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static LineSegment getNearestSegment(final Coordinate trigger, final PFeature pfeature) {
        LineSegment segment = null;
        double dist = Double.POSITIVE_INFINITY;
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

    /**
     * DOCUMENT ME!
     *
     * @param   mc              DOCUMENT ME!
     * @param   canvasPosition  DOCUMENT ME!
     * @param   vetoPoint       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D getNearestPointInArea(final MappingComponent mc,
            final Point2D canvasPosition,
            final Point2D vetoPoint) {
        final Rectangle2D area = new Rectangle((int)canvasPosition.getX() - (mc.getSnappingRectSize() / 2),
                (int)canvasPosition.getY()
                        - (mc.getSnappingRectSize() / 2),
                mc.getSnappingRectSize(),
                mc.getSnappingRectSize());
        final Rectangle2D d2d = mc.getCamera().localToView(new PBounds(area));
        final Point2D myPosition = mc.getCamera().localToView(canvasPosition);
        return getNearestPointInArea(mc, new PBounds(d2d), myPosition, vetoPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PNode getFirstValidObjectUnderPointer(final PInputEvent pInputEvent, final Class[] validClasses) {
        final double halo = ((CismapBroker.getInstance().getSrs().isMetric()) ? 1d : 0.0001);
        return getFirstValidObjectUnderPointer(pInputEvent, validClasses, halo);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   deepPick      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PNode getFirstValidObjectUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final boolean deepPick) {
        final double halo = ((CismapBroker.getInstance().getSrs().isMetric()) ? 1d : 0.0001);
        return getFirstValidObjectUnderPointer(pInputEvent, validClasses, halo, deepPick);
    }

    /**
     * Determines the nearest geometry coordinate.
     *
     * @param   mc                   DOCUMENT ME!
     * @param   canvasPosition       DOCUMENT ME!
     * @param   considerVetoObjects  veto objects are objects, which should be ignored from the snapping mechanism. This
     *                               can be the currently modifying feature.
     *
     * @return  DOCUMENT ME!
     *
     * @see     getNearestPointInArea(final MappingComponent mc, final Point2D canvasPosition, final Point2D vetoPoint)
     */
    public static Coordinate getNearestCoordinateInArea(final MappingComponent mc,
            final Point2D canvasPosition,
            final boolean considerVetoObjects) {
        final Point2D vetoPoint = (considerVetoObjects ? CismapBroker.getInstance().getSnappingVetoPoint() : null);

        return getNearestCoordinateInArea(mc, canvasPosition, vetoPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc              DOCUMENT ME!
     * @param   canvasPosition  DOCUMENT ME!
     * @param   vetoPoint       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate getNearestCoordinateInArea(final MappingComponent mc,
            final Point2D canvasPosition,
            final Point2D vetoPoint) {
        final Rectangle2D area = new Rectangle((int)canvasPosition.getX() - (mc.getSnappingRectSize() / 2),
                (int)canvasPosition.getY()
                        - (mc.getSnappingRectSize() / 2),
                mc.getSnappingRectSize(),
                mc.getSnappingRectSize());
        final Rectangle2D d2d = mc.getCamera().localToView(new PBounds(area));
        final Point2D myPosition = mc.getCamera().localToView(canvasPosition);
        return getNearestCoordinateInArea(mc, new PBounds(d2d), myPosition, vetoPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc          DOCUMENT ME!
     * @param   bounds      DOCUMENT ME!
     * @param   myPosition  DOCUMENT ME!
     * @param   vetoPoint   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Coordinate getNearestCoordinateInArea(final MappingComponent mc,
            final PBounds bounds,
            final Point2D myPosition,
            final Point2D vetoPoint) {
        final PFeature[] features = getPFeaturesInArea(mc, bounds);
        final List<Coordinate> coordinates = new ArrayList<Coordinate>();
        final Collection<Point2D> p = new ArrayList<Point2D>();
        if (features == null) {
            return null;
        }
        for (final PFeature pfeature : features) {
            for (int entityIndex = 0; entityIndex < pfeature.getNumOfEntities(); entityIndex++) {
                for (int ringIndex = 0; ringIndex < pfeature.getNumOfRings(entityIndex); ringIndex++) {
                    final float[] xp = pfeature.getXp(entityIndex, ringIndex);
                    final float[] yp = pfeature.getYp(entityIndex, ringIndex);
                    for (int position = 0; position < xp.length; position++) {
                        if (bounds.contains(xp[position], yp[position])) {
                            p.add(new Point2D.Float(xp[position], yp[position]));
                            coordinates.add(pfeature.getCoordinate(entityIndex, ringIndex, position));
                        }
                    }
                }
            }
        }
        final Point2D[] points = p.toArray(new Point2D[0]);

        double distance = -1;
        Coordinate nearestCoordinate = null;
        for (int i = 0; i < points.length; ++i) {
            if ((vetoPoint != null) && points[i].equals(vetoPoint)) {
                return null;
            }
            final double distanceCheck = myPosition.distanceSq(points[i]);
            if ((distance < 0) || (distanceCheck < distance)) {
                nearestCoordinate = coordinates.get(i);
                distance = distanceCheck;
            }
        }

        return nearestCoordinate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   halo          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PNode getFirstValidObjectUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
        return getFirstValidObjectUnderPointer(pInputEvent, validClasses, halo, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   halo          DOCUMENT ME!
     * @param   deepSeek      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PNode getFirstValidObjectUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo,
            final boolean deepSeek) {
        // Dieses Konstrukt sorgt daf\u00FCr das uninteressante Objekte die oberhalb dem Mauszeiger liegen
        // einfach ignoriert werden
        PNode pNode = null;

        final double xPos = pInputEvent.getPosition().getX();
        final double yPos = pInputEvent.getPosition().getY();

        final PPickPath pp = ((MappingComponent)pInputEvent.getComponent()).getCamera()
                    .pick(pInputEvent.getCanvasPosition().getX(), pInputEvent.getCanvasPosition().getY(), halo);
        pp.pushNode(pInputEvent.getPickedNode());

        if (deepSeek) {
            boolean first = true;
            do {
                if (first) {
                    pNode = pp.getPickedNode();
                    first = false;
                } else {
                    // this is a very time consuming method and should be avoided in move or drag listeners
                    pNode = pp.nextPickedNode();
                }

                // pNode is null if there is no Node to pick anymore

                // this if is needed to exit the loop
                if (pNode == null) {
                    break;
                }
                pNode = getRightPNodeOrNull(pNode, validClasses, xPos, yPos);
                // pNode is null if it is not of the right type
            } while (pNode == null);

            return pNode;
        } else {
            // deepSeek == false can be used in move or drag listeners
            // it uses the existing PStack to iterate over the picked Nodes
            // and check whether the nodes are from the right type
            // the loop starts with the highest index and goes down to 0
            // the node with the index 0 is always a camera but since we don't know
            // what is in the validClasses[] we should go through till 0

            int getIndex = pp.getNodeStackReference().size() - 1;
            if (pp.getNodeStackReference().size() > 0) {
                do {
                    pNode = (PNode)pp.getNodeStackReference().get(getIndex);
                    pNode = getRightPNodeOrNull(pNode, validClasses, xPos, yPos);
                    getIndex--;
                    // pNode is either null now if it not of the right type
                    // or it is not null. then we can exit the loop
                    // if the index is below zero we folund nothing
                } while ((getIndex >= 0) && (pNode == null));
            }
            return pNode;
        }
    }

    /**
     * Checks whether the given pNode is from the right type. It returns the pNode (or it's parent from the right type)
     * if the check is successful and null if not.
     *
     * @param   pNode         the pNode to check
     * @param   validClasses  an array of valid classe
     * @param   xPos          x Position for the pick
     * @param   yPos          y Position for the pick
     *
     * @return  DOCUMENT ME!
     */
    private static PNode getRightPNodeOrNull(final PNode pNode,
            final Class[] validClasses,
            final double xPos,
            final double yPos) {
        for (int i = 0; i < validClasses.length; ++i) {
            if ((pNode != null) && validClasses[i].isAssignableFrom(pNode.getClass())
                        && (pNode.getParent() != null) && pNode.getParent().getVisible()
                        && pNode.getVisible()) {
                if ((pNode instanceof PPath)
                            && (!isPolygon((PPath)pNode)
                                || ((PPath)pNode).getPathReference().contains(xPos, yPos))) {
                    return pNode;
                }
            } else if ((validClasses[i] == PFeature.class) && (pNode != null)
                        && ParentNodeIsAPFeature.class.isAssignableFrom(pNode.getClass())
                        && (pNode.getParent() != null) && pNode.getParent().getVisible()
                        && pNode.getVisible()) {
                final PNode parentPNode = getPFeatureByChild((ParentNodeIsAPFeature)pNode);
                if (parentPNode != null) {
                    return parentPNode;
                }
            }
        }
        return null;
    }

    /**
     * Checks, if the given geometry contains a polygon.
     *
     * @param   o  the Geometry to check
     *
     * @return  true, iff the given PPath is an instanec of PPfeature and contains a Polygon or Multipolygon
     */
    private static boolean isPolygon(final PPath o) {
        if ((o instanceof PFeature) && (((PFeature)o).getFeature() != null)) {
            final PFeature feature = (PFeature)o;

            return (feature.getFeature().getGeometry() instanceof Polygon)
                        || (feature.getFeature().getGeometry() instanceof MultiPolygon);
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PNode> getAllValidObjectsUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses) {
        return getValidObjectsUnderPointer(pInputEvent, validClasses, 0.003d, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   child  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public static PFeature getPFeatureByChild(final ParentNodeIsAPFeature child) {
        final PNode parent = ((PNode)child).getParent();
        if (parent instanceof PFeature) {
            return (PFeature)parent;
        } else if (parent instanceof ParentNodeIsAPFeature) {
            return getPFeatureByChild((ParentNodeIsAPFeature)parent);
        } else {
            throw new IllegalArgumentException("ParentNodeIsAPFeature " + child
                        + " has no ParentNode that is a PFeature"); // NOI18N
        }
    }

    /**
     * TODO move to a static geometryutils class.
     *
     * @param   pfeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Point2D centroid(final PFeature pfeature) {
        double cx = 0;
        double cy = 0;

        // TODO centroid wirklich über alle ringe berechnen ?
        for (int entityIndex = 0; entityIndex < pfeature.getNumOfEntities(); entityIndex++) {
            for (int ringIndex = 0; ringIndex < pfeature.getNumOfRings(entityIndex); ringIndex++) {
                final float[] xp = pfeature.getXp(entityIndex, ringIndex);
                final float[] yp = pfeature.getYp(entityIndex, ringIndex);
                final int n = xp.length;

                for (int i = 0; i < n; i++) {
                    final int j = (i + 1) % n;
                    final double factor = (xp[i] * yp[j]) - (xp[j] * yp[i]);
                    cx += (xp[i] + xp[j]) * factor;
                    cy += (yp[i] + yp[j]) * factor;
                }

                final double factor = 1 / (6.0f * area(pfeature));
                cx *= factor;
                cy *= factor;
            }
        }
        return new Point2D.Double(cx, cy);
    }

    /**
     * TODO move to a static geometryutils class.
     *
     * @param   pfeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static double area(final PFeature pfeature) {
        double areaTotal = 0;

        for (int entityIndex = 0; entityIndex < pfeature.getNumOfEntities(); entityIndex++) {
            for (int ringIndex = 0; ringIndex < pfeature.getNumOfRings(entityIndex); ringIndex++) {
                final float[] xp = pfeature.getXp(entityIndex, ringIndex);
                final float[] yp = pfeature.getYp(entityIndex, ringIndex);
                final int n = xp.length;

                double area = 0;
                for (int i = 0; i < n; i++) {
                    final int j = (i + 1) % n;
                    area += xp[i] * yp[j];
                    area -= xp[j] * yp[i];
                }
                area /= 2f;

                if (ringIndex == 0) { // polygon außenhülle
                    areaTotal += area;
                } else {              // loch
                    areaTotal -= area;
                }
            }
        }
        return areaTotal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pFeatures          DOCUMENT ME!
     * @param   thresholdInMeters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PFeatureCoordinateInformation> identifyMergeableCoordinates(final Collection<PFeature> pFeatures,
            final double thresholdInMeters) {
        final List<PFeatureCoordinateInformation> groupedInfos = new ArrayList<>();

        // collect all coordinate infos of all pfeatures
        final List<PFeatureCoordinateInformation> infos = new ArrayList<>();
        for (final PFeature pFeature : pFeatures) {
            final Geometry geom = pFeature.getFeature().getGeometry();
            for (int entityPosition = 0; entityPosition < pFeature.getNumOfEntities(); entityPosition++) {
                for (int ringPosition = 0; ringPosition < pFeature.getNumOfRings(entityPosition); ringPosition++) {
                    final int numOfCoordinates;
                    if ((geom instanceof Polygon) || (geom instanceof MultiPolygon)) {
                        // workaround for not removing last point of a polygon
                        numOfCoordinates = pFeature.getNumOfCoordinates(entityPosition, ringPosition) - 1;
                    } else {
                        numOfCoordinates = pFeature.getNumOfCoordinates(entityPosition, ringPosition);
                    }

                    for (int coordPosition = 0; coordPosition < numOfCoordinates; coordPosition++) {
                        final PFeatureCoordinateInformation info = new PFeatureCoordinateInformation(
                                pFeature,
                                entityPosition,
                                ringPosition,
                                coordPosition);
                        if (info.getCoordinate() != null) {
                            infos.add(info);
                        }
                    }
                }
            }
        }

        // calculate all neighbours of all coordinates of all pfeatures
        for (int i = 0; i < infos.size(); i++) {
            final PFeatureCoordinateInformation infoA = infos.get(i);
            final Coordinate coordA = infoA.getCoordinate();
            for (int j = i + 1; j < infos.size(); j++) {
                final PFeatureCoordinateInformation infoB = infos.get(j);
                final Coordinate coordB = infoB.getCoordinate();
                if (coordA.distance(coordB) < thresholdInMeters) {
                    infoA.getNeighbourInfos().add(infoB);
                    infoB.getNeighbourInfos().add(infoA);
                }
            }
        }

        // removing all zero-distance-only neighbours
        for (final PFeatureCoordinateInformation info : new ArrayList<>(infos)) {
            boolean onlyZeroDistance = true;

            final Coordinate coordinate = info.getCoordinate();

            final PFeature pFeature = info.getPFeature();
            for (final PFeatureCoordinateInformation neighbourInfo : info.getNeighbourInfos()) {
                final PFeature neighbourPFeature = neighbourInfo.getPFeature();
                final Coordinate neighbourCoordinate = neighbourInfo.getCoordinate();

                if (neighbourPFeature.equals(pFeature)) {
                    onlyZeroDistance = false;
                    break;
                }
                final double distance = neighbourCoordinate.distance(coordinate);
                if (distance > 0) {
                    onlyZeroDistance = false;
                    break;
                }
            }
            if (onlyZeroDistance) {
                infos.remove(info);
            }
        }

        // group all infos together by coordinate distance, prioritizing the coordinates with the most neighbours
        while (!infos.isEmpty()) {
            Collections.sort(infos);
            final PFeatureCoordinateInformation firstInfo = infos.get(0);
            if (!firstInfo.getNeighbourInfos().isEmpty()) {
                groupedInfos.add(firstInfo);
                for (final PFeatureCoordinateInformation neighbourInfo : firstInfo.getNeighbourInfos()) {
                    neighbourInfo.getNeighbourInfos().remove(firstInfo);
                }
            }
            infos.remove(firstInfo);
        }

        return groupedInfos;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pFeatures          DOCUMENT ME!
     * @param   thresholdInMeters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PFeatureCoordinateInformation> automergeCoordinates(final Collection<PFeature> pFeatures,
            final double thresholdInMeters) {
        return automergeCoordinates(identifyMergeableCoordinates(pFeatures, thresholdInMeters));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   groupedInfos  pFeatures DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<PFeatureCoordinateInformation> automergeCoordinates(
            final List<PFeatureCoordinateInformation> groupedInfos) {
        final List<PFeatureCoordinateInformation> unmergedInfos = new ArrayList<>();

        // calculating the centroid of all grouped infos, and setting the coordinates to the centroid
        final MultiMap coordinateRemoveMap = new MultiMap();
        final Set<PFeature> pFeatureToSync = new HashSet<>();

        for (final PFeatureCoordinateInformation info : groupedInfos) {
            final PFeature pFeature = info.getPFeature();
            final Coordinate coordinate = info.getCoordinate();
            final int entityPosition = info.getEntityPosition();
            final int ringPosition = info.getRingPosition();
            final int coordinatePosition = info.getCoordinatePosition();

            // calculating centroid
            final CentroidPoint centroidPoint = new CentroidPoint();

            try {
                centroidPoint.add(coordinate);
                for (final PFeatureCoordinateInformation neighbourInfo : info.getNeighbourInfos()) {
                    final Coordinate neighbourCoordinate = neighbourInfo.getCoordinate();
                    centroidPoint.add(neighbourCoordinate);
                }

                final Coordinate centroid = centroidPoint.getCentroid();

                // setting centroid to group "parent"
                LOG.info("setting centroid to group parent. before: "
                            + coordinate + " after:" + centroid);
                if (pFeature.moveCoordinate(entityPosition, ringPosition, coordinatePosition, centroid, false)) {
                    // setting centroid to group neighbours

                    for (final PFeatureCoordinateInformation neighbourInfo : info.getNeighbourInfos()) {
                        final PFeature neighbourPFeature = neighbourInfo.getPFeature();
                        final int neighbourEntityPosition = neighbourInfo.getEntityPosition();
                        final int neighbourRingPosition = neighbourInfo.getRingPosition();
                        final int neighbourCoordinatePosition = neighbourInfo.getCoordinatePosition();
                        final Coordinate neighbourCoordinate = neighbourInfo.getCoordinate();

                        if (neighbourPFeature.equals(pFeature)
                                    && (neighbourEntityPosition == entityPosition)
                                    && (neighbourRingPosition == ringPosition)) {
                            // it's on the same ring of the same pfeature, the coordinate can be removed
                            LOG.info("it's on the same ring of the same pfeature, the coordinate can be removed");

                            coordinateRemoveMap.put(neighbourPFeature, neighbourInfo);
                        } else {
                            LOG.info("setting centroid to group neighbour. before: " + neighbourCoordinate + " after:"
                                        + centroid);
                            if (neighbourPFeature.moveCoordinate(
                                            neighbourEntityPosition,
                                            neighbourRingPosition,
                                            neighbourCoordinatePosition,
                                            centroid,
                                            true)) {
                                pFeatureToSync.add(neighbourPFeature);
                            } else {
                                LOG.warn("cant move coordinate of  " + neighbourPFeature
                                            + ". It would result in an invalid geometry. coordinate: " + centroid);
                                unmergedInfos.add(neighbourInfo);
                            }
                        }
                    }
                    pFeatureToSync.add(pFeature);
                } else {
                    LOG.warn("cant move coordinate of  " + pFeature
                                + ". It would result in an invalid geometry. coordinate: " + centroid);
                    unmergedInfos.add(info);
                }
            } catch (final Exception ex) {
                LOG.warn("could not update all coordinates", ex);
            }
        }

        // removing now duplicate coordinates from pfeatures
        // ( in reverse coordinatePosition order, to avoid position-shifting-problems
        // when removing multiple coordinates from the same pfeature )
        for (final PFeature pFeature : (Set<PFeature>)coordinateRemoveMap.keySet()) {
            final Set<PFeatureCoordinateInformation> coordinateRemoveSet = new HashSet<>((Collection)
                    coordinateRemoveMap.get(pFeature));
            final List<PFeatureCoordinateInformation> coordinateRemoveInfos = new ArrayList<>(coordinateRemoveSet);
            Collections.sort(coordinateRemoveInfos, new Comparator<PFeatureCoordinateInformation>() {

                    @Override
                    public int compare(final PFeatureCoordinateInformation o1, final PFeatureCoordinateInformation o2) {
                        if (o1.getEntityPosition() != o2.getEntityPosition()) {
                            return -Integer.compare(o1.getEntityPosition(), o2.getEntityPosition());
                        } else if (o1.getRingPosition() != o2.getRingPosition()) {
                            return -Integer.compare(o1.getRingPosition(), o2.getRingPosition());
                        } else if (o1.getCoordinatePosition() != o2.getCoordinatePosition()) {
                            return -Integer.compare(o1.getCoordinatePosition(), o2.getCoordinatePosition());
                        } else {
                            return 0;
                        }
                    }
                });

            for (final PFeatureCoordinateInformation info : coordinateRemoveInfos) {
                final int entityPosition = info.getEntityPosition();
                final int ringPosition = info.getRingPosition();
                final int coordinatePosition = info.getCoordinatePosition();

                // but check before if the minimum number of coordinates for the given geometry type is
                // respected
                final Geometry geom = pFeature.getFeature().getGeometry();
                final int minCoordinates;
                if ((geom instanceof Polygon) || (geom instanceof MultiPolygon)) {
                    minCoordinates = 3;
                } else if ((geom instanceof LineString) || (geom instanceof MultiLineString)) {
                    minCoordinates = 2;
                } else {
                    minCoordinates = 0;
                }

                final int numOfCoords = pFeature.getNumOfCoordinates(entityPosition, ringPosition);
                if ((numOfCoords > minCoordinates)
                            && pFeature.removeCoordinate(
                                entityPosition,
                                ringPosition,
                                coordinatePosition,
                                false)) {
                    pFeatureToSync.add(pFeature);
                } else {
                    LOG.warn("cant remove coordinate from  " + pFeature
                                + ". It would result in an invalid geometry.");
                    unmergedInfos.add(info);
                }
            }
        }

        // update and sync
        for (final PFeature pFeature : pFeatureToSync) {
            pFeature.updatePath();
            pFeature.syncGeometry();
        }

        return unmergedInfos;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @AllArgsConstructor
    public static class PFeatureCoordinateInformation implements Comparable<PFeatureCoordinateInformation> {

        //~ Instance fields ----------------------------------------------------

        private final Collection<PFeatureCoordinateInformation> neighbourInfos = new ArrayList();
        private final PFeature pFeature;
        private final int entityPosition;
        private final int ringPosition;
        private final int coordinatePosition;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Coordinate getCoordinate() {
            return pFeature.getCoordinate(entityPosition, ringPosition, coordinatePosition);
        }

        @Override
        public int compareTo(final PFeatureCoordinateInformation o) {
            return Integer.compare(getNeighbourInfos().size(), o.getNeighbourInfos().size());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = (13 * hash) + Objects.hashCode(this.pFeature);
            hash = (13 * hash) + this.entityPosition;
            hash = (13 * hash) + this.ringPosition;
            hash = (13 * hash) + this.coordinatePosition;
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PFeatureCoordinateInformation other = (PFeatureCoordinateInformation)obj;
            if (this.entityPosition != other.entityPosition) {
                return false;
            }
            if (this.ringPosition != other.ringPosition) {
                return false;
            }
            if (this.coordinatePosition != other.coordinatePosition) {
                return false;
            }
            if (!Objects.equals(this.pFeature, other.pFeature)) {
                return false;
            }
            return true;
        }
    }
}
