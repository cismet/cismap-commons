/*
 * MappingModelEvent.java
 *
 * Created on 10. M\u00E4rz 2005, 11:28
 */

package de.cismet.cismap.commons;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;

/**
 *
 * @author hell
 */
public class MappingModelEvent extends java.util.EventObject{
    private int singleFeatureChangedIndex=-1;
    private Feature feature=null;
    private ServiceLayer layer=null;
    /** Creates a new instance of MappingModelEvent */
    public MappingModelEvent(FeatureCollection fc) {
        super(fc);
    }
    public MappingModelEvent(FeatureCollection fc,int featureIndex ) {
        super(fc);
        setSingleFeatureChangedIndex(featureIndex);
    }
    public MappingModelEvent(FeatureCollection fc,Feature feature) {
        super(fc);
        this.setFeature(feature);
    }

    public MappingModelEvent(ServiceLayer layer){
        super(layer);
        this.layer=layer;
    }
    
    public int getSingleFeatureChangedIndex() {
        return singleFeatureChangedIndex;
    }

    public void setSingleFeatureChangedIndex(int singleFeatureChangedIndex) {
        this.singleFeatureChangedIndex = singleFeatureChangedIndex;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    
    
    public ServiceLayer getServiceLayer() {
        return layer;
    }
    
    
    
}
