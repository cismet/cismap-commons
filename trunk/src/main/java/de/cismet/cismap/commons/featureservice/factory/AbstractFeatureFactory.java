/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import groovy.lang.GroovyShell;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

/**
 * Abstract impelementation of a FeatureFactory. Supports re-evaluation of id
 * and annotation expressions.
 *
 * @author Pascal Dihé
 */
public abstract class AbstractFeatureFactory<FT extends FeatureServiceFeature, QT> implements FeatureFactory<FT, QT>
{
  public final static boolean DEBUG = Debug.DEBUG;
  // -1 = not id available
  protected int ID = -1;
  protected Logger logger = Logger.getLogger(this.getClass());
  protected LayerProperties layerProperties;
  protected int maxFeatureCount = -1;
  protected GroovyShell groovyShell = null;
  //protected boolean idExpressionChanged = true;
  //protected boolean primaryAnnotationExpressionChanged = true;
  //protected boolean secondaryAnnotationExpressionChanged = true;
  protected Vector<FT> lastCreatedfeatureVector = new Vector();

  protected AbstractFeatureFactory(){}

  protected AbstractFeatureFactory(AbstractFeatureFactory aff)
  {
    this.ID = aff.ID;
    this.layerProperties = aff.layerProperties.clone();
    this.maxFeatureCount = aff.maxFeatureCount;
    this.lastCreatedfeatureVector = new Vector(aff.lastCreatedfeatureVector.size());
    this.lastCreatedfeatureVector.addAll(lastCreatedfeatureVector);
  }

  @Override
  public void setLayerProperties(LayerProperties layerProperties)
  {
    LayerProperties oldLayerProperties = this.layerProperties;
    this.layerProperties = layerProperties;

    if (this.isGenerateIds() && this.layerProperties.isIdExpressionEnabled())
    {
      logger.warn("factory supports automatic id generation, disabling id expression support in layer properties");
      this.layerProperties.setIdExpressionEnabled(false);
    }

    if (this.lastCreatedfeatureVector.size() > 0)
    {
      long start = System.currentTimeMillis();
      logger.debug(this.lastCreatedfeatureVector.size() + " last created features found, applying updated expressions if applicable");
      // check if at least one expression changed
      if ((oldLayerProperties.getIdExpression() == null || oldLayerProperties.getIdExpression().equals(this.layerProperties.getIdExpression())) &&
              (oldLayerProperties.getPrimaryAnnotationExpression() == null || oldLayerProperties.getPrimaryAnnotationExpression().equals(this.layerProperties.getPrimaryAnnotationExpression())) &&
              (oldLayerProperties.getSecondaryAnnotationExpression() == null || oldLayerProperties.getSecondaryAnnotationExpression().equals(this.layerProperties.getSecondaryAnnotationExpression())))
      {
        logger.debug("expressions did not change, re-elevation not neccessary");
        for (FT feature : this.lastCreatedfeatureVector)
        {
          feature.setLayerProperties(this.layerProperties);
        }
      } else if (this.layerProperties.getIdExpressionType() == LayerProperties.EXPRESSIONTYPE_UNDEFINED &&
              this.layerProperties.getPrimaryAnnotationExpressionType() == LayerProperties.EXPRESSIONTYPE_UNDEFINED &&
              this.layerProperties.getSecondaryAnnotationExpressionType() == LayerProperties.EXPRESSIONTYPE_UNDEFINED)
      {
        logger.debug("re-evaluation not necessary, no supported expressions");
        for (FT feature : this.lastCreatedfeatureVector)
        {
          feature.setLayerProperties(this.layerProperties);
        }
      } else
      {
        this.reEvaluteExpressions(this.lastCreatedfeatureVector, null);
      }
      logger.debug("updating layer properties of " + this.lastCreatedfeatureVector.size() + " features took " + ((System.currentTimeMillis() - start)) + " ms");
    } else
    {
      logger.warn("no last created features that could be refreshed found");
    }
  }

  @Override
  public LayerProperties getLayerProperties()
  {
    return this.layerProperties;
  }

  @Override
  public int getMaxFeatureCount()
  {
    return this.maxFeatureCount;
  }

  @Override
  public void setMaxFeatureCount(int maxFeatureCount)
  {
    this.maxFeatureCount = maxFeatureCount;
  }

  /**
   * Re-evaluates the expressions of all features in the list. This operation may
   * be called, if new layer properties are applied on cached features. 
   * 
   * @param features list of caches features
   */
  protected void reEvaluteExpressions(List<FT> features, SwingWorker workerThread)
  {
    logger.debug("SW[" + workerThread + "]: performing re-evaluation of the expressions of " + features.size() + " selected features");
    long start = System.currentTimeMillis();
    int i = 0;
    for (FT feature : features)
    {
      // check if thread is canceled .........................................
      if (this.checkCancelled(workerThread, " evaluating expression"))
      {
        return;
      }
      // check if thread is canceled .........................................

      feature.setLayerProperties(this.layerProperties);
      this.evaluateExpressions(feature, i);
      i++;
    }
    logger.debug("SW[" + workerThread + "]: re-evaluation of " + features.size() + " features took " + ((System.currentTimeMillis() - start)) + " ms");
  }

