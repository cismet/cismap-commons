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
package de.cismet.cismap.commons;

import edu.umd.cs.piccolo.PNode;

import java.awt.Image;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.PrintingSettingsWidget;
import de.cismet.cismap.commons.gui.printing.PrintingWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.raster.wms.AbstractWMSServiceLayer;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RepaintEvent;
import de.cismet.cismap.commons.retrieval.RepaintListener;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class HeadlessMapProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(HeadlessMapProvider.class);
    private static final double FEATURE_RESOLUTION_FACTOR = 125.0d;

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum DominatingDimension {

        //~ Enum constants -----------------------------------------------------

        SIZE, BOUNDINGBOX
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum NotificationLevel {

        //~ Enum constants -----------------------------------------------------

        TIP, INFO, SUCCESS, EXPERT, WARN, ERROR, ERROR_REASON, UNLOCKED
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum RoundingPrecision {

        //~ Enum constants -----------------------------------------------------

        NO_ROUNDING, TENTH, HUNDRETH, THOUSANDTH
    }

    //~ Instance fields --------------------------------------------------------

    MappingComponent map = new MappingComponent(false);
    ActiveLayerModel mappingModel = new ActiveLayerModel();
    XBoundingBox boundingBox = null;
    private double minimumScaleDenominator = 0;
    private double imgScaleDenominator;
    private List<PropertyChangeListener> propertyChangeListener = new ArrayList<PropertyChangeListener>();
    private HeadlessMapProvider.DominatingDimension dominatingDimension = HeadlessMapProvider.DominatingDimension.SIZE;
    private HeadlessMapProvider.RoundingPrecision roundScaleTo = HeadlessMapProvider.RoundingPrecision.NO_ROUNDING;
    private boolean centerMapOnResize = false;
    private double printingResolution = 0;
    private double resolution = 72.0;
    private double featureResolutionFactor = PrintingSettingsWidget.FEATURE_RESOLUTION_FACTOR;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HeadlessMapProvider object.
     */
    public HeadlessMapProvider() {
        map.setResizeEventActivated(false);
        map.setInternalLayerWidgetAvailable(false);

        if (CismapBroker.getInstance().getMappingComponent() != null) {
            // adopt the mapping configuration from the main MappingComponent
            // Set the default Crs
            final List<Crs> crsList = CismapBroker.getInstance().getMappingComponent().getCrsList();
            final String defaultCrs = CismapBroker.getInstance().getDefaultCrs();
            XBoundingBox homeBoundingBox = null;
            boolean defaultCrsFound = false;

            for (final Crs crs : crsList) {
                if (crs.getCode().equals(defaultCrs)) {
                    mappingModel.setSrs(crs);
                    defaultCrsFound = true;
                    break;
                }
            }

            if (!defaultCrsFound) {
                LOG.warn("Default crs not found. Use EPSG:25832");
                mappingModel.setSrs(new Crs("EPSG:25832", "", "", true, true));
            }

            if (CismapBroker.getInstance().getMappingComponent().getMappingModel().getInitialBoundingBox()
                        instanceof XBoundingBox) {
                boundingBox = (XBoundingBox)CismapBroker.getInstance().getMappingComponent().getMappingModel()
                            .getInitialBoundingBox();
            }

            final HashMap homeMap =
                ((ActiveLayerModel)CismapBroker.getInstance().getMappingComponent().getMappingModel())
                        .getHomeBoundingBoxes();
            final Set keys = homeMap.keySet();

            for (final Object key : keys) {
                homeBoundingBox = (XBoundingBox)homeMap.get(key);
                mappingModel.addHome(homeBoundingBox);
            }

            if (mappingModel.getInitialBoundingBox() == null) {
                LOG.error("Default home boundingBox not found");

                if (homeBoundingBox != null) {
                    LOG.warn("Calculate home bounding box from " + homeBoundingBox);
                    try {
                        final CrsTransformer transformer = new CrsTransformer(defaultCrs);

                        homeBoundingBox = transformer.transformBoundingBox(homeBoundingBox);
                        mappingModel.addHome(homeBoundingBox);
                    } catch (Exception e) {
                        LOG.error("Cannot calculate home bounding box", e);
                    }
                }
            }
        } else {
            // set default values for test purposes
            // Set the default Crs
            mappingModel.setSrs(new Crs("EPSG:35833", "EPSG:35833", "EPSG:35833", true, true));
            boundingBox = new XBoundingBox(33298653.1, 5994912.610934, 33308958.598, 5999709.97916, "EPSG:35833", true);
            mappingModel.addHome(boundingBox);
        }

        // set the model
        map.setMappingModel(mappingModel);
        map.setSize(500, 500);

        // initial positioning of the map
        map.setAnimationDuration(0);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a HeadlessMapProvider and adds the raster layers and feature layers from the mapping component to it.
     *
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static HeadlessMapProvider createHeadlessMapProviderAndAddLayers(final MappingComponent mappingComponent) {
        final HeadlessMapProvider headlessMapProvider = new HeadlessMapProvider();
        ((ActiveLayerModel)headlessMapProvider.getMappingComponent().getMappingModel()).setSrs(
            mappingComponent.getMappingModel().getSrs());
        headlessMapProvider.getMappingComponent().setInfoNodesVisible(mappingComponent.isInfoNodesVisible());

        // Raster Services
        final TreeMap rasterServices = mappingComponent.getMappingModel().getRasterServices();
        final List positionsRaster = new ArrayList(rasterServices.keySet());
        Collections.sort(positionsRaster);

        for (final Object position : positionsRaster) {
            boolean addable = false;
            final Object rasterService = rasterServices.get(position);
            if ((rasterService instanceof RetrievalServiceLayer)
                        && ((RetrievalServiceLayer)rasterService).isEnabled()) {
                if ((rasterService instanceof AbstractWMSServiceLayer)
                            && ((AbstractWMSServiceLayer)rasterService).isVisible()) {
                    addable = true;
                } else if ((rasterService instanceof AbstractWMS) && ((AbstractWMS)rasterService).isVisible()) {
                    addable = true;
                } else if ((rasterService instanceof AbstractFeatureService)
                            && ((AbstractFeatureService)rasterService).isVisible()) {
                    addable = true;
                }
            }
            if (addable) {
                headlessMapProvider.addLayer((RetrievalServiceLayer)rasterService);
            } else {
                LOG.warn(
                    "Layer can not be added to the headlessMapProvider as it is not an instance of RetrievalServiceLayer");
            }
        }

        // Feature Services
        // featureServices should always be empty
        final TreeMap featureServices = mappingComponent.getMappingModel().getFeatureServices();
        final List positionsFeatures = new ArrayList(featureServices.keySet());
        Collections.sort(positionsFeatures);

        for (final Object position : positionsFeatures) {
            final Object featureService = featureServices.get(position);
            if (featureService instanceof RetrievalServiceLayer) {
                headlessMapProvider.addLayer((RetrievalServiceLayer)featureService);
            } else {
                LOG.warn(
                    "Feature can not be added to the headlessMapProvider as it is not an instance of RetrievalServiceLayer");
            }
        }

        // Features
        for (final Feature f : mappingComponent.getFeatureCollection().getAllFeatures()) {
            headlessMapProvider.addFeature(f);
        }
        return headlessMapProvider;
    }
    
    /**
     * Creates a HeadlessMapProvider and adds the raster layers and feature layers from the mapping component to it.
     *
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static HeadlessMapProvider createHeadlessMapProviderAndAddLayers(final MappingComponent mappingComponent) {
        final HeadlessMapProvider headlessMapProvider = new HeadlessMapProvider();
        ((ActiveLayerModel)headlessMapProvider.getMappingComponent().getMappingModel()).setSrs(
            mappingComponent.getMappingModel().getSrs());
        headlessMapProvider.getMappingComponent().setInfoNodesVisible(mappingComponent.isInfoNodesVisible());

        // Raster Services
        final TreeMap rasterServices = mappingComponent.getMappingModel().getRasterServices();
        final List positionsRaster = new ArrayList(rasterServices.keySet());
        Collections.sort(positionsRaster);

        for (final Object position : positionsRaster) {
            boolean addable = false;
            final Object rasterService = rasterServices.get(position);
            if ((rasterService instanceof RetrievalServiceLayer)
                        && ((RetrievalServiceLayer)rasterService).isEnabled()) {
                if ((rasterService instanceof AbstractWMSServiceLayer)
                            && ((AbstractWMSServiceLayer)rasterService).isVisible()) {
                    addable = true;
                } else if ((rasterService instanceof AbstractWMS) && ((AbstractWMS)rasterService).isVisible()) {
                    addable = true;
                } else if ((rasterService instanceof AbstractFeatureService)
                            && ((AbstractFeatureService)rasterService).isVisible()) {
                    addable = true;
                }
            }
            if (addable) {
                headlessMapProvider.addLayer((RetrievalServiceLayer)rasterService);
            } else {
                LOG.warn(
                    "Layer can not be added to the headlessMapProvider as it is not an instance of RetrievalServiceLayer");
            }
        }

        // Feature Services
        // featureServices should always be empty
        final TreeMap featureServices = mappingComponent.getMappingModel().getFeatureServices();
        final List positionsFeatures = new ArrayList(featureServices.keySet());
        Collections.sort(positionsFeatures);

        for (final Object position : positionsFeatures) {
            final Object featureService = featureServices.get(position);
            if (featureService instanceof RetrievalServiceLayer) {
                headlessMapProvider.addLayer((RetrievalServiceLayer)featureService);
            } else {
                LOG.warn(
                    "Feature can not be added to the headlessMapProvider as it is not an instance of RetrievalServiceLayer");
            }
        }

        // Features
        for (final Feature f : mappingComponent.getFeatureCollection().getAllFeatures()) {
            headlessMapProvider.addFeature(f);
        }
        return headlessMapProvider;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return map;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return map;
    }

    /**
     * Adds a PropertyChangeListener that will notify about the progress of the map building.
     *
     * @param  listener  the listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeListener.add(listener);
    }

    /**
     * Removes a PropertyChangeListener that will notify about the progress of the map building.
     *
     * @param  listener  the listener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeListener.remove(listener);
    }

    /**
     * add a feature to the HeadlessMapProvider.
     *
     * @param  f  the feature to add
     */
    public void addFeature(final Feature f) {
        map.getFeatureCollection().addFeature(f);
    }

    /**
     * add a collection of features to the HeadlessMapProvider.
     *
     * @param  featureCollection  the feature collection to add
     */
    public void addFeatures(final Collection<? extends Feature> featureCollection) {
        map.getFeatureCollection().addFeatures(featureCollection);
    }

    /**
     * Add a layer to the HeadlessMapProvider. A clone of the given layer is added to the model.
     *
     * @param  layer  the layer to add
     */
    public void addLayer(final RetrievalServiceLayer layer) {
        if (layer instanceof AbstractRetrievalService) {
            final AbstractRetrievalService l = ((AbstractRetrievalService)layer).cloneWithoutRetrievalListeners();
            mappingModel.addLayer((RetrievalServiceLayer)l);
        } else {
            mappingModel.addLayer(layer);
        }
    }

    /**
     * set the bounding box of the HeadlessMapProvider.
     *
     * @param  boundingBox  the new BoundingBox of the map
     */
    public void setBoundingBox(final XBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final Crs crs) {
        mappingModel.setSrs(crs);
    }

    /**
     * Don't know what the idea was behind this method ;-) maybe take the 72dpi from jasper and recalculate the pixels
     * then call the next method.
     *
     * @param   dpi                 DOCUMENT ME!
     * @param   widthInMillimeters  DOCUMENT ME!
     * @param   heightInMilimeters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image getImage(final int dpi, final double widthInMillimeters, final double heightInMilimeters) {
        return null;
    }
    
    /**
     * This is the method called when you need to fill in a report: for jasper with (72 as basedpi and widzth and height
     * with the dimension of the image in the report
     *
     * @param   basedpi         DOCUMENT ME!
     * @param   targetDpi       DOCUMENT ME!
     * @param   widthInPixels   DOCUMENT ME!
     * @param   heightInPixels  DOCUMENT ME!
     *
     * @return  A future object that can provide an image
     */
    public Future<Image> getImage(final int basedpi,
            final int targetDpi,
            final double widthInPixels,
            final double heightInPixels) {
        printingResolution = targetDpi / FEATURE_RESOLUTION_FACTOR;
        final int imageWidth = (int)((double)widthInPixels / (double)basedpi
                        * (double)targetDpi);
        final int imageHeight = (int)((double)heightInPixels / (double)basedpi
                        * (double)targetDpi);
        resolution = targetDpi;
        return getImage(imageWidth, imageHeight);
    }

    /**
     * this is the most important function. all other getImage functions will call this function
     *
     * @param   widthPixels   DOCUMENT ME!
     * @param   heightPixels  DOCUMENT ME!
     *
     * @return  A future object that can provide an image
     */
    public Future<Image> getImage(final int widthPixels, final int heightPixels) {
        if (boundingBox == null) {
            boundingBox = (XBoundingBox)mappingModel.getHomeBoundingBoxes().get(mappingModel.getDefaultHomeSrs());
            LOG.warn("No BoundingBox was set explicitly. Will use the Home-BoundingBox");
        }

        // Check Ratio
        int correctedWidthPixels = widthPixels;
        int correctedHeightPixels = heightPixels;
        XBoundingBox correctedBoundingBox = boundingBox;

        correctedBoundingBox = new XBoundingBox(boundingBox.getX1(),
                boundingBox.getY1(),
                boundingBox.getX2(),
                boundingBox.getY2(),
                boundingBox.getSrs(),
                boundingBox.isMetric());
        final double pixelRelation = ((double)widthPixels / (double)heightPixels);
        final double boundingBoxRelation = (boundingBox.getWidth() / boundingBox.getHeight());
        if (pixelRelation != boundingBoxRelation) {
            if (dominatingDimension == HeadlessMapProvider.DominatingDimension.SIZE) {
                // adjusting the  BoundingBox so that the old box would sit in the center of the new box
                if (boundingBoxRelation < pixelRelation) {
                    final double boundingBoxWidth = (widthPixels * boundingBox.getHeight()) / heightPixels;
                    if (centerMapOnResize) {
                        final double changedWidth = Math.abs(boundingBoxWidth - boundingBox.getWidth());
                        correctedBoundingBox.setX2(correctedBoundingBox.getX2() + (changedWidth / 2));
                        correctedBoundingBox.setX1(correctedBoundingBox.getX1() - (changedWidth / 2));
                    } else {
                        correctedBoundingBox.setX2(correctedBoundingBox.getX1() + boundingBoxWidth);
                    }
                } else {
                    final double boundingBoxHeight = (heightPixels * boundingBox.getWidth()) / widthPixels;
                    if (centerMapOnResize) {
                        final double changedHeight = Math.abs(boundingBoxHeight - boundingBox.getHeight());
                        correctedBoundingBox.setY2(correctedBoundingBox.getY2() + (changedHeight / 2));
                        correctedBoundingBox.setY1(correctedBoundingBox.getY1() - (changedHeight / 2));
                    } else {
                        correctedBoundingBox.setY2(correctedBoundingBox.getY1() + boundingBoxHeight);
                    }
                }
            } else if (dominatingDimension == HeadlessMapProvider.DominatingDimension.BOUNDINGBOX) {
                // adjusting width and height
                if (boundingBoxRelation < pixelRelation) {
                    correctedHeightPixels = (int)((widthPixels * boundingBox.getHeight()) / boundingBox.getWidth());
                } else {
                    correctedWidthPixels = (int)((heightPixels * boundingBox.getWidth()) / boundingBox.getHeight());
                }
            }
        }

        map.setSize(correctedWidthPixels, correctedHeightPixels);
        map.gotoBoundingBox(correctedBoundingBox, true, true, 0, false);
        // check the minimum map scale condition
        if ((minimumScaleDenominator > 0) && (map.getScaleDenominator() < minimumScaleDenominator)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("map scale of " + map.getScaleDenominator() + " is less than the configured minimum scale ( "
                            + minimumScaleDenominator + "). Using minimim scale");
            }
            final BoundingBox bb = map.getBoundingBoxFromScale(minimumScaleDenominator);
            correctedBoundingBox = new XBoundingBox(bb.getX1(),
                    bb.getY1(),
                    bb.getX2(),
                    bb.getY2(),
                    boundingBox.getSrs(),
                    boundingBox.isMetric());
            map.gotoBoundingBox(correctedBoundingBox, true, true, 0, false);
        }

        // check if we have to round up the scale to a certain precision
        if (roundScaleTo != HeadlessMapProvider.RoundingPrecision.NO_ROUNDING) {
            double scale = 0;
            if (roundScaleTo == HeadlessMapProvider.RoundingPrecision.TENTH) {
                scale = Math.round((map.getScaleDenominator() / 10) + 0.5d) * 10;
            } else if (roundScaleTo == HeadlessMapProvider.RoundingPrecision.HUNDRETH) {
                scale = Math.round((map.getScaleDenominator() / 100) + 0.5d) * 100;
            } else if (roundScaleTo == HeadlessMapProvider.RoundingPrecision.THOUSANDTH) {
                scale = Math.round((map.getScaleDenominator() / 1000) + 0.5d) * 1000;
            }
            final BoundingBox bb = map.getBoundingBoxFromScale(scale);
            correctedBoundingBox = new XBoundingBox(bb.getX1(),
                    bb.getY1(),
                    bb.getX2(),
                    bb.getY2(),
                    boundingBox.getSrs(),
                    boundingBox.isMetric());
            map.gotoBoundingBox(correctedBoundingBox, true, true, 0, false);
        }

        imgScaleDenominator = map.getScaleDenominator();
        map.setFixedBoundingBox(correctedBoundingBox);

        if (printingResolution == 0) {
            map.setPrintingResolution(resolution / getFeatureResolutionFactor());
        } else {
            map.setPrintingResolution(printingResolution);
        }

        final HeadlessMapProvider.HeadlessMapProviderRetrievalListener listener =
            new HeadlessMapProvider.HeadlessMapProviderRetrievalListener(
                correctedWidthPixels,
                correctedHeightPixels,
                propertyChangeListener,
                mappingModel.getFeatureServices().size()
                        + mappingModel.getMapServices().size());
        map.addRepaintListener(listener);

        map.unlockWithoutReload();
        if (mappingModel.getMapServices().size() > 0) {
            map.queryServices();
        } else {
            listener.createImageFromFeatures();
        }

        // @Thorsten: siehe Kommentar zu prepareFeaturesBeforePaint()
        if (false) {
            map.setPrintingResolution(printingResolution);
            prepareFeaturesBeforePaint();
        }

        return listener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   widthPixels   DOCUMENT ME!
     * @param   heightPixels  DOCUMENT ME!
     *
     * @return  An image of the map
     *
     * @throws  ExecutionException    DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    public Image getImageAndWait(final int widthPixels, final int heightPixels) throws ExecutionException,
        InterruptedException {
        return getImage(widthPixels, heightPixels).get();
    }

    /**
     * This is the method called when you need to fill in a report: for jasper with (72 as basedpi and widzth and height
     * with the dimension of the image in the report.
     *
     * @param   basedpi         DOCUMENT ME!
     * @param   targetDpi       DOCUMENT ME!
     * @param   widthInPixels   DOCUMENT ME!
     * @param   heightInPixels  DOCUMENT ME!
     *
     * @return  An image of the map
     *
     * @throws  ExecutionException    DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    public Image getImageAndWait(final int basedpi,
            final int targetDpi,
            final double widthInPixels,
            final double heightInPixels) throws ExecutionException, InterruptedException {
        return getImage(basedpi, targetDpi, widthInPixels, heightInPixels).get();
    }

    /**
     * DOCUMENT ME!
     *
     * @Thorsten:  Die Idee von dieser Methode ist es dass die Features von der MappingComponent entfernt und nochmals
     *             draufgezeichnet werden, damit sie somit ihre Transparenz verlieren. Die Methode basiert auf
     *             MappingComponent.getImageOfFeatures(). Im Augenblick kann man sie in getImage() aktivieren oder
     *             deaktivieren.
     */
    private void prepareFeaturesBeforePaint() {
        final Collection<Feature> fc = map.getFeatureCollection().getAllFeatures();
        final List<PFeature> list = new ArrayList<PFeature>();

        try {
            for (final Feature f : fc) {
                final PFeature p = new PFeature(f, map);
                list.add(p);
            }
            final double scale = 1 / map.getCamera().getViewScale();

            map.getFeatureCollection().removeAllFeatures();

            // TODO Sorge dafür dass die PSwingKomponente richtig gedruckt wird und dass die Karte nicht mehr "zittert"

            int printingLineWidth = 10;
            for (final PFeature original : list) {
                original.setInfoNodeExpanded(false);

                if (printingLineWidth > 0) {
                    ((StyledFeature)original.getFeature()).setLineWidth(printingLineWidth);
                } else if (StyledFeature.class.isAssignableFrom(original.getFeature().getClass())) {
                    final int orginalLineWidth = ((StyledFeature)original.getFeature()).getLineWidth();
                    printingLineWidth = (int)Math.round(orginalLineWidth * (printingResolution * 2));
                    ((StyledFeature)original.getFeature()).setLineWidth(printingLineWidth);
                }

                final PFeature copy = new PFeature(original.getFeature(),
                        map.getWtst(),
                        0,
                        0,
                        map,
                        true);
                map.getLayer().addChild(copy);

                copy.setTransparency(original.getTransparency());
                copy.setStrokePaint(original.getStrokePaint());
                final boolean expanded = original.isInfoNodeExpanded();
                copy.addInfoNode();
                copy.setInfoNodeExpanded(false);
                copy.refreshInfoNode();

                original.refreshInfoNode();

                map.removeStickyNode(copy.getStickyChild());

                final PNode stickyChild = copy.getStickyChild();
                if (stickyChild != null) {
                    stickyChild.setScale(scale * printingResolution);
                    if (copy.hasSecondStickyChild()) {
                        copy.getSecondStickyChild().setScale(scale * printingResolution);
                    }
                }
            }
        } catch (final Exception exception) {
            LOG.error("Error during the creation of an image from features", exception); // NOI18N
        }
    }

    /**
     * The dominating dimension determines, what value is changed, when the image pixel ratio is adjusted to the
     * coordinate ratio.
     *
     * @return  the dominatingDimension
     */
    public HeadlessMapProvider.DominatingDimension getDominatingDimension() {
        return dominatingDimension;
    }

    /**
     * Set the dominating dimension. The dominating dimension determines, what value is changed, when the image pixel
     * ratio will be adjusted to the coordinate ratio
     *
     * @param  dominatingDimension  the dominatingDimension to set
     */
    public void setDominatingDimension(final HeadlessMapProvider.DominatingDimension dominatingDimension) {
        this.dominatingDimension = dominatingDimension;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  minimumScaleDenominator  DOCUMENT ME!
     */
    public void setMinimumScaleDenomimator(final double minimumScaleDenominator) {
        this.minimumScaleDenominator = minimumScaleDenominator;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  b  DOCUMENT ME!
     */
    public void setCenterMapOnResize(final boolean b) {
        centerMapOnResize = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rp  DOCUMENT ME!
     */
    public void setRoundScaleTo(final HeadlessMapProvider.RoundingPrecision rp) {
        this.roundScaleTo = rp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getImageScaleDenominator() {
        return imgScaleDenominator;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getCurrentBoundingBoxFromMap() {
        return map.getCurrentBoundingBoxFromCamera();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * This class uses the RetrievalLisener interface to build the map image and the Future interface to provide the map
     * image.
     *
     * @version  $Revision$, $Date$
     */
    class HeadlessMapProviderRetrievalListener implements Future<Image>, RepaintListener {

        //~ Instance fields ----------------------------------------------------

        int imageWidth;
        int imageHeight;
        private HashSet<RetrievalService> services;
        private HashSet<Object> results;
        private HashSet<Object> erroneous;
        private boolean cancel = false;
        private volatile boolean done = false;
        private final ReentrantLock lock = new ReentrantLock();
        private Condition condition = lock.newCondition();
        private List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();
        private int serviceCount = 0;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new HeadlessMapProviderRetrievalListener object.
         *
         * @param  imageWidth    the width of the image
         * @param  imageHeight   the height of the image
         * @param  listener      DOCUMENT ME!
         * @param  serviceCount  DOCUMENT ME!
         */
        public HeadlessMapProviderRetrievalListener(final int imageWidth,
                final int imageHeight,
                final List<PropertyChangeListener> listener,
                final int serviceCount) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            services = new HashSet<RetrievalService>();
            results = new HashSet<Object>();
            erroneous = new HashSet<Object>();
            this.listener = listener;
            this.serviceCount = serviceCount;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public synchronized void repaintStart(final RepaintEvent repaintEvent) {
            final RetrievalEvent e = repaintEvent.getRetrievalEvent();
            if (LOG.isDebugEnabled()) {
                LOG.debug("retrievalStarted" + e.getRetrievalService()); // NOI18N
            }

            if (e.isInitialisationEvent()) {
                LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalStarted ignored, initialisation event"); // NOI18N
                return;
            }

            sendNotification(org.openide.util.NbBundle.getMessage(
                    PrintingWidget.class,
                    "PrintingWidget.retrievalStarted(RetrievalEvent).msg",
                    new Object[] { e.getRetrievalService() }),
                HeadlessMapProvider.NotificationLevel.INFO); // NOI18N

            if (e.getRetrievalService() == null) {
                System.out.println("service is null");
            }

            if (e.getRetrievalService() instanceof ServiceLayer) {
                services.add(e.getRetrievalService());
            }
        }

        @Override
        public void repaintError(final RepaintEvent repaintEvent) {
            final RetrievalEvent e = repaintEvent.getRetrievalEvent();
            LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrievalError"); // NOI18N

            if (e.isInitialisationEvent()) {
                LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalError ignored, initialisation event"); // NOI18N
                return;
            }

            sendNotification(org.openide.util.NbBundle.getMessage(
                    PrintingWidget.class,
                    "PrintingWidget.retrievalError(RetrievalEvent).msg1",
                    new Object[] { e.getRetrievalService() }),
                HeadlessMapProvider.NotificationLevel.ERROR); // NOI18N
            sendNotification(org.openide.util.NbBundle.getMessage(
                    PrintingWidget.class,
                    "PrintingWidget.retrievalError(RetrievalEvent).msg2"),
                HeadlessMapProvider.NotificationLevel.ERROR_REASON,
                e);                                           // NOI18N
            repaintComplete(repaintEvent);
        }

        @Override
        public synchronized void repaintComplete(final RepaintEvent repaintEvent) {
            final RetrievalEvent e = repaintEvent.getRetrievalEvent();

            if (LOG.isInfoEnabled()) {
                LOG.info(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrievalComplete"); // NOI18N
            }
            if (e.isInitialisationEvent()) {
                LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalComplete ignored, initialisation event");                         // NOI18N
                return;
            }

            if (e.getRetrievalService() instanceof ServiceLayer) {
                if (!e.isHasErrors()) {
                    results.add(e.getRetrievalService());
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg",
                            new Object[] { e.getRetrievalService() }),
                        HeadlessMapProvider.NotificationLevel.SUCCESS); // NOI18N
                } else {
                    erroneous.add(e.getRetrievalService());
                    if (e.getRetrievedObject() instanceof Image) {
                        sendNotification(org.openide.util.NbBundle.getMessage(
                                PrintingWidget.class,
                                "PrintingWidget.retrievalComplete(RetrievalEvent).msg2",
                                new Object[] { e.getRetrievalService() }),
                            HeadlessMapProvider.NotificationLevel.ERROR_REASON,
                            e);                                         // NOI18N
                    }
                }
            }

            if ((results.size() + erroneous.size()) == serviceCount) {
                if (results.size() == serviceCount) {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg6"),
                        HeadlessMapProvider.NotificationLevel.SUCCESS); // NOI18N
                } else if (erroneous.size() == services.size()) {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg7"),
                        HeadlessMapProvider.NotificationLevel.WARN);    // NOI18N
                } else {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg8"),
                        HeadlessMapProvider.NotificationLevel.WARN);    // NOI18N
                }

                addFeaturesToTopLevelLayer();

                if (erroneous.size() < results.size()) {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg4"),
                        HeadlessMapProvider.NotificationLevel.SUCCESS); // NOI18N
                } else {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg5"),
                        HeadlessMapProvider.NotificationLevel.INFO);    // NOI18N
                }

                LOG.info("Following layers were painted: " + results); // NOI18N
                if (LOG.isDebugEnabled()) {
                    LOG.debug("services:" + services);                 // NOI18N
                }

                unlock();
            }
        }

        /**
         * Creates an image of the features. This is required if the map does only contain features and no layer.
         */
        public void createImageFromFeatures() {
            addFeaturesToTopLevelLayer();
            unlock();
        }

        /**
         * Adds the features to the map image.
         */
        private void addFeaturesToTopLevelLayer() {
            // Add Existing Features as TopLevelLayer
            if (map.isFeatureCollectionVisible()) {
                try {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg3"),
                        HeadlessMapProvider.NotificationLevel.INFO);                                        // NOI18N
                } catch (Throwable t) {
                    LOG.error("Error while adding local features to the map", t);                           // NOI18N
                }
            } else {
                final String localFeaturesNotAddedMessage = org.openide.util.NbBundle.getMessage(
                        PrintingWidget.class,
                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg9");
                sendNotification(localFeaturesNotAddedMessage, HeadlessMapProvider.NotificationLevel.INFO); // NOI18N
                if (LOG.isDebugEnabled()) {
                    LOG.debug(localFeaturesNotAddedMessage);
                }
            }
        }

        /**
         * Unlocks this object. This means that the map was created and the {@link get()} and
         * {@link get(long, TimeUnit)} methods can provide a result.
         */
        private void unlock() {
            lock.lock();
            try {
                done = true;
                condition.signalAll();
            } finally {
                lock.unlock();
                sendNotification("", NotificationLevel.UNLOCKED);
            }
        }

        /**
         * Send a notification to all registered listeners.
         *
         * @param  msg    the message to send
         * @param  level  the notification level
         */
        private void sendNotification(final String msg, final HeadlessMapProvider.NotificationLevel level) {
            sendNotification(msg, level, null);
        }

        /**
         * Send a notification to all registered listeners.
         *
         * @param  msg    the message to send
         * @param  level  the notification level
         * @param  e      a RetrievalEvent
         */
        private void sendNotification(final String msg,
                final HeadlessMapProvider.NotificationLevel level,
                final RetrievalEvent e) {
            final PropertyChangeEvent evt = new PropertyChangeEvent(
                    this,
                    "notification",
                    e,
                    new HeadlessMapProvider.NotificationMessage(msg, level));

            for (final PropertyChangeListener tmpListener : listener) {
                tmpListener.propertyChange(evt);
            }
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            this.cancel = true;

            return !done;
        }

        @Override
        public boolean isCancelled() {
            return cancel;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public Image get() throws InterruptedException, ExecutionException {
            lock.lock();
            if (!isDone()) {
                condition.await();
            }

            try {
                return map.getImage();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Image get(final long timeout, final TimeUnit unit) throws InterruptedException,
            ExecutionException,
            TimeoutException {
            lock.lock();
            try {
                if (!isDone()) {
                    if (!condition.await(timeout, unit)) {
                        throw new TimeoutException();
                    }
                }

                return map.getImage();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class NotificationMessage {

        //~ Instance fields ----------------------------------------------------

        private String msg;
        private HeadlessMapProvider.NotificationLevel level;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NotificationMessage object.
         */
        public NotificationMessage() {
        }

        /**
         * Creates a new NotificationMessage object.
         *
         * @param  msg    DOCUMENT ME!
         * @param  level  DOCUMENT ME!
         */
        public NotificationMessage(final String msg, final HeadlessMapProvider.NotificationLevel level) {
            this.msg = msg;
            this.level = level;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the msg
         */
        public String getMsg() {
            return msg;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  msg  the msg to set
         */
        public void setMsg(final String msg) {
            this.msg = msg;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the level
         */
        public HeadlessMapProvider.NotificationLevel getLevel() {
            return level;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level  the level to set
         */
        public void setLevel(final HeadlessMapProvider.NotificationLevel level) {
            this.level = level;
        }
    }
}
