package de.cismet.cismap.commons.wms.capabilities;

import de.cismet.cismap.commons.exceptions.ConvertException;

/**
 *
 * @author therter
 */
public interface Unit {
    public double convert(double value, Unit targetUnit) throws ConvertException;
    /**
     * the scale to convert to the base unit. This is metre for length units, seconds for time units
     * and radian for angle units
     */
    public double getScale();
    public double toBaseUnit(double value);
    public boolean isBaseType();
    @Override
    public String toString();
}
