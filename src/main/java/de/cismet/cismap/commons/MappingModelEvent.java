/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MappingModelEvent.java
 *
 * Created on 10. M\u00E4rz 2005, 11:28
 */
package de.cismet.cismap.commons;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class MappingModelEvent extends java.util.EventObject {

    //~ Instance fields --------------------------------------------------------

    private int singleFeatureChangedIndex = -1;
    private Feature feature = null;
    private ServiceLayer layer = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MappingModelEvent.
     *
     * @param  fc  DOCUMENT ME!
     */
    public MappingModelEvent(final FeatureCollection fc) {
        super(fc);
    }

    /**
     * Creates a new MappingModelEvent object.
     *
     * @param  layer  DOCUMENT ME!
     */
    public MappingModelEvent(final ServiceLayer layer) {
        super(layer);
        this.layer = layer;
    }
    /**
     * Creates a new MappingModelEvent object.
     *
     * @param  fc            DOCUMENT ME!
     * @param  featureIndex  DOCUMENT ME!
     */
    public MappingModelEvent(final FeatureCollection fc, final int featureIndex) {
        super(fc);
        setSingleFeatureChangedIndex(featureIndex);
    }
    /**
     * Creates a new MappingModelEvent object.
     *
     * @param  fc       DOCUMENT ME!
     * @param  feature  DOCUMENT ME!
     */
    public MappingModelEvent(final FeatureCollection fc, final Feature feature) {
        super(fc);
        this.setFeature(feature);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getSingleFeatureChangedIndex() {
        return singleFeatureChangedIndex;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  singleFeatureChangedIndex  DOCUMENT ME!
     */
    public void setSingleFeatureChangedIndex(final int singleFeatureChangedIndex) {
        this.singleFeatureChangedIndex = singleFeatureChangedIndex;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void setFeature(final Feature feature) {
        this.feature = feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServiceLayer getServiceLayer() {
        return layer;
    }
}
