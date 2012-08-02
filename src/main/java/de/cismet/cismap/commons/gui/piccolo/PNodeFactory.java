/*
 * PNodeFactory.java
 *
 * Created on 4. M\u00E4rz 2005, 14:51
 */

package de.cismet.cismap.commons.gui.piccolo;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;



/**
 *
 * @author hell
 */
public class PNodeFactory {
    
    public static edu.umd.cs.piccolo.PNode createPFeature(Feature feature,MappingComponent viewer) {
        return createPFeature(feature,null,0,0,viewer);
    }
//    public static FeaturePNode updatePNode(PNode pnode,org.deegree_impl.graphics.transformation.WorldToScreenTransform wtst,double x_offset,double y_offset) {
//        return null;
//    }
    public static PFeature createPFeature(Feature feature,WorldToScreenTransform wtst,double x_offset,double y_offset,MappingComponent viewer) {
        if (feature != null) {
            return new PFeature(feature,wtst,x_offset,y_offset,viewer);
        }
        else {
            return null;
        }
    }
    
    
//    public static FeaturePNode createPNode(de.cismet.mapping.Feature feature,org.deegree_impl.graphics.transformation.WorldToScreenTransform wtst,double x_offset,double y_offset) {
//        if (feature != null) {
//            Geometry geom=feature.getGeometry();
//            if (geom instanceof Polygon) {
//            //TODO:L\u00F6cher werden noch nicht verarbeitet
//                Polygon p=(Polygon)geom;
//                Coordinate[] coordArr=p.getExteriorRing().getCoordinates();
//                //coordArr[1].
//                float[] xp=new float[coordArr.length];
//                float[] yp=new float[coordArr.length];
//                for (int i=0;i<coordArr.length;++i) {
//                    if (wtst==null) {
//                        xp[i]=(float)(coordArr[i].x+x_offset);
//                        yp[i]=(float)(coordArr[i].y+y_offset);
//                    }
//                    else {
//                        xp[i]=(float)(wtst.getDestX(coordArr[i].x)+x_offset);
//                        yp[i]=(float)(wtst.getDestY(coordArr[i].y)+y_offset);
//                    }
//                }
//                PPath pn=PPath.createPolyline(xp,yp);
//                pn.setStroke(new FixedWidthStroke());
//                //TODO:Wenn feature=styledFeature jetzt noch Anpassungen machen
//                if ((feature instanceof StyledFeature)) {
//                    java.awt.Paint paint=((StyledFeature)feature).getFillingStyle();
//                    if (paint!=null) {
//                        pn.setPaint(paint);
//                    }
//                }
//                //TODO:Wenn feature=labeledFeature jetzt noch Anpassungen machen
//                if ((feature instanceof LabeledStyledFeature)) {
//                    //Mach ich im Moment \u00FCber den ToolTip
//                }
//
//
//
//
//                ((PPath)pn).setStroke(new FixedWidthStroke());
//                FeaturePNode fpn=new FeaturePNode();
//                fpn.addChild(pn);
//                fpn.setFeature(feature);
//                //fpn.setStroke(new FixedWidthStroke());
//                return fpn;
//            }
//            else {
//                if (feature.getGeometry()!=null) {
//                    System.err.println("Kann im Moment nur Polygon ("+feature.getGeometry().getGeometryType()+")");
//                }else {
//                    System.err.println("Feature ist NULL");
//                }
//
//            }
//            return null;
//        }
//        else {
//            return null;
//        }
//    }
    
}
