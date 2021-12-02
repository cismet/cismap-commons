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

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface AttributeTableExtendedRuleSet {

    //~ Methods ----------------------------------------------------------------

    /**
     * Will be invoked before the service will be saved.
     *
     * @param   features  the modified features
     *
     * @return  true, if the save operation should be executed
     */
    ErrorDetails prepareForSaveWithDetails(final List<FeatureServiceFeature> features);

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    public static class ErrorDetails {

        //~ Instance fields ----------------------------------------------------

        FeatureServiceFeature feature;
        String column;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ErrorDetails object.
         *
         * @param  feature  DOCUMENT ME!
         * @param  column   DOCUMENT ME!
         */
        public ErrorDetails(final FeatureServiceFeature feature, final String column) {
            this.feature = feature;
            this.column = column;
        }
    }
}
