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

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CapabilitiesPreferences {
    
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CapabilitiesPreferences.class);    
    private TreeMap<Integer, CapabilityLink> capabilities=new TreeMap<Integer, CapabilityLink>();
    private CapabilitiesListTreeNode capabilitiesListTree;
    public CapabilitiesPreferences() {
    }
    /** Creates a new instance of CapabilitiesPreferences */
    public CapabilitiesPreferences(Element serverParent,Element localParent) {
        Element serverRoot=serverParent.getChild("cismapCapabilitiesPreferences");//NOI18N
        Element clientRoot=localParent.getChild("cismapCapabilitiesPreferences");//NOI18N
        List caps=clientRoot.getChildren("capabilities");//NOI18N
        Iterator<Element> it=caps.iterator();
        int counter=0;
        while (it.hasNext()) {
            try {
                Element elem =it.next();
                String type=elem.getAttribute("type").getValue();//NOI18N
                String link=elem.getTextTrim();
                String subparent=elem.getAttributeValue("subparent");//NOI18N
                boolean active=false;
                try {active=elem.getAttribute("active").getBooleanValue();}catch(Exception unhandled) {}//NOI18N
                capabilities.put(new Integer(counter++),new CapabilityLink(type,link,active,subparent));
            } catch (Throwable t) {
                log.warn("Error while reading the CapabilityPreferences.",t);//NOI18N
            }
        }

        // capabilitiesList auslesen und in Baum speichern
        capabilitiesListTree = createCapabilitiesListTreeNode(null, serverRoot);
    }

    /**
     * Erzeugt rekursiv aus einem JDom-Element einen CapabilitiesList-Knoten
     * samt CapabilitiesList und Unterknoten.
     *
     * @param nodetitle  Title des CapabilitiesList-Knotens
     * @param element JDom-Element
     * @return CapabilitiesList-Knoten
     */
    private static CapabilitiesListTreeNode createCapabilitiesListTreeNode(String nodetitle, Element element) {
        CapabilitiesListTreeNode node = new CapabilitiesListTreeNode();
        int listCounter = 0;

        node.setTitle(nodetitle);
        
        TreeMap<Integer, CapabilityLink> capabilitiesList = new TreeMap<Integer, CapabilityLink>();
        for (Element elem : (List<Element>)element.getChildren("capabilitiesList")) {//NOI18N
            try {
                String type = elem.getAttribute("type").getValue();//NOI18N
                String title = elem.getAttribute("titlestring").getValue();//NOI18N

                if (type.equals(CapabilityLink.MENU)) {
                    // Unterknoten erzeugen
                    node.addSubnode(createCapabilitiesListTreeNode(title, elem));
                } else {
                    // CapabilitiesList-Eintrag erzeugen
                    String link = elem.getTextTrim();
                    String subparent = elem.getAttributeValue("subparent");//NOI18N
                    capabilitiesList.put(new Integer(listCounter++), new CapabilityLink(type, link, title, subparent));
                }
            } catch (Throwable t) {
                log.warn("Error while reading the CapabilityListPreferences.", t);//NOI18N
            }
        }

        // CapabilitiesList
        node.setCapabilitiesList(capabilitiesList);

        // fertig
        return node;
    }
 
    public TreeMap<Integer, CapabilityLink> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(TreeMap<Integer, CapabilityLink> capabilities) {
        this.capabilities = capabilities;
    }

    public CapabilitiesListTreeNode getCapabilitiesListTree() {
        return capabilitiesListTree;
    }

}
