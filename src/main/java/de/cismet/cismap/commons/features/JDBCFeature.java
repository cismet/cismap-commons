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

import java.awt.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.featureservice.H2AttributeTableRuleSet;
import de.cismet.cismap.commons.interaction.CismapBroker;

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
    private static final String DELETE_STATEMENT = "DELETE FROM \"%1s\" WHERE id = %2s;";

    //~ Instance fields --------------------------------------------------------

    private Map<String, StationEditorInterface> stations = null;
    private Color backgroundColor;

    private final JDBCFeatureInfo featureInfo;

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
            if (!editable && (stations != null)) {
                for (final String key : stations.keySet()) {
                    stations.get(key).dispose();
                }
                stations.clear();
            } else {
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unholdFeature(this);
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(this);
            }

            if (editable) {
                final H2AttributeTableRuleSet tableRuleSet = (H2AttributeTableRuleSet)getLayerProperties()
                            .getAttributeTableRuleSet();

                if (!((tableRuleSet.getAllLinRefInfos() != null) && !tableRuleSet.getAllLinRefInfos().isEmpty())) {
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                    setBackgroundColor(new Color(255, 91, 0));
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
                    container.put(rs.getMetaData().getColumnName(i + 1), rs.getObject(i + 1));
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
                    result = rs.getObject(1);
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
     * @param  propertyName   DOCUMENT ME!
     * @param  propertyValue  DOCUMENT ME!
     */
    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
        if (!existProperties()) {
            super.setProperties(getProperties());
        }

        if (getProperty(propertyName.toUpperCase()) != null) {
            super.addProperty(propertyName.toUpperCase(), propertyValue);
        } else {
            super.addProperty(propertyName, propertyValue);
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
    /**
     * DOCUMENT ME!
     *
     * @throws    Exception  DOCUMENT ME!
     *
     * @Override  DOCUMENT ME!
     */
    @Override
    public void saveChanges() throws Exception {
        if (!existProperties()) {
            return;
        }

        final String checkSql = "SELECT id FROM \"%1s\" WHERE id = %2s";
        final Statement st = featureInfo.getConnection().createStatement();

        try {
            final String sql = String.format(checkSql, featureInfo.getTableName(), getId());
            final ResultSet rs = st.executeQuery(sql);
            final boolean alreadyExists = ((rs != null) && rs.next());

            if (rs != null) {
                rs.close();
            }

            if (alreadyExists) {
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
            if ((value instanceof String) || (value instanceof Geometry)) {
                valueString = "'" + value + "'";
            } else {
                valueString = value.toString();
            }
            update.append(name).append(" = ").append(valueString);
        }
        update.append(" WHERE id = ").append(getId());
        st.executeUpdate(update.toString());

        super.getProperties().clear();
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

        for (final Object name : map.keySet()) {
            final Object value = map.get(name);
            String valueString;

            if ((value instanceof String) || (value instanceof Geometry)) {
                valueString = "'" + value + "'";
            } else {
                valueString = value.toString();
            }

            attributes.add(String.valueOf(name));
            values.add(valueString);
        }
        attributes.add(String.valueOf("id"));
        values.add(String.valueOf(getId()));

        final String query = String.format(
                insertSql,
                featureInfo.getTableName(),
                listToString(attributes),
                listToString(values));
        st.executeUpdate(query);

        super.getProperties().clear();
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
        final String deleteStat = String.format(DELETE_STATEMENT, featureInfo.getTableName(), getId());
        final Statement st = featureInfo.getConnection().createStatement();
        st.executeUpdate(deleteStat);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void undoAll() {
        super.getProperties().clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  properties  DOCUMENT ME!
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
        }
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
            logger.error("Cannot read geometry from the database.", e);
        }

        featureInfo.addGeometryToCache(getId(), g);

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

        super.addProperty(featureInfo.getGeoField(), geom);
    }

//    public PFeature getPFeature() {
//        return pfeature;
//    }
//
//    public void setPFeature(PFeature pfeature) {
//        this.pfeature = pfeature;
//    }

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
}
