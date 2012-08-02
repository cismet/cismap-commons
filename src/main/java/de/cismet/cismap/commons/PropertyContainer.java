/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons;

import java.util.HashMap;

/**
 *
 * @author thorsten
 */
public interface PropertyContainer {
    public HashMap getProperties();
    public void setProperties(HashMap properties);
    public void addProperty(String propertyName,Object property);
    public void removeProperty(String propertyName);
    public Object getProperty(String propertyName);
}
