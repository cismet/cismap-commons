/*
 * SimpleInternalLayerWidget.java
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
 * Created on 22. August 2005, 12:13
 *
 */
package de.cismet.cismap.commons.gui.simplelayerwidget;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelEvent;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.features.AnnotatedFeature;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.capabilitywidget.WFSSelectionAndCapabilities;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerTableCellEditor;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerTableCellRenderer;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerTreeCellRenderer;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;
import de.cismet.tools.gui.treetable.JTreeTable;
import de.cismet.tools.gui.treetable.TreeTableCellEditor;
import de.cismet.tools.gui.treetable.TreeTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.tree.TreePath;
import org.jdom.Element;

/**
 * 
 * @author nh
 */
public class NewSimpleInternalLayerWidget extends JInternalFrame implements MappingModelListener, TableModelListener, DropTargetListener, Configurable {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final ImageIcon UP = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/up.png"));
    private final ImageIcon DOWN = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/down.png"));
    private final ImageIcon DELETE = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/removeLayer.png"));
    private final ImageIcon DISABLE = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disable.png"));
    private final ImageIcon INVISIBLE = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerDLinvisible.png"));
    private static final int ROW_HEIGHT = 18;
    private static final int WIDGET_WIDTH = 350;
    private int layerCount = 0;
    private boolean deactivatePopupMenuButtons = true;
    private ActiveLayerModel activeLayerModel;
    private JTreeTable treeTable;
    private Image errorImage;
    private MappingComponent mc;
    private JPopupMenu popupMenu;
    private JMenuItem up,  down,  del,  vis,  dis;

    public NewSimpleInternalLayerWidget(MappingComponent mc) {
        this(mc, true);
    }

