/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.featureservice;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;

/**
 * Der WFSCapabilitiesTreeCellRenderer bestimmt, wie ein WFSCapabilitiesTree-Knoten dargestellt wird.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class WFSCapabilitiesTreeCellRenderer extends DefaultTreeCellRenderer {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.raster.wfs.WFSCapabilitiesTreeCellRenderer"); // NOI18N
    public static boolean showTitle = false;

    //~ Instance fields --------------------------------------------------------

    private ImageIcon serverIcon;
    private ImageIcon featureIcon;
    private ImageIcon elementIcon;
    private ImageIcon stringIcon;
    private ImageIcon integerIcon;
    private ImageIcon geomIcon;
    private String altRootName = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WMSCapabilitiesTreeCellRenderer.
     */
    public WFSCapabilitiesTreeCellRenderer() {
        serverIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/server.png"));   // NOI18N
        featureIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/layer.png"));   // NOI18N
        elementIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/attr.png"));    // NOI18N
        stringIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/string.png"));   // NOI18N
        integerIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/integer.png")); // NOI18N
        geomIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/geom.png"));       // NOI18N
    }

    /**
     * Creates a new WFSCapabilitiesTreeCellRenderer object.
     *
     * @param  name  DOCUMENT ME!
     */
    public WFSCapabilitiesTreeCellRenderer(final String name) {
        this();
        this.altRootName = name;
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
        super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

        try {
            // if node is a WFSCapabilities --> root
            if (value instanceof WFSCapabilities) {
                if (altRootName != null) {
                    setText(altRootName);
                } else {
                    setText("Web Feature Service"); // NOI18N
                }
                setIcon(serverIcon);
            } else if (value instanceof FeatureType) {
                final FeatureType e = (FeatureType)value;
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

                if (showTitle) {
                    setText(e.getTitle());
                } else {
                    setText(e.getName().getLocalPart());
                }
            } else if (value instanceof FeatureServiceAttribute) {
                final FeatureServiceAttribute fsf = (FeatureServiceAttribute)value;
                String type;
                if ((fsf.getType() != null) && (fsf.getType().lastIndexOf(":") != -1)) {  // NOI18N
                    type = fsf.getType().substring(fsf.getType().lastIndexOf(":") + 1);   // NOI18N
                } else {
                    type = fsf.getType();
                }
                if (type.equals(FeatureServiceUtilities.STRING_PROPERTY_TYPE)) {
                    setIcon(stringIcon);
                } else if (type.equals(FeatureServiceUtilities.INTEGER_PROPERTY_TYPE)) {
                    setIcon(integerIcon);
                } else if (FeatureServiceUtilities.isElementOfGeometryType(type)) {
                    setIcon(geomIcon);
                } else {
                    setIcon(elementIcon);
                }
                if ((fsf.getName() != null) && (fsf.getName().lastIndexOf(":") != -1)) {  // NOI18N
                    setText(fsf.getName().substring(fsf.getName().lastIndexOf(":") + 1)); // NOI18N
                } else {
                    setText(fsf.getName());
                }
            }
        } catch (Exception ex) {
            log.error("error in WFSCapabilitiesTreeCellRenderer", ex);                    // NOI18N
        }
        return this;
    }
}
