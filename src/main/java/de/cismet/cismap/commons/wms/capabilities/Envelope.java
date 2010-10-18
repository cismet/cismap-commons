package de.cismet.cismap.commons.wms.capabilities;

import de.cismet.cismap.commons.exceptions.ConvertException;

/**
 *
 * @author therter
 */
public interface Envelope {
    public Position getMax();
    public Position getMin();
    public double getWidth();
    public double getHeight();
    public CoordinateSystem getCoordinateSystem();
    public Envelope transform(String destCrs, String sourceCrs) throws ConvertException;
}
