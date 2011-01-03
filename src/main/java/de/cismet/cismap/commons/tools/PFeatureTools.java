/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.tools;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

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
     * @param   mc      DOCUMENT ME!
     * @param   bounds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PFeature[] getPFeaturesInArea(final MappingComponent mc, final PBounds bounds) {
        final ArrayList al = new ArrayList();
        mc.getFeatureLayer().findIntersectingNodes(bounds, al);

        for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
            final PNode p = mc.getMapServiceLayer().getChild(i);
            if (p instanceof PLayer) {
                mc.getMapServiceLayer().getChild(i).findIntersectingNodes(bounds, al);
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
                vRet.add(pf);
            } else {
                // log.fatal(next.getClass()+" ist nicht vom Typ PFeature ("+next+")");
            }
        }
        return vRet.toArray(new PFeature[0]);
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
        final Vector points = new Vector();
        if (features == null) {
            return null;
        }
        for (int i = 0; i < features.length; ++i) {
            final float[] xp = features[i].getXp();
            final float[] yp = features[i].getYp();
            for (int j = 0; j < xp.length; ++j) {
                if (bounds.contains(xp[j], yp[j])) {
                    points.add(new Point2D.Float(xp[j], yp[j]));
                }
            }
        }
        return (Point2D[])points.toArray(new Point2D[points.size()]);
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
                                && (((PNode)o).getParent() != null) && ((PNode)o).getParent().getVisible()) {
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
}
