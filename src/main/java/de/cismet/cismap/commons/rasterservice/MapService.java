/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.*;
import de.cismet.cismap.commons.retrieval.RetrievalService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface MapService extends RetrievalService, PNodeProvider {

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
}
