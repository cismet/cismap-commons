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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public abstract class AbstractNewFeature extends DefaultStyledFeature implements Cloneable, XStyledFeature, Attachable {

    //~ Static fields/initializers ---------------------------------------------

    static ImageIcon icoPoint = new javax.swing.ImageIcon(AbstractNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/point.png"));     // NOI18N
    static ImageIcon icoPolyline = new javax.swing.ImageIcon(AbstractNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/polyline.png"));  // NOI18N
    static ImageIcon icoPolygon = new javax.swing.ImageIcon(AbstractNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/polygon.png"));   // NOI18N
    static ImageIcon icoEllipse = new javax.swing.ImageIcon(AbstractNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/ellipse.png"));   // NOI18N
    static ImageIcon icoRectangle = new javax.swing.ImageIcon(AbstractNewFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/rectangle.png")); // NOI18N

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AbstractNewFeature.class);

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

    private Paint fillingPaint = new Color(1f, 0f, 0f, 0.4f);
    private geomTypes geomType = geomTypes.UNKNOWN;
    private String name = ""; // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  g  DOCUMENT ME!
     */
    public AbstractNewFeature(final Geometry g) {
        setGeometry(g);
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  point  DOCUMENT ME!
     * @param  wtst   DOCUMENT ME!
     */
    public AbstractNewFeature(final Point2D point, final WorldToScreenTransform wtst) {
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
    public AbstractNewFeature(final Point2D[] canvasPoints, final WorldToScreenTransform wtst) {
        synchronized (canvasPoints) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("canvasPoints " + canvasPoints);                   // NOI18N
                }
                final Coordinate[] coordArr = new Coordinate[canvasPoints.length];
                final float[] xp = new float[canvasPoints.length];
                final float[] yp = new float[canvasPoints.length];
                for (int i = 0; i < canvasPoints.length; ++i) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("canvasPoints[" + i + "]:" + canvasPoints[i]); // NOI18N
                    }
                    xp[i] = (float)(canvasPoints[i].getX());
                    yp[i] = (float)(canvasPoints[i].getY());
                    coordArr[i] = new Coordinate(wtst.getSourceX(xp[i]), wtst.getSourceY(yp[i]));
                }
                init(coordArr, wtst);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("pureNewFeature created");                         // NOI18N
                }
            } catch (Exception e) {
                LOG.error("Error during creating a PureNewfeatures", e);         // NOI18N
            }
        }
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    public AbstractNewFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
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
            LOG.warn("Error in init", e); // NOI18N
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
    public String getName() {
        return name;
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
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return null;
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
