/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.features;

import org.apache.log4j.Logger;

/**
 *
 * @author pascal
 */
public class PostgisFeature extends DefaultFeatureServiceFeature{

  public final static String FEATURE_TYPE_PROPERTY = "Type";
  public final static String GROUPING_KEY_PROPERTY = "GroupingKey";
  public final static String OBJECT_NAME_PROPERTY = "ObjectName";
  public final static String ID_PROPERTY = "Id";
  public final static String GEO_PROPERTY = "Geom";


  /**
   * Get the value of featureType
   *
   * @return the value of featureType
   */
  public String getFeatureType()
  {
    return this.getProperty(FEATURE_TYPE_PROPERTY).toString();
  }

  /**
   * Set the value of featureType
   *
   * @param featureType new value of featureType
   */
  public void setFeatureType(String featureType)
  {
    this.addProperty(FEATURE_TYPE_PROPERTY, featureType);
  }


  /**
   * Get the value of objectName
   *
   * @return the value of objectName
   */
  public String getObjectName()
  {
    return this.getProperty(OBJECT_NAME_PROPERTY).toString();
  }

  /**
   * Set the value of objectName
   *
   * @param objectName new value of objectName
   */
  public void setObjectName(String objectName)
  {
    this.addProperty(OBJECT_NAME_PROPERTY, objectName);
  }


  /**
   * Get the value of groupingKey
   *
   * @return the value of groupingKey
   */
  public String getGroupingKey()
  {
    return this.getProperty(GROUPING_KEY_PROPERTY).toString();
  }

  /**
   * Set the value of groupingKey
   *
   * @param groupingKey new value of groupingKey
   */
  public void setGroupingKey(String groupingKey)
  {
    this.addProperty(GROUPING_KEY_PROPERTY, groupingKey);
  }

  @Override
  public void setId(int id)
  {
    super.setId(id);
    this.addProperty(ID_PROPERTY, id);
  }
}
