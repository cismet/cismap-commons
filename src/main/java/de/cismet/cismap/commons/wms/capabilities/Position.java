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
public interface Position {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getX();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getY();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getZ();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getCoordinateDimension();
}
