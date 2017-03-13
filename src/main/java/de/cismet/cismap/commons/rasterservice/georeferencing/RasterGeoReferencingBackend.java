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
import java.awt.Point;
import java.awt.Rectangle;

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

    private final Map<File, RasterGeoReferencingHandler> metaDataMap = new HashMap<File, RasterGeoReferencingHandler>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingBackend object.
     */
    private RasterGeoReferencingBackend() {
        // workaround assuring RasterGeoRefPanel is listener of wizard (see constructor of RasterGeoRefPanel)
        RasterGeoReferencingDialog.getInstance();
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
                final File imagefile = irs.getImageFile();
                if (!metaDataMap.containsKey(imagefile)) {
                    try {
                        final RasterGeoReferencingHandler handler = createInitHandler(imagefile);

                        handler.addListener(new RasterGeoReferencingHandlerListener() {

                                private void transformationChanged(final int position) {
                                    if (imagefile.equals(irs.getImageFile())) {
                                        irs.retrieve(true);
                                    }
                                }

                                @Override
                                public void positionAdded(final int position) {
                                    transformationChanged(position);
                                }

                                @Override
                                public void positionRemoved(final int position) {
                                    transformationChanged(position);
                                }

                                @Override
                                public void positionChanged(final int position) {
                                    transformationChanged(position);
                                }
                            });
                    } catch (final Exception ex) {
                        LOG.warn(ex, ex);
                    }
                }
                if (metaDataMap.size() == 1) {
                    RasterGeoReferencingWizard.getInstance().setHandler(metaDataMap.get(imagefile));
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
    public RasterGeoReferencingHandler getHandler(final File imageFile) {
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
    private RasterGeoReferencingHandler createInitHandler(final File imageFile) throws Exception {
        final Dimension imageDimension = ImageFileUtils.getImageDimension(imageFile);
        final Rectangle imageBounds = new Rectangle(
                0,
                0,
                (int)imageDimension.getWidth(),
                (int)imageDimension.getHeight());

        final BoundingBox bb = CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera();

        final double scale;
        if ((bb.getWidth() / bb.getHeight()) > (imageBounds.getWidth() / imageBounds.getHeight())) {
            scale = bb.getHeight() / imageBounds.getHeight();
        } else {
            scale = bb.getWidth() / imageBounds.getWidth();
        }
        final Coordinate mapCenterCoordinate = new Coordinate(
                bb.getX1()
                        + ((bb.getX2() - bb.getX1()) / 2d),
                bb.getY2()
                        + ((bb.getY1() - bb.getY2()) / 2d));
        final Envelope imageEnvelope = new Envelope(
                mapCenterCoordinate.x
                        - ((imageBounds.getWidth() * scale) / 2d),
                mapCenterCoordinate.x
                        + ((imageBounds.getWidth() * scale) / 2d),
                mapCenterCoordinate.y
                        + ((imageBounds.getHeight() * scale) / 2d),
                mapCenterCoordinate.y
                        - ((imageBounds.getHeight() * scale) / 2d));

        final ImageFileMetaData metaData = new ImageFileMetaData(
                imageBounds,
                imageEnvelope,
                null);
        final RasterGeoReferencingHandler handler = new RasterGeoReferencingHandler(metaData);
        handler.addPair(new PointCoordinatePair(
                new Point(0, 0),
                new Coordinate(imageEnvelope.getMinX(), imageEnvelope.getMaxY())));
        // upper right
        handler.addPair(new PointCoordinatePair(
                new Point((int)imageBounds.getWidth(), 0),
                new Coordinate(imageEnvelope.getMaxX(), imageEnvelope.getMaxY())));
        // downer middle
        handler.addPair(new PointCoordinatePair(
                new Point((int)imageBounds.getWidth() / 2, (int)imageBounds.getHeight()),
                new Coordinate(
                    imageEnvelope.getMaxX()
                            - (imageEnvelope.getWidth() / 2d),
                    imageEnvelope.getMinY())));

        handler.doCoordinateCorrection();

        metaDataMap.put(imageFile, handler);
        return handler;
    }

//    /**
//     * DOCUMENT ME!
//     *
//     * @param   pairs  DOCUMENT ME!
//     *
//     * @return  DOCUMENT ME!
//     *
//     * @throws  IllegalStateException  DOCUMENT ME!
//     */
//    private AffineTransform calculateTransform(final List<PointCoordinatePair> pairs) {
//        if (pairs.size() >= 3) {
//            // TODO permutation over all possible transformations and choose
//            // the one with the minimal error
//            final PointCoordinatePair pair0 = pairs.get(0);
//            final PointCoordinatePair pair1 = pairs.get(1);
//            final PointCoordinatePair pair2 = pairs.get(2);
//
//            final AffineTransformationBuilder builder = new AffineTransformationBuilder(
//                    new Coordinate(pair0.getPoint().getX(), pair0.getPoint().getY()),
//                    new Coordinate(pair1.getPoint().getX(), pair1.getPoint().getY()),
//                    new Coordinate(pair2.getPoint().getX(), pair2.getPoint().getY()),
//                    new Coordinate(pair0.getCoordinate()),
//                    new Coordinate(pair1.getCoordinate()),
//                    new Coordinate(pair2.getCoordinate()));
//            final double[] matrix = builder.getTransformation().getMatrixEntries();
//            return new AffineTransform(matrix[0], matrix[3], matrix[1], matrix[4], matrix[2], matrix[5]);
//        } else {
//            throw new IllegalStateException("minimum of 3 pairs requiered");
//        }
//    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        if (e.getLayer() instanceof ImageRasterService) {
            final ImageRasterService irs = (ImageRasterService)e.getLayer();
            if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                final File imageFile = irs.getImageFile();
                if (metaDataMap.containsKey(imageFile)) {
                    metaDataMap.remove(imageFile);
                    RasterGeoReferencingWizard.getInstance().setHandler(null);
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
        RasterGeoReferencingHandler handler = null;
        if (e.getLayer() instanceof ImageRasterService) {
            final ImageRasterService irs = (ImageRasterService)e.getLayer();
            if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                final File imagefile = irs.getImageFile();
                if (metaDataMap.containsKey(imagefile)) {
                    handler = metaDataMap.get(imagefile);
                }
            }
        }
        RasterGeoReferencingWizard.getInstance().setHandler(handler);
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
