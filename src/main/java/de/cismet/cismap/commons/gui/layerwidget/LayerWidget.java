/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.MappingModelEvent;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.capabilitywidget.WFSSelectionAndCapabilities;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.util.DnDUtils;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wms.capabilities.*;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;
import de.cismet.tools.gui.treetable.JTreeTable;
import de.cismet.tools.gui.treetable.TreeTableCellEditor;
import de.cismet.tools.gui.treetable.TreeTableModel;
import de.cismet.tools.gui.treetable.TreeTableModelAdapter;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class LayerWidget extends JPanel implements DropTargetListener, Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static DataFlavor uriListFlavor;

    static {
        try {
            uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String"); // NOI18N
        } catch (ClassNotFoundException e) {                                        // can't happen
            e.printStackTrace();
        }
    }

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DragSource dragSource;
    private DragGestureListener dgListener;
    private DragSourceListener dsListener;
    private ActiveLayerModel activeLayerModel = new ActiveLayerModel();
    private JTreeTable treeTable;
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private Image errorImage;
    private MappingComponent mapC = null;
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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form LayerWidget.
     *
     * @param  mapC  DOCUMENT ME!
     */
    public LayerWidget(final MappingComponent mapC) {
        hackDragAndDropDataFlavors();
        initComponents();
        this.mapC = mapC;
        log.info("LayerWidget: " + activeLayerModel); // NOI18N
        final DropTarget dt = new DropTarget(this, acceptableActions, this);

        treeTable = new JTreeTable(activeLayerModel) {

                @Override
                public JToolTip createToolTip() {
                    if (log.isDebugEnabled()) {
                        log.debug("Tooltip"); // NOI18N
                    }
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
        final ActiveLayerTableCellEditor cellEditor = new ActiveLayerTableCellEditor();
        // treeTable.getTree().setCellEditor(cellEditor);
// treeTable.getTree().setEditable(true);
        final TreeTableCellEditor treeTableCellEditor = new TreeTableCellEditor(treeTable, treeTable.getTree());
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
        final ActiveLayerTableCellRenderer tableCellRenderer = new ActiveLayerTableCellRenderer();
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
                public void valueChanged(final TreeSelectionEvent e) {
                    if (treeTable.getTree().getSelectionPath() != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("ActiveLayerWidget: selectionChanged()\n" + e); // NOI18N
                        }
                        try {
                            final ActiveLayerEvent ale = new ActiveLayerEvent();
                            ale.setLayer(treeTable.getTree().getSelectionPath().getLastPathComponent());
                            if (ale.getLayer() instanceof WMSServiceLayer) {
                                ale.setCapabilities(((WMSServiceLayer)ale.getLayer()).getWmsCapabilities());
                            }
                            CismapBroker.getInstance().fireLayerSelectionChanged(ale);
                        } catch (Exception ex) {
                            log.error("error while changing the selected layer", ex); // NOI18N
                        }
                    }
                }
            });

        cmdZoomToFullExtent.setVisible(false);
        treeTable.setGridColor(this.getBackground());
        addComponentListener(new ComponentListener() {

                @Override
                public void componentHidden(final ComponentEvent e) {
                }

                @Override
                public void componentMoved(final ComponentEvent e) {
                }

                @Override
                public void componentResized(final ComponentEvent e) {
//                treeTable.repaint();
                }

                @Override
                public void componentShown(final ComponentEvent e) {
                }
            });
        activeLayerModel.addMappingModelListener(new MappingModelListener() {

                @Override
                public void mapServiceLayerStructureChanged(final MappingModelEvent mme) {
                    treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
                }

                @Override
                public void mapServiceAdded(final MapService mapService) {
                    treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
                }

                @Override
                public void mapServiceRemoved(final MapService mapService) {
                    treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
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

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEtchedBorder(),
                javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        setPreferredSize(new java.awt.Dimension(211, 114));
        setLayout(new java.awt.BorderLayout());

        scpMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scpMain.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(scpMain, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jToolBar1.setRollover(true);

        cmdTreeCollapse.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/collapseTree.png"))); // NOI18N
        cmdTreeCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdTreeCollapse.toolTipText"));                                                // NOI18N
        cmdTreeCollapse.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdTreeCollapse.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdTreeCollapseActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdTreeCollapse);

        cmdRefreshSingleLayer.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/refresh.png"))); // NOI18N
        cmdRefreshSingleLayer.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdRefreshSingleLayer.toolTipText"));                                     // NOI18N
        cmdRefreshSingleLayer.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRefreshSingleLayer.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRefreshSingleLayerActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRefreshSingleLayer);

        cmdDown.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/down.png"))); // NOI18N
        cmdDown.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdDown.toolTipText"));                                                // NOI18N
        cmdDown.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdDown.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdDownActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdDown);

        cmdUp.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/up.png")));                       // NOI18N
        cmdUp.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdUp.toolTipText")); // NOI18N
        cmdUp.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdUp.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdUpActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdUp);

        cmdZoomToFullExtent.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerZoom.png"))); // NOI18N
        cmdZoomToFullExtent.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdZoomToFullExtent.toolTipText"));                                         // NOI18N
        cmdZoomToFullExtent.setMargin(new java.awt.Insets(2, 1, 2, 1));
        jToolBar1.add(cmdZoomToFullExtent);

        cmdDisable.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disable.png"))); // NOI18N
        cmdDisable.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdDisable.toolTipText"));                                                // NOI18N
        cmdDisable.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdDisable.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdDisableActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdDisable);

        cmdRemove.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/removeLayer.png"))); // NOI18N
        cmdRemove.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdRemove.toolTipText"));                                                     // NOI18N
        cmdRemove.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRemove);

        cmdMakeInvisible.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerDLinvisible.png"))); // NOI18N
        cmdMakeInvisible.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdMakeInvisible.toolTipText"));                                                   // NOI18N
        cmdMakeInvisible.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdMakeInvisible.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdMakeInvisibleActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdMakeInvisible);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     */
    public void removeAllLayers() {
        activeLayerModel.removeAllLayers();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRefreshSingleLayerActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRefreshSingleLayerActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    if (tps != null) {
                        for (final TreePath tp : tps) {
                            if ((tp != null) && (tp.getLastPathComponent() instanceof RetrievalServiceLayer)) {
                                ((MapService)tp.getLastPathComponent()).setBoundingBox(
                                    mapC.getCurrentBoundingBoxFromCamera());
                                ((RetrievalServiceLayer)tp.getLastPathComponent()).retrieve(true);
                            } else if ((tp != null)
                                        && (tp.getParentPath().getLastPathComponent() instanceof RetrievalServiceLayer)) {
                                ((RetrievalServiceLayer)tp.getParentPath().getLastPathComponent()).retrieve(true);
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    treeTable.getTree().setSelectionPaths(tps);
                    StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                    treeTable.getTree().setSelectionPaths(tps);
                }
            };

        worker.execute();
    } //GEN-LAST:event_cmdRefreshSingleLayerActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdDownActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdDownActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.moveLayerDown(tp);
        }
        if (EventQueue.isDispatchThread()) {
            log.warn("InvokeLater in EDT");
        }                                                                       // NOI18N

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPath(tp);
                    StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                }
            });
    } //GEN-LAST:event_cmdDownActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdUpActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdUpActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.moveLayerUp(tp);
        }
        if (EventQueue.isDispatchThread()) {
            log.warn("InvokeLater in EDT");
        }                                                                     // NOI18N
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPath(tp);
                    StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                }
            });
    } //GEN-LAST:event_cmdUpActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdMakeInvisibleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdMakeInvisibleActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.handleVisibility(tp);
        }
        if (EventQueue.isDispatchThread()) {
            log.warn("InvokeLater in EDT");
        }                                                                                // NOI18N
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPath(tp);
                }
            });
    } //GEN-LAST:event_cmdMakeInvisibleActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdDisableActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdDisableActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();
        if (tp != null) {
            activeLayerModel.disableLayer(tp);
        }
        if (EventQueue.isDispatchThread()) {
            log.warn("InvokeLater in EDT");
        }                                                                          // NOI18N
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPath(tp);
                }
            });
    } //GEN-LAST:event_cmdDisableActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdTreeCollapseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdTreeCollapseActionPerformed
        // StaticSwingTools.jTreeCollapseAllNodes(treeTable.getTree());
