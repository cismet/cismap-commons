/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.shapeexport;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.event.EventListenerList;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadManager implements Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DownloadManager.class);
    private static DownloadManager instance = null;

    //~ Instance fields --------------------------------------------------------

    private Map<Download, ExportWFS> downloads = new HashMap<Download, ExportWFS>();
    private int countOfCurrentDownloads = 0;
    private EventListenerList listeners = new EventListenerList();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadManager object.
     */
    private DownloadManager() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DownloadManager instance() {
        if (instance == null) {
            instance = new DownloadManager();
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wfss  DOCUMENT ME!
     */
    public synchronized void add(final Collection<ExportWFS> wfss) {
        final Collection<Download> downloadsAdded = new LinkedHashSet<Download>();

        for (final ExportWFS wfs : wfss) {
            final File destinationFile = determineDestinationFile(wfs.getUrl(),
                    ShapeExport.getDestinationDirectory(),
                    ShapeExport.getDestinationFileExtension());
            if (destinationFile == null) {
                return;
            }

            final Download download = new Download(wfs.getUrl(), wfs.getQuery(), wfs.getTopic(), destinationFile);
            download.addObserver(this);
            downloads.put(download, wfs);

            countOfCurrentDownloads++;

            download.startDownload();

            downloadsAdded.add(download);
        }

        notifyDownloadListChanged(new DownloadListChangedEvent(
                this,
                downloadsAdded,
                DownloadListChangedEvent.Action.ADDED));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url                   DOCUMENT ME!
     * @param   destinationDirectory  DOCUMENT ME!
     * @param   extension             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected File determineDestinationFile(final URL url,
            final File destinationDirectory,
            final String extension) {
        final String destinationFile = ShapeExport.getDestinationFile();
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
                LOG.error("Could not create a file for the download from '" + url.toExternalForm()
                            + "'. The configured path is '" + destinationDirectory.getAbsolutePath()
                            + File.separatorChar + ShapeExport.getDestinationFile() + "<1.." + 999 + ">." + extension
                            + ".");
                return null;
            }
        }

        return fileToSaveTo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Map<Download, ExportWFS> getDownloads() {
        return downloads;
    }

    @Override
    public synchronized void update(final Observable o, final Object arg) {
        if (!(o instanceof Download)) {
            return;
        }

        final Download download = (Download)o;

        switch (download.getStatus()) {
            case Download.CANCELLED:
            case Download.COMPLETE: {
                downloads.remove(download);
                countOfCurrentDownloads--;
                notifyDownloadListChanged(new DownloadListChangedEvent(
                        this,
                        download,
                        DownloadListChangedEvent.Action.REMOVED));
                break;
            }
            case Download.ERROR: {
                notifyDownloadListChanged(new DownloadListChangedEvent(
                        this,
                        download,
                        DownloadListChangedEvent.Action.ERROR));
                break;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addDownloadListChangedListener(final DownloadListChangedListener listener) {
        listeners.add(DownloadListChangedListener.class, listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removeDownloadListChangedListener(final DownloadListChangedListener listener) {
        listeners.remove(DownloadListChangedListener.class, listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    protected synchronized void notifyDownloadListChanged(final DownloadListChangedEvent event) {
        for (final DownloadListChangedListener listener : listeners.getListeners(DownloadListChangedListener.class)) {
            listener.downloadListChanged(event);
        }
    }
}
