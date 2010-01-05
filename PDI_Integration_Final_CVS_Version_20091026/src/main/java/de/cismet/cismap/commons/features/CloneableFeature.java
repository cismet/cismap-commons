/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.features;

/**
 *
 * @author thorsten
 */
public interface CloneableFeature extends Feature,Cloneable{
    public Object clone();
}
