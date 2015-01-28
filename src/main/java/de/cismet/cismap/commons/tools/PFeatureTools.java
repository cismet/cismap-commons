/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.tools;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.*;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.Feature;
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

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.tools.PFeatureTools"); // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object getFirstValidObjectUnderPointer2(final PInputEvent pInputEvent,
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
    public static Object getFirstValidObjectUnderPointer2(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
        final List allValids = (List)getFirstObjectsUnderPointer(pInputEvent, validClasses, halo);
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
    public static Collection<Object> getFirstObjectsUnderPointer(final PInputEvent pInputEvent,
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
    public static Collection<Object> getValidObjectsUnderPointer(final PInputEvent pInputEvent,
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
    public static LinkedList getValidObjectsUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo,
            final boolean stopAfterFirstValid) {
        final ArrayList al = new ArrayList();
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

        findIntersectingPFeatures(mc.getFeatureLayer(), point, al);

        for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
            final PNode p = mc.getMapServiceLayer().getChild(i);
            if (p instanceof PLayer) {
                findIntersectingPFeatures(mc.getMapServiceLayer().getChild(i), point, al);
            }
        }
        final Iterator it = al.iterator();
        Object o;
        final LinkedList allValids = new LinkedList();
        while (it.hasNext()) {
            o = it.next();
            for (int i = 0; i < validClasses.length; ++i) {
                if ((o != null) && validClasses[i].isAssignableFrom(o.getClass()) && (((PNode)o).getParent() != null)
                            && ((PNode)o).getParent().getVisible() && ((PNode)o).getVisible()) {
                    allValids.add(o);
                    if (stopAfterFirstValid) {
                        break;
                    }
                }
            }
        }
        return allValids;
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
        final ArrayList al = new ArrayList();
        final WorldToScreenTransform wtst = mc.getWtst();

        findIntersectingPFeatures(mc.getFeatureLayer(), geom, al);

        for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
            final PNode p = mc.getMapServiceLayer().getChild(i);
            if (p instanceof PLayer) {
                findIntersectingPFeatures(mc.getMapServiceLayer().getChild(i), geom, al);
            }
        }
        Iterator it = al.iterator();

        final Vector<PFeature> vRet = new Vector<PFeature>();
        it = al.iterator();
        int i = 0;
        while (it.hasNext()) {
            Object next = null;
            next = it.next();
            if (next instanceof PFeature) {
                final PFeature pf = (PFeature)next;
                if (pf.isSnappable()) {
                    vRet.add(pf);
                }
            } else {
                // log.fatal(next.getClass()+" ist nicht vom Typ PFeature ("+next+")");
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
     * @param  node      The node, the PFeatures should be find in
     * @param  geometry  the search geometry
     * @param  al        the list, the result should be added to.
     */
    public static void findIntersectingPFeatures(final PNode node, final Geometry geometry, final ArrayList al) {
        final List<PNode> children = node.getChildrenReference();
        final String srs = CrsTransformer.createCrsFromSrid(geometry.getSRID());

        for (final PNode entry : children) {
            if (entry instanceof PFeature) {
                Geometry featureGeometry = ((PFeature)entry).getFeature().getGeometry();

                if (featureGeometry.getSRID() != geometry.getSRID()) {
                    featureGeometry = CrsTransformer.transformToGivenCrs(featureGeometry, srs);
                }

                if (intersects(featureGeometry, geometry)) {
                    al.add(entry);
                }
            } else {
                findIntersectingPFeatures(entry, geometry, al);
            }
        }
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
                break;
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
    public static Object getFirstValidObjectUnderPointer(final PInputEvent pInputEvent, final Class[] validClasses) {
        return getFirstValidObjectUnderPointer(pInputEvent, validClasses, 1d);
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
    public static Object getFirstValidObjectUnderPointer(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
        // Dieses Konstrukt sorgt daf\u00FCr das uninteressante Objekte die oberhalb dem Mauszeiger liegen
        // einfach ignoriert werden
        Object o = null;
        boolean rightType = false;
        boolean first = true;
//        PPickPath pp=pInputEvent.getInputManager().getMouseOver();

        final double xPos = pInputEvent.getPosition().getX();
        final double yPos = pInputEvent.getPosition().getY();

        final PPickPath pp = ((MappingComponent)pInputEvent.getComponent()).getCamera()
                    .pick(pInputEvent.getCanvasPosition().getX(), pInputEvent.getCanvasPosition().getY(), halo);
        pp.pushNode(pInputEvent.getPickedNode());
        do {
            if (first) {
                o = pp.getPickedNode();
                first = false;
            } else {
                o = pp.nextPickedNode();
            }
//            if (o!=null && o instanceof PPath && !((PPath)o).getPathReference().contains(xPos,yPos)) {
//                //In diesem Fall handelte es sich zwar um ein PPATH aber x,y war nicht im PPath enthalten, deshalb mach nix
//
//            } else
            // durch dieses if wird genaues selektieren erreicht
            {
                for (int i = 0; i < validClasses.length; ++i) {
//                    if (o!=null) log.debug("_ getFirstValidObjectUnderPointer teste "+o.getClass()+ ":"+validClasses[i].getName()+" :"+ validClasses[i].isAssignableFrom(o.getClass()));
                    if ((o != null) && validClasses[i].isAssignableFrom(o.getClass())
                                && (((PNode)o).getParent() != null) && ((PNode)o).getParent().getVisible()
                                && ((PNode)o).getVisible()) {
                        if ((o instanceof PPath)
                                    && (!isPolygon((PPath)o) || ((PPath)o).getPathReference().contains(xPos, yPos))) {
                            rightType = true;
                            break;
                        }
                    } else if ((validClasses[i] == PFeature.class) && (o != null)
                                && ParentNodeIsAPFeature.class.isAssignableFrom(o.getClass())
                                && (((PNode)o).getParent() != null) && ((PNode)o).getParent().getVisible()
                                && ((PNode)o).getVisible()) {
                        o = getPFeatureByChild((ParentNodeIsAPFeature)o);
                        if (o != null) {
                            rightType = true;
                            break;
                        }
                    }
                }
            }
        } while ((o != null) && !rightType);
        return o;
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
    public static LinkedList getAllValidObjectsUnderPointer(final PInputEvent pInputEvent, final Class[] validClasses) {
        return getValidObjectsUnderPointer(pInputEvent, validClasses, 0.001d, false);
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
}
