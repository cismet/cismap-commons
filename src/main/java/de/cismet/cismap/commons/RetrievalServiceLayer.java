/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import edu.umd.cs.piccolo.PNode;

import de.cismet.cismap.commons.retrieval.RetrievalService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface RetrievalServiceLayer extends ServiceLayer, RetrievalService {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  error  DOCUMENT ME!
     */
    void setErrorObject(Object error);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getErrorObject();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean hasErrors();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PNode getPNode();
    /**
     * DOCUMENT ME!
     *
     * @param  pNode  DOCUMENT ME!
     */
    void setPNode(PNode pNode);
}
