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
package de.cismet.cismap.commons.tools;

import java.io.File;

import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface ShapeWriter {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   features            DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   fileToSaveTo        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void writeShape(FeatureServiceFeature[] features, List<String[]> aliasAttributeList, File fileToSaveTo)
            throws Exception;
    /**
     * DOCUMENT ME!
     *
     * @param   features            DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   fileToSaveTo        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void writeDbf(FeatureServiceFeature[] features, List<String[]> aliasAttributeList, File fileToSaveTo)
            throws Exception;
}
