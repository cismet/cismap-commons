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

import org.jdom.Element;

import java.awt.EventQueue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CidsLayerFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CidsLayerFactory.class);
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CidsLayerFactory.class);
    private static boolean DEBUG = true;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   element       DOCUMENT ME!
     * @param   capabilities  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RetrievalServiceLayer createLayer(final Element element,
            final HashMap<String, WMSCapabilities> capabilities) {
        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("trying to create Layer '" + element.getName() + "'"); // NOI18N
            }
        }

        try {
            if (element.getName().equals("WMSServiceLayer")) {          // NOI18N
                final WMSServiceLayer wmsServiceLayer = new WMSServiceLayer(element, capabilities);
                try {
                    if (wmsServiceLayer.getWMSLayers().size() > 0) {
                        try {
                            LOG.info(
                                "createLayer WMSServiceLayer ("
                                        + wmsServiceLayer.getName()
                                        + ")");                         // NOI18N
                            return wmsServiceLayer;
                        } catch (IllegalArgumentException schonVorhanden) {
                            LOG.warn(
                                "Layer WMSServiceLayer '"
                                        + wmsServiceLayer.getName()
                                        + "' already existed. Do not add the Layer. \n"
                                        + schonVorhanden.getMessage()); // NOI18N
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error while initialising WMS", e);
                }
            } else if (element.getName().equals(WebFeatureService.WFS_FEATURELAYER_TYPE)) {
                final WebFeatureService wfs = new WebFeatureService(element);
                if (EventQueue.isDispatchThread()) {
                    LOG.fatal("InvokeLater in EDT");                    // NOI18N
                }

                try {
                    LOG.info(
                        "addLayer "
                                + WebFeatureService.WFS_FEATURELAYER_TYPE
                                + " ("
                                + wfs.getName()
                                + ")");                                           // NOI18N
                    return wfs;
                } catch (IllegalArgumentException schonVorhanden) {
                    LOG.warn(
                        "Layer "
                                + WebFeatureService.WFS_FEATURELAYER_TYPE
                                + " '"
                                + wfs.getName()
                                + "' already existed. Do not add the Layer. \n"
                                + schonVorhanden.getMessage());                   // NOI18N
                }
            } else if (element.getName().equals("DocumentFeatureServiceLayer")) { // NOI18N
                LOG.error("DocumentFeatureServiceLayer not supported");           // NOI18N
                // throw new UnsupportedOperationException("DocumentFeatureServiceLayer not supported");
                // if(DEBUG)log.debug("DocumentFeatureLayer von ConfigFile wird hinzugefügt"); URI documentURI =
                // new URI(element.getChildText("documentURI").trim()); File testFile = new File(documentURI); if
                // (!testFile.exists()) { log.warn("Das Angebene Document(" + testFile.getAbsolutePath() + ")
                // exisitiert nicht ---> abbruch, es wird kein Layer angelegt"); continue; }
                //
                // final GMLFeatureService gfs = new GMLFeatureService(element); //langsam sollte nicht im EDT
                // ausgeführt werden final DocumentFeatureService dfs =
                // DocumentFeatureServiceFactory.createDocumentFeatureService(element); //final
                // ShapeFileFeatureService sfs = new ShapeFileFeatureService(element); EventQueue.invokeLater(new
                // Runnable() {
                //
                // @Override public void run() { try { log.info("addLayer DocumentFeatureServiceLayer (" +
                // dfs.getName() + ")"); addLayer(dfs); } catch (IllegalArgumentException schonVorhanden) {
                // log.warn("Layer DocumentFeatureServiceLayer '" + dfs.getName() + "' already existed. Do not
                // add the Layer. \n" + schonVorhanden.getMessage()); } } });
            } else if (element.getName().equals("simpleWms")) {                                   // NOI18N
                final SimpleWMS simpleWMS = new SimpleWMS(element);
                LOG.info("addLayer SimpleWMS (" + simpleWMS.getName() + ")");                     // NOI18N
                try {
                    return simpleWMS;
                } catch (IllegalArgumentException schonVorhanden) {
                    LOG.warn(
                        "Layer SimpleWMS '"
                                + simpleWMS.getName()
                                + "' already existed. Do not add the Layer. \n"
                                + schonVorhanden.getMessage());                                   // NOI18N
                }
            } else if (element.getName().equals(SlidableWMSServiceLayerGroup.XML_ELEMENT_NAME)) { // NOI18N
                final SlidableWMSServiceLayerGroup wms = new SlidableWMSServiceLayerGroup(element, capabilities);
                LOG.info("addLayer SlidableWMSServiceLayerGroup (" + wms.getName() + ")");        // NOI18N
                try {
                    return wms;
                } catch (IllegalArgumentException schonVorhanden) {
                    LOG.warn(
                        "Layer SimpleWMS '"
                                + wms.getName()
                                + "' already existed. Do not add the Layer. \n"
                                + schonVorhanden.getMessage());                                   // NOI18N
                }
            } else if (element.getName().equals("simplePostgisFeatureService")) {                 // NOI18N
                SimplePostgisFeatureService spfs;
                if ((element.getAttributeValue("updateable") != null)
                            && element.getAttributeValue("updateable").equals("true")) {          // NOI18N
                    spfs = new SimpleUpdateablePostgisFeatureService(element);
                } else {
                    spfs = new SimplePostgisFeatureService(element);
                }

                final SimplePostgisFeatureService simplePostgisFeatureService = spfs;
                try {
                    LOG.info(
                        "addLayer SimplePostgisFeatureService ("
                                + simplePostgisFeatureService.getName()
                                + ")");                         // NOI18N
                    return simplePostgisFeatureService;
                } catch (IllegalArgumentException schonVorhanden) {
                    LOG.warn(
                        "Layer SimplePostgisFeatureService '"
                                + simplePostgisFeatureService.getName()
                                + "' already existed. Do not add the Layer. \n"
                                + schonVorhanden.getMessage()); // NOI18N
                }
            } else if (element.getName().equals("ModeLayer")) {
                final ModeLayer modeLayer = new ModeLayer();
                final String selectedMode = element.getAttributeValue("mode");
                final String modeLayerKey = element.getAttributeValue("key");
                modeLayer.setLayerKey(modeLayerKey);
                final Iterator modeIt = element.getChildren("Mode").iterator();
                String first = null;
                while (modeIt.hasNext()) {
                    final Element mode = (Element)modeIt.next();
                    final String key = mode.getAttributeValue("key");
                    if (first == null) {
                        first = key;
                    }
                    final Element layerDef = (Element)mode.getChildren().get(0);
                    final RetrievalServiceLayer layer = createLayer(layerDef, capabilities);
                    modeLayer.putModeLayer(key, layer);
                }
                if (selectedMode == null) {
                    modeLayer.setMode(first);
                } else {
                    modeLayer.setMode(selectedMode);
                }
                ModeLayerRegistry.getInstance().putModeLayer(modeLayerKey, modeLayer);
                return modeLayer;
            } else {
                try {
                    if (DEBUG) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("restoring generic layer configuration from xml element '" + element.getName()
                                        + "'");                 // NOI18N
                        }
                    }
                    final RetrievalServiceLayer layer = (RetrievalServiceLayer)XMLObjectFactory
                                .restoreObjectfromElement(element);

                    try {
                        LOG.info("addLayer generic layer configuration (" + layer.getName() + ")"); // NOI18N
                        return layer;
                    } catch (IllegalArgumentException schonVorhanden) {
                        LOG.warn(
                            "Layer SimplePostgisFeatureService '"
                                    + layer.getName()
                                    + "' already existed. Do not add the Layer. \n"
                                    + schonVorhanden.getMessage());                                 // NOI18N
                    }
                } catch (Throwable t) {
                    LOG.error("unsupported xml configuration, layer '" + element.getName()
                                + "' could not be created: \n" + t.getLocalizedMessage(),
                        t);                                                                         // NOI18N
                }
            }
        } catch (Throwable t) {
            LOG.error("Layer layer '" + element.getName() + "' could not be created: \n" + t.getMessage(), t); // NOI18N
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layerelement  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getKeyforLayerElement(final Element layerelement) {
        final String keyString = null;
        if (layerelement != null) {
            try {
                if (layerelement.getName().equals("WMSServiceLayer")) {                                            // NOI18N
                    final WMSServiceLayer wmsServiceLayer = new WMSServiceLayer(
                            layerelement,
                            new HashMap<String, WMSCapabilities>());
                    return getKeyForRetrievalService(wmsServiceLayer);
                } else if (layerelement.getName().equals(WebFeatureService.WFS_FEATURELAYER_TYPE)) {
                    final WebFeatureService wfs = new WebFeatureService(layerelement);
                    return getKeyForRetrievalService(wfs);
                } else if (layerelement.getName().equals("DocumentFeatureServiceLayer")) {                         // NOI18N
                    log.warn("Sollte nicht vorkommen. Die sollten alle von der XMLObjectFactory geladen werden."); // NOI18N
                } else if (layerelement.getName().equals("simpleWms")) {                                           // NOI18N
                    final SimpleWMS simpleWMS = new SimpleWMS(layerelement);
                    return getKeyForRetrievalService(simpleWMS);
                } else if (layerelement.getName().equals("simplePostgisFeatureService")) {                         // NOI18N
                    SimplePostgisFeatureService spfs;
                    if ((layerelement.getAttributeValue("updateable") != null)
                                && layerelement.getAttributeValue("updateable").equals("true")) {                  // NOI18N
                        spfs = new SimpleUpdateablePostgisFeatureService(layerelement);
                    } else {
                        spfs = new SimplePostgisFeatureService(layerelement);
                    }
                    return getKeyForRetrievalService(spfs);
                } else if (layerelement.getName().equals(SlidableWMSServiceLayerGroup.XML_ELEMENT_NAME)) {         // NOI18N
                    final SlidableWMSServiceLayerGroup slidableWms = new SlidableWMSServiceLayerGroup(
                            layerelement,
                            new HashMap<String, WMSCapabilities>());

                    // the listener and the internal widget should be removed by the slidable wms object
                    final ActiveLayerEvent event = new ActiveLayerEvent();
                    event.setLayer(slidableWms);
                    slidableWms.layerRemoved(event);

                    return getKeyForRetrievalService(slidableWms);
                } else if (layerelement.getName().equals("ModeLayer")) {
                    return "ModeLayer#" + layerelement.getAttributeValue("key");
                } else {
                    final RetrievalServiceLayer layer = (RetrievalServiceLayer)XMLObjectFactory
                                .restoreObjectfromElement(layerelement);
                    return getKeyForRetrievalService(layer);
                }
            } catch (Exception ex) {
                log.error((("Konnte keinen Key für das layerelement " + layerelement.getName()) != null)
                        ? layerelement.getName() : ("null" + "erstellen"),
                    ex);
            }
        }
        return null;
    }

    /**
     * Same as above if this is done directly by the retrievalservicelayer no instanceof is needed.
     *
     * @param   layer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String getKeyForRetrievalService(final RetrievalServiceLayer layer) {
        if (layer != null) {
            try {
                if (layer instanceof WMSServiceLayer) {                     // NOI18N
                    final WMSServiceLayer wmsServiceLayer = (WMSServiceLayer)layer;
                    return wmsServiceLayer.getName() + "#" + wmsServiceLayer.getCapabilitiesUrl();
                } else if (layer instanceof WebFeatureService) {
                    final WebFeatureService wfs = (WebFeatureService)layer;
                    return wfs.getName() + "#" + wfs.getHostname();
                } else if (layer instanceof DocumentFeatureService) {       // NOI18N
                    final DocumentFeatureService dfs = (DocumentFeatureService)layer;
                    return dfs.getName() + dfs.getDocumentURI();
                } else if (layer instanceof SimpleWMS) {                    // NOI18N
                    final SimpleWMS simpleWMS = (SimpleWMS)layer;
                    return simpleWMS.getName() + "#" + simpleWMS.getGmUrl().getUrlTemplate();
                } else if (layer instanceof SimplePostgisFeatureService) {  // NOI18N
                    final SimplePostgisFeatureService spfs = (SimplePostgisFeatureService)layer;
                    return spfs.getName() + "#" + spfs.getConnectionInfo().getUrl();
                } else if (layer instanceof SlidableWMSServiceLayerGroup) { // NOI18N
                    final SlidableWMSServiceLayerGroup wms = (SlidableWMSServiceLayerGroup)layer;
                    return wms.getName() + "#" + wms.getName();
                } else {
                    final RetrievalServiceLayer rsl = (RetrievalServiceLayer)layer;
                    return rsl.getName() + "#" + rsl.getClass();
                }
            } catch (Exception ex) {
                log.error("Konnte keinen Key für das layerelement erstellen", ex);
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer                      DOCUMENT ME!
     * @param  preferredBGColor           DOCUMENT ME!
     * @param  preferredExceptionsFormat  DOCUMENT ME!
     * @param  preferredRasterFormat      DOCUMENT ME!
     * @param  srs                        DOCUMENT ME!
     */
    public static void wmsSpecificConfiguration(final RetrievalServiceLayer layer,
            final String preferredBGColor,
            final String preferredExceptionsFormat,
            final String preferredRasterFormat,
            final Crs srs) {
        if (layer instanceof WMSServiceLayer) {
            final WMSServiceLayer wmsLayer = ((WMSServiceLayer)layer);
            if (wmsLayer.getBackgroundColor() == null) {
                wmsLayer.setBackgroundColor(preferredBGColor);
            }
            if (wmsLayer.getExceptionsFormat() == null) {
                wmsLayer.setExceptionsFormat(preferredExceptionsFormat);
            }
            if (wmsLayer.getImageFormat() == null) {
                wmsLayer.setImageFormat(preferredRasterFormat);
            }
            wmsLayer.setSrs(srs.getCode());
        } else if (layer instanceof SlidableWMSServiceLayerGroup) {
            ((SlidableWMSServiceLayerGroup)layer).setSrs(srs.getCode());
        } else if (layer instanceof ModeLayer) {
            final ModeLayer ml = (ModeLayer)layer;
            final Set<String> modes = ml.getModes();
            for (final String mode : modes) {
                wmsSpecificConfiguration(ml.getModeLayer(mode),
                    preferredBGColor,
                    preferredExceptionsFormat,
                    preferredRasterFormat,
                    srs);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs    DOCUMENT ME!
     * @param  layer  DOCUMENT ME!
     */
    public static void setLayerToCrs(final Crs crs, final Object layer) {
        if (layer instanceof WMSServiceLayer) {
            ((WMSServiceLayer)layer).setSrs(crs.getCode());
        } else if (layer instanceof SlidableWMSServiceLayerGroup) {
            ((SlidableWMSServiceLayerGroup)layer).setSrs(crs.getCode());
        } else if (layer instanceof WebFeatureService) {
            ((WebFeatureService)layer).setCrs(crs);
        } else if (layer instanceof ShapeFileFeatureService) {
            ((ShapeFileFeatureService)layer).setCrs(crs);
        } else if (layer instanceof ModeLayer) {
            ((ModeLayer)layer).setCrs(crs);
        } else {
            log.error("The SRS of a layer cannot be changed. Layer is of type  " + layer.getClass().getName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Element getElement(final MapService layer) {
        if (layer instanceof WMSServiceLayer) {
            return ((WMSServiceLayer)layer).getElement();
        } else if (layer instanceof SimpleWMS) {
            return ((SimpleWMS)layer).getElement();
        } else if (layer instanceof WebFeatureService) {
            return ((WebFeatureService)layer).toElement();
        } else if (layer instanceof DocumentFeatureService) {
            return ((DocumentFeatureService)layer).toElement();
        } else if (layer instanceof SimplePostgisFeatureService) {
            return ((SimplePostgisFeatureService)layer).toElement();
        } else if (layer instanceof SimpleUpdateablePostgisFeatureService) {
            return ((SimpleUpdateablePostgisFeatureService)layer).toElement();
        } else if (layer instanceof SlidableWMSServiceLayerGroup) {
            return ((SlidableWMSServiceLayerGroup)layer).toElement();
        } else if (layer instanceof ModeLayer) {
            return ((ModeLayer)layer).toElement();
        } else if (layer instanceof ConvertableToXML) {
            return ((ConvertableToXML)layer).toElement();
        } else {
            log.warn("saving configuration not supported by service: " + layer); // NOI18N
            return null;
        }
    }
}
