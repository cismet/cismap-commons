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

package de.cismet.cismap.commons.raster.wms;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.deegree.services.wms.capabilities.Layer;
import org.deegree.services.wms.capabilities.Style;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WMSCapabilitiesTreeCellRenderer extends DefaultTreeCellRenderer {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private javax.swing.ImageIcon layersIcon;
    private javax.swing.ImageIcon layerIcon;
    private javax.swing.ImageIcon layersInfoIcon;
    private javax.swing.ImageIcon layerInfoIcon;
    private javax.swing.ImageIcon styleIcon;
    private javax.swing.ImageIcon unselectedStyleIcon;
    private javax.swing.ImageIcon serverIcon;
    private DefaultTreeCellRenderer defaultRenderer;
    private javax.swing.ImageIcon disabledLayersIcon;
    private javax.swing.ImageIcon disabledLayerIcon;
    private javax.swing.ImageIcon disabledLayerInfoIcon;

    
    /**
     * Creates a new instance of WMSCapabilitiesTreeCellRenderer
     */
    public WMSCapabilitiesTreeCellRenderer() {
        layersIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png"));//NOI18N
        layerIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layer.png"));//NOI18N
        layersInfoIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers_i.png"));//NOI18N
        layerInfoIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layer_i.png"));//NOI18N
        styleIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/style.png"));//NOI18N
        unselectedStyleIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/style.png"));//NOI18N
        serverIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"));//NOI18N

        disabledLayersIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/disabledLayers.png"));//NOI18N
        disabledLayerIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/disabledLayer.png"));//NOI18N
        disabledLayerInfoIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/disabledLayer_i.png"));//NOI18N
    }
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, isSelected,
                expanded, leaf, row,
                hasFocus);
        //setForeground(Color.black);
        
        
        if (value instanceof Layer) {
            Layer tmpLayer=(Layer)value;
            boolean rightScale=true;
            
            try {
                double mxScale=tmpLayer.getScaleHint().getMax();
                double mnScale=tmpLayer.getScaleHint().getMin();
                double scale=CismapBroker.getInstance().getMappingComponent().getCurrentOGCScale();
                if (scale<mnScale||scale>mxScale) {
                    if (!isSelected) {
                        setForeground(Color.GRAY);
                    }
                    rightScale=false;
                    // setTooltip("Im momentanen Ma\u00DFstab nicht darstellbar");
                }
            } catch (Exception e) {
                //TODO logging last
                //log.debug("Fehler bei der ScaleHint Verarbeitung.Kein Problem",e);
            }
            setText(tmpLayer.getTitle());
            if (tmpLayer.isQueryable()) {
                if (tmpLayer.getLayer().length==0) {
                    if (rightScale) {
                        setIcon(layerInfoIcon);
                    }
                    else {
                        setIcon(disabledLayerInfoIcon);
                    }
                } else {
                    if (rightScale) {
                        setIcon(layersInfoIcon);
                    }
                    else {
                        //TODO
                        setIcon(layersInfoIcon);
                    }
                }
            } else {
                if (tmpLayer.getLayer().length==0) {
                    if (rightScale) {
                        setIcon(layerIcon);
                    }
                    else {
                        setIcon(disabledLayerIcon);
                    }
                } else {
                    if (rightScale) {
                        setIcon(layersIcon);
                    }
                    else {
                        setIcon(disabledLayersIcon);
                    }
                }
            }
        } else if (value instanceof Style) {
            Style tmpStyle=(Style)value;
            if (tmpStyle.getTitle()!=null&&tmpStyle.getTitle().trim().length()>0) {
                setText(tmpStyle.getTitle());
            } else {
                setText(tmpStyle.getName());
            }
            setIcon(styleIcon);
        }
        
        
        return this;
    }
}
