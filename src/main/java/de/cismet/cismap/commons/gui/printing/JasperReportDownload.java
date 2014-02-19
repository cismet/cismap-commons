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
import net.sf.jasperreports.engine.util.JRLoader;

import org.openide.util.Cancellable;

import java.util.Map;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * DOCUMENT ME!
 *
 * @author   DOCUMENT ME!
 * @version  $Revision$, $Date$
 */
public class JasperReportDownload extends AbstractDownload implements Cancellable {

    //~ Instance fields --------------------------------------------------------

    private JasperPrint print;
    private String reportResourceName;
    private Map parameters;
    private JasperDownloadParametersGenerator parametersGenerator;
    private JRDataSource dataSource;
    private JasperDownloadDataSourceGenerator dataSourceGenerator;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JasperDownload object.
     *
     * @param  reportResourceName  report DOCUMENT ME!
     * @param  parameters          DOCUMENT ME!
     * @param  dataSource          DOCUMENT ME!
     * @param  directory           DOCUMENT ME!
     * @param  title               DOCUMENT ME!
     * @param  filename            DOCUMENT ME!
     */
    public JasperReportDownload(final String reportResourceName,
            final Map parameters,
            final JRDataSource dataSource,
            final String directory,
            final String title,
            final String filename) {
        this.reportResourceName = reportResourceName;
        this.parameters = parameters;
        this.dataSource = dataSource;
        this.directory = directory;
        this.title = title;

        status = State.WAITING;
        determineDestinationFile(filename, ".pdf");
    }

    /**
     * Creates a new AnotherJasperDownload object.
     *
     * @param  reportResourceName   report DOCUMENT ME!
     * @param  parametersGenerator  DOCUMENT ME!
     * @param  dataSource           DOCUMENT ME!
     * @param  directory            DOCUMENT ME!
     * @param  title                DOCUMENT ME!
     * @param  filename             DOCUMENT ME!
     */
    public JasperReportDownload(final String reportResourceName,
            final JasperDownloadParametersGenerator parametersGenerator,
            final JRDataSource dataSource,
            final String directory,
            final String title,
            final String filename) {
        this.reportResourceName = reportResourceName;
        this.parametersGenerator = parametersGenerator;
        this.dataSource = dataSource;
        this.directory = directory;
        this.title = title;

        status = State.WAITING;
        determineDestinationFile(filename, ".pdf");
    }

    /**
     * Creates a new AnotherJasperDownload object.
     *
     * @param  reportResourceName   report DOCUMENT ME!
     * @param  parameters           DOCUMENT ME!
     * @param  dataSourceGenerator  DOCUMENT ME!
     * @param  directory            DOCUMENT ME!
     * @param  title                DOCUMENT ME!
     * @param  filename             DOCUMENT ME!
     */
    public JasperReportDownload(final String reportResourceName,
            final Map parameters,
            final JasperDownloadDataSourceGenerator dataSourceGenerator,
            final String directory,
            final String title,
            final String filename) {
        this.reportResourceName = reportResourceName;
        this.parameters = parameters;
        this.dataSourceGenerator = dataSourceGenerator;
        this.directory = directory;
        this.title = title;

        status = State.WAITING;
        determineDestinationFile(filename, ".pdf");
    }

    /**
     * Creates a new AnotherJasperDownload object.
     *
     * @param  reportResourceName   report DOCUMENT ME!
     * @param  parametersGenerator  DOCUMENT ME!
     * @param  dataSourceGenerator  DOCUMENT ME!
     * @param  directory            DOCUMENT ME!
     * @param  title                DOCUMENT ME!
     * @param  filename             DOCUMENT ME!
     */
    public JasperReportDownload(final String reportResourceName,
            final JasperDownloadParametersGenerator parametersGenerator,
            final JasperDownloadDataSourceGenerator dataSourceGenerator,
            final String directory,
            final String title,
            final String filename) {
        this.reportResourceName = reportResourceName;
        this.parametersGenerator = parametersGenerator;
        this.dataSourceGenerator = dataSourceGenerator;
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

        if (parameters == null) {
            parameters = parametersGenerator.generateParamters();
        }

        if (dataSource == null) {
            dataSource = dataSourceGenerator.generateDataSource();
        }

        try {
            final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(JasperReportDownload.class
                            .getResourceAsStream(
                                reportResourceName));
            print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        } catch (JRException ex) {
            error(ex);
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

    @Override
    public boolean cancel() {
        boolean cancelled = true;
        if (downloadFuture != null) {
            cancelled = downloadFuture.cancel(true);
        }
        if (cancelled) {
            status = State.ABORTED;
            stateChanged();
        }
        return cancelled;
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
