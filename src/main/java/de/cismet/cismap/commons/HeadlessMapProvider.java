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

import org.apache.commons.beanutils.ConversionException;

import org.jdom.DataConversionException;

import org.openide.util.Exceptions;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JComponent;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener;
import de.cismet.cismap.commons.gui.printing.PrintingWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
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

        TIP, INFO, SUCCESS, EXPERT, WARN, ERROR, ERROR_REASON
    }

    //~ Instance fields --------------------------------------------------------

    MappingComponent map = new MappingComponent(false);
    ActiveLayerModel mappingModel = new ActiveLayerModel();
    XBoundingBox boundingBox = null;
    private List<PropertyChangeListener> propertyChangeListener = new ArrayList<PropertyChangeListener>();
    private DominatingDimension dominatingDimension = DominatingDimension.SIZE;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HeadlessMapProvider object.
     */
    public HeadlessMapProvider() {
        map.setInternalLayerWidgetAvailable(false);

        if (CismapBroker.getInstance().getMappingComponent() != null) {
            // adopt the mapping configuration from the main MappingComponent
            // Set the default Crs
            final List<Crs> crsList = CismapBroker.getInstance().getMappingComponent().getCrsList();
            final String defaultCrs = CismapBroker.getInstance().getDefaultCrs();
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

            mappingModel.addHome(boundingBox);

            if (boundingBox == null) {
                LOG.error("Home boundingBox not found");
                mappingModel.addHome(new XBoundingBox(
                        374271.251964098,
                        5681514.032498134,
                        374682.9413952776,
                        5681773.852810634,
                        "EPSG:25832",
                        true));
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
        map.gotoInitialBoundingBox();
        map.unlock();
    }

    //~ Methods ----------------------------------------------------------------

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
     * add a layer to the HeadlessMapProvider.
     *
     * @param  layer  the layer to add
     */
    public void addLayer(final RetrievalServiceLayer layer) {
        mappingModel.addLayer(layer);
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
        final int imageWidth = (int)((double)widthInPixels / (double)basedpi
                        * (double)targetDpi);
        final int imageHeight = (int)((double)heightInPixels / (double)basedpi
                        * (double)targetDpi);
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
            if (dominatingDimension == DominatingDimension.SIZE) {
                // adjusting the  BoundingBox
                if (boundingBoxRelation < pixelRelation) {
                    final double boundingBoxWidth = (widthPixels * boundingBox.getHeight()) / heightPixels;
                    correctedBoundingBox.setX2(correctedBoundingBox.getX1() + boundingBoxWidth);
                } else {
                    final double boundingBoxHeight = (heightPixels * boundingBox.getWidth()) / widthPixels;
                    correctedBoundingBox.setY2(correctedBoundingBox.getY1() + boundingBoxHeight);
                }
            } else if (dominatingDimension == DominatingDimension.BOUNDINGBOX) {
                // adjusting width and height
                if (boundingBoxRelation < pixelRelation) {
                    correctedHeightPixels = (int)((widthPixels * boundingBox.getHeight()) / boundingBox.getWidth());
                } else {
                    correctedWidthPixels = (int)((heightPixels * boundingBox.getWidth()) / boundingBox.getHeight());
                }
            }
        }

        map.setSize(correctedWidthPixels, correctedHeightPixels);
        map.gotoBoundingBox(correctedBoundingBox, false, false, 0, false);
        map.setFixedBoundingBox(correctedBoundingBox);

        final HeadlessMapProviderRetrievalListener listener = new HeadlessMapProviderRetrievalListener(
                correctedWidthPixels,
                correctedHeightPixels,
                propertyChangeListener,
                mappingModel.getFeatureServices().size()
                        + mappingModel.getMapServices().size());

        if (mappingModel.getMapServices().size() > 0) {
            map.queryServicesIndependentFromMap(
                correctedWidthPixels,
                correctedHeightPixels,
                correctedBoundingBox,
                listener); // evtl angepasst
        } else {
            listener.createImageFromFeatures();
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
     * The dominating dimension determines, what value is changed, when the image pixel ratio is adjusted to the
     * coordinate ratio.
     *
     * @return  the dominatingDimension
     */
    public DominatingDimension getDominatingDimension() {
        return dominatingDimension;
    }

    /**
     * Set the dominating dimension. The dominating dimension determines, what value is changed, when the image pixel
     * ratio will be adjusted to the coordinate ratio
     *
     * @param  dominatingDimension  the dominatingDimension to set
     */
    public void setDominatingDimension(final DominatingDimension dominatingDimension) {
        this.dominatingDimension = dominatingDimension;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * This class uses the RetrievalLisener interface to build the map image and the Future interface to provide the map
     * image.
     *
     * @version  $Revision$, $Date$
     */
    class HeadlessMapProviderRetrievalListener implements Future<Image>, RetrievalListener {

        //~ Instance fields ----------------------------------------------------

        int imageWidth;
        int imageHeight;

        private TreeMap<Integer, RetrievalService> services;
        private TreeMap<Integer, Object> results;
        private TreeMap<Integer, Object> erroneous;
        private boolean cancel = false;
        private volatile boolean done = false;
        private Image image;
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
            services = new TreeMap<Integer, RetrievalService>();
            results = new TreeMap<Integer, Object>();
            erroneous = new TreeMap<Integer, Object>();
            this.listener = listener;
            this.serviceCount = serviceCount;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public synchronized void retrievalStarted(final RetrievalEvent e) {
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
                NotificationLevel.INFO); // NOI18N

            if (e.getRetrievalService() == null) {
                System.out.println("service is null");
            }

            if (e.getRetrievalService() instanceof ServiceLayer) {
                final int num = ((ServiceLayer)e.getRetrievalService()).getLayerPosition();
                services.put(num, e.getRetrievalService());
            }
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrieval progress: "
                            + e.getPercentageDone()); // NOI18N
            }

            if (e.isInitialisationEvent()) {
                LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalProgress ignored, initialisation event"); // NOI18N
                return;
            }
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
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
                NotificationLevel.ERROR);        // NOI18N
            sendNotification(org.openide.util.NbBundle.getMessage(
                    PrintingWidget.class,
                    "PrintingWidget.retrievalError(RetrievalEvent).msg2"),
                NotificationLevel.ERROR_REASON); // NOI18N
            retrievalComplete(e);
        }

        @Override
        public synchronized void retrievalComplete(final RetrievalEvent e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrievalComplete"); // NOI18N
            }
            if (e.isInitialisationEvent()) {
                LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalComplete ignored, initialisation event");                         // NOI18N
                return;
            }

            if (e.getRetrievalService() instanceof ServiceLayer) {
                final int num = ((ServiceLayer)e.getRetrievalService()).getLayerPosition();
                if (!e.isHasErrors()) {
                    results.put(num, e.getRetrievedObject());
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg",
                            new Object[] { e.getRetrievalService() }),
                        NotificationLevel.SUCCESS);          // NOI18N
                } else {
                    erroneous.put(num, e);
                    if (e.getRetrievedObject() instanceof Image) {
                        sendNotification(org.openide.util.NbBundle.getMessage(
                                PrintingWidget.class,
                                "PrintingWidget.retrievalComplete(RetrievalEvent).msg2",
                                new Object[] { e.getRetrievalService() }),
                            NotificationLevel.ERROR_REASON); // NOI18N
                    }
                }
            }

            if ((results.size() + erroneous.size()) == serviceCount) {
                if (results.size() == serviceCount) {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg6"),
                        NotificationLevel.SUCCESS); // NOI18N
                } else if (erroneous.size() == services.size()) {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg7"),
                        NotificationLevel.WARN);    // NOI18N
                } else {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg8"),
                        NotificationLevel.WARN);    // NOI18N
                }

                for (final Integer i : results.keySet()) {
                    // Transparency
                    final RetrievalService rs = services.get(i);

                    float transparency = 0f;
                    if (rs instanceof ServiceLayer) {
                        transparency = ((ServiceLayer)rs).getTranslucency();
                    }
                    final Composite alphaComp = AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            transparency);
                    final Object o = results.get(i);

                    LOG.info("processing results of type '" + ((o != null) ? o.getClass().getSimpleName() : " null ")
                                + "' from service #" + i
                                + " '"
                                + rs + "' (" + ((rs != null) ? rs.getClass().getSimpleName() : null) + ")"); // NOI18N
                    if (o instanceof Image) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("service '" + rs + "' returned an image, must be a raster service");   // NOI18N
                        }
                        final Image image2add = (Image)o;
                        addImage(image2add, alphaComp);
                    } else if (Collection.class.isAssignableFrom(o.getClass())) {
                        final Collection featureCollection = (Collection)o;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("service '" + rs + "' returned a collection, must be a feature service ("
                                        + featureCollection.size() + " features retrieved)");                // NOI18N
                        }
                        final Image image2add = map.getImageOfFeatures(
                                featureCollection,
                                imageWidth,
                                imageHeight);
                        addImage(image2add, null);
                    } else {
                        LOG.error("unknown results retrieved: " + o.getClass().getSimpleName());             // NOI18N
                    }
                }

                addFeaturesToTopLevelLayer();

                if (erroneous.size() < results.size()) {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg4"),
                        NotificationLevel.SUCCESS); // NOI18N
                } else {
                    sendNotification(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg5"),
                        NotificationLevel.INFO);    // NOI18N
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("ALLE FERTIG");          // NOI18N
                    LOG.debug("results:" + results);   // NOI18N
                    LOG.debug("services:" + services); // NOI18N
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
                        NotificationLevel.INFO); // NOI18N

                    // Transparency
                    float transparency = 0f;
                    transparency = map.getFeatureLayer().getTransparency();
                    final Composite alphaComp = AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            transparency);
                    final Image image2add = map.getFeatureImage(imageWidth, imageHeight);
                    addImage(image2add, alphaComp);
                } catch (Throwable t) {
                    LOG.error("Error while adding local features to the map", t); // NOI18N
                }
            } else {
                final String localFeaturesNotAddedMessage = org.openide.util.NbBundle.getMessage(
                        PrintingWidget.class,
                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg9");
                sendNotification(localFeaturesNotAddedMessage, NotificationLevel.INFO); // NOI18N
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
            }
        }

        /**
         * Send a notification to all registered listeners.
         *
         * @param  msg    the message to send
         * @param  level  the notification level
         */
        private void sendNotification(final String msg, final NotificationLevel level) {
            final PropertyChangeEvent evt = new PropertyChangeEvent(
                    this,
                    "notification",
                    null,
                    new NotificationMessage(msg, level));

            for (final PropertyChangeListener tmpListener : listener) {
                tmpListener.propertyChange(evt);
            }

//            System.out.println(msg);
        }

        /**
         * Adds an image (map component) to the map image.
         *
         * @param  img        the overlay image to add
         * @param  composite  the composite object to be used for rendering
         */
        private synchronized void addImage(final Image img, final Composite composite) {
            image = mergeImages(image, img, composite);
        }

        /**
         * adds an image to an other image.
         *
         * @param   image      the base image
         * @param   overlay    the overlay image to add
         * @param   composite  the composite object to be used for rendering
         *
         * @return  DOCUMENT ME!
         */
        private BufferedImage mergeImages(final Image image, final Image overlay, final Composite composite) {
            final int w = Math.max(((image == null) ? 0 : image.getWidth(null)), overlay.getWidth(null));
            final int h = Math.max(((image == null) ? 0 : image.getHeight(null)), overlay.getHeight(null));
            final BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = (Graphics2D)combined.getGraphics();

            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }

            if (composite != null) {
                g.setComposite(composite);
            }
            g.drawImage(overlay, 0, 0, null);

            return combined;
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            LOG.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrievalAborted"); // NOI18N
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
                return image;
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

                return image;
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
        private NotificationLevel level;

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
        public NotificationMessage(final String msg, final NotificationLevel level) {
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
        public NotificationLevel getLevel() {
            return level;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  level  the level to set
         */
        public void setLevel(final NotificationLevel level) {
            this.level = level;
        }
    }
}