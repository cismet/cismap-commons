/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities;

import java.net.URL;

import java.util.List;

import de.cismet.cismap.commons.capabilities.Service;

/**
 * The WMSCapabilities interface represents a WMS GetCaapabilities response document. This interface should be used to
 * eliminate the deegree dependency for the capabilities parsing.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface WMSCapabilities {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Service getService();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Request getRequest();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Layer getLayer();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVersion();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    URL getURL();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<String> getExceptions();
}
