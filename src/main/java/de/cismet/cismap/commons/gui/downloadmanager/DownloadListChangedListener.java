/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.downloadmanager;

import java.util.EventListener;

/**
 * The interface for DownloadListChangedListeners.
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public interface DownloadListChangedListener extends EventListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * The method to be called by download manager when the download list has changed.
     *
     * @param  event  A DownloadListChangedEvent containing the download that has changed.
     */
    void downloadListChanged(DownloadListChangedEvent event);
}
