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

import java.util.HashMap;
import java.util.Map;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;

/**
 * JasperReportDownload is a Download which can be immediately added to the DownloadManager and allows it to create the
 * needed datasource and parameters afterwards. This is the advantage over {@link JasperDownload}, as the
 * DownloadManager opens immediately. The datasource and parameters are created via the strategy pattern, which is
 * realized with the two interfaces JasperReportParametersGenerator and JasperReportDataSourceGenerator. A concrete
 * class of these interfaces contains the knowledge of creating the datasource or the parameters. These concrete classes
 * are run in the run()-method of JasperReportDownload and therefor create the datasource or parameters after the
 * download itself has been added to the download manager. Another point is that the creation of the JasperPrint is also
 * time-consuming, with this implementation it has to be created in the download, this was not necessary in
 * {@link JasperDownload}.
 *
 * <p>Note 1: the creation of the datasource and the parameters will not run in the EDT.</p>
 *
 * <p>Note 2: the datasource will be created before the parameters</p>
 *
 * @author   DOCUMENT ME!
 * @version  $Revision$, $Date$
 */
public class JasperReportDownload extends AbstractCancellableDownload {

    //~ Instance fields --------------------------------------------------------

    protected JasperPrint print;
    protected String reportResourceName;
    protected Map parameters;
    protected JasperReportParametersGenerator parametersGenerator;
    protected JRDataSource dataSource;
    protected JasperReportDataSourceGenerator dataSourceGenerator;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnotherJasperDownload object. This can be used for Reports without parameters.
     *
     * @param  reportResourceName   report DOCUMENT ME!
     * @param  dataSourceGenerator  DOCUMENT ME!
     * @param  directory            DOCUMENT ME!
     * @param  title                DOCUMENT ME!
     * @param  filename             DOCUMENT ME!
     */
    public JasperReportDownload(final String reportResourceName,
            final JasperReportDataSourceGenerator dataSourceGenerator,
            final String directory,
            final String title,
            final String filename) {
        this(reportResourceName, dataSourceGenerator, directory, title, filename, ".pdf");
    }

    /**
     * Creates a new JasperReportDownload object.
     *
     * @param  reportResourceName   DOCUMENT ME!
     * @param  dataSourceGenerator  DOCUMENT ME!
     * @param  directory            DOCUMENT ME!
     * @param  title                DOCUMENT ME!
     * @param  filename             DOCUMENT ME!
     * @param  extension            DOCUMENT ME!
     */
    public JasperReportDownload(final String reportResourceName,
            final JasperReportDataSourceGenerator dataSourceGenerator,
            final String directory,
            final String title,
            final String filename,
            final String extension) {
        this.reportResourceName = reportResourceName;
        this.parameters = new HashMap();
        this.dataSourceGenerator = dataSourceGenerator;
        this.directory = directory;
        this.title = title;

        status = State.WAITING;
        determineDestinationFile(filename, extension);
    }

    /**
     * Creates a new AnotherJasperDownload object. Sometimes it is easier/better to generate the parameters before the
     * actual download. E.g. fetching user input from the GUI.
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
            final JasperReportDataSourceGenerator dataSourceGenerator,
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
            final JasperReportParametersGenerator parametersGenerator,
            final JasperReportDataSourceGenerator dataSourceGenerator,
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

        // anything could go wrong here
        try {
            // if the dataSource does not exist create it
            if (dataSource == null) {
                dataSource = dataSourceGenerator.generateDataSource();
            }

            // if the paramters does not exist create them
            if (parameters == null) {
                parameters = parametersGenerator.generateParamters();
            }
        } catch (Exception ex) {
            error(ex);
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
                    exportReportFile();
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
     *
     * @throws  JRException  DOCUMENT ME!
     */
    protected void exportReportFile() throws JRException {
        JasperExportManager.exportReportToPdfFile(print, fileToSaveTo.getPath());
    }

    /**
     * DOCUMENT ME!
     */
    private void deleteFile() {
        if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
            fileToSaveTo.delete();
        }
    }

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public interface JasperReportParametersGenerator {

        //~ Methods ------------------------------------------------------------

        /**
         * This method should contain the logic on how to create the parameters for a JasperReport. The result of this
         * method can be used to create a JasperPrint in JasperReportDownload.
         *
         * @return  a parameters map for a JasperReport
         */
        Map generateParamters();
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public interface JasperReportDataSourceGenerator {

        //~ Methods ------------------------------------------------------------

        /**
         * This method should contain the logic on how to create the datasource for a JasperReport. The result of this
         * method can be used to create a JasperPrint in JasperReportDownload.
         *
         * @return  a JRDataSource for a JasperReport
         */
        JRDataSource generateDataSource();
    }
}
