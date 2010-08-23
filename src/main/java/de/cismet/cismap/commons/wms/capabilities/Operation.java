package de.cismet.cismap.commons.wms.capabilities;

import java.net.URL;

/**
 * The operation interface represents a wms operation.
 * This interface should be used to eliminate the deegree dependency for
 * the capabilities parsing.
 *
 * @author therter
 */
public interface Operation {
    public URL getGet();
    public URL getPost();
}
