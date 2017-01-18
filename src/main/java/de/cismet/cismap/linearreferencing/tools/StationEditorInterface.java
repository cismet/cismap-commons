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
package de.cismet.cismap.linearreferencing.tools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface StationEditorInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void dispose();
    /**
     * DOCUMENT ME!
     */
    void undoChanges();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getValue();
}
