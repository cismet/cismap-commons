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
public interface Unit {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   value       DOCUMENT ME!
     * @param   targetUnit  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConvertException  DOCUMENT ME!
     */
    double convert(double value, Unit targetUnit) throws ConvertException;
    /**
     * the scale to convert to the base unit. This is metre for length units, seconds for time units and radian for
     * angle units
     *
     * @return  DOCUMENT ME!
     */
    double getScale();
    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double toBaseUnit(double value);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isBaseType();
    @Override
    String toString();
}
