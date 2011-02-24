/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.Polygon;

import org.deegree.crs.components.Unit;
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

import java.util.List;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CrsTransformer {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CrsTransformer.class);

    //~ Instance fields --------------------------------------------------------

    private GeoTransformer transformer;
    private CoordinateSystem crs;
    private String destCrsAsString;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CrsTransformer object.
     *
     * @param   destCrs  the destination crs in the form "EPSG:XXXX"
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
     * @return  the destination crs in the form "EPSG:XXXX"
     */
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
    public BoundingBox transformBoundingBox(final BoundingBox bbox, String sourceCrs) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException {
        if (isDefaultCrs(sourceCrs)) {
            sourceCrs = CismapBroker.getInstance().getDefaultCrs();
        }
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
            String sourceCrs) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException,
        GeometryException {
        if (isDefaultCrs(sourceCrs)) {
            sourceCrs = CismapBroker.getInstance().getDefaultCrs();
        }
        final CoordinateSystem coordSystem = CRSFactory.create(sourceCrs);
        com.vividsolutions.jts.geom.Geometry newGeom = (com.vividsolutions.jts.geom.Geometry)geom.clone();
        Geometry deegreeGeom = JTSAdapter.wrap(newGeom);
        deegreeGeom = transformer.transform(deegreeGeom, coordSystem.getCRS());

        newGeom = JTSAdapter.export(deegreeGeom);
        setSrid(newGeom);
        return newGeom;
    }

    /**
     * use this method carefully. If you transform the coordinates of a geometry, you should also change the SRID of the
     * geomerty that contains the coordinates. That is required because the coordinates don't contain the SRID.
     *
     * @param   coords     the coordinates to transform
     * @param   sourceCrs  the CRS of the given coordinates
     *
     * @return  the new coordinates
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

    /**
     * Set the SRID of the given Geometry object to the destination CRS of this CRSTransformer object. The coordinates
     * of the geometry will not be changed.
     *
     * @param  geom  DOCUMENT ME!
     */
    public void setSrid(final com.vividsolutions.jts.geom.Geometry geom) {
        geom.setSRID(extractSridFromCrs(destCrsAsString));
    }

    /**
     * extracts the srid from the given srs. The srs should have the form "EPSG:XXXX"
     *
     * @param   crs  DOCUMENT ME!
     *
     * @return  the srid or -1 if the srid could not be determined
     */
    public static int extractSridFromCrs(final String crs) {
        try {
            return Integer.parseInt(crs.substring(crs.indexOf(":") + 1));
        } catch (Exception e) {
            log.error("Cannot extract the SRID from the CRS " + crs);
            return -1;
        }
    }

    /**
     * creates the crs from the given srid.
     *
     * @param   srid  crs DOCUMENT ME!
     *
     * @return  the crs
     */
    public static String createCrsFromSrid(final int srid) {
        return "EPSG:" + srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the current srid
     */
    public static int getCurrentSrid() {
        return extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
    }

    /**
     * transforms the given geometry to the current CRS.
     *
     * @param   geom  the geometry to transform
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the current CRS
     */
    public static com.vividsolutions.jts.geom.Geometry transformToCurrentCrs(
            final com.vividsolutions.jts.geom.Geometry geom) {
        if (geom == null) {
            return null;
        }
        String crsname = "EPSG:" + geom.getSRID();
        final String defaultCrs = CismapBroker.getInstance().getDefaultCrs();
        final String currentSrs = CismapBroker.getInstance().getSrs().getCode();
        if (log.isDebugEnabled()) {
            log.debug("crsname " + crsname + " currentSrs " + currentSrs + " default crs: " + defaultCrs);
        }
        if (isDefaultCrs(crsname)) {
            crsname = defaultCrs;
        }

        if (!currentSrs.equals(crsname)) {
            try {
                final CrsTransformer crsTransformer = new CrsTransformer(currentSrs);
                return crsTransformer.transformGeometry(geom, crsname);
            } catch (Exception e) {
                log.error("Cannot transform the geometry from " + crsname + " to " + currentSrs, e);
            }
        }

        return geom;
    }

    /**
     * transforms the given geometry to the default CRS.
     *
     * @param   geom  the geometry to transform
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the default CRS
     */
    public static com.vividsolutions.jts.geom.Geometry transformToDefaultCrs(
            final com.vividsolutions.jts.geom.Geometry geom) {
        if (geom == null) {
            return null;
        }

        com.vividsolutions.jts.geom.Geometry newGeom = null;
        final String curCrs = "EPSG:" + geom.getSRID();
        // Wenn SRID nicht gesetzt oder auf -1 gesetzt ist, dann handelt es sich schon um das default CRS
        if (!isDefaultCrs(curCrs)) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("transform geometry from " + curCrs + " to "
                                + CismapBroker.getInstance().getDefaultCrs());
                }
                final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getDefaultCrs());
                newGeom = transformer.transformGeometry(geom, curCrs);
            } catch (Exception e) {
                log.error("Cannot transform the geometry from " + curCrs + " to "
                            + CismapBroker.getInstance().getDefaultCrs(),
                    e);
                newGeom = geom;
            }
        } else {
            newGeom = geom;
        }

        return newGeom;
    }

    /**
     * transforms the given geometry to a metric CRS.
     *
     * @param   geom     the geometry to transform
     * @param   crsList  DOCUMENT ME!
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the current CRS
     */
    public static com.vividsolutions.jts.geom.Geometry transformToMetricCrs(
            final com.vividsolutions.jts.geom.Geometry geom,
            final List<Crs> crsList) {
        if (geom == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry newGeom = null;
        final String curCrs = "EPSG:" + geom.getSRID();

        // Wenn SRID nicht gesetzt oder auf -1 gesetzt ist, dann handelt es sich um das default CRS
        // und das ist immer metrisch
        if (!isDefaultCrs(curCrs)) {
            final int index = crsList.indexOf(curCrs);
            if (index != -1) {
                final Crs crs = crsList.get(index);
                if (crs.isMetric()) {
                    newGeom = geom;
                }
            }

            if (newGeom == null) {
                final String defaultCrs = CismapBroker.getInstance().getDefaultCrs();
                try {
                    final CrsTransformer transformer = new CrsTransformer(defaultCrs);
                    newGeom = transformer.transformGeometry(geom, curCrs);
                } catch (Exception e) {
                    log.error("Cannot transform the geometry from " + curCrs + " to " + defaultCrs, e);
                    newGeom = geom;
                }
            }
        } else {
            newGeom = geom;
        }

        return newGeom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   crs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isDefaultCrs(final String crs) {
        if (crs.endsWith(":" + CismapBroker.getInstance().getDefaultCrsAlias())
                    || crs.endsWith(":0") || crs.endsWith(":-1")) {
            return true;
        } else {
            return false;
        }
    }
}
