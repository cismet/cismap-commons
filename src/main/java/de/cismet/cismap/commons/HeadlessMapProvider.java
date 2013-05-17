/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.printing.PrintingWidget;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.retrieval.RetrievalService;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Collection;
import java.util.TreeMap;

/**
 *
 * @author thorsten
 */
public class HeadlessMapProvider {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(HeadlessMapProvider.class);
    
    enum DominatingDimension { SIZE, BOUNDINGBOX};
    
    DominatingDimension dominatingDiomension=DominatingDimension.SIZE;
    
    MappingComponent map = new MappingComponent(false);
    ActiveLayerModel mappingModel = new ActiveLayerModel();
    XBoundingBox boundingBox=null;
    public HeadlessMapProvider() {
        map.setInternalLayerWidgetAvailable(false);
        mappingModel.setSrs(new Crs("EPSG:25832", "", "", true, true));
        mappingModel.addHome(new XBoundingBox(374271.251964098, 5681514.032498134, 374682.9413952776, 5681773.852810634, "EPSG:25832", true));

// set the model
        map.setMappingModel(mappingModel);
        //map.set Size(500, 500);

// initial positioning of the map
        map.setAnimationDuration(0);
        map.gotoInitialBoundingBox();
        map.unlock();
       
    }
    
   //please add here: addLayer,addfeature, setBoundingBox, zoom to FeatureCollection etc.
    
    
    //Don't know what the idea was behind this method ;-)
    //maybe take the 72dpi from jasper and recalculate the pixels then call the next method
    public Image getImage(int dpi, double widthInMillimeters, double heightInMilimeters){
        return null;
    }
    
    
    
    //This is the method called when you need to fill in a report: for jasper with (72 as basedpi and widzth and height with the dimension of the image in the report
    public Image getImage(int basedpi, int targetDpi, double widthInPixels, double heightInPixels){
        int imageWidth = (int)((double)widthInPixels / (double)basedpi
                        * (double)targetDpi);
        int imageHeight = (int)((double)heightInPixels / (double)basedpi
                        * (double)targetDpi);
        return getImage(imageWidth, imageHeight);
        
    }
    
    
    //this is the most important function. all other getImage functions will call this function
    public <Future>Image getImage(final int widthPixels,final int heightPixels){
        if (boundingBox==null){
            boundingBox=(XBoundingBox)mappingModel.getHomeBoundingBoxes().get(mappingModel.getDefaultHomeSrs());
            LOG.warn("No BoundingBox was set explicitly. Will use the Home-BoundingBox");
        }
        //Check Ratio - TODO
        //if dominatingDimension=SIZE adjusting the  BoundingBox (default)
        //if  dominatingDimension=BoundingBOx adjusting width and height
        
        map.queryServicesIndependentFromMap(widthPixels, heightPixels, boundingBox, new HeadlessMapProviderRetrievalListener(widthPixels,heightPixels)); //evtl angepasst
        
        
        
        
        return null;
        
    }


class HeadlessMapProviderRetrievalListener implements RetrievalListener {
    int imageWidth;
    int imageHeight;

        public HeadlessMapProviderRetrievalListener(int imageWidth, int imageHeight) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }
        
    
    private TreeMap<Integer, RetrievalService> services;
    private TreeMap<Integer, Object> results;
    private TreeMap<Integer, Object> erroneous;
    
    
    final static boolean DEBUG=true;
    @Override
    public void retrievalStarted(final RetrievalEvent e) {
        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("retrievalStarted" + e.getRetrievalService()); // NOI18N
            }
        }

        if (e.isInitialisationEvent()) {
            LOG.error(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                        + "]: retrievalStarted ignored, initialisation event"); // NOI18N
            return;
        }

//        addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                PrintingWidget.class,
//                "PrintingWidget.retrievalStarted(RetrievalEvent).msg",
//                new Object[] { e.getRetrievalService() }),
//            INFO); // NOI18N
        if (e.getRetrievalService() instanceof ServiceLayer) {
            final int num = ((ServiceLayer)e.getRetrievalService()).getLayerPosition();
            services.put(num, e.getRetrievalService());
        }
    }

    @Override
    public void retrievalProgress(final RetrievalEvent e) {
        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrieval progress: "
                            + e.getPercentageDone()); // NOI18N
            }
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

