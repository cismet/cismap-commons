/*
 * WMSServiceLayer.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 22. November 2005, 12:21
 *
 */
package de.cismet.cismap.commons.raster.wms;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Operation;
import de.cismet.cismap.commons.wms.capabilities.Style;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import edu.umd.cs.piccolo.PNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import de.cismet.tools.PropertyEqualsProvider;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WMSServiceLayer extends AbstractWMSServiceLayer implements RetrievalServiceLayer, RasterMapService, PropertyEqualsProvider {

    //private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    Vector wmsLayers = new Vector();
    Vector ogcLayers = new Vector();
    private PNode imageObject = new PNode();
    private String imageFormat = null;
    private String backgroundColor = null;
    private String exceptionsFormat = null;
    private boolean transparentImage = true;
    private String srs;
    private WMSCapabilities wmsCapabilities;
    private String capabilitiesUrl;
    //Used by clone()
    private Vector treePaths;
    private Element wmsServiceLayerElement;
    private HashMap<String, WMSCapabilities> capabilities;

    public WMSServiceLayer() {
    }

    public WMSServiceLayer(Vector treePaths) {
        this.treePaths = treePaths;
        if (treePaths != null) {
            Iterator it = treePaths.iterator();
            if (treePaths.size() > 1) {
                setName("Layerzusammenstellung");//NOI18N
            }

            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof TreePath) {
                    TreePath nextTreePath = (TreePath) next;
                    if (nextTreePath.getLastPathComponent() instanceof Layer) {
                        Layer nextLayer = (Layer) nextTreePath.getLastPathComponent();
                        addLayer(nextLayer);
                        if (getName() == null) {
                            setName(nextLayer.getTitle());
                        }
                    } else if (nextTreePath.getLastPathComponent() instanceof Style) {
                        Style nextStyle = (Style) nextTreePath.getLastPathComponent();
                        if (nextTreePath.getPathComponent(nextTreePath.getPathCount() - 2) instanceof Layer) {
                            Layer nextLayer = ((Layer) nextTreePath.getPathComponent(nextTreePath.getPathCount() - 2));
                            addLayer(nextLayer, nextStyle);
                            if (getName() == null) {
                                setName(nextLayer.getTitle());
                            }
                        }
                    }
                }
            }
        }
    }

    public WMSServiceLayer(Element wmsServiceLayerElement, HashMap<String, WMSCapabilities> capabilities) {
        this.wmsServiceLayerElement = wmsServiceLayerElement;
        this.capabilities = capabilities;

        setName(wmsServiceLayerElement.getAttribute("name").getValue());//NOI18N

        try {
            setEnabled(wmsServiceLayerElement.getAttribute("enabled").getBooleanValue());//NOI18N
        } catch (DataConversionException ex) {
        }
        try {
            setTranslucency(wmsServiceLayerElement.getAttribute("translucency").getFloatValue());//NOI18N
        } catch (DataConversionException ex) {
        }
        try {
            setVisible(wmsServiceLayerElement.getAttribute("visible").getBooleanValue());//NOI18N
        } catch (DataConversionException ex) {
        }
        setBackgroundColor(wmsServiceLayerElement.getAttribute("bgColor").getValue());//NOI18N
        setImageFormat(wmsServiceLayerElement.getAttribute("imageFormat").getValue());//NOI18N
        setExceptionsFormat(wmsServiceLayerElement.getAttribute("exceptionFormat").getValue());//NOI18N
        CapabilityLink cp = new CapabilityLink(wmsServiceLayerElement);
        WMSCapabilities wmsCaps = capabilities.get(cp.getLink());
        setWmsCapabilities(wmsCaps);
        setCapabilitiesUrl(cp.getLink());

        //Grundeinstellungen sind gemacht, Jetzt fehlen noch die Layer

        List layerList = wmsServiceLayerElement.getChildren("wmsLayer");//NOI18N
        Iterator<Element> it = layerList.iterator();
        while (it.hasNext()) {
            Element elem = it.next();
            String name = elem.getAttribute("name").getValue();//NOI18N
            String styleName = null;
            boolean enabled = true;
            boolean info = false;
            try {
                enabled = elem.getAttribute("enabled").getBooleanValue();//NOI18N
            } catch (Exception ex) {
            }
            try {
                info = elem.getAttribute("info").getBooleanValue();//NOI18N
            } catch (Exception ex) {
            }
            try {
                styleName = elem.getAttribute("style").getValue();//NOI18N
            } catch (Exception ex) {
            }
            if (wmsCaps != null) {
                Layer l = searchForLayer(getWmsCapabilities().getLayer(), name);
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

    private void addLayer(Layer nextLayer, Style selectedStyle, boolean enabled, boolean info) {
        if (nextLayer.getName() != null && !nextLayer.getName().equals(""))//NOI18N
        {
            if (selectedStyle == null) {
                if (nextLayer.getStyles() != null && nextLayer.getStyles().length > 0 && nextLayer.getStyles()[0] != null) {
                    selectedStyle = nextLayer.getStyles()[0];
                }
            }
            WMSLayer wmsLayer = new WMSLayer(nextLayer, selectedStyle);
            wmsLayer.setEnabled(enabled);
            wmsLayer.setParentServiceLayer(this);
            wmsLayer.setQuerySelected(info);
            if (ogcLayers.indexOf(wmsLayer.getOgcCapabilitiesLayer()) < 0) {
                wmsLayers.add(wmsLayer);
                ogcLayers.add(wmsLayer.getOgcCapabilitiesLayer());
            }
        }

        for (int i = 0; i < nextLayer.getChildren().length; ++i) {
            Layer childLayer = nextLayer.getChildren()[i];
            addLayer(childLayer);
        }

    }

    private void addLayer(Layer nextLayer, Style selectedStyle) {
        addLayer(nextLayer, selectedStyle, true, false);
    }

    private void addLayer(Layer nextLayer) {
        addLayer(nextLayer, null);
    }

    public void removeLayer(WMSLayer layer) {
        wmsLayers.remove(layer);
        ogcLayers.remove(layer.getOgcCapabilitiesLayer());
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return "...";//NOI18N
        }
    }

    public Vector getWMSLayers() {
        return wmsLayers;
    }
    Object retrievalBlocker = new Object();

    @Override
    public void retrieve(boolean forced) {
//        synchronized(retrievalBlocker) {
        if (DEBUG) {
            logger.debug("retrieve()");//NOI18N
        }
        setRefreshNeeded(false);
        if (ir != null && ir.isAlive() && ir.getUrl().equals(getGetMapUrl()) && !forced) {
            //macht nix
            //mehrfachaufruf mit der gleichen url = unsinn
            logger.debug("multiple invocations with the same url = humbug");//NOI18N
        } else {
            if (ir != null && ir.isAlive()) {
                //logger.fatal("Versuche den vorherigen Retrievalprozess zu stoppen. (interrupt())");
                ir.youngerWMSCall();
                ir.interrupt();

                retrievalAborted(new RetrievalEvent());
            }
            ir = new ImageRetrieval(this);
            if (DEBUG) {
                logger.debug("getMapURL(): " + getGetMapUrl());//NOI18N
            }
            ir.setUrl(getGetMapUrl());
            //new
            ir.setWMSCapabilities(getWmsCapabilities());
            if (DEBUG) {
                logger.debug("ir.start();");//NOI18N
            }
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }
//        }
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public String getBackgroundColor() {
        if (backgroundColor != null) {
            return backgroundColor;
        } else {
            logger.warn("backgroundcolor was null. Set it to 0xF0F0F0");//NOI18N
            backgroundColor = "0xF0F0F0";//NOI18N
            return backgroundColor;
        }
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getExceptionsFormat() {
        return exceptionsFormat;
    }

    public void setExceptionsFormat(String exceptionsFormat) {
        this.exceptionsFormat = exceptionsFormat;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    private String getGetMapUrl() {
        String url = getGetMapPrefix();

        if (bb != null && url != null) {
            if (url.indexOf("?") < 0)//NOI18N
            {
                url += "?";//NOI18N
            }
            String version = getWmsCapabilities().getVersion();
            if (version.trim().equals("1.0.0") || version.trim().equals("1.0") || version.trim().equals("1"))//NOI18N
            {
                url += "&WMTVER=1.0.0&REQUEST=map";//NOI18N
            } else {
                url += "&VERSION=" + version + "&REQUEST=GetMap";//NOI18N
            }
            url += "&BBOX=" + bb.getURLString();//NOI18N
            url += "&WIDTH=" + width;//NOI18N
            url += "&HEIGHT=" + height;//NOI18N

            if (version.trim().equals("1.3") || version.trim().equals("1.3.0")) {
                url += "&CRS=" + srs;//NOI18N
            } else {
                url += "&SRS=" + srs;//NOI18N
            }
            url += "&FORMAT=" + imageFormat;//NOI18N
            url += "&TRANSPARENT=" + new Boolean(transparentImage).toString().toUpperCase();//NOI18N
            url += "&BGCOLOR=" + getBackgroundColor();//NOI18N
            url += "&EXCEPTIONS=" + exceptionsFormat;//NOI18N
            url += getLayersString(wmsLayers);
            if (hasEveryLayerAStyle(wmsLayers)) {
                // the styles parameter must contain the same number of values as the layers parameter.
                // If this requirement cannot be fulfilled, the style parameter should be sent without a value due
                // to generate a valid request.
                url += getStylesString(wmsLayers);
            } else {
                logger.debug("style parameter was added without a value to the getMap Request, because not every layer, " + //NOI18N
                        "which is used within the request has a selected style"); //NOI18N
                url += "&STYLES=";
            }
            return url;
        } else {
            return null;
        }

    }

    public String getGetFeatureInfoUrl(int x, int y, WMSLayer l) {
        String url = getGetFeatureInfoPrefix();

        if (bb != null && url != null) {
            if (url.indexOf("?") < 0)//NOI18N
            {
                url += "?";//NOI18N
            }
            String version = getWmsCapabilities().getVersion();
            if (version.trim().equals("1.0.0") || version.trim().equals("1.0") || version.trim().equals("1"))//NOI18N
            {
                url += "&WMTVER=1.0.0&REQUEST=feature_info";//NOI18N
            } else {
                url += "&VERSION=" + version + "&REQUEST=GetFeatureInfo";//NOI18N
            }
            url += "&BBOX=" + bb.getURLString();//NOI18N
            url += "&WIDTH=" + width;//NOI18N
            url += "&HEIGHT=" + height;//NOI18N
            url += "&SRS=" + CismapBroker.getInstance().getSrs().getCode();//NOI18N
            url += "&FORMAT=" + imageFormat;//NOI18N
            url += "&TRANSPARENT=" + new Boolean(transparentImage).toString().toUpperCase();//NOI18N
            url += "&BGCOLOR=" + backgroundColor;//NOI18N
            //url+="&EXCEPTIONS="+"text/html";//exceptionsFormat;
            url += getLayersString(wmsLayers);

            if (hasEveryLayerAStyle(wmsLayers)) {
                // the styles parameter must have the same number of values as the layers parameter.
                // If this requirement cannot be fulfilled, the optional style parameter should be omitted due
                // to generate a valid request.
                url += getStylesString(wmsLayers);
            }

            url += "&QUERY_LAYERS=" + l.getOgcCapabilitiesLayer().getName();//NOI18N
            url += "&INFO_FORMAT=text/html";//NOI18N
            url += "&X=" + x;//NOI18N
            url += "&Y=" + y;//NOI18N
            return url;
        } else {
            return null;
        }
    }

    private String getGetMapPrefix() {
        try {
            Operation op = getWmsCapabilities().getRequest().getMapOperation();
            String prefix = null;

            if (op != null) {
                //ToDo UGLY WINNING WSS schneidet wenn es get und post gibt das geht.
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
            logger.warn("Throwable in getMapPrefix", npe);//NOI18N
            return null;
        }

    }

    private String getGetFeatureInfoPrefix() {
        try {
            Operation op = getWmsCapabilities().getRequest().getFeatureInfoOperation();
            String prefix = null;

            if (op != null) {
                prefix = op.getGet().toString();
            }
            return prefix;
        } catch (NullPointerException npe) {
            logger.warn("NPE in getGetMapPrefix()", npe);//NOI18N
            return null;
        }
    }

    private String getLayersString(Vector wmsLayers) {
        StringBuilder layerString = new StringBuilder("");//NOI18N
        int counter = 0;
        Iterator it = wmsLayers.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof WMSLayer && ((WMSLayer) o).isEnabled()) {
                counter++;
                if (counter > 1) {
                    layerString.append(",");//NOI18N
                }
                layerString.append(((WMSLayer) o).getOgcCapabilitiesLayer().getName().replaceAll(" ", "%20"));//NOI18N
            }
        }
        if (counter > 0) {
            return "&LAYERS=" + layerString.toString();//NOI18N
        } else {
            return "";//NOI18N
        }
    }

    private String getStylesString(Vector wmsLayers) {
        StringBuilder stylesString = new StringBuilder("");//NOI18N
        int counter = 0;
        Iterator it = wmsLayers.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof WMSLayer && ((WMSLayer) o).getSelectedStyle() != null && ((WMSLayer) o).isEnabled()) {
                counter++;
                if (counter > 1) {
                    stylesString.append(",");//NOI18N
                }
                stylesString.append(((WMSLayer) o).getSelectedStyle().getName().replaceAll(" ", "%20"));//NOI18N
            }
        }
//        if (counter>0){
//            return "&STYLES="+stylesString;
//        } else return "";
        return "&STYLES=" + stylesString.toString(); //LDS Bugfix//NOI18N
    }

    /**
     * @param wmsLayers
     * @return true, if every of the given layer has a selected style
     */
    private boolean hasEveryLayerAStyle(Vector wmsLayers) {
        Iterator it = wmsLayers.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof WMSLayer && ((WMSLayer) o).isEnabled()) {
                if (((WMSLayer) o).getSelectedStyle() == null) {
                    return false;
                }
            }
        }

        return true;
    }

    public WMSCapabilities getWmsCapabilities() {
        return wmsCapabilities;
    }

    public void setWmsCapabilities(WMSCapabilities wmsCapabilities) {
        this.wmsCapabilities = wmsCapabilities;

    }

    @Override
    public void setPNode(PNode imageObject) {
        boolean vis = imageObject.getVisible();
        this.imageObject = imageObject;
        imageObject.setVisible(vis);
    }

    @Override
    public PNode getPNode() {
        return imageObject;
    }

    public String getCapabilitiesUrl() {
        return capabilitiesUrl;
    }

    public void setCapabilitiesUrl(String capabilitiesUrl) {
        this.capabilitiesUrl = capabilitiesUrl;
    }

    public Element getElement() {
        Element layerConf = new Element("WMSServiceLayer");//NOI18N
        layerConf.setAttribute("name", getName());//NOI18N
        layerConf.setAttribute("visible", new Boolean(getPNode().getVisible()).toString());//NOI18N
        layerConf.setAttribute("enabled", new Boolean(isEnabled()).toString());//NOI18N
        layerConf.setAttribute("translucency", new Float(getTranslucency()).toString());//NOI18N
        layerConf.setAttribute("bgColor", getBackgroundColor());//NOI18N
        layerConf.setAttribute("imageFormat", getImageFormat());//NOI18N
        layerConf.setAttribute("exceptionFormat", getExceptionsFormat());//NOI18N
        CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, getCapabilitiesUrl(), false);
        layerConf.addContent(capLink.getElement());
        Iterator lit = getWMSLayers().iterator();
        while (lit.hasNext()) {
            Object elem = lit.next();
            if (elem instanceof WMSLayer) {
                WMSLayer wmsLayer = (WMSLayer) elem;
                Element wmsLayerConf = new Element("wmsLayer");//NOI18N
                wmsLayerConf.setAttribute("name", wmsLayer.getOgcCapabilitiesLayer().getName());//NOI18N
                wmsLayerConf.setAttribute("enabled", new Boolean(wmsLayer.isEnabled()).toString());//NOI18N
                try {
                    wmsLayerConf.setAttribute("style", wmsLayer.getSelectedStyle().getName());//NOI18N
                } catch (Exception e) {
                }
                wmsLayerConf.setAttribute("info", new Boolean(wmsLayer.isQuerySelected()).toString());//NOI18N
                layerConf.addContent(wmsLayerConf);
            }
        }
        return layerConf;
    }

    private Layer searchForLayer(Layer layer, String name) {
        if (layer.getName() != null && layer.getName().equals(name)) {
            return layer;
        } else {
            Layer[] lArr = layer.getChildren();
            for (int i = 0; i < lArr.length; i++) {
                Layer l = searchForLayer(lArr[i], name);
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
    public boolean propertyEquals(Object obj) {
        //TODO Dieses equals wird 10000 mal aufgerufen
        if (obj instanceof WMSServiceLayer) {
            WMSServiceLayer tester = (WMSServiceLayer) obj;
            if (getName().equals(tester.getName())
                    && getGetMapPrefix().equals(tester.getGetMapPrefix())
                    && getLayersString(wmsLayers).equals(tester.getLayersString(tester.wmsLayers))
                    && getStylesString(wmsLayers).equals(tester.getStylesString(tester.wmsLayers))) {
                return true;
            }
        }

        return false;
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
