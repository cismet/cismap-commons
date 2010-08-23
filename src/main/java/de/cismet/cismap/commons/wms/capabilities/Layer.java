package de.cismet.cismap.commons.wms.capabilities;

/**
 * This interface represents a wms layer.
 * This interface should be used to eliminate the deegree dependency for
 * the capabilities parsing.
 *
 * @author therter
 */
public interface Layer {
    public String getTitle();
    public String getName();
    public String getAbstract();
    public boolean isQueryable();
    public boolean isSrsSupported(String srs);
    public String[] getSrs();
    public double getScaleDenominationMax();
    public double getScaleDenominationMin();
    public Style getStyleResource(String name);
    public Style[] getStyles();
    public Layer[] getChildren();
}
