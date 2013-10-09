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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.*;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.ParentNodeIsAPFeature;

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
     * @param   pInputEvent   mc DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     * @param   halo          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object getFirstValidObjectUnderPointer2(final PInputEvent pInputEvent,
            final Class[] validClasses,
            final double halo) {
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
        Object o = null;
        boolean rightType = false;
        do {
            if (!it.hasNext()) {
                return null;
            }
            o = it.next();
            for (int i = 0; i < validClasses.length; ++i) {
                if ((o != null) && validClasses[i].isAssignableFrom(o.getClass())) {
                    rightType = true;
                }
            }
        } while ((o != null) && !rightType);
        return o;
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
        final ArrayList al = new ArrayList();
        final WorldToScreenTransform wtst = mc.getWtst();
        final Geometry bBox = getGeometryFromPBounds(bounds, wtst, mc.getMappingModel().getSrs().getCode());

        findIntersectingPFeatures(mc.getFeatureLayer(), bBox, al);

        for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
            final PNode p = mc.getMapServiceLayer().getChild(i);
            if (p instanceof PLayer) {
                findIntersectingPFeatures(mc.getMapServiceLayer().getChild(i), bBox, al);
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

                if (featureGeometry.intersects(geometry)) {
                    al.add(entry);
                }
            } else {
                findIntersectingPFeatures(entry, geometry, al);
            }
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
        final Point2D myPosition = mc.getCamera().viewToLocal(canvasPosition);
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
        return getFirstValidObjectUnderPointer(pInputEvent, validClasses, 0.001d);
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
                        if ((o != null) && (o instanceof PPath) && ((PPath)o).getPathReference().contains(xPos, yPos)) {
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
     * DOCUMENT ME!
     *
     * @param   pInputEvent   DOCUMENT ME!
     * @param   validClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static LinkedList getAllValidObjectsUnderPointer(final PInputEvent pInputEvent, final Class[] validClasses) {
        Object o = null;
        boolean first = true;
        final LinkedList v = new LinkedList();
        final PPickPath pp = ((MappingComponent)pInputEvent.getComponent()).getCamera()
                    .pick(pInputEvent.getCanvasPosition().getX(), pInputEvent.getCanvasPosition().getY(), 0.001d);
        final double xPos = pInputEvent.getPosition().getX();
        final double yPos = pInputEvent.getPosition().getY();
        do {
            if (first) {
                o = pp.getPickedNode();
                first = false;
            } else {
                o = pp.nextPickedNode();
            }
            if ((o != null) && (o instanceof PPath) && !((PPath)o).getPathReference().contains(xPos, yPos)) {
                // In diesem Fall handelte es sich zwar um ein PPATH aber x,y war nicht im PPath enthalten, deshalb mach
                // nix
            } else {
                for (int i = 0; i < validClasses.length; ++i) {
                    // if (o!=null) log.debug("_ getFirstValidObjectUnderPointer teste "+o.getClass()+
                    // ":"+validClasses[i].getName()+" :"+ validClasses[i].isAssignableFrom(o.getClass()));
                    if ((o != null) && validClasses[i].isAssignableFrom(o.getClass())
                                && (((PNode)o).getParent() != null) && ((PNode)o).getParent().getVisible()) {
                        v.add(o);
                        break;
                    } else if ((validClasses[i] == PFeature.class) && (o != null)
                                && ParentNodeIsAPFeature.class.isAssignableFrom(o.getClass())
                                && (((PNode)o).getParent() != null) && ((PNode)o).getParent().getVisible()) {
                        o = getPFeatureByChild((ParentNodeIsAPFeature)o);
                        if (o != null) {
                            v.add(o);
                            break;
                        }
                    }
                }
            }
        } while (o != null);
        return v;
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
