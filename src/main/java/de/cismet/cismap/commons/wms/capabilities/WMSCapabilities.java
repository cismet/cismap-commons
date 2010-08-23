package de.cismet.cismap.commons.wms.capabilities;

import de.cismet.cismap.commons.capabilities.Service;
import java.net.URL;

/**
 * The WMSCapabilities interface represents a WMS GetCaapabilities response document.
 * This interface should be used to eliminate the deegree dependency for
 * the capabilities parsing.
 *
 * @author therter
 */
public interface WMSCapabilities {
    public Service getService();
    public Request getRequest();
    public Layer getLayer();
    public String getVersion();
    public URL getURL();
}
