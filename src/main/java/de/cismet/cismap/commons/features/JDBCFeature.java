/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.JTSAdapter;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JDBCFeature extends DefaultFeatureServiceFeature {
    
    //~ Static fields/initializers ---------------------------------------------

    // caches the last feature properties
    private static final Object sync = new Object();

    //~ Instance fields --------------------------------------------------------

    private final ShapeInfo shapeInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFeature object.
     *
     * @param  shapeInfo  typename DOCUMENT ME!
     */

    public JDBCFeature(final ShapeInfo shapeInfo) {
        this.shapeInfo = shapeInfo;
    }

    //~ Methods ----------------------------------------------------------------

// /**
// * Creates a new ShapeFeature object.
// *
// * @param  typename  DOCUMENT ME!
// * @param  styles    DOCUMENT ME!
// */
// public ShapeFeature(final ShapeInfo shapeInfo, final List<org.deegree.style.se.unevaluated.Style> styles) {
// setSLDStyles(styles); // super.style = styles;
// this.shapeInfo = shapeInfo;
// }

    @Override
    public HashMap getProperties() {
        LinkedHashMap<String, Object> container = null;
        final int id = getId();

        if (shapeInfo.getFc() == null) {
            try {
                container = shapeInfo.getPropertiesFromCache(id);

                if (container != null) {
                    return container;
                } else {
                    container = new LinkedHashMap<String, Object>();
                }

                ResultSet rs = null;
                rs = shapeInfo.getStatement().executeQuery("select * from poly where id = " + id);
                
                if (rs.next()) {
                    int count = rs.getMetaData().getColumnCount();
                    for (int i = 0; i < count; ++i) {
                        container.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                    }
                }

                shapeInfo.addPropertiesToCache(id, container);
            } catch (final Exception e) {
                logger.error("Cannot read properties from file.", e);
            }
        } else {
            container = new LinkedHashMap<String, Object>();

            try {
                final org.deegree.model.feature.Feature degreeFeature = shapeInfo.getFc().getFeature(id);
                final FeatureProperty[] featureProperties = degreeFeature.getProperties();

                for (final FeatureProperty fp : featureProperties) {
                    container.put(fp.getName().getAsString(), fp.getValue());
                }
            } catch (final Exception e) {
                logger.error("Cannot read properties from file.", e);
            }
        }

        return container;
    }

    @Override
    public Object getProperty(final String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public void setProperties(final HashMap properties) {
        // nothing to do
    }

    @Override
    public void addProperty(final String propertyName, final Object property) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @param  map  DOCUMENT ME!
     */
    public void addProperties(final Map<String, Object> map) {
        // nothing to do
    }

    @Override
    public Geometry getGeometry() {
        Geometry g = null;
        g = shapeInfo.getGeometryFromCache(getId());

        if (g != null) {
            return g;
        }

        try {
            PreparedStatement ps = shapeInfo.getGeometryStatement();
            ps.setInt(1, getId());
            ResultSet rs = ps.executeQuery();
//                ResultSet rs = shapeInfo.getStatement().executeQuery("select the_geom from poly where id = " + getId());
            if (rs.next()) {
                g = (Geometry)rs.getObject(1);
            }
//                g.setSRID(shapeInfo.getSrid());
        } catch (final Exception e) {
            logger.error("Cannot read geometry from shape file.", e);
        }

        shapeInfo.addGeometryToCache(getId(), g);

        return g;
    }

    @Override
    public void setGeometry(final Geometry geom) {
        // do nothing
    }
}
