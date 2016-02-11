/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.cismet.cismap.commons.ModeLayer;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.featureservice.GMLFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.wms.capabilities.Layer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ActiveLayerTreeCellRenderer extends DefaultTreeCellRenderer {

    //~ Static fields/initializers ---------------------------------------------

    private static int SINGLE = 4;
    private static int MULTI = 8;
    private static int INFO = 16;
    private static int DL = 32;
    private static int SUPPORTER = 32;
    private static int ASCII = 64;
    private static int GML = 96;
    private static int POSTGIS = 160;
    private static int SHAPE = 192;
    private static int SIMPLEWMS = 224;
    private static int WFS = 256;
    private static int WFST = 288;
    private static int H2 = 300;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DefaultTreeCellRenderer defaultRenderer;
    private HashMap icons = new HashMap();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ActiveLayerTreeCellRenderer.
     */
    @SuppressWarnings("unchecked")
    public ActiveLayerTreeCellRenderer() {
        icons.put(new IconType(SINGLE, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layer.png")));           // NOI18N
        icons.put(new IconType(SINGLE + DL, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerOverlaywms.png"))); // NOI18N
        icons.put(new IconType(SINGLE + DL, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerOverlaywmsInvisible.png")));  // NOI18N
        icons.put(new IconType(SINGLE + INFO, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerInfo.png")));       // NOI18N
        icons.put(new IconType(SINGLE + INFO + DL, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerInfoDL.png")));     // NOI18N
        icons.put(new IconType(SINGLE + INFO + DL, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerInfoDLinvisible.png")));      // NOI18N
        icons.put(new IconType(MULTI, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layers.png")));          // NOI18N
        icons.put(new IconType(MULTI + DL, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layersDL.png")));        // NOI18N
        icons.put(new IconType(MULTI + DL, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layersDLinvisible.png")));         // NOI18N
        icons.put(new IconType(SUPPORTER, true, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/featureSupporter.png")));          // NOI18N
        icons.put(new IconType(SUPPORTER, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/featureSupporterInvisible.png"))); // NOI18N
        icons.put(new IconType(SUPPORTER, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/featureSupporter.png"))); // NOI18N
        icons.put(new IconType(SUPPORTER, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/featureSupporterInvisible.png")));           // NOI18N

        icons.put(new IconType(SINGLE, true, false),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layer.png"))); // NOI18N
        icons.put(new IconType(SINGLE + DL, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerOverlaywms.png"))); // NOI18N
        icons.put(new IconType(SINGLE + DL, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerOverlaywmsInvisible.png")));           // NOI18N

        icons.put(new IconType(SINGLE + INFO, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerInfo.png")));   // NOI18N
        icons.put(new IconType(SINGLE + INFO + DL, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerInfoDL.png"))); // NOI18N
        icons.put(new IconType(SINGLE + INFO + DL, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerInfoDLinvisible.png")));           // NOI18N

        icons.put(new IconType(MULTI, true, false),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layers.png"))); // NOI18N
        icons.put(new IconType(MULTI + DL, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layersDL.png")));         // NOI18N
        icons.put(new IconType(MULTI + DL, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layersDLinvisible.png")));                   // NOI18N

        icons.put(new IconType(ASCII, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerAscii.png"))); // NOI18N
        icons.put(new IconType(ASCII, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerAsciiInvisible.png")));  // NOI18N
        icons.put(new IconType(ASCII, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerAscii.png")));  // NOI18N
        icons.put(new IconType(ASCII, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerAsciiInvisible.png")));            // NOI18N

        icons.put(new IconType(POSTGIS, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgis.png"))); // NOI18N
        icons.put(new IconType(POSTGIS, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgisInvisible.png")));  // NOI18N
        icons.put(new IconType(POSTGIS, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgis.png")));  // NOI18N
        icons.put(new IconType(POSTGIS, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgisInvisible.png")));            // NOI18N

        icons.put(new IconType(SIMPLEWMS, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerSimplewms.png"))); // NOI18N
        icons.put(new IconType(SIMPLEWMS, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerSimplewmsInvisible.png")));  // NOI18N
        icons.put(new IconType(SIMPLEWMS, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerSimplewms.png")));  // NOI18N
        icons.put(new IconType(SIMPLEWMS, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerSimplewmsInvisible.png")));            // NOI18N

        icons.put(new IconType(WFST, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfst.png"))); // NOI18N
        icons.put(new IconType(WFST, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfstInvisible.png")));  // NOI18N
        icons.put(new IconType(WFST, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfst.png")));  // NOI18N
        icons.put(new IconType(WFST, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfstInvisible.png")));            // NOI18N

        icons.put(new IconType(WFS, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfs.png"))); // NOI18N
        icons.put(new IconType(WFS, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfsInvisible.png")));  // NOI18N
        icons.put(new IconType(WFS, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfs.png")));  // NOI18N
        icons.put(new IconType(WFS, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfsInvisible.png")));            // NOI18N

        icons.put(new IconType(GML, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerGml.png"))); // NOI18N
        icons.put(new IconType(GML, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerGmlInvisible.png")));  // NOI18N
        icons.put(new IconType(GML, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGml.png")));  // NOI18N
        icons.put(new IconType(GML, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGmlInvisible.png")));            // NOI18N

        icons.put(new IconType(WFST, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfst.png")));
        icons.put(new IconType(WFST, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfstInvisible.png")));
        icons.put(new IconType(WFST, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfst.png")));
        icons.put(new IconType(WFST, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfstInvisible.png")));

        icons.put(new IconType(SHAPE, true, true),
            new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png"))); // NOI18N
        icons.put(new IconType(SHAPE, false, true),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));  // NOI18N
        icons.put(new IconType(SHAPE, true, false),
            new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));  // NOI18N
        icons.put(new IconType(SHAPE, false, false),
            new ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png")));            // NOI18N
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
        if (value instanceof ModeLayer) {
            return getTreeCellRendererComponent(
                    tree,
                    ((ModeLayer)value).getCurrentLayer(),
                    isSelected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);
        }
        final JLabel ret = (JLabel)super.getTreeCellRendererComponent(
                tree,
                value,
                isSelected,
                expanded,
                leaf,
                row,
                hasFocus);
        ret.setText(value.toString());

        if (value instanceof RetrievalServiceLayer) {
            final RetrievalServiceLayer layer = (RetrievalServiceLayer)value;
            ret.setText(layer.toString());
            if (value instanceof WMSServiceLayer) {
                final WMSServiceLayer wmsLayer = (WMSServiceLayer)layer;
                if (wmsLayer.getWMSLayers().size() == 1) {
                    if (!wmsLayer.isDummy()
                                && ((WMSLayer)wmsLayer.getWMSLayers().get(0)).getOgcCapabilitiesLayer().isQueryable()) {
                        ret.setIcon(getRightIcon(
                                SINGLE
                                        + DL
                                        + INFO,
                                wmsLayer.getPNode().getVisible(),
                                layer.isEnabled()));
                    } else {
                        ret.setIcon(getRightIcon(SINGLE + DL, wmsLayer.getPNode().getVisible(), layer.isEnabled()));
                    }
                }
                if (wmsLayer.getWMSLayers().size() > 1) {
                    ret.setIcon(getRightIcon(MULTI + DL, layer.getPNode().getVisible(), layer.isEnabled()));
                }
            } else if (value instanceof SimpleWMS) {
                ret.setIcon(getRightIcon(SIMPLEWMS, layer.getPNode().getVisible(), layer.isEnabled()));
            } else if (value instanceof WebFeatureService) {
                ret.setIcon(getRightIcon(WFS, layer.getPNode().getVisible(), layer.isEnabled()));
//            } else if (value instanceof TransactionalWebFeatureService) {
//                setIcon(getRightIcon(WFST,layer.getPNode().getVisible(),layer.isEnabled()));
//            } else if (value instanceof ShapeService) {
//                setIcon(getRightIcon(SHAPE,true,((ShapeService)value).isEnabled()));
//            } else if (value instanceof GMLService) {
//                setIcon(getRightIcon(GML,true,((GMLService)value).isEnabled()));
//            } else if (value instanceof ASCIIService) {
//                setIcon(getRightIcon(ASCII,true,((ASCIIService)value).isEnabled()));
            } else if (value instanceof GMLFeatureService) {
                ret.setIcon(getRightIcon(GML, layer.getPNode().getVisible(), layer.isEnabled()));
            } else if (value instanceof ShapeFileFeatureService) {
                ret.setIcon(getRightIcon(SHAPE, layer.getPNode().getVisible(), layer.isEnabled()));
            } else if (value instanceof SimplePostgisFeatureService) {
                ret.setIcon(getRightIcon(POSTGIS, layer.getPNode().getVisible(), layer.isEnabled()));
            } else if (value instanceof H2FeatureService) {
                int type = 0;

                if (layer.getPNode().getVisible()) {
                    if (layer.isEnabled()) {
                        type = H2FeatureService.LAYER_ENABLED_VISIBLE;
                    } else {
                        type = H2FeatureService.LAYER_DISABLED_VISIBLE;
                    }
                } else {
                    if (layer.isEnabled()) {
                        type = H2FeatureService.LAYER_ENABLED_INVISIBLE;
                    } else {
                        type = H2FeatureService.LAYER_DISABLED_INVISIBLE;
                    }
                }

                ret.setIcon(((H2FeatureService)value).getLayerIcon(type));
            } else {
                ret.setIcon(getRightIcon(SUPPORTER, layer.getPNode().getVisible(), layer.isEnabled()));
            }
        } else if (value instanceof WMSLayer) {
            boolean queryable;

            if (!((WMSLayer)value).isDummy()) {
                queryable = ((WMSLayer)value).getOgcCapabilitiesLayer().isQueryable();
            } else {
                queryable = ((WMSLayer)value).isQueryable();
            }

            if (queryable) {
                ret.setIcon(getRightIcon(SINGLE + INFO, true, ((WMSLayer)value).isEnabled()));
            } else {
                ret.setIcon(getRightIcon(SINGLE, true, ((WMSLayer)value).isEnabled()));
            }
        } else if (value instanceof SimpleWMS) {
            ret.setIcon(getRightIcon(SIMPLEWMS, true, ((SimpleWMS)value).isEnabled()));
        }

        // ScaleHint
        final Color fg = getForeground();
        try {
            Layer tmpLayer = null;
            if ((value instanceof WMSServiceLayer) && (((WMSServiceLayer)value).getWMSLayers().size() == 1)) {
                tmpLayer = ((WMSLayer)((WMSServiceLayer)value).getWMSLayers().get(0)).getOgcCapabilitiesLayer();
            } else if (value instanceof WMSLayer) {
                tmpLayer = ((WMSLayer)value).getOgcCapabilitiesLayer();
            }

            if (tmpLayer != null) {
                boolean rightScale = true;
                final double mxScale = tmpLayer.getScaleDenominationMax();
                final double mnScale = tmpLayer.getScaleDenominationMin();
                final double scale = CismapBroker.getInstance().getMappingComponent().getCurrentOGCScale();
                if ((scale < mnScale) || (scale > mxScale)) {
                    if (!isSelected) {
                        ret.setForeground(Color.GRAY);
                    } else {
                        ret.setForeground(fg);
                    }
                    rightScale = false;
                    // setTooltip("Im momentanen Maßstab nicht darstellbar");
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Fehler bei der ScaleHint Verarbeitung.Kein Problem", e); // NOI18N
            }
        }
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type     DOCUMENT ME!
     * @param   visible  DOCUMENT ME!
     * @param   enabled  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ImageIcon getRightIcon(final int type, final boolean visible, final boolean enabled) {
        final Object o = icons.get(new IconType(type, visible, enabled));
        if ((o != null) && (o instanceof ImageIcon)) {
            return (ImageIcon)o;
        } else {
            log.warn("Icon not found, used default icon."); // NOI18N
            return (ImageIcon)icons.get(new IconType(SINGLE, true, true));
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class IconType {

    //~ Instance fields --------------------------------------------------------

    int type = -1;
    boolean visible;
    boolean enabled;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IconType object.
     *
     * @param  type     DOCUMENT ME!
     * @param  visible  DOCUMENT ME!
     * @param  enabled  DOCUMENT ME!
     */
    public IconType(final int type, final boolean visible, final boolean enabled) {
        this.type = type;
        this.visible = visible;
        this.enabled = enabled;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int hashCode() {
        int ret = 0;
        if (visible) {
            ret += 1;
        }
        if (enabled) {
            ret += 2;
        }
        ret += type;
        return ret;
    }

    @Override
    public boolean equals(final Object o) {
        return ((o instanceof IconType) && (((IconType)o).type == type) && (((IconType)o).visible == visible)
                        && (((IconType)o).enabled == enabled));
    }
}
