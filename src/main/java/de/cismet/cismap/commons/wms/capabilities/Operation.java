/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities;

import java.net.URL;

/**
 * The operation interface represents a wms operation. This interface should be used to eliminate the deegree dependency
 * for the capabilities parsing.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface Operation {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    URL getGet();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    URL getPost();
}
