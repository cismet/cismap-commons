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
package de.cismet.cismap.commons.rasterservice;

import com.vividsolutions.jts.geom.Envelope;

import org.apache.batik.ext.awt.image.codec.tiff.TIFFImage;
import org.apache.log4j.Logger;

import org.deegree.io.geotiff.GeoTiffReader;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ImageFileUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ImageFileUtils.class);

    private static final Map<String, String> WORLD_FILE_ENDINGS = new HashMap<String, String>();

    static {
        WORLD_FILE_ENDINGS.put("jpg", "jgw");
        WORLD_FILE_ENDINGS.put("jpeg", "jgw");
        WORLD_FILE_ENDINGS.put("png", "pgw");
        WORLD_FILE_ENDINGS.put("gif", "gfw");
        WORLD_FILE_ENDINGS.put("tif", "tfw");
        WORLD_FILE_ENDINGS.put("tiff", "tfw");
    }

    public static final String[] SUPPORTED_IMAGE_FORMATS = { "png", "jpg", "jpeg", "tif", "tiff", "gif" };

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Mode {

        //~ Enum constants -----------------------------------------------------

        WORLDFILE, TIFF, GEO_REFERENCED
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static File getWorldFile(final File imageFile) {
        final String name = imageFile.getAbsolutePath();
        final String ending = name.substring(name.lastIndexOf(".") + 1).toLowerCase();

        final String wfEnding = WORLD_FILE_ENDINGS.get(ending);

        if (wfEnding != null) {
            final String worldFileName = name.substring(0, name.lastIndexOf(".") + 1) + wfEnding;
            final File worldFile = new File(worldFileName);

            if (worldFile.exists()) {
                return worldFile;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static ImageFileMetaData getTiffMetaData(final File imageFile) throws Exception {
        GeoTiffReader r = new GeoTiffReader(imageFile);
        final org.deegree.model.spatialschema.Envelope e = r.getBoundingBox();
        final Envelope en = new Envelope(e.getMin().getX(), e.getMax().getX(), e.getMin().getY(), e.getMax().getY());
        final TIFFImage tiffImage = r.getTIFFImage();
        final Rectangle rec = tiffImage.getBounds();
        r = null;
        System.gc();

        return new ImageFileMetaData(rec, en, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fileName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isImageFileEnding(final String fileName) {
        for (final String ending : SUPPORTED_IMAGE_FORMATS) {
            if (fileName.endsWith(ending)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Mode determineMode(final File imageFile) {
        final Mode mode;
        final File worldFile = ImageFileUtils.getWorldFile(imageFile);
        if (worldFile != null) {
            mode = Mode.WORLDFILE;
        } else {
            ImageFileMetaData md = null;
            if (imageFile.getName().toLowerCase().endsWith("tif")) {
                try {
                    md = ImageFileUtils.getTiffMetaData(imageFile);
                } catch (final Exception ex) {
                    md = null;
                }
            }
            if (md != null) {
                mode = Mode.TIFF;
            } else {
                mode = Mode.GEO_REFERENCED;
            }
        }
        return mode;
    }

    /**
     * Determines the width and height of the given iamge file. This method does not completely read the image.
     *
     * @param   imgFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static Dimension getImageDimension(final File imgFile) throws IOException {
        final int pos = imgFile.getName().lastIndexOf(".");

        if (pos == -1) {
            throw new IOException("The file " + imgFile.getAbsolutePath()
                        + " has not extension, so no reader can be found.");
        }

        final String fileSuffix = imgFile.getName().substring(pos + 1);
        final Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(fileSuffix);

        if (iter.hasNext()) {
            final ImageReader reader = iter.next();

            try {
                final ImageInputStream stream = new FileImageInputStream(imgFile);
                reader.setInput(stream);
                final int width = reader.getWidth(reader.getMinIndex());
                final int height = reader.getHeight(reader.getMinIndex());

                return new Dimension(width, height);
            } catch (IOException e) {
                LOG.warn("Error reading: " + imgFile.getAbsolutePath(), e);
            } finally {
                reader.dispose();
            }
        }

        throw new IOException("No suitable reader found for file format: " + fileSuffix);
    }
}
