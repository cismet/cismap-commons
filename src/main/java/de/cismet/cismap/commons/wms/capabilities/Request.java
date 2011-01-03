/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities;

/**
 * The request interface represents the request element of WMS GetCapabilities response documents. This interface should
 * be used to eliminate the deegree dependency for the capabilities parsing.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface Request {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Operation getMapOperation();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Operation getFeatureInfoOperation();
}
