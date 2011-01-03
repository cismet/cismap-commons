/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.retrieval;

/**
 * Base Interface of all Retrieval Services.
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface RetrievalService {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  rl  DOCUMENT ME!
     */
    void addRetrievalListener(RetrievalListener rl);
    /**
     * DOCUMENT ME!
     *
     * @param  irl  DOCUMENT ME!
     */
    void removeRetrievalListener(RetrievalListener irl);
    /**
     * DOCUMENT ME!
     *
     * @param  forced  DOCUMENT ME!
     */
    void retrieve(boolean forced);
    /**
     * DOCUMENT ME!
     *
     * @param  refreshNeeded  DOCUMENT ME!
     */
    void setRefreshNeeded(boolean refreshNeeded);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isRefreshNeeded();
    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void addPropertyChangeListener(java.beans.PropertyChangeListener l);
    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getProgress();
    /**
     * DOCUMENT ME!
     *
     * @param  progress  DOCUMENT ME!
     */
    void setProgress(int progress);
}
