/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.downloadmanager;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.event.EventListenerList;

import de.cismet.cismap.commons.gui.shapeexport.ExportWFS;
import de.cismet.cismap.commons.gui.shapeexport.ShapeExport;

/**
 * The download manager manages all current downloads. New downloads are added to a collection, completed downloads are
 * removed. Erraneous downloads remain in the collection. The download manager observes all download objects for state
 * changed and informs the download manager panel via the DownloadListChangedListener interface.
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadManager implements Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DownloadManager.class);
    private static DownloadManager instance = null;

    //~ Instance fields --------------------------------------------------------

    private LinkedList<Download> downloads = new LinkedList<Download>();
    private EventListenerList listeners = new EventListenerList();
    private int countDownloadsTotal = 0;
    private int countDownloadsRunning = 0;
    private int countDownloadsErraneous = 0;
    private int countDownloadsCompleted = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadManager object.
     */
    private DownloadManager() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * It's a Singleton. There can only be one download manager.
     *
     * @return  The download manager.
     */
    public static DownloadManager instance() {
        if (instance == null) {
            instance = new DownloadManager();
        }

        return instance;
    }

    /**
     * This method is used to add new downloads to the download list.
     *
     * @param  downloads  wfss A collection of downloads to add.
     */
    public void add(final Collection<Download> downloads) {
        if ((downloads == null) || (downloads.size() <= 0)) {
            return;
        }

        this.downloads.addAll(0, downloads);
        countDownloadsTotal += downloads.size();

        for (final Download download : downloads) {
            download.addObserver(this);
            download.startDownload();
        }

        notifyDownloadListChanged(new DownloadListChangedEvent(
                this,
                downloads,
                DownloadListChangedEvent.Action.ADDED));
        notifyDownloadListChanged(new DownloadListChangedEvent(
                this,
                downloads,
                DownloadListChangedEvent.Action.CHANGED_COUNTERS));
    }

    /**
     * Removes oboslete downloads. Only running downloads aren't obsolote.
     */
    public synchronized void removeObsoleteDownloads() {
        final Collection<Download> downloadsRemoved = new LinkedList<Download>();

        for (final Download download : downloads) {
            if ((download.getStatus() == Download.COMPLETED) || (download.getStatus() == Download.ERROR)) {
                downloadsRemoved.add(download);
            }
        }

        if (downloadsRemoved.size() > 0) {
            for (final Download download : downloadsRemoved) {
                downloads.remove(download);
                countDownloadsTotal--;

                switch (download.getStatus()) {
                    case Download.ERROR: {
                        countDownloadsErraneous--;
                        break;
                    }
                    case Download.COMPLETED: {
                        countDownloadsCompleted--;
                        break;
                    }
                }
            }

            notifyDownloadListChanged(new DownloadListChangedEvent(
                    this,
                    downloadsRemoved,
                    DownloadListChangedEvent.Action.REMOVED));
            notifyDownloadListChanged(new DownloadListChangedEvent(
                    this,
                    downloadsRemoved,
                    DownloadListChangedEvent.Action.CHANGED_COUNTERS));
        }
    }

    /**
     * A helper method which determines the download location for a download. It uses a counter between 2 and 1000. The
     * value of the counter is added to the file name. The pattern is
     * &lt;USER_HOME&gt;/&lt;DESTINATION_DIRECTORY&gt;/&lt;DESTINATION_FILE&gt;&lt;COUNTER&gt;&lt;EXTENSION&gt;
     *
     * @param   wfs                   url The requested URL. Only necessary for the log message if the destination file
     *                                could'nt be determined.
     * @param   destinationDirectory  The destination directory.
     * @param   extension             The file extension.
     *
     * @return  A file object pointing to the download location.
     */
    protected File determineDestinationFile(final ExportWFS wfs,
            final File destinationDirectory,
            final String extension) {
        final String destinationFile = wfs.getFile();
        boolean fileFound = false;
        int counter = 2;
        File fileToSaveTo = new File(destinationDirectory, destinationFile + extension);

        while (!fileFound) {
            while (fileToSaveTo.exists() && (counter < 1000)) {
                fileToSaveTo = new File(destinationDirectory, destinationFile + counter + extension);
                counter++;
            }

            try {
                fileToSaveTo.createNewFile();

                if (fileToSaveTo.exists() && fileToSaveTo.isFile() && fileToSaveTo.canWrite()) {
                    fileFound = true;
                }
            } catch (IOException ex) {
                fileToSaveTo.deleteOnExit();
            }

            if ((counter >= 1000) && !fileFound) {
                LOG.error("Could not create a file for the download from '" + wfs.getUrl().toExternalForm()
                            + "'. The configured path is '" + destinationDirectory.getAbsolutePath()
                            + File.separatorChar + ShapeExport.getDestinationFile() + "<1.." + 999 + ">." + extension
                            + ".");
                return null;
            }
        }

        return fileToSaveTo;
    }

    /**
     * Returns the current download list.
     *
     * @return  The current download list.
     */
    public Collection<Download> getDownloads() {
        return downloads;
    }

    /**
     * Returns the count of erraneous downloads.
     *
     * @return  The count of erraneous downloads.
     */
    public int getCountDownloadsErraneous() {
        return countDownloadsErraneous;
    }

    /**
     * Returns the count of running downloads.
     *
     * @return  The count of running downloads.
     */
    public int getCountDownloadsRunning() {
        return countDownloadsRunning;
    }

    /**
     * Returns the count of completed downloads.
     *
     * @return  The count of completed downloads.
     */
    public int getCountDownloadsCompleted() {
        return countDownloadsCompleted;
    }

    /**
     * Returns the total count of downloads.
     *
     * @return  The total count of downloads.
     */
    public int getCountDownloadsTotal() {
        return countDownloadsTotal;
    }

    @Override
    public synchronized void update(final Observable o, final Object arg) {
        if (!(o instanceof Download)) {
            return;
        }

        final Download download = (Download)o;

        switch (download.getStatus()) {
            case Download.COMPLETED: {
                countDownloadsRunning--;
                countDownloadsCompleted++;
                break;
            }
            case Download.ERROR: {
                countDownloadsRunning--;
                countDownloadsErraneous++;
                break;
            }
            case Download.RUNNING: {
                countDownloadsRunning++;
                break;
            }
        }

        notifyDownloadListChanged(new DownloadListChangedEvent(
                this,
                download,
                DownloadListChangedEvent.Action.CHANGED_COUNTERS));
    }

    /**
     * Adds a new DownloadListChangedListener.
     *
     * @param  listener  The listener to add.
     */
    public void addDownloadListChangedListener(final DownloadListChangedListener listener) {
        listeners.add(DownloadListChangedListener.class, listener);
    }

    /**
     * Removes a DownloadListChangedListener.
     *
     * @param  listener  The listener to remove.
     */
    public void removeDownloadListChangedListener(final DownloadListChangedListener listener) {
        listeners.remove(DownloadListChangedListener.class, listener);
    }

    /**
     * Notifies all current DownloadListChangedListeners.
     *
     * @param  event  The event to notify about.
     */
    protected synchronized void notifyDownloadListChanged(final DownloadListChangedEvent event) {
        for (final DownloadListChangedListener listener : listeners.getListeners(DownloadListChangedListener.class)) {
            listener.downloadListChanged(event);
        }
    }
}
