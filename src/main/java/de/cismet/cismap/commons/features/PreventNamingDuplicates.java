/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface PreventNamingDuplicates {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getOriginalName();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getNumber();

    /**
     * DOCUMENT ME!
     *
     * @param  n  DOCUMENT ME!
     */
    void setNumber(int n);
}
