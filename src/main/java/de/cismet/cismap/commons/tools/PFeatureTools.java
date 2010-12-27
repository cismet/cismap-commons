/*
 * PFeatureTools.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 8. August 2005, 12:47
 *
 */

package de.cismet.cismap.commons.tools;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.ParentNodeIsAPFeature;
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

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class PFeatureTools {
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.tools.PFeatureTools");//NOI18N
        public static PFeature[] getPFeaturesInArea(MappingComponent mc, final PBounds bounds) {
        ArrayList al=new ArrayList();
        mc.getFeatureLayer().findIntersectingNodes(bounds, al);


 
        for (int i=0;i<mc.getMapServiceLayer().getChildrenCount();++i) {
            PNode p=mc.getMapServiceLayer().getChild(i);
            if (p instanceof PLayer){
                mc.getMapServiceLayer().getChild(i).findIntersectingNodes(bounds, al);
            }
        }
        Iterator it=al.iterator();
       

        Vector<PFeature> vRet=new Vector<PFeature>();
        it=al.iterator();
        int i=0;
        while (it.hasNext()) {
            Object next=null;
            next=it.next();
            if (next instanceof PFeature) {
                PFeature pf=(PFeature)next;
                vRet.add(pf);
            }
            else {
                //log.fatal(next.getClass()+" ist nicht vom Typ PFeature ("+next+")");
            }

        }
        return vRet.toArray(new PFeature[0]);

    }
 
    public static Point2D[] getPointsInArea(MappingComponent mc, PBounds bounds) {
        PFeature[] features=getPFeaturesInArea(mc,bounds);
        Vector points=new Vector();
        if (features==null)return null;
        for (int i=0;i<features.length;++i) {
            float[] xp=features[i].getXp();
            float[] yp=features[i].getYp();
            for (int j=0;j<xp.length;++j) {
                if (bounds.contains(xp[j],yp[j])) {
                    points.add(new Point2D.Float(xp[j],yp[j]));
                }
            }
        }
        return (Point2D[])points.toArray(new Point2D[points.size()]);
    }
    
    
    private static Point2D getNearestPointInArea(MappingComponent mc, PBounds bounds,Point2D myPosition, Point2D vetoPoint) {
        Point2D[] points=getPointsInArea(mc,bounds);
        double distance=-1;
        Point2D nearestPoint=null;
        for (int i=0;i<points.length;++i) {
            if (vetoPoint!=null&&points[i].equals(vetoPoint)) {
                break;
            }
            double distanceCheck=myPosition.distanceSq(points[i]);
            if (distance<0||distanceCheck<distance) {
                nearestPoint=points[i];
                distance=distanceCheck;
            }
        }
        return nearestPoint;
    }
    
    public static Point2D getNearestPointInArea(MappingComponent mc, Point2D canvasPosition) {
        return getNearestPointInArea(mc,canvasPosition, null);
    }
    
    public static Point2D getNearestPointInArea(MappingComponent mc, Point2D canvasPosition, Point2D vetoPoint) {
        Rectangle2D area=new Rectangle((int)canvasPosition.getX()-mc.getSnappingRectSize()/2,(int)canvasPosition.getY()-mc.getSnappingRectSize()/2,mc.getSnappingRectSize(),mc.getSnappingRectSize());
        Rectangle2D d2d=mc.getCamera().localToView(new PBounds(area));
        Point2D myPosition=mc.getCamera().viewToLocal(canvasPosition);
        return getNearestPointInArea(mc,new PBounds(d2d), myPosition, vetoPoint);
    }
    public static Object getFirstValidObjectUnderPointer(PInputEvent pInputEvent,Class[] validClasses) {
        return getFirstValidObjectUnderPointer(pInputEvent, validClasses, 0.001d);
    }

    public static Object getFirstValidObjectUnderPointer(PInputEvent pInputEvent,Class[] validClasses,double halo) {
        //Dieses Konstrukt sorgt daf\u00FCr das uninteressante Objekte die oberhalb dem Mauszeiger liegen
        //einfach ignoriert werden
        Object o=null;
        boolean rightType=false;
        boolean first=true;
//        PPickPath pp=pInputEvent.getInputManager().getMouseOver();
        
        double xPos=pInputEvent.getPosition().getX();
        double yPos=pInputEvent.getPosition().getY();
        
        PPickPath pp=((MappingComponent)pInputEvent.getComponent()).getCamera().pick(pInputEvent.getCanvasPosition().getX(),pInputEvent.getCanvasPosition().getY(),halo);
        
        do {
            if (first) {
                o=pp.getPickedNode();
                first=false;
            } else {
                o=pp.nextPickedNode();
            }
//            if (o!=null && o instanceof PPath && !((PPath)o).getPathReference().contains(xPos,yPos)) {
//                //In diesem Fall handelte es sich zwar um ein PPATH aber x,y war nicht im PPath enthalten, deshalb mach nix
//
//            } else
            // durch dieses if wird genaues selektieren erreicht
            {
                for (int i=0;i<validClasses.length;++i) {
//                    if (o!=null) log.debug("_ getFirstValidObjectUnderPointer teste "+o.getClass()+ ":"+validClasses[i].getName()+" :"+ validClasses[i].isAssignableFrom(o.getClass()));
                    if (o!=null&&validClasses[i].isAssignableFrom(o.getClass())&&((PNode)o).getParent()!=null&&((PNode)o).getParent().getVisible()) {
                        if (o!=null && o instanceof PPath && ((PPath)o).getPathReference().contains(xPos,yPos)) {
                            rightType=true;
                            break;
                        }
                    } else if (validClasses[i]==PFeature.class &&o!=null&&ParentNodeIsAPFeature.class.isAssignableFrom(o.getClass())&&((PNode)o).getParent()!=null&&((PNode)o).getParent().getVisible()&&((PNode)o).getVisible()){
                        o=getPFeatureByChild((ParentNodeIsAPFeature)o);
                        if (o!=null) {
                            rightType=true;
                            break;
                        }
                    }
                }
            }
        }
        while (o !=null && !rightType);
        return o;
    }
    public static LinkedList getAllValidObjectsUnderPointer(PInputEvent pInputEvent,Class[] validClasses) {
        Object o=null;
        boolean first=true;
        LinkedList v=new LinkedList();
        PPickPath pp=((MappingComponent)pInputEvent.getComponent()).getCamera().pick(pInputEvent.getCanvasPosition().getX(),pInputEvent.getCanvasPosition().getY(),0.001d);
        double xPos=pInputEvent.getPosition().getX();
        double yPos=pInputEvent.getPosition().getY();
        do {
            if (first) {
                o=pp.getPickedNode();
                first=false;
            } else {
                o=pp.nextPickedNode();
            }
            if (o!=null && o instanceof PPath && !((PPath)o).getPathReference().contains(xPos,yPos)) {
                //In diesem Fall handelte es sich zwar um ein PPATH aber x,y war nicht im PPath enthalten, deshalb mach nix
                
            } else {
                for (int i=0;i<validClasses.length;++i) {
                    //if (o!=null) log.debug("_ getFirstValidObjectUnderPointer teste "+o.getClass()+ ":"+validClasses[i].getName()+" :"+ validClasses[i].isAssignableFrom(o.getClass()));
                    if (o!=null&&validClasses[i].isAssignableFrom(o.getClass())&&((PNode)o).getParent()!=null&&((PNode)o).getParent().getVisible()) {
                        v.add(o);
                        break;
                    } else if (validClasses[i]==PFeature.class &&o!=null&&ParentNodeIsAPFeature.class.isAssignableFrom(o.getClass())&&((PNode)o).getParent()!=null&&((PNode)o).getParent().getVisible()){
                        o=getPFeatureByChild((ParentNodeIsAPFeature)o);
                        if (o!=null) {
                            v.add(o);
                            break;
                        }
                    }
                }
            }
        }
        while (o !=null);
        return v;
    }
    
    
    
    public static PFeature getPFeatureByChild(ParentNodeIsAPFeature child) {
        PNode parent=((PNode)child).getParent();
        if ( parent instanceof PFeature) {
            return (PFeature)parent;
        } else if (parent instanceof ParentNodeIsAPFeature) {
            return getPFeatureByChild((ParentNodeIsAPFeature)parent);
        } else {
            throw new  IllegalArgumentException("ParentNodeIsAPFeature "+child +" has no ParentNode that is a PFeature");//NOI18N
        }
    }
    
}
