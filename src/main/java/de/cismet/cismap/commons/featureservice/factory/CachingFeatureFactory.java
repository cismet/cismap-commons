/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.*;

/**
 * The CachingFeatureFactory stores retrieved features in a spatial index
 * structure and thus allows fast bounding box requests. This is especially useful
 * for StaticFeature Services which may require a FeatureFactory to pre-allocate
 * all available Features (e.g. from a SHAPE or GML File). In contrast to a
 * ordinary FeatureFactory which caches only the features of the last
 * {@code createFeatures} request the CachingFeatureFactory may cache features
 * upon initialisation and/or upon each request to the {@code createFeatures}
 * operation. If the factory is able to cache <b>all</b> features upon
 * initialisation (e.g. when loading small GML or SHP files), the {@code createFeatures}
 * request is always executed on the cache.
 *
 * @author Pascal Dihé
 */
public interface CachingFeatureFactory<FT extends FeatureServiceFeature, QT> extends FeatureFactory<FT, QT>
{

  /**
   * Denotes if the features are allocated and cached during initialisation or
   * during the <b>first</b> invocation of the {@code createFeatures()} operation.
   * If this operation returns {@code false} it is stronly advised to instantiate
   * this factory in a separate thread.
   *
   * @return {@code true} if the cached features a allocated upon the <b>first</b> request to {@code createFeatures()}
   */
  public boolean isLazy();

  /**
   * Flushes the internal cache and causes a re-allocate of the features 
   * upon the <b>next</b> {@code createFeatures()} request.
   */
  public void flush();

  /**
   * Returns the maximum number of features that can be <b>allocated and
   * cached</b> by this feature factory.
   *
   * @return maximum number of features that can be cached
   */
  public int getMaxCachedFeatureCount();

  /**
   * Sets the maximum number of features that can be <b>allocated and cached</b>
   * by this feature factory. In general, this value must be higher than
   * {@code maxFeatureCount} and is applied to all available features regardless
   * if they may fall into the specified BoundingBox or not.
   *
   * If during allocation the maximum number is reached the factory throws a
   * TooManyFeaturesException.
   *
   * @param maxCachedFeatureCount maximum number of features that can be cached
   * @see FeatureFactory#setMaxFeatureCount
   */
  public void setMaxCachedFeatureCount(int maxCachedFeatureCount);
}
