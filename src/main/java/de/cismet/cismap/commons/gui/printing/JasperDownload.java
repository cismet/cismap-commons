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
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;

/**
 * A download for JasperReports. The disadvantage of this class is that it needs the already created parameters and
 * datasource for the report. This creation is often a time-consuming task, which has to happen before the download can
 * be added to DownloadManager. During this waiting time the user has no feedback or an extra dialog has to be shown.
 * This behavior is needed for the Printing of maps with it own progress dialog with extra information about the loading
 * of the maps.
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class JasperDownload extends AbstractCancellableDownload {

    //~ Instance fields --------------------------------------------------------

    private ArrayList<JasperPrint> prints = new ArrayList<JasperPrint>(5);
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
        prints.add(print);
        this.directory = directory;
        this.title = title;

        status = State.WAITING;

        determineDestinationFile(filename, ".pdf");
    }

    /**
     * Constructor for Download.
     *
     * @param  prints     The JasperPrint to export.
     * @param  directory  Specifies in which directory to save the file. This should be specified relative to the
     *                    general download directory.
     * @param  title      The title of the download.
     * @param  filename   A String containing the filename.
     */
    public JasperDownload(final Collection<JasperPrint> prints,
            final String directory,
            final String title,
            final String filename) {
        this.prints.addAll(prints);
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

        if (prints.isEmpty() && (report != null)) {
            try {
                prints.add(JasperFillManager.fillReport(report, parameters, dataSource));
            } catch (JRException ex) {
                error(ex);
            }
        }

        if (prints.size() > 0) {
            try {
                if (!Thread.interrupted()) {
                    final JRPdfExporter exporter = new JRPdfExporter();

                    exporter.setExporterInput(SimpleExporterInput.getInstance(prints));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileToSaveTo.getPath()));
                    final SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
                    configuration.setCreatingBatchModeBookmarks(true);
                    exporter.setConfiguration(configuration);
                    exporter.exportReport();
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

    /**
     * DOCUMENT ME!
     *
     * @param  fileToSaveTo  DOCUMENT ME!
     */
    public void setFileToSaveTo(final File fileToSaveTo) {
        this.fileToSaveTo = fileToSaveTo;
    }
}
