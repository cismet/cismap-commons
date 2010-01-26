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
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author hell
 */
public class PureNewFeature extends DefaultStyledFeature implements Cloneable, XStyledFeature {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    static ImageIcon icoPoint = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/point.png"));
    static ImageIcon icoLinestring = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/linestring.png"));
    static ImageIcon icoPolygon = new javax.swing.ImageIcon(PureNewFeature.class.getResource("/de/cismet/cismap/commons/gui/res/polygon.png"));
    private static final ResourceBundle I18N = ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");
    private String name = "";

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
                log.debug("canvasPoints " + canvasPoints);
                Coordinate[] coordArr = new Coordinate[canvasPoints.length];
                float[] xp = new float[canvasPoints.length];
                float[] yp = new float[canvasPoints.length];
                for (int i = 0; i < canvasPoints.length; ++i) {
                    log.debug("canvasPoints["+i+"]:"+canvasPoints[i]);
                    xp[i] = (float) (canvasPoints[i].getX());
                    yp[i] = (float) (canvasPoints[i].getY());
                    coordArr[i] = new Coordinate(wtst.getSourceX(xp[i]), wtst.getSourceY(yp[i]));
                }
                init(coordArr, wtst);
                log.debug("pureNewFeature wurde angelegt");
            } catch (Exception e) {
                log.error("Fehler beim anlegen eines PureNewfeatures", e);
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
            log.warn("Fehler im init", e);
        }
    }

    public java.awt.Stroke getLineStyle() {
        return null;
    }

    public java.awt.Paint getFillingPaint() {
        try {
            return new Color(
                new Float(I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.getFillingPaint().RED")),
                new Float(I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.getFillingPaint().GREEN")),
                new Float(I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.getFillingPaint().BLUE")),
                new Float(I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.getFillingPaint().TRANSPARENT")));
        } catch (Exception e) {
            return new Color(1f, 0f, 0f, 0.4f);
        }
    }

    public float getTransparency() {
        return 1f;
    }

    public String getType() {
        return "";
    }

    public String getName() {
        try {
            Vector<Feature> allFeatures=CismapBroker.getInstance().getMappingComponent().getFeatureCollection().getAllFeatures();
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
                name= I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.name.neuerPunkt");
            } else if (getGeometry() instanceof LineString) {
                name = I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.name.neuerLinienzug");
            } else {
                name = I18N.getString("de.cismet.cismap.commons.features.PureNewFeature.name.neuesPolygon");
            }
        }
        return name;
        }
        catch (Exception e){
            log.fatal("getName() error",e);
            return "Error in getName()";
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
        } else if (getGeometry() instanceof LineString) {
            return icoLinestring;
        } else {
            return icoPolygon;
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

    
}
