/*
 * ActiveLayerTableCellEditor.java
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
 * Created on 24. November 2005, 10:43
 *
 */
package de.cismet.cismap.commons.gui.layerwidget;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.features.CloneableFeature;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.featureservice.QueryEditorDialog;
import de.cismet.cismap.commons.featureservice.WFSOperator;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.featureservice.style.StyleDialog;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.gui.StaticSwingTools;
import edu.umd.cs.piccolo.PNode;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellEditor;
import org.deegree.services.wms.capabilities.Style;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerTableCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor, PropertyChangeListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private JCheckBox informationBox;
    private Object value;
    private JTable table;
    private JComboBox cbbStyleChooser;
    private StyleDialog styleDialog;
    private JButton moreButton = new JButton("...");
    private JButton wfsStyleButton = new JButton() {

        @Override
        protected void paintComponent(Graphics g) {
            CloneableFeature feature = ((WebFeatureService) value).getRenderingFeature();
            try {
                if (feature instanceof StyledFeature) {
                    StyledFeature style = (StyledFeature) feature;
                    Graphics2D g2d = (Graphics2D) g;
                    if (style.getFillingPaint() != null) {
                        g2d.setColor((Color) style.getFillingPaint());
                        g2d.fillRect(10, 4, getWidth() - 20, getHeight() - 8);
                    }
                    if (style.getLinePaint() != null) {
                        g2d.setColor((Color) style.getLinePaint());
                        float width = new Float(Math.min(3.0f, style.getLineWidth())).intValue();
                        g2d.setStroke(new BasicStroke(width));
                        g2d.drawRect(10, 4, getWidth() - 20, getHeight() - 8);
                    }
                }
            } catch (Exception ex) {
            }
        }
    };
    private DefaultCellEditor informationCellEditor;
    private DefaultCellEditor stylesCellEditor;
    private JProgressBar progress = new JProgressBar(0, 100);
    private JSlider slider = new JSlider(0, 100);
    private JLabel visibilityLabel = new JLabel("", JLabel.CENTER);
    private JLabel emptyLabel = new JLabel();
    private RetrievalServiceLayer wmsServiceLayerThatFiresPropertyChangeEvents = null;

    /** Creates a new instance of ActiveLayerTableCellEditor */
    public ActiveLayerTableCellEditor() {
        //progress.setUI(new MetalProgressBarUI());
        progress.setString("");
        progress.setStringPainted(true);

        visibilityLabel.setOpaque(false);
        visibilityLabel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (value instanceof RetrievalServiceLayer) {
                        RetrievalServiceLayer layer = ((RetrievalServiceLayer) value);
                        boolean flag = layer.getPNode().getVisible();
                        layer.getPNode().setVisible(!flag);
                        layer.setEnabled(!flag);
//                        fireTreeNodesChanged(this, new Object[]{root}, null, null);

                        if (!flag) {
//                            layer.setRefreshNeeded(true);
                            layer.retrieve(true);
                        }
                        ActiveLayerEvent ale = new ActiveLayerEvent();
                        ale.setLayer(value);
                        CismapBroker.getInstance().fireLayerVisibilityChanged(ale);
                        table.repaint();
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        informationBox = new JCheckBox();
        informationBox.setHorizontalAlignment(JCheckBox.CENTER);
        cbbStyleChooser = new JComboBox();
        cbbStyleChooser.setEditable(false);
        cbbStyleChooser.setRenderer(new StyleChooserCellRenderer());

        informationBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                WMSLayer l = null;
                if (value instanceof WMSLayer) {
                    l = ((WMSLayer) value);
                } else if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() == 1) {
                    l = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0));
                }
                l.setQuerySelected(informationBox.isSelected());
                ActiveLayerEvent ale = new ActiveLayerEvent();
                ale.setLayer(l.getParentServiceLayer());
                CismapBroker.getInstance().fireLayerInformationStatusChanged(ale);
            }
        });

        cbbStyleChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    WMSLayer l = null;
                    if (value instanceof WMSLayer) {
                        l = ((WMSLayer) value);
                    } else if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() == 1) {
                        l = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0));
                    }
                    if (!(l.getSelectedStyle().equals((Style) cbbStyleChooser.getSelectedItem()))) {
                        ActiveLayerEvent ale = new ActiveLayerEvent();
                        ale.setLayer(l.getParentServiceLayer());
                        //CismapBroker.getInstance().fireLayerRemoved(ale);
                        l.setSelectedStyle((Style) cbbStyleChooser.getSelectedItem());
                        ((de.cismet.cismap.commons.retrieval.RetrievalService) value).retrieve(true);
                        //CismapBroker.getInstance().fireLayerAdded(ale);
                        CismapBroker.getInstance().fireLayerInformationStatusChanged(ale);
                    }
                } catch (Exception ex) {
                    log.error("Fehler beim Aendern des Styles", ex);
                }

            }
        });

        moreButton.setFocusPainted(false);
        moreButton.setEnabled(false);
        moreButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("MORE"+value);
                log.debug("FeatureService-QueryEditorDialog aufrufen");
                QueryEditorDialog dia = new QueryEditorDialog(StaticSwingTools.getParentFrame(moreButton), true,
                        ((WebFeatureService) value).getWfsQueryString());
                dia.setVisible(true);
                if (dia.getReturnStatus() == QueryEditorDialog.RET_OK) {
                    ((WebFeatureService) value).setWfsQueryString(dia.getQueryString());
                }
            }
        });

        wfsStyleButton.setFocusPainted(false);
        wfsStyleButton.setEnabled(true);
        wfsStyleButton.setBorderPainted(false);
        wfsStyleButton.setContentAreaFilled(false);
        wfsStyleButton.setIconTextGap(0);
        wfsStyleButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Runnable t = new Runnable() {

                        @Override
                        public void run() {
                            WebFeatureService tempWFS = (WebFeatureService) value;
                            log.debug("FeatureService-StyleDialog aufrufen");
                            if (styleDialog == null) {
                                styleDialog = new StyleDialog(StaticSwingTools.getParentFrame(wfsStyleButton), true);
                            }
                            styleDialog.setSelectedAttributes(WFSOperator.getPropertyNamesFromQuery(tempWFS.getWfsQuery()));
                            styleDialog.setSelectedGeoAttribute(WFSOperator.getGeometry(tempWFS.getWfsQuery()));
                            styleDialog.setAttributes(tempWFS.getAttributes());

                            // Stylefeature übergeben
                            CloneableFeature feat = tempWFS.getRenderingFeature();
                            styleDialog.setFeature(feat);

                            // TODO Anzeige-Regeln des WFS sollten hier gesetzt werden

                            styleDialog.setVisible(true);
                            if (styleDialog.getReturnStatus() != null) {
                                if (styleDialog.isQueryChanged()) {
                                    int i = JOptionPane.showConfirmDialog(StaticSwingTools.getParentFrame(wfsStyleButton), "Sollen die Änderungen im QueryString überschrieben werden ?", "Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                    if (i == JOptionPane.YES_OPTION) {
                                        tempWFS.setRenderingFeature(styleDialog.getReturnStatus());
                                        tempWFS.refreshFeatures();
                                        Element query = tempWFS.getWfsQuery();
                                        WFSOperator.setGeometry(query, styleDialog.getSelectedGeoAttribute());
                                        tempWFS.setWfsQuery(WFSOperator.changePropertyNames(query, styleDialog.getSelectedAttributes()));
                                    } else if (i == JOptionPane.NO_OPTION) {
                                        tempWFS.setWfsQueryString(styleDialog.getQueryString());
                                        tempWFS.setRenderingFeature(styleDialog.getReturnStatus());
                                        tempWFS.refreshFeatures();
                                    }
                                } else {
                                    tempWFS.setRenderingFeature(styleDialog.getReturnStatus());
                                    tempWFS.refreshFeatures();
                                    Element query = tempWFS.getWfsQuery();
                                    WFSOperator.setGeometry(query, styleDialog.getSelectedGeoAttribute());
                                    tempWFS.setWfsQuery(WFSOperator.changePropertyNames(query, styleDialog.getSelectedAttributes()));
                                }
                            }
                        }
                    };
                    CismetThreadPool.execute(t);
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        informationCellEditor = new DefaultCellEditor(informationBox);
        stylesCellEditor = new DefaultCellEditor(cbbStyleChooser);

        progress.setLayout(new BorderLayout(2, 2));
        progress.add(slider, BorderLayout.CENTER);
        slider.setOpaque(false);
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();

                if (value instanceof RetrievalServiceLayer) {
                    float f = (float) (slider.getValue() * 0.01);
                    ((RetrievalServiceLayer) value).setTranslucency(f);
                    PNode pi = ((RetrievalServiceLayer) value).getPNode();
                    if (pi != null) {
                        pi.setTransparency(f);
                        pi.repaint();
                    }
                    //log.debug("Slider:"+f);
                }

                if (!slider.getValueIsAdjusting()) {
                }
            }
        });
        slider.addMouseListener(new MouseListener() {

            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                log.debug("Click" + e);
            }
        });
        slider.dispatchEvent(new MouseEvent(slider, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                MouseEvent.BUTTON1_MASK, 3, 2, 1, false, MouseEvent.BUTTON1));
    }

    /**
     * Sets an initial <I>value</I> for the editor.  This will cause
     * the editor to stopEditing and lose any partially edited value
     * if the editor is editing when this method is called. <p>
     * 
     * Returns the component that should be added to the client's
     * Component hierarchy.  Once installed in the client's hierarchy
     * this component will then be able to draw and receive user input.
     * 
     * @param	tree		the JTree that is asking the editor to edit;
     * 				this parameter can be null
     * @param	value		the value of the cell to be edited
     * @param	isSelected	true is the cell is to be renderer with
     * 				selection highlighting
     * @param	expanded	true if the node is expanded
     * @param	leaf		true if the node is a leaf node
     * @param	row		the row index of the node being edited
     * @return	the component for editing
     */
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
            boolean expanded, boolean leaf, int row) {
        log.debug("TreeCellEditor requested");
        JTextField treeEditorTextField = new JTextField();
        treeEditorTextField.setEditable(true);
        DefaultCellEditor treeEditor = new DefaultCellEditor(treeEditorTextField);
        return treeEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    /**
     *  Sets an initial <code>value</code> for the editor.  This will cause
     *  the editor to <code>stopEditing</code> and lose any partially
     *  edited value if the editor is editing when this method is called. <p>
     * 
     *  Returns the component that should be added to the client's
     *  <code>Component</code> hierarchy.  Once installed in the client's
     *  hierarchy this component will then be able to draw and receive
     *  user input.
     * 
     * @param	table		the <code>JTable</code> that is asking the
     * 				editor to edit; can be <code>null</code>
     * @param	value		the value of the cell to be edited; it is
     * 				up to the specific editor to interpret
     * 				and draw the value.  For example, if value is
     * 				the string "true", it could be rendered as a
     * 				string or it could be rendered as a check
     * 				box that is checked.  <code>null</code>
     * 				is a valid value
     * @param	isSelected	true if the cell is to be rendered with
     * 				highlighting
     * @param	row     	the row of the cell being edited
     * @param	column  	the column of the cell being edited
     * @return	the component for editing
     */
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, boolean isSelected, int row, int column) {
        int realColumn = table.convertColumnIndexToModel(column);
        this.value = value;
        this.table = table;
        WMSLayer wmsLayer = null;
        if (value instanceof WMSLayer) {
            wmsLayer = ((WMSLayer) value);
        } else if (value instanceof WMSServiceLayer) {
            wmsLayer = (WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0);
        }
        if (realColumn == 0) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            visibilityLabel.setIcon(((JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, isSelected, row, column)).getIcon());
            return visibilityLabel;
        } else if (realColumn == 2) {
            log.debug("Editor Spalte=" + realColumn);
            if (value instanceof WMSServiceLayer) {
                try {
                    log.debug("Combo");
                    DefaultComboBoxModel model = new DefaultComboBoxModel(wmsLayer.getOgcCapabilitiesLayer().getStyles());
                    cbbStyleChooser.setModel(model);
                    cbbStyleChooser.setSelectedItem(wmsLayer.getSelectedStyle());
                    return stylesCellEditor.getTableCellEditorComponent(table, wmsLayer.getSelectedStyle(), isSelected, row, column);
                } catch (Exception e) {
                    log.warn("Fehler beim setzen des StyleEditors", e);
                    return null;
                }
            } else {
                log.debug("StyleButton");
                return wfsStyleButton;
            }
        } else if (realColumn == 3) {
            return informationCellEditor.getTableCellEditorComponent(table, new Boolean(wmsLayer.isQuerySelected()), isSelected, row, column);
        } else if (realColumn == 4) {
            if (wmsServiceLayerThatFiresPropertyChangeEvents != null) {
                wmsServiceLayerThatFiresPropertyChangeEvents.removePropertyChangeListener(this);
            }
            ((RetrievalServiceLayer) value).addPropertyChangeListener(this);
            wmsServiceLayerThatFiresPropertyChangeEvents = ((RetrievalServiceLayer) value);
            slider.setValue((int) (((RetrievalServiceLayer) value).getTranslucency() * 100));
            slider.requestFocus();
            if (((RetrievalServiceLayer) value).getProgress() == 0) {
                progress.setIndeterminate(true);
            } else {
                progress.setIndeterminate(false);
            }
            progress.setValue(((RetrievalServiceLayer) value).getProgress());
            slider.setSize(progress.getSize());
            slider.repaint();
            return progress;
        } else if (realColumn == 5) {
            if (value instanceof WMSServiceLayer) {
                moreButton.setEnabled(false);
            } else if (value instanceof WebFeatureService) {
                moreButton.setEnabled(true);
            }
            return moreButton;
        } else {
            return null;
        }
    }

    /**
     * Returns the value contained in the editor.
     * @return the value contained in the editor
     */
    @Override
    public Object getCellEditorValue() {
        return value;
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("Progressvalue in Editor changed");
        if (evt.getSource() instanceof RetrievalServiceLayer && evt.getPropertyName().equals("progress")) {
            int newValue = ((Integer) (evt.getNewValue())).intValue();
            if (newValue == 0) {
                progress.setIndeterminate(true);
            } else {
                progress.setIndeterminate(false);
                progress.setValue(newValue);
            }
        }
    }

    /**
     * Calls <code>fireEditingCanceled</code>.
     */
    @Override
    public void cancelCellEditing() {
        if (wmsServiceLayerThatFiresPropertyChangeEvents != null) {
            wmsServiceLayerThatFiresPropertyChangeEvents.removePropertyChangeListener(this);
        }
        super.cancelCellEditing();
    }

    /**
     * Calls <code>fireEditingStopped</code> and returns true.
     * @return true
     */
    @Override
    public boolean stopCellEditing() {
        if (wmsServiceLayerThatFiresPropertyChangeEvents != null) {
            wmsServiceLayerThatFiresPropertyChangeEvents.removePropertyChangeListener(this);
        }
        boolean retValue;
        try {
            retValue = super.stopCellEditing();
        } finally {
        }
        return retValue;
    }
}

class StyleChooserCellRenderer extends DefaultListCellRenderer {

    private ImageIcon styleIcon;

    public StyleChooserCellRenderer() {
        styleIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/style.png"));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object listValue, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel retValue;
        retValue = (JLabel) super.getListCellRendererComponent(list, listValue, index, isSelected, cellHasFocus);
        retValue.setText(((Style) listValue).getTitle());
        retValue.setIcon(styleIcon);
        retValue.setIconTextGap(4);
        return retValue;
    }
}
