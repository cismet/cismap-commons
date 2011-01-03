/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import edu.umd.cs.piccolo.PNode;

import de.cismet.cismap.commons.*;
import de.cismet.cismap.commons.retrieval.RetrievalService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface MapService extends RetrievalService {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  height  DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     */
    void setSize(int height, int width);
    /**
     * DOCUMENT ME!
     *
     * @param  bb  DOCUMENT ME!
     */
    void setBoundingBox(BoundingBox bb);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getTranslucency();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isVisible();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PNode getPNode();
    /**
     * DOCUMENT ME!
     *
     * @param  imageObject  DOCUMENT ME!
     */
    void setPNode(PNode imageObject);
}
