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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

import org.h2.jdbc.JdbcClob;

import java.awt.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.BufferedReader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.factory.JDBCFeatureFactory;
import de.cismet.cismap.commons.gui.attributetable.H2AttributeTableRuleSet;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.cismap.linearreferencing.tools.StationEditorInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JDBCFeature extends DefaultFeatureServiceFeature implements ModifiableFeature {

    //~ Static fields/initializers ---------------------------------------------

    // caches the last feature properties
    private static final Object sync = new Object();
    private static final String DELETE_STATEMENT = "DELETE FROM \"%1s\" WHERE \"%2s\" = %3s;";

    //~ Instance fields --------------------------------------------------------

    private Map<String, StationEditorInterface> stations = null;
    private Color backgroundColor;

    private final JDBCFeatureInfo featureInfo;
    private boolean modified = false;
    private boolean deleted = false;

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
    public void setEditable(final boolean editable) {
        final boolean oldEditableStatus = isEditable();
        super.setEditable(editable);

        if (oldEditableStatus != editable) {
            modified = false;

            if (!editable && (stations != null)) {
                for (final String key : stations.keySet()) {
                    stations.get(key).dispose();
                }
                stations.clear();
            } else {
                if (!editable
                            || CismapBroker.getInstance().getMappingComponent().getFeatureCollection().contains(this)) {
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unholdFeature(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(this);
                }
            }

            if (editable) {
                final H2AttributeTableRuleSet tableRuleSet = (H2AttributeTableRuleSet)getLayerProperties()
                            .getAttributeTableRuleSet();

                if (!((tableRuleSet.getAllLinRefInfos() != null) && !tableRuleSet.getAllLinRefInfos().isEmpty())) {
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                    SelectionManager.getInstance().addSelectedFeatures(Collections.nCopies(1, this));
                    setBackgroundColor(new Color(255, 91, 0));
                } else {
                    tableRuleSet.startEditMode(this);
                }
            }
        }
    }

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
                    container.put(rs.getMetaData().getColumnName(i + 1), getPrepareObject(rs.getObject(i + 1)));
                }
            }

            featureInfo.addPropertiesToCache(id, container);
        } catch (final Exception e) {
            logger.error("Cannot read properties from the database.", e);
        }

        return container;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   propertyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
                    result = getPrepareObject(rs.getObject(1));
                }

                try {
                    rs.close();
                } catch (Exception e) {
                    logger.error("Error while closing H2 result set", e);
                }
                featureInfo.addPropertyToCache(cacheId, result);
            } catch (final Exception e) {
                logger.error("Cannot read property from the database.", e);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object getPrepareObject(final Object o) {
        if (o instanceof JdbcClob) {
            try {
                final BufferedReader r = new BufferedReader(((JdbcClob)o).getCharacterStream());
                String tmp;
                final StringBuilder resultString = new StringBuilder();

                while ((tmp = r.readLine()) != null) {
                    resultString.append(tmp).append('\n');
                }

                return resultString.toString();
            } catch (Exception e) {
                logger.error("Error while reading clob", e);
                return null;
            }
        } else {
            return o;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyName   DOCUMENT ME!
     * @param  propertyValue  DOCUMENT ME!
     */
    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
        if (!existProperties()) {
            super.setProperties(getProperties());
        }

        super.addProperty(propertyName, propertyValue);

        featureInfo.clearCache();
        if (isEditable()) {
            modified = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean existProperties() {
        return !super.getProperties().isEmpty();
    }

    @Override
    public FeatureServiceFeature saveChanges() throws Exception {
        saveChangesWithoutReload();

        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private boolean existsInDB() throws Exception {
        final String checkSql = "SELECT \"%s\" FROM \"%s\" WHERE \"%s\" = %s";
        final Statement st = featureInfo.getConnection().createStatement();

        try {
            final String sql = String.format(
                    checkSql,
                    featureInfo.getIdField(),
                    featureInfo.getTableName(),
                    featureInfo.getIdField(),
                    getId());
            final ResultSet rs = st.executeQuery(sql);
            final boolean alreadyExists = ((rs != null) && rs.next());

            if (rs != null) {
                rs.close();
            }

            return alreadyExists;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    @Override
    public void saveChangesWithoutReload() throws Exception {
        if (!existProperties() || deleted) {
            // no changes
            return;
        }

        if (stations != null) {
            for (final String name : stations.keySet()) {
                setGeometry(stations.get(name).getGeometry());
                if (stations.get(name).getGeometry() instanceof LineString) {
                    break;
                }
            }
        }

        final Statement st = featureInfo.getConnection().createStatement();

        try {
            if (existsInDB()) {
                updateFeature(st);
            } else {
                addFeature(st);
            }

            if ((getLayerProperties() != null) && (getLayerProperties().getFeatureService() != null)) {
                final JDBCFeatureFactory factory = (JDBCFeatureFactory)getLayerProperties().getFeatureService()
                            .getFeatureFactory();

                if (factory instanceof H2FeatureServiceFactory) {
                    final Geometry envelope = factory.getEnvelope();

                    if ((getGeometry() != null) && (envelope != null) && !envelope.contains(getGeometry())) {
                        ((H2FeatureServiceFactory)factory).determineEnvelope();
                    }
                }
            }
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void saveChangesWithoutUpdateEnvelope() throws Exception {
        if (!existProperties() || deleted) {
            // no changes
            return;
        }

        if (stations != null) {
            for (final String name : stations.keySet()) {
                setGeometry(stations.get(name).getGeometry());
                if (stations.get(name).getGeometry() instanceof LineString) {
                    break;
                }
            }
        }

        final Statement st = featureInfo.getConnection().createStatement();

        try {
            if (existsInDB()) {
                updateFeature(st);
            } else {
                addFeature(st);
            }
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   st  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void updateFeature(final Statement st) throws Exception {
        final HashMap map = super.getProperties();
        final StringBuilder update = new StringBuilder("UPDATE \"");
        update.append(featureInfo.getTableName()).append("\" SET ");

        boolean first = true;

        for (final Object name : map.keySet()) {
            final Object value = map.get(name);
            if (!first) {
                update.append(", ");
            } else {
                first = false;
            }

            String valueString;
            if ((value instanceof String) || (value instanceof Geometry) || (value instanceof java.sql.Timestamp)) {
                valueString = "'" + value + "'";
            } else {
                valueString = String.valueOf(value);
            }
            update.append("\"").append(name).append("\"").append(" = ").append(valueString);
        }
        update.append(" WHERE \"" + featureInfo.getIdField() + "\" = ").append(getId());
        st.executeUpdate(update.toString());

        super.getProperties().clear();
        featureInfo.clearCache();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   st  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void addFeature(final Statement st) throws Exception {
        final HashMap map = super.getProperties();
        final String insertSql = "INSERT INTO \"%1s\" (%2s) VALUES (%3s)";
        final List<String> attributes = new ArrayList<String>();
        final List<String> values = new ArrayList<String>();
        boolean idExists = false;

        for (final Object name : map.keySet()) {
            final Object value = map.get(name);
            String valueString;

            if ((value instanceof String) || (value instanceof Geometry) || (value instanceof java.sql.Timestamp)) {
                valueString = "'" + value + "'";
            } else {
                valueString = String.valueOf(value);
            }

            attributes.add("\"" + String.valueOf(name) + "\"");
            values.add(valueString);

            if (name.equals("id")) {
                idExists = true;
            }
        }
        if (!idExists) {
            attributes.add("\"" + String.valueOf("id") + "\"");
            values.add(String.valueOf(getId()));
        }

        final String query = String.format(
                insertSql,
                featureInfo.getTableName(),
                listToString(attributes),
                listToString(values));
        st.executeUpdate(query);

        super.getProperties().clear();
        featureInfo.clearCache();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String listToString(final List<String> attributes) {
        boolean firstElement = true;
        final StringBuilder sb = new StringBuilder();

        for (final String element : attributes) {
            if (firstElement) {
                firstElement = false;
            } else {
                sb.append(",");
            }

            sb.append(element);
        }

        return sb.toString();
    }

    @Override
    public void delete() throws Exception {
        // this is required for a clean restore
        super.getProperties().clear();
        super.setProperties(getProperties());

        final String deleteStat = String.format(
                DELETE_STATEMENT,
                featureInfo.getTableName(),
                featureInfo.getIdField(),
                getId());
        final Statement st = featureInfo.getConnection().createStatement();
        st.executeUpdate(deleteStat);
        deleted = true;
    }

    @Override
    public void restore() throws Exception {
        deleted = false;
        saveChangesWithoutReload();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void undoAll() {
        try {
            if (existsInDB()) {
                super.getProperties().clear();
            }
        } catch (Exception e) {
            logger.error("Error while undo.", e);
            super.getProperties().clear();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  properties  colName properties DOCUMENT ME!
     */
    @Override
    public void setProperties(final HashMap properties) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @param   colName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getStationEditor(final String colName) {
        if (stations != null) {
            return stations.get(colName);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  colName  DOCUMENT ME!
     * @param  editor   DOCUMENT ME!
     */
    public void setStationEditor(final String colName, final StationEditorInterface editor) {
        if (stations == null) {
            stations = new HashMap<String, StationEditorInterface>();
        }

        stations.put(colName, editor);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PropertyChangeListener getPropertyChangeListener() {
        return new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    for (final String name : stations.keySet()) {
                        setProperty(name, stations.get(name).getValue());
                        firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
                    }
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyName  DOCUMENT ME!
     * @param  property      DOCUMENT ME!
     */
    @Override
    public void addProperty(final String propertyName, final Object property) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @param  map  DOCUMENT ME!
     */
    @Override
    public void addProperties(final Map<String, Object> map) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Geometry getGeometry() {
        if (existProperties()) {
            return (Geometry)super.getProperty(featureInfo.getGeoField());
        } else {
            return getOriginalGeometry();
        }
    }

    /**
     * Provides the geometry from the database. The content of the property container will be ignored.
     *
     * @return  The geometry that is currently saved within the database
     */
    private Geometry getOriginalGeometry() {
        Geometry g = null;
        g = featureInfo.getGeometryFromCache(getId());
        if (g != null) {
            final GeometryFactory fg = new GeometryFactory(g.getPrecisionModel(),
                    g.getSRID(),
                    CoordinateArraySequenceFactory.instance());
            g = fg.createGeometry(g);
        }
        if (g != null) {
            return toSerializableGeometry(g);
        }

        ResultSet rs = null;

        if (featureInfo.getGeometryStatement() == null) {
            // the feature has no geometry
            return null;
        }

        try {
            synchronized (sync) {
                final PreparedStatement ps = featureInfo.getGeometryStatement();
                ps.setInt(1, getId());
                rs = ps.executeQuery();
                if (rs.next()) {
                    g = (Geometry)rs.getObject(1);
                    g.setSRID(featureInfo.getSrid());
                }
            }
        } catch (final Exception e) {
            logger.error("Cannot read geometry from the database.", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // nothing to do
                }
            }
        }

        featureInfo.addGeometryToCache(getId(), g);

        return toSerializableGeometry(g);
    }

    /**
     * The geometry is not serializable, if the com.vividsolutions.jts.geom.impl.PackedCoordinateSequence is used. So
     * this method replaces the PackedCoordinateSequence with the CoordinateArraySequenceFactory.
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry toSerializableGeometry(final Geometry g) {
        if (g instanceof LineString) {
            final LineString ls = (LineString)g;
            if (ls.getCoordinateSequence() instanceof com.vividsolutions.jts.geom.impl.PackedCoordinateSequence) {
                final GeometryFactory fg = new GeometryFactory(g.getPrecisionModel(),
                        g.getSRID(),
                        CoordinateArraySequenceFactory.instance());
                final Geometry newGeometry = fg.createGeometry(g);

                return newGeometry;
            }
        }

        return g;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    @Override
    public void setGeometry(final Geometry geom) {
        if (!existProperties()) {
            super.setProperties(getProperties());
        }
        final Geometry oldGeom = getGeometry();

        if (((oldGeom == null) != (geom == null))
                    || (((oldGeom != null) && (geom != null)
                            && (!oldGeom.getEnvelope().equalsExact(geom.getEnvelope()))) || !oldGeom.equalsExact(
                            geom))) {
            // the old geometry and the new geometry are different
            featureInfo.clearCache();
            super.addProperty(featureInfo.getGeoField(), geom);
            if (isEditable()) {
                modified = true;
            }
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof JDBCFeature) {
            final JDBCFeature other = (JDBCFeature)obj;

            if ((getId() != -1) || (other.getId() != -1)) {
                return featureInfo.getTableName().equals(other.featureInfo.getTableName())
                            && (getId() == other.getId());
            } else {
                return obj == other;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (41 * hash) + this.getId();
        hash = (41 * hash)
                    + (((this.featureInfo != null) && (this.featureInfo.getTableName() != null))
                        ? this.featureInfo.getTableName().hashCode() : 0);
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the backgroundColor
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  backgroundColor  the backgroundColor to set
     */
    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public boolean isFeatureChanged() {
        final Geometry geom = getGeometry();
        final Geometry backupGeometry = getOriginalGeometry();

        if (((backupGeometry == null) != (geom == null))
                    || ((backupGeometry != null) && (geom != null) && !backupGeometry.equalsExact(geom))) {
            // The geometry will not changed with the setGeometry() method, but also within the geometry object itself.
            return true;
        } else {
            return modified;
        }
    }
}
