/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.simplelayerwidget;

import org.jdom.Element;

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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelEvent;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.capabilitywidget.WFSSelectionAndCapabilities;
import de.cismet.cismap.commons.gui.layerwidget.*;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;
import de.cismet.tools.gui.treetable.JTreeTable;
import de.cismet.tools.gui.treetable.TreeTableCellEditor;
import de.cismet.tools.gui.treetable.TreeTableModel;

/**
 * DOCUMENT ME!
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class NewSimpleInternalLayerWidget extends JInternalFrame implements MappingModelListener,
    TableModelListener,
    DropTargetListener,
    Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final int ROW_HEIGHT = 18;
    private static final int WIDGET_WIDTH = 350;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final ImageIcon UP = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/up.png"));               // NOI18N
    private final ImageIcon DOWN = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/down.png"));             // NOI18N
    private final ImageIcon DELETE = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/removeLayer.png"));      // NOI18N
    private final ImageIcon DISABLE = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/disable.png"));          // NOI18N
    private final ImageIcon INVISIBLE = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/layerDLinvisible.png")); // NOI18N
    private int layerCount = 0;
    private boolean deactivatePopupMenuButtons = true;
    private ActiveLayerModel activeLayerModel;
    private JTreeTable treeTable;
    private Image errorImage;
    private MappingComponent mc;
    private JPopupMenu popupMenu;
    private JMenuItem up;
    private JMenuItem down;
    private JMenuItem del;
    private JMenuItem vis;
    private JMenuItem dis;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scpMain;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewSimpleInternalLayerWidget object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public NewSimpleInternalLayerWidget(final MappingComponent mc) {
        this(mc, true);
    }

    /**
     * Erzeugt eine neue NewSimpleInternalLayerWidget-Instanz.
     *
     * @param  mc                          Parent-MappingComponent des Wdgets
     * @param  deactivatePopupMenuButtons  DOCUMENT ME!
     */
    public NewSimpleInternalLayerWidget(final MappingComponent mc, final boolean deactivatePopupMenuButtons) {
        log.info("NewSimpleInternalLayerWidget erstellen"); // NOI18N
        this.deactivatePopupMenuButtons = deactivatePopupMenuButtons;

        // JInternalFrame fixieren, indem alle MouseMotionListener der Statusbar entfernt werden
        try {
            final BasicInternalFrameUI fui = (BasicInternalFrameUI)getUI();
            final Component north = fui.getNorthPane();
            final MouseMotionListener[] listener = (MouseMotionListener[])north.getListeners(MouseMotionListener.class);
            for (int i = 0; i < listener.length; i++) {
                north.removeMouseMotionListener(listener[i]);
            }
        } catch (Exception e) {
            log.error("Error during the removal of the Mousemotionlisteners", e); // NOI18N
        }
        initComponents();
        this.mc = mc;
        activeLayerModel = (ActiveLayerModel)mc.getMappingModel();
        popupMenu = new JPopupMenu();
        up = new JMenuItem();
        up.setText(org.openide.util.NbBundle.getMessage(
                NewSimpleInternalLayerWidget.class,
                "NewSimpleInternalLayerWidget.up.text"));                         // NOI18N
        up.setIcon(UP);
        up.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final TreePath tp = treeTable.getTree().getSelectionPath();
                    if (tp != null) {
                        activeLayerModel.moveLayerUp(tp);
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                treeTable.getTree().setSelectionPath(tp);
                                StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                            }
                        });
                }
            });
        down = new JMenuItem();
        down.setText(org.openide.util.NbBundle.getMessage(
                NewSimpleInternalLayerWidget.class,
                "NewSimpleInternalLayerWidget.down.text")); // NOI18N
        down.setIcon(DOWN);
        down.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final TreePath tp = treeTable.getTree().getSelectionPath();
                    if (tp != null) {
                        activeLayerModel.moveLayerDown(tp);
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                treeTable.getTree().setSelectionPath(tp);
                                StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                            }
                        });
                }
            });
        dis = new JMenuItem();
        dis.setText(org.openide.util.NbBundle.getMessage(
                NewSimpleInternalLayerWidget.class,
                "NewSimpleInternalLayerWidget.dis.text")); // NOI18N
        dis.setIcon(DISABLE);
        dis.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final TreePath tp = treeTable.getTree().getSelectionPath();
                    if (tp != null) {
                        activeLayerModel.disableLayer(tp);
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                treeTable.getTree().setSelectionPath(tp);
                            }
                        });
                }
            });
        del = new JMenuItem();
        del.setText(org.openide.util.NbBundle.getMessage(
                NewSimpleInternalLayerWidget.class,
                "NewSimpleInternalLayerWidget.del.text")); // NOI18N
        del.setIcon(DELETE);
        del.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final TreePath tp = treeTable.getTree().getSelectionPath();
                    final int row = treeTable.getSelectedRow();
                    if (tp != null) {
                        activeLayerModel.removeLayer(tp);
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                treeTable.getSelectionModel().setSelectionInterval(row, row);
                            }
                        });
                }
            });
        vis = new JMenuItem();
        vis.setText(org.openide.util.NbBundle.getMessage(
                NewSimpleInternalLayerWidget.class,
                "NewSimpleInternalLayerWidget.vis.text")); // NOI18N
        vis.setIcon(INVISIBLE);
        vis.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final TreePath tp = treeTable.getTree().getSelectionPath();
                    if (tp != null) {
                        activeLayerModel.handleVisibility(tp);
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
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

        try {
            putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);                         // NOI18N DropTarget dt
                                                                                                 // = new
                                                                                                 // DropTarget(this,
                                                                                                 // acceptableActions,
                                                                                                 // this);
        } catch (Throwable t) {
            log.error("Fehler beim setzen der Client Property isPalette: " + t.getMessage(), t); // NOI18N
        }

        if (activeLayerModel != null) {
            createTree();
        }
        pack();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Setzt das ActiveLayerModel des Widgets neu und erzeugt darauf basierend einen LayerTree.
     *
     * @param  mm  alm das neue ActiveLayerModel
     */
    public void setMappingModel(final MappingModel mm) {
        if (mm instanceof ActiveLayerModel) {
            this.activeLayerModel = (ActiveLayerModel)mm;
            createTree();
        } else {
            log.info("MappingModel ist kein ActiveLayerModel, kann InternalWidget nicht erstellen"); // NOI18N
        }
    }

    /**
     * Returns whether the buttons "disable" and "delete" are not shown in the TreeTable popup menu.
     *
     * @return  true, if the buttons are hidden, else false
     */
    public boolean isDeactivatePopupMenuButtons() {
        return this.deactivatePopupMenuButtons;
    }

    /**
     * Hides or shows the buttons "disable" and "delete" in the TreeTable popup menu.
     *
     * @param  deactivate  flag the new boolean value (true hides, false shows)
     */
    public void setDeactivatePopupMenuButtons(final boolean deactivate) {
        if (log.isDebugEnabled()) {
            log.debug("setDeactivatePopupMenuButtons"); // NOI18N
        }
        if (!deactivatePopupMenuButtons && deactivate) {
            // popupMenu.remove(dis);
            popupMenu.remove(del);
        } else if (deactivatePopupMenuButtons && !deactivate) {
            popupMenu.remove(vis);
            // popupMenu.add(dis);
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
                        if (log.isDebugEnabled()) {
                            log.debug("Tooltip"); // NOI18N
                        }
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
            final ActiveLayerTableCellEditor cellEditor = new ActiveLayerTableCellEditor();
            final ActiveLayerTableCellRenderer tableCellRenderer = new ActiveLayerTableCellRenderer(true);
            final TreeTableCellEditor treeTableCellEditor = new TreeTableCellEditor(treeTable, treeTable.getTree());
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

                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {
                        if (treeTable.getTree().getSelectionPath() != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("ActiveLayerWidget: selectionChanged()\n" + e);      // NOI18N
                            }
                            try {
                                final ActiveLayerEvent ale = new ActiveLayerEvent();
                                ale.setLayer(treeTable.getTree().getSelectionPath().getLastPathComponent());
                                if (ale.getLayer() instanceof WMSServiceLayer) {
                                    ale.setCapabilities(((WMSServiceLayer)ale.getLayer()).getWmsCapabilities());
                                }
                                CismapBroker.getInstance().fireLayerSelectionChanged(ale);
                            } catch (Exception ex) {
                                log.warn("Error at fireLayerSelectionChanged ... no problem"); // NOI18N
                            }
                        }
                    }
                });
            treeTable.setGridColor(this.getBackground());

            treeTable.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }

                    @Override
                    public void mousePressed(final MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });

            reshapeWidget(true);
            scpMain.setViewportView(treeTable);

            addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentResized(final ComponentEvent e) {
                        treeTable.repaint();
                    }
                });
        } catch (Exception ex) {
            log.error("Error during the creation of a TreeTable!", ex); // NOI18N
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        scpMain = new javax.swing.JScrollPane();

        setMinimumSize(new java.awt.Dimension(350, 50));
        setPreferredSize(new java.awt.Dimension(350, 100));
        addKeyListener(new java.awt.event.KeyAdapter() {

                @Override
                public void keyPressed(final java.awt.event.KeyEvent evt) {
                    formKeyPressed(evt);
                }
            });

        scpMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(scpMain, java.awt.BorderLayout.CENTER);

        pack();
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void formKeyPressed(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_formKeyPressed
    }                                                                //GEN-LAST:event_formKeyPressed

    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(final DropTargetEvent dte) {
    }

    @Override
    public void drop(final DropTargetDropEvent dtde) {
        final DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType,
                "SelectionAndCapabilities");                                                                       // NOI18N
        try {
            if (log.isDebugEnabled()) {
                log.debug("There are " + dtde.getTransferable().getTransferDataFlavors().length + "DataFlavours"); // NOI18N
            }
            for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                if (log.isDebugEnabled()) {
                    log.debug("DataFlavour" + i + ":" + dtde.getTransferable().getTransferDataFlavors()[i]);       // NOI18N
                }
            }
            final Object trasferData = dtde.getTransferable().getTransferData(TREEPATH_FLAVOR);
            final Vector treePaths = new Vector();
            dtde.dropComplete(true);
            if (trasferData instanceof SelectionAndCapabilities) {
                final TreePath[] tpa = ((SelectionAndCapabilities)trasferData).getSelection();
                for (int i = 0; i < tpa.length; ++i) {
                    treePaths.add(tpa[i]);
                }
                final WMSServiceLayer l = new WMSServiceLayer(treePaths);
                if (l.getWMSLayers().size() > 0) {
                    if ((treeTable.getEditingRow() != -1) && (treeTable.getEditingColumn() != -1)) {
                        treeTable.getCellEditor(treeTable.getEditingRow(), treeTable.getEditingColumn())
                                .stopCellEditing();
                    }
                    l.setWmsCapabilities(((SelectionAndCapabilities)trasferData).getCapabilities());
                    activeLayerModel.addLayer(l);
                }
                l.setWmsCapabilities(((SelectionAndCapabilities)trasferData).getCapabilities());
                l.setCapabilitiesUrl(((SelectionAndCapabilities)trasferData).getUrl());
                if (log.isDebugEnabled()) {
                    log.debug("((SelectionAndCapabilities)o).getUrl()"
                                + ((SelectionAndCapabilities)trasferData).getUrl());                               // NOI18N
                }
            }                                                                                                      // Drop-Objekt war ein WFS-Element
            else if (trasferData instanceof WFSSelectionAndCapabilities) {
                final WFSSelectionAndCapabilities sac = (WFSSelectionAndCapabilities)trasferData;
                for (final FeatureType feature : sac.getFeatures()) {
                    try {
                        final WebFeatureService wfs = new WebFeatureService(feature.getPrefixedNameString(),
                                feature.getWFSCapabilities().getURL().toString(),
                                feature.getWFSQuery(),
                                feature.getFeatureAttributes(),
                                feature);

                        if (log.isDebugEnabled()) {
                            log.debug("setting PrimaryAnnotationExpression of WFS Layer to '" + sac.getIdentifier()
                                        + "' (EXPRESSIONTYPE_PROPERTYNAME)");                 // NOI18N
                        }
                        wfs.getLayerProperties()
                                .setPrimaryAnnotationExpression(sac.getIdentifier(),
                                    LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                        activeLayerModel.addLayer(wfs);
                    } catch (IllegalArgumentException schonVorhanden) {
                        JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
                            org.openide.util.NbBundle.getMessage(
                                LayerWidget.class,
                                "LayerWidget.drop(DropTargetDropEvent).JOptionPane.message"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                LayerWidget.class,
                                "LayerWidget.drop(DropTargetDropEvent).JOptionPane.title"),   // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
                scpMain.setViewportView(treeTable);
            }
        } catch (IllegalArgumentException schonVorhanden) {
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
                org.openide.util.NbBundle.getMessage(
                    NewSimpleInternalLayerWidget.class,
                    "NewSimpleInternalLayerWidget.drop(DropTargetDropEvent).JOptionPane.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    NewSimpleInternalLayerWidget.class,
                    "NewSimpleInternalLayerWidget.drop(DropTargetDropEvent).JOptionPane.title"), // NOI18N
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    @Override
    public void configure(final Element parent) {
        activeLayerModel.configure(parent);
    }

    @Override
    public void masterConfigure(final Element parent) {
        activeLayerModel.masterConfigure(parent);
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        return activeLayerModel.getConfiguration();
    }

    @Override
    public void tableChanged(final TableModelEvent e) {
        e.getColumn();
        treeTable.repaint(treeTable.getBounds());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  add  DOCUMENT ME!
     */
    private void reshapeWidget(final boolean add) {
        final Dimension pref = treeTable.getPreferredSize();
        pref.setSize(WIDGET_WIDTH, pref.height + 23);
        setPreferredSize(pref);
        final int posX = mc.getWidth() - getWidth() - 1;
        final int posY = mc.getHeight() - getHeight() - 1;
        if (add) {
            setBounds(posX, posY - ROW_HEIGHT, pref.width, pref.height + ROW_HEIGHT);
        } else {
            setBounds(posX, posY + ROW_HEIGHT, pref.width, pref.height);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  errorImage  DOCUMENT ME!
     */
    public void setErrorImage(final Image errorImage) {
        this.errorImage = errorImage;
    }

    @Override
    public void mapServiceLayerStructureChanged(final MappingModelEvent mme) {
    }

    @Override
    public void mapServiceAdded(final MapService mapService) {
        reshapeWidget(true);
    }

    @Override
    public void mapServiceRemoved(final MapService mapService) {
        reshapeWidget(false);
    }
}
