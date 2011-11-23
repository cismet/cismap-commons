/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import de.cismet.cismap.commons.features.Feature;

/**
 * Features implementing FeatureGroupMember are grouped according their group id. Note that each group has its own layer
 * in {@link MappingComponent} which is a child of the feature layer. If there is no group layer for the id returned by
 * {@link FeatureGroupMember#getGroupId() }, a new layer is created and added to the feature layer.
 *
 * @author   Benjamin Friedrich (benjamin.friedrich@cismet.de)
 * @version  1.0, 15.11.2011
 */
public interface FeatureGroupMember extends Feature {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the the display name of the group. This name can be used for GUI purposes e.g. for displaying groups in
     * the layer overview.
     *
     * @return  group display name
     */
    String getGroupName();

    /**
     * Returns the group id.
     *
     * @return  group id
     */
    String getGroupId();

    /**
     * Return the actual feature belonging to the FeatureGroupMember implementation. This method is needed as most of
     * the (current) Features do not implement FeatureGroupMember immediately (as this is quite new interface) leading
     * to wrapper implementations. However, there might occur problems with using collections in this context (which can
     * NOT be solved just with impementing {@link Object#hashCode() } and {@link Object#equals(java.lang.Object)} (see
     * Lagis as example)). Therefore, only the underlying Feature is used for such purposes such as working with hash
     * maps (Feature as key).
     *
     * @return  underlying feature
     */
    Feature getFeature();
}
