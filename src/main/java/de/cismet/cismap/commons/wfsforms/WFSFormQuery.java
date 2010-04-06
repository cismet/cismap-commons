/*
 * WFSFormQuery.java
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
 * Created on 24. Juli 2006, 11:17
 *
 */

package de.cismet.cismap.commons.wfsforms;

import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WFSFormQuery {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String INITIAL="INITIAL";//NOI18N
    public static final String FOLLOWUP="FOLLOWUP";//NOI18N
    
    private String serverUrl;
    private String filename;
    private String wfsQueryString;
    private String title;
    private String id;
    private String displayTextProperty;
    
    private String propertyPrefix;
    private String propertyNamespace;
    private String idProperty;
    private String extentProperty;
    private String positionProperty;
    private String type;
    private String componentName;
    private String queryPlaceholder;
    
    
    /** Creates a new instance of WFSFormQuery */
    public WFSFormQuery() {
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDisplayTextProperty() {
        return displayTextProperty;
    }
    
    public void setDisplayTextProperty(String displayTextProperty) {
        this.displayTextProperty = displayTextProperty;
    }
    
    public String getIdProperty() {
        return idProperty;
    }
    
    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }
    
    public String getExtentProperty() {
        return extentProperty;
    }
    
    public void setExtentProperty(String positionProperty) {
        this.extentProperty = extentProperty;
    }
    public String getPositionProperty() {
        return positionProperty;
    }
    
    public void setPositionProperty(String positionProperty) {
        this.positionProperty = positionProperty;
    }
    
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    public String getQueryPlaceholder() {
        return queryPlaceholder;
    }
    
    public void setQueryPlaceholder(String queryPlaceholder) {
        this.queryPlaceholder = queryPlaceholder;
    }
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public String getWfsQueryString() {
        return wfsQueryString;
    }
    
    public void setWfsQueryString(String wfsQueryString) {
        this.wfsQueryString = wfsQueryString;
    }
    
    public Element getElement() {
        Element ret=new Element("wfsFormQuery");//NOI18N
        ret.setAttribute("id",getId());//NOI18N
        ret.setAttribute("title",getTitle());//NOI18N
        ret.setAttribute("server",getServerUrl());//NOI18N
        ret.setAttribute("queryFile",getFilename());//NOI18N
         ret.setAttribute("propertyPrefix",getFilename());//NOI18N
          ret.setAttribute("propertyNamespace",getFilename());//NOI18N
        ret.setAttribute("displayTextProperty",getDisplayTextProperty());//NOI18N
        ret.setAttribute("extentProperty",getExtentProperty());//NOI18N
        ret.setAttribute("positionProperty",getExtentProperty());//NOI18N
        ret.setAttribute("idProperty",getIdProperty());//NOI18N
        ret.setAttribute("type",getType());//NOI18N
        ret.setAttribute("componentName",getComponentName());//NOI18N
        if (getQueryPlaceholder()!=null) {
            ret.setAttribute("queryPlaceholder",getQueryPlaceholder());//NOI18N
        }
        return ret;
    }

    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    public void setPropertyPrefix(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    public String getPropertyNamespace() {
        return propertyNamespace;
    }

    public void setPropertyNamespace(String propertyNamespace) {
        this.propertyNamespace = propertyNamespace;
    }
    
}
