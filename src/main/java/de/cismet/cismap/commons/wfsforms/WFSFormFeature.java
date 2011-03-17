/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfsforms;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.cismet.cismap.commons.CrsTransformer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WFSFormFeature {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Feature feature;
    private WFSFormQuery query;
    private String featureCrs = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WFSFormFeature.
     *
     * @param  feature  DOCUMENT ME!
     * @param  query    DOCUMENT ME!
     */
    public WFSFormFeature(final Feature feature, final WFSFormQuery query) {
        this.feature = feature;
        this.query = query;
        if ((feature != null) && (feature.getDefaultGeometryPropertyValue() != null)
                    && (feature.getDefaultGeometryPropertyValue().getCoordinateSystem() != null)) {
            this.featureCrs = feature.getDefaultGeometryPropertyValue().getCoordinateSystem().getIdentifier();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIdentifier() {
        try {
//return feature.getAttribute(query.getIdProperty()).toString();
            // return feature.getProperty(query.getIdProperty()).toString();
            // return feature.getProperties( new QualifiedName(query.getIdProperty().toString()) );
            // return
            return feature
                        .getProperties(new QualifiedName(
                                query.getPropertyPrefix().toString(),
                                query.getIdProperty().toString(),
                                new URI(query.getPropertyNamespace().toString())))[0].getValue()
                        .toString();
        } catch (Exception e) {
            log.error("Error in toIdentifier()", e); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prefix      DOCUMENT ME!
     * @param   identifier  DOCUMENT ME!
     * @param   namespace   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public FeatureProperty[] getRawFeatureArray(final String prefix, final String identifier, final String namespace)
            throws Exception {
        return feature.getProperties(new QualifiedName(prefix, identifier, new URI(namespace)));
    }

    @Override
    public String toString() {
        try { // return feature.getAttribute(query.getDisplayTextProperty()).toString();
            // return feature.getProperty(query.getDisplayTextProperty()).toString();
            if (query.getPropertyPrefix() != null) {
                final String s = feature
                            .getProperties(new QualifiedName(
                                    query.getPropertyPrefix().toString(),
                                    query.getDisplayTextProperty().toString(),
                                    new URI(query.getPropertyNamespace().toString())))[0].getValue()
                            .toString();
                return s;
            } else {
                final String s = feature.getProperties(new QualifiedName(query.getDisplayTextProperty().toString()))[0]
                            .getValue().toString();
                return s;
            }

//        ByteBuffer bb = ByteBuffer.wrap(ret.getBytes());
//        return Charset.forName("utf-8").decode(bb).toString();

//        try {
//            return new String (feature.getProperties(new QualifiedName(query.getDisplayTextProperty().toString()))[0].getValue().toString().getBytes(),"ISO-8859-1");
//        }
//        catch (Exception skip) {
//            return "";
//        }
        } catch (Exception e) {
//            try {
//                String ret =feature.getProperties(new QualifiedName("app",query.getDisplayTextProperty().toString(),new URI("http://www.deegree.org/app")))[0].getValue().toString();
//                return ret;
//            } catch (Exception ex) {
            try {
                log.error("Error in toString() angefragt wurde: "
                            + new QualifiedName(
                                query.getPropertyPrefix().toString(),
                                query.getDisplayTextProperty().toString(),
                                new URI(query.getPropertyNamespace().toString())).toString(),
                    e);                                                            // NOI18N
            } catch (Exception never) {
                log.error("Error in toString()", e);                               // NOI18N
            }
            for (final FeatureProperty fp : feature.getProperties()) {
                log.fatal(fp.getName().getPrefix() + "." + fp.getName().getLocalName() + "."
                            + fp.getName().getNamespace() + "->" + fp.getValue()); // NOI18N
            }
            return null;
//            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void setFeature(final Feature feature) {
        log.error("setFeature " + feature, new Exception());
        this.feature = feature;
        if ((feature != null) && (feature.getDefaultGeometryPropertyValue() != null)
                    && (feature.getDefaultGeometryPropertyValue().getCoordinateSystem() != null)) {
            this.featureCrs = feature.getDefaultGeometryPropertyValue().getCoordinateSystem().getIdentifier();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WFSFormQuery getQuery() {
        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getJTSGeometry() {
        try {
            Geometry res = JTSAdapter.export(feature.getDefaultGeometryPropertyValue());
            if (log.isDebugEnabled()) {
                log.debug("feature srid: " + res.getSRID());
            }

            if ((feature != null) && (feature.getDefaultGeometryPropertyValue() != null)
                        && (feature.getDefaultGeometryPropertyValue().getCoordinateSystem() != null)) {
                final String crsString = feature.getDefaultGeometryPropertyValue()
                            .getCoordinateSystem()
                            .getIdentifier();
                final int srid = CrsTransformer.extractSridFromCrs(crsString);
                final GeometryFactory fac = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
                res = fac.createGeometry(res);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Use saved crs from member variable. " + this.featureCrs);
                }

                if (this.featureCrs != null) {
                    final int srid = CrsTransformer.extractSridFromCrs(this.featureCrs);
                    final GeometryFactory fac = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
                    res = fac.createGeometry(res);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("feature srid after setting: " + res.getSRID());
            }
            return res;
        } catch (GeometryException ex) {
            log.error("Error in getJTSGeometry()", ex); // NOI18N
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Point getPosition() {
        try {
            final FeatureProperty[] fp = feature.getProperties(new QualifiedName(
                        query.getPropertyPrefix().toString(),
                        query.getPositionProperty().toString(),
                        new URI(query.getPropertyNamespace().toString())));
            final org.deegree.model.spatialschema.Geometry geo = ((org.deegree.model.spatialschema.Geometry)
                    (fp[0].getValue()));
            // the JTSAdapter ignores the srid. The resulted geometry will not have a srid and
            // its factory will not have a srid. So the result of an operation like .buffer(int) will not have
            // a srid. To prevent this, a new point is created from a factory with a srid.
            Point p = (Point)(JTSAdapter.export(geo));
            final int srid = CrsTransformer.extractSridFromCrs(geo.getCoordinateSystem().getIdentifier());
            final GeometryFactory fac = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);

            p = fac.createPoint(p.getCoordinate());
            if (log.isDebugEnabled()) {
                log.debug("POSITION=" + p);                                                              // NOI18N
            }
            return p;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Feature has no POSITION. Calculate the centroid from getJTSGeometry() ", ex); // NOI18N
            }
            final Point p = getJTSGeometry().getCentroid();
            return p;
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @param  query  DOCUMENT ME!
     */
    public void setQuery(final WFSFormQuery query) {
        this.query = query;
    }
}
