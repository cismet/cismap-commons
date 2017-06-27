/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import java.util.List;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class XBoundingBox extends BoundingBox {

    //~ Instance fields --------------------------------------------------------

    private String srs;
    private boolean metric = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BoundingBox object.
     *
     * @param  geom  DOCUMENT ME!
     */
    public XBoundingBox(final Geometry geom) {
        super(geom);
        this.srs = CrsTransformer.createCrsFromSrid(geom.getSRID());
        final List<Crs> crsList = CismapBroker.getInstance().getMappingComponent().getCrsList();

        for (final Crs crsTmp : crsList) {
            if (crsTmp.getCode().equals(this.srs)) {
                this.metric = crsTmp.isMetric();
                break;
            }
        }
    }

    /**
     * Creates a new XBoundingBox object.
     *
     * @param   boundingBoxElementParent  DOCUMENT ME!
     *
     * @throws  DataConversionException  DOCUMENT ME!
     */
    public XBoundingBox(final Element boundingBoxElementParent) throws DataConversionException {
        super(boundingBoxElementParent);

        final Element conf = boundingBoxElementParent.getChild("BoundingBox"); // NOI18N
        final Attribute attributeSrs = conf.getAttribute("srs");               // NOI18N
        final Attribute attributeMetric = conf.getAttribute("metric");         // NOI18N

        if (attributeSrs != null) {
            this.srs = attributeSrs.getValue();
        } else {
            this.srs = null;
        }
        if (attributeMetric != null) {
            this.metric = attributeMetric.getBooleanValue();
        } else {
            this.metric = false;
        }
    }

    /**
     * Creates a new XBoundingBox object.
     *
     * @param  geom    DOCUMENT ME!
     * @param  crs     DOCUMENT ME!
     * @param  metric  DOCUMENT ME!
     */
    public XBoundingBox(final Geometry geom, final String crs, final boolean metric) {
        super(geom);
        this.srs = crs;
        this.metric = metric;
    }

    /**
     * Creates a new XBoundingBox object.
     *
     * @param   boundingBoxElementParent  DOCUMENT ME!
     * @param   srs                       DOCUMENT ME!
     * @param   metric                    DOCUMENT ME!
     *
     * @throws  DataConversionException  DOCUMENT ME!
     */
    public XBoundingBox(final Element boundingBoxElementParent, final String srs, final boolean metric)
            throws DataConversionException {
        super(boundingBoxElementParent);
        this.srs = srs;
        this.metric = metric;
    }

    /**
     * Creates a new instance of XBoundingBox.
     *
     * @param  x1      DOCUMENT ME!
     * @param  y1      DOCUMENT ME!
     * @param  x2      DOCUMENT ME!
     * @param  y2      DOCUMENT ME!
     * @param  srs     DOCUMENT ME!
     * @param  metric  DOCUMENT ME!
     */
    public XBoundingBox(final double x1,
            final double y1,
            final double x2,
            final double y2,
            final String srs,
            final boolean metric) {
        super(x1, y1, x2, y2);
        this.srs = srs;
        this.metric = metric;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSrs() {
        return srs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setSrs(final String srs) {
        this.srs = srs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMetric() {
        return metric;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metric  DOCUMENT ME!
     */
    public void setMetric(final boolean metric) {
        this.metric = metric;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeometry() {
        final GeometryFactory factory = new GeometryFactory(
                new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(srs));

        final Coordinate[] bbox = new Coordinate[5];
        bbox[0] = new Coordinate(getX1(), getY1());
        bbox[1] = new Coordinate(getX1(), getY2());
        bbox[2] = new Coordinate(getX2(), getY2());
        bbox[3] = new Coordinate(getX2(), getY1());
        bbox[4] = new Coordinate(getX1(), getY1());
        final LinearRing ring = new LinearRing(new CoordinateArraySequence(bbox), factory);

        return factory.createPolygon(ring, new LinearRing[0]);
    }

    @Override
    public Element getJDOMElement() {
        final Element result = super.getJDOMElement();

        result.setAttribute(new Attribute("srs", srs));
        result.setAttribute(new Attribute("metric", Boolean.toString(metric)));

        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof XBoundingBox) {
            final XBoundingBox bb = (XBoundingBox)other;

            return ((getX1() == bb.getX1()) && (getX2() == bb.getX2()) && (getY1() == bb.getY1())
                            && (getY2() == bb.getY2())) && (getSrs() == bb.getSrs());
        } else {
            return super.equals(other);
        }
    }
}
