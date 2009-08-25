/*
 * ActiveLayerTreeCellRenderer.java
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
 * Created on 23. November 2005, 15:19
 *
 */

package de.cismet.cismap.commons.gui.layerwidget;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.deegree.services.wms.capabilities.Layer;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerTreeCellRenderer extends DefaultTreeCellRenderer{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DefaultTreeCellRenderer defaultRenderer;
    private HashMap icons=new HashMap();
    private static final Dimension DIM = new Dimension(250,16);
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
    
    
    /** Creates a new instance of ActiveLayerTreeCellRenderer */
    @SuppressWarnings("unchecked")
    public ActiveLayerTreeCellRenderer() {
        icons.put(new IconType(SINGLE, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layer.png")));
        icons.put(new IconType(SINGLE + DL, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerOverlaywms.png")));
        icons.put(new IconType(SINGLE + DL, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerOverlaywmsInvisible.png")));
        icons.put(new IconType(SINGLE + INFO, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerInfo.png")));
        icons.put(new IconType(SINGLE + INFO + DL, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerInfoDL.png")));
        icons.put(new IconType(SINGLE + INFO + DL, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerInfoDLinvisible.png")));
        icons.put(new IconType(MULTI, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layers.png")));
        icons.put(new IconType(MULTI + DL, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layersDL.png")));
        icons.put(new IconType(MULTI + DL, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layersDLinvisible.png")));
        icons.put(new IconType(SUPPORTER, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/featureSupporter.png")));
        icons.put(new IconType(SUPPORTER, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/featureSupporterInvisible.png")));
        icons.put(new IconType(SUPPORTER, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/featureSupporter.png")));
        icons.put(new IconType(SUPPORTER, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/featureSupporterInvisible.png")));

        icons.put(new IconType(SINGLE, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layer.png")));
        icons.put(new IconType(SINGLE + DL, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerOverlaywms.png")));
        icons.put(new IconType(SINGLE + DL, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerOverlaywmsInvisible.png")));
        icons.put(new IconType(SINGLE + INFO, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerInfo.png")));
        icons.put(new IconType(SINGLE + INFO + DL, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerInfoDL.png")));
        icons.put(new IconType(SINGLE + INFO + DL, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerInfoDLinvisible.png")));
        icons.put(new IconType(MULTI, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layers.png")));
        icons.put(new IconType(MULTI + DL, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layersDL.png")));
        icons.put(new IconType(MULTI + DL, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layersDLinvisible.png")));

        icons.put(new IconType(ASCII, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerAscii.png")));
        icons.put(new IconType(ASCII, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerAsciiInvisible.png")));
        icons.put(new IconType(ASCII, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerAscii.png")));
        icons.put(new IconType(ASCII, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerAsciiInvisible.png")));

        icons.put(new IconType(GML, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerGml.png")));
        icons.put(new IconType(GML, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerGmlInvisible.png")));
        icons.put(new IconType(GML, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGml.png")));
        icons.put(new IconType(GML, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGmlInvisible.png")));

        icons.put(new IconType(POSTGIS, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgis.png")));
        icons.put(new IconType(POSTGIS, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgisInvisible.png")));
        icons.put(new IconType(POSTGIS, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgis.png")));
        icons.put(new IconType(POSTGIS, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgisInvisible.png")));

        icons.put(new IconType(SHAPE, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png")));
        icons.put(new IconType(SHAPE, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));
        icons.put(new IconType(SHAPE, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));
        icons.put(new IconType(SHAPE, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png")));

        icons.put(new IconType(SIMPLEWMS, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerSimplewms.png")));
        icons.put(new IconType(SIMPLEWMS, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerSimplewmsInvisible.png")));
        icons.put(new IconType(SIMPLEWMS, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerSimplewms.png")));
        icons.put(new IconType(SIMPLEWMS, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerSimplewmsInvisible.png")));

        icons.put(new IconType(WFS, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfs.png")));
        icons.put(new IconType(WFS, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfsInvisible.png")));
        icons.put(new IconType(WFS, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfs.png")));
        icons.put(new IconType(WFS, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfsInvisible.png")));

        icons.put(new IconType(WFST, true, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfst.png")));
        icons.put(new IconType(WFST, false, true), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfstInvisible.png")));
        icons.put(new IconType(WFST, true, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfst.png")));
        icons.put(new IconType(WFST, false, false), new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfstInvisible.png")));
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, isSelected,
                expanded, leaf, row,
                hasFocus);
        setMinimumSize(DIM);
        setMaximumSize(DIM);
        setPreferredSize(DIM);
        setText(value.toString());
        if (value instanceof RetrievalServiceLayer) {
            RetrievalServiceLayer layer=(RetrievalServiceLayer)value;
            setText(layer.toString());
            if (value instanceof WMSServiceLayer){
                WMSServiceLayer wmsLayer=(WMSServiceLayer)layer;
                if (wmsLayer.getWMSLayers().size()==1) {                    
                    if (((WMSLayer)wmsLayer.getWMSLayers().get(0)).getOgcCapabilitiesLayer().isQueryable()) {
                        setIcon(getRightIcon(SINGLE+DL+INFO,wmsLayer.getPNode().getVisible(),layer.isEnabled()));
                    } else {
                        setIcon(getRightIcon(SINGLE+DL,wmsLayer.getPNode().getVisible(),layer.isEnabled()));
                    }
                }
                if (wmsLayer.getWMSLayers().size()>1) {
                    setIcon(getRightIcon(MULTI+DL,layer.getPNode().getVisible(),layer.isEnabled()));
                }
            } else if (value instanceof SimpleWMS) {
                setIcon(getRightIcon(SIMPLEWMS,layer.getPNode().getVisible(),layer.isEnabled()));
            } else if (value instanceof WebFeatureService) {
                setIcon(getRightIcon(WFS,layer.getPNode().getVisible(),layer.isEnabled()));
//            } else if (value instanceof TransactionalWebFeatureService) {
//                setIcon(getRightIcon(WFST,layer.getPNode().getVisible(),layer.isEnabled()));
//            } else if (value instanceof ShapeService) {
//                setIcon(getRightIcon(SHAPE,true,((ShapeService)value).isEnabled()));
//            } else if (value instanceof GMLService) {
//                setIcon(getRightIcon(GML,true,((GMLService)value).isEnabled()));
//            } else if (value instanceof ASCIIService) {
//                setIcon(getRightIcon(ASCII,true,((ASCIIService)value).isEnabled()));
            } else if (value instanceof SimplePostgisFeatureService) {
                setIcon(getRightIcon(POSTGIS,layer.getPNode().getVisible(),layer.isEnabled()));
            } else {
                setIcon(getRightIcon(SUPPORTER,layer.getPNode().getVisible(),layer.isEnabled()));
            }
        } else if (value instanceof WMSLayer) {
            if (((WMSLayer)value).getOgcCapabilitiesLayer().isQueryable()) {
                setIcon(getRightIcon(SINGLE+INFO,true,((WMSLayer)value).isEnabled()));
            } else {
                setIcon(getRightIcon(SINGLE,true,((WMSLayer)value).isEnabled()));
            }
        } else if (value instanceof SimpleWMS) {
            setIcon(getRightIcon(SIMPLEWMS,true,((SimpleWMS)value).isEnabled()));
        }
        
        //ScaleHint
        Color fg=getForeground();
        try {
            Layer tmpLayer=null;
            if (value instanceof WMSServiceLayer&&((WMSServiceLayer)value).getWMSLayers().size()==1) {
                tmpLayer=((WMSLayer)((WMSServiceLayer)value).getWMSLayers().get(0)).getOgcCapabilitiesLayer();
            }
            else if (value instanceof WMSLayer){
                tmpLayer=((WMSLayer)value).getOgcCapabilitiesLayer();
            }
            
            if (tmpLayer!=null&&tmpLayer.getScaleHint()!=null) {
                boolean rightScale=true;
                double mxScale=tmpLayer.getScaleHint().getMax();
                double mnScale=tmpLayer.getScaleHint().getMin();
                double scale=CismapBroker.getInstance().getMappingComponent().getCurrentOGCScale();
                if (scale<mnScale||scale>mxScale) {
                    if (!isSelected) {
                        setForeground(Color.GRAY);
                    } else {
                        setForeground(fg);
                    }
                    rightScale=false;
                    // setTooltip("Im momentanen Maßstab nicht darstellbar");
                }
            }
        } catch (Exception e) {
            log.debug("Fehler bei der ScaleHint Verarbeitung.Kein Problem",e);
        }
        return this;
    }
    
    
    private ImageIcon getRightIcon(int type,boolean visible,boolean enabled) {
        Object o=icons.get(new IconType(type, visible, enabled));
        if (o!=null && o instanceof ImageIcon) {
            return (ImageIcon)o;
        } else {
            log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("ActiveLayerTreeCellRenderer.log.Icon_nicht_gefunden._Standardicon_verwendet."));
            return (ImageIcon)icons.get(new IconType(SINGLE,true,true));
        }
    }
}

class IconType {
    int type=-1;
    boolean visible;
    boolean enabled;
    public IconType(int type,boolean visible,boolean enabled) {
        this.type=type;
        this.visible=visible;
        this.enabled=enabled;
    }
    
    @Override
    public int hashCode() {
        int ret=0;
        if (visible) ret+=1;
        if (enabled) ret+=2;
        ret+=type;
        return ret;
    }
    @Override
    public boolean equals(Object o) {
        return (o instanceof IconType && ((IconType)o).type==type && ((IconType)o).visible==visible && ((IconType)o).enabled==enabled);
    }
}