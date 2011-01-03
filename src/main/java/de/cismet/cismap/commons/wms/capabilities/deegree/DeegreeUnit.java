/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.exceptions.ConvertException;
import de.cismet.cismap.commons.wms.capabilities.Unit;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeUnit implements Unit {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.crs.components.Unit un;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeUnit object.
     *
     * @param  un  DOCUMENT ME!
     */
    public DeegreeUnit(final org.deegree.crs.components.Unit un) {
        this.un = un;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public double convert(final double value, final Unit targetUnit) throws ConvertException {
        if (targetUnit instanceof DeegreeUnit) {
            final org.deegree.crs.components.Unit dUnit = ((DeegreeUnit)targetUnit).toDeegreeUnit();
            if (un.canConvert(dUnit)) {
                return un.convert(value, dUnit);
            } else {
                throw new ConvertException("cannot convert from " + this.toString() + " to " + targetUnit);
            }
        } else {
            throw new ConvertException(
                "can only convert objects of the type DeegreeUnit. The given object has the type "
                        + targetUnit.getClass().getName());
        }
    }

    @Override
    public double getScale() {
        return un.getScale();
    }

    @Override
    public double toBaseUnit(final double value) {
        return un.toBaseUnits(value);
    }

    @Override
    public boolean isBaseType() {
        return un.isBaseType();
    }

    @Override
    public String toString() {
        return un.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private org.deegree.crs.components.Unit toDeegreeUnit() {
        return un;
    }
}
