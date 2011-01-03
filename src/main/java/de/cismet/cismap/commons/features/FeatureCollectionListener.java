/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.MappingModelEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FeatureCollectionListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    void featuresAdded(FeatureCollectionEvent fce);
    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    void allFeaturesRemoved(FeatureCollectionEvent fce);
    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    void featuresRemoved(FeatureCollectionEvent fce);
    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    void featuresChanged(FeatureCollectionEvent fce);
    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    void featureSelectionChanged(FeatureCollectionEvent fce);
    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    void featureReconsiderationRequested(FeatureCollectionEvent fce);
    /**
     * DOCUMENT ME!
     */
    void featureCollectionChanged();
}
