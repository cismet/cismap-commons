/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.printing;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JExcelApiExporter;

/**
 * DOCUMENT ME!
 *
 * @author   daniel
 * @version  $Revision$, $Date$
 */
public class JasperReportExcelDownload extends JasperReportDownload {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JasperReportCsvDownload object.
     *
     * @param  reportResourceName   DOCUMENT ME!
     * @param  dataSourceGenerator  DOCUMENT ME!
     * @param  directory            DOCUMENT ME!
     * @param  title                DOCUMENT ME!
     * @param  filename             DOCUMENT ME!
     */
    public JasperReportExcelDownload(final String reportResourceName,
            final JasperReportDownload.JasperReportDataSourceGenerator dataSourceGenerator,
            final String directory,
            final String title,
            final String filename) {
        super(reportResourceName,
            dataSourceGenerator,
            directory,
            title,
            filename,
            ".xls");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void exportReportFile() throws JRException {
        final JExcelApiExporter exporter = new JExcelApiExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fileToSaveTo.toString());
        exporter.exportReport();
    }
}
