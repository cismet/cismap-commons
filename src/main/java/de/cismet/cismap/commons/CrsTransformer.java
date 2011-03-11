/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import org.apache.log4j.Logger;

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

    private static final transient Logger LOG = Logger.getLogger(CrsTransformer.class);

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
     * @param   sourceCrs  DOCUMENT ME!
     * @param   coords     coords <T> DOCUMENT ME!
     *
     * @return  a copy of the given geometry in the destination crs. The given geometry will not be changed!
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     * @throws  GeometryException           DOCUMENT ME!
     */
    //J-
    public <T extends com.vividsolutions.jts.geom.Geometry> T transformGeometry(final T geom, final String sourceCrs)
            throws UnknownCRSException,
            CRSTransformationException,
            IllegalArgumentException,
            GeometryException {
        final com.vividsolutions.jts.geom.Geometry newGeom = (com.vividsolutions.jts.geom.Geometry)geom.clone();

        return (T)fastTransformGeometry(newGeom, sourceCrs);
    }
    //J+

    /**
     * DOCUMENT ME!
     *
     * @param   sourceCrs  DOCUMENT ME!
     * @param   coords     coords <T> DOCUMENT ME!
     *
     * @return  the given geometry transformed to the destination CRS. The given geometry will be changed!
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     * @throws  GeometryException           DOCUMENT ME!
     */
    //J-
    public <T extends com.vividsolutions.jts.geom.Geometry> T fastTransformGeometry(final T geom,
            final String sourceCrs) throws UnknownCRSException,
        CRSTransformationException,
        IllegalArgumentException,
        GeometryException {
        final String srcCrs;
        if (isDefaultCrs(sourceCrs)) {
            srcCrs = CismapBroker.getInstance().getDefaultCrs();
        } else {
            srcCrs = sourceCrs;
        }

        final CoordinateSystem coordSystem = CRSFactory.create(srcCrs);
        Geometry deegreeGeom = JTSAdapter.wrap(geom);
        deegreeGeom = transformer.transform(deegreeGeom, coordSystem.getCRS());

        final T ret = (T)JTSAdapter.export(deegreeGeom);
        setSrid(ret);

        return ret;
    }
    //J+

    /**
     * use this method carefully. If you transform the coordinates of a geometry, you should also change the SRID of the
     * geomerty that contains the coordinates. That is required because the coordinates don't contain the SRID.
     *
     * @param   sourceCrs  the CRS of the given coordinates
     * @param   coords     the coordinates to transform
     *
     * @return  the new coordinates
     *
     * @throws  UnknownCRSException         DOCUMENT ME!
     * @throws  CRSTransformationException  DOCUMENT ME!
     * @throws  IllegalArgumentException    DOCUMENT ME!
     * @throws  GeometryException           DOCUMENT ME!
     */
    public com.vividsolutions.jts.geom.Coordinate[] transformGeometry(final String sourceCrs,
            final com.vividsolutions.jts.geom.Coordinate... coords) throws UnknownCRSException,
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
    public static int extractSridFromCrs(String crs) {
        try {
            if (isDefaultCrs(crs)) {
                crs = CismapBroker.getInstance().getDefaultCrs();
            }

            return Integer.parseInt(crs.substring(crs.indexOf(":") + 1)); // NOI18N
        } catch (final Exception e) {
            LOG.error("Cannot extract the SRID from the CRS " + crs);     // NOI18N
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
        if (isDefaultCrs("EPSG:" + srid)) {
            return CismapBroker.getInstance().getDefaultCrs();
        } else {
            return "EPSG:" + srid;
        }
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
     * @param   <T>   DOCUMENT ME!
     * @param   geom  the geometry to transform
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the current CRS
     */
    public static <T extends com.vividsolutions.jts.geom.Geometry> T transformToCurrentCrs(
            final T geom) {
        final String currentSrs = CismapBroker.getInstance().getSrs().getCode();

        return transformToGivenCrs(geom, currentSrs);
    }

    /**
     * transforms the given geometry to the current CRS.
     *
     * @param   <T>   DOCUMENT ME!
     * @param   geom  the geometry to transform
     * @param   crs   DOCUMENT ME!
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the current CRS
     */
    public static <T extends com.vividsolutions.jts.geom.Geometry> T transformToGivenCrs(final T geom,
            final String crs) {
        if (geom == null) {
            return null;
        }
        String crsname = "EPSG:" + geom.getSRID(); // NOI18N
        final String defaultCrs = CismapBroker.getInstance().getDefaultCrs();

        if (LOG.isDebugEnabled()) {
            LOG.debug("crsname " + crsname + " given crs " + crs + " default crs: " + defaultCrs); // NOI18N
        }
        if (isDefaultCrs(crsname)) {
            crsname = defaultCrs;
        }

        if (!crs.equals(crsname)) {
            try {
                final CrsTransformer crsTransformer = new CrsTransformer(crs);
                return crsTransformer.transformGeometry(geom, crsname);
            } catch (Exception e) {
                LOG.error("Cannot transform the geometry from " + crsname + " to " + crs, e); // NOI18N
            }
        }

        return geom;
    }

    /**
     * transforms the given geometry to the default CRS.
     *
     * @param   <T>   DOCUMENT ME!
     * @param   geom  the geometry to transform
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the default CRS
     */
    public static <T extends com.vividsolutions.jts.geom.Geometry> T transformToDefaultCrs(final T geom) {
        if (geom == null) {
            return null;
        }

        com.vividsolutions.jts.geom.Geometry newGeom = null;
        final String curCrs = "EPSG:" + geom.getSRID(); // NOI18N
        // Wenn SRID nicht gesetzt oder auf -1 gesetzt ist, dann handelt es sich schon um das default CRS
        if (!isDefaultCrs(curCrs)) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("transform geometry from " + curCrs + " to " // NOI18N
                                + CismapBroker.getInstance().getDefaultCrs());
                }
                final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getDefaultCrs());
                newGeom = transformer.transformGeometry(geom, curCrs);
            } catch (Exception e) {
                LOG.error("Cannot transform the geometry from " + curCrs + " to " // NOI18N
                            + CismapBroker.getInstance().getDefaultCrs(),
                    e);
                newGeom = geom;
            }
        } else {
            newGeom = geom;
        }

        return (T)newGeom;
    }

    /**
     * transforms the given geometry to a metric CRS.
     *
     * @param   <T>      DOCUMENT ME!
     * @param   geom     the geometry to transform
     * @param   crsList  DOCUMENT ME!
     *
     * @return  the new geomerty or the given geometry, if the given geometry is already in the current CRS
     */
    public static <T extends com.vividsolutions.jts.geom.Geometry> T transformToMetricCrs(
            final T geom,
            final List<Crs> crsList) {
        if (geom == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry newGeom = null;
        final String curCrs = "EPSG:" + geom.getSRID(); // NOI18N

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
                    LOG.error("Cannot transform the geometry from " + curCrs + " to " + defaultCrs, e); // NOI18N
                    newGeom = geom;
                }
            }
        } else {
            newGeom = geom;
        }

        return (T)newGeom;
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
                    || crs.endsWith(":0") || crs.endsWith(":-1") // NOI18N
                    || crs.equals(CismapBroker.getInstance().getDefaultCrs())) {
            return true;
        } else {
            return false;
        }
    }
}
