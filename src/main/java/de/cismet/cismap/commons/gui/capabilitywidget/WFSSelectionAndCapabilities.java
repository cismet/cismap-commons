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
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import java.util.Vector;

/**
 * TODO: Diese Klasse kann entfernt werden, da sie keine
 * zusaetzliche Funktionalitaet zur Klasse FeatureType besitzt.
 * @author nh
 */
public class WFSSelectionAndCapabilities {
    private FeatureType feature;

    public WFSSelectionAndCapabilities(FeatureType feature) {
        this.feature = feature;
    }

    public String getName() {
        return feature.getPrefixedNameString();
    }

    public String getHost() {
        return feature.getWFSCapabilities().getURL().toString();
    }

    public Element getQuery() {
        return feature.getWFSQuery();
    }

    public String getIdentifier() {
        return "";
    }

    public FeatureType getFeature() {
        return feature;
    }


    public Vector<FeatureServiceAttribute> getAttributes() {
        return feature.getFeatureAttributes();
    }
}
