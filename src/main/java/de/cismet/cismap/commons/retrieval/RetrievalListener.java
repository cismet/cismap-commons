/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.retrieval;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public interface RetrievalListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void retrievalStarted(RetrievalEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void retrievalProgress(RetrievalEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void retrievalComplete(RetrievalEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void retrievalAborted(RetrievalEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void retrievalError(RetrievalEvent e);
}
