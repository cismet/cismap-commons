/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.featureservice.style.Style;

/**
 * Combines a set of commen properties of a group of features which belong to the
 * same layer, for example the styles, id and name attributes. This interface
 * allows the management of such common properties in a central place.
 *
 * @author Pascal Dihé
 */
public interface LayerProperties extends ConvertableToXML, Cloneable
{
  /**
   * The expression is undefined or not supported
   */
  public final static int EXPRESSIONTYPE_UNDEFINED = -1;
    /**
   * The expression refers to a static string
   */
  public final static int EXPRESSIONTYPE_STATIC = 0;
  /**
   * The expression refers to a name of a property of feature
   */
  public final static int EXPRESSIONTYPE_PROPERTYNAME = 1;
  /**
   * The expression is a grooy script that must be evaluated by the groovy shell
   */
  public final static int EXPRESSIONTYPE_GROOVY = 2;
  /**
   * The expression is a beansehell script that must be evaluated by BeanShell
   */
  public final static int EXPRESSIONTYPE_BEANSHELL = 3;
  /**
   * The query type is not defined, the layee dooes not support queries
   */
  public final static int QUERYTYPE_UNDEFINED = -1;
  /**
   * HTTP_GET style parameters query, e.g. WMS query
   */
  public final static int QUERYTYPE_HTTPGET = 1;
  /**
   * XML style query, eg.g WFS query
   */
  public final static int QUERYTYPE_XML = 2;

  /**
   * SQL style query
   */
  public final static int QUERYTYPE_SQL = 3;

  public final static String LAYER_PROPERTIES_ELEMENT = "LayerProperties";

  /**
   * Sets the style of this layer. The Style shall by applied to all Features
   * beloging to this layer.
   *
   *
   * @param featureStyle New style object to be set
   */
  public void setStyle(Style featureStyle);

  /**
   * Returns the style  associated to this object. If the operation
   * returns null either the feature has not been initilaized yet or the
   * feature does not support style objects.
   *
   * @return the style object of this feature or null.
   */
  public Style getStyle();

  /**
   * Returns the expression able to assign a unique id to each feature
   * associated with this layer. To determine how this expression should be
   * processed use the getIdExpressionType() operation.
   *
   * @return the actual id expression, e.g. a groovy script or null
   */
  public String getIdExpression();

  /**
   * Sets the expression able to assign a unique id to each feature
   * associated with this layer. The mandatory type parameter indicates how the
   * expression can be procedd, e.g. by a GrovyShell instance.
   *
   * @param idExpression the new expression to be set
   * @param type the type of the expression, see static EXPRESSIONTYPE_ variables
   */
  public void setIdExpression(String idExpression, int type);

  /**
   * Returns the type of the actual idExpression. Please not that the process of
   * assigning an id to a feature can be significantly accelerated by using
   * expressions of type EXPRESSIONTYPE_PROPERTYNAME.
   *
   * @return Type of the expression or -1, see static EXPRESSIONTYPE_ variables
   */
  public int getIdExpressionType();

  /**
   * Returns the expression able to assign a primary annotation to each feature
   * associated with this layer. To determine how this expression should be
   * processed use the getIdExpressionType()operation.
   *
   * @return the expression, e.g. a property name or null
   */
  public String getPrimaryAnnotationExpression();

  /**
   * Returns the type of the actual primaryAnnotationExpressio. Please not that
   * the process of assigning an id to a feature can be significantly accelerated
   * by using expressions of type EXPRESSIONTYPE_PROPERTYNAME.
   *
   * @return Type of the expression or -1, see static EXPRESSIONTYPE_ variables
   */
  public int getPrimaryAnnotationExpressionType();

  /**
   * Sets the expression able to assign a primary annotation to each feature
   * associated with this layer. The mandatory type parameter indicates how the
   * expression can be procedd, e.g. by a GrovyShell instance.
   *
   * @param primaryAnnotationExpression the new expression to be set
   * @param type the type of the expression, see static EXPRESSIONTYPE_ variables
   */
  public void setPrimaryAnnotationExpression(String primaryAnnotationExpression, int type);

  /**
   * Returns the expression able to assign a secondary annotation to each feature
   * associated with this layer. To determine how this expression should be
   * processed use the getIdExpressionType()operation.
   *
   * @return the expression, e.g. a property name or null
   */
  public String getSecondaryAnnotationExpression();

  /**
   * Returns the type of the actual secondaryAnnotationExpressio. Please not that
   * the process of assigning an id to a feature can be significantly accelerated
   * by using expressions of type EXPRESSIONTYPE_PROPERTYNAME.
   *
   * @return Type of the expression or -1, see static EXPRESSIONTYPE_ variables
   */
  public int getSecondaryAnnotationExpressionType();

  /**
   * Sets the expression able to assign a secondary annotation to each feature
   * associated with this layer. The mandatory type parameter indicates how the
   * expression can be procedd, e.g. by a GrovyShell instance.
   *
   * @param secondaryAnnotationExpression the new expression to be set
   * @param type the type of the expression, see static EXPRESSIONTYPE_ variables
   */
  public void setSecondaryAnnotationExpression(String secondaryAnnotationExpression, int type);

  /**
   * Assigns all properties of the {@code layerProperties} object to the current
   * instance {@code this}. The operation can for example be used to re-assign
   * the modified  properties of a cloned instance of {@code this} to {@code this}.
   * Especially usefull when {@code this} is referenced by many
   * {@code InheritsLayerProperties} features.
   *
   * @param layerProperties propertiesd to be assigned to this instance
   */
  public void assign(LayerProperties layerProperties);

  /**
   * Returns the type of the query used to query the layer for new features. If
   * the layer does not support queries, the operation returns -1.
   *
   * @return the type of the query, see static QUERYTYPE_ variables
   */
  public int getQueryType();

  /**
   * Sets the type of the query used to query the layer for new features.
   *
   * @param QueryType the query type as specified by the QUERYTYPE_ variables
   */
  public void setQueryType(int queryType);

  /**
   * Returns the template to build the query used to query the layer for new
   * features. If the layer does not support queries, the operation returns null.
   *
   * @return the query template or null
   */
  //public String getQueryTemplate();

  /**
   * Sets the template to build the query used to query the layer for new
   * features.
   *
   * @param queryTemplate  the query template
   * @param queryType the type of the query, see static QUERYTYPE_ variables
   */
  //public void setQueryTemplate(String queryTemplate, int queryType);

  /**
   * Determines if the id expression for this particular layer should be evaluated.
   * FeatureFactories that support unique id generation will always set this
   * property to {@code false}
   *
   * @return {@code true} if id expression is supported, {@code false} otherwise
   */public boolean isIdExpressionEnabled();


   /**
    * Enables or disables the evaluation of the is expression
    *
    * @param idExpressionEnabled set to {@code true} if enabled
    */
   public void setIdExpressionEnabled(boolean idExpressionEnabled);

  /**
   * Creates a 1:1 copy of this object.
   *
   * @return Cloned instance of this object
   */
  public LayerProperties clone();
}