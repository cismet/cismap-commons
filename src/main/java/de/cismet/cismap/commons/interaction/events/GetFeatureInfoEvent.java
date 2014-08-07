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
package de.cismet.cismap.commons.interaction.events;

import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

import de.cismet.cismap.commons.features.Feature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GetFeatureInfoEvent {

    //~ Instance fields --------------------------------------------------------

    private Object source;
    private Geometry geom;
    private List<Feature> features;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GetFeatureInfoEvent object.
     */
    public GetFeatureInfoEvent() {
    }

    /**
     * Creates a new GetFeatureInfoEvent object.
     *
     * @param  source  DOCUMENT ME!
     * @param  geom    DOCUMENT ME!
     */
    public GetFeatureInfoEvent(final Object source, final Geometry geom) {
        this.source = source;
        this.geom = geom;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the geom
     */
    public Geometry getGeom() {
        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  the geom to set
     */
    public void setGeom(final Geometry geom) {
        this.geom = geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the source
     */
    public Object getSource() {
        return source;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  source  the source to set
     */
    public void setSource(final Object source) {
        this.source = source;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the features
     */
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  features  the features to set
     */
    public void setFeatures(final List<Feature> features) {
        this.features = features;
    }
}