// int sel = treeTable.getSelectionModel().getMinSelectionIndex();
// if (treeTable.getRowCount() > 0) {
// treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
// treeTable.getSelectionModel().setSelectionInterval(0, treeTable.getRowCount() - 1);
// if (sel == -1) {
// treeTable.getSelectionModel().setSelectionInterval(0, 0);
// } else {
// treeTable.getSelectionModel().setSelectionInterval(sel, sel);
// }
// treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
// }

        treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
    } //GEN-LAST:event_cmdTreeCollapseActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveActionPerformed
        try {
            final TreePath tp = treeTable.getTree().getSelectionPath();
            final int row = treeTable.getSelectedRow();

            if (tp != null) {
                activeLayerModel.removeLayer(tp);
            }

            final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        int selectedRow = row;
                        if (selectedRow >= treeTable.getRowCount()) {
                            selectedRow = treeTable.getRowCount() - 1;
                        }
                        treeTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                    }
                };

            // do invoke later in EDT is required, because the selection should be adopted
            // after the layer is removed
            EventQueue.invokeLater(r);
        } catch (Exception e) {
            log.error("Error during romaval of layer", e);
        }
    } //GEN-LAST:event_cmdRemoveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ActiveLayerModel getMappingModel() {
        return activeLayerModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new Tester().setVisible(true);
                }
            });
    }

    /**
     * Called when the drag operation has terminated with a drop on the operable part of the drop site for the <code>
     * DropTarget</code> registered with this listener.
     *
     * <p>This method is responsible for undertaking the transfer of the data associated with the gesture. The <code>
     * DropTargetDropEvent</code> provides a means to obtain a <code>Transferable</code> object that represents the data
     * object(s) to be transfered.</p>
     *
     * <P>From this method, the <code>DropTargetListener</code> shall accept or reject the drop via the acceptDrop(int
     * dropAction) or rejectDrop() methods of the <code>DropTargetDropEvent</code> parameter.</P>
     *
     * <P>Subsequent to acceptDrop(), but not before, <code>DropTargetDropEvent</code>'s getTransferable() method may be
     * invoked, and data transfer may be performed via the returned <code>Transferable</code>'s getTransferData()
     * method.</P>
     *
     * <P>At the completion of a drop, an implementation of this method is required to signal the success/failure of the
     * drop by passing an appropriate <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.</P>
     *
     * <P>Note: The data transfer should be completed before the call to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method. After that, a call to the getTransferData() method of the <code>
     * Transferable</code> returned by <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to succeed only
     * if the data transfer is local; that is, only if <code>DropTargetDropEvent.isLocalTransfer()</code> returns <code>
     * true</code>. Otherwise, the behavior of the call is implementation-dependent.</P>
     *
     * @param  dtde  the <code>DropTargetDropEvent</code>
     */
    @Override
    public void drop(final java.awt.dnd.DropTargetDropEvent dtde) {
        final DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType,
                "SelectionAndCapabilities");                                           // NOI18N
        if (log.isDebugEnabled()) {
            log.debug("Drop with this flavors:" + dtde.getCurrentDataFlavorsAsList()); // NOI18N
        }
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            try {
                List<File> data = null;
                final Transferable transferable = dtde.getTransferable();
                if (dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Drop is unix drop");                                // NOI18N
                    }

                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Drop is Mac drop xxx"
                                        + transferable.getTransferData(DataFlavor.javaFileListFlavor)); // NOI18N
                        }

                        data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    } catch (UnsupportedFlavorException e) {
                        // transferable.getTransferData(DataFlavor.javaFileListFlavor) will throw an
                        // UnsupportedFlavorException on Linux
                        if (data == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Drop is Linux drop"); // NOI18N
                            }
                            data = DnDUtils.textURIListToFileList((String)transferable.getTransferData(
                                        DnDUtils.URI_LIST_FLAVOR));
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Drop is windows drop");       // NOI18N
                    }
                    data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Drag&Drop File List: " + data); // NOI18N
                }
                if (data != null) {
                    for (final File currentFile : data) {
                        // NO HARDCODING
                        try {
                            log.info("DocumentUri: " + currentFile.toURI()); // NOI18N
                            // GMLFeatureService gfs = new
                            // GMLFeatureService(currentFile.getName(),currentFile.toURI(),null); langsam sollte nicht
                            // im EDT ausgeführt werden
                            final DocumentFeatureService dfs = DocumentFeatureServiceFactory
                                        .createDocumentFeatureService(currentFile);
                            activeLayerModel.addLayer(dfs);

                            if (dfs instanceof ShapeFileFeatureService) {
                                new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            do {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (InterruptedException e) {
                                                    // nothing to do
                                                }
                                            } while (!dfs.isInitialized());

                                            if (((ShapeFileFeatureService)dfs).isErrorInGeometryFound()) {
                                                JOptionPane.showMessageDialog(
                                                    StaticSwingTools.getParentFrame(LayerWidget.this),
                                                    NbBundle.getMessage(
                                                        LayerWidget.class,
                                                        "LayerWidget.drop().errorInShapeGeometryFoundMessage"),
                                                    NbBundle.getMessage(
                                                        LayerWidget.class,
                                                        "LayerWidget.drop().errorInShapeGeometryFoundTitle"),
                                                    JOptionPane.ERROR_MESSAGE);
                                            } else if (((ShapeFileFeatureService)dfs).isNoGeometryRecognised()) {
                                                JOptionPane.showMessageDialog(
                                                    StaticSwingTools.getParentFrame(LayerWidget.this),
                                                    NbBundle.getMessage(
                                                        LayerWidget.class,
                                                        "LayerWidget.drop().noGeometryFoundInShapeMessage"),
                                                    NbBundle.getMessage(
                                                        LayerWidget.class,
                                                        "LayerWidget.drop().noGeometryFoundInShapeTitle"),
                                                    JOptionPane.WARNING_MESSAGE);
                                            }
                                        }
                                    }).start();
                            }
                        } catch (Exception ex) {
                            log.error("Error during creation of a FeatureServices", ex); // NOI18N
                        }
                    }
                } else {
                    log.warn("No files available");                                      // NOI18N
                }
            } catch (Exception ex) {
                log.error("Failure during drag & drop opertation", ex);                  // NOI18N
            }
        } else if (dtde.isDataFlavorSupported(TREEPATH_FLAVOR)) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("There are " + dtde.getTransferable().getTransferDataFlavors().length + " DataFlavours"); // NOI18N
                }
                for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                    if (log.isDebugEnabled()) {
                        log.debug("DataFlavour" + i + ": " + dtde.getTransferable().getTransferDataFlavors()[i]); // NOI18N
                    }
                }
                final Object o = dtde.getTransferable().getTransferData(TREEPATH_FLAVOR);
                final Vector v = new Vector();
                dtde.dropComplete(true);
                if (o instanceof SelectionAndCapabilities) {
                    final TreePath[] tpa = ((SelectionAndCapabilities)o).getSelection();
                    for (int i = 0; i < tpa.length; ++i) {
                        v.add(tpa[i]);
                    }

                    if ((((TreePath)v.get(0)).getLastPathComponent()
                                    instanceof de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer)
                                && ((de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer)
                                    (((TreePath)v.get(0)).getLastPathComponent())).getTitle().endsWith("[]")) {
                        final SlidableWMSServiceLayerGroup l = new SlidableWMSServiceLayerGroup(v);
                        l.setWmsCapabilities(((SelectionAndCapabilities)o).getCapabilities());
                        l.setCapabilitiesUrl(((SelectionAndCapabilities)o).getUrl());
                        activeLayerModel.addLayer(l);
                    } else {
                        final WMSServiceLayer l = new WMSServiceLayer(v);
                        if (l.getWMSLayers().size() > 0) {
                            if ((treeTable.getEditingRow() != -1) && (treeTable.getEditingColumn() != -1)) {
                                try {
                                    treeTable.getCellEditor(treeTable.getEditingRow(), treeTable.getEditingColumn())
                                            .stopCellEditing();
                                } catch (Exception e) {
                                    // stopCellEditing went wrong. I don't care ;-)
                                }
                            }
                            l.setWmsCapabilities(((SelectionAndCapabilities)o).getCapabilities());
                            activeLayerModel.addLayer(l);
                        }
                        l.setWmsCapabilities(((SelectionAndCapabilities)o).getCapabilities());
                        l.setCapabilitiesUrl(((SelectionAndCapabilities)o).getUrl());
                        if (log.isDebugEnabled()) {
                            log.debug("((SelectionAndCapabilities)o).getUrl()"
                                        + ((SelectionAndCapabilities)o).getUrl()); // NOI18N
                        }
                    }
                }                                                                  // Drop-Objekt war ein WFS-Element
                else if (o instanceof WFSSelectionAndCapabilities) {
                    final WFSSelectionAndCapabilities sac = (WFSSelectionAndCapabilities)o;

                    for (final FeatureType feature : sac.getFeatures()) {
                        try {
                            final WebFeatureService wfs = new WebFeatureService(feature.getPrefixedNameString(),
                                    feature.getWFSCapabilities().getURL().toString(),
                                    feature.getWFSQuery(),
                                    feature.getFeatureAttributes(),
                                    feature);
                            if ((sac.getIdentifier() != null) && (sac.getIdentifier().length() > 0)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("setting PrimaryAnnotationExpression of WFS Layer to '"
                                                + sac.getIdentifier()
                                                + "' (EXPRESSIONTYPE_PROPERTYNAME)");        // NOI18N
                                }
                                wfs.getLayerProperties()
                                        .setPrimaryAnnotationExpression(sac.getIdentifier(),
                                            LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                            } else {
                                log.warn("could not determine PrimaryAnnotationExpression"); // NOI18N
                            }

                            activeLayerModel.addLayer(wfs);
                        } catch (IllegalArgumentException schonVorhanden) {
                            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
                                org.openide.util.NbBundle.getMessage(
                                    LayerWidget.class,
                                    "LayerWidget.drop(DropTargetDropEvent).JOptionPane.message"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    LayerWidget.class,
                                    "LayerWidget.drop(DropTargetDropEvent).JOptionPane.title"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (IllegalArgumentException schonVorhanden) {
                JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
                    org.openide.util.NbBundle.getMessage(
                        LayerWidget.class,
                        "LayerWidget.drop(DropTargetDropEvent).JOptionPane.message"),           // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        LayerWidget.class,
                        "LayerWidget.drop(DropTargetDropEvent).JOptionPane.title"),             // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                log.error(e, e);
            }
        } else {
            log.warn("No Matching dataFlavour: " + dtde.getCurrentDataFlavorsAsList());         // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dte  DOCUMENT ME!
     */
    @Override
    public void dragExit(final java.awt.dnd.DropTargetEvent dte) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dropActionChanged(final java.awt.dnd.DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dragOver(final java.awt.dnd.DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dragEnter(final java.awt.dnd.DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getScale() {
        return 2.0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image getErrorImage() {
        return errorImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  errorImage  DOCUMENT ME!
     */
    public void setErrorImage(final Image errorImage) {
        this.errorImage = errorImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  NoWriteError  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public Element getConfiguration() throws NoWriteError {
        return activeLayerModel.getConfiguration();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public void masterConfigure(final Element e) {
        activeLayerModel.masterConfigure(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public void configure(final Element e) {
        activeLayerModel.configure(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   e  DOCUMENT ME!
     * @param   v  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Vector<String> getCapabilities(final Element e, final Vector<String> v) {
        try {
            if (e.getName().equals("capabilities") && (e.getAttribute("type") != null)
                        && (e.getAttribute("type").getValue().equals(CapabilityLink.OGC)
                            || e.getAttribute("type").getValue().equals(CapabilityLink.OGC_DEPRECATED))) { // NOI18N
                final String url = e.getTextTrim();
                if (!v.contains(url)) {
                    v.add(url);
                    return v;
                }
            } else {
                final Iterator it = e.getChildren().iterator();
                while (it.hasNext()) {
                    final Object elem = (Object)it.next();
                    if (elem instanceof Element) {
                        getCapabilities((Element)elem, v);
                    }
                }
            }
            return v;
        } catch (Exception ex) {
            return new Vector<String>();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JTreeTable getTreeTable() {
        return treeTable;
    }

    /**
     * This is required to prevent a bug on Macs that causes the first drop operation to fail with an exception,
     * "java.awt.datatransfer.UnsupportedFlavorException: application/x-java-file-list". This bug is related to
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4746177 but only occurs during the first Mac OS drop operation
     * in a Java application.
     */
    private static void hackDragAndDropDataFlavors() {
        final SystemFlavorMap sfm = (SystemFlavorMap)SystemFlavorMap.getDefaultFlavorMap();
        final String nativeValue = "application/x-java-file-list"; // NOI18N
        final DataFlavor dataFlavor = new DataFlavor(
                "application/x-java-file-list; charset=ASCII; class=java.util.List",
                "File List");                                      // NOI18N
        sfm.addUnencodedNativeForFlavor(dataFlavor, nativeValue);
        sfm.addFlavorForUnencodedNative(nativeValue, dataFlavor);
    }
}
