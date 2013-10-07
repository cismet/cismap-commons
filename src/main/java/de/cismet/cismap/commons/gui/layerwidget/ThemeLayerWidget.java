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
import java.awt.Font;
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
import java.util.EventObject;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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

import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.DefaultPopupMenuListener;
import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ThemeLayerWidget extends javax.swing.JPanel implements TreeSelectionListener { // implements

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

        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setCellEditor(new CheckBoxNodeEditor());
        tree.setEditable(true);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        transferHandler = new TreeTransferHandler();
        tree.setTransferHandler(transferHandler);
//        tree.setCellRenderer(new DefaultTreeCellRenderer() {
//
//            @Override
//            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//                Component renderer = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//                //JPanel pan = new JPanel();
//                //pan.add(new JCheckBox());
//                set
//                return renderer;
//            }
//        });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mappingModel  DOCUMENT ME!
     */
    public void setMappingModel(final ActiveLayerModel mappingModel) {
        layerModel = mappingModel;
        tree.setModel(layerModel);

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
        menuItems.add(new LabelMenuItem());
        menuItems.add(new StartProcessingModeMenuItem());
        menuItems.add(new ExportMenuItem());
        menuItems.add(new OptionsMenuItem());

        tree.getSelectionModel().addTreeSelectionListener(this);
        createPopupMenu();
        tree.addMouseListener(popupMenuListener);
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
        }

        mask += (root ? ThemeLayerMenuItem.ROOT : 0);
        mask += (folder ? ThemeLayerMenuItem.FOLDER : 0);
        mask += (node ? ThemeLayerMenuItem.NODE : 0);
        mask += (multi ? ThemeLayerMenuItem.MULTI : 0);

        for (final ThemeLayerMenuItem item : menuItems) {
            if (item.isVisible(mask)) {
                if (item.isNewSection()) {
                    popupMenu.addSeparator();
                }
                popupMenu.add(item);
                item.setEnabled(item.isSelectable(mask));
            }
        }
    }

    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        createPopupMenu();
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
                        | FOLDER,
                1);
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
                NODE);
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
                        | MULTI);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            // TODO: Uses the same logic as the ZoomToThemeMenuItem, at the moment.
            final TreePath[] tps = tree.getSelectionPaths();
            final ZoomToLayerWorker worker = new ZoomToLayerWorker(tps);
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
                        | MULTI);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = tree.getSelectionPaths();
            final ZoomToLayerWorker worker = new ZoomToLayerWorker(tps);
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
                        | MULTI);
        }

        //~ Methods ------------------------------------------------------------

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
                        | MULTI);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
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
                        | MULTI);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
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
                        | MULTI);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LabelMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public LabelMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.LabelMenuItem.pmenuItem.text"),
                NODE
                        | MULTI,
                0);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class StartProcessingModeMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public StartProcessingModeMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.StartProcessingModeMenuItem.pmenuItem.text"),
                NODE
                        | MULTI,
                0);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
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
                        | MULTI,
                0);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
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
                NODE,
                0);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private abstract class ThemeLayerMenuItem extends JMenuItem implements ActionListener {

        //~ Static fields/initializers -----------------------------------------

        public static final int ROOT = 1;
        public static final int NODE = 2;
        public static final int FOLDER = 4;
        public static final int MULTI = 8;
        public static final int EVER = 255;

        //~ Instance fields ----------------------------------------------------

        protected int selectable = 0;
        protected boolean newSection = false;

        private int visibility = 0;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ThemeLayerMenuItem object.
         *
         * @param  title       DOCUMENT ME!
         * @param  visibility  DOCUMENT ME!
         */
        public ThemeLayerMenuItem(final String title, final int visibility) {
            this(title, visibility, visibility);
        }

        /**
         * Creates a new ThemeLayerMenuItem object.
         *
         * @param  title       DOCUMENT ME!
         * @param  visibility  DOCUMENT ME!
         * @param  selectable  DOCUMENT ME!
         */
        public ThemeLayerMenuItem(final String title, final int visibility, final int selectable) {
            super(title);
            this.visibility = visibility;
            this.selectable = selectable;
            addActionListener(this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   mask  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isVisible(final int mask) {
            return (visibility & mask) == mask;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   mask  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isSelectable(final int mask) {
            return (selectable & mask) == mask;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isNewSection() {
            return newSection;
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
            final Component ret = super.getTreeCellRendererComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);
            final JPanel pan = new JPanel();
            final JCheckBox leafRenderer = new JCheckBox();
            pan.setLayout(new GridBagLayout());

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

            leafRenderer.setSelected(isValueSelected(value));

            pan.add(leafRenderer);
            pan.add(ret);
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
            final Object renderedObject = value;

            leafRenderer.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (renderedObject.equals(layerModel.getRoot())) {
                            for (int i = 0; i < layerModel.getChildCount(layerModel.getRoot()); ++i) {
                                changeVisibility(layerModel.getChild(layerModel.getRoot(), i));
                            }
                        } else {
                            changeVisibility(renderedObject);
                        }
                    }

                    private void changeVisibility(final Object objectToChange) {
                        if (objectToChange instanceof ServiceLayer) {
                            ((ServiceLayer)objectToChange).setEnabled(!((ServiceLayer)objectToChange).isEnabled());
                            layerModel.handleVisibility(tree.getSelectionPath());
                        } else if (objectToChange instanceof LayerCollection) {
                            ((LayerCollection)objectToChange).setEnabled(
                                !((LayerCollection)objectToChange).isEnabled());
                        }
                    }
                });

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
                            final String text = ((JLabel)ret).getText();
                            ((JLabel)ret).setText("");
                            log.error("setText " + text);
                            treeEditorTextField = new JTextField(text);
                            treeEditorTextField.addKeyListener(new KeyAdapter() {

                                    @Override
                                    public void keyTyped(final KeyEvent e) {
                                        if (e.getKeyChar() == '\n') {
                                            stopCellEditing();
                                            ((JLabel)ret).setText(getCellEditorValue().toString());
                                            ((JLabel)ret).setPreferredSize(treeEditorTextField.getPreferredSize());
                                            ((JLabel)ret).setMinimumSize(treeEditorTextField.getMinimumSize());
                                            ((JLabel)ret).setMaximumSize(treeEditorTextField.getMaximumSize());
                                            pan.remove(treeEditorTextField);
                                            pan.repaint();
                                            pan.updateUI();
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
}
