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

import com.vividsolutions.jts.geom.Geometry;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.io.File;

import java.util.Collection;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PersistentFeature;

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
        init(filename, extension, features, null, null, null);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != Download.State.WAITING) {
            return;
        }

        try {
            status = Download.State.RUNNING;
            stateChanged();

            try {
                loadFeaturesIfRequired();
            } catch (Exception e) {
                log.error("Error while retrieving features", e);
                error(e);
                return;
            }

            if ((features != null) && (features.length > 0)) {
                try {
                    final Collection<? extends ShapeWriter> writer = Lookup.getDefault().lookupAll(ShapeWriter.class);

                    if (writer.size() > 0) {
                        if (getDefaultExtension().equalsIgnoreCase(".dbf")) {
                            writer.iterator().next().writeDbf(features, aliasAttributeList, fileToSaveTo);
                        } else {
                            writer.iterator().next().writeShape(features, aliasAttributeList, fileToSaveTo);
                        }
                    }
                } catch (Exception ex) {
                    error(ex);
                }

                if (features[0] instanceof PersistentFeature) {
                    ((PersistentFeature)features[0]).getPersistenceManager().close();
                }
            } else {
                try {
                    final FeatureServiceFeature feature = service.getFeatureFactory().createNewFeature();
                    final String geometryType = service.getGeometryType();

                    if (!getDefaultExtension().equalsIgnoreCase(".dbf")) {
                        final Geometry g = GeometryUtils.createDummyGeometry(geometryType);
                        feature.setGeometry(g);
                    }

                    features = new FeatureServiceFeature[] { feature };
                    String filenameStem = fileToSaveTo.getAbsolutePath();

                    if (filenameStem.contains(".")) {
                        filenameStem = filenameStem.substring(0, filenameStem.indexOf("."));
                    }

                    final Collection<? extends ShapeWriter> writer = Lookup.getDefault().lookupAll(ShapeWriter.class);

                    if (writer.size() > 0) {
                        if (getDefaultExtension().equalsIgnoreCase(".dbf")) {
                            writer.iterator().next().writeDbf(features, aliasAttributeList, fileToSaveTo);
                        } else {
                            writer.iterator().next().writeShape(features, aliasAttributeList, fileToSaveTo);
                        }
                    }

                    final int shpGeoType = GeometryUtils.getShpGeometryType(geometryType);

                    try {
                        GeometryUtils.clearShpOrShxFile(filenameStem + ".shp", shpGeoType);
                        GeometryUtils.clearShpOrShxFile(filenameStem + ".shx", shpGeoType);
                        GeometryUtils.clearDbfFile(filenameStem + ".dbf");
                    } catch (Exception e) {
                        log.error("Cannot remove content from shape. So remove it completely", e);
                        final File shapeFile = new File(filenameStem + ".shp");
                        final File shxFile = new File(filenameStem + ".shx");
                        final File dbfFile = new File(filenameStem + ".dbf");

                        if (shapeFile.exists()) {
                            shapeFile.delete();
                        }
                        if (shxFile.exists()) {
                            shxFile.delete();
                        }
                        if (dbfFile.exists()) {
                            dbfFile.delete();
                        }
                    }

                    if (getDefaultExtension().equalsIgnoreCase(".dbf")) {
                        String shpFileName = filenameStem + ".shp";

                        deleteFileIfExists(shpFileName);
                        shpFileName = filenameStem + ".shx";
                        deleteFileIfExists(shpFileName);
                    }
                } catch (Exception e) {
                    error(e);
                }
            }

            if (status == Download.State.RUNNING) {
                status = Download.State.COMPLETED;
                stateChanged();
            }
        } finally {
            // without the following lines, a lot of memory will be used as long as
            // this download is in the download list
            features = null;
            service = null;
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