  /**
   * Evaluates id an annotation expressions of the current layer properties and
   * applies it to the feature.
   * 
   * @param feature to on that the expressions are applied
   * @param index is used as if the evaluation of the id expression fails
   */
  protected void evaluateExpressions(FT feature, int index)
  {
    Object property = null;
    String id = null;

    // ID Expression ...........................................................
    if (!this.isGenerateIds())
    {
      if (DEBUG)logger.debug("evaluating idExpression '" + this.layerProperties.getIdExpression() + "' of type " + this.layerProperties.getIdExpressionType());
      switch (this.layerProperties.getIdExpressionType())
      {
        case LayerProperties.EXPRESSIONTYPE_PROPERTYNAME:
          if (DEBUG)
          {
            logger.debug("evaluating idExpression: EXPRESSIONTYPE_PROPERTYNAME " + LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
          }
          property = feature.getProperty(this.layerProperties.getIdExpression());
          try
          {
            if (property != null)
            {
              if (DEBUG)
              {
                logger.debug("evaluating idExpression: property '" + property + "'");
              }
              feature.setId(Integer.parseInt(property.toString()));
            } else
            {
              feature.setId(ID);
              if (DEBUG)
              {
                logger.warn("evaluating idExpression: property '" + this.layerProperties.getIdExpression() + "' not found, setting id to " + ID);
              }
            }
          } catch (NumberFormatException nfe)
          {
            feature.setId(ID);
            if (DEBUG)
            {
              logger.warn("evaluating idExpression: property '" + property.toString() + "' could not be converted to int, setting id to " + ID);
            }
          }
          break;

        case LayerProperties.EXPRESSIONTYPE_BEANSHELL:
          if (DEBUG)
          {
            logger.debug("evaluating idExpression: EXPRESSIONTYPE_BEANSHELL " + LayerProperties.EXPRESSIONTYPE_BEANSHELL);
          }
          id = this.evaluateBeanShellExpression(feature, this.layerProperties.getIdExpression());
          try
          {
            if (id != null)
            {
              feature.setId(Integer.parseInt(id.toString()));
            } else
            {
              feature.setId(ID);
            }
          } catch (NumberFormatException nfe)
          {
            feature.setId(ID);
          }
          break;

        case LayerProperties.EXPRESSIONTYPE_GROOVY:
          if (DEBUG)
          {
            logger.debug("evaluating idExpression: EXPRESSIONTYPE_GROOVY " + LayerProperties.EXPRESSIONTYPE_GROOVY);
          }
          id = this.evaluateGroovyExpressions(feature, this.layerProperties.getIdExpression());
          try
          {
            if (id != null)
            {
              feature.setId(Integer.parseInt(id.toString()));
            } else
            {
              feature.setId(ID);
            }
          } catch (NumberFormatException nfe)
          {
            feature.setId(ID);
          }
          break;

        default:
          feature.setId(ID);
          break;
      }
    }

    // PrimaryAnnotationExpression .............................................
    if (DEBUG)
    {
      logger.debug("evaluating PrimaryAnnotationExpression '" + this.layerProperties.getPrimaryAnnotationExpression() + "' of type " + this.layerProperties.getPrimaryAnnotationExpressionType());
    }
    switch (this.layerProperties.getPrimaryAnnotationExpressionType())
    {
      case LayerProperties.EXPRESSIONTYPE_STATIC:
        feature.setPrimaryAnnotation(this.layerProperties.getPrimaryAnnotationExpression());
        break;

      case LayerProperties.EXPRESSIONTYPE_PROPERTYNAME:
        property = feature.getProperty(this.layerProperties.getPrimaryAnnotationExpression());
        if (DEBUG)
        {
          logger.debug("evaluating PrimaryAnnotationExpression: setting PrimaryAnnotationExpression '" + property + "'");
        }
        if (property != null)
        {
          feature.setPrimaryAnnotation(property.toString());
        }
        break;

      case LayerProperties.EXPRESSIONTYPE_BEANSHELL:
        feature.setPrimaryAnnotation(this.evaluateBeanShellExpression(feature, this.layerProperties.getPrimaryAnnotationExpression()));
        break;

      case LayerProperties.EXPRESSIONTYPE_GROOVY:
        feature.setPrimaryAnnotation(this.evaluateGroovyExpressions(feature, this.layerProperties.getPrimaryAnnotationExpression()));
        break;
    }

    // SecondaryAnnotationExpression ...........................................
    if (DEBUG)
    {
      logger.debug("evaluating SecondaryAnnotationExpression '" + this.layerProperties.getSecondaryAnnotationExpression() + "' of type " + this.layerProperties.getSecondaryAnnotationExpressionType());
    }
    switch (this.layerProperties.getSecondaryAnnotationExpressionType())
    {
      case LayerProperties.EXPRESSIONTYPE_STATIC:
        feature.setSecondaryAnnotation(this.layerProperties.getSecondaryAnnotationExpression());
        break;

      case LayerProperties.EXPRESSIONTYPE_PROPERTYNAME:
        property = feature.getProperty(this.layerProperties.getSecondaryAnnotationExpression());
        if (DEBUG)
        {
          logger.debug("evaluating PrimaryAnnotationExpression: setting SecondaryAnnotationExpression '" + property + "'");
        }
        if (property != null)
        {
          feature.setSecondaryAnnotation(property.toString());
        }
        break;

      case LayerProperties.EXPRESSIONTYPE_BEANSHELL:
        feature.setSecondaryAnnotation(this.evaluateBeanShellExpression(feature, this.layerProperties.getSecondaryAnnotationExpression()));
        break;

      case LayerProperties.EXPRESSIONTYPE_GROOVY:
        feature.setSecondaryAnnotation(this.evaluateGroovyExpressions(feature, this.layerProperties.getSecondaryAnnotationExpression()));
        break;
    }
  }

  /**
   * Evaluates a groovy expression.
   *
   * @param feature
   * @param expression
   * @return
   */
  protected String evaluateGroovyExpressions(FT feature, String expression)
  {
    if(this.groovyShell == null)
    {
      if(DEBUG)logger.debug("performing lazy first time initialisation of GroovyShell");
      this.groovyShell = new GroovyShell();
    }

    try
    {
      groovyShell.getContext().getVariables().clear();
      for (Object key : feature.getProperties().keySet())
      {
        Object property = feature.getProperty(key.toString());
        groovyShell.setVariable(key.toString().replaceAll(":", "_"), property);
      }

      expression = expression.replaceAll(":", "_");
      return groovyShell.evaluate(expression).toString();
    } catch (Throwable t)
    {
      logger.error("could not evaluate groovy expression '" + expression + "'", t);
      return null;
    }
  }

  protected String evaluateBeanShellExpression(FT feature, String expression)
  {
    throw new UnsupportedOperationException("BeanShell not supported");
  }

  /**
   * Checks if the worker thread was cancelled and performs cleanup.
   * @param worker
   * @param message
   * @return true if the worker thread was cancelled
   */
  protected boolean checkCancelled(SwingWorker workerThread, String message)
  {
    if (workerThread != null && workerThread.isCancelled())
    {
      logger.warn("FRW[" + workerThread + "]: operation is canceled after " + message);
      this.lastCreatedfeatureVector.clear();
      return true;
    }

    return false;
  }

  @Override
  public synchronized Vector<FT> getLastCreatedFeatures()
  {
    // return copy to prevent concurrent modification exception when thread is canceled
    return new Vector<FT>(this.lastCreatedfeatureVector);
  }

  /**
   * Determines if the service automatically generates unique IDs for all
   * queryable features. Note that if this operation returns {@code true}, the
   * id expression is not evaluated.
   *
   * @return {@code true} if the service generates id}
   */
  protected abstract boolean isGenerateIds();
//  protected void processFeatures() throws Exception
//  {
//    if (AbstractFeatureService.this instanceof StaticFeatureService)
//    {
//      Coordinate[] polyCords = new Coordinate[5];
//      polyCords[0] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY1());
//      polyCords[1] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY2());
//      polyCords[2] = new Coordinate(getBoundingBox().getX2(), getBoundingBox().getY2());
//      polyCords[3] = new Coordinate(getBoundingBox().getX2(), getBoundingBox().getY1());
//      polyCords[4] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY1());
//      Polygon boundingPolygon = (new GeometryFactory()).createPolygon((new GeometryFactory()).createLinearRing(polyCords), null);
//
//      if (!(JTSAdapter.export(current.getDefaultGeometryPropertyValue())).intersects(boundingPolygon))
//      {
//        //if(DEBUG)logger.debug("Feature ist nicht in boundingbox");
//        continue;
//      }
//    }
//
//  }

  protected synchronized void updateLastCreatedFeatures(Collection<FT> features)
  {
    this.lastCreatedfeatureVector.clear();
    this.lastCreatedfeatureVector.ensureCapacity(features.size());
    this.lastCreatedfeatureVector.addAll(features);
    this.lastCreatedfeatureVector.trimToSize();
  }

  @Override
  public abstract AbstractFeatureFactory clone();
}
