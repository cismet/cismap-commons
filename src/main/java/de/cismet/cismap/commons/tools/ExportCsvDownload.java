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
package de.cismet.cismap.commons.tools;

import org.openide.util.NbBundle;

import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportCsvDownload extends ExportTxtDownload {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object.
     */
    public ExportCsvDownload() {
        super();
    }

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  filename        DOCUMENT ME!
     * @param  extension       DOCUMENT ME!
     * @param  features        DOCUMENT ME!
     * @param  service         DOCUMENT ME!
     * @param  attributeNames  A list with string arrays. Every array should have 2 elements. The first element is the
     *                         alias of the column and the second element is the name of the attribute, that should be
     *                         shown in the column
     */
    public ExportCsvDownload(final String filename,
            final String extension,
            final FeatureServiceFeature[] features,
            final AbstractFeatureService service,
            final List<String[]> attributeNames) {
        super(filename, extension, features, service, attributeNames);
        separator = ",";
        writeHeader = true;
        nullValue = "";
        quotes = "\"";
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void init(final String filename,
            final String extension,
            final FeatureServiceFeature[] features,
            final AbstractFeatureService service,
            final List<String[]> aliasAttributeList,
            final String query) {
        super.init(filename, extension, features, service, aliasAttributeList, null);
        separator = ",";
        writeHeader = true;
        nullValue = "";
        quotes = "\"";
    }

    @Override
    public String getDefaultExtension() {
        return ".csv";
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(ExportShapeDownload.class, "ExportCsvDownload.toString");
    }
}
