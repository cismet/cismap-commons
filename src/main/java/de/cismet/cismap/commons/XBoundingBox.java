/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
}
