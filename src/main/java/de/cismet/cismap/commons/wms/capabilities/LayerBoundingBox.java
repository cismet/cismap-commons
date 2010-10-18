package de.cismet.cismap.commons.wms.capabilities;

/**
 *
 * @author therter
 */
public interface LayerBoundingBox extends Envelope {
    public double getResX();
    public double getResY();
    public String getSRS();
}
