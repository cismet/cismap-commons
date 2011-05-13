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
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadListChangedEvent extends EventObject {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
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
     * @param  source    DOCUMENT ME!
     * @param  download  DOCUMENT ME!
     * @param  action    DOCUMENT ME!
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
     * @param  source     DOCUMENT ME!
     * @param  downloads  DOCUMENT ME!
     * @param  action     DOCUMENT ME!
     */
    public DownloadListChangedEvent(final Object source, final Collection<Download> downloads, final Action action) {
        super(source);
        this.downloads = downloads;
        this.action = action;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Download> getDownloads() {
        return downloads;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Action getAction() {
        return action;
    }
}
