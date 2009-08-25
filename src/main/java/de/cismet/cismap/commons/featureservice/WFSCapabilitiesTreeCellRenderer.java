/*
 * WMSCapabilitiesTreeCellRenderer.java
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
 * Created on 14. Oktober 2005, 13:16
 *
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.raster.wms.*;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.deegree2.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree2.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.jdom.Element;

/**
 * Der WFSCapabilitiesTreeCellRenderer bestimmt, wie ein WFSCapabilitiesTree-Knoten
 * dargestellt wird.
 * @author nh
 */
public class WFSCapabilitiesTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.raster.wfs.WFSCapabilitiesTreeCellRenderer");
    private static final String STRING = "xsd:string";
    private static final String INTEGER = "xsd:integer";
    private static final String GEOM = "gml:GeometryPropertyType";
    private ImageIcon serverIcon;
    private ImageIcon featureIcon;
    private ImageIcon elementIcon;
    private ImageIcon stringIcon;
    private ImageIcon integerIcon;
    private ImageIcon geomIcon;
    private String altRootName = null;

    /**
     * Creates a new instance of WMSCapabilitiesTreeCellRenderer
     */
    public WFSCapabilitiesTreeCellRenderer() {
        serverIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/server.png"));
        featureIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/layer.png"));
        elementIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/attr.png"));
        stringIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/string.png"));
        integerIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/integer.png"));
        geomIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/geom.png"));
    }
    
    public WFSCapabilitiesTreeCellRenderer(String name) {
        this();
        this.altRootName = name;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

        try {
            // wenn Knoten == WFSCapabilities --> Root
            if (value instanceof WFSCapabilities) {
                if (altRootName != null) {
                    setText(altRootName);
                } else {
                    setText("Web Feature Service");
                }
                setIcon(serverIcon);
            }
            // bleiben nur noch Element-Knoten \u00FCbrig
            else {
                Element e = (Element) value;
                if (e.getChildren().size() == 0) {
                    if (e.getAttributeValue("type").equals(STRING)) {
                        setIcon(stringIcon);
                    } else if (e.getAttributeValue("type").equals(INTEGER)) {
                        setIcon(integerIcon);
                    } else if (e.getAttributeValue("type").equals(GEOM)) {
                        setIcon(geomIcon);
                    } else {
                        setIcon(elementIcon);
                    }
                } else {
                    setIcon(featureIcon);
                }
                setText(WFSOperator.deleteApp(e.getAttributeValue("name")));
            }
        } catch (Exception ex) {
            log.error("Fehler im WFSCapabilitiesTreeCellRenderer", ex);
        }
        return this;
    }
}
