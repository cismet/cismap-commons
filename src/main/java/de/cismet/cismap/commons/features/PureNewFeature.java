/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class PureNewFeature extends DefaultStyledFeature implements Cloneable, XStyledFeature, Attachable {

    //~ Static fields/initializers ---------------------------------------------

    static ImageIcon icoPoint = new javax.swing.ImageIcon(PureNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/point.png"));     // NOI18N
    static ImageIcon icoPolyline = new javax.swing.ImageIcon(PureNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/polyline.png"));  // NOI18N
    static ImageIcon icoPolygon = new javax.swing.ImageIcon(PureNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/polygon.png"));   // NOI18N
    static ImageIcon icoEllipse = new javax.swing.ImageIcon(PureNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/ellipse.png"));   // NOI18N
    static ImageIcon icoRectangle = new javax.swing.ImageIcon(PureNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/rectangle.png")); // NOI18N

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum geomTypes {

        //~ Enum constants -----------------------------------------------------

        ELLIPSE, LINESTRING, RECTANGLE, POINT, POLYGON, MULTIPOLYGON, UNKNOWN
    }

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Paint fillingPaint = new Color(1f, 0f, 0f, 0.4f);
    private geomTypes geomType = geomTypes.UNKNOWN;
    private String name = ""; // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  g  DOCUMENT ME!
     */
    public PureNewFeature(final Geometry g) {
        setGeometry(g);
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  point  DOCUMENT ME!
     * @param  wtst   DOCUMENT ME!
     */
    public PureNewFeature(final Point2D point, final WorldToScreenTransform wtst) {
        final Coordinate[] coordArr = new Coordinate[1];
        coordArr[0] = new Coordinate(wtst.getSourceX(point.getX()), wtst.getSourceY(point.getY()));
        init(coordArr, wtst);
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  canvasPoints  DOCUMENT ME!
     * @param  wtst          DOCUMENT ME!
     */
    public PureNewFeature(final Point2D[] canvasPoints, final WorldToScreenTransform wtst) {
        synchronized (canvasPoints) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("canvasPoints " + canvasPoints);                   // NOI18N
                }
                final Coordinate[] coordArr = new Coordinate[canvasPoints.length];
                final float[] xp = new float[canvasPoints.length];
                final float[] yp = new float[canvasPoints.length];
                for (int i = 0; i < canvasPoints.length; ++i) {
                    if (log.isDebugEnabled()) {
                        log.debug("canvasPoints[" + i + "]:" + canvasPoints[i]); // NOI18N
                    }
                    xp[i] = (float)(canvasPoints[i].getX());
                    yp[i] = (float)(canvasPoints[i].getY());
                    coordArr[i] = new Coordinate(wtst.getSourceX(xp[i]), wtst.getSourceY(yp[i]));
                }
                init(coordArr, wtst);
                if (log.isDebugEnabled()) {
                    log.debug("pureNewFeature created");                         // NOI18N
                }
            } catch (Exception e) {
                log.error("Error during creating a PureNewfeatures", e);         // NOI18N
            }
        }
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    public PureNewFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
        init(coordArr, wtst);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    private void init(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
        try {
            final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CrsTransformer.getCurrentSrid());
            // TODO Im Moment nur f�r einfache Polygone ohne L�cher
            if (coordArr.length == 1) {
                // Point
                final Point p = gf.createPoint(coordArr[0]);
                setGeometry(p);
            } else if (coordArr[0].equals(coordArr[coordArr.length - 1]) && (coordArr.length > 3)) {
                // simple Polygon
                final LinearRing shell = gf.createLinearRing(coordArr);
                final Polygon poly = gf.createPolygon(shell, null);
                setGeometry(poly);
            } else {
                // Linestring
                final LineString line = gf.createLineString(coordArr);
                setGeometry(line);
            }
        } catch (Exception e) {
            log.warn("Error in init", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public java.awt.Stroke getLineStyle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public java.awt.Paint getFillingPaint() {
        return fillingPaint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fillingStyle  DOCUMENT ME!
     */
    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        this.fillingPaint = fillingStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public float getTransparency() {
        return 1f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getType() {
        return ""; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getName() {
        if (getGeometryType() != null) {
            if ((name != null) && name.trim().equals("")) {
                switch (getGeometryType()) {
                    case RECTANGLE: {
                        return org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newRectangle");    // NOI18N
                    }
                    case LINESTRING: {
                        return org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPolyline");     // NOI18N
                    }
                    case ELLIPSE: {
                        return org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newEllipse");      // NOI18N
                    }
                    case POINT: {
                        return org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPoint");        // NOI18N
                    }
                    case POLYGON: {
                        return org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPolygon");      // NOI18N
                    }
                    case MULTIPOLYGON: {
                        return org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newMultiPolygon"); // NOI18N
                    }
                    default: {
//                        return org.openide.util.NbBundle.getMessage(
//                                PureNewFeature.class,
//                                "PureNewFeature.getName().errorInGetName");  // NOI18N
                        return getGeometryType().toString() + " " + getGeometry().getGeometryType();
                    }
                }
            } else {
                return name;
            }
        } else {
            try {
                if (name.trim().equals("")) {                                // NOI18N
                    if (getGeometry() instanceof Point) {
                        name = org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPoint");        // NOI18N
                    } else if (getGeometry() instanceof LineString) {
                        name = org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPolyline");     // NOI18N
                    } else if (getGeometry() instanceof Polygon) {
                        name = org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPolygon");      // NOI18N
                    } else if (getGeometry() instanceof MultiPolygon) {
                        name = org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newMultiPolygon"); // NOI18N
                    } else {
                        name = "-";                                          // NOI18N
                    }
                }
                return name;
            } catch (Exception e) {
                log.fatal("getName() error", e);                             // NOI18N
                return "Error in getName()";                                 // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   refresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
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
        } else if (getGeometryType() == geomTypes.MULTIPOLYGON) {
            return icoPolygon;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getLinePaint() {
        final Paint retValue;

        retValue = super.getLinePaint();
        return retValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getInfoComponentTransparency() {
        return getTransparency();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geomType  DOCUMENT ME!
     */
    public void setGeometryType(final geomTypes geomType) {
        if (geomType == null) {
            this.geomType = geomTypes.UNKNOWN;
        } else {
            this.geomType = geomType;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public geomTypes getGeometryType() {
        return geomType;
    }
}
