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
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.JTSAdapter;

/**
 * Features read from a SHP File. 
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ShapeFeature extends DefaultFeatureServiceFeature {

    //~ Instance fields --------------------------------------------------------
    private final ShapeInfo shapeInfo;


    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFeature object.
     *
     * @param  typename  DOCUMENT ME!
     */

    public ShapeFeature(final ShapeInfo shapeInfo) {
        this.shapeInfo = shapeInfo;
    }

//    /**
//     * Creates a new ShapeFeature object.
//     *
//     * @param  typename  DOCUMENT ME!
//     * @param  styles    DOCUMENT ME!
//     */
//    public ShapeFeature(final ShapeInfo shapeInfo, final List<org.deegree.style.se.unevaluated.Style> styles) {
//        setSLDStyles(styles); // super.style = styles;
//        this.shapeInfo = shapeInfo;
//    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public HashMap getProperties() {
        LinkedHashMap<String, Object> container = new LinkedHashMap<String, Object>();
            
        try {
            org.deegree.model.feature.Feature degreeFeature = shapeInfo.getFile().getFeatureByRecNo(getId());
            final FeatureProperty[] featureProperties = degreeFeature.getProperties();

            for (final FeatureProperty fp : featureProperties) {
                container.put(fp.getName().getAsString(), fp.getValue());
            }
        } catch (final Exception e) {
            logger.error("Cannot read geometry from shape file.", e);
        }
        
        return container;
    }

    @Override
    public Object getProperty(String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public void setProperties(HashMap properties) {
        //nothing to do
    }

    @Override
    public void addProperty(String propertyName, Object property) {
        //nothing to do
    }

    public void addProperties(Map<String, Object> map) {
        //nothing to do
    }
    
    @Override
    public Geometry getGeometry() {
        Geometry g = null;
        
        try {
            g = JTSAdapter.export(shapeInfo.getFile().getGeometryByRecNo(getId()));
            g.setSRID(shapeInfo.getSrid());
        } catch (final Exception e) {
            logger.error("Cannot read geometry from shape file.", e);
        }
        
        return g;
    }

    @Override
    public void setGeometry(Geometry geom) {
        //do nothing
    }
}
