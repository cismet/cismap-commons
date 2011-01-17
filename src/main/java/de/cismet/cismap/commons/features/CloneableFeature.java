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
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface CloneableFeature extends Feature, Cloneable {

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */

    //J-
    Object clone();
    //J+
}
