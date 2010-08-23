package de.cismet.cismap.commons.wms.capabilities;

/**
 * The request interface represents the request element of WMS GetCapabilities response documents.
 * This interface should be used to eliminate the deegree dependency for
 * the capabilities parsing.
 *
 * @author therter
 */
public interface Request {
    public Operation getMapOperation();
    public Operation getFeatureInfoOperation();
}
