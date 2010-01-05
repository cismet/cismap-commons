/*
 * SimpleFeatureSupporterRasterServiceUrl.java
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
 * Created on 18. Juli 2006, 16:12
 *
 */

package de.cismet.cismap.commons.raster.wms.featuresupportlayer;

import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimpleFeatureSupporterRasterServiceUrl extends SimpleWmsGetMapUrl{
     public static String FILTER_TOKEN="<cismap:filterString>";
     private String filterToken;
     private String filter;
     public SimpleFeatureSupporterRasterServiceUrl(String urlTemplate) {
         super(urlTemplate);
         filterToken=FILTER_TOKEN;
     }
    public String toString() {
        String retValue;
        retValue = super.toString();
        retValue=retValue.replaceAll(filterToken, filter);
        return retValue;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
    
    public static void main(String[] args){
        SimpleFeatureSupporterRasterServiceUrl u=new SimpleFeatureSupporterRasterServiceUrl("http://s102w2k1.wuppertal-intra.de/wunda_dk_v61/isserver/ims/scripts/ShowMap.pl?datasource=erhebungsflaechen&VERSION=1.1.1&REQUEST=GetMap&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_inimage&LAYERS=09_2&STYLES=farbe_altabl&<cismap:filterString>");
        u.setFilter("Testfilter");
        u.setX1(0.1);
        u.setX2(0.2);
        u.setY1(0.3);
        u.setY2(0.4);
        u.setHeight(1);
        u.setWidth(1000);
        System.out.println(u);
    }
    
    public boolean equals(Object o) {
        return o instanceof SimpleFeatureSupporterRasterServiceUrl && ((SimpleFeatureSupporterRasterServiceUrl)o).urlTemplate.equals(this.urlTemplate);
    }
    
    public int hashCode() {
        return this.urlTemplate.hashCode();
    }
    
}

