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

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.deegree2.framework.xml.schema.ElementDeclaration;
import org.deegree2.ogcwebservices.wfs.capabilities.WFSCapabilities;

/**
 * Der WFSCapabilitiesTreeCellRenderer bestimmt, wie ein WFSCapabilitiesTree-Knoten
 * dargestellt wird.
 * @author nh
 */
public class WFSCapabilitiesTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.raster.wfs.WFSCapabilitiesTreeCellRenderer");
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
            // if node is a WFSCapabilities --> root
            if (value instanceof WFSCapabilities) {
                if (altRootName != null) {
                    setText(altRootName);
                } else {
                    setText("Web Feature Service");
                }
                setIcon(serverIcon);
            } else if (value instanceof ElementDeclaration) {
                ElementDeclaration e = (ElementDeclaration) value;
//                if (!e.getType().isResolved()) {
//                    if (e.getType().getName().getLocalName().equals(FeatureServiceUtilities.STRING_PROPERTY_TYPE)) {
//                        setIcon(stringIcon);
//                    } else if (e.getType().getName().getLocalName().equals(FeatureServiceUtilities.INTEGER_PROPERTY_TYPE)) {
//                        setIcon(integerIcon);
//                    } else if (e.getType().getName().getLocalName().equals(FeatureServiceUtilities.GEO_PROPERTY_TYPE)) {
//                        setIcon(geomIcon);
//                    } else {
//
//                    }
//                } else {
//                    setIcon(featureIcon);
//                }
                setIcon(featureIcon);
                setText(e.getName().getLocalName());
            } else if (value instanceof FeatureServiceAttribute) {
                FeatureServiceAttribute fsf = (FeatureServiceAttribute) value;
                String type;
                if(fsf.getType() != null && fsf.getType().lastIndexOf(":") != -1){
                    type = fsf.getType().substring(fsf.getType().lastIndexOf(":")+1);
                } else {
                    type = fsf.getType();
                }
                if (type.equals(FeatureServiceUtilities.STRING_PROPERTY_TYPE)) {
                    setIcon(stringIcon);
                } else if (type.equals(FeatureServiceUtilities.INTEGER_PROPERTY_TYPE)) {
                    setIcon(integerIcon);
                } else if (type.equals(FeatureServiceUtilities.GEO_PROPERTY_TYPE)) {
                    setIcon(geomIcon);
                } else {
                    setIcon(elementIcon);
                }
                if(fsf.getName() != null && fsf.getName().lastIndexOf(":") != -1){
                    setText(fsf.getName().substring(fsf.getName().lastIndexOf(":")+1));
                } else {
                    setText(fsf.getName());
                }
            }
        } catch (Exception ex) {
            log.error("Fehler im WFSCapabilitiesTreeCellRenderer", ex);
        }
        return this;
    }
}
