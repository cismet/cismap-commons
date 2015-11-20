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
package de.cismet.cismap.commons.features;

/**
 * This interface can be used to specify a scale range, within which the feature should be drawn in the map.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface ScaleAwareFeatureRenderer {

    //~ Methods ----------------------------------------------------------------

    /**
     * The feature should be drawn in the map, when scale is equal or greater than this scale. Notice: the integer value
     * that will be returned by this method should be greater than the integer that will be returned by the method
     * getMaxScale().
     *
     * <p>To see the geoemtry in the map, the following constraint must be fulfilled: (getMaxScale() <= mapScale) &&
     * (getMinScale() >= mapScale)</p>
     *
     * @return  the min scale
     */
    int getMinScale();

    /**
     * The feature should be drawn in the map, when scale is equal or less than this scale. Notice: the integer value
     * that will be returned by this method should be less than the integer that will be returned by the method
     * getMinScale().
     *
     * <p>To see the geoemtry in the map, the following constraint must be fulfilled: (getMaxScale() <= mapScale) &&
     * (getMinScale() >= mapScale)</p>
     *
     * @return  the max scale
     */
    int getMaxScale();
}
