/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.features;

/**
 *
 * @author thorsten
 */
public interface FeatureWithId {
    public int getId();
    public void setId(int id);
    public String getIdExpression();
    public void setIdExpression(String idExpression);
}
