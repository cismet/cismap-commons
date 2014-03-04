/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.printing;

import org.openide.util.Cancellable;

import java.io.File;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * A Download which can immediately be added to the download manager and afterwards executes some task, containing the
 * logic of the download itself. A BackgroundTaskDownload gets his task via an implementation of the interface
 * DownloadTask, see also strategy pattern. This means that different kind of tasks can be given to this download, which
 * are then executed in the run()-Method. Such a task typically contains fetching the data needed for the download and
 * then writing the download to a file. The <code>File</code>, which can be used, is provided by BackgroundTaskDownload.
 *
 * <p>Note 1: the download task will not run in the EDT.</p>
 *
 * <p>Note 2: this download is not at all specialized and more specialized downloads for certain tasks may exists. E.g.
 * {@link JasperReportDownload}</p>
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class BackgroundTaskDownload extends AbstractDownload implements Cancellable {

    //~ Instance fields --------------------------------------------------------

    private DownloadTask downloadTask;
    private SwingWorker<Void, Void> worker;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RunnableDownload object.
     *
     * @param  downloadTask  DOCUMENT ME!
     * @param  title         DOCUMENT ME!
     * @param  directory     DOCUMENT ME!
     * @param  filename      DOCUMENT ME!
     * @param  extension     DOCUMENT ME!
     */
    public BackgroundTaskDownload(final DownloadTask downloadTask,
            final String title,
            final String directory,
            final String filename,
            final String extension) {
        this.title = title;
        this.directory = directory;
        this.downloadTask = downloadTask;

        status = State.WAITING;
        determineDestinationFile(filename, extension);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    downloadTask.download(fileToSaveTo);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (InterruptedException ex) {
                        // do nothing, the download was cancelled
                    } catch (CancellationException ex) {
                        // do nothing, the download was cancelled
                    } catch (ExecutionException ex) {
                        error(ex);
                    } catch (Exception ex) {
                        error(ex);
                    }

                    if (status == State.RUNNING) {
                        status = State.COMPLETED;
                        stateChanged();
                    }
                }
            };

        if (status != State.WAITING) {
            return;
        }

        status = State.RUNNING;
        stateChanged();
        worker.execute();
    }

    @Override
    public boolean cancel() {
        boolean cancelled = true;
        status = null;
        if ((downloadFuture != null) && (worker != null)) {
            cancelled = worker.cancel(true) || downloadFuture.cancel(true);
        }
        if (cancelled) {
            status = State.ABORTED;
            stateChanged();
        }
        return cancelled;
    }

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public interface DownloadTask {

        //~ Methods ------------------------------------------------------------

        /**
         * A task, which is executed during the run()-method of BackgroundTaskDownload. The parameter fileToSaveTo is a
         * File determined by BackgroundTaskDownload, due to the provided filename. Every exception thrown in this
         * method, will later on be caught by the BackgroundTaskDownload and shown in the DownloadManager.
         *
         * <p>Note: Do not forget to close the system resources e.g. FileOutputStream. This can be done with a
         * try-finally block.</p>
         *
         * @param   fileToSaveTo  A File which was determined in BackgroundTaskDownload
         *
         * @throws  Exception  the Exceptions will be caught by BackgroundTaskDownload
         */
        void download(File fileToSaveTo) throws Exception;
    }
}
