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
public class RepaintEvent {

    //~ Instance fields --------------------------------------------------------

    RetrievalEvent retrievalEvent = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RepaintEvent object.
     *
     * @param  retrievalEvent  DOCUMENT ME!
     */
    public RepaintEvent(final RetrievalEvent retrievalEvent) {
        this.retrievalEvent = retrievalEvent;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RetrievalEvent getRetrievalEvent() {
        return retrievalEvent;
    }
}
