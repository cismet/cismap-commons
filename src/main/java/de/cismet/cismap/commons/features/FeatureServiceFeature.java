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

//import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.PropertyContainer;

/**
 * This interface describes a Feature that is served by a FeatureService. FeatureServiceFeature belonging to the same
 * layer shall share on LayerProperties instance.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public interface FeatureServiceFeature extends StyledFeature,
    CloneableFeature,
    AnnotatedFeature,
    PropertyContainer,
    AnnotatedByPropertyFeature,
    FeatureWithId,
    InheritsLayerProperties,
    SLDStyledFeature /*,ConvertableToXML*/ {
}
