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
package de.cismet.cismap.commons.retrieval;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public interface RepaintListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void repaintStart(RepaintEvent e);
    
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void repaintComplete(RepaintEvent e);

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void repaintError(RepaintEvent e);
}
