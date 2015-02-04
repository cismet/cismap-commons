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

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.style.StyleDialogInterface;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget.CheckBoxNodeRenderer;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.tools.ExportShapeDownload;

import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.tools.gui.DefaultPopupMenuListener;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ThemeLayerWidget extends javax.swing.JPanel implements TreeSelectionListener, FeatureCollectionListener { // implements

    //~ Instance fields --------------------------------------------------------

    // DropTargetListener {

    // implements

    // DropTargetListener {
    private Logger log = Logger.getLogger(ThemeLayerWidget.class);
    private JPopupMenu popupMenu = new JPopupMenu();
    private List<ThemeLayerMenuItem> menuItems = new ArrayList<ThemeLayerMenuItem>();
    private ActiveLayerModel layerModel;
    private DefaultPopupMenuListener popupMenuListener = new DefaultPopupMenuListener(popupMenu);
    private TreeTransferHandler transferHandler;
    private StyleDialogInterface styleDialog;
    private Map<Object, Integer> selectedFeatures = new HashMap<Object, Integer>();
    private Map<Object, Integer> modifiableFeatures = new HashMap<Object, Integer>();
    private UpdateSelectionThread selectionUpdateThread = null;
    private ExecutorService executor = CismetExecutors.newSingleThreadExecutor();
    private List<AbstractFeatureService> editableServices = new ArrayList<AbstractFeatureService>();

    private AddThemeMenuItem addThemeMenuItem;
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

        editableServices = Collections.synchronizedList(editableServices);
        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setCellEditor(new CheckBoxNodeEditor());
        tree.setEditable(true);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        transferHandler = new TreeTransferHandler();
        tree.setTransferHandler(transferHandler);
    }

    //~ Methods ----------------------------------------------------------------

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

        menuItems.add(new AddFolderMenuItem());
        menuItems.add(new RemoveGroupMenuItem());
        addThemeMenuItem = new AddThemeMenuItem();
        menuItems.add(addThemeMenuItem);
        menuItems.add(new VisibilityMenuItem());
        menuItems.add(new InvisibilityMenuItem());
        menuItems.add(new ExpandMenuItem());
        menuItems.add(new CollapseMenuItem());
        menuItems.add(new RemoveThemeMenuItem());
        menuItems.add(new OpenAttributeTableMenuItem());
        menuItems.add(new ZoomToThemeMenuItem());
        menuItems.add(new ZoomToSelectedItemsMenuItem());
        menuItems.add(new SelectAllMenuItem());
        menuItems.add(new InvertSelectionTableMenuItem());
        menuItems.add(new ClearSelectionMenuItem());
        menuItems.add(new SelectableMenuItem());
        menuItems.add(new ExportMenuItem());
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

        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        mc.getFeatureCollection().addFeatureCollectionListener(this);
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
            if (item.isVisible(mask)) {
                if (item.isNewSection()) {
                    popupMenu.addSeparator();
                }
                popupMenu.add(item);
                item.setEnabled(item.isSelectable(mask));
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

            for (int i = 0; i < lc.size(); ++i) {
                changeVisibility(lc.get(i));
            }
        } else if (objectToChange instanceof ServiceLayer) {
            ((ServiceLayer)objectToChange).setEnabled(!((ServiceLayer)objectToChange).isEnabled());
            // only the last component of the tree path will be considered within
            // the methods isVisible(TreePath) and handleVisibiliy(TreePath)
            final TreePath tp = new TreePath(new Object[] { layerModel.getRoot(), objectToChange });
            layerModel.handleVisibility(tp);

            if (((ServiceLayer)objectToChange).isEnabled() && (objectToChange instanceof MapService)) {
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
        tree = new javax.swing.JTree();

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
    public void featuresAdded(final FeatureCollectionEvent fce) {
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        refreshSelectedFeatureCounts();
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }

    /**
     * DOCUMENT ME!
     */
    private synchronized void refreshSelectedFeatureCounts() {
        if (selectionUpdateThread != null) {
            selectionUpdateThread.interrupt();
        }

        selectionUpdateThread = new UpdateSelectionThread();
        // The selection of the features has not changed, yet. Wait until
        // all other selection listeners were notified.
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    executor.submit(selectionUpdateThread);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public void switchProcessingMode(final AbstractFeatureService service) {
        if (editableServices.contains(service)) {
            editableServices.remove(service);
        } else {
            editableServices.add(service);
        }
        refreshSelectedFeatureCounts();
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

            changeVisibility(paths);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  paths  DOCUMENT ME!
         */
        private void changeVisibility(final TreePath[] paths) {
            for (final TreePath path : paths) {
                final Object selectedComponent = path.getLastPathComponent();

                if (selectedComponent instanceof ServiceLayer) {
                    final ServiceLayer layer = (ServiceLayer)selectedComponent;
                    if (layer.isEnabled() != shouldBeEnabled) {
                        layer.setEnabled(shouldBeEnabled);
                    }
                    if (layerModel.isVisible(path) != shouldBeEnabled) {
                        layerModel.handleVisibility(path);
                    }
                } else if (selectedComponent instanceof LayerCollection) {
                    final LayerCollection layer = (LayerCollection)selectedComponent;

                    if (layer.isEnabled() != shouldBeEnabled) {
                        layer.setEnabled(shouldBeEnabled);
                    }
                } else if (selectedComponent.equals(layerModel.getRoot())) {
                    final List<TreePath> tp = new ArrayList<TreePath>();
                    final TreePath rootPath = new TreePath(layerModel.getRoot());

                    for (int i = 0; i < layerModel.getChildCount(layerModel.getRoot()); ++i) {
                        tp.add(rootPath.pathByAddingChild(layerModel.getChild(layerModel.getRoot(), i)));
                    }

                    changeVisibility(tp.toArray(new TreePath[tp.size()]));
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
                final Object selectedComponent = paths[0].getLastPathComponent();
                final Object parentComponent = paths[0].getParentPath().getLastPathComponent();

                tree.setSelectionPath(null);
                layerModel.removeLayer(paths[0]);

//                if (parentComponent instanceof LayerCollection) {
//                    final LayerCollection parent = (LayerCollection)parentComponent;
//                    tree.setSelectionPath(null);
//                    parent.remove(selectedComponent);
//                    layerModel.fireTreeStructureChanged(layerModel, new Object[] { selectedComponent }, null, null);
//                } else {
//                    tree.setSelectionPath(null);
//                    layerModel.removeLayerCollection((LayerCollection)selectedComponent);
//                    layerModel.fireTreeStructureChanged(layerModel, new Object[] { selectedComponent }, null, null);
//                }
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

            for (final TreePath tmpPath : paths) {
                layerModel.removeLayer(tmpPath);
            }
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
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = tree.getSelectionPaths();
            final List<PNode> nodesOfTheSelectedServices = new ArrayList<PNode>(paths.length);

            for (final TreePath o : paths) {
                final AbstractFeatureService afs = (AbstractFeatureService)o.getLastPathComponent();
                nodesOfTheSelectedServices.add(afs.getPNode());
            }

            final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
            final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
            final List<PFeature> sel = sl.getAllSelectedPFeatures();
            final Feature[] features = new Feature[sel.size()];

            for (int i = 0; i < sel.size(); ++i) {
                final PFeature feature = sel.get(i);
                if (nodesOfTheSelectedServices.contains(feature.getParent())) {
                    features[i] = feature.getFeature();
                }
            }

            final ZoomToFeaturesWorker worker = new ZoomToFeaturesWorker(features, 10);
            worker.execute();
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

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    final List<Feature> toBeSelected = new ArrayList<Feature>();
                    for (final Object featureObject : service.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;

                        if (!feature.isSelected()) {
                            feature.setSelected(true);
                            final SelectionListener sl = (SelectionListener)CismapBroker.getInstance()
                                        .getMappingComponent()
                                        .getInputEventListener()
                                        .get(MappingComponent.SELECT);
                            sl.addSelectedFeature(feature);
                            toBeSelected.add(feature.getFeature());
                        }
                    }

                    ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                            .addToSelection(toBeSelected);
                }
            }
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

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    final AbstractFeatureService service = (AbstractFeatureService)path.getLastPathComponent();
                    final List<Feature> toBeSelected = new ArrayList<Feature>();
                    final List<Feature> toBeUnselected = new ArrayList<Feature>();
                    for (final Object featureObject : service.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;

                        feature.setSelected(!feature.isSelected());
                        final SelectionListener sl = (SelectionListener)CismapBroker.getInstance().getMappingComponent()
                                    .getInputEventListener()
                                    .get(MappingComponent.SELECT);

                        if (feature.isSelected()) {
                            sl.addSelectedFeature(feature);
                            toBeSelected.add(feature.getFeature());
                        } else {
                            sl.removeSelectedFeature(feature);
                            toBeUnselected.add(feature.getFeature());
                        }
                    }
                    ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                            .addToSelection(toBeSelected);
                    ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                            .unselect(toBeUnselected);
                }
            }
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
                    final List<Feature> toBeUnselected = new ArrayList<Feature>();
                    for (final Object featureObject : service.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;

                        if (feature.isSelected()) {
                            feature.setSelected(false);
                            final SelectionListener sl = (SelectionListener)CismapBroker.getInstance()
                                        .getMappingComponent()
                                        .getInputEventListener()
                                        .get(MappingComponent.SELECT);
                            sl.removeSelectedFeature(feature);
                            toBeUnselected.add(feature.getFeature());
                        }
                    }
                    ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                            .unselect(toBeUnselected);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExportMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public ExportMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.ExportMenuItem.pmenuItem.text"),
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
            String name = null;
            final List<DefaultFeatureServiceFeature> features = new ArrayList<DefaultFeatureServiceFeature>();
            final TreePath[] paths = tree.getSelectionPaths();
            final List<AbstractFeatureService> services = new ArrayList<AbstractFeatureService>(paths.length);

            for (final TreePath o : paths) {
                final AbstractFeatureService afs = (AbstractFeatureService)o.getLastPathComponent();

                if (name == null) {
                    name = afs.getName();
                    if (name.indexOf(".") != -1) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }
                }
                services.add(afs);
            }

            final int option = JOptionPane.showOptionDialog(
                    this,
                    "Alle Features exportieren oder nur die ausgewählten?",
                    "Features exportieren",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[] { "alle Features", "selektierte Features" },
                    "alle Features");

            if (option == -1) {
                // The dialog was closed
                return;
            } else if (option == 1) {
                // export all selected features
                final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
                final SelectionListener sl = (SelectionListener)map.getInputEventListener()
                            .get(MappingComponent.SELECT);
                final List<PFeature> sel = sl.getAllSelectedPFeatures();
                final List<PNode> nodesOfTheSelectedServices = new ArrayList<PNode>(paths.length);

                for (final AbstractFeatureService afs : services) {
                    nodesOfTheSelectedServices.add(afs.getPNode());
                }

                for (int i = 0; i < sel.size(); ++i) {
                    final PFeature feature = sel.get(i);
                    if (nodesOfTheSelectedServices.contains(feature.getParent())) {
                        features.add((DefaultFeatureServiceFeature)feature.getFeature());
                    }
                }
            } else if (option == 0) {
                // export all features
                for (final AbstractFeatureService afs : services) {
                    final Geometry g = ZoomToLayerWorker.getServiceBounds(afs);
                    final XBoundingBox bb = new XBoundingBox(g);

                    try {
                        features.addAll(afs.getFeatureFactory().createFeatures(afs.getQuery(), bb, null, 0, 0, null));
                    } catch (Exception ex) {
                        log.error("Error while retrieving features", ex);
                    }
                }
            }

            if (DownloadManagerDialog.showAskingForUserTitle(this)) {
                DownloadManager.instance()
                        .add(new ExportShapeDownload(
                                name,
                                ".shp",
                                features.toArray(new DefaultFeatureServiceFeature[features.size()])));
            }
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
            /*
             * final JumpSLDEditor editor = new JumpSLDEditor();
             *
             * editor.ConfigureEditor( selectedService, StaticSwingTools.getParentFrame(wfsStyleButton),
             * CismapBroker.getInstance().getMappingComponent());
             */
            try {
                if (log.isDebugEnabled()) {
                    log.debug(
                        "invoke FeatureService - StyleDialog"); // NOI18N
                }
                // only create one instance of the styledialog
                final Frame parentFrame = StaticSwingTools.getParentFrame(ThemeLayerWidget.this);
                if (styleDialog == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("creating new StyleDialog '"
                                    + parentFrame.getTitle() + "'"); // NOI18N
                    }

                    final String lookupkey = "Jump";

                    if ((lookupkey != null) && !lookupkey.isEmpty()) {
                        final Lookup.Result<StyleDialogInterface> result = Lookup.getDefault()
                                    .lookupResult(StyleDialogInterface.class);

                        for (final StyleDialogInterface dialog : result.allInstances()) {
                            if (lookupkey.equals(dialog.getKey())) {
                                styleDialog = dialog;
                            }
                        }
                    }
                    if (styleDialog == null) {
                        styleDialog = Lookup.getDefault().lookup(StyleDialogInterface.class);
                    }
                }

                // configure dialog, adding attributes to the tab and
                // set style from the layer properties

                final ArrayList<String> args = new ArrayList<String>();
                args.add("Allgemein");
                args.add("Darstellung");
                args.add("Massstab");
                args.add("Thematische Farbgebung");
                args.add("Beschriftung");
                args.add("TextEditor");
                args.add("QueryPanel");
                // args.add("Begleitsymbole");

                final JDialog dialog = styleDialog.configureDialog(
                        selectedService,
                        parentFrame,
                        CismapBroker.getInstance().getMappingComponent(),
                        args);

                dialog.setPreferredSize(new Dimension(
                        dialog.getPreferredSize().width
                                + 70,
                        dialog.getPreferredSize().height));
                if (log.isDebugEnabled()) {
                    log.debug("set dialog visible");                                // NOI18N
                }
                StaticSwingTools.showDialog(dialog);
            } catch (Throwable t) {
                log.error("could not configure StyleDialog: " + t.getMessage(), t); // NOI18N
            }
            // check returnstatus
            if ((styleDialog != null) && styleDialog.isAccepted()) {
                final Runnable r = styleDialog.createResultTask();

                final ExecutorService es = CismetExecutors.newSingleThreadExecutor();
                es.submit(r);
                es.submit(new Runnable() {

                        @Override
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        tree.updateUI();
                                    }
                                });
                        }
                    });
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Style Dialog canceled"); // NOI18N
                }
            }
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
                String label = "<html>" + value.toString();
                final Integer selectedFeatureCount = selectedFeatures.get(value);

                if ((selectedFeatureCount != null) && (selectedFeatureCount != 0)) {
                    label += " | " + selectedFeatureCount;
                }

                final Integer modifiableFeatureCount = modifiableFeatures.get(value);

                if (modifiableFeatureCount != null) {
                    label += " | <span color=\"#FF0000\">" + modifiableFeatureCount + "</span>";
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

            if ((value instanceof ShapeFileFeatureService) && ((ShapeFileFeatureService)value).isFileNotFound()) {
                lab.setForeground(Color.GRAY);
            } else if ((value instanceof H2FeatureService) && ((H2FeatureService)value).isTableNotFound()) {
                lab.setForeground(Color.GRAY);
            }

            leafRenderer.setSelected(isValueSelected(value));

            if (!(value instanceof WMSLayer)) {
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
            final JCheckBox leafRenderer = (JCheckBox)pan.getComponent(0);
            final Component ret = pan.getComponent(1);

            if (lastAdapter != null) {
                ret.removeMouseListener(lastAdapter);
            }
            treeEditorTextField = null;

            lastAdapter = new MouseAdapter() {

                    @Override
                    public void mousePressed(final MouseEvent e) {
                        popupMenuListener.mousePressed(e);
                    }

                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        popupMenuListener.mouseReleased(e);
                    }

                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (e.isPopupTrigger() || (e.getButton() != MouseEvent.BUTTON1)) {
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
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class UpdateSelectionThread extends Thread {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UpdateSelectionThread object.
         */
        public UpdateSelectionThread() {
            super("UpdateSelectionThread");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            final TreeMap<Integer, MapService> mapTree = layerModel.getMapServices();
            boolean interrupted = false;

            for (final MapService service : mapTree.values()) {
                int count = 0;
                int modCount = 0;

                if (service instanceof AbstractFeatureService) {
                    final AbstractFeatureService featureService = (AbstractFeatureService)service;
                    final boolean serviceInEditMode = editableServices.contains(featureService);

                    for (final Object featureObject : featureService.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;
                        if (interrupted()) {
                            interrupted = true;
                            break;
                        }

                        if (feature.isSelected()) {
                            ++count;

                            if (serviceInEditMode) {
                                if (feature.getFeature() instanceof PermissionProvider) {
                                    if (((PermissionProvider)feature.getFeature()).hasWritePermissions()) {
                                        ++modCount;
                                    }
                                } else {
                                    ++modCount;
                                }
                            }
                        }
                    }

                    if (interrupted) {
                        break;
                    }

                    selectedFeatures.put(service, count);

                    if (serviceInEditMode && (count > 0)) {
                        modifiableFeatures.put(service, modCount);
                    } else {
                        modifiableFeatures.remove(service);
                    }
                }
            }

            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        tree.updateUI();
                    }
                });
        }
    }
}
