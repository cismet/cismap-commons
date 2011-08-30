/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import edu.umd.cs.piccolo.util.PBounds;

import org.apache.log4j.Logger;


//import org.deegree.gml.GMLGeometry;
//import org.deegree.model.geometry.GM_Envelope;
//import org.deegree.model.geometry.GM_Exception;
//import org.deegree.model.geometry.GM_Object;
//import org.deegree_impl.model.geometry.GMLAdapter;
import org.jdom.DataConversionException;
import org.jdom.Element;

import java.io.Serializable;

/**
 * Class to store a simple BoundingBox.
 *
 * <p>This class should not be used. XBoundingBox should be used instead.</p>
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
@Deprecated
public class BoundingBox implements Cloneable, Serializable {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private double x1 = -1;
    private double y1 = -1;
    private double x2 = -1;
    private double y2 = -1;

    //~ Constructors -----------------------------------------------------------

// the classes GM_Object and GM_Envelope are not contained in the new cismetDeegree jar
// public BoundingBox(GMLGeometry geom) {
// try {
// GM_Object gmo=GMLAdapter.wrap(geom);
// GM_Envelope gme=gmo.getEnvelope();
// setX1(gme.getMin().getX());
// setX2(gme.getMax().getX());
// setY1(gme.getMin().getY());
// setY2(gme.getMax().getY());
// }
//
// catch (GM_Exception gmEx) {
// log.error("Error during creating BoundingBox from GML",gmEx);//NOI18N
// }
// }

    /**
     * Empty Constructor.
     */
    public BoundingBox() {
    }

    /**
     * Creates a new BoundingBox object.
     *
     * @param   parent  DOCUMENT ME!
     *
     * @throws  DataConversionException  DOCUMENT ME!
     */
    public BoundingBox(final Element parent) throws DataConversionException {
        final Element conf = parent.getChild("BoundingBox"); // NOI18N
        setX1(conf.getAttribute("x1").getDoubleValue());     // NOI18N
        setY1(conf.getAttribute("y1").getDoubleValue());     // NOI18N
        setX2(conf.getAttribute("x2").getDoubleValue());     // NOI18N
        setY2(conf.getAttribute("y2").getDoubleValue());     // NOI18N
    }

    /**
     * Creates a new BoundingBox object.
     *
     * @param  geom  DOCUMENT ME!
     */
    public BoundingBox(final Geometry geom) {
        final Geometry bb = geom.getEnvelope().buffer(0.000d);
        if (bb instanceof Point) {
            setX1(((Point)bb).getX());
            setX2(((Point)bb).getX());
            setY1(((Point)bb).getY());
            setY2(((Point)bb).getY());
        } else if (bb instanceof Polygon) {
            // minx,miny
            setX1(((Polygon)bb).getExteriorRing().getCoordinateN(0).x);
            setY1(((Polygon)bb).getExteriorRing().getCoordinateN(0).y);
            // maxx,maxy
            setX2(((Polygon)bb).getExteriorRing().getCoordinateN(2).x);
            setY2(((Polygon)bb).getExteriorRing().getCoordinateN(2).y);
        } else {
            log.fatal("BoundingBox was not created by jtsGeometry:" + geom); // NOI18N
        }
    }

    /**
     * Creates a new instance of BoundingBox.
     *
     * @param  x1  x coordinate of the lower left point
     * @param  y1  y coordinate of the lower left point
     * @param  x2  x coordinate of the upper right point
     * @param  y2  y coordinate of the upper right point
     */
    public BoundingBox(final double x1, final double y1, final double x2, final double y2) {
        if (x1 <= x2) {
            this.x1 = x1;
            this.x2 = x2;
        } else {
            this.x1 = x2;
            this.x2 = x1;
        }
        if (y1 <= y2) {
            this.y1 = y1;
            this.y2 = y2;
        } else {
            this.y1 = y2;
            this.y2 = y1;
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Getter for property x1.
     *
     * @return  Value of property x1.
     */
    public double getX1() {
        if (x1 <= x2) {
            return x1;
        } else {
            return x2;
        }
    }

    /**
     * Setter for property x1.
     *
     * @param  x1  New value of property x1.
     */
    public void setX1(final double x1) {
        this.x1 = x1;
    }

    /**
     * Getter for property y1.
     *
     * @return  Value of property y1.
     */
    public double getY1() {
        if (y1 <= y2) {
            return y1;
        } else {
            return y2;
        }
    }

    /**
     * Setter for property y1.
     *
     * @param  y1  New value of property y1.
     */
    public void setY1(final double y1) {
        this.y1 = y1;
    }

    /**
     * Getter for property x2.
     *
     * @return  Value of property x2.
     */
    public double getX2() {
        if (x2 > x1) {
            return x2;
        } else {
            return x1;
        }
    }

    /**
     * Setter for property x2.
     *
     * @param  x2  New value of property x2.
     */
    public void setX2(final double x2) {
        this.x2 = x2;
    }

    /**
     * Getter for property y2.
     *
     * @return  Value of property y2.
     */
    public double getY2() {
        if (y2 > y1) {
            return y2;
        } else {
            return y1;
        }
    }

    /**
     * Setter for property y2.
     *
     * @param  y2  New value of property y2.
     */
    public void setY2(final double y2) {
        this.y2 = y2;
    }

    /**
     * This Method return the BoundingBox like (1.11,2.22,3.33,4.44).
     *
     * @return  String value
     */
    @Override
    public String toString() {
        return "(" + round(getX1()) + "," + round(getY1()) + "," + round(getX2()) + "," + round(getY2()) + ")"; // NOI18N
    }

    /**
     * This Method return the BoundingBox like
     * -179.99999999999997,-105.48710130136223,176.35443037974682,114.6141645214226.
     *
     * @return  String to assemble URL
     */
    public String getURLString() {
        return getX1() + "," + getY1() + "," + getX2() + "," + getY2(); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGeometryFromTextLineString() {
        return "LINESTRING(" + getX1() + " " + getY1() + "," + getX2() + " " + getY2() + ")"; // NOI18N
    }
    /**
     * This Method return the BoundingBox like
     * BOX3D(-179.99999999999997,-105.48710130136223,176.35443037974682,114.6141645214226).
     *
     * @return  String to work in GeometryFromText
     */
    public String getGeometryFromTextCompatibleString() {
        return "BOX3D(" + getURLString() + ")"; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Logger getLog() {
        return log;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wtst  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PBounds getPBounds(final WorldToScreenTransform wtst) {
        return new PBounds(wtst.getScreenX(x1),
                wtst.getScreenY(y2),
                x2
                        - x1,
                wtst.getScreenY(y1)
                        - wtst.getScreenY(y2));
    }

    /**
     * return true if the given boundingBox has the same values.
     *
     * @param   bb  BoundingBox to be checked
     *
     * @return  true if the two Boxes are the same
     */
    boolean equals(final BoundingBox bb) {
        return ((getX1() == bb.getX1()) && (getX2() == bb.getX2()) && (getY1() == bb.getY1())
                        && (getY2() == bb.getY2()));
    }

    /**
     * Clone Method.
     *
     * @return  a BoundingBox with the same values
     */
    @Override
    public Object clone() {
        return new BoundingBox(x1, y1, x2, y2);
    }

    /**
     * Rounds a double to #.00.
     *
     * @param   d  The double
     *
     * @return  the new double
     */
    public static String round(final double d) {
        final double dd = ((double)(Math.round(d * 100))) / 100;
        final String pattern = "0.00"; // NOI18N
        final java.text.DecimalFormat myFormatter = new java.text.DecimalFormat(pattern);
        final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator('.');
        myFormatter.setDecimalFormatSymbols(symbols);
        return myFormatter.format(d);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getJDOMElement() {
        final Element e = new Element("BoundingBox");    // NOI18N
        e.setAttribute("x1", new Double(x1).toString()); // NOI18N
        e.setAttribute("y1", new Double(y1).toString()); // NOI18N
        e.setAttribute("x2", new Double(x2).toString()); // NOI18N
        e.setAttribute("y2", new Double(y2).toString()); // NOI18N
        return e;
    }
    /**
     * public Geometry getGeometry() { // GeometryFactory gf=new GeometryFactory(); // LinearRing lr=gf.c //
     * gf.createPolygon() }.
     *
     * @return  DOCUMENT ME!
     */
    public double getWidth() {
        return Math.abs(x2 - x1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getHeight() {
        return Math.abs(y2 - y1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toGmlString() {
        return "<gml:Box><gml:coord><gml:X>" + getX1() + "</gml:X><gml:Y>" + getY1() + "</gml:Y></gml:coord>" // NOI18N
                    + "<gml:coord><gml:X>" + getX2() + "</gml:X><gml:Y>" + getY2() + "</gml:Y></gml:coord>"   // NOI18N
                    + "</gml:Box>";                                                                           // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toGml4WFS110String() {
        return "<gml:Envelope><gml:lowerCorner>" + getX1()                                // NOI18N
                    + " " + getY1() + "</gml:lowerCorner>" + "<gml:upperCorner>"          // NOI18N
                    + getX2() + " " + getY2() + "</gml:upperCorner>" + "</gml:Envelope>"; // NOI18N
    }
}
