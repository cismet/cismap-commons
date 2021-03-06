/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.attributetable.creator;

import java.util.Map;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

/**
 * The base class of feature creators, which are used to create new features.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractFeatureCreator implements FeatureCreator {

    //~ Instance fields --------------------------------------------------------

    protected Map<String, Object> properties;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  properties  the properties to set
     */
    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature     DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    protected static void fillFeatureWithDefaultValues(final DefaultFeatureServiceFeature feature,
            final Map<String, Object> properties) {
        if (properties != null) {
            for (final String propName : properties.keySet()) {
                final Object o = properties.get(propName);

                if ((o instanceof String) && ((String)o).startsWith("@")) {
                    final String referencedProperty = ((String)o).substring(1);
                    final Object value = feature.getProperty(referencedProperty);
                    feature.setProperty(propName, value);
                } else {
                    feature.setProperty(propName, o);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature     DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    protected static void fillFeatureWithDefaultValuesAfterSave(final DefaultFeatureServiceFeature feature,
            final Map<String, Object> properties) {
        if (properties != null) {
            for (final String propName : properties.keySet()) {
                final Object o = properties.get(propName);

                if ((o instanceof String) && ((String)o).startsWith("@")) {
                    final String referencedProperty = ((String)o).substring(1);
                    final Object value = feature.getProperty(referencedProperty);
                    feature.setProperty(propName, value);
                }
            }
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean isCreationAllowed(final MappingComponent mc) {
        return true;
    }
}
