/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.printing;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import java.util.Map;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;

/**
 * A deprecated download for JasperReports. The disadvantage of this class is that it needs the already created
 * parameters and datasource for the report. This creation is often a time-consuming task, which has to happen before
 * the download can be added to DownloadManager. During this waiting time the user has no feedback or an extra dialog
 * has to be shown. This behavior should be avoided.
 *
 * @author      jweintraut
 * @version     $Revision$, $Date$
 * @deprecated  Use the class {@link JasperReportDownload} instead.
 */
public class JasperDownload extends AbstractCancellableDownload {

    //~ Instance fields --------------------------------------------------------

    private JasperPrint print;
    private JasperReport report;
    private Map parameters;
    private JRDataSource dataSource;

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
        this.print = print;
        this.directory = directory;
        this.title = title;

        status = State.WAITING;

        determineDestinationFile(filename, ".pdf");
    }

    /**
     * Creates a new JasperDownload object.
     *
     * @param  report      DOCUMENT ME!
     * @param  parameters  DOCUMENT ME!
     * @param  dataSource  DOCUMENT ME!
     * @param  directory   DOCUMENT ME!
     * @param  title       DOCUMENT ME!
     * @param  filename    DOCUMENT ME!
     */
    public JasperDownload(final JasperReport report,
            final Map parameters,
            final JRDataSource dataSource,
            final String directory,
            final String title,
            final String filename) {
        this.report = report;
        this.parameters = parameters;
        this.dataSource = dataSource;
        this.directory = directory;
        this.title = title;

        status = State.WAITING;

        determineDestinationFile(filename, ".pdf");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            return;
        }

        status = State.RUNNING;

        stateChanged();

        if (print == null) {
            try {
                print = JasperFillManager.fillReport(report, parameters, dataSource);
            } catch (JRException ex) {
                error(ex);
            }
        }

        if (print != null) {
            try {
                if (!Thread.interrupted()) {
                    JasperExportManager.exportReportToPdfFile(print, fileToSaveTo.getPath());
                } else {
                    log.info("Download was interuppted");
                    deleteFile();
                    return;
                }
            } catch (JRException ex) {
                error(ex);
            }
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void deleteFile() {
        if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
            fileToSaveTo.delete();
        }
    }
}
