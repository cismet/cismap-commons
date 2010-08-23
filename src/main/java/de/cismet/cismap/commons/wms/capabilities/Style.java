package de.cismet.cismap.commons.wms.capabilities;

import java.net.URL;

/**
 * The Style interface represents style information of wms layer.
 * This interface should be used to eliminate the deegree dependency for
 * the capabilities parsing.
 *
 * @author therter
 */
public interface Style {
    public String getTitle();
    public String getName();
    public URL[] getLegendURL();
}
