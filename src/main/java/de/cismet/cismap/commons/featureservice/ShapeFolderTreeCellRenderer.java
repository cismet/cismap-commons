/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import java.awt.Component;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShapeFolderTreeCellRenderer extends DefaultTreeCellRenderer {

    //~ Instance fields --------------------------------------------------------

    private javax.swing.ImageIcon layersIcon;
    private javax.swing.ImageIcon layerIcon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WMSCapabilitiesTreeCellRenderer.
     */
    public ShapeFolderTreeCellRenderer() {
        layersIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers.png")); // NOI18N
        layerIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer.png"));  // NOI18N
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

        if (value instanceof File) {
            if (((File)value).isDirectory()) {
                setIcon(layersIcon);
            } else {
                setIcon(layerIcon);
            }

            setText(((File)value).getName());
        }

        return this;
    }
}
