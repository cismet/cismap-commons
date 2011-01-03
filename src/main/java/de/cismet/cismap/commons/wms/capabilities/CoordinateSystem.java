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
public interface CoordinateSystem {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Unit[] getAxisUnits();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIdentifier();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPrefixedName();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getDimension();
}
