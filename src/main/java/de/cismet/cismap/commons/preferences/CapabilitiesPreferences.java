/*
 * CapabilitiesPreferences.java
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
 * Created on 2. M\u00E4rz 2006, 15:37
 *
 */

package de.cismet.cismap.commons.preferences;

import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CapabilitiesPreferences {
    
    final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private TreeMap<Integer, CapabilityLink> capabilities=new TreeMap<Integer, CapabilityLink>();
    private TreeMap<Integer, CapabilityLink> capabilitiesList=new TreeMap<Integer, CapabilityLink>();
    public CapabilitiesPreferences() {
    }
    /** Creates a new instance of CapabilitiesPreferences */
    public CapabilitiesPreferences(Element serverParent,Element localParent) {
        Element serverRoot=serverParent.getChild("cismapCapabilitiesPreferences");
        Element clientRoot=localParent.getChild("cismapCapabilitiesPreferences");
        List caps=clientRoot.getChildren("capabilities");
        Iterator<Element> it=caps.iterator();
        int counter=0;
        while (it.hasNext()) {
            try {
                Element elem =it.next();
                String type=elem.getAttribute("type").getValue();
                String link=elem.getTextTrim();
                String subparent=elem.getAttributeValue("subparent");
                boolean active=false;
                try {active=elem.getAttribute("active").getBooleanValue();}catch(Exception unhandled) {}
                capabilities.put(new Integer(counter++),new CapabilityLink(type,link,active,subparent));
            } catch (Throwable t) {
                log.warn("Fehler beim Auslesen der CapabilityPreferences.",t);
            }
        }
        List capsList=serverRoot.getChildren("capabilitiesList");
        Iterator<Element> itList=capsList.iterator();
        int listCounter=0;
        while (itList.hasNext()) {
            try {
                Element elem =itList.next();
                String type=elem.getAttribute("type").getValue();
                String link=elem.getTextTrim();
                String subparent=elem.getAttributeValue("subparent");
                capabilitiesList.put(new Integer(listCounter++),new CapabilityLink(type,link,elem.getAttribute("titlestring").getValue(),subparent));
            } catch (Throwable t) {
                log.warn("Fehler beim Auslesen der CapabilityListPreferences.",t);
            }
        }        
    }
 
    public TreeMap<Integer, CapabilityLink> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(TreeMap<Integer, CapabilityLink> capabilities) {
        this.capabilities = capabilities;
    }

    public TreeMap<Integer, CapabilityLink> getCapabilitiesList() {
        return capabilitiesList;
    }

    public void setCapabilitiesList(TreeMap<Integer, CapabilityLink> capabilitiesList) {
        this.capabilitiesList = capabilitiesList;
    }
}
