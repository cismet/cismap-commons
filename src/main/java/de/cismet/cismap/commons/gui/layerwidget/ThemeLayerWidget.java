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
package de.cismet.cismap.commons.gui.layerwidget;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.PNodeProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.style.StyleDialogClosedEvent;
import de.cismet.cismap.commons.featureservice.style.StyleDialogClosedListener;
import de.cismet.cismap.commons.featureservice.style.StyleDialogStarter;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget.CheckBoxNodeRenderer;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RepaintEvent;
import de.cismet.cismap.commons.retrieval.RepaintListener;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;
import de.cismet.cismap.commons.wms.capabilities.Layer;

import de.cismet.tools.gui.DefaultPopupMenuListener;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.veto.VetoException;

import static de.cismet.cismap.commons.gui.layerwidget.ThemeLayerMenuItem.FOLDER;
import static de.cismet.cismap.commons.gui.layerwidget.ThemeLayerMenuItem.ROOT;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ThemeLayerWidget extends javax.swing.JPanel implements TreeSelectionListener, SelectionChangedListener { // implements

    //~ Instance fields --------------------------------------------------------

    private Logger log = Logger.getLogger(ThemeLayerWidget.class);
    private JPopupMenu popupMenu = new JPopupMenu();
    private List<ThemeLayerMenuItem> menuItems = new ArrayList<ThemeLayerMenuItem>();
    private ActiveLayerModel layerModel;
    private DefaultPopupMenuListener popupMenuListener = new DefaultPopupMenuListener(popupMenu);
    private TreeTransferHandler transferHandler;
    private List<ThemeLayerListener> themeLayerListener = new ArrayList<ThemeLayerListener>();
    private AddThemeMenuItem addThemeMenuItem;
    private final List<TreePath> expendedPaths = new ArrayList<TreePath>();
    private Timer refreshTimer = new Timer("ThemeTree refresh thread");
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ThemeLayerWidget.
     */
    public ThemeLayerWidget() {
        initComponents();
        final SelectionManager manager = SelectionManager.getInstance();
        manager.addSelectionChangedListener(this);
        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setCellEditor(new CheckBoxNodeEditor());
        tree.setEditable(true);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        transferHandler = new TreeTransferHandler();
        tree.setTransferHandler(transferHandler);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                    synchronized (popupMenu.getTreeLock()) {
                        for (int i = 0; i < popupMenu.getComponentCount(); ++i) {
                            final TreePath[] paths = tree.getSelectionPaths();
                            final Object component = popupMenu.getComponent(i);

                            if (component instanceof ThemeLayerMenuItem) {
                                final ThemeLayerMenuItem menuItem = (ThemeLayerMenuItem)component;
                                menuItem.refreshText(paths);
                            }
                        }
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(final PopupMenuEvent e) {
                }
            });
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {

                @Override
                public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
                }

                @Override
                public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
                    if (event.getPath().getLastPathComponent().equals(tree.getModel().getRoot())) {
                        throw new ExpandVetoException(event);
                    }
                }
            });

        tree.addTreeExpansionListener(new TreeExpansionListener() {

                @Override
                public void treeExpanded(final TreeExpansionEvent event) {
                    saveExpandedPaths();
                }

                @Override
                public void treeCollapsed(final TreeExpansionEvent event) {
                    saveExpandedPaths();
                }

                private void saveExpandedPaths() {
                    expendedPaths.clear();
                    final TreePath root = new TreePath(new Object[] { layerModel.getRoot() });
                    final Enumeration<TreePath> en = tree.getExpandedDescendants(root);

                    while (en.hasMoreElements()) {
                        expendedPaths.add(en.nextElement());
                    }
                }
            });

        CismapBroker.getInstance().addStatusListener(new StatusListener() {

                @Override
                public void statusValueChanged(final StatusEvent e) {
                    if (e.getName().equals(StatusEvent.SCALE)) {
                        ThemeLayerWidget.this.repaint();
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void updateTree() {
        if (tree != null) {
            tree.repaint();
            tree.revalidate();
            tree.updateUI();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mappingModel  DOCUMENT ME!
     */
    public void setMappingModel(final ActiveLayerModel mappingModel) {
        layerModel = mappingModel;
        final ActiveLayerModelWrapperWithoutProgress model = new ActiveLayerModelWrapperWithoutProgress(layerModel);
        tree.setModel(model);
        model.addTreeToUpdate(tree);
        CismapBroker.getInstance().getMappingComponent().addRepaintListener(new RepaintListener() {

                @Override
                public void repaintStart(final RepaintEvent e) {
                }

                @Override
                public void repaintComplete(final RepaintEvent e) {
                    if ((e != null) && (e.getRetrievalEvent() != null)) {
                        if (e.getRetrievalEvent().isInitialisationEvent()) {
                            updateTree();
                        }
                    }
                }

                @Override
                public void repaintError(final RepaintEvent e) {
                    if ((e != null) && (e.getRetrievalEvent() != null)) {
                        if (e.getRetrievalEvent().isInitialisationEvent()) {
                            updateTree();
                        }
                    }
                }
            });

        menuItems.add(new AddFolderMenuItem());
        menuItems.add(new RemoveGroupMenuItem());
        addThemeMenuItem = new AddThemeMenuItem();
        menuItems.add(addThemeMenuItem);
        menuItems.add(new VisibilityMenuItem());
        menuItems.add(new InvisibilityMenuItem());
        menuItems.add(new AllSelectableMenuItem());
        menuItems.add(new AllUnselectableMenuItem());
        menuItems.add(new ExpandMenuItem());
        menuItems.add(new CollapseMenuItem());
        menuItems.add(new RemoveThemeMenuItem());
        menuItems.add(new OpenAttributeTableMenuItem());
        menuItems.add(new SelectAllMenuItem());
        menuItems.add(new InvertSelectionTableMenuItem());
        menuItems.add(new ClearSelectionMenuItem());
        menuItems.add(new SelectableMenuItem());
        menuItems.add(new ZoomToThemeMenuItem());
        menuItems.add(new ZoomToSelectedItemsMenuItem());
        menuItems.add(new EditModeMenuItem());
        menuItems.add(new OptionsMenuItem());

        tree.getSelectionModel().addTreeSelectionListener(this);
        createPopupMenu();
        tree.addMouseListener(popupMenuListener);
        tree.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (!e.isPopupTrigger() && (e.getClickCount() == 1)) {
                        final int x = e.getX();
                        final int y = e.getY();

                        // is click over the combobox?
                        final TreePath tp = tree.getPathForLocation(x, y);

                        if (tp != null) {
                            final int pathCount = tp.getPathCount() - 1;
                            final int minX = pathCount * 20;
                            final int maxX = minX + 15;

                            if ((x >= minX) && (x <= maxX)) {
                                // click is over the checkbox
                                changeVisibility(tp.getLastPathComponent());
                                tree.cancelEditing();
                            }
                        }
                    }
                }
            });

        model.addTreeModelListener(new TreeModelListener() {

                @Override
                public void treeNodesChanged(final TreeModelEvent e) {
                    resetExpansion();
                }

                @Override
                public void treeNodesInserted(final TreeModelEvent e) {
                    resetExpansion();
                }

                @Override
                public void treeNodesRemoved(final TreeModelEvent e) {
                    resetExpansion();
                }

                @Override
                public void treeStructureChanged(final TreeModelEvent e) {
                    resetExpansion();
                }
            });
    }

    /**
     * Get the original list of the ThemeLayermenuItems. Changes in this list leads to changes in the context menu of
     * the ThemeLayerWidget
     *
     * @return  the list of the ThemeLayermenuItems
     */
    public List<ThemeLayerMenuItem> getContextMenuItems() {
        return menuItems;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addAddThemeMenuItemListener(final ActionListener l) {
        addThemeMenuItem.addActionListener(l);
    }

    /**
     * DOCUMENT ME!
     */
    private void createPopupMenu() {
        final List<ServiceLayer> serviceLayerList = new ArrayList<ServiceLayer>();
        boolean node = false;
        boolean folder = false;
        boolean multi = false;
        boolean root = false;
        boolean feature = false;
        int mask = 0;

        final TreePath[] paths = tree.getSelectionPaths();

        popupMenu.removeAll();

        if (paths == null) {
            return;
        }

        if (paths.length > 1) {
            multi = true;
        }

        for (final TreePath p : paths) {
            final Object o = p.getLastPathComponent();

            if (o instanceof LayerCollection) {
                folder = true;
            } else if (o.equals(layerModel.getRoot())) {
                root = true;
            } else {
                node = true;
            }

            if (o instanceof ServiceLayer) {
                serviceLayerList.add((ServiceLayer)o);
            }
            if (o instanceof AbstractFeatureService) {
                feature = true;
            }
        }

        mask += (root ? ThemeLayerMenuItem.ROOT : 0);
        mask += (folder ? ThemeLayerMenuItem.FOLDER : 0);
        mask += (node ? ThemeLayerMenuItem.NODE : 0);
        mask += (multi ? ThemeLayerMenuItem.MULTI : 0);
        mask += (feature ? ThemeLayerMenuItem.FEATURE_SERVICE : 0);

        for (final ThemeLayerMenuItem item : menuItems) {
            if (item.isVisible(mask) && item.isVisible(serviceLayerList)) {
                if (item.isNewSection()) {
                    popupMenu.addSeparator();
                }
                popupMenu.add(item);
                item.setEnabled(item.isSelectable(mask) && item.isSelectable(serviceLayerList));
                item.refreshText(paths);
            }
        }
    }

    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        createPopupMenu();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addTreeSelectionListener(final TreeSelectionListener l) {
        tree.getSelectionModel().addTreeSelectionListener(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void removeTreeSelectionListener(final TreeSelectionListener l) {
        tree.getSelectionModel().removeTreeSelectionListener(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreePath[] getSelectionPath() {
        return tree.getSelectionPaths();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreePath getLeadSelectionPath() {
        return tree.getLeadSelectionPath();
    }

    /**
     * Adds the given ThemeLayerMenuItem to the context menu.
     *
     * @param  index     the position to insert the menu item. This does only work, if the setMappignModel method was
     *                   invoked earlier.
     * @param  menuItem  the menu item to insert
     */
    public void insertMenuItemIntoContextMenu(final int index, final ThemeLayerMenuItem menuItem) {
        menuItems.add(index, menuItem);
    }

    /**
     * changes the visibility of the given object.
     *
     * @param  objectToChange  either the root layer, a LayerCollection or a ServiceLayer
     */
    private void changeVisibility(final Object objectToChange) {
        if (objectToChange.equals(layerModel.getRoot())) {
            for (int i = 0; i < layerModel.getChildCount(layerModel.getRoot()); ++i) {
                changeVisibility(layerModel.getChild(layerModel.getRoot(), i));
            }
        } else if (objectToChange instanceof LayerCollection) {
            final LayerCollection lc = (LayerCollection)objectToChange;
            final boolean visibility = !lc.isEnabled();

            changeVisibility(lc, visibility);
        } else if (objectToChange instanceof ServiceLayer) {
            final ServiceLayer sl = (ServiceLayer)objectToChange;
            final boolean visibility = !sl.isEnabled();
            changeVisibility(sl, visibility);
        }

        updateUI();
    }

    /**
     * changes the visibility of the given object.
     *
     * @param  objectToChange  either the root layer, a LayerCollection or a ServiceLayer
     * @param  visible         the new visibility
     */
    private void changeVisibility(final Object objectToChange, final boolean visible) {
        if (objectToChange instanceof LayerCollection) {
            final LayerCollection lc = (LayerCollection)objectToChange;

            for (int i = 0; i < lc.size(); ++i) {
                changeVisibility(lc.get(i), visible);
            }
        } else if (objectToChange instanceof ServiceLayer) {
            boolean changeVisibility = false;
            boolean statusChanged = false;
            final ServiceLayer sl = (ServiceLayer)objectToChange;

            if (sl.isEnabled() != visible) {
                sl.setEnabled(visible);
                statusChanged = true;
            }

            if (objectToChange instanceof PNodeProvider) {
                final PNodeProvider pr = (PNodeProvider)objectToChange;

                if ((pr.getPNode() != null) && (pr.getPNode().getVisible() != visible)) {
                    if (!visible) {
                        pr.getPNode().removeAllChildren();
                    }
                    changeVisibility = true;
                }
            }

            if (changeVisibility) {
                // only the last component of the tree path will be considered within
                // the methods isVisible(TreePath) and handleVisibiliy(TreePath)
                final TreePath tp = new TreePath(new Object[] { layerModel.getRoot(), objectToChange });
                layerModel.handleVisibility(tp);
                statusChanged = true;
            }

            if (visible && statusChanged && (objectToChange instanceof MapService)) {
                ((MapService)objectToChange).setBoundingBox(
                    CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera());
                ((RetrievalServiceLayer)objectToChange).retrieve(true);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        final java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new TreeWithoutNPEAfterDrop();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(tree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        refreshTimer.cancel();
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                createPopupMenu();
                                tree.updateUI();
                            }
                        });
                }
            }, 100);
    }

    /**
     * DOCUMENT ME!
     */
    private void resetExpansion() {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final List<TreePath> pathCopy = new ArrayList<TreePath>(expendedPaths);

                    if (pathCopy.isEmpty()) {
                        // root should always be expanded
                        final TreePath root = new TreePath(new Object[] { layerModel.getRoot() });
                        pathCopy.add(root);
                    }

                    for (final TreePath tp : pathCopy) {
                        tree.expandPath(tp);
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addThemeLayerListener(final ThemeLayerListener l) {
        themeLayerListener.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void removeThemeLayerListener(final ThemeLayerListener l) {
        themeLayerListener.remove(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  VetoException  DOCUMENT ME!
     */
    private void fireRemoveLayerEvent(final ThemeLayerEvent e) throws VetoException {
        for (final ThemeLayerListener l : themeLayerListener) {
            l.removeLayer(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   path  DOCUMENT ME!
     *
     * @throws  VetoException  DOCUMENT ME!
     */
    private void removePath(final TreePath path) throws VetoException {
        try {
            if (path.getLastPathComponent() instanceof MapService) {
                final ThemeLayerEvent event = new ThemeLayerEvent((MapService)path.getLastPathComponent(),
                        this);
                fireRemoveLayerEvent(event);
            }
            layerModel.removeLayer(path);

            for (final TreePath tp : new ArrayList<TreePath>(expendedPaths)) {
                tree.expandPath(tp);
            }
        } catch (VetoException ex) {
            // nothing to do
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AddFolderMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AddFolderMenuItem object.
         */
        public AddFolderMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.addPopupMenu().pmenuItem.text"),
                ROOT
                        | FOLDER);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            if (paths.length == 1) {
                final Object selectedComponent = paths[0].getLastPathComponent();
                if (selectedComponent instanceof LayerCollection) {
//                    final LayerCollection lc = (LayerCollection)selectedComponent;
                    final LayerCollection newLayer = new LayerCollection();
                    layerModel.addEmptyLayerCollection(paths[0], newLayer);
//                    lc.add(newLayer);
//                    layerModel.fireTreeStructureChanged(
//                        layerModel,
//                        paths[0].getPath(),
//                        null,
//                        new Object[] { newLayer });
                } else if (selectedComponent.equals(layerModel.getRoot())) {
                    layerModel.addEmptyLayerCollection(new LayerCollection());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class VisibilityMenuItem extends ThemeLayerMenuItem {

        //~ Instance fields ----------------------------------------------------

        protected boolean shouldBeEnabled = true;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new VisibilityMenuItem object.
         */
        public VisibilityMenuItem() {
            this(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.VisibilityMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER,
                true);
            newSection = true;
        }

        /**
         * Creates a new VisibilityMenuItem object.
         *
         * @param  title            DOCUMENT ME!
         * @param  visibility       DOCUMENT ME!
         * @param  shouldBeEnabled  DOCUMENT ME!
         */
        protected VisibilityMenuItem(final String title, final int visibility, final boolean shouldBeEnabled) {
            super(title, visibility);
            this.shouldBeEnabled = shouldBeEnabled;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            changeVisibilityForPath(paths);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  paths  DOCUMENT ME!
         */
        private void changeVisibilityForPath(final TreePath[] paths) {
            for (final TreePath path : paths) {
                final Object selectedComponent = path.getLastPathComponent();

                if (selectedComponent instanceof ServiceLayer) {
                    final ServiceLayer layer = (ServiceLayer)selectedComponent;
                    if (layer.isEnabled() != shouldBeEnabled) {
                        layer.setEnabled(shouldBeEnabled);
                    }
                    if (layerModel.isVisible(path) != shouldBeEnabled) {
                        changeVisibility(selectedComponent, shouldBeEnabled);
                    }
                } else if (selectedComponent instanceof LayerCollection) {
                    final LayerCollection layer = (LayerCollection)selectedComponent;

                    if (layer.isEnabled() != shouldBeEnabled) {
                        changeVisibility(selectedComponent, shouldBeEnabled);
                    }
                } else if (selectedComponent.equals(layerModel.getRoot())) {
                    final List<TreePath> tp = new ArrayList<TreePath>();
                    final TreePath rootPath = new TreePath(layerModel.getRoot());

                    for (int i = 0; i < layerModel.getChildCount(layerModel.getRoot()); ++i) {
                        tp.add(rootPath.pathByAddingChild(layerModel.getChild(layerModel.getRoot(), i)));
                    }

                    changeVisibilityForPath(tp.toArray(new TreePath[tp.size()]));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class InvisibilityMenuItem extends VisibilityMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new VisibilityMenuItem object.
         */
        public InvisibilityMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.InvisibilityMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER,
                false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AllSelectableMenuItem extends ThemeLayerMenuItem {

        //~ Instance fields ----------------------------------------------------

        protected boolean shouldBeEnabled = true;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new VisibilityMenuItem object.
         */
        public AllSelectableMenuItem() {
            this(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.AllSelectableMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER,
                true);
            newSection = true;
        }

        /**
         * Creates a new VisibilityMenuItem object.
         *
         * @param  title            DOCUMENT ME!
         * @param  visibility       DOCUMENT ME!
         * @param  shouldBeEnabled  DOCUMENT ME!
         */
        protected AllSelectableMenuItem(final String title, final int visibility, final boolean shouldBeEnabled) {
            super(title, visibility);
            this.shouldBeEnabled = shouldBeEnabled;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            changeSelectability(paths);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  paths  DOCUMENT ME!
         */
        private void changeSelectability(final TreePath[] paths) {
            for (final TreePath path : paths) {
                final Object selectedComponent = path.getLastPathComponent();

                if (selectedComponent instanceof AbstractFeatureService) {
                    final AbstractFeatureService layer = (AbstractFeatureService)selectedComponent;
                    layer.setSelectable(shouldBeEnabled);
                } else if (selectedComponent instanceof LayerCollection) {
                    final LayerCollection layer = (LayerCollection)selectedComponent;
                    makeSelectable(layer);
                } else if (selectedComponent.equals(layerModel.getRoot())) {
                    final List<TreePath> tp = new ArrayList<TreePath>();
                    final TreePath rootPath = new TreePath(layerModel.getRoot());

                    for (int i = 0; i < layerModel.getChildCount(layerModel.getRoot()); ++i) {
                        tp.add(rootPath.pathByAddingChild(layerModel.getChild(layerModel.getRoot(), i)));
                    }

                    changeSelectability(tp.toArray(new TreePath[tp.size()]));
                }
            }
            // workaround to avoid visualisation problems without this workaround, the
            // bounds of the row of the edited path are wrong
            tree.updateUI();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  lc  DOCUMENT ME!
         */
        private void makeSelectable(final LayerCollection lc) {
            for (final Object layer : lc) {
                if (layer instanceof AbstractFeatureService) {
                    ((AbstractFeatureService)layer).setSelectable(shouldBeEnabled);
                } else if (layer instanceof LayerCollection) {
                    makeSelectable((LayerCollection)layer);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AllUnselectableMenuItem extends AllSelectableMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new VisibilityMenuItem object.
         */
        public AllUnselectableMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.AllUnselectableMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER,
                false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExpandMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EntensionMenuItem object.
         */
        public ExpandMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.EntensionMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            expandAll(new TreePath(layerModel.getRoot()));
        }

        /**
         * DOCUMENT ME!
         *
         * @param  parent  DOCUMENT ME!
         */
        private void expandAll(final TreePath parent) {
            final Object lastComponent = parent.getLastPathComponent();

            tree.expandPath(parent);

            for (int i = 0; i < layerModel.getChildCount(lastComponent); ++i) {
                final TreePath newPath = parent.pathByAddingChild(layerModel.getChild(lastComponent, i));
                expandAll(newPath);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AddThemeMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EntensionMenuItem object.
         */
        public AddThemeMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.AddThemeMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            // do nothing
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CollapseMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EntensionMenuItem object.
         */
        public CollapseMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.CollapseMenuItem.pmenuItem.text"),
                ROOT
                        | FOLDER);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            int row = tree.getRowCount() - 1;

            while (row > 0) {
                tree.collapseRow(row--);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class RemoveGroupMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveGroupMenuItem object.
         */
        public RemoveGroupMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.RemoveGroupMenuItem.pmenuItem.text"),
                FOLDER);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            if (paths.length == 1) {
                tree.setSelectionPath(null);
                try {
                    removeGroupLayerContent(paths[0]);
                    layerModel.removeLayer(paths[0]);
                } catch (VetoException ex) {
                    // nothing to do
                }
//                for (final TreePath tp : expendedPaths) {
//                    tree.expandPath(tp);
//                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   path  DOCUMENT ME!
         *
         * @throws  VetoException  DOCUMENT ME!
         */
        private void removeGroupLayerContent(final TreePath path) throws VetoException {
            if (path.getLastPathComponent() instanceof LayerCollection) {
                final LayerCollection lc = (LayerCollection)path.getLastPathComponent();
                final List<Object> children = new ArrayList<Object>(lc);

                for (final Object o : children) {
                    if (o instanceof LayerCollection) {
                        removeGroupLayerContent(path.pathByAddingChild(o));
                    } else {
                        removePath(path.pathByAddingChild(o));
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class RemoveThemeMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public RemoveThemeMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.RemoveThemeMenuItem.pmenuItem.text"),
                NODE
                        | FEATURE_SERVICE
                        | MULTI);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();
            tree.setSelectionPath(null);

            final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(this),
                    true,
                    NbBundle.getMessage(
                        SelectAllMenuItem.class,
                        "ThemeLayerWidget.RemoveThemeMenuItem.actionPerformed.text"),
                    null,
                    500,
                    true) {

                    @Override
                    protected Void doInBackground() throws Exception {
                        Thread.currentThread().setName("ThemeLayerWidget_remove_theme");
                        int progress = 0;
                        wd.setMax(paths.length);

                        for (final TreePath tmpPath : paths) {
                            wd.setProgress(++progress);
                            EventQueue.invokeAndWait(new Runnable() {

                                    @Override
                                    public void run() {
                                        try {
                                            removePath(tmpPath);
                                        } catch (VetoException ex) {
                                            // nothing to do
                                        }
                                    }
                                });
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (Exception e) {
                            log.error("Error while removing layer from tree", e);
                        }
                    }
                };
            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ZoomToSelectedItemsMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public ZoomToSelectedItemsMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.ZoomToSelectedItemsMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();
            final List<Feature> features = new ArrayList<Feature>();

            for (final TreePath o : paths) {
                final AbstractFeatureService afs = (AbstractFeatureService)o.getLastPathComponent();
                final List<Feature> featuresForService = SelectionManager.getInstance().getSelectedFeatures(afs);

                if (featuresForService != null) {
                    features.addAll(featuresForService);
                }
            }

            final ZoomToFeaturesWorker worker = new ZoomToFeaturesWorker(features.toArray(new Feature[features.size()]),
                    10);
            worker.execute();
        }

        @Override
        public boolean isSelectable(final int mask) {
            final TreePath[] paths = tree.getSelectionPaths();
            boolean featuresSelected = false;

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    if (SelectionManager.getInstance().getSelectedFeaturesCount(service) > 0) {
                        featuresSelected = true;
                        break;
                    }
                }
            }

            return featuresSelected && super.isSelectable(mask);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ZoomToThemeMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public ZoomToThemeMenuItem() {
            super(NbBundle.getMessage(ThemeLayerWidget.class, "ThemeLayerWidget.ZoomToThemeMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = tree.getSelectionPaths();
            final ZoomToLayerWorker worker = new ZoomToLayerWorker(tps, 10);
            worker.execute();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class OpenAttributeTableMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public OpenAttributeTableMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.OpenAttributeTableMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    AttributeTableFactory.getInstance().showAttributeTable(service);
                }
            }
        }
    }

    /**
     * Switches the processing mode of a feature service.
     *
     * @version  $Revision$, $Date$
     */
    private class EditModeMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EditModeMenuItem object.
         */
        public EditModeMenuItem() {
            super(NbBundle.getMessage(EditModeMenuItem.class, "ThemeLayerWidget.EditModeMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public boolean isSelectable(final int mask) {
            final TreePath[] paths = tree.getSelectionPaths();

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    if (!((AbstractFeatureService)path.getLastPathComponent()).isEditable()) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    AttributeTableFactory.getInstance().switchProcessingMode(service);
                }
            }

            refreshText(paths);
        }

        @Override
        public void refreshText(final TreePath[] paths) {
            boolean isInProcessingMode = true;

            for (final TreePath tp : paths) {
                if (tp.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)tp.getLastPathComponent();
                    if (!SelectionManager.getInstance().getEditableServices().contains(service)) {
                        isInProcessingMode = false;
                    }
                }
            }

            if (!isInProcessingMode) {
                setText(NbBundle.getMessage(
                        ThemeLayerWidget.class,
                        "ThemeLayerWidget.EditModeMenuItem.pmenuItem.text"));
            } else {
                setText(NbBundle.getMessage(
                        ThemeLayerWidget.class,
                        "ThemeLayerWidget.NoEditModeMenuItem.pmenuItem.text"));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SelectAllMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public SelectAllMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.SelectAllMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

//            for (final TreePath path : paths) {
//                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
//                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
//                    final List<Feature> toBeSelected = new ArrayList<Feature>();
//                    for (final Object featureObject : service.getPNode().getChildrenReference()) {
//                        final PFeature feature = (PFeature)featureObject;
//
//                        if (!feature.isSelected()) {
//                            feature.setSelected(true);
//                            final SelectionListener sl = (SelectionListener)CismapBroker.getInstance()
//                                        .getMappingComponent()
//                                        .getInputEventListener()
//                                        .get(MappingComponent.SELECT);
//                            sl.addSelectedFeature(feature);
//                            toBeSelected.add(feature.getFeature());
//                        }
//                    }
//
//                    ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
//                            .addToSelection(toBeSelected);
//                }

            final WaitingDialogThread<List<FeatureServiceFeature>> wdt =
                new WaitingDialogThread<List<FeatureServiceFeature>>(StaticSwingTools.getParentFrame(this),
                    true,
                    NbBundle.getMessage(
                        SelectAllMenuItem.class,
                        "ThemeLayerWidget.SelectAllMenuItem.actionPerformed.text"),
                    null,
                    500,
                    true) {

                    @Override
                    protected List<FeatureServiceFeature> doInBackground() throws Exception {
                        Thread.currentThread().setName("ThemeLayerWidget_select_all");
                        final List<FeatureServiceFeature> toBeSelected = new ArrayList<FeatureServiceFeature>();

                        for (final TreePath path : paths) {
                            if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                                final AbstractFeatureService service = (AbstractFeatureService)
                                    path.getLastPathComponent();
                                final XBoundingBox bb = null;
                                service.initAndWait();
                                final FeatureFactory factory = service.getFeatureFactory();
                                List<FeatureServiceFeature> featureList;
                                final int pageSize = service.getMaxFeaturesPerPage();

                                if (pageSize != -1) {
                                    final AttributeTable table = SelectionManager.getInstance()
                                                .getAttributeTableForService(service);

                                    if (table == null) {
                                        featureList = factory.createFeatures(
                                                service.getQuery(),
                                                bb,
                                                null,
                                                0,
                                                pageSize,
                                                null);
                                    } else {
                                        EventQueue.invokeLater(new Runnable() {

                                                @Override
                                                public void run() {
                                                    table.selectAll();
                                                }
                                            });

                                        return null;
                                    }
                                } else {
                                    featureList = factory.createFeatures(service.getQuery(),
                                            bb,
                                            null, 0, 0, null);
                                }

                                toBeSelected.addAll(featureList);
                            }

                            if (Thread.interrupted() || canceled) {
                                return null;
                            }
                        }

                        return toBeSelected;
                    }

                    @Override
                    protected void done() {
                        try {
                            final List<FeatureServiceFeature> features = get();

                            if (features != null) {
                                SelectionManager.getInstance().addSelectedFeatures(features);
                            }
                        } catch (Exception e) {
                            log.error("Error while selecting all features", e);
                        }
                    }
                };
            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class InvertSelectionTableMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public InvertSelectionTableMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.InvertSelectionTableMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            final WaitingDialogThread<List[]> wdt;
            wdt = new WaitingDialogThread<List[]>(StaticSwingTools.getParentFrame(this),
                    true,
                    NbBundle.getMessage(
                        SelectAllMenuItem.class,
                        "ThemeLayerWidget.InvertSelectionTableMenuItem.actionPerformed.text"),
                    null,
                    500,
                    true) {

                    @Override
                    protected List[] doInBackground() throws Exception {
                        Thread.currentThread().setName("ThemeLayerWidget_invert_selection");
                        final List<Feature> toBeSelected = new ArrayList<Feature>();
                        final List<Feature> toBeUnselected = new ArrayList<Feature>();
                        final List<Feature>[] featureLists = new List[2];

                        for (final TreePath path : paths) {
                            if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                                final AbstractFeatureService service = (AbstractFeatureService)
                                    path.getLastPathComponent();
                                final XBoundingBox bb = null;
                                service.initAndWait();
                                final FeatureFactory factory = service.getFeatureFactory();
                                List<FeatureServiceFeature> featureList;
                                final int pageSize = service.getMaxFeaturesPerPage();

                                if (pageSize != -1) {
                                    featureList = factory.createFeatures(
                                            service.getQuery(),
                                            bb,
                                            null,
                                            0,
                                            pageSize,
                                            null);
                                } else {
                                    featureList = factory.createFeatures(service.getQuery(),
                                            bb,
                                            null, 0, 0, null);
                                }

                                toBeSelected.addAll(featureList);
                                final List<Feature> selectedFeatures = SelectionManager.getInstance()
                                            .getSelectedFeatures(service);
                                toBeSelected.removeAll(selectedFeatures);
                                toBeUnselected.addAll(selectedFeatures);
                            }

                            if (Thread.interrupted() || canceled) {
                                return null;
                            }
                        }

                        featureLists[0] = toBeUnselected;
                        featureLists[1] = toBeSelected;

                        return featureLists;
                    }

                    @Override
                    protected void done() {
                        try {
                            final List[] features = get();

                            SelectionManager.getInstance().removeSelectedFeatures(features[0]);
                            SelectionManager.getInstance().addSelectedFeatures(features[1]);
                        } catch (Exception e) {
                            log.error("Error while selecting all features", e);
                        }
                    }
                };
            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ClearSelectionMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public ClearSelectionMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.ClearSelectionMenuItem.pmenuItem.text"),
                NODE
                        | MULTI
                        | FEATURE_SERVICE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    final List<Feature> toBeUnselected = SelectionManager.getInstance().getSelectedFeatures(service);
                    SelectionManager.getInstance().removeSelectedFeatures(toBeUnselected);
                }
            }
        }

        @Override
        public boolean isSelectable(final int mask) {
            final TreePath[] paths = tree.getSelectionPaths();
            boolean featuresSelected = false;

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    if (SelectionManager.getInstance().getSelectedFeaturesCount(service) > 0) {
                        featuresSelected = true;
                        break;
                    }
                }
            }

            return featuresSelected && super.isSelectable(mask);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class OptionsMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public OptionsMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.OptionsMenuItem.pmenuItem.text"),
                NODE
                        | FEATURE_SERVICE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath path = tree.getSelectionPath();

            final AbstractFeatureService selectedService = (AbstractFeatureService)path.getLastPathComponent();
            final Frame parentFrame = StaticSwingTools.getParentFrame(ThemeLayerWidget.this);
            final ArrayList<String> args = new ArrayList<String>();
            args.add("Allgemein");
            args.add("Darstellung");
            args.add("Massstab");
            args.add("Thematische Farbgebung");
            args.add("Beschriftung");
            args.add("TextEditor");
            args.add("QueryPanel");

            final StyleDialogStarter starter = new StyleDialogStarter(parentFrame, selectedService, args, 500);

            starter.addStyleDialogClosedListener(new StyleDialogClosedListener() {

                    @Override
                    public void StyleDialogClosed(final StyleDialogClosedEvent evt) {
                        tree.updateUI();
                    }
                });

            starter.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SelectableMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public SelectableMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.SelectionMenuItem.pmenuItem.text"),
                NODE
                        | FEATURE_SERVICE
                        | MULTI);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    service.setSelectable(!service.isSelectable());

                    // workaround to avoid visualisation problems without this workaround, the
                    // bounds of the row of the edited path are wrong
                    tree.updateUI();
                }
            }

            refreshText(paths);
        }

        @Override
        public void refreshText(final TreePath[] paths) {
            boolean isSelected = true;

            for (final TreePath tp : paths) {
                if (tp.getLastPathComponent() instanceof AbstractFeatureService) {
                    if (!((AbstractFeatureService)tp.getLastPathComponent()).isSelectable()) {
                        isSelected = false;
                    }
                }
            }

            if (!isSelected) {
                setText(NbBundle.getMessage(
                        ThemeLayerWidget.class,
                        "ThemeLayerWidget.SelectionMenuItem.pmenuItem.text"));
            } else {
                setText(NbBundle.getMessage(
                        ThemeLayerWidget.class,
                        "ThemeLayerWidget.NotSelectionMenuItem.pmenuItem.text"));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class CheckBoxNodeRenderer extends ActiveLayerTreeCellRenderer {

        //~ Instance fields ----------------------------------------------------

        protected Color selectionBorderColor;
        protected Color selectionForeground;
        protected Color selectionBackground;
        protected Color textForeground;
        protected Color textBackground;
        protected Boolean drawsFocusBorderAroundIcon;
        protected Font fontValue;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CheckBoxNodeRenderer object.
         */
        public CheckBoxNodeRenderer() {
            fontValue = UIManager.getFont("Tree.font");
            drawsFocusBorderAroundIcon = (Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon");
            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getTreeCellRendererComponent(final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean hasFocus) {
            JLabel lab = null;
            synchronized (ThemeLayerWidget.this.getTreeLock()) {
                String label = "<html>";
                boolean modifiable = false;

                if ((value instanceof AbstractFeatureService)
                            && SelectionManager.getInstance().getEditableServices().contains(
                                (AbstractFeatureService)value)) {
                    modifiable = true;
                }

                if (modifiable) {
                    label += "<span color=\"#FF0000\">" + value.toString() + "</span>";
                } else {
                    if (value.equals(layerModel.getRoot())) {
                        label += NbBundle.getMessage(
                                CheckBoxNodeRenderer.class,
                                "ThemeLayerWidget.CheckBoxNodeRenderer.getTreeCellRendererComponent.root");
                    } else {
                        label += value.toString();
                    }
                }
                final AbstractFeatureService service = ((value instanceof AbstractFeatureService)
                        ? (AbstractFeatureService)value : null);

                if (service != null) {
                    final Integer selectedFeatureCount = SelectionManager.getInstance()
                                .getSelectedFeaturesCount(service);

                    if ((selectedFeatureCount != null) && (selectedFeatureCount != 0)) {
                        label += " | " + selectedFeatureCount;
                    }

                    final Integer modifiableFeatureCount = SelectionManager.getInstance()
                                .getModifiableFeaturesCount(service);

                    if (modifiableFeatureCount != null) {
                        label += " | <span color=\"#FF0000\">" + modifiableFeatureCount + "</span>";
                    }
                }
                label += "</html>";

                final Component ret = super.getTreeCellRendererComponent(
                        tree,
                        value,
                        selected,
                        expanded,
                        leaf,
                        row,
                        hasFocus);
                final JLabel retLab = (JLabel)ret;
                lab = new JLabel(label, retLab.getIcon(), retLab.getHorizontalAlignment());

                if (value instanceof LayerCollection) {
                    if (expanded) {
                        lab.setIcon(openIcon);
                    } else {
                        lab.setIcon(closedIcon);
                    }
                }
            }
            final JPanel pan = new JPanel();
            final JCheckBox leafRenderer = new JCheckBox();
            pan.setLayout(new GridBagLayout());

            if (value instanceof AbstractFeatureService) {
                if (((AbstractFeatureService)value).isSelectable()) {
                    final Font boldFont = lab.getFont().deriveFont(Font.BOLD);
                    lab.setFont(boldFont);
                }
            }

            if (fontValue != null) {
                leafRenderer.setFont(fontValue);
            }
            leafRenderer.setFocusPainted((drawsFocusBorderAroundIcon != null)
                        && (drawsFocusBorderAroundIcon.booleanValue()));

            leafRenderer.setEnabled(tree.isEnabled());

            if (selected) {
                leafRenderer.setForeground(selectionForeground);
                leafRenderer.setBackground(selectionBackground);
                pan.setForeground(selectionForeground);
                pan.setBackground(selectionBackground);
            } else {
                leafRenderer.setForeground(textForeground);
                leafRenderer.setBackground(textBackground);
                pan.setForeground(textForeground);
                pan.setBackground(textBackground);
            }

            if (value instanceof AbstractFeatureService) {
                final Object box = CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera();

                if (box instanceof XBoundingBox) {
                    final XBoundingBox currentBBox = (XBoundingBox)box;

                    if (!((AbstractFeatureService)value).isVisibleInBoundingBox(currentBBox)) {
                        leafRenderer.setEnabled(false);
                    }
                }
            } else if (value instanceof WMSServiceLayer) {
                final WMSServiceLayer serviceLayer = (WMSServiceLayer)value;
                double min = Double.MIN_VALUE;
                double max = Double.MAX_VALUE;
                Layer tmpLayer = null;

                if (((WMSServiceLayer)value).getWMSLayers().size() == 1) {
                    tmpLayer = ((WMSLayer)((WMSServiceLayer)value).getWMSLayers().get(0)).getOgcCapabilitiesLayer();

                    if (tmpLayer != null) {
                        min = tmpLayer.getScaleDenominationMin();
                        max = tmpLayer.getScaleDenominationMax();
                    }
                }

                if ((tmpLayer == null) && (serviceLayer.getWmsCapabilities() != null)
                            && (serviceLayer.getWmsCapabilities().getLayer() != null)) {
                    min = serviceLayer.getWmsCapabilities().getLayer().getScaleDenominationMin();
                    max = serviceLayer.getWmsCapabilities().getLayer().getScaleDenominationMax();
                }

                final double scale = CismapBroker.getInstance().getMappingComponent().getCurrentOGCScale();

                if ((scale < min) || (scale > max)) {
                    leafRenderer.setEnabled(false);
                }
            }

            if ((value instanceof ShapeFileFeatureService) && ((ShapeFileFeatureService)value).isFileNotFound()) {
                lab.setForeground(Color.GRAY);
            } else if ((value instanceof H2FeatureService) && ((H2FeatureService)value).isTableNotFound()) {
                lab.setForeground(Color.GRAY);
            } else if (value instanceof ImageRasterService) {
                if ((((ImageRasterService)value).getImageFile() != null)
                            && !((ImageRasterService)value).getImageFile().exists()) {
                    lab.setForeground(Color.GRAY);
                }
            }

            leafRenderer.setSelected(isValueSelected(value));

            if (!(value instanceof WMSLayer) && !value.equals(layerModel.getRoot())) {
                pan.add(leafRenderer);
            }
            pan.add(lab);
            pan.doLayout();
            pan.repaint();
            return pan;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   value  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        protected boolean isValueSelected(final Object value) {
            if (value instanceof ServiceLayer) {
                final ServiceLayer layer = (ServiceLayer)value;
                return layer.isEnabled();
            } else if (value instanceof LayerCollection) {
                final LayerCollection layer = (LayerCollection)value;
                return layer.isEnabled();
            } else if (value.equals(layerModel.getRoot())) {
                boolean enabled = true;

                for (int i = 0; i < layerModel.getChildCount(layerModel.getRoot()); ++i) {
                    final Object tmpLayer = layerModel.getChild(layerModel.getRoot(), i);
                    if (tmpLayer instanceof LayerCollection) {
                        if (!((LayerCollection)tmpLayer).isEnabled()) {
                            enabled = false;
                        }
                    } else if (tmpLayer instanceof ServiceLayer) {
                        if (!((ServiceLayer)tmpLayer).isEnabled()) {
                            enabled = false;
                        }
                    }
                }

                return enabled;
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class CheckBoxNodeEditor extends CheckBoxNodeRenderer implements TreeCellEditor {

        //~ Instance fields ----------------------------------------------------

        private Object value;
        private MouseAdapter lastAdapter = null;
        private JTextField treeEditorTextField;
        private List<CellEditorListener> listener = new ArrayList<CellEditorListener>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CheckBoxNodeEditor object.
         */
        public CheckBoxNodeEditor() {
            super();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getTreeCellRendererComponent(final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean hasFocus) {
            final JPanel pan = (JPanel)super.getTreeCellRendererComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);
            if (!(pan.getComponent(0) instanceof JCheckBox)) {
                // the root element has no checkbox
                return pan;
            }
            final JCheckBox leafRenderer = (JCheckBox)pan.getComponent(0);
            final Component ret = pan.getComponent(1);

            if (lastAdapter != null) {
                ret.removeMouseListener(lastAdapter);
            }
            treeEditorTextField = null;

            lastAdapter = new MouseAdapter() {

                    @Override
                    public void mousePressed(final MouseEvent e) {
                        createPopupMenu();
                        popupMenuListener.mousePressed(e);
                    }

                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        createPopupMenu();
                        popupMenuListener.mouseReleased(e);
                    }

                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (e.isPopupTrigger() || (e.getButton() != MouseEvent.BUTTON1)) {
                            createPopupMenu();
                            popupMenuListener.mouseClicked(e);
                            return;
                        }
                        if (ret instanceof JLabel) {
                            final String text = value.toString();
                            ((JLabel)ret).setText("");
                            treeEditorTextField = new JTextField(text);
                            treeEditorTextField.addKeyListener(new KeyAdapter() {

                                    @Override
                                    public void keyTyped(final KeyEvent e) {
                                        if (e.getKeyChar() == '\n') {
                                            tree.stopEditing();
                                            // workaround to avoid visualisation problems without this workaround, the
                                            // bounds of the row of the edited path are wrong
                                            tree.startEditingAtPath(tree.getPathForRow(row));
                                            tree.stopEditing();
                                        }
                                    }
                                });
                            treeEditorTextField.setPreferredSize(((JLabel)ret).getPreferredSize());
                            treeEditorTextField.setMinimumSize(((JLabel)ret).getMinimumSize());
                            treeEditorTextField.setMaximumSize(((JLabel)ret).getMaximumSize());
                            treeEditorTextField.setEditable(true);
                            ((JLabel)ret).setMinimumSize(null);
                            ((JLabel)ret).setMaximumSize(null);
                            ((JLabel)ret).setPreferredSize(null);
                            pan.add(
                                treeEditorTextField,
                                new GridBagConstraints(
                                    2,
                                    0,
                                    1,
                                    1,
                                    1,
                                    0,
                                    GridBagConstraints.CENTER,
                                    GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0),
                                    0,
                                    0));
                            treeEditorTextField.setSelectionStart(0);
                            treeEditorTextField.setSelectionEnd(treeEditorTextField.getText().length());
                            treeEditorTextField.setFocusable(true);
                            treeEditorTextField.requestFocusInWindow();
                            pan.repaint();
                            pan.updateUI();
                        }
                    }
                };

            ret.addMouseListener(lastAdapter);

            leafRenderer.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        changeVisibility(value);
                    }
                });

            leafRenderer.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        // The action performed method will not be invoked, if the checkbox is disabled
                        if (!leafRenderer.isEnabled()) {
                            leafRenderer.setSelected(!leafRenderer.isSelected());
                            changeVisibility(value);
                        }
                    }
                });

            return pan;
        }

        @Override
        public Component getTreeCellEditorComponent(final JTree tree,
                final Object value,
                final boolean isSelected,
                final boolean expanded,
                final boolean leaf,
                final int row) {
            this.value = value;
            return getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        }

        @Override
        public Object getCellEditorValue() {
            if (treeEditorTextField != null) {
                return treeEditorTextField.getText();
            }
            return value;
        }

        @Override
        public boolean isCellEditable(final EventObject anEvent) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(final EventObject anEvent) {
            return false;
        }

        @Override
        public boolean stopCellEditing() {
            setNewName(getCellEditorValue().toString());
            return true;
        }

        @Override
        public void cancelCellEditing() {
        }

        /**
         * Adds the <code>CellEditorListener</code>.
         *
         * @param  l  the listener to be added
         */
        @Override
        public void addCellEditorListener(final CellEditorListener l) {
            listener.add(l);
        }

        /**
         * Removes the previously added <code>CellEditorListener</code>.
         *
         * @param  l  the listener to be removed
         */
        @Override
        public void removeCellEditorListener(final CellEditorListener l) {
            listener.remove(l);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  name  DOCUMENT ME!
         */
        private void setNewName(final String name) {
            if (value instanceof ServiceLayer) {
                final ServiceLayer layer = (ServiceLayer)value;
                layer.setName(name);
            } else if (value instanceof LayerCollection) {
                final LayerCollection layer = (LayerCollection)value;
                layer.setName(name);
            }
        }
    }

    /**
     * Without this special JTree, a NPE will be thrown when a drop operation on a TransferHandler occurs.
     *
     * @version  $Revision$, $Date$
     */
    private class TreeWithoutNPEAfterDrop extends JTree {

        //~ Methods ------------------------------------------------------------

        @Override
        protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
            if ((newValue != null) || !propertyName.equals("dropLocation")) {
                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }
    }
}
