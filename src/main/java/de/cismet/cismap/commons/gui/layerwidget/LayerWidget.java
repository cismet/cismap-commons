/*
 * LayerWidget.java
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
 * Created on 8. November 2005, 14:31
 *
 */
package de.cismet.cismap.commons.gui.layerwidget;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.featureservice.DocumentFeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.capabilitywidget.WFSSelectionAndCapabilities;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.util.DnDUtils;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;
import de.cismet.tools.gui.treetable.JTreeTable;
import de.cismet.tools.gui.treetable.TreeTableCellEditor;
import de.cismet.tools.gui.treetable.TreeTableModel;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.jdom.Element;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class LayerWidget extends JPanel implements DropTargetListener, Configurable {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DragSource dragSource;
    private DragGestureListener dgListener;
    private DragSourceListener dsListener;
    private ActiveLayerModel activeLayerModel = new ActiveLayerModel();
    private JTreeTable treeTable;
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private Image errorImage;
    private MappingComponent mapC = null;

    /** Creates new form LayerWidget */
    public LayerWidget(MappingComponent mapC) {
        hackDragAndDropDataFlavors();
        initComponents();
        this.mapC = mapC;
        log.info("LayerWidget: " + activeLayerModel);//NOI18N
        DropTarget dt = new DropTarget(this, acceptableActions, this);

        treeTable = new JTreeTable(activeLayerModel) {

            @Override
            public JToolTip createToolTip() {
                log.debug("Tooltip");//NOI18N
                if (getErrorImage() != null) {
                    return new ImageToolTip(getErrorImage());
                } else {
                    return super.createToolTip();
                }
            }
        };

        treeTable.setAutoCreateColumnsFromModel(true);
        treeTable.setShowGrid(true);
        treeTable.getTableHeader().setReorderingAllowed(true);
        treeTable.getTree().setShowsRootHandles(true);
        treeTable.getTree().setRootVisible(false);
        treeTable.getTree().setCellRenderer(new ActiveLayerTreeCellRenderer());
        ActiveLayerTableCellEditor cellEditor = new ActiveLayerTableCellEditor();
        //treeTable.getTree().setCellEditor(cellEditor);
//        treeTable.getTree().setEditable(true);
        TreeTableCellEditor treeTableCellEditor = new TreeTableCellEditor(treeTable, treeTable.getTree());
        treeTableCellEditor.setClickCountToStart(2);
        treeTable.setDefaultEditor(TreeTableModel.class, treeTableCellEditor);
        treeTable.getColumnModel().getColumn(0).setMaxWidth(20);
        treeTable.getColumnModel().getColumn(3).setMaxWidth(50);
        treeTable.getColumnModel().getColumn(5).setMaxWidth(50);
        treeTable.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(2).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(3).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(4).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(5).setCellEditor(cellEditor);
        ActiveLayerTableCellRenderer tableCellRenderer = new ActiveLayerTableCellRenderer();
        treeTable.getColumnModel().getColumn(0).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(2).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(3).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(4).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(5).setCellRenderer(tableCellRenderer);

//        treeTable.setShowHorizontaLines(true);
        treeTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        scpMain.setViewportView(treeTable);

        treeTable.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {

                if (treeTable.getTree().getSelectionPath() != null) {
                    log.debug("ActiveLayerWidget: selectionChanged()\n" + e);//NOI18N
                    try {
                        ActiveLayerEvent ale = new ActiveLayerEvent();
                        ale.setLayer(treeTable.getTree().getSelectionPath().getLastPathComponent());
                        if (ale.getLayer() instanceof WMSServiceLayer) {
                            ale.setCapabilities(((WMSServiceLayer) ale.getLayer()).getWmsCapabilities());
                        }
                        CismapBroker.getInstance().fireLayerSelectionChanged(ale);
                    } catch (Exception ex) {
                    }
                }
            }
        });

        cmdZoomToFullExtent.setVisible(false);
        treeTable.setGridColor(this.getBackground());
        addComponentListener(new ComponentListener() {

            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentResized(ComponentEvent e) {
//                treeTable.repaint();
            }

            public void componentShown(ComponentEvent e) {
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scpMain = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        cmdTreeCollapse = new javax.swing.JButton();
        cmdRefreshSingleLayer = new javax.swing.JButton();
        cmdDown = new javax.swing.JButton();
        cmdUp = new javax.swing.JButton();
        cmdZoomToFullExtent = new javax.swing.JButton();
        cmdDisable = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        cmdMakeInvisible = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        setPreferredSize(new java.awt.Dimension(211, 114));
        setLayout(new java.awt.BorderLayout());

        scpMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scpMain.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(scpMain, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jToolBar1.setRollover(true);

        cmdTreeCollapse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/collapseTree.png"))); // NOI18N
        cmdTreeCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdTreeCollapse.toolTipText")); // NOI18N
        cmdTreeCollapse.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdTreeCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdTreeCollapseActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdTreeCollapse);

        cmdRefreshSingleLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/refresh.png"))); // NOI18N
        cmdRefreshSingleLayer.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdRefreshSingleLayer.toolTipText")); // NOI18N
        cmdRefreshSingleLayer.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRefreshSingleLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRefreshSingleLayerActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdRefreshSingleLayer);

        cmdDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/down.png"))); // NOI18N
        cmdDown.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdDown.toolTipText")); // NOI18N
        cmdDown.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDownActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdDown);

        cmdUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/up.png"))); // NOI18N
        cmdUp.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdUp.toolTipText")); // NOI18N
        cmdUp.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdUpActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdUp);

        cmdZoomToFullExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerZoom.png"))); // NOI18N
        cmdZoomToFullExtent.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdZoomToFullExtent.toolTipText")); // NOI18N
        cmdZoomToFullExtent.setMargin(new java.awt.Insets(2, 1, 2, 1));
        jToolBar1.add(cmdZoomToFullExtent);

        cmdDisable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disable.png"))); // NOI18N
        cmdDisable.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdDisable.toolTipText")); // NOI18N
        cmdDisable.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdDisable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDisableActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdDisable);

        cmdRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/removeLayer.png"))); // NOI18N
        cmdRemove.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdRemove.toolTipText")); // NOI18N
        cmdRemove.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdRemove);

        cmdMakeInvisible.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerDLinvisible.png"))); // NOI18N
        cmdMakeInvisible.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdMakeInvisible.toolTipText")); // NOI18N
        cmdMakeInvisible.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdMakeInvisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMakeInvisibleActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdMakeInvisible);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    public void removeAllLayers() {
        activeLayerModel.removeAllLayers();
    }

    private void cmdRefreshSingleLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRefreshSingleLayerActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null && tp.getLastPathComponent() instanceof RetrievalServiceLayer) {
            ((MapService) tp.getLastPathComponent()).setBoundingBox(mapC.getCurrentBoundingBox());
            ((RetrievalServiceLayer) tp.getLastPathComponent()).retrieve(true);
        } else if (tp != null && tp.getParentPath().getLastPathComponent() instanceof RetrievalServiceLayer) {
            ((RetrievalServiceLayer) tp.getParentPath().getLastPathComponent()).retrieve(true);
        }
        if (EventQueue.isDispatchThread()){log.fatal("InvokeLater in EDT");}//NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                treeTable.getTree().setSelectionPath(tp);
                StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
            }
        });
    }//GEN-LAST:event_cmdRefreshSingleLayerActionPerformed

    private void cmdDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDownActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.moveLayerDown(tp);
        }
        if (EventQueue.isDispatchThread()){log.fatal("InvokeLater in EDT");}//NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                treeTable.getTree().setSelectionPath(tp);
                StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
            }
        });
    }//GEN-LAST:event_cmdDownActionPerformed

    private void cmdUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUpActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.moveLayerUp(tp);
        }
        if (EventQueue.isDispatchThread()){log.fatal("InvokeLater in EDT");}//NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                treeTable.getTree().setSelectionPath(tp);
                StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
            }
        });
    }//GEN-LAST:event_cmdUpActionPerformed

    private void cmdMakeInvisibleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMakeInvisibleActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.handleVisibility(tp);
        }
        if (EventQueue.isDispatchThread()){log.fatal("InvokeLater in EDT");}//NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                treeTable.getTree().setSelectionPath(tp);
            }
        });
    }//GEN-LAST:event_cmdMakeInvisibleActionPerformed

    private void cmdDisableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDisableActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.disableLayer(tp);
        }
        if (EventQueue.isDispatchThread()){log.fatal("InvokeLater in EDT");}//NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                treeTable.getTree().setSelectionPath(tp);
            }
        });
    }//GEN-LAST:event_cmdDisableActionPerformed

    private void cmdTreeCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdTreeCollapseActionPerformed
        StaticSwingTools.jTreeCollapseAllNodes(treeTable.getTree());
    }//GEN-LAST:event_cmdTreeCollapseActionPerformed

    private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveActionPerformed
        TreePath tp = treeTable.getTree().getSelectionPath();
        final int row = treeTable.getSelectedRow();
        if (tp != null) {
            activeLayerModel.removeLayer(tp);
        }
        if (EventQueue.isDispatchThread()){log.fatal("InvokeLater in EDT");}//NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                treeTable.getSelectionModel().setSelectionInterval(row, row);
            }
        });
    }//GEN-LAST:event_cmdRemoveActionPerformed

    public ActiveLayerModel getMappingModel() {
        return activeLayerModel;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdDisable;
    private javax.swing.JButton cmdDown;
    private javax.swing.JButton cmdMakeInvisible;
    private javax.swing.JButton cmdRefreshSingleLayer;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JButton cmdTreeCollapse;
    private javax.swing.JButton cmdUp;
    private javax.swing.JButton cmdZoomToFullExtent;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane scpMain;
    // End of variables declaration//GEN-END:variables

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Tester().setVisible(true);
            }
        });
    }
    private static DataFlavor uriListFlavor;

    static {
        try {
            uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");//NOI18N
        } catch (ClassNotFoundException e) { // can't happen
            e.printStackTrace();
        }
    }

    /**
     * Called when the drag operation has terminated with a drop on
     * the operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     * <p>
     * This method is responsible for undertaking
     * the transfer of the data associated with the
     * gesture. The <code>DropTargetDropEvent</code>
     * provides a means to obtain a <code>Transferable</code>
     * object that represents the data object(s) to
     * be transfered.<P>
     * From this method, the <code>DropTargetListener</code>
     * shall accept or reject the drop via the
     * acceptDrop(int dropAction) or rejectDrop() methods of the
     * <code>DropTargetDropEvent</code> parameter.
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code>'s getTransferable()
     * method may be invoked, and data transfer may be
     * performed via the returned <code>Transferable</code>'s
     * getTransferData() method.
     * <P>
     * At the completion of a drop, an implementation
     * of this method is required to signal the success/failure
     * of the drop by passing an appropriate
     * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.
     * <P>
     * Note: The data transfer should be completed before the call  to the
     * <code>DropTargetDropEvent</code>'s dropComplete(boolean success) method.
     * After that, a call to the getTransferData() method of the
     * <code>Transferable</code> returned by
     * <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to
     * succeed only if the data transfer is local; that is, only if
     * <code>DropTargetDropEvent.isLocalTransfer()</code> returns
     * <code>true</code>. Otherwise, the behavior of the call is
     * implementation-dependent.
     * <P>
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    @Override
    public void drop(final java.awt.dnd.DropTargetDropEvent dtde) {
        DataFlavor TREEPATH_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "SelectionAndCapabilities");//NOI18N
        log.debug("Drop with this flavors:" + dtde.getCurrentDataFlavorsAsList());//NOI18N
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            try {
                List<File> data = null;
                Transferable transferable = dtde.getTransferable();
                if (dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                    data = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                } else {
                    if (dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                        log.debug("Drop is unix drop xxx " + transferable.getTransferData(DataFlavor.javaFileListFlavor));//NOI18N

                        data = DnDUtils.textURIListToFileList((String) transferable.getTransferData(DnDUtils.URI_LIST_FLAVOR));
                    } else {
                        log.debug("Drop is windows drop");//NOI18N
                        data = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    }
                }
                log.debug("Drag&Drop File List: " + data);//NOI18N
                if (data != null) {
                    for (File currentFile : data) {
                        //NO HARDCODING
                        try {
                            log.info("DocumentUri: " + currentFile.toURI());//NOI18N
                            //GMLFeatureService gfs = new GMLFeatureService(currentFile.getName(),currentFile.toURI(),null);
                            //langsam sollte nicht im EDT ausgeführt werden
                            DocumentFeatureService dfs = DocumentFeatureServiceFactory.createDocumentFeatureService(currentFile);
                            activeLayerModel.addLayer(dfs);
                        } catch (Exception ex) {
                            log.error("Error during creation of a FeatureServices", ex);//NOI18N
                        }
                    }
                } else {
                    log.warn("No files available");//NOI18N
                }
            } catch (Exception ex) {
                log.error("Failure during drag & drop opertation", ex);//NOI18N
            }
        } else if (dtde.isDataFlavorSupported(TREEPATH_FLAVOR)) {
            try {
                log.debug("There are " + dtde.getTransferable().getTransferDataFlavors().length + " DataFlavours");//NOI18N
                for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                    log.debug("DataFlavour" + i + ": " + dtde.getTransferable().getTransferDataFlavors()[i]);//NOI18N
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
                            try {
                            treeTable.getCellEditor(treeTable.getEditingRow(), treeTable.getEditingColumn()).stopCellEditing();
                            }
                            catch (Exception e){
                                //stopCellEditing went wrong. I don't care ;-)
                            }
                        }
                        l.setWmsCapabilities(((SelectionAndCapabilities) o).getCapabilities());
                        activeLayerModel.addLayer(l);
                    }
                    l.setWmsCapabilities(((SelectionAndCapabilities) o).getCapabilities());
                    l.setCapabilitiesUrl(((SelectionAndCapabilities) o).getUrl());
                    log.debug("((SelectionAndCapabilities)o).getUrl()" + ((SelectionAndCapabilities) o).getUrl());//NOI18N
                } // Drop-Objekt war ein WFS-Element
                else if (o instanceof WFSSelectionAndCapabilities) {
                    WFSSelectionAndCapabilities sac = (WFSSelectionAndCapabilities) o;

                    WebFeatureService wfs = new WebFeatureService(sac.getName(), sac.getHost(), sac.getQuery(), sac.getAttributes());
                    if (sac.getIdentifier() != null && sac.getIdentifier().length() > 0) {
                        log.debug("setting PrimaryAnnotationExpression of WFS Layer to '" + sac.getIdentifier() + "' (EXPRESSIONTYPE_PROPERTYNAME)");//NOI18N
                        wfs.getLayerProperties().setPrimaryAnnotationExpression(sac.getIdentifier(), LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                    } else {
                        log.warn("could not determine PrimaryAnnotationExpression");//NOI18N
                    }

                    activeLayerModel.addLayer(wfs);
                }
            } catch (IllegalArgumentException schonVorhanden) {
                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
                        org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.drop().JOptionPane.message"),
                        org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.drop().JOptionPane.title"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                log.error(e, e);
            }
        } else {
            log.warn("No Matching dataFlavour: " + dtde.getCurrentDataFlavorsAsList());//NOI18N
        }
    }

    public void dragExit(java.awt.dnd.DropTargetEvent dte) {
    }

    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {
    }

    public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {
    }

    public void dragEnter(java.awt.dnd.DropTargetDragEvent dtde) {
    }

    public double getScale() {
        return 2.0;
    }

    public Image getErrorImage() {
        return errorImage;
    }

    public void setErrorImage(Image errorImage) {
        this.errorImage = errorImage;
    }

    @Deprecated
    public Element getConfiguration() throws NoWriteError {
        return activeLayerModel.getConfiguration();
    }

    @Deprecated
    public void masterConfigure(Element e) {
        activeLayerModel.masterConfigure(e);
    }

    @Deprecated
    public void configure(Element e) {
        activeLayerModel.configure(e);
    }

    public static Vector<String> getCapabilities(Element e, Vector<String> v) {
        try {
            if (e.getName().equals("capabilities") && e.getAttribute("type") != null && (e.getAttribute("type").getValue().equals(CapabilityLink.OGC) || e.getAttribute("type").getValue().equals(CapabilityLink.OGC_DEPRECATED))) {//NOI18N
                String url = e.getTextTrim();
                if (!v.contains(url)) {
                    v.add(url);
                    return v;
                }
            } else {
                Iterator it = e.getChildren().iterator();
                while (it.hasNext()) {
                    Object elem = (Object) it.next();
                    if (elem instanceof Element) {
                        getCapabilities((Element) elem, v);
                    }
                }
            }
            return v;
        } catch (Exception ex) {
            return new Vector<String>();
        }
    }

    public JTreeTable getTreeTable() {
        return treeTable;
    }

    /**
     * This is required to prevent a bug on Macs that causes the first drop operation to fail with an exception,
     * "java.awt.datatransfer.UnsupportedFlavorException: application/x-java-file-list".  This bug is related to
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4746177 but only occurs during the first Mac OS drop operation in a
     * Java application.
     */
    private static void hackDragAndDropDataFlavors() {
        SystemFlavorMap sfm = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
        String nativeValue = "application/x-java-file-list";//NOI18N
        DataFlavor dataFlavor = new DataFlavor("application/x-java-file-list; charset=ASCII; class=java.util.List", "File List");//NOI18N
        sfm.addUnencodedNativeForFlavor(dataFlavor, nativeValue);
        sfm.addFlavorForUnencodedNative(nativeValue, dataFlavor);
    }
}
