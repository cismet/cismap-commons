/*
 * SimpleWMS.java
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
 * Created on 22. Juni 2005, 13:52
 *
 */
package de.cismet.cismap.commons.raster.wms.simple;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import edu.umd.cs.piccolo.PNode;
import org.jdom.Attribute;
import org.jdom.Element;
import java.util.Vector;
import org.apache.commons.httpclient.HttpClient;
import org.jdom.CDATA;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimpleWMS extends AbstractWMS implements MapService,RasterMapService, RetrievalServiceLayer {//implements RasterService,RetrievalListener,ServiceLayer {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private SimpleWmsGetMapUrl gmUrl;
    private ImageRetrieval ir;
    private PNode pNode;
    private String name = "SimpleWMS";//NOI18N
    private HttpClient preferredClient=null;

    /**
     * This constructor is used by the TileLayer
     * Creates a new instance of SimpleWMS 
     */
    public SimpleWMS() {
    }


    /**
     * Creates a new instance of SimpleWMS
     */
    public SimpleWMS(SimpleWmsGetMapUrl gmUrl) {
        this.gmUrl = gmUrl;
    }

    public SimpleWMS(SimpleWMS s) {
        this(s.gmUrl);
        bb = (BoundingBox) s.bb.clone();
        enabled = s.enabled;
        height = s.height;
        layerPosition = s.layerPosition;
        name = s.name;
        pNode = s.pNode;
        translucency = s.translucency;
        width = s.width;
        ir = new ImageRetrieval(s);
        listeners = new Vector();
        listeners.addAll(s.listeners);
    }

    public SimpleWMS(Element object) throws Exception {

        String urlTemplate = object.getTextTrim();
        SimpleWmsGetMapUrl url = new SimpleWmsGetMapUrl(urlTemplate);
        gmUrl = url;
        Attribute layerPositionAttr = object.getAttribute("layerPosition");//NOI18N
        if (layerPositionAttr != null) {
            try {
                layerPosition = layerPositionAttr.getIntValue();
            } catch (Exception e) {
            }
        }
        Attribute enabledAttr = object.getAttribute("enabled");//NOI18N
        if (enabledAttr != null) {
            try {
                enabled = enabledAttr.getBooleanValue();
            } catch (Exception e) {
            }
        }
        Attribute nameAttr = object.getAttribute("name");//NOI18N
        if (nameAttr != null) {
            try {
                name = nameAttr.getValue();
            } catch (Exception e) {
            }
        }
        Attribute translucencyAttr = object.getAttribute("translucency");//NOI18N
        if (translucencyAttr != null) {
            try {
                setTranslucency(translucencyAttr.getFloatValue());
            } catch (Exception e) {
            }


        }


    }

    public Element getElement() {
        Element element = new Element("simpleWms");//NOI18N
        element.setAttribute("layerPosition", new Integer(layerPosition).toString());//NOI18N
        element.setAttribute("skip", "false");//NOI18N
        element.setAttribute("enabled", new Boolean(enabled).toString());//NOI18N
        element.setAttribute("name", name);//NOI18N
        element.setAttribute("translucency", new Float(translucency).toString());//NOI18N
        CDATA data = new CDATA(gmUrl.getUrlTemplate());
        element.addContent(data);
        return element;


    }

    public SimpleWMS(SimpleWmsGetMapUrl gmUrl, int layerPosition, boolean enabled, boolean canbeDisabled, String name) {
        this.gmUrl = gmUrl;
    }

    public synchronized void retrieve(boolean forced) {
        log.debug("retrieve()");//NOI18N
        gmUrl.setHeight(height);
        gmUrl.setWidth(width);
        gmUrl.setX1(bb.getX1());
        gmUrl.setY1(bb.getY1());
        gmUrl.setX2(bb.getX2());
        gmUrl.setY2(bb.getY2());
        if (ir != null && ir.isAlive() && ir.getUrl().equals(gmUrl.toString()) && !forced) {
            //mach nix 
            //mehrfachaufruf mit der gleichen url = unsinn
            log.debug("multiple invocations with the same url = humbug");//NOI18N
        } else {
            if (ir != null && ir.isAlive()) {
                ir.youngerWMSCall();
                ir.interrupt();
                retrievalAborted(new RetrievalEvent());
//                try {
//                    ir.join();   
//                }
//                catch (InterruptedException iex){
//                    log.warn("ir.join() wurde unterbrochen",iex);
//                }
            }
            ir = new ImageRetrieval(this);
            ir.setPreferredHttpClient(preferredClient);
            ir.setUrl(gmUrl.toString());
            log.debug("ir.start();");//NOI18N
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }
    }

    public SimpleWmsGetMapUrl getGmUrl() {
        return gmUrl;
    }

    public void setGmUrl(SimpleWmsGetMapUrl gmUrl) {
        this.gmUrl = gmUrl;
    }

    public void setPNode(PNode imageObject) {
        pNode = imageObject;
    }

    public PNode getPNode() {
        return pNode;
    }

    public Object clone() {
        return new SimpleWMS(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    }

    public HttpClient getPreferredClient() {
        return preferredClient;
    }

    public void setPreferredClient(HttpClient preferredClient) {
        this.preferredClient = preferredClient;
    }
    
}
