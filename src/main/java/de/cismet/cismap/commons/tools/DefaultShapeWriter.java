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
package de.cismet.cismap.commons.tools;

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;

import java.io.File;

import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    position = 10,
    service = ShapeWriter.class
)
public class DefaultShapeWriter implements ShapeWriter {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void writeShape(final FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList,
            final File fileToSaveTo) throws Exception {
        final FeatureCollection fc = new SimpleFeatureCollection(
                getId(),
                features,
                aliasAttributeList);
        final ShapeFile shape = new ShapeFile(
                fc,
                fileToSaveTo.getAbsolutePath().substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf(".")));
        final ShapeFileWriter writer = new ShapeFileWriter(shape);
        writer.write();
    }

    @Override
    public void writeDbf(final FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList,
            final File fileToSaveTo) throws Exception {
        writeShape(features, aliasAttributeList, fileToSaveTo);

        if (fileToSaveTo.getAbsolutePath().toLowerCase().endsWith(".dbf")) {
            final String fileNameWithoutExt = fileToSaveTo.getAbsolutePath()
                        .substring(0, fileToSaveTo.getAbsolutePath().length() - 4);
            String fileName = fileNameWithoutExt + ".shp";

            deleteFileIfExists(fileName);
            fileName = fileNameWithoutExt + ".shx";
            deleteFileIfExists(fileName);
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
}
