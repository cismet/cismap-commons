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
package de.cismet.cismap.commons.featureservice.style;

import java.awt.Frame;

import java.util.ArrayList;

import javax.swing.JDialog;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public interface StyleDialogInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   FeatureService    DOCUMENT ME!
     * @param   parentFrame       DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     * @param   configTabs        Possible values are "Darstellung", "Massstab", "Thematische Farbgebung",
     *                            "Beschriftung", "Begleitsymbole"
     *
     * @return  DOCUMENT ME!
     */
    JDialog configureDialog(final AbstractFeatureService FeatureService,
            final Frame parentFrame,
            final MappingComponent mappingComponent,
            ArrayList<String> configTabs);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Runnable createResultTask();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isAccepted();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getKey();
}
