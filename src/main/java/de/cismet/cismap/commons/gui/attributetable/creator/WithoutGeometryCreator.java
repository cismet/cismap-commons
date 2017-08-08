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
package de.cismet.cismap.commons.gui.attributetable.creator;


import org.apache.log4j.Logger;

import org.openide.util.NbBundle;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;

/**
 * Creates new features, which use no geometry.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WithoutGeometryCreator extends AbstractFeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WithoutGeometryCreator.class);

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureCreatedListener> listener = new ArrayList<FeatureCreatedListener>();

    private AbstractFeatureService service = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrimitiveGeometryCreator object.
     */
    public WithoutGeometryCreator() {
        this(null);
    }

    /**
     * Creates a new PrimitiveGeometryCreator object.
     *
     * @param  properties  DOCUMENT ME!
     */
    public WithoutGeometryCreator(final Map<String, Object> properties) {
        this.properties = properties;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void createFeature(final MappingComponent mc,
            final FeatureServiceFeature feature) {
        if ((feature != null) && (feature.getLayerProperties() != null)) {
            service = feature.getLayerProperties().getFeatureService();
        }
        if (feature instanceof DefaultFeatureServiceFeature) {
            try {
                fillFeatureWithDefaultValues((DefaultFeatureServiceFeature)feature,
                    properties);
                ((DefaultFeatureServiceFeature)feature).saveChanges();
                fillFeatureWithDefaultValuesAfterSave((DefaultFeatureServiceFeature)feature,
                    properties);

                for (final FeatureCreatedListener featureCreatedListener : this.listener) {
                    featureCreatedListener.featureCreated(new FeatureCreatedEvent(
                            WithoutGeometryCreator.this,
                            feature));
                }
            } catch (Exception e) {
                LOG.error("Cannot save new feature", e);
            }
        }
    }

    @Override
    public void addFeatureCreatedListener(final FeatureCreatedListener listener) {
        this.listener.add(listener);
    }

    @Override
    public String getTypeName() {
        return NbBundle.getMessage(WithoutGeometryCreator.class, "PrimitiveGeometryCreator.getTypeName().other");
    }

    @Override
    public AbstractFeatureService getService() {
        return service;
    }
}
