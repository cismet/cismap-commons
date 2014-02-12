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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Features read from a SHP File.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ShapeFeature extends DefaultFeatureServiceFeature {

    //~ Static fields/initializers ---------------------------------------------

    // caches the last feature properties
    private static int lastFeatureId = -1;
    private static LinkedHashMap<String, Object> lastFeatureContainer = null;

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
        final LinkedHashMap<String, Object> container = new LinkedHashMap<String, Object>();

        if (shapeInfo.getFc() == null) {
            try {
                if ((lastFeatureId == getId()) && (lastFeatureContainer != null)) {
                    return lastFeatureContainer;
                }

                final org.deegree.model.feature.Feature degreeFeature = shapeInfo.getFile().getFeatureByRecNo(getId());
                final FeatureProperty[] featureProperties = degreeFeature.getProperties();

                for (final FeatureProperty fp : featureProperties) {
                    container.put(fp.getName().getAsString(), fp.getValue());
                }

                lastFeatureId = getId();
                lastFeatureContainer = container;
            } catch (final Exception e) {
                logger.error("Cannot read properties from file.", e);
            }
        } else {
            try {
                final org.deegree.model.feature.Feature degreeFeature = shapeInfo.getFc().getFeature(getId());
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

        if (shapeInfo.getFc() == null) {
            try {
                g = JTSAdapter.export(shapeInfo.getFile().getGeometryByRecNo(getId()));
                g.setSRID(shapeInfo.getSrid());
            } catch (final Exception e) {
                logger.error("Cannot read geometry from shape file.", e);
            }
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
}
