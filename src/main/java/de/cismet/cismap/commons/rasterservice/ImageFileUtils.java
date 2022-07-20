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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import org.apache.batik.ext.awt.image.codec.tiff.TIFFImage;
import org.apache.log4j.Logger;

import org.deegree.io.geotiff.GeoTiffReader;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.georeferencing.PointCoordinatePair;

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
        final File worldFile = getWorldFileWithoutCheck(imageFile);

        if ((worldFile != null) && worldFile.exists()) {
            return worldFile;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static File getWorldFileWithoutCheck(final File imageFile) {
        final String name = imageFile.getAbsolutePath();
        final String ending = name.substring(name.lastIndexOf(".") + 1).toLowerCase();

        final String wfEnding = WORLD_FILE_ENDINGS.get(ending);

        if (wfEnding != null) {
            final String worldFileName = name.substring(0, name.lastIndexOf(".") + 1) + wfEnding;
            final File worldFile = new File(worldFileName);
            return worldFile;
        } else {
            return null;
        }
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

        return new ImageFileMetaData(rec, en, null, null);
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
            if (fileName.toLowerCase().endsWith(ending)) {
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
        ImageFileMetaData md = null;

        final File worldFile = ImageFileUtils.getWorldFile(imageFile);
        if (worldFile != null) {
            md = getWorldFileMetaData(imageFile, worldFile);
            if (md != null) {
                return md.isRasterGeoRef() ? Mode.GEO_REFERENCED : Mode.WORLDFILE;
            }
        }

        if (imageFile.getName().toLowerCase().endsWith("tif")) {
            try {
                return Mode.TIFF;
            } catch (final Exception ex) {
            }
        }
        return (md != null) ? Mode.TIFF : Mode.GEO_REFERENCED;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   worldFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean checkIfRasterGeoRef(final File worldFile) {
        if (worldFile != null) {
            try {
                final BufferedReader br = new BufferedReader(new FileReader(worldFile));
                String line;
                while (((line = br.readLine()) != null)) {
                    if (line.startsWith("#cidsgeoref;")) {
                        return true;
                    }
                }
                br.close();
            } catch (Exception e) {
                LOG.error("Cannot parse the world file", e);
            }
        }
        return false;
    }

    /**
     * Determines the meta information about the image file. It will be assumed, that a world file for the image file
     * exists. The world file must have the file ending .jgw, .pgw, .gfw or .tfw depending on the image format.
     *
     * @param   imageFile  DOCUMENT ME!
     * @param   worldFile  DOCUMENT ME!
     *
     * @return  the meta information about the image file
     */
    public static ImageFileMetaData getWorldFileMetaData(final File imageFile, final File worldFile) {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(worldFile));
            final double[] matrix = new double[6];
            final List<PointCoordinatePair> pairs = new ArrayList<>();
            boolean isRasterGeoReof = false;

            // read parameter from world file
            int index = 0;
            String line;
            while (((line = br.readLine()) != null)) {
                if (line.startsWith("#cidsgeoref;")) {
                    isRasterGeoReof = true;
                    continue;
                }
                if (isRasterGeoReof) {
                    if (line.startsWith("#")) {
                        final String[] parts = line.substring(1).split(";|,");
                        if (parts.length == 4) {
                            final Point point = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                            final Coordinate coordinate = new Coordinate(Double.parseDouble(parts[2]),
                                    Double.parseDouble(parts[3]));
                            pairs.add(new PointCoordinatePair(point, coordinate));
                        }
                    }
                } else {
                    if (line.trim().isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.contains(",")) {
                        line = line.replaceAll(",", ".");
                    }
                    matrix[index++] = Double.parseDouble(line);
                }
            }

            br.close();

            if (index == matrix.length) {
                final AffineTransformation transform = new AffineTransformation(
                        matrix[0],
                        matrix[2],
                        matrix[4],
                        matrix[1],
                        matrix[3],
                        matrix[5]);

                final Dimension imageDimension = ImageFileUtils.getImageDimension(imageFile);
                final double imageWidth = imageDimension.getWidth();
                final double imageHeight = imageDimension.getHeight();
                final Rectangle imageBounds = new Rectangle(0, 0, (int)imageWidth, (int)imageHeight);

                final GeometryFactory factory = new GeometryFactory(
                        new PrecisionModel(),
                        CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
                final LinearRing linear = factory.createLinearRing(
                        new Coordinate[] {
                            new Coordinate(0, 0),
                            new Coordinate(imageWidth, 0),
                            new Coordinate(imageWidth, imageHeight),
                            new Coordinate(0, imageHeight),
                            new Coordinate(0, 0)
                        });

                final Envelope imageEnvelope = transform.transform(factory.createPolygon(linear, null))
                            .getEnvelopeInternal();
                final ImageFileMetaData metaData = new ImageFileMetaData(
                        imageBounds,
                        imageEnvelope,
                        transform,
                        (isRasterGeoReof) ? pairs.toArray(new PointCoordinatePair[0]) : null);
                return metaData;
            }
        } catch (Exception e) {
            LOG.error("Cannot parse the world file", e);
        }

        return null;
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
