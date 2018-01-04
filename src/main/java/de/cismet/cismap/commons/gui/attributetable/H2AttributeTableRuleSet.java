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
package de.cismet.cismap.commons.gui.attributetable;

import java.util.List;

import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface H2AttributeTableRuleSet extends AttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  refInfos      DOCUMENT ME!
     * @param  geometryType  DOCUMENT ME!
     * @param  attributes    DOCUMENT ME!
     * @param  tableName     DOCUMENT ME!
     */
    void init(final List<LinearReferencingInfo> refInfos,
            final String geometryType,
            final List<FeatureServiceAttribute> attributes,
            final String tableName);

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    LinearReferencingInfo getInfoForColumn(final String columnName);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<LinearReferencingInfo> getAllLinRefInfos();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    H2AttributeTableRuleSet clone();

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    void startEditMode(final JDBCFeature feature);
}
