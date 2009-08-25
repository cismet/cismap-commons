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

import org.deegree.services.wms.capabilities.Layer;
import org.deegree.services.wms.capabilities.Operation;
import org.deegree.services.wms.capabilities.Style;
import org.deegree.services.wms.capabilities.WMSCapabilities;
import org.deegree_impl.services.capabilities.HTTP_Impl;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WMSServiceLayer extends AbstractWMSServiceLayer implements RetrievalServiceLayer,RasterMapService{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    
    Vector wmsLayers=new Vector();
    Vector ogcLayers=new Vector();
    
    private PNode imageObject=new PNode();
    private String imageFormat=null;
    private String backgroundColor=null;
    private String exceptionsFormat=null;;
    private boolean transparentImage=true;
    private String srs;
    private WMSCapabilities wmsCapabilities;
    private int progress;
    private boolean refreshNeeded=false;
    private Object errorObject=null;
    private String capabilitiesUrl;
    
    //Used by clone()
    private Vector treePaths;
    private Element wmsServiceLayerElement;
    private HashMap<String,WMSCapabilities> capabilities;
    
    public WMSServiceLayer() {
        
    }
    
    public WMSServiceLayer(Vector treePaths) {
        this.treePaths=treePaths;
        if (treePaths!=null) {
            Iterator it=treePaths.iterator();
            if (treePaths.size()>1) {
                setName("Layerzusammenstellung");
            }
            
            while (it.hasNext()) {
                Object next=it.next();
                if (next instanceof TreePath) {
                    TreePath nextTreePath=(TreePath)next;
                    if (nextTreePath.getLastPathComponent() instanceof Layer) {
                        Layer nextLayer=(Layer)nextTreePath.getLastPathComponent();
                        addLayer(nextLayer);
                        if (getName()==null) {
                            setName(nextLayer.getTitle());
                        }
                    } else if (nextTreePath.getLastPathComponent() instanceof Style) {
                        Style nextStyle=(Style)nextTreePath.getLastPathComponent();
                        if (nextTreePath.getPathComponent(nextTreePath.getPathCount()-2) instanceof Layer) {
                            Layer nextLayer=((Layer)nextTreePath.getPathComponent(nextTreePath.getPathCount()-2));
                            addLayer(nextLayer,nextStyle);
                            if (getName()==null) {
                                setName(nextLayer.getTitle());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public WMSServiceLayer(Element wmsServiceLayerElement,HashMap<String,WMSCapabilities> capabilities) {
        this.wmsServiceLayerElement=wmsServiceLayerElement;
        this.capabilities=capabilities;
        
        setName(wmsServiceLayerElement.getAttribute("name").getValue());
        
        try {
            setEnabled(wmsServiceLayerElement.getAttribute("enabled").getBooleanValue());
        } catch (DataConversionException ex) {
            
        }
        try {
            setTranslucency(wmsServiceLayerElement.getAttribute("translucency").getFloatValue());
        } catch (DataConversionException ex) {
            
        }
        try {
            setVisible(wmsServiceLayerElement.getAttribute("visible").getBooleanValue());
        } catch (DataConversionException ex) {
            
        }
        setBackgroundColor(wmsServiceLayerElement.getAttribute("bgColor").getValue());
        setImageFormat(wmsServiceLayerElement.getAttribute("imageFormat").getValue());
        setExceptionsFormat(wmsServiceLayerElement.getAttribute("exceptionFormat").getValue());
        CapabilityLink cp=new CapabilityLink(wmsServiceLayerElement);
        WMSCapabilities wmsCaps=capabilities.get(cp.getLink());
        setWmsCapabilities(wmsCaps);
        setCapabilitiesUrl(cp.getLink());
        
        //Grundeinstellungen sind gemacht, Jetzt fehlen noch die Layer
        
        List layerList=wmsServiceLayerElement.getChildren("wmsLayer");
        Iterator<Element> it=layerList.iterator();
        while (it.hasNext()) {
            Element elem = it.next();
            String name=elem.getAttribute("name").getValue();
            String styleName=null;
            boolean enabled=true;
            boolean info=false;
            try {
                enabled=elem.getAttribute("enabled").getBooleanValue();
            } catch (Exception ex) {
                
            }
            try {
                info=elem.getAttribute("info").getBooleanValue();
            } catch (Exception ex) {
                
            }
            try {
                styleName=elem.getAttribute("style").getValue();
            } catch (Exception ex) {
                
            }
            if(wmsCaps != null){
                Layer l=searchForLayer(getWmsCapabilities().getCapability().getLayer(),name);
                if (layerList.size()==1){
                    setName(l.getTitle());
                }
                
                Style style=null;
                if (styleName!=null) {
                    style= l.getStyleResource(styleName);
                }
                this.addLayer(l,style,enabled,info);
            }
        }
    }
    
    private void addLayer(Layer nextLayer,Style selectedStyle, boolean enabled, boolean info) {
        if (nextLayer.getName()!=null&&!nextLayer.getName().equals("")) {
            if (selectedStyle==null) {
                if (nextLayer.getStyles()!=null&&nextLayer.getStyles().length>0&&nextLayer.getStyles()[0]!=null) {
                    selectedStyle=nextLayer.getStyles()[0];
                }
            }
            WMSLayer wmsLayer=new WMSLayer(nextLayer, selectedStyle);
            wmsLayer.setEnabled(enabled);
            wmsLayer.setParentServiceLayer(this);
            wmsLayer.setQuerySelected(info);
            if (ogcLayers.indexOf(wmsLayer.getOgcCapabilitiesLayer())<0) {
                wmsLayers.add(wmsLayer);
                ogcLayers.add(wmsLayer.getOgcCapabilitiesLayer());
            }
        }
        
        for (int i=0;i<nextLayer.getLayer().length;++i) {
            Layer childLayer=nextLayer.getLayer()[i];
            addLayer(childLayer);
        }
        
    }
    
    private void addLayer(Layer nextLayer,Style selectedStyle) {
        addLayer(nextLayer,selectedStyle,true,false);
    }
    
    private void addLayer(Layer nextLayer) {
        addLayer(nextLayer,null);
    }
    
    public void removeLayer(WMSLayer layer) {
        wmsLayers.remove(layer);
        ogcLayers.remove(layer.getOgcCapabilitiesLayer());
    }
    
    public String toString() {
        if (name!=null) {
            return name;
        } else {
            return "...";
        }
    }
    
    
    public Vector getWMSLayers() {
        return wmsLayers;
    }
    
    
    Object retrievalBlocker=new Object();
    public void retrieve(boolean forced) {
//        synchronized(retrievalBlocker) {
            log.debug("retrieve()");
            setRefreshNeeded(false);
            if (ir!=null&&ir.isAlive()&&ir.getUrl().equals(getGetMapUrl())&&!forced) {
                //macht nix
                //mehrfachaufruf mit der gleichen url = unsinn
                log.debug("mehrfachaufruf mit der gleichen url = unsinn");
            } else {                
                if (ir!=null&&ir.isAlive()) {
                    //log.fatal("Versuche den vorherigen Retrievalprozess zu stoppen. (interrupt())");
                    ir.youngerWMSCall();
                    ir.interrupt();
                    
                    retrievalAborted(new RetrievalEvent());
                }
                ir=new ImageRetrieval(this);
                log.debug("getMapURL(): "+getGetMapUrl());
                ir.setUrl(getGetMapUrl());
                //new
                ir.setWMSCapabilities(getWmsCapabilities());
                log.debug("ir.start();");
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
        if (backgroundColor!=null) {
            return backgroundColor;
        } else {
            log.warn("backgroundcolor was null. Set it to 0xF0F0F0");
            backgroundColor="0xF0F0F0";
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
        String url=getGetMapPrefix();
        
        if (bb!=null &&url != null) {
            if (url.indexOf("?")<0) {
                url+="?";
            }
            String version=getWmsCapabilities().getVersion();
            if (version.trim().equals("1.0.0")||version.trim().equals("1.0")||version.trim().equals("1")) {
                url+="&WMTVER=1.0.0&REQUEST=map";
            } else {
                url+="&VERSION="+version+"&REQUEST=GetMap";
            }
            url+="&BBOX="+bb.getURLString();
            url+="&WIDTH="+width;
            url+="&HEIGHT="+height;
            url+="&SRS="+srs;
            url+="&FORMAT="+imageFormat;
            url+="&TRANSPARENT="+new Boolean(transparentImage).toString().toUpperCase();
            url+="&BGCOLOR="+getBackgroundColor();
            url+="&EXCEPTIONS="+exceptionsFormat;
            url+=getLayersString(wmsLayers);
            url+=getStylesString(wmsLayers);            
            return url;
        } else {            
            return null;
        }
        
    }
    public String getGetFeatureInfoUrl(int x,int y,WMSLayer l) {
        String url=getGetFeatureInfoPrefix();
        
        if (bb!=null &&url != null) {
            if (url.indexOf("?")<0) {
                url+="?";
            }
            String version=getWmsCapabilities().getVersion();
            if (version.trim().equals("1.0.0")||version.trim().equals("1.0")||version.trim().equals("1")) {
                url+="&WMTVER=1.0.0&REQUEST=feature_info";
            } else {
                url+="&VERSION="+version+"&REQUEST=GetFeatureInfo";
            }
            url+="&BBOX="+bb.getURLString();
            url+="&WIDTH="+width;
            url+="&HEIGHT="+height;
            url+="&SRS="+srs;
            url+="&FORMAT="+imageFormat;
            url+="&TRANSPARENT="+new Boolean(transparentImage).toString().toUpperCase();
            url+="&BGCOLOR="+backgroundColor;
            //url+="&EXCEPTIONS="+"text/html";//exceptionsFormat;
            url+=getLayersString(wmsLayers);
            url+=getStylesString(wmsLayers);
            url+="&QUERY_LAYERS="+l.getOgcCapabilitiesLayer().getName();
            url+="&INFO_FORMAT=text/html";
            url+="&X="+x;
            url+="&Y="+y;
            return url;
        } else {
            return null;
        }
    }
    
    private String getGetMapPrefix() {        
        try {            
            Operation op=getWmsCapabilities().getCapability().getRequest().getOperation(Operation.MAP);            
            Object o=null;
            String prefix=null;
            if (op==null) {                
                op=getWmsCapabilities().getCapability().getRequest().getOperation(Operation.GETMAP);                
            }
            
            if (op!=null) {
                o=op.getDCPTypes()[0].getProtocol();                
            }
            if (o instanceof HTTP_Impl){                
                //ToDo UGLY WINNING WSS schneidet wenn es get und post gibt das geht.
                if(((HTTP_Impl)o).getGetOnlineResources().length !=0){
                    prefix=((HTTP_Impl)o).getGetOnlineResources()[0].toString();
                } else if(((HTTP_Impl)o).getPostOnlineResources().length!=0){                    
                    prefix=((HTTP_Impl)o).getPostOnlineResources()[0].toString();
                } else {                    
                    return null;
                }                
            }            
            return prefix;
        } catch (Throwable npe){
            log.warn("Throwable in getMapPrefix",npe);            
            return null;
        }
        
    }
    private String getGetFeatureInfoPrefix() {
        try {
            Operation op=getWmsCapabilities().getCapability().getRequest().getOperation(Operation.FEATUREINFO);
            Object o=null;
            String prefix=null;
            if (op==null) {
                op=getWmsCapabilities().getCapability().getRequest().getOperation(Operation.GETFEATUREINFO);
            }
            
            if (op!=null) {
                o=op.getDCPTypes()[0].getProtocol();
            }
            if (o instanceof HTTP_Impl){
                prefix=((HTTP_Impl)o).getGetOnlineResources()[0].toString();
                
            }
            return prefix;
        } catch (NullPointerException npe){
            log.warn("NPE in getGetMapPrefix()",npe);
            return null;
        }
    }
    
    
    
    private String getLayersString(Vector wmsLayers){
        String layerString="";
        int counter=0;
        Iterator it=wmsLayers.iterator();
        while (it.hasNext()){
            Object o=it.next();
            if (o instanceof WMSLayer && ((WMSLayer)o).isEnabled()) {
                counter++;
                if (counter>1) {
                    layerString+=",";
                }
                layerString+=((WMSLayer)o).getOgcCapabilitiesLayer().getName().replaceAll(" ", "%20");
            }
        }
        if (counter>0){
            return "&LAYERS="+layerString;
        } else return "";
    }
    private String getStylesString(Vector wmsLayers){
        String stylesString="";
        int counter=0;
        Iterator it=wmsLayers.iterator();
        while (it.hasNext()){
            Object o=it.next();
            if (o instanceof WMSLayer&& ((WMSLayer)o).getSelectedStyle()!=null && ((WMSLayer)o).isEnabled()) {
                counter++;
                if (counter>1) {
                    stylesString+=",";
                }
                stylesString+=((WMSLayer)o).getSelectedStyle().getName().replaceAll(" ", "%20");
            }
        }
//        if (counter>0){
//            return "&STYLES="+stylesString;
//        } else return "";
        return "&STYLES="+stylesString; //LDS Bugfix
    }
    
    
    public WMSCapabilities getWmsCapabilities() {
        return wmsCapabilities;
    }
    
    public void setWmsCapabilities(WMSCapabilities wmsCapabilities) {
        this.wmsCapabilities = wmsCapabilities;
        
    }
    
    public void setPNode(PNode imageObject) {
        boolean vis=imageObject.getVisible();
        this.imageObject=imageObject;
        imageObject.setVisible(vis);
    }
    
    public PNode getPNode() {
        return imageObject;
    }
    
    
    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Getter for property progress.
     * @return Value of property progress.
     */
    public int getProgress() {
        
        return this.progress;
    }
    
    /**
     * Setter for property progress.
     * @param progress New value of property progress.
     */
    public void setProgress(int progress) {
        
        int oldProgress = this.progress;
        this.progress = progress;
        propertyChangeSupport.firePropertyChange("progress", new Integer(oldProgress), new Integer(progress));
    }
    
    public boolean isRefreshNeeded() {
        return refreshNeeded;
    }
    
    public void setRefreshNeeded(boolean refreshNeeded) {
        this.refreshNeeded = refreshNeeded;
    }
    
    public boolean hasErrors() {
        if (errorObject==null) {
            return false;
        } else {
            return true;
        }
    }
    
    public void setErrorObject(Object o) {
        errorObject=o;
    }
    public Object getErrorObject() {
        return errorObject;
    }
    
    public String getCapabilitiesUrl() {
        return capabilitiesUrl;
    }
    
    public void setCapabilitiesUrl(String capabilitiesUrl) {
        this.capabilitiesUrl = capabilitiesUrl;
    }
    
    
    public Element getElement() {
        Element layerConf=new Element("WMSServiceLayer");
        layerConf.setAttribute("name",getName());
        layerConf.setAttribute("visible",new Boolean(getPNode().getVisible()).toString());
        layerConf.setAttribute("enabled",new Boolean(isEnabled()).toString());
        layerConf.setAttribute("translucency",new Float(getTranslucency()).toString());
        layerConf.setAttribute("bgColor",getBackgroundColor());
        layerConf.setAttribute("imageFormat",getImageFormat());
        layerConf.setAttribute("exceptionFormat",getExceptionsFormat());
        CapabilityLink capLink=new CapabilityLink(CapabilityLink.OGC,getCapabilitiesUrl(),false);
        layerConf.addContent(capLink.getElement());
        Iterator lit=getWMSLayers().iterator();
        while (lit.hasNext()) {
            Object elem = lit.next();
            if (elem instanceof WMSLayer) {
                WMSLayer wmsLayer=(WMSLayer)elem;
                Element wmsLayerConf=new Element("wmsLayer");
                wmsLayerConf.setAttribute("name",wmsLayer.getOgcCapabilitiesLayer().getName());
                wmsLayerConf.setAttribute("enabled",new Boolean(wmsLayer.isEnabled()).toString());
                try {wmsLayerConf.setAttribute("style",wmsLayer.getSelectedStyle().getName());} catch (Exception e){}
                wmsLayerConf.setAttribute("info",new Boolean(wmsLayer.isQuerySelected()).toString());
                layerConf.addContent(wmsLayerConf);
            }
        }
        return layerConf;
    }
    
    private Layer searchForLayer(Layer layer,String name) {
        if (layer.getName()!=null&&layer.getName().equals(name)) {
            return layer;
        } else {
            Layer[] lArr=layer.getLayer();
            for (int i = 0; i < lArr.length; i++) {
                Layer l=searchForLayer(lArr[i],name);
                if (l!=null) {
                    return l;
                }
            }
            return null;
        }
    }
    
    public Object clone() {
        WMSServiceLayer w=null;
        if (treePaths!=null) {
            w=new WMSServiceLayer(treePaths);
        } else {
            w=new WMSServiceLayer(wmsServiceLayerElement,capabilities);
        }
        w.bb=bb;
        w.capabilitiesUrl=capabilitiesUrl;
        w.enabled=enabled;
        w.errorObject=errorObject;
        w.exceptionsFormat=exceptionsFormat;
        w.height=height;
        w.imageFormat=imageFormat;
        w.imageObject=imageObject;
        w.ir=new ImageRetrieval(w);
        w.layerPosition=layerPosition;
        w.listeners=new Vector(listeners);
        w.name=name;
        w.ogcLayers=ogcLayers;
        w.progress=progress;
        w.propertyChangeSupport=propertyChangeSupport;
        w.refreshNeeded=refreshNeeded;
        w.srs=srs;
        w.translucency=translucency;
        w.transparentImage=transparentImage;
        w.treePaths=treePaths;
        w.width=width;
        w.wmsCapabilities=wmsCapabilities;
        w.wmsLayers=wmsLayers;
        w.wmsServiceLayerElement=wmsServiceLayerElement;
        return w;
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The <code>equals</code> method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     <code>x</code>, <code>x.equals(x)</code> should return
     *     <code>true</code>.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     <code>x</code> and <code>y</code>, <code>x.equals(y)</code>
     *     should return <code>true</code> if and only if
     *     <code>y.equals(x)</code> returns <code>true</code>.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     <code>x</code>, <code>y</code>, and <code>z</code>, if
     *     <code>x.equals(y)</code> returns <code>true</code> and
     *     <code>y.equals(z)</code> returns <code>true</code>, then
     *     <code>x.equals(z)</code> should return <code>true</code>.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     <code>x</code> and <code>y</code>, multiple invocations of
     *     <tt>x.equals(y)</tt> consistently return <code>true</code>
     *     or consistently return <code>false</code>, provided no
     *     information used in <code>equals</code> comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value <code>x</code>,
     *     <code>x.equals(null)</code> should return <code>false</code>.
     * </ul>
     * <p>
     * The <tt>equals</tt> method for class <code>Object</code> implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values <code>x</code> and
     * <code>y</code>, this method returns <code>true</code> if and only
     * if <code>x</code> and <code>y</code> refer to the same object
     * (<code>x == y</code> has the value <code>true</code>).
     * <p>
     * Note that it is generally necessary to override the <tt>hashCode</tt>
     * method whenever this method is overridden, so as to maintain the
     * general contract for the <tt>hashCode</tt> method, which states
     * that equal objects must have equal hash codes.
     *
     *
     * @param obj   the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    public boolean equals(Object obj) {
        //TODO Dieses equals wird 10000 mal aufgerufen
        if (obj instanceof WMSServiceLayer) {
            WMSServiceLayer tester=(WMSServiceLayer)obj;
            return (getName()+getGetMapPrefix()+getLayersString(wmsLayers)+getStylesString(wmsLayers)).equals(
                    tester.getName()+tester.getGetMapPrefix()+tester.getLayersString(tester.wmsLayers)+tester.getStylesString(tester.wmsLayers)
                    );
        } else {
            return false;
        }
    }
    
    
    
    
}
