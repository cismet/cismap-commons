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
package de.cismet.cismap.commons;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface PropertyContainer {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    HashMap getProperties();
    /**
     * DOCUMENT ME!
     *
     * @param  properties  DOCUMENT ME!
     */
    void setProperties(HashMap properties);
    /**
     * DOCUMENT ME!
     *
     * @param  propertyName  DOCUMENT ME!
     * @param  property      DOCUMENT ME!
     */
    void addProperty(String propertyName, Object property);
    /**
     * DOCUMENT ME!
     *
     * @param  propertyName  DOCUMENT ME!
     */
    void removeProperty(String propertyName);
    /**
     * DOCUMENT ME!
     *
     * @param   propertyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getProperty(String propertyName);
}
