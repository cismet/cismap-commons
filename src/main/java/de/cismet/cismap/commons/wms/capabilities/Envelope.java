/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities;

import de.cismet.cismap.commons.exceptions.ConvertException;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface Envelope {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Position getMax();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Position getMin();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getWidth();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getHeight();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CoordinateSystem getCoordinateSystem();
    /**
     * DOCUMENT ME!
     *
     * @param   destCrs    DOCUMENT ME!
     * @param   sourceCrs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConvertException  DOCUMENT ME!
     */
    Envelope transform(String destCrs, String sourceCrs) throws ConvertException;
}
