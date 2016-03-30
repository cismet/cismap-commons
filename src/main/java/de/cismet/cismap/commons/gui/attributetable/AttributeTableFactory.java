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

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTableFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AttributeTableFactory.class);

    //~ Instance fields --------------------------------------------------------

    private MappingComponent mappingComponent;

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
        return LazyInitializer.INSTANCE;
    }

    /**
     * Opens the attribute table of the given service.
     *
     * @param  featureService  the service of the attribute table that should be opened
     */
    public void showAttributeTable(final AbstractFeatureService featureService) {
        try {
            final AttributeTable table = new AttributeTable(featureService);
            table.setMappingComponent(mappingComponent);

            listener.showAttributeTable(
                table,
                createId(featureService),
                NbBundle.getMessage(
                    AttributeTableFactory.class,
                    "AttributeTableFactory.showAttributeTable().name",
                    featureService.getName()),
                featureService.getName());
        } catch (Exception e) {
            LOG.error("Error while retrieving all features", e);
        }
    }

    /**
     * Switch the processing mode for the given service.
     *
     * @param  featureService  the service of the attribute table that should be opened
     */
    public void switchProcessingMode(final AbstractFeatureService featureService) {
        listener.switchProcessingMode(featureService, createId(featureService));
    }

    /**
     * Switch the processing mode for the given service.
     *
     * @param   featureService  the service of the attribute table that should be opened
     *
     * @return  DOCUMENT ME!
     */
    public AttributeTable getAttributeTable(final AbstractFeatureService featureService) {
        return listener.getAttributeTable(createId(featureService));
    }

    /**
     * Closes the attribute table of the given service.
     *
     * @param  featureService  the service of the attribute table that should be closed
     */
    public void closeAttributeTable(final AbstractFeatureService featureService) {
        listener.closeAttributeTable(featureService);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureService  DOCUMENT ME!
     * @param  newName         DOCUMENT ME!
     */
    public void changeAttributeTableName(final AbstractFeatureService featureService, final String newName) {
        try {
            listener.changeName(
                createId(featureService),
                newName);
        } catch (Exception e) {
            LOG.error("Error while retrieving all features", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureService  DOCUMENT ME!
     * @param  active          DOCUMENT ME!
     */
    public void processingModeChanged(final AbstractFeatureService featureService, final boolean active) {
        try {
            listener.processingModeChanged(featureService, active);
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
     * DOCUMENT ME!
     *
     * @return  the mappingComponent
     */
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mappingComponent  the mappingComponent to set
     */
    public void setMappingComponent(final MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String createId(final AbstractFeatureService service) {
        return "Attributtabelle " + service.getName();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient AttributeTableFactory INSTANCE = new AttributeTableFactory();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
