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

import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface AttributeTableFieldCalculation {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   table        DOCUMENT ME!
     * @param   service      DOCUMENT ME!
     * @param   attribute    DOCUMENT ME!
     * @param   featureList  DOCUMENT ME!
     *
     * @return  true, if the calculation was started
     */
    boolean openPanel(AttributeTable table,
            AbstractFeatureService service,
            FeatureServiceAttribute attribute,
            final List<FeatureServiceFeature> featureList);
}
