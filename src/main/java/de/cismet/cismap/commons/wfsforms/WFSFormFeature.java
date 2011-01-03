/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfsforms;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        this.feature = feature;
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
            return JTSAdapter.export(feature.getDefaultGeometryPropertyValue());
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
            final Point p = (Point)(JTSAdapter.export((org.deegree.model.spatialschema.Geometry)(fp[0].getValue())));
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
