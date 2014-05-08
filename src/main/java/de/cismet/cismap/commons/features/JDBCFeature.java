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
import java.sql.Statement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.gui.piccolo.PFeature;

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

    private final JDBCFeatureInfo featureInfo;
//    private PFeature pfeature = null;

    //~ Constructors -----------------------------------------------------------

    /**
    * Creates a new ShapeFeature object.
    *
    * @param  shapeInfo  typename DOCUMENT ME!
    * @param  styles     DOCUMENT ME!
    */
    public JDBCFeature(final JDBCFeatureInfo shapeInfo, final List<org.deegree.style.se.unevaluated.Style> styles) {
        setSLDStyles(styles); // super.style = styles;
        this.featureInfo = shapeInfo;
    }

    //~ Methods ----------------------------------------------------------------


    @Override
    public HashMap getProperties() {
        if (existProperties()) {
            return super.getProperties();
        }

        LinkedHashMap<String, Object> container = null;
        final int id = getId();

        try {
            container = featureInfo.getPropertiesFromCache(id);

            if (container != null) {
                return container;
            } else {
                container = new LinkedHashMap<String, Object>();
            }

            ResultSet rs = null;
            final PreparedStatement ps = featureInfo.getPropertiesStatement();
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                final int count = rs.getMetaData().getColumnCount();
                for (int i = 0; i < count; ++i) {
                    container.put(rs.getMetaData().getColumnName(i + 1), rs.getObject(i + 1));
                }
            }

            featureInfo.addPropertiesToCache(id, container);
        } catch (final Exception e) {
            logger.error("Cannot read properties from file.", e);
        }

        return container;
    }

    @Override
    public Object getProperty(final String propertyName) {
        if (existProperties()) {
            return super.getProperties().get(propertyName);
        }
        Object result;
        final int id = getId();
        final String cacheId = id + "@" + propertyName;
        result = featureInfo.getPropertyFromCache(cacheId);

        if (result == null) {
            try {
                final PreparedStatement ps = featureInfo.getPreparedStatementForProperty(propertyName);
                ps.setInt(1, id);
                final ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    result = rs.getObject(1);
                }

                featureInfo.addPropertyToCache(cacheId, result);
            } catch (final Exception e) {
                logger.error("Cannot read property from file.", e);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyName   DOCUMENT ME!
     * @param  propertyValue  DOCUMENT ME!
     */
    public void setProperty(final String propertyName, final Object propertyValue) {
        if (!existProperties()) {
            super.setProperties(getProperties());
        }

        super.addProperty(propertyName, propertyValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean existProperties() {
        return !super.getProperties().isEmpty();
    }
    /**
     * DOCUMENT ME!
     *
     * @throws    Exception  DOCUMENT ME!
     *
     * @Override  DOCUMENT ME!
     */
    public void saveChanges() throws Exception {
        if (!existProperties()) {
            return;
        }

        final HashMap map = super.getProperties();
        final Statement st = featureInfo.getConnection().createStatement();
        final StringBuilder update = new StringBuilder("UPDATE \"");
        update.append(featureInfo.getTableName()).append("\" SET ");

        boolean first = true;

        for (final Object name : map.keySet()) {
            final Object value = map.get(name);
            if ((value instanceof Geometry)) {
                continue;
            }
            if (!first) {
                update.append(", ");
            } else {
                first = false;
            }

            String valueString;
            if (value instanceof String) {
                valueString = "'" + value + "'";
            } else {
                valueString = value.toString();
            }
            update.append(name).append(" = ").append(valueString);
        }
        update.append(" WHERE id = ").append(getId());
        st.execute(update.toString());

        super.getProperties().clear();
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
        g = featureInfo.getGeometryFromCache(getId());

        if (g != null) {
            return g;
        }

        try {
            final PreparedStatement ps = featureInfo.getGeometryStatement();
            ps.setInt(1, getId());
            final ResultSet rs = ps.executeQuery();
//                ResultSet rs = shapeInfo.getStatement().executeQuery("select the_geom from poly where id = " + getId());
            if (rs.next()) {
                g = (Geometry)rs.getObject(1);
            }
//                g.setSRID(shapeInfo.getSrid());
        } catch (final Exception e) {
            logger.error("Cannot read geometry from shape file.", e);
        }

        featureInfo.addGeometryToCache(getId(), g);

        return g;
    }

    @Override
    public void setGeometry(final Geometry geom) {
        // do nothing
    }

//    public PFeature getPFeature() {
//        return pfeature;
//    }
//
//    public void setPFeature(PFeature pfeature) {
//        this.pfeature = pfeature;
//    }
}