//        addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                PrintingWidget.class,
//                "PrintingWidget.retrievalError(RetrievalEvent).msg1",
//                new Object[] { e.getRetrievalService() }),
//            ERROR);        // NOI18N
//        addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                PrintingWidget.class,
//                "PrintingWidget.retrievalError(RetrievalEvent).msg2"),
//            ERROR_REASON); // NOI18N
        retrievalComplete(e);
    }

    @Override
    public void retrievalComplete(final RetrievalEvent e) {
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
//                addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                        PrintingWidget.class,
//                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg",
//                        new Object[] { e.getRetrievalService() }),
//                    SUCCESS); // NOI18N
            } else {
                erroneous.put(num, e);
                if (e.getRetrievedObject() instanceof Image) {
//                    final Image i = Static2DTools.removeUnusedBorder((Image)e.getRetrievedObject(), 5, 0.7);
//                    addIconToProgressPane(errorImage, i);
//                    addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                            PrintingWidget.class,
//                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg2",
//                            new Object[] { e.getRetrievalService() }),
//                        ERROR_REASON); // NOI18N
                }
            }
        }

        if ((results.size() + erroneous.size()) == services.size()) {
            if (results.size() == services.size()) {
//                addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                        PrintingWidget.class,
//                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg6"),
//                    SUCCESS); // NOI18N
            } else if (erroneous.size() == services.size()) {
//                addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                        PrintingWidget.class,
//                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg7"),
//                    WARN);    // NOI18N
            } else {
//                addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                        PrintingWidget.class,
//                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg8"),
//                    WARN);    // NOI18N
            }

            for (final Integer i : results.keySet()) {
                final Graphics2D g2d = (Graphics2D)map.getGraphics();
                // Transparency
                final RetrievalService rs = services.get(i);

                float transparency = 0f;
                if (rs instanceof ServiceLayer) {
                    transparency = ((ServiceLayer)rs).getTranslucency();
                }
                final Composite alphaComp = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        transparency);
                g2d.setComposite(alphaComp);
                final Object o = results.get(i);

                LOG.info("processing results of type '" + o.getClass().getSimpleName() + "' from service #" + i + " '"
                            + rs + "' (" + rs.getClass().getSimpleName() + ")");                       // NOI18N
                if (o instanceof Image) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("service '" + rs + "' returned an image, must be a raster service"); // NOI18N
                    }
                    final Image image2add = (Image)o;
                    g2d.drawImage(image2add, 0, 0, null);
                } else if (Collection.class.isAssignableFrom(o.getClass())) {
                    final Collection featureCollection = (Collection)o;
                    if (DEBUG) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("service '" + rs + "' returned a collection, must be a feature service ("
                                        + featureCollection.size() + " features retrieved)");          // NOI18N
                        }
                    }
                    final Image image2add = map.getImageOfFeatures(
                            featureCollection,
                            imageWidth,
                            imageHeight);
                    g2d.drawImage(image2add, 0, 0, null);
                } else {
                    LOG.error("unknown results retrieved: " + o.getClass().getSimpleName());           // NOI18N
                }
            }

            // Add Existing Features as TopLevelLayer
            if (map.isFeatureCollectionVisible()) {
                try {
                    final Graphics2D g2d = (Graphics2D)map.getGraphics();
//                    addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                            PrintingWidget.class,
//                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg3"),
//                        INFO); // NOI18N

                    // Transparency
                    float transparency = 0f;
                    transparency = map.getFeatureLayer().getTransparency();
                    final Composite alphaComp = AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            transparency);
                    g2d.setComposite(alphaComp);
                    
                    
                    final Image image2add = map.getFeatureImage(imageWidth, imageHeight);
                    g2d.drawImage(image2add, 0, 0, null);
                } catch (Throwable t) {
                    LOG.error("Error while adding local features to the map", t); // NOI18N
                }
            } else {
                final String localFeaturesNotAddedMessage = org.openide.util.NbBundle.getMessage(
                        PrintingWidget.class,
                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg9");
//                addMessageToProgressPane(localFeaturesNotAddedMessage, INFO);     // NOI18N
                if (LOG.isDebugEnabled()) {
                    LOG.debug(localFeaturesNotAddedMessage);
                }
            }

            if (erroneous.size() < results.size()) {
//                addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                        PrintingWidget.class,
//                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg4"),
//                    SUCCESS); // NOI18N
            } else {
//                addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
//                        PrintingWidget.class,
//                        "PrintingWidget.retrievalComplete(RetrievalEvent).msg5"),
//                    INFO);    // NOI18N
            }

            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ALLE FERTIG");          // NOI18N
                    LOG.debug("results:" + results);   // NOI18N
                    LOG.debug("services:" + services); // NOI18N
                }
            }

            
        }
    }

    @Override
    public void retrievalAborted(final RetrievalEvent e) {
        LOG.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier() + "]: retrievalAborted"); // NOI18N
    }
    
}

}