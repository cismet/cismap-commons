/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.EventQueue;

import java.beans.PropertyChangeSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.ChildrenProvider;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Operation;
import de.cismet.cismap.commons.wms.capabilities.Style;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilitiesFactory;

import de.cismet.tools.PropertyEqualsProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public final class WMSServiceLayer extends AbstractWMSServiceLayer implements RetrievalServiceLayer,
    RasterMapService,
    PropertyEqualsProvider,
    LayerInfoProvider,
    ChildrenProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(WMSServiceLayer.class);

    private static final String TEMPLATETOKEN_WIDTH = "<cismap:width>";                       // NOI18N
    private static final String TEMPLATETOKEN_HEIGHT = "<cismap:height>";                     // NOI18N
    private static final String TEMPLATETOKEN_BOUNDINGBOX_LL_X = "<cismap:boundingBox_ll_x>"; // NOI18N
    private static final String TEMPLATETOKEN_BOUNDINGBOX_LL_Y = "<cismap:boundingBox_ll_y>"; // NOI18N
    private static final String TEMPLATETOKEN_BOUNDINGBOX_UR_X = "<cismap:boundingBox_ur_x>"; // NOI18N
    private static final String TEMPLATETOKEN_BOUNDINGBOX_UR_Y = "<cismap:boundingBox_ur_y>"; // NOI18N
    private static final String TEMPLATETOKEN_SRS = "<cismap:srs>";                           // NOI18N
    private static final String TEMPLATETOKEN_CUSTOMSTYLE = "<cismap:customStyle>";           // NOI18N
    public static final String TEMPLATETOKEN_CUSTOMSTYLE_LAYERNAME = "<cismap:layerName>";    // NOI18N
    public static final String TEMPLATETOKEN_CUSTOMSTYLE_TITLE = "<cismap:title>";            // NOI18N

    private static final String EPSG_NAMESPACE = "http://www.opengis.net/gml/srs/epsg.xml"; // NOI18N

    private static final String TEMPLATE_GETMAP_PAYLOAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<GetMap xmlns:ows=\"http://www.opengis.net/ows\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1.1\" service=\"WMS\">"
                + TEMPLATETOKEN_CUSTOMSTYLE
                + "<BoundingBox srsName=\"" + EPSG_NAMESPACE + "#" + TEMPLATETOKEN_SRS + "\">"
                + "<gml:coord>"
                + "<gml:X>" + TEMPLATETOKEN_BOUNDINGBOX_LL_X + "</gml:X>"
                + "<gml:Y>" + TEMPLATETOKEN_BOUNDINGBOX_LL_Y + "</gml:Y>"
                + "</gml:coord>"
                + "<gml:coord>"
                + "<gml:X>" + TEMPLATETOKEN_BOUNDINGBOX_UR_X + "</gml:X>"
                + "<gml:Y>" + TEMPLATETOKEN_BOUNDINGBOX_UR_Y + "</gml:Y>"
                + "</gml:coord>"
                + "</BoundingBox>"
                + "<Output>"
                + "<Format>image/png</Format>"
                + "<Size>"
                + "<Width>" + TEMPLATETOKEN_WIDTH + "</Width>"
                + "<Height>" + TEMPLATETOKEN_HEIGHT + "</Height>"
                + "</Size>"
                + "<Transparent>true</Transparent>"
                + "</Output>"
                + "<Exceptions>application/vnd.ogc.se+xml</Exceptions>"
                + "</GetMap>";

    //~ Instance fields --------------------------------------------------------

    List wmsLayers = new ArrayList();
    List ogcLayers = new ArrayList();
    Object retrievalBlocker = new Object();
    private PNode imageObject = new PNode();
    private String imageFormat = null;
    private String backgroundColor = null;
    private String exceptionsFormat = null;
    private boolean transparentImage = true;
    private String srs;
    private String title;
    private WMSCapabilities wmsCapabilities;
    private String capabilitiesUrl;
    private String customSLD;
    // Used by clone()
    private List treePaths;
    private Element wmsServiceLayerElement;
    private HashMap<String, WMSCapabilities> capabilities;
    private List<WMSLayer> dummyLayer = new ArrayList<WMSLayer>();
    private boolean reverseAxisOrder = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WMSServiceLayer object.
     */
    public WMSServiceLayer() {
    }

    /**
     * Creates a new WMSServiceLayer object.
     *
     * @param  treePaths  DOCUMENT ME!
     */
    public WMSServiceLayer(final List treePaths) {
        this(treePaths, true, false);
    }

    /**
     * Creates a new WMSServiceLayer object.
     *
     * @param  l  The layer from which to create a WMSServiceLayer.
     */
    public WMSServiceLayer(final Layer l) {
        setTitle(l.getTitle());
        setName(l.getName());
        addLayer(l);
    }

    /**
     * Creates a new WMSServiceLayer object.
     *
     * @param  wmsServiceLayerElement  DOCUMENT ME!
     * @param  capabilities            DOCUMENT ME!
     */
    public WMSServiceLayer(final Element wmsServiceLayerElement, final HashMap<String, WMSCapabilities> capabilities) {
        this.wmsServiceLayerElement = wmsServiceLayerElement;
        this.capabilities = capabilities;

        // a dummy object without a capabilities document will be created
        init(wmsServiceLayerElement, capabilities, false);
    }

    /**
     * Creates a new WMSServiceLayer object.
     *
     * @param  treePaths             DOCUMENT ME!
     * @param  reverseLayerOrder     DOCUMENT ME!
     * @param  reverseSubLayerOrder  DOCUMENT ME!
     */
    public WMSServiceLayer(final List treePaths, final boolean reverseLayerOrder, final boolean reverseSubLayerOrder) {
        this.treePaths = treePaths;
        if (treePaths != null) {
            if (treePaths.size() > 1) {
                setName("Layerzusammenstellung"); // NOI18N
            }

            int i = (reverseLayerOrder ? (treePaths.size() - 1) : 0);

            while ((reverseLayerOrder ? (i >= 0) : (i < treePaths.size()))) {
                final Object next = treePaths.get(i);

                if (next instanceof TreePath) {
                    final TreePath nextTreePath = (TreePath)next;
                    if (nextTreePath.getLastPathComponent() instanceof Layer) {
                        final Layer nextLayer = (Layer)nextTreePath.getLastPathComponent();
                        addLayer(nextLayer, null, true, false, true, reverseSubLayerOrder);
                        if (getName() == null) {
                            setName(nextLayer.getTitle());
                        }
                    } else if (nextTreePath.getLastPathComponent() instanceof Style) {
                        final Style nextStyle = (Style)nextTreePath.getLastPathComponent();
                        if (nextTreePath.getPathComponent(nextTreePath.getPathCount() - 2) instanceof Layer) {
                            final Layer nextLayer = ((Layer)nextTreePath.getPathComponent(
                                        nextTreePath.getPathCount()
                                                - 2));
                            addLayer(nextLayer, nextStyle);
                            if (getName() == null) {
                                setName(nextLayer.getTitle());
                            }
                        }
                    }
                }

                if (reverseLayerOrder) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Sets the tile of this layer. TODO: Move to upper class
     *
     * @param  title  The title.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Returns the title of this layer.
     *
     * @return  The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsServiceLayerElement  DOCUMENT ME!
     * @param  capabilities            DOCUMENT ME!
     * @param  loadCapDoc              refCaps DOCUMENT ME!
     */
    private void init(final Element wmsServiceLayerElement,
            final HashMap<String, WMSCapabilities> capabilities,
            final boolean loadCapDoc) {
        setName(wmsServiceLayerElement.getAttribute("name").getValue()); // NOI18N

        final Attribute attributeTitle = wmsServiceLayerElement.getAttribute("title"); // NOI18N
        if (attributeTitle != null) {
            setTitle(attributeTitle.getValue());
        }

        final Attribute attributeReverseAxisOrder = wmsServiceLayerElement.getAttribute("reverseAxisOrder"); // NOI18N
        if (attributeReverseAxisOrder != null) {
            try {
                this.reverseAxisOrder = attributeReverseAxisOrder.getBooleanValue();
            } catch (DataConversionException e) {
                LOG.error("Cannot parse the reverse axis order", e);
            }
        }

        try {
            setVisible(wmsServiceLayerElement.getAttribute("visible").getBooleanValue()); // NOI18N
        } catch (DataConversionException ex) {
        }

        try {
            setTranslucency(wmsServiceLayerElement.getAttribute("translucency").getFloatValue()); // NOI18N
        } catch (DataConversionException ex) {
        }

        try {
            final Float minOpacity = CismapBroker.getInstance().getMinOpacityToStayEnabled();

            if ((minOpacity != null) && ((getTranslucency() <= minOpacity) || !isVisible())) {
                this.setEnabled(false);                                                         // NOI18N
            } else {
                setEnabled(wmsServiceLayerElement.getAttribute("enabled").getBooleanValue());   // NOI18N
            }
        } catch (DataConversionException ex) {
        }
        setBackgroundColor(wmsServiceLayerElement.getAttribute("bgColor").getValue());          // NOI18N
        setImageFormat(wmsServiceLayerElement.getAttribute("imageFormat").getValue());          // NOI18N
        setExceptionsFormat(wmsServiceLayerElement.getAttribute("exceptionFormat").getValue()); // NOI18N
        final CapabilityLink cp = new CapabilityLink(wmsServiceLayerElement);
        WMSCapabilities wmsCaps = capabilities.get(cp.getLink());
        if (loadCapDoc && (wmsCaps == null)) {
            try {
                wmsCaps = createCapabilitiesDocument();
                capabilities.put(cp.getLink(), wmsCaps);
            } catch (final Exception e) {
                errorObject = e.getMessage();
                LOG.error("Error while initialising a WMSServiceLayer object.", e);
            }
        }
        setWmsCapabilities(wmsCaps);
        setCapabilitiesUrl(cp.getLink());

        // Grundeinstellungen sind gemacht, Jetzt fehlen noch die Layer

        final List layerList = wmsServiceLayerElement.getChildren("wmsLayer"); // NOI18N
        final Iterator<Element> it = layerList.iterator();
        wmsLayers.clear();
        ogcLayers.clear();
        dummyLayer.clear();

        while (it.hasNext()) {
            final Element elem = it.next();
            final String lName = elem.getAttribute("name").getValue();      // NOI18N
            String styleName = null;
            boolean isEnabled = true;
            boolean info = false;
            try {
                isEnabled = elem.getAttribute("enabled").getBooleanValue(); // NOI18N
            } catch (Exception ex) {
            }
            try {
                info = elem.getAttribute("info").getBooleanValue();         // NOI18N
            } catch (Exception ex) {
            }
            try {
                styleName = elem.getAttribute("style").getValue();          // NOI18N
            } catch (Exception ex) {
            }
            if (wmsCaps != null) {
                final Layer l = searchForLayer(getWmsCapabilities().getLayer(), lName);

                if (l != null) {
                    if (layerList.size() == 1) {
                        // do not change custom name
                        if ((getName() == null) || getName().equals("")) {
                            setName(l.getTitle());
                        }
                    }

                    Style style = null;
                    if (styleName != null) {
                        style = l.getStyleResource(styleName);
                    }
                    this.addLayer(l, style, isEnabled, info, false);
                }
            } else {
                this.addLayer(lName, styleName, true, info);
            }
        }

        setEnabled(isEnabled());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nextLayer      DOCUMENT ME!
     * @param  selectedStyle  DOCUMENT ME!
     * @param  enabled        DOCUMENT ME!
     * @param  info           DOCUMENT ME!
     */
    protected void addLayer(final Layer nextLayer,
            final Style selectedStyle,
            final boolean enabled,
            final boolean info) {
        addLayer(nextLayer, selectedStyle, enabled, info, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nextLayer      DOCUMENT ME!
     * @param  selectedStyle  DOCUMENT ME!
     * @param  enabled        DOCUMENT ME!
     * @param  info           DOCUMENT ME!
     * @param  addSubLayer    DOCUMENT ME!
     */
    protected void addLayer(final Layer nextLayer,
            final Style selectedStyle,
            final boolean enabled,
            final boolean info,
            final boolean addSubLayer) {
        addLayer(nextLayer, selectedStyle, enabled, info, addSubLayer, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nextLayer             DOCUMENT ME!
     * @param  selectedStyle         DOCUMENT ME!
     * @param  enabled               DOCUMENT ME!
     * @param  info                  DOCUMENT ME!
     * @param  addSubLayer           DOCUMENT ME!
     * @param  reverseSubLayerOrder  DOCUMENT ME!
     */
    protected void addLayer(final Layer nextLayer,
            Style selectedStyle,
            final boolean enabled,
            final boolean info,
            final boolean addSubLayer,
            final boolean reverseSubLayerOrder) {
        if ((nextLayer.getName() != null) && !nextLayer.getName().equals("")) // NOI18N
        {
            if (selectedStyle == null) {
                if ((nextLayer.getStyles() != null) && (nextLayer.getStyles().length > 0)
                            && (nextLayer.getStyles()[0] != null)) {
                    selectedStyle = nextLayer.getStyles()[0];
                }
            }
            final WMSLayer wmsLayer = new WMSLayer(nextLayer, selectedStyle);
            wmsLayer.setEnabled(enabled);
            wmsLayer.setParentServiceLayer(this);
            wmsLayer.setQuerySelected(info);
            if (ogcLayers.indexOf(wmsLayer.getOgcCapabilitiesLayer()) < 0) {
                wmsLayers.add(wmsLayer);
                ogcLayers.add(wmsLayer.getOgcCapabilitiesLayer());
            }
            EventQueue.invokeLater(new Thread("fireLayerInformationStatusChanged") {

                    @Override
                    public void run() {
                        final ActiveLayerEvent ale = new ActiveLayerEvent();
                        ale.setLayer(wmsLayer);
                        CismapBroker.getInstance().fireLayerInformationStatusChanged(ale);
                    }
                });
        }

        if (addSubLayer) {
            if (reverseSubLayerOrder) {
                for (int i = nextLayer.getChildren().length - 1; i >= 0; --i) {
                    final Layer childLayer = nextLayer.getChildren()[i];
                    addLayer(childLayer, null, true, false, true, true);
                }
            } else {
                for (int i = 0; i < nextLayer.getChildren().length; ++i) {
                    final Layer childLayer = nextLayer.getChildren()[i];
                    addLayer(childLayer);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name       nextLayer DOCUMENT ME!
     * @param  styleName  DOCUMENT ME!
     * @param  enabled    DOCUMENT ME!
     * @param  info       DOCUMENT ME!
     */
    protected void addLayer(final String name, final String styleName, final boolean enabled, final boolean info) {
        final WMSLayer wmsLayer = new WMSLayer(name, styleName, enabled, info);
        wmsLayer.setEnabled(enabled);
        wmsLayer.setParentServiceLayer(this);
        wmsLayer.setQuerySelected(false);
        dummyLayer.add(wmsLayer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nextLayer      DOCUMENT ME!
     * @param  selectedStyle  DOCUMENT ME!
     */
    protected void addLayer(final Layer nextLayer, final Style selectedStyle) {
        addLayer(nextLayer, selectedStyle, true, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nextLayer  DOCUMENT ME!
     */
    protected void addLayer(final Layer nextLayer) {
        addLayer(nextLayer, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public void removeLayer(final WMSLayer layer) {
        wmsLayers.remove(layer);
        ogcLayers.remove(layer.getOgcCapabilitiesLayer());
        disableWhenChildrenDisabled();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        // if enabled is true and no sublayer is enabled, then enable all sublayer
        if (enabled) {
            boolean enableAll = true;
            Iterator it = getWMSLayers().iterator();

            while (it.hasNext()) {
                final Object o = it.next();

                if (o instanceof WMSLayer) {
                    if (((WMSLayer)o).isEnabled()) {
                        enableAll = false;
                        break;
                    }
                }
            }

            if (enableAll) {
                it = getWMSLayers().iterator();

                while (it.hasNext()) {
                    final Object o = it.next();

                    if (o instanceof WMSLayer) {
                        ((WMSLayer)o).setEnabled(enabled);
                    }
                }
            }
        }
    }

    /**
     * disables the layer, if all child layers are disabled.
     */
    public void disableWhenChildrenDisabled() {
        boolean childrenDisabled = true;
        final Iterator it = getWMSLayers().iterator();

        while (it.hasNext()) {
            final Object o = it.next();

            if (o instanceof WMSLayer) {
                if (((WMSLayer)o).isEnabled()) {
                    childrenDisabled = false;
                    break;
                }
            }
        }

        if (childrenDisabled) {
            setEnabled(false);
        }
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return "..."; // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List getWMSLayers() {
        if (isDummy()) {
            return dummyLayer;
        } else {
            return wmsLayers;
        }
    }

    @Override
    public void retrieve(final boolean forced) {
        if (isDummy()) {
            init(wmsServiceLayerElement, capabilities, true);

            if (!isDummy()) {
                setEnabled(true);
                dummyLayer.clear();

                final StatusEvent se = new StatusEvent(StatusEvent.AWAKED_FROM_DUMMY, this);
                CismapBroker.getInstance().fireStatusValueChanged(se);
            } else {
                retrievalError(new RetrievalEvent());
                return;
            }
        }

        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("retrieve()", new Exception()); // NOI18N
            }
        }

        setRefreshNeeded(false);
        final String getMapUrl = getGetMapUrl(customSLD != null);

        if (getMapUrl == null) {
            final RetrievalEvent e = new RetrievalEvent();
            e.setInitialisationEvent(true);
            e.setPercentageDone(0);
            e.setHasErrors(true);
            e.setRetrievedObject(NbBundle.getMessage(WMSServiceLayer.class, "WMSServiceLayer.retrieve.urlNotFound"));
            fireRetrievalError(e);
            return;
        }
        final String getMapPayload = getGetMapPayload();

        if ((ir != null) && ir.isAlive() && ir.getUrl().equals(getMapUrl) && !forced) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Multiple invocations with the same url. Skipping this invocation."); // NOI18N
            }
        } else if ((width < 1) || (height < 1)) {
            if (LOG.isDebugEnabled()) {
                // NoOp. Otherwise the wms will response with an exception.
                LOG.debug("Width or height is less than 1. Skipping retrieval.");
            }
        } else {
            if ((ir != null) && ir.isAlive()) {
                ir.youngerWMSCall();
                ir.interrupt();

                retrievalAborted(new RetrievalEvent());
            }

            ir = new ImageRetrieval(this);

            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getGetMapURL(): " + getMapUrl); // NOI18N
                }
            }

            ir.setUrl(getMapUrl);

            if (getMapPayload != null) {
                ir.setPayload(getMapPayload);
            }

            // new
            ir.setWMSCapabilities(getWmsCapabilities());

            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ir.start();"); // NOI18N
                }
            }

            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private WMSCapabilities createCapabilitiesDocument() throws Exception {
        final WMSCapabilitiesFactory factory = new WMSCapabilitiesFactory();
        final CapabilityLink link = new CapabilityLink(wmsServiceLayerElement);
        return factory.createCapabilities(link.getLink());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getImageFormat() {
        return imageFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  imageFormat  DOCUMENT ME!
     */
    public void setImageFormat(final String imageFormat) {
        this.imageFormat = imageFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBackgroundColor() {
        if (backgroundColor != null) {
            return backgroundColor;
        } else {
            LOG.warn("backgroundcolor was null. Set it to 0xF0F0F0"); // NOI18N
            backgroundColor = "0xF0F0F0";                             // NOI18N
            return backgroundColor;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  backgroundColor  DOCUMENT ME!
     */
    public void setBackgroundColor(final String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getExceptionsFormat() {
        return exceptionsFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  exceptionsFormat  DOCUMENT ME!
     */
    public void setExceptionsFormat(final String exceptionsFormat) {
        final List<String> exceptions = ((wmsCapabilities != null) ? wmsCapabilities.getExceptions() : null);

        if ((exceptionsFormat != null) && (exceptions != null) && (exceptions.size() > 0)
                    && !exceptions.contains(exceptionsFormat)) {
            // the preferred exception format is not supported. Use an other one
            String format = null;
            for (final String tmp : exceptions) {
                if (tmp.toLowerCase().indexOf(exceptionsFormat.toLowerCase()) != -1) {
                    format = tmp;
                    // The right format is found
                    break;
                } else if (tmp.toLowerCase().indexOf("xml") != -1) {
                    format = tmp;
                }
            }
            if (format == null) {
                format = exceptions.get(0);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Preferred exception format is not supported. Use format: " + format);
            }
            this.exceptionsFormat = format;
        } else {
            this.exceptionsFormat = exceptionsFormat;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSrs() {
        return srs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setSrs(final String srs) {
        this.srs = srs;
    }

    /**
     * Generates the URL for the GetMap request.
     *
     * @param   minimalUrl  Specifies if the generated url shall only contain the necessary parameters.
     *
     * @return  The GetMap request url.
     */
    private String getGetMapUrl(final boolean minimalUrl) {
        final String mapPrefix = getGetMapPrefix();

        if (mapPrefix == null) {
            return null;
        }

        final StringBuilder url = new StringBuilder(mapPrefix);

        if ((bb == null) || (url == null) || (url.length() == 0)) {
            return null;
        }

        if (url.indexOf("?") < 0) {
            url.append("?"); // NOI18N
        }

        final String version = getWmsCapabilities().getVersion();
        if (version.trim().equals("1.0.0") || version.trim().equals("1.0") || version.trim().equals("1")) {
            url.append("&WMTVER=1.0.0&REQUEST=map"); // NOI18N
        } else {
            url.append("&VERSION=").append(version);
            url.append("&REQUEST=GetMap");           // NOI18N
        }

        url.append("&BBOX=").append(bb.getURLString()); // NOI18N
        url.append("&WIDTH=").append(width);            // NOI18N
        url.append("&HEIGHT=").append(height);          // NOI18N

        if (minimalUrl) {
            return url.toString();
        }

        if (version.trim().equals("1.3") || version.trim().equals("1.3.0")) {
            url.append("&CRS="); // NOI18N
        } else {
            url.append("&SRS="); // NOI18N
        }
        url.append(srs);

        url.append("&FORMAT=").append(imageFormat);                                                     // NOI18N
        url.append("&TRANSPARENT=").append(Boolean.valueOf(transparentImage).toString().toUpperCase()); // NOI18N
        url.append("&BGCOLOR=").append(getBackgroundColor());                                           // NOI18N
        url.append("&EXCEPTIONS=").append(exceptionsFormat);                                            // NOI18N

        url.append(getLayersString());
        if (hasEveryLayerAStyle()) {
            // the styles parameter must contain the same number of values as the layers parameter.
            // If this requirement cannot be fulfilled, the style parameter should be sent without a value due
            // to generate a valid request.
            url.append(getStylesString());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "style parameter was added without a value to the getMap Request, because not every layer, " // NOI18N
                            + "which is used within the request has a selected style");                          // NOI18N
            }
            url.append("&STYLES=");
        }

        return url.toString();
    }

    /**
     * Generates the payload for the GetMap request. The payload will only be generated if there is a custom SLD, a SRS
     * and a bounding box set.
     *
     * @return  The payload for the GetMap request.
     */
    private String getGetMapPayload() {
        if ((customSLD == null) || (srs == null) || (bb == null)) {
            return null;
        }

        String result = TEMPLATE_GETMAP_PAYLOAD;

        result = result.replaceAll(
                TEMPLATETOKEN_CUSTOMSTYLE,
                customSLD.replaceAll(TEMPLATETOKEN_CUSTOMSTYLE_LAYERNAME, name));
        result = result.replaceAll(
                TEMPLATETOKEN_SRS,
                srs.startsWith("EPSG:") ? srs.substring(srs.indexOf(':') + 1) : srs);
        result = result.replaceAll(TEMPLATETOKEN_BOUNDINGBOX_LL_X, Double.toString(bb.getX1()));
        result = result.replaceAll(TEMPLATETOKEN_BOUNDINGBOX_LL_Y, Double.toString(bb.getY1()));
        result = result.replaceAll(TEMPLATETOKEN_BOUNDINGBOX_UR_X, Double.toString(bb.getX2()));
        result = result.replaceAll(TEMPLATETOKEN_BOUNDINGBOX_UR_Y, Double.toString(bb.getY2()));
        result = result.replaceAll(TEMPLATETOKEN_WIDTH, Integer.toString(width));
        result = result.replaceAll(TEMPLATETOKEN_HEIGHT, Integer.toString(height));

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   x  DOCUMENT ME!
     * @param   y  DOCUMENT ME!
     * @param   l  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGetFeatureInfoUrl(final int x, final int y, final WMSLayer l) {
        return getGetFeatureInfoUrl_internal(x, y, l, "text/html");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   x       DOCUMENT ME!
     * @param   y       DOCUMENT ME!
     * @param   l       DOCUMENT ME!
     * @param   format  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGetFeatureInfoUrl(final int x, final int y, final WMSLayer l, final String format) {
        return getGetFeatureInfoUrl_internal(x, y, l, format);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   x       DOCUMENT ME!
     * @param   y       DOCUMENT ME!
     * @param   l       DOCUMENT ME!
     * @param   format  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGetFeatureInfoUrl_internal(final int x, final int y, final WMSLayer l, final String format) {
        String url = getGetFeatureInfoPrefix();

        if ((bb != null) && (url != null)) {
            if (url.indexOf("?") < 0)                                                                         // NOI18N
            {
                url += "?";                                                                                   // NOI18N
            }
            final String version = getWmsCapabilities().getVersion();
            if (version.trim().equals("1.0.0") || version.trim().equals("1.0") || version.trim().equals("1")) // NOI18N
            {
                url += "&WMTVER=1.0.0&REQUEST=feature_info";                                                  // NOI18N
            } else {
                url += "&VERSION=" + version + "&REQUEST=GetFeatureInfo";                                     // NOI18N
            }
            url += "&BBOX=" + bb.getURLString();                                                              // NOI18N
            url += "&WIDTH=" + width;                                                                         // NOI18N
            url += "&HEIGHT=" + height;                                                                       // NOI18N
            if (version.trim().equals("1.3") || version.trim().equals("1.3.0")) {
                url += "&CRS=" + srs;                                                                         // NOI18N
            } else {
                url += "&SRS=" + srs;                                                                         // NOI18N
            }
            url += "&FORMAT=" + imageFormat;                                                                  // NOI18N
            url += "&TRANSPARENT=" + Boolean.valueOf(transparentImage).toString().toUpperCase();              // NOI18N
            url += "&BGCOLOR=" + backgroundColor;                                                             // NOI18N
            url += "&EXCEPTIONS=" + exceptionsFormat;                                                         // exceptionsFormat;
            url += "&FEATURE_COUNT=99";
            url += getLayersString();

            if (hasEveryLayerAStyle()) {
                // the styles parameter must have the same number of values as the layers parameter.
                // If this requirement cannot be fulfilled, the optional style parameter should be omitted due
                // to generate a valid request.
                url += getStylesString();
            }

            url += "&QUERY_LAYERS=" + l.getOgcCapabilitiesLayer().getName(); // NOI18N
            url += "&INFO_FORMAT=" + format;                                 // NOI18N
            if (version.trim().equals("1.3") || version.trim().equals("1.3.0")) {
                url += "&I=" + x;                                            // NOI18N
                url += "&J=" + y;                                            // NOI18N
            } else {
                url += "&X=" + x;                                            // NOI18N
                url += "&Y=" + y;                                            // NOI18N
            }
            return url;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGetMapPrefix() {
        try {
            String prefix = null;
            if ((getWmsCapabilities() != null) && (getWmsCapabilities().getRequest() != null)) {
                final Operation op = getWmsCapabilities().getRequest().getMapOperation();

                if (op != null) {
                    // ToDo UGLY WINNING WSS schneidet wenn es get und post gibt das geht.
                    if (op.getGet() != null) {
                        prefix = op.getGet().toString();
                    } else if (op.getPost() != null) {
                        prefix = op.getPost().toString();
                    } else {
                        return null;
                    }
                }
            }
            return prefix;
        } catch (Throwable npe) {
            LOG.warn("Throwable in getMapPrefix", npe); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGetFeatureInfoPrefix() {
        try {
            final Operation op = getWmsCapabilities().getRequest().getFeatureInfoOperation();
            String prefix = null;

            if (op != null) {
                prefix = op.getGet().toString();
            }
            return prefix;
        } catch (NullPointerException npe) {
            LOG.warn("NPE in getGetMapPrefix()", npe); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getLayersString() {
        final StringBuilder layerString = new StringBuilder("");                                                  // NOI18N
        int counter = 0;
        final Iterator it = getWMSLayers().iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if ((o instanceof WMSLayer) && ((WMSLayer)o).isEnabled()) {
                counter++;
                if (counter > 1) {
                    layerString.append(",");                                                                      // NOI18N
                }
                if (!isDummy()) {
                    layerString.append(((WMSLayer)o).getOgcCapabilitiesLayer().getName().replaceAll(" ", "%20")); // NOI18N
                } else {
                    layerString.append(((WMSLayer)o).toString().replaceAll(" ", "%20"));                          // NOI18N
                }
            }
        }
        if (counter > 0) {
            return "&LAYERS=" + layerString.toString();                                                           // NOI18N
        } else {
            return "";                                                                                            // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getStylesString() {
        final StringBuilder stylesString = new StringBuilder("");                                               // NOI18N
        int counter = 0;
        final Iterator it = getWMSLayers().iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if ((o instanceof WMSLayer) && ((WMSLayer)o).isEnabled()) {
                if ((!isDummy() && (((WMSLayer)o).getSelectedStyle() != null))
                            || (isDummy() && (((WMSLayer)o).getStyleName() != null))) {
                    counter++;
                    if (counter > 1) {
                        stylesString.append(",");                                                               // NOI18N
                    }
                    if (!isDummy()) {
                        stylesString.append(((WMSLayer)o).getSelectedStyle().getName().replaceAll(" ", "%20")); // NOI18N
                    } else {
                        stylesString.append(((WMSLayer)o).getStyleName().replaceAll(" ", "%20"));               // NOI18N
                    }
                }
            }
        }

        return "&STYLES=" + stylesString.toString(); // LDS Bugfix//NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  true, if every of the given layer has a selected style
     */
    private boolean hasEveryLayerAStyle() {
        final Iterator it = getWMSLayers().iterator();

        while (it.hasNext()) {
            final Object o = it.next();

            if ((o instanceof WMSLayer) && ((WMSLayer)o).isEnabled()) {
                if (((WMSLayer)o).getSelectedStyle() == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WMSCapabilities getWmsCapabilities() {
        return wmsCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsCapabilities  DOCUMENT ME!
     */
    public void setWmsCapabilities(final WMSCapabilities wmsCapabilities) {
        this.wmsCapabilities = wmsCapabilities;
    }

    @Override
    public void setPNode(final PNode imageObject) {
        final boolean vis = imageObject.getVisible();
        this.imageObject = imageObject;
        imageObject.setVisible(vis);
    }

    @Override
    public PNode getPNode() {
        return imageObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCapabilitiesUrl() {
        return capabilitiesUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilitiesUrl  DOCUMENT ME!
     */
    public void setCapabilitiesUrl(final String capabilitiesUrl) {
        this.capabilitiesUrl = capabilitiesUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  customSLD  DOCUMENT ME!
     */
    public void setCustomSLD(final String customSLD) {
        this.customSLD = customSLD;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        try {
            final Element layerConf = new Element("WMSServiceLayer");                                          // NOI18N
            layerConf.setAttribute("name", getName());                                                         // NOI18N
            layerConf.setAttribute("title", (title == null) ? "" : title);                                     // NOI18N
            layerConf.setAttribute("visible", Boolean.valueOf(getPNode().getVisible()).toString());            // NOI18N
            layerConf.setAttribute("enabled", Boolean.valueOf(isEnabled()).toString());                        // NOI18N
            layerConf.setAttribute("translucency", new Float(getTranslucency()).toString());                   // NOI18N
            layerConf.setAttribute("bgColor", getBackgroundColor());                                           // NOI18N
            layerConf.setAttribute("imageFormat", getImageFormat());                                           // NOI18N
            layerConf.setAttribute("exceptionFormat", getExceptionsFormat());                                  // NOI18N
            final CapabilityLink capLink = new CapabilityLink(
                    CapabilityLink.OGC,
                    getCapabilitiesUrl(),
                    reverseAxisOrder,
                    false);
            layerConf.addContent(capLink.getElement());
            final Iterator lit = getWMSLayers().iterator();
            while (lit.hasNext()) {
                final Object elem = lit.next();
                if (elem instanceof WMSLayer) {
                    final WMSLayer wmsLayer = (WMSLayer)elem;
                    final Element wmsLayerConf = new Element("wmsLayer");                                      // NOI18N
                    if (!isDummy()) {
                        wmsLayerConf.setAttribute("name", wmsLayer.getOgcCapabilitiesLayer().getName());       // NOI18N
                        wmsLayerConf.setAttribute("title", wmsLayer.getOgcCapabilitiesLayer().getTitle());     // NOI18N
                    } else {
                        wmsLayerConf.setAttribute("name", wmsLayer.toString());                                // NOI18N
                        wmsLayerConf.setAttribute("title", wmsLayer.toString());                               // NOI18N
                    }
                    wmsLayerConf.setAttribute("enabled", Boolean.valueOf(wmsLayer.isEnabled()).toString());    // NOI18N
                    try {
                        if (!isDummy()) {
                            wmsLayerConf.setAttribute("style", wmsLayer.getSelectedStyle().getName());         // NOI18N
                        } else {
                            wmsLayerConf.setAttribute("style", wmsLayer.getStyleName());                       // NOI18N
                        }
                    } catch (Exception e) {
                        // nothing to do. The layer has no style.
                        // So no style is saved and the default style is used after loading
                    }
                    wmsLayerConf.setAttribute("info", Boolean.valueOf(wmsLayer.isQuerySelected()).toString()); // NOI18N
                    layerConf.addContent(wmsLayerConf);
                }
            }
            return layerConf;
        } catch (Exception e) {
            LOG.error("Exception while saving layer", e);
            return new Element("WMSServiceLayer");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layer  DOCUMENT ME!
     * @param   name   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Layer searchForLayer(final Layer layer, final String name) {
        if ((layer.getName() != null) && layer.getName().equals(name)) {
            return layer;
        } else {
            final Layer[] lArr = layer.getChildren();
            for (int i = 0; i < lArr.length; i++) {
                final Layer l = searchForLayer(lArr[i], name);
                if (l != null) {
                    return l;
                }
            }
            return null;
        }
    }

    @Override
    public Object clone() {
        WMSServiceLayer w = null;
        if (treePaths != null) {
            w = new WMSServiceLayer(treePaths);
        } else {
            w = new WMSServiceLayer(wmsServiceLayerElement, capabilities);
        }
        w.bb = bb;
        w.capabilitiesUrl = capabilitiesUrl;
        w.enabled = enabled;
        w.errorObject = errorObject;
        w.exceptionsFormat = exceptionsFormat;
        w.height = height;
        w.imageFormat = imageFormat;
        w.visible = visible;
        // The cloned service layer and the origin service layer should not use the same pnode,
        // because this would lead to problems, if the cloned layer and the origin layer are
        // used in 2 different MappingComponents
        w.imageObject = null;
        w.ir = new ImageRetrieval(w);
        w.propertyChangeSupport = new PropertyChangeSupport(this);
        w.layerPosition = layerPosition;
        w.listeners = new ArrayList(listeners);
        w.name = name;
        w.ogcLayers = ogcLayers;
        w.progress = progress;
        w.refreshNeeded = refreshNeeded;
        w.srs = srs;
        w.translucency = translucency;
        w.transparentImage = transparentImage;
        w.treePaths = treePaths;
        w.width = width;
        w.wmsCapabilities = wmsCapabilities;
        w.wmsServiceLayerElement = wmsServiceLayerElement;
        w.capabilities = capabilities;
        w.dummyLayer = dummyLayer;
        w.reverseAxisOrder = reverseAxisOrder;
        final List<WMSLayer> layers = new ArrayList<WMSLayer>(wmsLayers.size());

        for (final Object layerObject : wmsLayers) {
            final WMSLayer layer = (WMSLayer)layerObject;
            final WMSLayer newLayer = new WMSLayer(layer.getOgcCapabilitiesLayer(), layer.getSelectedStyle());
            newLayer.setParentServiceLayer(w);
            layers.add(newLayer);
        }

        w.wmsLayers = layers;
        return w;
    }

    @Override
    public boolean propertyEquals(final Object obj) {
        // TODO Dieses equals wird 10000 mal aufgerufen
        if (obj instanceof WMSServiceLayer) {
            final WMSServiceLayer tester = (WMSServiceLayer)obj;
            if (getName().equals(tester.getName())
                        && ((getGetMapPrefix() == tester.getGetMapPrefix())
                            || ((getGetMapPrefix() != null) && getGetMapPrefix().equals(tester.getGetMapPrefix())))
                        && getLayersString().equals(tester.getLayersString())
                        && getStylesString().equals(tester.getStylesString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getLayerURI() {
        return getName();
    }

    @Override
    public String getServerURI() {
        return getCapabilitiesUrl();
    }

    @Override
    public Collection getChildren() {
        return getWMSLayers();
    }

    @Override
    public boolean isLayerQuerySelected() {
        return ((WMSLayer)getWMSLayers().get(0)).isQuerySelected();
    }

    @Override
    public void setLayerQuerySelected(final boolean selected) {
        ((WMSLayer)getWMSLayers().get(0)).setQuerySelected(selected);
    }

    @Override
    public boolean isQueryable() {
        return ((getWMSLayers().size() == 1) && ((WMSLayer)getWMSLayers().get(0)).isQueryable());
    }

    @Override
    public Layer getLayerInformation() {
        if (wmsCapabilities != null) {
            Layer layer = searchForLayer(wmsCapabilities.getLayer(), name);

            if (layer == null) {
                layer = wmsCapabilities.getLayer();
            }

            return layer;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isDummy() {
        return getWmsCapabilities() == null;
    }
}
