/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * LayerableFeatureCollection.java
 *
 * Created on 30. Oktober 2007, 10:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

import java.util.Collection;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public interface LayerableFeatureCollection extends FeatureCollection {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  layers  DOCUMENT ME!
     */
    void createLayers(String... layers);
    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    void addLayer(String layer);
    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    void removeLayer(String layer);
    /**
     * DOCUMENT ME!
     *
     * @param  f      DOCUMENT ME!
     * @param  layer  DOCUMENT ME!
     */
    void assignFeatureToLayer(Feature f, String layer);
    /**
     * DOCUMENT ME!
     *
     * @param  f      DOCUMENT ME!
     * @param  layer  DOCUMENT ME!
     */
    void addFeature(Feature f, String layer);
    /**
     * DOCUMENT ME!
     *
     * @param  cf     DOCUMENT ME!
     * @param  layer  DOCUMENT ME!
     */
    void addFeatures(Collection<Feature> cf, String layer);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Vector<String> getAllLayers();
}