    /**
     * Erzeugt eine neue NewSimpleInternalLayerWidget-Instanz.
     * @param mc Parent-MappingComponent des Wdgets
     */
    public NewSimpleInternalLayerWidget(MappingComponent mc, boolean deactivatePopupMenuButtons) {
        log.info("SimpleInternalLayerWidget erstellen");
        this.deactivatePopupMenuButtons = deactivatePopupMenuButtons;

        // JInternalFrame fixieren, indem alle MouseMotionListener der Statusbar entfernt werden
        try {
            BasicInternalFrameUI fui = (BasicInternalFrameUI) getUI();
            Component north = fui.getNorthPane();
            MouseMotionListener[] listener = (MouseMotionListener[]) north.getListeners(MouseMotionListener.class);
            for (int i = 0; i < listener.length; i++) {
                north.removeMouseMotionListener(listener[i]);
            }
        } catch (Exception e) {
            log.error("Fehler beim Entfernen von Mousemotionlisteners", e);
        }
        initComponents();
        this.mc = mc;
        activeLayerModel = (ActiveLayerModel) mc.getMappingModel();
        popupMenu = new JPopupMenu();
        up = new JMenuItem();
        up.setText("nach oben");
        up.setIcon(UP);
        up.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final TreePath tp = treeTable.getTree().getSelectionPath();
                if (tp != null) {
                    activeLayerModel.moveLayerUp(tp);
                }
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        treeTable.getTree().setSelectionPath(tp);
                        StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                    }
                });
            }
        });
        down = new JMenuItem();
        down.setText("nach unten");
        down.setIcon(DOWN);
        down.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final TreePath tp = treeTable.getTree().getSelectionPath();
                if (tp != null) {
                    activeLayerModel.moveLayerDown(tp);
                }
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        treeTable.getTree().setSelectionPath(tp);
                        StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                    }
                });
            }
        });
        dis = new JMenuItem();
        dis.setText("deaktivieren");
        dis.setIcon(DISABLE);
        dis.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final TreePath tp = treeTable.getTree().getSelectionPath();
                if (tp != null) {
                    activeLayerModel.disableLayer(tp);
                }
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        treeTable.getTree().setSelectionPath(tp);
                    }
                });
            }
        });
        del = new JMenuItem();
        del.setText("entfernen");
        del.setIcon(DELETE);
        del.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TreePath tp = treeTable.getTree().getSelectionPath();
                final int row = treeTable.getSelectedRow();
                if (tp != null) {
                    activeLayerModel.removeLayer(tp);
                }
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        treeTable.getSelectionModel().setSelectionInterval(row, row);
                    }
                });
            }
        });
        vis = new JMenuItem();
        vis.setText("unsichtbar");
        vis.setIcon(INVISIBLE);
        vis.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final TreePath tp = treeTable.getTree().getSelectionPath();
                if (tp != null) {
                    activeLayerModel.handleVisibility(tp);
                }
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        treeTable.getTree().setSelectionPath(tp);
                    }
                });
            }
        });
        popupMenu.add(up);
        popupMenu.add(down);
        popupMenu.addSeparator();
        popupMenu.add(dis);
        if (!deactivatePopupMenuButtons) {
            popupMenu.add(del);
        }
        popupMenu.add(vis);

        try
        {
          putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
  //        DropTarget dt = new DropTarget(this, acceptableActions, this);

        }
        catch(Throwable t)
        {
          log.error("Fehler beim setzen der Client Property isPalette: " + t.getMessage(), t);
        }

        if (activeLayerModel != null) {
            createTree();
        }
        pack();
    }

    /**
     * Setzt das ActiveLayerModel des Widgets neu und erzeugt darauf basierend
     * einen LayerTree.
     * @param alm das neue ActiveLayerModel
     */
    public void setMappingModel(MappingModel mm) {
        if (mm instanceof ActiveLayerModel) {
            this.activeLayerModel = (ActiveLayerModel) mm;
            createTree();
        } else {
            log.info("MappingModel ist kein ActiveLayerModel, kann InternalWidget nicht erstellen");
        }
    }

    /**
     * Returns whether the buttons "disable" and "delete" are not shown in the
     * TreeTable popup menu.
     * @return true, if the buttons are hidden, else false
     */
    public boolean isDeactivatePopupMenuButtons() {
        return this.deactivatePopupMenuButtons;
    }

    /**
     * Hides or shows the buttons "disable" and "delete" in the TreeTable popup menu.
     * @param flag the new boolean value (true hides, false shows)
     */
    public void setDeactivatePopupMenuButtons(boolean deactivate) {
        log.debug("setDeactivatePopupMenuButtons");
        if (!deactivatePopupMenuButtons && deactivate) {
            //popupMenu.remove(dis);
            popupMenu.remove(del);
        } else if (deactivatePopupMenuButtons && !deactivate) {
            popupMenu.remove(vis);
            //popupMenu.add(dis);
            popupMenu.add(del);
            popupMenu.add(vis);
        }
        this.deactivatePopupMenuButtons = deactivate;
    }

    /**
     * Erstellt die TreeTable, nachdem ein ActiveLayerModel gesetzt wurde.
     */
    private void createTree() {
        try {
            treeTable = new JTreeTable(activeLayerModel) {

                @Override
                public JToolTip createToolTip() {
                    log.debug("Tooltip");
                    if (errorImage != null) {
                        return new ImageToolTip(errorImage);
                    } else {
                        return super.createToolTip();
                    }
                }
            };
            treeTable.setAutoCreateColumnsFromModel(true);
            treeTable.setShowGrid(true);
            treeTable.setGridColor(getBackground());
            treeTable.setTableHeader(null);
            treeTable.getTree().setShowsRootHandles(true);
            treeTable.getTree().setRootVisible(false);
            treeTable.getTree().setCellRenderer(new ActiveLayerTreeCellRenderer());
            treeTable.getModel().addTableModelListener(this);
            activeLayerModel.addMappingModelListener(this);
            ActiveLayerTableCellEditor cellEditor = new ActiveLayerTableCellEditor();
            ActiveLayerTableCellRenderer tableCellRenderer = new ActiveLayerTableCellRenderer(true);
            TreeTableCellEditor treeTableCellEditor = new TreeTableCellEditor(treeTable, treeTable.getTree());
            treeTableCellEditor.setClickCountToStart(2);
            treeTable.setDefaultEditor(TreeTableModel.class, treeTableCellEditor);
            treeTable.getColumnModel().removeColumn(treeTable.getColumnModel().getColumn(5));
            treeTable.getColumnModel().removeColumn(treeTable.getColumnModel().getColumn(3));
            treeTable.getColumnModel().removeColumn(treeTable.getColumnModel().getColumn(2));
            treeTable.getColumnModel().getColumn(0).setMaxWidth(20);
            treeTable.getColumnModel().getColumn(2).setMaxWidth(100);
            treeTable.getColumnModel().getColumn(0).setCellEditor(cellEditor);
            treeTable.getColumnModel().getColumn(2).setCellEditor(cellEditor); // vorher getColumn(5)
            treeTable.getColumnModel().getColumn(0).setCellRenderer(tableCellRenderer);
            treeTable.getColumnModel().getColumn(2).setCellRenderer(tableCellRenderer);
            treeTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
            treeTable.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

                public void valueChanged(TreeSelectionEvent e) {
                    if (treeTable.getTree().getSelectionPath() != null) {
                        log.debug("ActiveLayerWidget: selectionChanged()\n" + e);
                        try {
                            ActiveLayerEvent ale = new ActiveLayerEvent();
                            ale.setLayer(treeTable.getTree().getSelectionPath().getLastPathComponent());
                            if (ale.getLayer() instanceof WMSServiceLayer) {
                                ale.setCapabilities(((WMSServiceLayer) ale.getLayer()).getWmsCapabilities());
                            }
                            CismapBroker.getInstance().fireLayerSelectionChanged(ale);
                        } catch (Exception ex) {
                            log.warn("Fehler bei fireLayerSelectionChanged ... kein Problem");
                        }
                    }
                }
            });
            treeTable.setGridColor(this.getBackground());

            treeTable.addMouseListener(new MouseListener() {

                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                public void mouseClicked(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }
            });

            reshapeWidget(true);
            scpMain.setViewportView(treeTable);

            addComponentListener(new ComponentListener() {

                public void componentResized(ComponentEvent e) {
                    treeTable.repaint();
                }

                public void componentMoved(ComponentEvent e) {
                }

                public void componentShown(ComponentEvent e) {
                }

                public void componentHidden(ComponentEvent e) {
                }
            });
        } catch (Exception ex) {
            log.error("Fehler beim erstellen der TreeTable!", ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scpMain = new javax.swing.JScrollPane();

        setMinimumSize(new java.awt.Dimension(350, 50));
        setPreferredSize(new java.awt.Dimension(350, 100));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        scpMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(scpMain, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
    }//GEN-LAST:event_formKeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scpMain;
    // End of variables declaration//GEN-END:variables

    public void dragEnter(DropTargetDragEvent dtde) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void drop(DropTargetDropEvent dtde) {
        DataFlavor TREEPATH_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "SelectionAndCapabilities");
        try {
            log.debug(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.log.Es_gibt") + dtde.getTransferable().getTransferDataFlavors().length +
                    java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.log.DataFlavours"));
            for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                log.debug(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.log.DataFlavour") + i + ":" + dtde.getTransferable().getTransferDataFlavors()[i]);
            }
            final Object o = dtde.getTransferable().getTransferData(TREEPATH_FLAVOR);
            final Vector v = new Vector();
            dtde.dropComplete(true);
            if (o instanceof SelectionAndCapabilities) {
                TreePath[] tpa = ((SelectionAndCapabilities) o).getSelection();
                for (int i = 0; i < tpa.length; ++i) {
                    v.add(tpa[i]);
                }
                WMSServiceLayer l = new WMSServiceLayer(v);
                if (l.getWMSLayers().size() > 0) {
                    if (treeTable.getEditingRow() != -1 && treeTable.getEditingColumn() != -1) {
                        treeTable.getCellEditor(treeTable.getEditingRow(), treeTable.getEditingColumn()).stopCellEditing();
                    }
                    l.setWmsCapabilities(((SelectionAndCapabilities) o).getCapabilities());
                    activeLayerModel.addLayer(l);
                }
                l.setWmsCapabilities(((SelectionAndCapabilities) o).getCapabilities());
                l.setCapabilitiesUrl(((SelectionAndCapabilities) o).getUrl());
                log.debug("((SelectionAndCapabilities)o).getUrl()" + ((SelectionAndCapabilities) o).getUrl());
            } // Drop-Objekt war ein WFS-Element
            else if (o instanceof WFSSelectionAndCapabilities) {
                WFSSelectionAndCapabilities sac = (WFSSelectionAndCapabilities) o;
                WebFeatureService wfs = new WebFeatureService(sac.getName(), sac.getHost(), sac.getQuery(), sac.getAttributes());
                if (wfs.getRenderingFeature() instanceof AnnotatedFeature) {
                    ((AnnotatedFeature) wfs.getRenderingFeature()).setPrimaryAnnotation(sac.getIdentifier());
                }
                activeLayerModel.addLayer(wfs);
            }
            scpMain.setViewportView(treeTable);
        } catch (IllegalArgumentException schonVorhanden) {
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this), java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.LayerAlreadyExists"),
                    java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AddLayer"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public void configure(Element parent) {
        activeLayerModel.configure(parent);
    }

    public void masterConfigure(Element parent) {
        activeLayerModel.masterConfigure(parent);
    }

    public Element getConfiguration() throws NoWriteError {
        return activeLayerModel.getConfiguration();
    }

    public void tableChanged(TableModelEvent e) {
        e.getColumn();
        treeTable.repaint(treeTable.getBounds());
    }

    private void reshapeWidget(boolean add) {
        Dimension pref = treeTable.getPreferredSize();
        pref.setSize(WIDGET_WIDTH, pref.height + 23);
        setPreferredSize(pref);
        int posX = mc.getWidth() - getWidth() - 1;
        int posY = mc.getHeight() - getHeight() - 1;
        if (add) {
            setBounds(posX, posY - ROW_HEIGHT, pref.width, pref.height + ROW_HEIGHT);
        } else {
            setBounds(posX, posY + ROW_HEIGHT, pref.width, pref.height);
        }
    }

    public void setErrorImage(Image errorImage) {
        this.errorImage = errorImage;
    }

    public void mapServiceLayerStructureChanged(MappingModelEvent mme) {
    }

    public void mapServiceAdded(MapService mapService) {
        reshapeWidget(true);
    }

    public void mapServiceRemoved(MapService mapService) {
        reshapeWidget(false);
    }
}
