/*
 * ActiveLayerTableCellRenderer.java
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
 * Created on 23. November 2005, 16:12
 *
 */
package de.cismet.cismap.commons.gui.layerwidget;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.features.CloneableFeature;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.simplelayerwidget.NewSimpleInternalLayerWidget;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.treetable.TreeTableModelAdapter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.deegree.services.wms.capabilities.Style;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerTableCellRenderer extends DefaultTableCellRenderer {
    private javax.swing.ImageIcon styleIcon;
    private javax.swing.ImageIcon unselectedStyleIcon;
    private LayerWidget layerWidgetParent = null;
    private NewSimpleInternalLayerWidget internalWidgetParent = null;
    private JButton moreButton = new JButton("...");
    private JSlider slider = new JSlider(0, 100);
    private JSlider slider2 = new JSlider(0, 100);
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ImageIcon layersIcon,  layerIcon,  layersInfoIcon,  layerInfoIcon,  okIcon,  errorIcon,  refreshIcon,  refreshNeededIcon,  inProgressIcon;
    private Timer t = null;
    private boolean isWidgetTable = false;
    private JProgressBar progress = new JProgressBar(0, 100) {
        @Override
        public boolean isDisplayable() {
            // This does the trick. It makes sure animation is always performed
            return true;
        }
    };
    private JProgressBar progressIndeterminate = new JProgressBar(0, 100) {
        @Override
        public boolean isDisplayable() {
            // This does the trick. It makes sure animation is always performed
            return true;
        }
    };
    

    public ActiveLayerTableCellRenderer(boolean widgetTable) {
        this();
        this.isWidgetTable = widgetTable;
    }

    /** Creates a new instance of ActiveLayerTableCellRenderer */
    public ActiveLayerTableCellRenderer() {
//        progress.setUI(new MetalProgressBarUI());
//        progress.setString("");progress.setStringPainted(true);
//        progressIndeterminate.setUI(new MetalProgressBarUI());
//        progressIndeterminate.setString("");progressIndeterminate.setStringPainted(true);
        styleIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/style.png"));
        unselectedStyleIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/disabledStyle.png"));
        moreButton.setEnabled(false);
        progress.setLayout(new BorderLayout(2, 2));
        slider.setOpaque(false);
        progress.add(slider, BorderLayout.CENTER);
        progressIndeterminate.setLayout(new BorderLayout(2, 2));
        slider2.setOpaque(false);
        progressIndeterminate.add(slider2, BorderLayout.CENTER);
        progressIndeterminate.setIndeterminate(true);

        layersIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png"));
        layerIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layer.png"));
        layersInfoIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers_i.png"));
        layerInfoIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layer_i.png"));

        okIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/enable30.png"));
        errorIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/error.png"));
        refreshIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/refresh.png"));
        refreshNeededIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/refresh30.png"));
        inProgressIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/inProgress.png"));
    }

    /**
     * Returns the default table cell renderer.
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     * 			<code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, boolean isSelected, boolean hasFocus, final int row, final int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
       
        int realColumn = table.convertColumnIndexToModel(column);
        final TableCellRenderer booleanRenderer = table.getDefaultRenderer(Boolean.class);
        final TableCellRenderer stringRenderer = table.getDefaultRenderer(String.class);

        setToolTipText(null);
        if (!isWidgetTable) {
            if (layerWidgetParent == null) {
                layerWidgetParent = StaticSwingTools.findSpecificParentComponent(table, LayerWidget.class);
                layerWidgetParent.setErrorImage(null);
            } else {
                layerWidgetParent.setErrorImage(null);
            }
        } else {
            if (internalWidgetParent == null) {
                internalWidgetParent = StaticSwingTools.findSpecificParentComponent(table, NewSimpleInternalLayerWidget.class);
                internalWidgetParent.setErrorImage(null);
            } else {
                internalWidgetParent.setErrorImage(null);
            }
        }
        if (realColumn == 0) {
            setText("");
            //label.setIconTextGap(4);
            setHorizontalAlignment(JLabel.CENTER);
            if (value instanceof RetrievalServiceLayer) {
                if (((RetrievalServiceLayer) value).isRefreshNeeded()) {
                    setIcon(refreshNeededIcon);
                } else if (((RetrievalServiceLayer) value).getProgress() < 100) {
                    setIcon(inProgressIcon);
                } else if (((RetrievalServiceLayer) value).hasErrors()) {
                    setIcon(errorIcon);
                    if (((RetrievalServiceLayer) value).getErrorObject() instanceof Image) {
                        if (!isWidgetTable) {
                            if (layerWidgetParent == null) {
                                layerWidgetParent = StaticSwingTools.findSpecificParentComponent(table, LayerWidget.class);
                                layerWidgetParent.setErrorImage((Image) ((RetrievalServiceLayer) value).getErrorObject());
                            } else {
                                layerWidgetParent.setErrorImage((Image) ((RetrievalServiceLayer) value).getErrorObject());
                            }
                        } else {
                            if (internalWidgetParent == null) {
                                internalWidgetParent = StaticSwingTools.findSpecificParentComponent(table, NewSimpleInternalLayerWidget.class);
                                internalWidgetParent.setErrorImage((Image) ((RetrievalServiceLayer) value).getErrorObject());
                            } else {
                                internalWidgetParent.setErrorImage((Image) ((RetrievalServiceLayer) value).getErrorObject());
                            }
                        }
                        setToolTipText(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("ActiveLayerTableCellRenderer.Der_Server_lieferte_folgendes_Fehlerbild_zurueck"));
                    } else if (((RetrievalServiceLayer) value).getErrorObject() instanceof String) {
                        setToolTipText(((RetrievalServiceLayer) value).getErrorObject().toString());
                    }
                } else {
                    setIcon(okIcon);
                }
            }
        } else if (realColumn == 2) {
            setText("");
            setIcon(null);
            if (value instanceof WMSServiceLayer) {
                if (((WMSServiceLayer) value).getWMSLayers().size() > 1) {
                    setText("");
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                    setText("");
                    //label.setIconTextGap(4);
                    Style selectedStyle = null;

                    int styleCount = 0;
                    if (value instanceof WMSLayer) {
                        selectedStyle = ((WMSLayer) value).getSelectedStyle();
                        styleCount = ((WMSLayer) value).getOgcCapabilitiesLayer().getStyles().length;
                    } else if (value instanceof WMSServiceLayer) {
                        //Kann nur ein WMSLayer haben (wegen Bedingung weiter oben)
                        selectedStyle = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0)).getSelectedStyle();
                        styleCount = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0)).getOgcCapabilitiesLayer().getStyles().length;
                    }
                    if (selectedStyle != null) {
                        setText(selectedStyle.getTitle());
                    }
                    if (styleCount <= 1) {
                        setIcon(unselectedStyleIcon);
                    } else {
                        setIcon(styleIcon);
                    }
                }
            } else if (value instanceof WebFeatureService) {
                log.debug("WFS TableCellRenderer");
                final Color bg = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column).getBackground();
                JLabel temp = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        CloneableFeature feature = ((WebFeatureService)value).getRenderingFeature();
                        try {
                            DefaultStyledFeature style = (DefaultStyledFeature)feature;
                            Graphics2D g2d = (Graphics2D)g;
                            g.setColor(bg);
                            g.fillRect(0, 0, getWidth(), getHeight());
                            if (style.getFillingPaint() != null) {
                                g2d.setColor((Color)style.getFillingPaint());
                                g2d.fillRect(10,4,getWidth()-20,getHeight()-8);
                            }
                            if (style.getLinePaint() != null) {
                                g2d.setColor((Color)style.getLinePaint());
                                float width = new Float(Math.min(3.0f, style.getLineWidth()));
                                g2d.setStroke(new BasicStroke(width));
                                g2d.drawRect(10,4,getWidth()-20,getHeight()-8);
                            }
                        } catch (Exception ex) {
                        }
                    }
                };
                return temp;
            }
        } else if (realColumn == 3) {
            WMSLayer wmsLayer = null;
            setText("");
            setIcon(null);
            setHorizontalAlignment(JLabel.LEFT);
            if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() > 1) {

            } else {
                if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() == 1) {
                    wmsLayer = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0));
                } else if (value instanceof WMSLayer) {
                    wmsLayer = (WMSLayer) value;
                }
                if (wmsLayer != null && wmsLayer.getOgcCapabilitiesLayer().isQueryable()) {
                    return booleanRenderer.getTableCellRendererComponent(table, new Boolean(wmsLayer.isQuerySelected()), isSelected, hasFocus, row, column);
                } else {
                    setIcon(null);
                    setText("");
                }
            }
        } else if (realColumn == 4) {
            Component returnComp = null;
            if (value instanceof RetrievalServiceLayer) {
                slider.setSize(progress.getSize());
                slider2.setSize(progress.getSize());
                slider.setValue((int) (((RetrievalServiceLayer) value).getTranslucency() * 100));
                slider2.setValue((int) (((RetrievalServiceLayer) value).getTranslucency() * 100));
                if (((RetrievalServiceLayer) value).getProgress() < 100 && ((RetrievalServiceLayer) value).isEnabled() && !((RetrievalServiceLayer) value).isRefreshNeeded()) {
                    returnComp = progressIndeterminate;
                    if (t == null) {
                        t = new Timer(10, new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
//                                for (int i = 0; i < table.getModel().getRowCount(); ++i) {
//                                   ((TreeTableModelAdapter) (table.getModel())).fireTableCellUpdated(i, column);
//                                }
                                ((TreeTableModelAdapter) (table.getModel())).fireTableCellUpdated(row, column);
                            }
                        });
                    }

                    t.start();
                    t.setRepeats(true);

                } else {
                    returnComp = progress;
                    progress.setValue(((RetrievalServiceLayer) value).getProgress());
                    if (t != null) {
                        t.setRepeats(false);
                    }
                }
                slider.setSize(progress.getSize());
                slider2.setSize(progress.getSize());

                return returnComp;
            } else {
                setIcon(null);
                setText("");
            }
        } else if (realColumn == 5) {
            if (value instanceof WMSServiceLayer) {
                moreButton.setEnabled(false);
            } else if (value instanceof WebFeatureService) {
                moreButton.setEnabled(true);
            }
            return moreButton;
        } else {
            setIcon(null);
            setText("");
        }
        return this;
    }
}
