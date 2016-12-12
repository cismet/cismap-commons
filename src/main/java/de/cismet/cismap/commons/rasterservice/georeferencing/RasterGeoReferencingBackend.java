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
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;
import de.cismet.cismap.commons.rasterservice.ImageFileUtils;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingBackend implements ActiveLayerListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            RasterGeoReferencingBackend.class);

    //~ Instance fields --------------------------------------------------------

    private Map<File, ImageFileMetaData> metaDataMap = new HashMap<File, ImageFileMetaData>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingBackend object.
     */
    private RasterGeoReferencingBackend() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RasterGeoReferencingBackend getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    @Override
    public void layerAdded(final ActiveLayerEvent e) {
        if (e.getLayer() instanceof ImageRasterService) {
            final ImageRasterService irs = (ImageRasterService)e.getLayer();
            if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                LOG.fatal(irs.getName());
                final File imagefile = irs.getImageFile();
                final ImageFileMetaData metaData;
                if (!metaDataMap.containsKey(imagefile)) {
                    try {
                        metaData = createDefaultMetaData(imagefile);
                        metaDataMap.put(imagefile, metaData);
                    } catch (final Exception ex) {
                        LOG.warn(ex, ex);
                    }
                } else {
                    metaData = metaDataMap.get(imagefile);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageFileMetaData getImageMetaData(final File imageFile) {
        return metaDataMap.get(imageFile);
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
    private ImageFileMetaData createDefaultMetaData(final File imageFile) throws Exception {
        final Dimension imageDimension = ImageFileUtils.getImageDimension(imageFile);
        final Rectangle imageBounds = new Rectangle(
                0,
                0,
                (int)imageDimension.getWidth(),
                (int)imageDimension.getHeight());

        final BoundingBox bb = CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera();
        final Rectangle mapBounds = CismapBroker.getInstance().getMappingComponent().getBounds();

        final double scale;
        if ((bb.getWidth() / bb.getHeight()) > (imageBounds.getWidth() / imageBounds.getHeight())) {
            scale = bb.getHeight() / imageBounds.getHeight();
        } else {
            scale = bb.getWidth() / imageBounds.getWidth();
        }
        final Coordinate mapCenterCoordinate = new Coordinate(
                bb.getX1()
                        + ((bb.getX2() - bb.getX1()) / 2),
                bb.getY2()
                        + ((bb.getY1() - bb.getY2()) / 2));
        final Point2D.Double imageCenterPoint = new Point2D.Double((imageDimension.getWidth() * scale) / 2,
                (imageDimension.getHeight() * scale)
                        / 2);
        final AffineTransform transform = new AffineTransform(
                scale,
                0,
                0,
                -scale,
                mapCenterCoordinate.x
                        - imageCenterPoint.getX(),
                mapCenterCoordinate.y
                        + imageCenterPoint.getY());

        final Rectangle transformedBounds = ((Path2D)transform.createTransformedShape(imageBounds)).getBounds();
        return new ImageFileMetaData(
                imageBounds,
                new Envelope(
                    transformedBounds.getX(),
                    transformedBounds.getX()
                            + transformedBounds.getWidth(),
                    transformedBounds.getY(),
                    transformedBounds.getY()
                            + transformedBounds.getHeight()),
                transform);
    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        if (e.getLayer() instanceof ImageRasterService) {
            final ImageRasterService irs = (ImageRasterService)e.getLayer();
            if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                final File imageFile = irs.getImageFile();
                if (metaDataMap.containsKey(imageFile)) {
                    metaDataMap.remove(imageFile);
                }
            }
        }
    }

    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerAvailabilityChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerSelectionChanged(final ActiveLayerEvent e) {
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final RasterGeoReferencingBackend INSTANCE = new RasterGeoReferencingBackend();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
