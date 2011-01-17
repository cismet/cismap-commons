/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;

import com.jhlabs.image.BlurFilter;

import edu.umd.cs.piccolo.PNode;

import org.jdom.DataConversionException;
import org.jdom.Element;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.ChildrenProvider;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.gui.piccolo.XPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Operation;
import de.cismet.cismap.commons.wms.capabilities.Style;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

import de.cismet.tools.PropertyEqualsProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WMSServiceLayer extends AbstractWMSServiceLayer implements RetrievalServiceLayer,
    RasterMapService,
    PropertyEqualsProvider,
    LayerInfoProvider,
    ChildrenProvider {

    //~ Instance fields --------------------------------------------------------

    // private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    Vector wmsLayers = new Vector();
    Vector ogcLayers = new Vector();
    Object retrievalBlocker = new Object();
    private PNode imageObject = new PNode();
    private String imageFormat = null;
    private String backgroundColor = null;
    private String exceptionsFormat = null;
    private boolean transparentImage = true;
    private String srs;
    private WMSCapabilities wmsCapabilities;
    private String capabilitiesUrl;
    // Used by clone()
    private Vector treePaths;
    private Element wmsServiceLayerElement;
    private HashMap<String, WMSCapabilities> capabilities;

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
    public WMSServiceLayer(final Vector treePaths) {
        this.treePaths = treePaths;
        if (treePaths != null) {
            final Iterator it = treePaths.iterator();
            if (treePaths.size() > 1) {
                setName("Layerzusammenstellung"); // NOI18N
            }

            while (it.hasNext()) {
                final Object next = it.next();
                if (next instanceof TreePath) {
                    final TreePath nextTreePath = (TreePath)next;
                    if (nextTreePath.getLastPathComponent() instanceof Layer) {
                        final Layer nextLayer = (Layer)nextTreePath.getLastPathComponent();
                        addLayer(nextLayer);
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
            }
        }
//        JDialog jd=new JDialog();
//        JButton cmd=new JButton("Test");
//        cmd.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Image i=((XPImage)getPNode()).getImage();
//                BlurFilter bf=new BlurFilter();
//                BufferedImage in=new BufferedImage(i.getWidth(null),i.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//                in.getGraphics().drawImage(i, 0,0, null);
//                BufferedImage out=new BufferedImage(i.getWidth(null),i.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//                bf.filter(in, out);
//                ((XPImage)getPNode()).setImage(out);
//            }
//        });
//        jd.getContentPane().add(cmd);
//        jd.pack();
//        jd.setVisible(true);
    }

    /**
     * Creates a new WMSServiceLayer object.
     *
     * @param  l  DOCUMENT ME!
     */
    public WMSServiceLayer(final Layer l) {
        setName(l.getTitle());
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

        setName(wmsServiceLayerElement.getAttribute("name").getValue()); // NOI18N

        try {
            setEnabled(wmsServiceLayerElement.getAttribute("enabled").getBooleanValue());         // NOI18N
        } catch (DataConversionException ex) {
        }
        try {
            setTranslucency(wmsServiceLayerElement.getAttribute("translucency").getFloatValue()); // NOI18N
        } catch (DataConversionException ex) {
        }
        try {
            setVisible(wmsServiceLayerElement.getAttribute("visible").getBooleanValue());         // NOI18N
        } catch (DataConversionException ex) {
        }
        setBackgroundColor(wmsServiceLayerElement.getAttribute("bgColor").getValue());            // NOI18N
        setImageFormat(wmsServiceLayerElement.getAttribute("imageFormat").getValue());            // NOI18N
        setExceptionsFormat(wmsServiceLayerElement.getAttribute("exceptionFormat").getValue());   // NOI18N
        final CapabilityLink cp = new CapabilityLink(wmsServiceLayerElement);
        final WMSCapabilities wmsCaps = capabilities.get(cp.getLink());
        setWmsCapabilities(wmsCaps);
        setCapabilitiesUrl(cp.getLink());

        // Grundeinstellungen sind gemacht, Jetzt fehlen noch die Layer

        final List layerList = wmsServiceLayerElement.getChildren("wmsLayer"); // NOI18N
        final Iterator<Element> it = layerList.iterator();
        while (it.hasNext()) {
            final Element elem = it.next();
            final String name = elem.getAttribute("name").getValue();          // NOI18N
            String styleName = null;
            boolean enabled = true;
            boolean info = false;
            try {
                enabled = elem.getAttribute("enabled").getBooleanValue();      // NOI18N
            } catch (Exception ex) {
            }
            try {
                info = elem.getAttribute("info").getBooleanValue();            // NOI18N
            } catch (Exception ex) {
            }
            try {
                styleName = elem.getAttribute("style").getValue();             // NOI18N
            } catch (Exception ex) {
            }
            if (wmsCaps != null) {
                final Layer l = searchForLayer(getWmsCapabilities().getLayer(), name);
                if (layerList.size() == 1) {
                    setName(l.getTitle());
                }

                Style style = null;
                if (styleName != null) {
                    style = l.getStyleResource(styleName);
                }
                this.addLayer(l, style, enabled, info);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  nextLayer      DOCUMENT ME!
     * @param  selectedStyle  DOCUMENT ME!
     * @param  enabled        DOCUMENT ME!
     * @param  info           DOCUMENT ME!
     */
    protected void addLayer(final Layer nextLayer, Style selectedStyle, final boolean enabled, final boolean info) {
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
        }

        for (int i = 0; i < nextLayer.getChildren().length; ++i) {
            final Layer childLayer = nextLayer.getChildren()[i];
            addLayer(childLayer);
        }
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
    public Vector getWMSLayers() {
        return wmsLayers;
    }

    @Override
    public void retrieve(final boolean forced) {
//        synchronized(retrievalBlocker) {
        if (DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug("retrieve()"); // NOI18N
            }
        }
        setRefreshNeeded(false);
        if ((ir != null) && ir.isAlive() && ir.getUrl().equals(getGetMapUrl()) && !forced) {
            if (logger.isDebugEnabled()) {
                // macht nix
                // mehrfachaufruf mit der gleichen url = unsinn
                logger.debug("multiple invocations with the same url = humbug"); // NOI18N
            }
        } else {
            if ((ir != null) && ir.isAlive()) {
                // logger.fatal("Versuche den vorherigen Retrievalprozess zu stoppen. (interrupt())");
                ir.youngerWMSCall();
                ir.interrupt();

                retrievalAborted(new RetrievalEvent());
            }
            ir = new ImageRetrieval(this);
            if (DEBUG) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getMapURL(): " + getGetMapUrl()); // NOI18N
                }
            }
            ir.setUrl(getGetMapUrl());
            // new
            ir.setWMSCapabilities(getWmsCapabilities());
            if (DEBUG) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ir.start();"); // NOI18N
                }
            }
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }
//        }
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
            logger.warn("backgroundcolor was null. Set it to 0xF0F0F0"); // NOI18N
            backgroundColor = "0xF0F0F0";                                // NOI18N
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
        this.exceptionsFormat = exceptionsFormat;
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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGetMapUrl() {
        String url = getGetMapPrefix();

        if ((bb != null) && (url != null)) {
            if (url.indexOf("?") < 0)                                                                         // NOI18N
            {
                url += "?";                                                                                   // NOI18N
            }
            final String version = getWmsCapabilities().getVersion();
            if (version.trim().equals("1.0.0") || version.trim().equals("1.0") || version.trim().equals("1")) // NOI18N
            {
                url += "&WMTVER=1.0.0&REQUEST=map";                                                           // NOI18N
            } else {
                url += "&VERSION=" + version + "&REQUEST=GetMap";                                             // NOI18N
            }
            url += "&BBOX=" + bb.getURLString();                                                              // NOI18N
            url += "&WIDTH=" + width;                                                                         // NOI18N
            url += "&HEIGHT=" + height;                                                                       // NOI18N

            if (version.trim().equals("1.3") || version.trim().equals("1.3.0")) {
                url += "&CRS=" + srs;                                                        // NOI18N
            } else {
                url += "&SRS=" + srs;                                                        // NOI18N
            }
            url += "&FORMAT=" + imageFormat;                                                 // NOI18N
            url += "&TRANSPARENT=" + new Boolean(transparentImage).toString().toUpperCase(); // NOI18N
            url += "&BGCOLOR=" + getBackgroundColor();                                       // NOI18N
            url += "&EXCEPTIONS=" + exceptionsFormat;                                        // NOI18N
            url += getLayersString(wmsLayers);
            if (hasEveryLayerAStyle(wmsLayers)) {
                // the styles parameter must contain the same number of values as the layers parameter.
                // If this requirement cannot be fulfilled, the style parameter should be sent without a value due
                // to generate a valid request.
                url += getStylesString(wmsLayers);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "style parameter was added without a value to the getMap Request, because not every layer, " // NOI18N
                                + "which is used within the request has a selected style");                          // NOI18N
                }
                url += "&STYLES=";
            }
            return url;
        } else {
            return null;
        }
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
            url += "&SRS=" + CismapBroker.getInstance().getSrs().getCode();                                   // NOI18N
            url += "&FORMAT=" + imageFormat;                                                                  // NOI18N
            url += "&TRANSPARENT=" + new Boolean(transparentImage).toString().toUpperCase();                  // NOI18N
            url += "&BGCOLOR=" + backgroundColor;                                                             // NOI18N
            // url+="&EXCEPTIONS="+"text/html";//exceptionsFormat;
            url += getLayersString(wmsLayers);

            if (hasEveryLayerAStyle(wmsLayers)) {
                // the styles parameter must have the same number of values as the layers parameter.
                // If this requirement cannot be fulfilled, the optional style parameter should be omitted due
                // to generate a valid request.
                url += getStylesString(wmsLayers);
            }

            url += "&QUERY_LAYERS=" + l.getOgcCapabilitiesLayer().getName(); // NOI18N
            url += "&INFO_FORMAT=text/html";                                 // NOI18N
            url += "&X=" + x;                                                // NOI18N
            url += "&Y=" + y;                                                // NOI18N
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
            final Operation op = getWmsCapabilities().getRequest().getMapOperation();
            String prefix = null;

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
            return prefix;
        } catch (Throwable npe) {
            logger.warn("Throwable in getMapPrefix", npe); // NOI18N
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
            logger.warn("NPE in getGetMapPrefix()", npe); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wmsLayers  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getLayersString(final Vector wmsLayers) {
        final StringBuilder layerString = new StringBuilder("");                                              // NOI18N
        int counter = 0;
        final Iterator it = wmsLayers.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if ((o instanceof WMSLayer) && ((WMSLayer)o).isEnabled()) {
                counter++;
                if (counter > 1) {
                    layerString.append(",");                                                                  // NOI18N
                }
                layerString.append(((WMSLayer)o).getOgcCapabilitiesLayer().getName().replaceAll(" ", "%20")); // NOI18N
            }
        }
        if (counter > 0) {
            return "&LAYERS=" + layerString.toString();                                                       // NOI18N
        } else {
            return "";                                                                                        // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wmsLayers  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getStylesString(final Vector wmsLayers) {
        final StringBuilder stylesString = new StringBuilder("");                                       // NOI18N
        int counter = 0;
        final Iterator it = wmsLayers.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if ((o instanceof WMSLayer) && (((WMSLayer)o).getSelectedStyle() != null) && ((WMSLayer)o).isEnabled()) {
                counter++;
                if (counter > 1) {
                    stylesString.append(",");                                                           // NOI18N
                }
                stylesString.append(((WMSLayer)o).getSelectedStyle().getName().replaceAll(" ", "%20")); // NOI18N
            }
        }
//        if (counter>0){
//            return "&STYLES="+stylesString;
//        } else return "";
        return "&STYLES=" + stylesString.toString(); // LDS Bugfix//NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wmsLayers  DOCUMENT ME!
     *
     * @return  true, if every of the given layer has a selected style
     */
    private boolean hasEveryLayerAStyle(final Vector wmsLayers) {
        final Iterator it = wmsLayers.iterator();

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
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        final Element layerConf = new Element("WMSServiceLayer");                                      // NOI18N
        layerConf.setAttribute("name", getName());                                                     // NOI18N
        layerConf.setAttribute("visible", new Boolean(getPNode().getVisible()).toString());            // NOI18N
        layerConf.setAttribute("enabled", new Boolean(isEnabled()).toString());                        // NOI18N
        layerConf.setAttribute("translucency", new Float(getTranslucency()).toString());               // NOI18N
        layerConf.setAttribute("bgColor", getBackgroundColor());                                       // NOI18N
        layerConf.setAttribute("imageFormat", getImageFormat());                                       // NOI18N
        layerConf.setAttribute("exceptionFormat", getExceptionsFormat());                              // NOI18N
        final CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, getCapabilitiesUrl(), false);
        layerConf.addContent(capLink.getElement());
        final Iterator lit = getWMSLayers().iterator();
        while (lit.hasNext()) {
            final Object elem = lit.next();
            if (elem instanceof WMSLayer) {
                final WMSLayer wmsLayer = (WMSLayer)elem;
                final Element wmsLayerConf = new Element("wmsLayer");                                  // NOI18N
                wmsLayerConf.setAttribute("name", wmsLayer.getOgcCapabilitiesLayer().getName());       // NOI18N
                wmsLayerConf.setAttribute("enabled", new Boolean(wmsLayer.isEnabled()).toString());    // NOI18N
                try {
                    wmsLayerConf.setAttribute("style", wmsLayer.getSelectedStyle().getName());         // NOI18N
                } catch (Exception e) {
                }
                wmsLayerConf.setAttribute("info", new Boolean(wmsLayer.isQuerySelected()).toString()); // NOI18N
                layerConf.addContent(wmsLayerConf);
            }
        }
        return layerConf;
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
        w.imageObject = imageObject;
        w.ir = new ImageRetrieval(w);
        w.layerPosition = layerPosition;
        w.listeners = new Vector(listeners);
        w.name = name;
        w.ogcLayers = ogcLayers;
        w.progress = progress;
        w.propertyChangeSupport = propertyChangeSupport;
        w.refreshNeeded = refreshNeeded;
        w.srs = srs;
        w.translucency = translucency;
        w.transparentImage = transparentImage;
        w.treePaths = treePaths;
        w.width = width;
        w.wmsCapabilities = wmsCapabilities;
        w.wmsLayers = wmsLayers;
        w.wmsServiceLayerElement = wmsServiceLayerElement;
        return w;
    }

    @Override
    public boolean propertyEquals(final Object obj) {
        // TODO Dieses equals wird 10000 mal aufgerufen
        if (obj instanceof WMSServiceLayer) {
            final WMSServiceLayer tester = (WMSServiceLayer)obj;
            if (getName().equals(tester.getName())
                        && getGetMapPrefix().equals(tester.getGetMapPrefix())
                        && getLayersString(wmsLayers).equals(tester.getLayersString(tester.wmsLayers))
                        && getStylesString(wmsLayers).equals(tester.getStylesString(tester.wmsLayers))) {
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

//  @Override
//  public int hashCode()
//  {
//    int hash = 3;
//    hash = 73 * hash + (this.wmsLayers != null ? this.wmsLayers.hashCode() : 0);
//    hash = 73 * hash + (this.ogcLayers != null ? this.ogcLayers.hashCode() : 0);
//    hash = 73 * hash + (this.srs != null ? this.srs.hashCode() : 0);
//    hash = 73 * hash + (this.capabilitiesUrl != null ? this.capabilitiesUrl.hashCode() : 0);
//    return hash;
//  }

}
