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

import javax.swing.JPanel;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface AttributeTableListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  table    panel DOCUMENT ME!
     * @param  id       DOCUMENT ME!
     * @param  name     DOCUMENT ME!
     * @param  tooltip  DOCUMENT ME!
     */
    void showAttributeTable(AttributeTable table, String id, String name, String tooltip);

    /**
     * DOCUMENT ME!
     *
     * @param  featureService  table panel DOCUMENT ME!
     * @param  id              DOCUMENT ME!
     */
    void switchProcessingMode(AbstractFeatureService featureService, String id);

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    AttributeTable getAttributeTable(final String id);

    /**
     * DOCUMENT ME!
     *
     * @param  id    DOCUMENT ME!
     * @param  name  DOCUMENT ME!
     */
    void changeName(String id, String name);

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     * @param  active   DOCUMENT ME!
     */
    void processingModeChanged(AbstractFeatureService service, boolean active);

    /**
     * Closes the given Attribute table, if it is open.
     *
     * @param  service  featureService the table to close
     */
    void closeAttributeTable(AbstractFeatureService service);
}
