/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.raster.tms;

import de.cismet.cismap.commons.raster.tms.tmscapability.TMSCapabilities;
import de.cismet.cismap.commons.raster.tms.tmscapability.TileSet;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author cschmidt
 */
public class TMSCapabilitiesTreeCellRenderer extends DefaultTreeCellRenderer{
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
    
    
    public TMSCapabilitiesTreeCellRenderer() {
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
    
    
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, isSelected,
                expanded, leaf, row,
                hasFocus);
        //setForeground(Color.black);
        
        if (value instanceof TMSCapabilities) {
            TMSCapabilities tmsCaps = (TMSCapabilities)value;
            setText(tmsCaps.getName());
            setIcon(layerIcon);

        } else if (value instanceof TileSet) {
            TileSet tileSet = (TileSet)value;
            setText(tileSet.getLayer());
            setIcon(styleIcon);
        }
        
        
        return this;
    }

}
