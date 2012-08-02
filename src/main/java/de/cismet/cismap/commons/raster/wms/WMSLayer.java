/*
 * WMSLayer.java
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
 * Created on 22. November 2005, 12:51
 *
 */

package de.cismet.cismap.commons.raster.wms;
import org.deegree.services.wms.capabilities.Layer;
import org.deegree.services.wms.capabilities.Style;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WMSLayer {
    private boolean enabled;
    private Style selectedStyle;
    private boolean querySelected;
    private Layer ogcCapabilitiesLayer;
    private WMSServiceLayer parentServiceLayer=null;
    
    /** Creates a new instance of WMSLayer */
    public WMSLayer(Layer ogcCapabilitiesLayer,Style selectedStyle) {
        this.ogcCapabilitiesLayer=ogcCapabilitiesLayer;
        this.selectedStyle=selectedStyle;
        setEnabled(true);
        querySelected=false;
        this.selectedStyle=selectedStyle;
    }
    
    public String toString() {
        if (ogcCapabilitiesLayer!=null) {
            return ogcCapabilitiesLayer.getTitle();
        }
        else {
            return super.toString();
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Style getSelectedStyle() {
        return selectedStyle;
    }

    public void setSelectedStyle(Style selectedStyle) {
        this.selectedStyle = selectedStyle;
    }

    public boolean isQuerySelected() {
        return querySelected;
    }

    public void setQuerySelected(boolean querySelected) {
        this.querySelected = querySelected;
    }

    public Layer getOgcCapabilitiesLayer() {
        return ogcCapabilitiesLayer;
    }

    public void setOgcCapabilitiesLayer(Layer ogcCapabilitiesLayer) {
        this.ogcCapabilitiesLayer = ogcCapabilitiesLayer;
    }
    
    public boolean isSrsSupported(String srs) {
        return ogcCapabilitiesLayer.isSrsSupported(srs);
    }

    public WMSServiceLayer getParentServiceLayer() {
        return parentServiceLayer;
    }

    public void setParentServiceLayer(WMSServiceLayer parentServiceLayer) {
        this.parentServiceLayer = parentServiceLayer;
    }
    
    
}
