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

/**
 * DOCUMENT ME!
 *
 * @author   pascal
 * @version  $Revision$, $Date$
 */
public class PostgisFeature extends DefaultFeatureServiceFeature {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FEATURE_TYPE_PROPERTY = "Type";
    public static final String GROUPING_KEY_PROPERTY = "GroupingKey";
    public static final String OBJECT_NAME_PROPERTY = "ObjectName";
    public static final String ID_PROPERTY = "Id";
    public static final String GEO_PROPERTY = "Geom";

    //~ Methods ----------------------------------------------------------------

    /**
     * Get the value of featureType.
     *
     * @return  the value of featureType
     */
    public String getFeatureType() {
        return this.getProperty(FEATURE_TYPE_PROPERTY).toString();
    }

    /**
     * Set the value of featureType.
     *
     * @param  featureType  new value of featureType
     */
    public void setFeatureType(final String featureType) {
        this.addProperty(FEATURE_TYPE_PROPERTY, featureType);
    }

    /**
     * Get the value of objectName.
     *
     * @return  the value of objectName
     */
    public String getObjectName() {
        return this.getProperty(OBJECT_NAME_PROPERTY).toString();
    }

    /**
     * Set the value of objectName.
     *
     * @param  objectName  new value of objectName
     */
    public void setObjectName(final String objectName) {
        this.addProperty(OBJECT_NAME_PROPERTY, objectName);
    }

    /**
     * Get the value of groupingKey.
     *
     * @return  the value of groupingKey
     */
    public String getGroupingKey() {
        return this.getProperty(GROUPING_KEY_PROPERTY).toString();
    }

    /**
     * Set the value of groupingKey.
     *
     * @param  groupingKey  new value of groupingKey
     */
    public void setGroupingKey(final String groupingKey) {
        this.addProperty(GROUPING_KEY_PROPERTY, groupingKey);
    }

    @Override
    public void setId(final int id) {
        super.setId(id);
        this.addProperty(ID_PROPERTY, id);
    }

    @Override
    public String getName() {
        return getObjectName();
    }
}
