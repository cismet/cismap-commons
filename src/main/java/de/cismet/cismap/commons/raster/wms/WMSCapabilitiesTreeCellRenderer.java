/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.wms.capabilities.Layer;
import de.cismet.commons.wms.capabilities.Style;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WMSCapabilitiesTreeCellRenderer extends DefaultTreeCellRenderer {

    //~ Instance fields --------------------------------------------------------

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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WMSCapabilitiesTreeCellRenderer.
     */
    public WMSCapabilitiesTreeCellRenderer() {
        layersIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers.png"));   // NOI18N
        layerIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer.png"));    // NOI18N
        layersInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers_i.png")); // NOI18N
        layerInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer_i.png"));  // NOI18N
        styleIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/style.png"));    // NOI18N
        unselectedStyleIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/style.png"));    // NOI18N
        serverIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/server.png"));   // NOI18N

        disabledLayersIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/disabledLayers.png"));  // NOI18N
        disabledLayerIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/disabledLayer.png"));   // NOI18N
        disabledLayerInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/disabledLayer_i.png")); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
            final Object value,
            final boolean isSelected,
            final boolean expanded,
            final boolean leaf,
            final int row,
            final boolean hasFocus) {
        super.getTreeCellRendererComponent(
            tree,
            value,
            isSelected,
            expanded,
            leaf,
            row,
            hasFocus);
        // setForeground(Color.black);

        if (value instanceof Layer) {
            final Layer tmpLayer = (Layer)value;
            boolean rightScale = true;

            try {
                final double mxScale = tmpLayer.getScaleDenominationMax();
                final double mnScale = tmpLayer.getScaleDenominationMin();
                final double scale = CismapBroker.getInstance().getMappingComponent().getCurrentOGCScale();
                if ((scale < mnScale) || (scale > mxScale)) {
                    if (!isSelected) {
                        setForeground(Color.GRAY);
                    }
                    rightScale = false;
                    // setTooltip("Im momentanen Ma\u00DFstab nicht darstellbar");
                }
            } catch (Exception e) {
                // TODO logging last
                // log.debug("Fehler bei der ScaleHint Verarbeitung.Kein Problem",e);
            }
            setText(tmpLayer.getTitle());
            if (tmpLayer.isQueryable()) {
                if (tmpLayer.getChildren().length == 0) {
                    if (rightScale) {
                        setIcon(layerInfoIcon);
                    } else {
                        setIcon(disabledLayerInfoIcon);
                    }
                } else {
                    if (rightScale) {
                        setIcon(layersInfoIcon);
                    } else {
                        // TODO
                        setIcon(layersInfoIcon);
                    }
                }
            } else {
                if (tmpLayer.getChildren().length == 0) {
                    if (rightScale) {
                        setIcon(layerIcon);
                    } else {
                        setIcon(disabledLayerIcon);
                    }
                } else {
                    if (rightScale) {
                        setIcon(layersIcon);
                    } else {
                        setIcon(disabledLayersIcon);
                    }
                }
            }
        } else if (value instanceof Style) {
            final Style tmpStyle = (Style)value;
            if ((tmpStyle.getTitle() != null) && (tmpStyle.getTitle().trim().length() > 0)) {
                setText(tmpStyle.getTitle());
            } else {
                setText(tmpStyle.getName());
            }
            setIcon(styleIcon);
        }

        return this;
    }
}
