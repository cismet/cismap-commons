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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

import de.cismet.cismap.commons.WorldToScreenTransform;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class PureNewFeature extends AbstractNewFeature implements Cloneable,
    XStyledFeature,
    Attachable,
    PreventNamingDuplicates {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PureNewFeature.class);

    //~ Instance fields --------------------------------------------------------

    int number = 0;

    private Paint fillingPaint = new Color(1f, 0f, 0f, 0.4f);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  g  DOCUMENT ME!
     */
    public PureNewFeature(final Geometry g) {
        super(g);
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  point  DOCUMENT ME!
     * @param  wtst   DOCUMENT ME!
     */
    public PureNewFeature(final Point2D point, final WorldToScreenTransform wtst) {
        super(point, wtst);
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  canvasPoints  DOCUMENT ME!
     * @param  wtst          DOCUMENT ME!
     */
    public PureNewFeature(final Point2D[] canvasPoints, final WorldToScreenTransform wtst) {
        super(canvasPoints, wtst);
    }

    /**
     * Creates a new PureNewFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    public PureNewFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
        super(coordArr, wtst);
    }

    //~ Methods ----------------------------------------------------------------

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
    public String getOriginalName() {
        final String name = super.getName();
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
                if (name.trim().equals("")) {                                 // NOI18N
                    if (getGeometry() instanceof Point) {
                        setName(org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPoint"));        // NOI18N
                    } else if (getGeometry() instanceof LineString) {
                        setName(org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPolyline"));     // NOI18N
                    } else if (getGeometry() instanceof Polygon) {
                        setName(org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newPolygon"));      // NOI18N
                    } else if (getGeometry() instanceof MultiPolygon) {
                        setName(org.openide.util.NbBundle.getMessage(
                                PureNewFeature.class,
                                "PureNewFeature.getName().newMultiPolygon")); // NOI18N
                    } else {
                        setName("-");                                         // NOI18N
                    }
                }
                return super.getName();
            } catch (Exception e) {
                LOG.fatal("getName() error", e);                              // NOI18N
                return "Error in getName()";                                  // NOI18N
            }
        }
    }
    @Override
    public String getName() {
        if (number == 0) {
            return getOriginalName();
        } else {
            return getOriginalName() + " - " + number;
        }
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(final int n) {
        number = n;
    }
}
