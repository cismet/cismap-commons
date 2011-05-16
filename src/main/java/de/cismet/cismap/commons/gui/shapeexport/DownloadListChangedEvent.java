/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.shapeexport;

import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedHashSet;

/**
 * A wrapper class for the communication between download manager and its DownloadListChangedListeners. An event
 * contains the changed download and the change action (ADDED, REMOVED, ERROR).
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadListChangedEvent extends EventObject {

    //~ Enums ------------------------------------------------------------------

    /**
     * An enumeration representing the action of a download.
     *
     * @version  $Revision$, $Date$
     */
    public enum Action {

        //~ Enum constants -----------------------------------------------------

        ADDED, REMOVED, ERROR
    }

    //~ Instance fields --------------------------------------------------------

    private Collection<Download> downloads;
    private Action action;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadListChangedEvent object.
     *
     * @param  source    The source object.
     * @param  download  The download.
     * @param  action    The change action.
     */
    public DownloadListChangedEvent(final Object source, final Download download, final Action action) {
        super(source);
        this.downloads = new LinkedHashSet<Download>();
        this.downloads.add(download);
        this.action = action;
    }

    /**
     * Creates a new DownloadListChangedEvent object.
     *
     * @param  source     The source object.
     * @param  downloads  The changed downloads.
     * @param  action     The change aciton.
     */
    public DownloadListChangedEvent(final Object source, final Collection<Download> downloads, final Action action) {
        super(source);
        this.downloads = downloads;
        this.action = action;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the changed downloads.
     *
     * @return  A collection of changed downloads.
     */
    public Collection<Download> getDownloads() {
        return downloads;
    }

    /**
     * Returns the change action.
     *
     * @return  The change action.
     */
    public Action getAction() {
        return action;
    }
}
