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
package de.cismet.cismap.commons.gui.shapeexport;

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.util.Date;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShapeExportHelper {

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a shape file with the given features and writes it to the given zip stream.
     *
     * @param   featureCollection  the content of the shape file
     * @param   shapeFileName      the name of the shape file without extension
     * @param   tempDirectory      a temporary directory that will be used to create the shape file
     * @param   zipStream          the shape file will be written to this zip stream
     * @param   esriWKT            the content of the prj file. If this parameter is null, no prj file will be created
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void writeShapeFileToZip(final org.deegree.model.feature.FeatureCollection featureCollection,
            final String shapeFileName,
            final File tempDirectory,
            final ZipOutputStream zipStream,
            final String esriWKT) throws Exception {
        File tmpSubDirectory = null;

        try {
            String subDirName = null;
            final String separator = System.getProperty("file.separator");
            final Random rand = new Random(new Date().getTime());

            do {
                subDirName = "tmp" + rand.nextInt(Integer.MAX_VALUE);
                tmpSubDirectory = new File(tempDirectory, subDirName);
            } while (tmpSubDirectory.exists());

            tmpSubDirectory.mkdirs();
            final String shapeFileBase = tmpSubDirectory.getAbsoluteFile()
                        + separator
                        + shapeFileName;

            final ShapeFile shape = new ShapeFile(
                    featureCollection,
                    shapeFileBase);
            final ShapeFileWriter writer = new ShapeFileWriter(shape);
            writer.write();

            if (esriWKT != null) {
                final BufferedWriter bw = new BufferedWriter(new FileWriter(shapeFileBase + ".prj"));
                bw.write(esriWKT);
                bw.close();
            }

            zipDirectory(tmpSubDirectory, zipStream, "");
        } finally {
            if ((tmpSubDirectory != null) && tmpSubDirectory.exists()) {
                deleteDirectory(tmpSubDirectory);
            }
        }
    }

    /**
     * Deletes the given directory and its content.
     *
     * @param  dir  the directory to delete
     */
    private static void deleteDirectory(final File dir) {
        for (final File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }

        dir.delete();
    }

    /**
     * Creates a zip file that contains the content of the given directory.
     *
     * @param   inputDir  the directory that should be zipped
     * @param   out       file the zip file that should be created
     * @param   dirName   the current sub directory within the zip file
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void zipDirectory(final File inputDir, final ZipOutputStream out, final String dirName)
            throws Exception {
        final int BYTES_ARRAY_LENGTH = 256;
        final byte[] tmp = new byte[BYTES_ARRAY_LENGTH];
        int byteCount;

        for (final File f : inputDir.listFiles()) {
            if (f.isDirectory()) {
                zipDirectory(f, out, dirName + "/" + f.getName() + "/");
            } else {
                final ZipEntry entry = new ZipEntry(dirName + f.getName());
                out.putNextEntry(entry);
                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

                try {
                    while ((byteCount = bis.read(tmp, 0, BYTES_ARRAY_LENGTH)) != -1) {
                        out.write(tmp, 0, byteCount);
                    }
                } finally {
                    out.closeEntry();
                    bis.close();
                }
            }
        }
    }
}
