/*
 * SelectionAndCapabilities.java
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
 * Created on 29. November 2005, 10:44
 *
 */
package de.cismet.cismap.commons.gui.capabilitywidget;

import org.jdom.Element;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import java.util.Vector;

/**
 *
 * @author nh
 */
public class WFSSelectionAndCapabilities {
    private String name, host, id;
    private Element query;
    private Vector<FeatureServiceAttribute> attributes;

    // TODO WFS Attribute rausschmeissen
    public WFSSelectionAndCapabilities(String name, String host, Element query,
            String id, Vector<FeatureServiceAttribute> attributes) {
        this.name = name;
        this.host = host;
        this.query = query;
        this.id = id;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Element getQuery() {
        return query;
    }

    public void setQuery(Element query) {
        this.query = query;
    }
    
    public void setIdentifier(String id) {
        this.id = id;
    }
    
    public String getIdentifier() {
        return id;
    }

    public Vector<FeatureServiceAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Vector<FeatureServiceAttribute> attributes) {
        this.attributes = attributes;
    }
}
