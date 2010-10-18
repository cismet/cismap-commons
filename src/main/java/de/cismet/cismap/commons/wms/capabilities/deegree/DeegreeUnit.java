package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.exceptions.ConvertException;
import de.cismet.cismap.commons.wms.capabilities.Unit;

/**
 *
 * @author therter
 */
public class DeegreeUnit implements Unit {
    private org.deegree.crs.components.Unit un;

    public DeegreeUnit(org.deegree.crs.components.Unit un) {
        this.un = un;
    }


    @Override
    public double convert(double value, Unit targetUnit) throws ConvertException {
        if (targetUnit instanceof DeegreeUnit) {
            org.deegree.crs.components.Unit dUnit = ((DeegreeUnit)targetUnit).toDeegreeUnit();
            if ( un.canConvert( dUnit ) ) {
                return un.convert(value, dUnit);
            } else {
                throw new ConvertException("cannot convert from " + this.toString() + " to " + targetUnit);
            }
        } else {
            throw new ConvertException("can only convert objects of the type DeegreeUnit. The given object has the type " + targetUnit.getClass().getName());
        }
    }

    @Override
    public double getScale() {
        return un.getScale();
    }

    @Override
    public double toBaseUnit(double value) {
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

    private org.deegree.crs.components.Unit toDeegreeUnit() {
        return un;
    }
}
