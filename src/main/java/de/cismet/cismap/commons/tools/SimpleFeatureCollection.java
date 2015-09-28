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
package de.cismet.cismap.commons.tools;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.AbstractFeatureCollection;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Polygon;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.PropertyPath;

import java.math.BigDecimal;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SimpleFeatureCollection extends AbstractFeatureCollection {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SimpleFeatureCollection.class);

    //~ Instance fields --------------------------------------------------------

    List<String[]> aliasAttributeList;

    private Feature[] features;
    private Map<String, FeatureServiceAttribute> attributeMap;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleFeatureCollection object.
     *
     * @param  id                  DOCUMENT ME!
     * @param  features            The feature list. This list should not be null or empty
     * @param  aliasAttributeList  DOCUMENT ME!
     */
    public SimpleFeatureCollection(final String id,
            final de.cismet.cismap.commons.features.FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList) {
        this(id, features, aliasAttributeList, null);
    }

    /**
     * Creates a new SimpleFeatureCollection object.
     *
     * @param  id                  DOCUMENT ME!
     * @param  features            The feature list. This list should not be null or empty
     * @param  aliasAttributeList  DOCUMENT ME!
     * @param  attributeMap        contains all attribute descriptors
     */
    public SimpleFeatureCollection(final String id,
            final de.cismet.cismap.commons.features.FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList,
            final Map<String, FeatureServiceAttribute> attributeMap) {
        super(id);
        this.features = new Feature[features.length];
        this.aliasAttributeList = aliasAttributeList;
        this.attributeMap = attributeMap;
        int index = 0;

        if (aliasAttributeList == null) {
            this.aliasAttributeList = generateAliasAttributeList(features);
        }

        if (features.length > 0) {
            final PropertyType[] propTypeArray = getPropertyTypes(features[0]);

            for (final FeatureServiceFeature tmp : features) {
                this.features[index++] = toDeegreeFeature(tmp, propTypeArray);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   features  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String[]> generateAliasAttributeList(
            final de.cismet.cismap.commons.features.FeatureServiceFeature[] features) {
        final List<String[]> aliasAttrList = new ArrayList<String[]>();
        Map props = null;

        if (attributeMap != null) {
            props = attributeMap;
        } else if ((features[0].getLayerProperties() != null)
                    && (features[0].getLayerProperties().getFeatureService() != null)
                    && (features[0].getLayerProperties().getFeatureService().getFeatureServiceAttributes() != null)) {
            props = features[0].getLayerProperties().getFeatureService().getFeatureServiceAttributes();
        } else {
            // the feature properties contains always a geometry property. Even, if the feature should not have any
            // geometry
            props = features[0].getProperties();
        }

        if ((features != null) && (features.length > 0)) {
            for (final Object key : props.keySet()) {
                final String[] aliasAttr = new String[2];
                aliasAttr[0] = key.toString();
                aliasAttr[1] = key.toString();
                aliasAttrList.add(aliasAttr);
            }
        }
        return aliasAttrList;
    }

    @Override
    public FeatureProperty getDefaultProperty(final PropertyPath pp) throws PropertyPathResolvingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProperty(final FeatureProperty fp, final int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addProperty(final FeatureProperty fp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeProperty(final QualifiedName qn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replaceProperty(final FeatureProperty fp, final FeatureProperty fp1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feature cloneDeep() throws CloneNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        features = null;
    }

    @Override
    public Feature getFeature(final int i) {
        if (features != null) {
            return features[i];
        }

        return null;
    }

    @Override
    public Feature getFeature(final String string) {
        if (features != null) {
            for (int i = 0; i < features.length; i++) {
                if (features[i].getId().equals(string)) {
                    return features[i];
                }
            }
        }
        return null;
    }

    @Override
    public Feature[] toArray() {
        return features;
    }

    @Override
    public Iterator<Feature> iterator() {
        return Arrays.asList(features).iterator();
    }

    @Override
    public void add(final Feature ftr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feature remove(final Feature ftr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feature remove(final int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        return features.length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature        DOCUMENT ME!
     * @param   propTypeArray  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature toDeegreeFeature(final FeatureServiceFeature feature, final PropertyType[] propTypeArray) {
        final Map props = feature.getProperties();
        final DefaultFeatureProperty[] propArray = new DefaultFeatureProperty[aliasAttributeList.size()];
        int index = 0;

        for (final String[] aliasAttr : aliasAttributeList) {
            final QualifiedName name = new QualifiedName(aliasAttr[0]);
            Object value = props.get(aliasAttr[1]);

            if (value instanceof Geometry) {
                try {
                    value = JTSAdapter.wrap((Geometry)value);
                } catch (GeometryException ex) {
                    LOG.error("Cannot convert JTS geometry to deegree geometry.", ex);
                }
            }
            propArray[index++] = new DefaultFeatureProperty(name, value);
        }

        final FeatureType ft = FeatureFactory.createFeatureType("test", false, propTypeArray);
        return FeatureFactory.createFeature(String.valueOf(feature.getId()), ft, propArray);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PropertyType[] getPropertyTypes(final FeatureServiceFeature feature) {
        final Map props = feature.getProperties();
        final Map<String, FeatureServiceAttribute> featureServiceAttributes;

        if (attributeMap != null) {
            featureServiceAttributes = attributeMap;
        } else {
            featureServiceAttributes = feature.getLayerProperties().getFeatureService().getFeatureServiceAttributes();
        }
        final PropertyType[] propTypeArray = new PropertyType[aliasAttributeList.size()];
        int index = 0;

        for (final String[] aliasAttr : aliasAttributeList) {
            final QualifiedName name = new QualifiedName(aliasAttr[0]);
            Object value = props.get(aliasAttr[1]);

            if (value instanceof Geometry) {
                try {
                    value = JTSAdapter.wrap((Geometry)value);
                } catch (GeometryException ex) {
                    LOG.error("Cannot convert JTS geometry to deegree geometry.", ex);
                }
            }
            FeatureServiceAttribute attr = featureServiceAttributes.get(aliasAttr[1]);
            if (attr == null) {
                attr = featureServiceAttributes.get(aliasAttr[1].toUpperCase());
            }
            propTypeArray[index++] = getPropertyType(name, attr, value);
        }

        return propTypeArray;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name   DOCUMENT ME!
     * @param   attr   DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PropertyType getPropertyType(final QualifiedName name,
            final FeatureServiceAttribute attr,
            final Object value) {
        if ((attr != null) && !attr.isGeometry()) {
            final Class cl = FeatureTools.getClass(attr);
            if (cl.equals(String.class)) {
                return new SimplePropertyType(name, Types.VARCHAR, 0, 1);
            } else if (cl.equals(Integer.class)) {
                return new SimplePropertyType(name, Types.INTEGER, 0, 1);
            } else if (cl.equals(Long.class)) {
                return new SimplePropertyType(name, Types.BIGINT, 0, 1);
            } else if (cl.equals(Double.class)) {
                return new SimplePropertyType(name, Types.DOUBLE, 0, 1);
            } else if (cl.equals(Date.class)) {
                return new SimplePropertyType(name, Types.TIMESTAMP, 0, 1);
            } else if (cl.equals(Boolean.class)) {
                return new SimplePropertyType(name, Types.BOOLEAN, 0, 1);
            } else if (cl.equals(BigDecimal.class)) {
                return new SimplePropertyType(name, Types.NUMERIC, 0, 1);
            } else {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unknown attribute type found. Try to convert it to a deegree type");
                    }
                    final int deegreeType = Integer.parseInt(attr.getType());

                    return new SimplePropertyType(name, deegreeType, 0, 1);
                } catch (NumberFormatException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unknown attribute type cannot be converted to a deegree type.", e);
                    }
                }
            }
        }

        if (value instanceof Integer) {
            return new SimplePropertyType(name, Types.INTEGER, 0, 1);
        } else if (value instanceof Double) {
            return new SimplePropertyType(name, Types.DOUBLE, 0, 1);
        } else if (value instanceof String) {
            return new SimplePropertyType(name, Types.VARCHAR, 0, 1);
        } else if (value instanceof Float) {
            return new SimplePropertyType(name, Types.FLOAT, 0, 1);
        } else if (value instanceof Polygon) {
            return new SimplePropertyType(name, Types.CURVE, 0, 1);
        } else if (value instanceof Point) {
            return new SimplePropertyType(name, Types.POINT, 0, 1);
        } else if (value instanceof MultiPoint) {
            return new SimplePropertyType(name, Types.MULTIPOINT, 0, 1);
        } else if (value instanceof Long) {
            return new SimplePropertyType(name, Types.BIGINT, 0, 1);
        } else if (value instanceof Curve) {
            return new SimplePropertyType(name, Types.CURVE, 0, 1);
        } else if (value instanceof MultiCurve) {
            return new SimplePropertyType(name, Types.MULTICURVE, 0, 1);
        } else if (value instanceof Surface) {
            return new SimplePropertyType(name, Types.SURFACE, 0, 1);
        } else if (value instanceof MultiSurface) {
            return new SimplePropertyType(name, Types.MULTISURFACE, 0, 1);
        } else if (value instanceof Timestamp) {
            return new SimplePropertyType(name, Types.TIMESTAMP, 0, 1);
        } else {
            LOG.error("unbekannter Typ: " + value.getClass().getName());
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DefaultFeatureProperty implements FeatureProperty {

        //~ Instance fields ----------------------------------------------------

        private Object value = null;

        private QualifiedName name = null;

        //~ Constructors -------------------------------------------------------

        /**
         * constructor for complete initializing the FeatureProperty.
         *
         * @param  name   qualified name of the property
         * @param  value  the properties value
         */
        DefaultFeatureProperty(final QualifiedName name, final Object value) {
            setValue(value);
            this.name = name;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * returns the qualified name of the property.
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public QualifiedName getName() {
            return name;
        }

        /**
         * returns the value of the property.
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Object getValue() {
            return value;
        }

        /**
         * returns the value of the property; if the value is null the passed defaultValuewill be returned.
         *
         * @param   defaultValue  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Object getValue(final Object defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        /**
         * sets the value of the property.
         *
         * @param  value  DOCUMENT ME!
         */
        @Override
        public void setValue(final Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            String ret = null;
            ret = "name = " + name + "\n";
            ret += "value = " + value + "\n";
            return ret;
        }

        @Override
        public Feature getOwner() {
            return null;
        }
    }
}
