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
package de.cismet.cismap.commons.gui.attributetable;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.PropertyContainer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTableFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static AttributeTableFactory instance = null;
    private static final Logger LOG = Logger.getLogger(AttributeTableFactory.class);
    private MappingComponent mappingComponent;

    static {
        instance = new AttributeTableFactory();
    }

    //~ Instance fields --------------------------------------------------------

    private AttributeTableListener listener = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AttributeTableFactory object.
     */
    private AttributeTableFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AttributeTableFactory getInstance() {
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureService  DOCUMENT ME!
     */
    public void showAttributeTable(final AbstractFeatureService featureService) {
        final FeatureFactory factory = featureService.getFeatureFactory();

        try {
            final AttributeTable table = new AttributeTable(featureService);
            table.setMappingComponent(mappingComponent);
                    
            listener.showPanel(
                table,
                featureService.getName(),
                "Attribute "
                        + featureService.getName(),
                featureService.getName());
        } catch (Exception e) {
            LOG.error("Error while retrieving all features", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void setAttributeTableListener(final AttributeTableListener listener) {
        this.listener = listener;
    }

    /**
     * @return the mappingComponent
     */
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * @param mappingComponent the mappingComponent to set
     */
    public void setMappingComponent(MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }
}
