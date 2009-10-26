/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

/**
 *
 * @author thorsten hell
 */
public interface FeatureWithId
{

  public int getId();

  public void setId(int id);

  /**
   *
   * @return
   * @deprecated The id expression should be optained from LayerProperties
   */
  @Deprecated
  public String getIdExpression();

  /**
   *
   * @param idExpression
   * @deprecated The id expression should be set on the LayerProperties
   */
  @Deprecated
  public void setIdExpression(String idExpression);
}
