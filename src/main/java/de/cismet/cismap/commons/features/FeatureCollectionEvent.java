/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class FeatureCollectionEvent {

    //~ Instance fields --------------------------------------------------------

    private Collection<Feature> eventFeatures;
    private FeatureCollection featureCollection;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FeatureCollectionEvent.
     *
     * @param  fc        DOCUMENT ME!
     * @param  features  DOCUMENT ME!
     */
    public FeatureCollectionEvent(final FeatureCollection fc, final Collection<Feature> features) {
        this.setEventFeatures(features);
        this.setFeatureCollection(fc);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Feature> getEventFeatures() {
        return eventFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  features  DOCUMENT ME!
     */
    public void setEventFeatures(final Collection<Feature> features) {
        this.eventFeatures = features;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureCollection  DOCUMENT ME!
     */
    public void setFeatureCollection(final FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }
}
