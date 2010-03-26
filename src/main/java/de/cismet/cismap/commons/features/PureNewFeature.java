/*
 * PureNewFeature.java
 *
 * Created on 19. April 2005, 10:54
 */
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author hell
 */
public class PureNewFeature extends DefaultStyledFeature implements Cloneable, XStyledFeature {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public static enum geomTypes {

        ELLIPSE, LINESTRING, RECTANGLE, POINT, POLYGON
    };
    private Paint fillingPaint;
    private geomTypes geomType;
    static ImageIcon icoPoint = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/point.png"));//NOI18N
    static ImageIcon icoPolyline = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/polyline.png"));//NOI18N
    static ImageIcon icoPolygon = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/polygon.png"));//NOI18N
    static ImageIcon icoEllipse = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/ellipse.png"));//NOI18N
    static ImageIcon icoRectangle = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/rectangle.png"));//NOI18N
    private String name = "";//NOI18N

    public PureNewFeature(Geometry g) {
        setGeometry(g);
    }

    public PureNewFeature(Point2D point, WorldToScreenTransform wtst) {
        Coordinate[] coordArr = new Coordinate[1];
        coordArr[0] = new Coordinate(wtst.getSourceX(point.getX()), wtst.getSourceY(point.getY()));
        init(coordArr, wtst);
    }

    public PureNewFeature(final Point2D[] canvasPoints, WorldToScreenTransform wtst) {
        synchronized (canvasPoints) {
            try {
                log.debug("canvasPoints " + canvasPoints);//NOI18N
                Coordinate[] coordArr = new Coordinate[canvasPoints.length];
                float[] xp = new float[canvasPoints.length];
                float[] yp = new float[canvasPoints.length];
                for (int i = 0; i < canvasPoints.length; ++i) {
                    log.debug("canvasPoints[" + i + "]:" + canvasPoints[i]);//NOI18N
                    xp[i] = (float) (canvasPoints[i].getX());
                    yp[i] = (float) (canvasPoints[i].getY());
                    coordArr[i] = new Coordinate(wtst.getSourceX(xp[i]), wtst.getSourceY(yp[i]));
                }
                init(coordArr, wtst);
                log.debug("pureNewFeature created");//NOI18N
            } catch (Exception e) {
                log.error("Error during creating a PureNewfeatures", e);//NOI18N
            }
        }
    }

    public PureNewFeature(Coordinate[] coordArr, WorldToScreenTransform wtst) {
        init(coordArr, wtst);
    }

    private void init(Coordinate[] coordArr, WorldToScreenTransform wtst) {
        try {
            GeometryFactory gf = new GeometryFactory();
            //TODO Im Moment nur f�r einfache Polygone ohne L�cher
            if (coordArr.length == 1) {
                //Point
                Point p = gf.createPoint(coordArr[0]);
                setGeometry(p);
            } else if (coordArr[0].equals(coordArr[coordArr.length - 1]) && coordArr.length > 3) {
                //simple Polygon
                LinearRing shell = gf.createLinearRing(coordArr);
                Polygon poly = gf.createPolygon(shell, null);
                setGeometry(poly);
            } else {
                //Linestring
                LineString line = gf.createLineString(coordArr);
                setGeometry(line);
            }
        } catch (Exception e) {
            log.warn("Error in init", e);//NOI18N
        }
        fillingPaint = new Color(1f, 0f, 0f, 0.4f);
    }

    public java.awt.Stroke getLineStyle() {
        return null;
    }

    public java.awt.Paint getFillingPaint() {
        return fillingPaint;
    }

    @Override
    public void setFillingPaint(Paint fillingStyle) {
        this.fillingPaint = fillingStyle;
    }

    public float getTransparency() {
        return 1f;
    }

    public String getType() {
        return "";//NOI18N
    }

    @Override
    public String getName() {
        if (getGeometryType() != null) {
            switch (getGeometryType()) {
                case RECTANGLE:
                    return org.openide.util.NbBundle.getMessage(PureNewFeature.class, "PureNewFeature.name.newRectangle") ;
                case LINESTRING:
                    return org.openide.util.NbBundle.getMessage(PureNewFeature.class, "PureNewFeature.name.newPolyline") ;
                case ELLIPSE:
                    return org.openide.util.NbBundle.getMessage(PureNewFeature.class, "PureNewFeature.name.newEllipse") ;
                case POINT:
                    return org.openide.util.NbBundle.getMessage(PureNewFeature.class, "PureNewFeature.name.newPoint") ;
                case POLYGON:
                    return org.openide.util.NbBundle.getMessage(PureNewFeature.class, "PureNewFeature.name.newPolygon") ;
                default:
                    return org.openide.util.NbBundle.getMessage(PureNewFeature.class, "PureNewFeature.name.errorInGetName") ;
            }
        } else {
            try {
                Vector<Feature> allFeatures = CismapBroker.getInstance().getMappingComponent().getFeatureCollection().getAllFeatures();
//
//        int countNewPoints=1; 
//        int countNewLines=1;
//        int countNewPoly=1;
//
//        for (Feature f:allFeatures){
//            if (f instanceof PureNewFeature ){
//                if (((PureNewFeature)f).name.startsWith("Neuer Punkt")){
//                    countNewPoints++;
//                }
//                else if (((PureNewFeature)f).name.startsWith("Neuer Linienzug")){
//                    countNewLines++;
//                }
//                else if (((PureNewFeature)f).name.startsWith("Neues Polygon")){
//                    countNewPoly++;
//                }
//
//            }
//        }
//
//        if (name.trim().equals("")) {
//            if (getGeometry() instanceof Point) {
//                name= "Neuer Punkt ("+countNewPoints+")";
//            } else if (getGeometry() instanceof LineString) {
//                name = "Neuer Linienzug ("+countNewLines+")";
//            } else {
//                name = "Neues Polygon ("+countNewPoly+")";
//            }
//        }


                if (name.trim().equals("")) {
                    if (getGeometry() instanceof Point) {
                        name = "Neuer Punkt";
                    } else if (getGeometry() instanceof LineString) {
                        name = "Neuer Linienzug";
                    } else {
                        name = "Neues Polygon";
                    }
                }
                return name;
            } catch (Exception e) {
                log.fatal("getName() error", e);
                return "Error in getName()";
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public JComponent getInfoComponent(Refreshable refresh) {
        return null;
    }

    public ImageIcon getIconImage() {
        if (getGeometry() instanceof Point) {
            return icoPoint;
        } else if (getGeometryType() == geomTypes.LINESTRING) {
            return icoPolyline;
        } else if (getGeometryType() == geomTypes.ELLIPSE) {
            return icoEllipse;
        } else if (getGeometryType() == geomTypes.RECTANGLE) {
            return icoRectangle;
        } else if (getGeometryType() == geomTypes.POLYGON) {
            return icoPolygon;
        } else {
            return null;
        }
    }

    public Paint getLinePaint() {
        Paint retValue;

        retValue = super.getLinePaint();
        return retValue;
    }

    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return null;
    }

    public float getInfoComponentTransparency() {
        return getTransparency();
    }

    public void setGeometryType(geomTypes geomType) {
        this.geomType = geomType;
    }

    public geomTypes getGeometryType() {
        return geomType;
    }
}
