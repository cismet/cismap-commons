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
 * The Style interface represents style information of wms layer. This interface should be used to eliminate the deegree
 * dependency for the capabilities parsing.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface Style {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getTitle();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getName();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    URL[] getLegendURL();
}
