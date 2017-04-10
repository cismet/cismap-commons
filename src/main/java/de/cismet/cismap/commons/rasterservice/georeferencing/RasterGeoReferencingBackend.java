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
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;

import lombok.AccessLevel;
import lombok.Getter;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RasterGeoRefFeature;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;
import de.cismet.cismap.commons.rasterservice.ImageFileUtils;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;

import static de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandler.createAverageTransformation;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingBackend {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            RasterGeoReferencingBackend.class);

    //~ Instance fields --------------------------------------------------------

    @Getter(AccessLevel.PRIVATE)
    private final Map<File, RasterGeoReferencingHandler> metaDataMap = new HashMap<>();

    @Getter private final ActiveLayerListenerHandler activeLayerListenerHandler = new ActiveLayerListenerHandler();

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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MappingComponent getMainMap() {
        return CismapBroker.getInstance().getMappingComponent();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RasterGeoReferencingHandler getHandler(final File imageFile) {
        return getMetaDataMap().get(imageFile);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   service    DOCUMENT ME!
     * @param   imageFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static RasterGeoReferencingHandler createInitHandler(final ImageRasterService service, final File imageFile)
            throws Exception {
        final Dimension imageDimension = ImageFileUtils.getImageDimension(imageFile);
        final Rectangle imageBounds = new Rectangle(
                0,
                0,
                (int)imageDimension.getWidth(),
                (int)imageDimension.getHeight());

        final BoundingBox bb = getMainMap().getCurrentBoundingBoxFromCamera();

        final double scale;
        if ((bb.getWidth() / bb.getHeight()) > (imageBounds.getWidth() / imageBounds.getHeight())) {
            scale = bb.getHeight()
                        / imageBounds.getHeight();
        } else {
            scale = bb.getWidth()
                        / imageBounds.getWidth();
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

        final PointCoordinatePair[] pairs = new PointCoordinatePair[] {
                // upper left
                new PointCoordinatePair(
                    new Point(0, 0),
                    new Coordinate(imageEnvelope.getMinX(), imageEnvelope.getMaxY())),
                // upper right
                new PointCoordinatePair(
                    new Point((int)imageBounds.getWidth(), 0),
                    new Coordinate(imageEnvelope.getMaxX(), imageEnvelope.getMaxY())),
                // bottom middle
                new PointCoordinatePair(
                    new Point((int)imageBounds.getWidth() / 2, (int)imageBounds.getHeight()),
                    new Coordinate(
                        imageEnvelope.getMaxX()
                                - (imageEnvelope.getWidth() / 2d),
                        imageEnvelope.getMinY()))
            };
        final AffineTransformation transform = calculateAvgTransformation(pairs);

        final ImageFileMetaData metaData = new ImageFileMetaData(
                imageBounds,
                imageEnvelope,
                transform);

        return new RasterGeoReferencingHandler(service, metaData);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RasterGeoReferencingWizard getWizard() {
        return RasterGeoReferencingWizard.getInstance();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   completePairs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AffineTransformation calculateAvgTransformation(final PointCoordinatePair[] completePairs) {
        final List<AffineTransformation> transforms = new ArrayList<>();
        if (completePairs.length >= 3) {
            for (final Object[] arr : RasterGeoReferencingHandler.getCombinations(completePairs, 3)) {
                final PointCoordinatePair pair0 = (PointCoordinatePair)arr[0];
                final PointCoordinatePair pair1 = (PointCoordinatePair)arr[1];
                final PointCoordinatePair pair2 = (PointCoordinatePair)arr[2];

                final AffineTransformationBuilder builder = new AffineTransformationBuilder(
                        new Coordinate(pair0.getPoint().getX(), pair0.getPoint().getY()),
                        new Coordinate(pair1.getPoint().getX(), pair1.getPoint().getY()),
                        new Coordinate(pair2.getPoint().getX(), pair2.getPoint().getY()),
                        pair0.getCoordinate(),
                        pair1.getCoordinate(),
                        pair2.getCoordinate());

                final AffineTransformation transform = builder.getTransformation();
                if (transform != null) {
                    transforms.add(transform);
                }
            }

            final AffineTransformation avgTransform = createAverageTransformation(transforms);
            return avgTransform;
        }
        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ActiveLayerListenerHandler implements ActiveLayerListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void layerAdded(final ActiveLayerEvent e) {
            final Object layer = e.getLayer();
            if (!RasterGeoReferencingWizard.getInstance().getIgnoreLayerList().contains(layer)) {
                try {
                    if (layer instanceof ImageRasterService) {
                        final ImageRasterService irs = (ImageRasterService)layer;
                        if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                            final File imagefile = irs.getImageFile();
                            if (!getMetaDataMap().containsKey(imagefile)) {
                                try {
                                    final RasterGeoReferencingHandler handler = createInitHandler(irs, imagefile);
                                    getMetaDataMap().put(imagefile, handler);
                                    handler.addListener(new RasterGeoReferencingHandlerListener() {

                                            @Override
                                            public void transformationChanged() {
                                                if (handler.isComplete()) {
                                                    if (imagefile.equals(irs.getImageFile())) {
                                                        irs.retrieve(true);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void positionAdded(final int position) {
                                            }

                                            @Override
                                            public void positionRemoved(final int position) {
                                            }

                                            @Override
                                            public void positionChanged(final int position) {
                                            }
                                        });

                                    final Feature feature = handler.getFeature();
                                    final FeatureCollection featureCollection = getMainMap().getFeatureCollection();
                                    if ((feature != null) && (featureCollection != null)
                                                && !featureCollection.contains(feature)) {
                                        featureCollection.addFeature(feature);
                                    }
                                } catch (final Exception ex) {
                                    LOG.warn(ex, ex);
                                }
                            }

                            // load handler into the wizard if it is the only handler
                            if (getMetaDataMap().size() == 1) {
                                getWizard().setHandler(getMetaDataMap().get(imagefile));
                            }
                        }
                    }
                } catch (final Throwable t) {
                    LOG.fatal(t, t);
                }
            }
        }

        @Override
        public void layerRemoved(final ActiveLayerEvent e) {
            final Object layer = e.getLayer();
            if (!RasterGeoReferencingWizard.getInstance().getIgnoreLayerList().contains(layer)) {
                if (layer instanceof ImageRasterService) {
                    final ImageRasterService irs = (ImageRasterService)layer;
                    if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                        final File imageFile = irs.getImageFile();
                        if (getMetaDataMap().containsKey(imageFile)) {
                            final RasterGeoRefFeature feature = getHandler(imageFile).getFeature();
                            getWizard().setHandler(null);
                            final FeatureCollection featureCollection = getMainMap().getFeatureCollection();
                            if ((feature != null) && (featureCollection != null)
                                        && featureCollection.contains(feature)) {
                                featureCollection.removeFeature(feature);
                                getWizard().removeListener(feature);
                            }
                            getMetaDataMap().remove(imageFile);
                        }
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
            final Object layer = e.getLayer();
            if (!RasterGeoReferencingWizard.getInstance().getIgnoreLayerList().contains(layer)) {
                if (layer instanceof ImageRasterService) {
                    final ImageRasterService irs = (ImageRasterService)layer;
                    if (ImageFileUtils.Mode.GEO_REFERENCED == irs.getMode()) {
                        final File imagefile = irs.getImageFile();
                        if (getMetaDataMap().containsKey(imagefile)) {
                            handler = getMetaDataMap().get(imagefile);
                        }
                    }
                }
                getWizard().setHandler(handler);
            }
        }
    }

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
