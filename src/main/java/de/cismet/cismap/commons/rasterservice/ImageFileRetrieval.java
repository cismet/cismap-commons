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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import org.deegree.io.geotiff.GeoTiffException;
import org.deegree.io.geotiff.GeoTiffReader;

import org.openide.util.Exceptions;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingBackend;
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

    //~ Instance fields --------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */

    @Getter @Setter private int width;
    @Getter @Setter private int height;
    @Getter @Setter private double x1;
    @Getter @Setter private double x2;
    @Getter @Setter private double y1;
    @Getter @Setter private double y2;
    @Getter @Setter private File imageFile;
    private RetrievalListener listener = null;
    private volatile boolean youngerCall = false;
    private ImageFileMetaData metaData;
    private final ImageFileUtils.Mode mode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageFileRetrieval object.
     *
     * @param  imageFile  DOCUMENT ME!
     * @param  listener   DOCUMENT ME!
     * @param  mode       DOCUMENT ME!
     */
    public ImageFileRetrieval(final File imageFile,
            final RetrievalListener listener,
            final ImageFileUtils.Mode mode) {
        super("ImageFileRetrieval");
        this.imageFile = imageFile;
        this.listener = listener;

        this.mode = mode;
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

            if (youngerCall && isInterrupted()) {
                LOG.warn("Image retrieval aborted");
                return;
            }

            final BufferedImage mapImage = createImage(metaData);

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
     * DOCUMENT ME!
     *
     * @param   mapWorldBounds       DOCUMENT ME!
     * @param   imageMapWorldOffset  DOCUMENT ME!
     * @param   imageBounds          DOCUMENT ME!
     * @param   worldFileTransform   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Rectangle getClippingRect(final Rectangle.Double mapWorldBounds,
            final Point.Double imageMapWorldOffset,
            final Rectangle imageBounds,
            final AffineTransform worldFileTransform) {
        int mapPartStartX = (int)Math.floor(-imageMapWorldOffset.getX() / worldFileTransform.getScaleX());
        int mapPartStartY = (int)Math.floor(-imageMapWorldOffset.getY() / -worldFileTransform.getScaleY());
        int mapPartWidth = (int)Math.ceil(mapWorldBounds.getWidth() / worldFileTransform.getScaleX()) + 1;
        int mapPartHeight = (int)Math.ceil(mapWorldBounds.getHeight() / -worldFileTransform.getScaleY()) + 1;

        if (mapPartStartX < 0) {
            mapPartStartX = 0;
            mapPartWidth += mapPartStartX;
        }
        if (mapPartStartY < 0) {
            mapPartStartY = 0;
            mapPartHeight += mapPartStartY;
        }
        if ((mapPartStartX + mapPartWidth) > imageBounds.getWidth()) {
            mapPartWidth = (int)imageBounds.getWidth() - mapPartStartX;
        }
        if ((mapPartStartY + mapPartHeight) > imageBounds.getHeight()) {
            mapPartHeight = (int)imageBounds.getHeight() - mapPartStartY;
        }

        return new Rectangle(mapPartStartX, mapPartStartY, mapPartWidth, mapPartHeight);
    }

    /**
     * Creates an image of the given map section.
     *
     * @param   metaData  origImageBounds the bounds of the original image
     *
     * @return  an image of the given map section
     *
     * @throws  IOException                      DOCUMENT ME!
     * @throws  InterruptedException             DOCUMENT ME!
     * @throws  NoninvertibleTransformException  DOCUMENT ME!
     */
    private BufferedImage createImage(final ImageFileMetaData metaData) throws IOException,
        InterruptedException,
        NoninvertibleTransformException {
        final double[] matrix = metaData.getTransform().getMatrixEntries();
        final AffineTransform worldFileTransform = new AffineTransform(
                matrix[0],
                matrix[3],
                matrix[1],
                matrix[4],
                matrix[2],
                matrix[5]);

        // bounds in pixel dimensions
        final Rectangle mapBounds = new Rectangle(width, height);
        final Rectangle imageBounds = metaData.getImageBounds();

        // bounds in world dimensions
        final Rectangle.Double mapWorldBounds = new Rectangle.Double(x1, y1, x2 - x1, y2 - y1);
        final Envelope imageWorldBounds = metaData.getImageEnvelope();

        // the offset of the image in relation to the map in world dimensions
        final Point.Double imageMapWorldOffset = new Point.Double(
                imageWorldBounds.getMinX()
                        - mapWorldBounds.getMinX(),
                mapWorldBounds.getMaxY()
                        - imageWorldBounds.getMaxY());

        // meter per pixel ration (the better appropriate "Dimension" class only supports Integers
        // so we are using Rectangle.Double instead)
        final Rectangle.Double meterPerPixel = new Rectangle.Double(
                0,
                0,
                mapWorldBounds.getWidth()
                        / mapBounds.getWidth(),
                mapWorldBounds.getHeight()
                        / mapBounds.getHeight());

        // LOAD RAW IMAGE
        BufferedImage rawImage = ImageIO.read(imageFile);

        handleInterruption();

        // PRECLIPPING
        final Point.Double clippingWorldOffset;
        BufferedImage clippedImage;
        if ((worldFileTransform.getShearX() == 0) && (worldFileTransform.getShearY() == 0)) {
            // calculating clipping rectangle
            final Rectangle clippingRect = getClippingRect(
                    mapWorldBounds,
                    imageMapWorldOffset,
                    imageBounds,
                    worldFileTransform);

            // the clipped image does not start at the same positon. an offset is needed to compensate for this
            clippingWorldOffset = new Point.Double(
                    clippingRect.getX()
                            * worldFileTransform.getScaleX(),
                    -clippingRect.getY()
                            * worldFileTransform.getScaleY());
            // clipping the image
            clippedImage = rawImage.getSubimage((int)clippingRect.getX(),
                    (int)clippingRect.getY(),
                    (int)clippingRect.getWidth(),
                    (int)clippingRect.getHeight());
        } else { // no preclipping for sheared/rotated images for simplicity reasons
            clippingWorldOffset = new Point.Double(0, 0);
            clippedImage = rawImage;
        }

        // cleaning memory
        rawImage = null;
        System.gc();

        handleInterruption();

        // TRANSFORMATION
        // Just calculating the transformed shape first.
        // (Not very elegant to do this, but it works)
        // scaling and shearing = worldfile scaling/shearing divided by meterPerPixel
        final AffineTransform transformation = new AffineTransform(
                worldFileTransform.getScaleX()
                        / meterPerPixel.getWidth(),
                -worldFileTransform.getShearY()
                        / meterPerPixel.getHeight(),
                worldFileTransform.getShearX()
                        / meterPerPixel.getWidth(),
                -worldFileTransform.getScaleY()
                        / meterPerPixel.getHeight(),
                0,
                0);

        // The x/y coordinate of the transformed (but not yet translated) is either 0
        // or negative. This is important, because negative values hav to be added to the offset
        final Rectangle shapeBounds = transformation.createTransformedShape(imageBounds).getBounds();

        // We apply now the full transormation to the image
        // position = combined world offsets divided by meterPerPixel
        final AffineTransform transformation2 = new AffineTransform(
                transformation.getScaleX(),
                transformation.getShearY(),
                transformation.getShearX(),
                transformation.getScaleY(),
                ((imageMapWorldOffset.getX() + clippingWorldOffset.getX())
                            / meterPerPixel.getWidth())
                        - shapeBounds.getX(),
                ((imageMapWorldOffset.getY() + clippingWorldOffset.getY())
                            / meterPerPixel.getHeight())
                        - shapeBounds.getY());
        final BufferedImage transformedImage = transform(transformation2, clippedImage);

        // cleaning memory
        clippedImage = null;
        System.gc();

        return transformedImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  InterruptedException  DOCUMENT ME!
     */
    private void handleInterruption() throws InterruptedException {
        if (youngerCall && isInterrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * Rescale the given image and add transparent borders.
     *
     * @param   transform  width transform DOCUMENT ME!
     * @param   image      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage transform(final AffineTransform transform, final BufferedImage image) {
        final BufferedImage transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if (image != null) {
            final Graphics2D g = transformedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, transform, null);
        }
        return transformedImage;
    }

    /**
     * Determines the meta information about the image file.
     *
     * @return  the meta information about the image file
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private ImageFileMetaData getImageMetaData() throws Exception {
        if (mode != null) {
            switch (mode) {
                case WORLDFILE: {
                    return getWorldFileMetaData(getWorldFile());
                }
                case TIFF: {
                    getTiffMetaData();
                }
                case GEO_REFERENCED: {
                    return getGeoReferencedMetaData();
                }
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private ImageFileMetaData getGeoReferencedMetaData() throws Exception {
        return RasterGeoReferencingBackend.getInstance().getHandler(imageFile).getMetaData();
    }

    /**
     * Determines the meta information about the image file. It will be assumed, that the image file is a tiff.
     *
     * @return  the meta information about the image file
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private ImageFileMetaData getTiffMetaData() throws Exception {
        return ImageFileUtils.getTiffMetaData(imageFile);
    }

    /**
     * Determines the meta information about the image file. It will be assumed, that a world file for the image file
     * exists. The world file must have the file ending .jgw, .pgw, .gfw or .tfw depending on the image format.
     *
     * @param   worldFile  DOCUMENT ME!
     *
     * @return  the meta information about the image file
     */
    private ImageFileMetaData getWorldFileMetaData(final File worldFile) {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(worldFile));
            final double[] matrix = new double[6];

            // read parameter from world file
            int index = 0;
            String line;
            while (((line = br.readLine()) != null) && (index < (matrix.length))) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                if (line.contains(",")) {
                    line = line.replaceAll(",", ".");
                }
                matrix[index++] = Double.parseDouble(line);
            }

            br.close();

            if (index == matrix.length) {
                final AffineTransformation transform = new AffineTransformation(matrix);

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
                return new ImageFileMetaData(
                        imageBounds,
                        imageEnvelope,
                        transform);
            }
        } catch (Exception e) {
            LOG.error("Cannot parse the world file", e);
        }

        return null;
    }

    /**
     * Returns the world file or null, if it does not exist.
     *
     * @return  the world file of the <code>imageFile</code>
     */
    private File getWorldFile() {
        return ImageFileUtils.getWorldFile(imageFile);
    }

    /**
     * The envelope of the image.
     *
     * @return  the envelope of the image
     */
    public Envelope getEnvelope() {
        try {
            if (metaData == null) {
                final ImageFileMetaData metaData = getImageMetaData();

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
}
