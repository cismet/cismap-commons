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
 *
 * @author hell
 */
public interface LayerableFeatureCollection extends FeatureCollection{
    public void createLayers(String... layers);
    public void addLayer(String layer);
    public void removeLayer(String layer);
    public void assignFeatureToLayer(Feature f, String layer);
   public void addFeature(Feature f,String layer);
    public void addFeatures(Collection<Feature> cf,String layer);
    public Vector<String> getAllLayers();
}
