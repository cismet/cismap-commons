/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.printing;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class JasperDownload extends AbstractDownload {

    //~ Instance fields --------------------------------------------------------

    private JasperPrint print;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for Download.
     *
     * @param  print      The JasperPrint to export.
     * @param  directory  Specifies in which directory to save the file. This should be specified relative to the
     *                    general download directory.
     * @param  title      The title of the download.
     * @param  filename   A String containing the filename.
     */
    public JasperDownload(final JasperPrint print,
            final String directory,
            final String title,
            final String filename) {
        this.log = Logger.getLogger(JasperDownload.class);
        this.print = print;
        this.directory = directory;
        this.title = title;

        if (DownloadManager.instance().isEnabled()) {
            determineDestinationFile(filename, ".pdf");
            status = State.WAITING;
        } else {
            status = State.COMPLETED_WITH_ERROR;
            caughtException = new Exception("DownloadManager is disabled. Cancelling download.");
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            return;
        }

        status = State.RUNNING;

        stateChanged();

        try {
            JasperExportManager.exportReportToPdfFile(print, fileToSaveTo.getPath());
        } catch (JRException ex) {
            error(ex);
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof JasperDownload)) {
            return false;
        }

        final JasperDownload other = (JasperDownload)obj;

        boolean result = true;

        if ((this.print == null) ? (other.print != null) : (!this.print.equals(other.print))) {
            result &= false;
        }
        if ((this.fileToSaveTo == null) ? (other.fileToSaveTo != null)
                                        : (!this.fileToSaveTo.equals(other.fileToSaveTo))) {
            result &= false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (43 * hash) + ((this.print != null) ? this.print.hashCode() : 0);
        hash = (43 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);

        return hash;
    }
}
