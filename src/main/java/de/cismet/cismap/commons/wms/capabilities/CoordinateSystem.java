package de.cismet.cismap.commons.wms.capabilities;

/**
 *
 * @author therter
 */
public interface CoordinateSystem {
    public Unit[] getAxisUnits();
    public String getIdentifier();
    public String getPrefixedName();
    public int getDimension();
}