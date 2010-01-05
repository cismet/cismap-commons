/*
 * CismapPreferences.java
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
 * Created on 16. August 2005, 11:30
 *
 */

package de.cismet.cismap.commons.preferences;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CismapPreferences {
    final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private LayersPreferences layersPrefs;
    private GlobalPreferences globalPrefs;
    private CapabilitiesPreferences capabilityPrefs;
    
    
    /**
     * Creates a new instance of CismapPreferences
     */
    public CismapPreferences(URL url) {
        try {
            SAXBuilder builder = new SAXBuilder(false);
            Document doc=builder.build(url);
            Element prefs=doc.getRootElement();
            readFromCismapPreferences(prefs);
        } catch (Exception e) {
            
        }
    }
    
    public CismapPreferences(Element cismapPreferences) {
        readFromCismapPreferences(cismapPreferences);
    }
    private void readFromCismapPreferences(Element cismapPreferences) {
        try {layersPrefs=new LayersPreferences(this, cismapPreferences.getChild("cismapLayersPreferences"));}
        catch (Exception e) {log.warn("Fehler beim Laden der LayersPreferences",e);}
        try {globalPrefs=new GlobalPreferences(cismapPreferences.getChild("cismapGlobalPreferences"));}
        catch (Exception e) {log.warn("Fehler beim Laden der GlobalPreferences",e);}
        try {capabilityPrefs=new CapabilitiesPreferences(cismapPreferences.getChild("cismapCapabilitiesPreferences"),cismapPreferences.getChild("cismapCapabilitiesPreferences"));}
        catch (Exception e) {log.warn("Fehler beim Laden der CapabilitiesPreferences",e);}
    }
    public LayersPreferences getLayersPrefs() {
        return layersPrefs;
    }
    
    public void setLayersPrefs(LayersPreferences layersPrefs) {
        this.layersPrefs = layersPrefs;
    }
    
    public GlobalPreferences getGlobalPrefs() {
        return globalPrefs;
    }
    
    public void setGlobalPrefs(GlobalPreferences globalPrefs) {
        this.globalPrefs = globalPrefs;
    }

    public CapabilitiesPreferences getCapabilityPrefs() {
        return capabilityPrefs;
    }

    public void setCapabilityPrefs(CapabilitiesPreferences capabilityPrefs) {
        this.capabilityPrefs = capabilityPrefs;
    }
    
}
