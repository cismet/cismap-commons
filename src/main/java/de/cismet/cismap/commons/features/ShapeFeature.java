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

import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.JTSAdapter;

import org.deegree.feature.Feature;
import org.deegree.feature.types.FeatureType;

import org.deegree.style.se.unevaluated.Style;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Features read from a SHP File.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ShapeFeature extends DefaultFeatureServiceFeature {

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

    public ShapeFeature(final ShapeInfo shapeInfo) {
        this.shapeInfo = shapeInfo;
    }
    
   /**
    * Creates a new ShapeFeature object.
    *
    * @param  typename  DOCUMENT ME!
    * @param  styles    DOCUMENT ME!
    */
    public ShapeFeature(final ShapeInfo shapeInfo, final List<org.deegree.style.se.unevaluated.Style> styles) {
        setSLDStyles(styles); // super.style = styles;
        this.shapeInfo = shapeInfo;
    }

    
   
    //~ Methods ----------------------------------------------------------------

    @Override
    public HashMap getProperties() {
        LinkedHashMap<String, Object> container = null;
        final int id = getId();
        if (existProperties()) {
            return super.getProperties();
        }

        if (shapeInfo.getFc() == null) {
            try {
                container = shapeInfo.getPropertiesFromCache(id);

                if (container != null) {
                    return container;
                } else {
                    container = new LinkedHashMap<String, Object>();
                }

                org.deegree.model.feature.Feature degreeFeature = null;
                synchronized (sync) {
                    // getFeatureByRecNo is not threadsafe
                    degreeFeature = shapeInfo.getFile().getFeatureByRecNo(id);
                }
                final FeatureProperty[] featureProperties = degreeFeature.getProperties();

                for (final FeatureProperty fp : featureProperties) {
                    container.put(fp.getName().getAsString(), fp.getValue());
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

    @Override
    public void saveChanges() throws Exception {
//        org.deegree.model.feature.Feature deegreeFeature = null;
//        synchronized (sync) {
//            deegreeFeature = shapeInfo.getFile().getFeatureByRecNo(getId());
//        }
//
//        Map<String, Object> map = super.getProperties();
//
//        final FeatureProperty[] featureProperties = deegreeFeature.getProperties();
//        for (final FeatureProperty fp : featureProperties) {
//            fp.setValue(map.get(fp.getName().getAsString()));
//        }
//
//        shapeInfo.getFile().writeShape(null);
//        super.getProperties().clear();
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
        if (shapeInfo.getFc() == null) {
            g = shapeInfo.getGeometryFromCache(getId());

            if (g != null) {
                return g;
            }

            try {
                g = JTSAdapter.export(shapeInfo.getFile().getGeometryByRecNo(getId()));
                g.setSRID(shapeInfo.getSrid());
            } catch (final Exception e) {
                logger.error("Cannot read geometry from shape file.", e);
            }

            shapeInfo.addGeometryToCache(getId(), g);
        } else {
            try {
                g = JTSAdapter.export(shapeInfo.getFc().getFeature(getId()).getDefaultGeometryPropertyValue());
                g.setSRID(shapeInfo.getSrid());
            } catch (final Exception e) {
                logger.error("Cannot read geometry from shape file.", e);
            }
        }

        return g;
    }

    @Override
    public void setGeometry(final Geometry geom) {
        // do nothing
    }
    
    //~ Methods ----------------------------------------------------------------

    @Override
    protected Feature getDeegreeFeature() {
        return new ShapeFileLayerDeegreeFeature();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class ShapeFileLayerDeegreeFeature extends DeegreeFeature {

        //~ Methods ------------------------------------------------------------

        @Override
        public FeatureType getType() {
            return new DeegreeFeatureType() {

                    @Override
                    public QName getName() {
                        return new QName("Feature");// for demo only
                    }
                };
        }
    }
}
