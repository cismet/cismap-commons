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
package de.cismet.cismap.commons.rasterservice;

import com.vividsolutions.jts.geom.Envelope;

import org.apache.batik.ext.awt.image.codec.tiff.TIFFImage;
import org.apache.log4j.Logger;

import org.deegree.io.geotiff.GeoTiffException;
import org.deegree.io.geotiff.GeoTiffReader;

import org.openide.util.Exceptions;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

/**
 * Loads map sections from image files.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ImageFileRetrieval extends Thread {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ImageFileRetrieval.class);
    private static final Map<String, String> WORLD_FILE_ENDINGS = new HashMap<String, String>();

    static {
        WORLD_FILE_ENDINGS.put("jpg", "jgw");
        WORLD_FILE_ENDINGS.put("png", "pgw");
        WORLD_FILE_ENDINGS.put("gif", "gfw");
        WORLD_FILE_ENDINGS.put("tif", "tfw");
    }

    //~ Instance fields --------------------------------------------------------

    private int width;
    private int height;
    private double x1;
    private double x2;
    private double y1;
    private double y2;
    private File imageFile;
    private RetrievalListener listener = null;
    private volatile boolean youngerCall = false;
    private ImageMetaData metaData;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageFileRetrieval object.
     *
     * @param  imageFile  DOCUMENT ME!
     * @param  listener   DOCUMENT ME!
     */
    public ImageFileRetrieval(final File imageFile, final RetrievalListener listener) {
        this.imageFile = imageFile;
        this.listener = listener;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void youngerCall() {
        youngerCall = true;
    }

    @Override
    public void run() {
        try {
            listener.retrievalStarted(new RetrievalEvent());

            if (metaData == null) {
                metaData = getImageMetaData();
            }

            if (metaData == null) {
                final RetrievalEvent re = new RetrievalEvent();
                re.setIsComplete(false);
                re.setRetrievedObject("Cannot read meta data");
                re.setErrorType(RetrievalEvent.CLIENTERROR);
                listener.retrievalError(re);

                return;
            }

            final Envelope en = metaData.getImageEnvelope();
            final Rectangle rec = metaData.getImageBounds();

            if (youngerCall && isInterrupted()) {
                LOG.warn("Image retrieval aborted");
                return;
            }

            final BufferedImage mapImage = createImage(rec, en);

            final RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(true);
            re.setRetrievedObject(mapImage);
            listener.retrievalComplete(re);
        } catch (InterruptedException e) {
            LOG.warn("Image retrieval aborted");
        } catch (OutOfMemoryError ex) {
            LOG.error("Image retrieval aborted. Out o memory error.", ex);
            final RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(false);
            re.setRetrievedObject(ex.getMessage());
            re.setErrorType(RetrievalEvent.CLIENTERROR);
            listener.retrievalError(re);
        } catch (Exception ex) {
            LOG.error("Error during image processing.", ex);
            final RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(false);
            if ((ex.getMessage() == null) || ex.getMessage().equals("null")) { // NOI18N
                try {
                    final String cause = ex.getCause().getMessage();
                    re.setRetrievedObject(cause);
                } catch (Exception ee) {
                }
            } else {
                re.setRetrievedObject(ex.getMessage());
                re.setErrorType(RetrievalEvent.CLIENTERROR);
            }

            listener.retrievalError(re);
        }
    }

    /**
     * Creates an image of the given map section.
     *
     * @param   origImageBounds  the bounds of the original image
     * @param   origImageCoords  the envelope of the original image
     *
     * @return  an image of the given map section
     *
     * @throws  IOException           DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    private BufferedImage createImage(final Rectangle origImageBounds, final Envelope origImageCoords)
            throws IOException, InterruptedException {
        final double meterPerPixelWidth = origImageCoords.getWidth() / origImageBounds.getWidth();
        final double meterPerPixelHeight = origImageCoords.getHeight() / origImageBounds.getHeight();
        int mapPartStartX = (int)((x1 - origImageCoords.getMinX()) / meterPerPixelWidth);
        int mapPartStartY = (int)((origImageCoords.getMaxY() - y2) / meterPerPixelHeight);
        int mapPartWidth = (int)((x2 - x1) / meterPerPixelWidth);
        int mapPartHeight = (int)((y2 - y1) / meterPerPixelHeight);
        int imageWidth = width;
        int imageHeight = height;
        int borderLeft = 0;
        int borderTop = 0;
        int borderRight = 0;
        int borderBottom = 0;

        if (mapPartStartX < 0) {
            // add left border
            mapPartWidth -= Math.abs(mapPartStartX);
            borderLeft = (int)(Math.abs(mapPartStartX) * meterPerPixelWidth / ((x2 - x1) / width));
            imageWidth -= borderLeft;
            mapPartStartX = 0;
        }

        if (mapPartStartY < 0) {
            // add top border
            mapPartHeight -= Math.abs(mapPartStartY);
            borderTop = (int)(Math.abs(mapPartStartY) * meterPerPixelHeight / ((y2 - y1) / height));
            imageHeight -= borderTop;
            mapPartStartY = 0;
        }

        if ((mapPartStartX + mapPartWidth) > origImageBounds.getWidth()) {
            // add right border
            borderRight = (int)(((mapPartStartX + mapPartWidth) - origImageBounds.getWidth()) * meterPerPixelWidth
                            / ((x2 - x1) / width));
            mapPartWidth = ((int)origImageBounds.getWidth() - mapPartStartX);
            imageWidth = imageWidth - borderRight;
        }

        if ((mapPartStartY + mapPartHeight) > origImageBounds.getHeight()) {
            // add bottom border
            borderBottom = (int)(((mapPartStartY + mapPartHeight) - origImageBounds.getHeight()) * meterPerPixelHeight
                            / ((y2 - y1) / height));
            mapPartHeight = ((int)origImageBounds.getHeight() - mapPartStartY);
            imageHeight = imageHeight - borderBottom;
        }

        BufferedImage imagePart = null;

        if ((mapPartWidth > 0) && (mapPartHeight > 0)) {
            BufferedImage i = ImageIO.read(imageFile);
            imagePart = i.getSubimage(mapPartStartX, mapPartStartY, mapPartWidth, mapPartHeight);
            i = null;
            System.gc();
        }

        if (youngerCall && isInterrupted()) {
            throw new InterruptedException();
        }

        final BufferedImage rescaledImage = rescale(
                imageWidth,
                imageHeight,
                borderLeft,
                borderRight,
                borderTop,
                borderBottom,
                BufferedImage.TYPE_INT_ARGB,
                imagePart);

        imagePart = null;
        System.gc();

        return rescaledImage;
    }

    /**
     * Rescale the given image and add transparent borders.
     *
     * @param   width         DOCUMENT ME!
     * @param   height        DOCUMENT ME!
     * @param   borderLeft    DOCUMENT ME!
     * @param   borderRight   DOCUMENT ME!
     * @param   borderTop     DOCUMENT ME!
     * @param   borderBottom  DOCUMENT ME!
     * @param   type          DOCUMENT ME!
     * @param   image         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage rescale(final int width,
            final int height,
            final int borderLeft,
            final int borderRight,
            final int borderTop,
            final int borderBottom,
            final int type,
            final BufferedImage image) {
        final int totalWdth = width + borderLeft + borderRight;
        final int totalHeight = height + borderTop + borderBottom;
        final BufferedImage resized = new BufferedImage(totalWdth, totalHeight, type);

        if (image != null) {
            final Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(
                image,
                borderLeft,
                borderTop,
                width
                        + borderLeft,
                height
                        + borderTop,
                0,
                0,
                image.getWidth(),
                image.getHeight(),
                null);
            g.dispose();
        }

        return resized;
    }

    /**
     * Determines the meta information about the image file.
     *
     * @return  the meta information about the image file
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private ImageMetaData getImageMetaData() throws Exception {
        final File worldFile = getWorldFile();

        if (worldFile != null) {
            return getWorldFileMetaData(worldFile);
        } else if (imageFile.getName().toLowerCase().endsWith("tif")) {
            return getTiffMetaData();
        }

        return null;
    }

    /**
     * Determines the meta information about the image file. It will be assumed, that the image file is a tiff.
     *
     * @return  the meta information about the image file
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private ImageMetaData getTiffMetaData() throws Exception {
        GeoTiffReader r = new GeoTiffReader(imageFile);
        final org.deegree.model.spatialschema.Envelope e = r.getBoundingBox();
        final Envelope en = new Envelope(e.getMin().getX(), e.getMax().getX(), e.getMin().getY(), e.getMax().getY());
        final TIFFImage tiffImage = r.getTIFFImage();
        final Rectangle rec = tiffImage.getBounds();
        r = null;
        System.gc();

        return new ImageMetaData(rec, en);
    }

    /**
     * Determines the meta information about the image file. It will be assumed, that a world file for the image file
     * exists. The world file must have the file ending .jgw, .pgw, .gfw or .tfw depending on the image format.
     *
     * @param   worldFile  DOCUMENT ME!
     *
     * @return  the meta information about the image file
     */
    private ImageMetaData getWorldFileMetaData(final File worldFile) {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(worldFile));
            String line;
            final List<Double> parameter = new ArrayList<Double>();

            // read parameter from world file
            while ((line = br.readLine()) != null) {
                if (parameter.size() > 5) {
                    break;
                }
                parameter.add(Double.parseDouble(line));
            }

            br.close();

            if (parameter.size() > 5) {
                if ((parameter.get(1) == 0.0) && (parameter.get(2) == 0.0)) {
                    final Dimension imageDimension = getImageDimension(imageFile);
                    final Rectangle bounds = new Rectangle(
                            0,
                            0,
                            (int)imageDimension.getWidth(),
                            (int)imageDimension.getHeight());

                    final double x1 = parameter.get(4);
                    final double x2 = x1 + (parameter.get(0) * imageDimension.getWidth());
                    final double y2 = parameter.get(5);
                    final double y1 = y2 + (parameter.get(3) * imageDimension.getHeight());

                    final Envelope en = new Envelope(x1, x2, y1, y2);

                    return new ImageMetaData(bounds, en);
                } else {
                    // todo transform image
                }
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

    /**
     * Returns the world file or null, if it does not exist.
     *
     * @return  the world file of the <code>imageFile</code>
     */
    private File getWorldFile() {
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
     * The envelope of the image.
     *
     * @return  the envelope of the image
     */
    public Envelope getEnvelope() {
        try {
            if (metaData == null) {
                final ImageMetaData metaData = getImageMetaData();

                if (metaData != null) {
                    return metaData.getImageEnvelope();
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot determine the envelope of the image.", e);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            final GeoTiffReader r = new GeoTiffReader(new File("/home/therter/share/daten/uek250.tif"));
            System.out.println(r.getHumanReadableCoordinateSystem());
            r.getTIFFImage().getBounds();
            r.getTIFFImage().getHeight();
            r.getTIFFImage().getMinTileX();
            r.getTIFFImage().getNumXTiles();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (GeoTiffException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  width  the width to set
     */
    public void setWidth(final int width) {
        this.width = width;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  height  the height to set
     */
    public void setHeight(final int height) {
        this.height = height;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the x1
     */
    public double getX1() {
        return x1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x1  the x1 to set
     */
    public void setX1(final double x1) {
        this.x1 = x1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the x2
     */
    public double getX2() {
        return x2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x2  the x2 to set
     */
    public void setX2(final double x2) {
        this.x2 = x2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the y1
     */
    public double getY1() {
        return y1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y1  the y1 to set
     */
    public void setY1(final double y1) {
        this.y1 = y1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the y2
     */
    public double getY2() {
        return y2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y2  the y2 to set
     */
    public void setY2(final double y2) {
        this.y2 = y2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the imageFile
     */
    public File getImageFile() {
        return imageFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  imageFile  the imageFile to set
     */
    public void setImageFile(final File imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (59 * hash) + this.width;
        hash = (59 * hash) + this.height;
        hash = (59 * hash) + (int)(Double.doubleToLongBits(this.x1) ^ (Double.doubleToLongBits(this.x1) >>> 32));
        hash = (59 * hash) + (int)(Double.doubleToLongBits(this.x2) ^ (Double.doubleToLongBits(this.x2) >>> 32));
        hash = (59 * hash) + (int)(Double.doubleToLongBits(this.y1) ^ (Double.doubleToLongBits(this.y1) >>> 32));
        hash = (59 * hash) + (int)(Double.doubleToLongBits(this.y2) ^ (Double.doubleToLongBits(this.y2) >>> 32));
        hash = (59 * hash) + ((this.imageFile != null) ? this.imageFile.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImageFileRetrieval other = (ImageFileRetrieval)obj;
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        if (Double.doubleToLongBits(this.x1) != Double.doubleToLongBits(other.x1)) {
            return false;
        }
        if (Double.doubleToLongBits(this.x2) != Double.doubleToLongBits(other.x2)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y1) != Double.doubleToLongBits(other.y1)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y2) != Double.doubleToLongBits(other.y2)) {
            return false;
        }
        if ((this.imageFile != other.imageFile)
                    && ((this.imageFile == null) || !this.imageFile.equals(other.imageFile))) {
            return false;
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  other  the object, the meta data should be copied from
     */
    public void copyMetaData(final ImageFileRetrieval other) {
        this.metaData = other.metaData;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ImageMetaData {

        //~ Instance fields ----------------------------------------------------

        private Rectangle imageBounds;
        private Envelope imageEnvelope;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ImageMetaData object.
         *
         * @param  imageBounds    DOCUMENT ME!
         * @param  imageEnvelope  DOCUMENT ME!
         */
        public ImageMetaData(final Rectangle imageBounds, final Envelope imageEnvelope) {
            this.imageBounds = imageBounds;
            this.imageEnvelope = imageEnvelope;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the imageBounds
         */
        public Rectangle getImageBounds() {
            return imageBounds;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  imageBounds  the imageBounds to set
         */
        public void setImageBounds(final Rectangle imageBounds) {
            this.imageBounds = imageBounds;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the imageEnvelope
         */
        public Envelope getImageEnvelope() {
            return imageEnvelope;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  imageEnvelope  the imageEnvelope to set
         */
        public void setImageEnvelope(final Envelope imageEnvelope) {
            this.imageEnvelope = imageEnvelope;
        }
    }
}
