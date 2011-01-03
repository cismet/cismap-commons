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

import de.cismet.cismap.commons.featureservice.LayerProperties;

/**
 * Features implementing this interface shall inherit certain properties (e.g. Styles) from a LayerProperties object.
 * All Features belonging to the same layer shall share the same LayerProperties instance.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public interface InheritsLayerProperties {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the shared LayerProperties instance.
     *
     * @return  the LayerProperties instance associated with this feature
     */
    LayerProperties getLayerProperties();

    /**
     * Sets the layer properties of this feature. If this feature belongs to a certain layer this operatain shall be
     * invoked on all features belongiung to the same layer with the same instance of the layerProperties object.
     *
     * @param  layerProperties  the new LayerProperties that sahll be applied to this features
     */
    void setLayerProperties(LayerProperties layerProperties);
}
