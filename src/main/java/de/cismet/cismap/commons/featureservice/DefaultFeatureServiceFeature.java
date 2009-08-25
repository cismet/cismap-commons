/*
 * DefaultFeatureServiceFeature.java
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
 * Created on 13. Juli 2005, 11:10
 *
 */

package de.cismet.cismap.commons.featureservice;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.rasterservice.MapService;
import java.util.HashMap;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class DefaultFeatureServiceFeature extends DefaultStyledFeature implements FeatureServiceFeature{
    private HashMap clientProperties=new HashMap();
    private String serviceInfo="";
    private String objectName="";
    private String groupingKey="";
    private String featureType="";
    private int id;
    private MapService mapService;
    
    
    /** Creates a new instance of DefaultFeatureServiceFeature */
    public DefaultFeatureServiceFeature() {
    }

    public Object getProperty(Object o) {
        return clientProperties.get(o);
    }

    public String getServiceInfo() {
        return serviceInfo;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getGroupingKey() {
        return groupingKey;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void addProperty(Object key, Object property) {
        clientProperties.put(key,property);
    }

    public HashMap getClientProperties() {
        return clientProperties;
    }

    public void setClientProperties(HashMap clientProperties) {
        this.clientProperties = clientProperties;
    }

    public void setServiceInfo(String serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setGroupingKey(String groupingKey) {
        this.groupingKey = groupingKey;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }
    public boolean canBeSelected() {
        return false;
    }
    public PureNewFeature createPureNewFeature() {
        //TODO hier muss ein FeatureServiceFeature hin
        PureNewFeature pnf=new PureNewFeature((Geometry)(getGeometry().clone()));
        return pnf;
    }

    public MapService getFeatureService() {
        return mapService;
    }

    public void setFeatureService(MapService featureService) {
        this.mapService = featureService;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

  
    
    
}
