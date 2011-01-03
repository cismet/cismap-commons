/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface LayerBoundingBox extends Envelope {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getResX();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getResY();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getSRS();
}
