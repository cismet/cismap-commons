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

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;

import org.openide.util.NbBundle;

import java.io.File;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.downloadmanager.Download;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportShapeDownload extends ExportDownload {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object. The init method must be invoked before the download can be started, if
     * this constructor is used.
     */
    public ExportShapeDownload() {
    }

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  filename   DOCUMENT ME!
     * @param  extension  DOCUMENT ME!
     * @param  features   DOCUMENT ME!
     */
    public ExportShapeDownload(final String filename,
            final String extension,
            final FeatureServiceFeature[] features) {
        init(filename, extension, features, null, null);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != Download.State.WAITING) {
            return;
        }

        status = Download.State.RUNNING;

        if ((features != null) && (features.length > 0)) {
            stateChanged();
            try {
                final FeatureCollection fc = new SimpleFeatureCollection(
                        getId(),
                        (FeatureServiceFeature[])features,
                        aliasAttributeList);
                final ShapeFile shape = new ShapeFile(
                        fc,
                        fileToSaveTo.getAbsolutePath().substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf(".")));
                final ShapeFileWriter writer = new ShapeFileWriter(shape);
                writer.write();

                if (extension.equalsIgnoreCase(".dbf")) {
                    if (fileToSaveTo.getAbsolutePath().toLowerCase().endsWith(".dbf")) {
                        final String fileNameWithoutExt = fileToSaveTo.getAbsolutePath()
                                    .substring(0, fileToSaveTo.getAbsolutePath().length() - 4);
                        String fileName = fileNameWithoutExt + ".shp";

                        deleteFileIfExists(fileName);
                        fileName = fileNameWithoutExt + ".shx";
                        deleteFileIfExists(fileName);
                    }
                }
            } catch (Exception ex) {
                error(ex);
            }
        } else {
            error(new Exception("No features found"));
        }

        if (status == Download.State.RUNNING) {
            status = Download.State.COMPLETED;
            stateChanged();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileName  DOCUMENT ME!
     */
    private void deleteFileIfExists(final String fileName) {
        final File fileToDelete = new File(fileName);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getId() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    public String getDefaultExtension() {
        return ".shp";
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(ExportShapeDownload.class, "ExportShapeDownload.toString");
    }
}
