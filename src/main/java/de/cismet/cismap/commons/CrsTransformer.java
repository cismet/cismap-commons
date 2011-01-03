/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.LineString;

import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.Point;

import java.security.InvalidParameterException;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CrsTransformer {

    //~ Instance fields --------------------------------------------------------

    private GeoTransformer transformer;
    private CoordinateSystem crs;
    private String destCrsAsString;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CrsTransformer object.
     *
     * @param   destCrs  DOCUMENT ME!
     *
     * @throws  UnknownCRSException        DOCUMENT ME!
     * @throws  InvalidParameterException  DOCUMENT ME!
     */
    public CrsTransformer(final String destCrs) throws UnknownCRSException, InvalidParameterException {
        this.destCrsAsString = destCrs;
        transformer = new GeoTransformer(destCrs);
        crs = CRSFactory.create(destCrs);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDestinationCrs() {
        return destCrsAsString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bbox       DOCUMENT ME!
     * @param   sourceCrs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     */
    public BoundingBox transformBoundingBox(final BoundingBox bbox, final String sourceCrs) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException {
        final CoordinateSystem coordSystem = CRSFactory.create(sourceCrs);
        Point minPoint = GeometryFactory.createPoint(bbox.getX1(), bbox.getY1(), coordSystem);
        Point maxPoint = GeometryFactory.createPoint(bbox.getX2(), bbox.getY2(), coordSystem);
        minPoint = (org.deegree.model.spatialschema.Point)transformer.transform(minPoint);
        maxPoint = (org.deegree.model.spatialschema.Point)transformer.transform(maxPoint);
        BoundingBox newBbox;

        if (bbox instanceof XBoundingBox) {
            newBbox = new XBoundingBox(minPoint.getX(),
                    minPoint.getY(),
                    maxPoint.getX(),
                    maxPoint.getY(),
                    crs.getIdentifier(),
                    crs.getAxisUnits()[0].equals(Unit.METRE));
        } else {
            newBbox = new BoundingBox(minPoint.getX(), minPoint.getY(), maxPoint.getX(), maxPoint.getY());
        }

        return newBbox;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bbox  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     */
    public XBoundingBox transformBoundingBox(final XBoundingBox bbox) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException {
        return (XBoundingBox)transformBoundingBox(bbox, bbox.getSrs());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom       DOCUMENT ME!
     * @param   sourceCrs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     * @throws  GeometryException           DOCUMENT ME!
     */
    public com.vividsolutions.jts.geom.Geometry transformGeometry(final com.vividsolutions.jts.geom.Geometry geom,
            final String sourceCrs) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException,
        GeometryException {
        final CoordinateSystem coordSystem = CRSFactory.create(sourceCrs);
        Geometry deegreeGeom = JTSAdapter.wrap(geom);
        deegreeGeom = transformer.transform(deegreeGeom, coordSystem.getCRS());

        return JTSAdapter.export(deegreeGeom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coords     DOCUMENT ME!
     * @param   sourceCrs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     * @throws  GeometryException           DOCUMENT ME!
     */
    public com.vividsolutions.jts.geom.Coordinate[] transformGeometry(
            final com.vividsolutions.jts.geom.Coordinate[] coords,
            final String sourceCrs) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException,
        GeometryException {
        final com.vividsolutions.jts.geom.GeometryFactory gfac = new com.vividsolutions.jts.geom.GeometryFactory();
        com.vividsolutions.jts.geom.Geometry geom;

        if (coords.length == 1) {
            geom = gfac.createPoint(coords[0]);
        } else {
            geom = gfac.createLineString(coords);
        }

        geom = transformGeometry(geom, sourceCrs);
        return geom.getCoordinates();
    }
}
