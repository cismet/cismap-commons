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

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.cismet.cismap.commons.util.SimpleCache;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JDBCFeatureInfo {

    //~ Static fields/initializers ---------------------------------------------

    // caches the last feature properties
    private static final Logger LOG = Logger.getLogger(JDBCFeatureInfo.class);

    //~ Instance fields --------------------------------------------------------

    private final SimpleCache<LinkedHashMap<String, Object>> cache = new SimpleCache<LinkedHashMap<String, Object>>(2);
    private final SimpleCache<Geometry> geoCache = new SimpleCache<Geometry>(1);
    private final SimpleCache<Object> propertyCache = new SimpleCache<Object>(10);

    private int srid;
    private Connection connection;
    private PreparedStatement geometryStatement;
    private PreparedStatement propertiesStatement;
    private String tableName;
    private final String geoField;
    private final Map<String, PreparedStatement> propStats = new HashMap<String, PreparedStatement>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeInfo object.
     *
     * @param  connection  file DOCUMENT ME!
     * @param  srid        DOCUMENT ME!
     * @param  geoField    fc DOCUMENT ME!
     * @param  tableName   DOCUMENT ME!
     */
    public JDBCFeatureInfo(final Connection connection,
            final int srid,
            final String geoField,
            final String tableName) {
        this.connection = connection;
        this.srid = srid;
        this.tableName = tableName;
        this.geoField = geoField;

        createStatements();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void createStatements() {
        try {
            geometryStatement = connection.prepareStatement("select " + getGeoField() + " from \"" + tableName
                            + "\" where id = ?");
        } catch (Exception e) {
            LOG.error("Error while creating prepared statement for geometries", e);
        }
        try {
            propertiesStatement = connection.prepareStatement("select * from \"" + tableName + "\" where id = ?");
        } catch (Exception e) {
            LOG.error("Error while creating prepared statement for properties", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   property  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PreparedStatement getPreparedStatementForProperty(final String property) {
        PreparedStatement ps = propStats.get(property);

        if (ps == null) {
            try {
                final String query = "select " + property + " from \"" + tableName + "\" WHERE id = ?";
                ps = connection.prepareStatement(query);
                propStats.put(property, ps);
            } catch (Exception e) {
                LOG.error("Error while creating prepared statement for property" + property, e);
            }
        }

        return ps;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the srid
     */
    public int getSrid() {
        return srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srid  the srid to set
     */
    public void setSrid(final int srid) {
        this.srid = srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized LinkedHashMap<String, Object> getPropertiesFromCache(final int id) {
        return cache.get(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id         DOCUMENT ME!
     * @param  container  DOCUMENT ME!
     */
    public synchronized void addPropertiesToCache(final int id, final LinkedHashMap<String, Object> container) {
        cache.add(id, container);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized Object getPropertyFromCache(final String id) {
        return propertyCache.get(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id         DOCUMENT ME!
     * @param  container  DOCUMENT ME!
     */
    public synchronized void addPropertyToCache(final String id, final Object container) {
        propertyCache.add(id, container);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized Geometry getGeometryFromCache(final int id) {
        return geoCache.get(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id   DOCUMENT ME!
     * @param  geo  container DOCUMENT ME!
     */
    public synchronized void addGeometryToCache(final int id, final Geometry geo) {
        geoCache.add(id, geo);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the statement
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connection  statement the statement to set
     */
    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the geometryStatement
     */
    public PreparedStatement getGeometryStatement() {
        return geometryStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the propertiesStatement
     */
    public PreparedStatement getPropertiesStatement() {
        return propertiesStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tableName  DOCUMENT ME!
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
        propStats.clear();
        createStatements();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the geoField
     */
    public String getGeoField() {
        return geoField;
    }
}
