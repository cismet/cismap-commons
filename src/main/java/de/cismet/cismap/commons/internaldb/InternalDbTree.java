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
package de.cismet.cismap.commons.internaldb;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.h2gis.utilities.wrapper.ConnectionWrapper;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.JDBCFeatureService;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.StringFilter;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.tools.PointReferencingDialog;

import de.cismet.cismap.linearreferencing.tools.LinearReferencingDialog;

import de.cismet.tools.gui.DefaultPopupMenuListener;
import de.cismet.tools.gui.StaticSwingTools;

/**
 * This trees are shown in the capability widget to show the content of an internal db.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class InternalDbTree extends JTree {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(InternalDbTree.class);

    //~ Instance fields --------------------------------------------------------

    List<InternalDbMenuItem> menuList = new ArrayList<InternalDbMenuItem>();

    private Icon shapeIcon = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png"));

    private String databasePath;
    private JPopupMenu popupMenu = new JPopupMenu();
    private final List<TreePath> expendedPaths = new ArrayList<TreePath>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new InternalDbTree object.
     *
     * @param  databasePath  DOCUMENT ME!
     */
    public InternalDbTree(final String databasePath) {
        super();
        this.databasePath = databasePath;
        setDragEnabled(true);
        setEditable(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new DBTransferHandler());
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        menuList.add(new ZoomTo());
        menuList.add(new AddFolderItem());
        menuList.add(new RemoveItem());
        menuList.add(new AddPointGeometry());
        menuList.add(new AddLinearReferencing());

        addMouseListener(new DefaultPopupMenuListener(popupMenu) {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    super.mouseClicked(e); // To change body of generated methods, choose Tools | Templates.
                }
            });

        final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {

                @Override
                public Component getTreeCellRendererComponent(final JTree tree,
                        final Object value,
                        final boolean selected,
                        final boolean expanded,
                        final boolean leaf,
                        final int row,
                        final boolean hasFocus) {
                    final Component c = super.getTreeCellRendererComponent(
                            tree,
                            value,
                            selected,
                            expanded,
                            leaf,
                            row,
                            hasFocus);

                    if (c instanceof JLabel) {
                        if ((value instanceof DBEntry) && !(value instanceof DBFolder)) {
                            final Icon serviceIcon = H2FeatureService.getLayerIcon(
                                    H2FeatureService.LAYER_ENABLED_VISIBLE,
                                    ((DBEntry)value).getName(),
                                    databasePath);
                            ((JLabel)c).setIcon(serviceIcon);
                        } else if (value instanceof DBEntry) {
                            if (expanded) {
                                ((JLabel)c).setIcon(getOpenIcon());
                            } else {
                                ((JLabel)c).setIcon(getClosedIcon());
                            }
                        } else if (value.equals(getModel().getRoot())) {
                            if (expanded) {
                                ((JLabel)c).setIcon(getOpenIcon());
                            } else {
                                ((JLabel)c).setIcon(getClosedIcon());
                            }
                        }
                    }

                    return c;
                }
            };
        setCellRenderer(renderer);
        setCellEditor(new DefaultTreeCellEditor(this, renderer) {

                @Override
                protected void determineOffset(final JTree tree,
                        final Object value,
                        final boolean isSelected,
                        final boolean expanded,
                        final boolean leaf,
                        final int row) {
                    if (renderer != null) {
                        if ((value instanceof DBEntry) && !(value instanceof DBFolder)) {
                            editingIcon = H2FeatureService.getLayerIcon(
                                    H2FeatureService.LAYER_ENABLED_VISIBLE,
                                    ((DBEntry)value).getName(),
                                    databasePath);
//                            editingIcon = shapeIcon;
                        } else if (value instanceof DBEntry) {
                            if (expanded) {
                                editingIcon = renderer.getOpenIcon();
                            } else {
                                editingIcon = renderer.getClosedIcon();
                            }
                        } else if (value.equals(getModel().getRoot())) {
                            if (expanded) {
                                editingIcon = renderer.getOpenIcon();
                            } else {
                                editingIcon = renderer.getClosedIcon();
                            }
                        }

                        if (editingIcon != null) {
                            offset = renderer.getIconTextGap()
                                        + editingIcon.getIconWidth();
                        } else {
                            offset = renderer.getIconTextGap();
                        }
                    } else {
                        editingIcon = null;
                        offset = 0;
                    }
                }

                @Override
                protected boolean canEditImmediately(final EventObject event) {
                    return true;
                }
            });
//        transferHandler = new TreeTransferHandler();
//        tree.setTransferHandler(transferHandler);
        setModel(new InternalDBTreeModel());
        createPopupMenu();
        addTreeSelectionListener(new TreeSelectionListener() {

                @Override
                public void valueChanged(final TreeSelectionEvent e) {
                    createPopupMenu();
                }
            });

        popupMenu.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                    synchronized (popupMenu.getTreeLock()) {
                        for (int i = 0; i < popupMenu.getComponentCount(); ++i) {
                            final TreePath[] paths = getSelectionPaths();
                            final Object component = popupMenu.getComponent(i);

                            if (component instanceof InternalDbMenuItem) {
                                final InternalDbMenuItem menuItem = (InternalDbMenuItem)component;
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
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        // without this modification, a NPE will be thrown when a drop operation on a TransferHandler occurs.
        if ((newValue != null) || !propertyName.equals("dropLocation")) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void createPopupMenu() {
        final TreePath[] paths = getSelectionPaths();
        popupMenu.removeAll();

        for (final InternalDbMenuItem item : menuList) {
            if (item.isVisible(paths)) {
                popupMenu.add(item);
            }
        }
    }

    /**
     * refreshs the tree model.
     */
    public void refresh() {
        setModel(new InternalDBTreeModel());
    }

    /**
     * Removes the given DbFolder from the data base and the tree model.
     *
     * @param  folder  DOCUMENT ME!
     */
    public void removeFolder(final DBFolder folder) {
        final List<DBEntry> copy = new ArrayList<DBEntry>(folder.getChildren());
        for (final DBEntry entry : copy) {
            removeEntry(entry);
        }

        final InternalDBTreeModel model = (InternalDBTreeModel)getModel();
        Statement st = null;

        try {
            st = model.getConnection().createStatement();
            st.execute("delete from \"" + H2FeatureServiceFactory.SORT_TABLE_NAME + "\" where table = '"
                        + folder.getName() + "'");
            st.execute("delete from \"" + H2FeatureServiceFactory.SORT_TABLE_NAME + "\" where left(folder, "
                        + folder.getName().length() + ") = '" + folder.getName() + "'");
        } catch (Exception e) {
            LOG.error("Error while removing folder", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Error while removing folder", ex);
                }
            }
        }

        model.removeFolder(folder);
    }

    /**
     * Removes the given DBEntry from the data base and the tree model. If the given entry has the type DbFolder, the
     * method {@link #removeFolder(de.cismet.cismap.commons.internaldb.DbFolder) } will be invoked.
     *
     * @param  entry  the entry to remove
     */
    public void removeEntry(final DBEntry entry) {
        try {
            if (entry instanceof DBFolder) {
                removeFolder(((DBFolder)entry));
                ((InternalDBTreeModel)getModel()).fireTreeStructureChanged();
                return;
            }
            final InternalDBTreeModel model = (InternalDBTreeModel)getModel();
            final Connection con = model.getConnection();
            Statement st = null;
            H2FeatureService.removeTableIfExists(entry.getName());

            try {
                st = con.createStatement();
                st.execute("delete from \"" + H2FeatureServiceFactory.SORT_TABLE_NAME
                            + "\" where table = '"
                            + entry.getName() + "'");
            } catch (Exception e) {
                LOG.error("Error while removing folder", e);
            } finally {
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while removing folder", ex);
                    }
                }
            }
            model.remove(entry.getName());

            removeEntryFromActiveLayerModel(entry);
            ((InternalDBTreeModel)getModel()).fireTreeStructureChanged();
        } catch (Exception e) {
            LOG.error("Cannot remove entry", e);
        }
    }

    /**
     * Removes the given entry from the MappingModel.
     *
     * @param  entry  the entry to remove
     */
    private void removeEntryFromActiveLayerModel(final DBEntry entry) {
        final MappingModel model = CismapBroker.getInstance().getMappingComponent().getMappingModel();
        final TreeMap<Integer, MapService> map = model.getRasterServices();

        if (map != null) {
            for (final MapService service : map.values()) {
                if (service instanceof H2FeatureService) {
                    final H2FeatureService h2Service = (H2FeatureService)service;
                    if (h2Service.getTableName().equals(entry.getName())) {
                        model.removeLayer(h2Service);
                        AttributeTableFactory.getInstance().closeAttributeTable(h2Service);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void saveExpandedPaths() {
        final InternalDBTreeModel model = (InternalDBTreeModel)getModel();
        expendedPaths.clear();
        final TreePath root = new TreePath(new Object[] { model.getRoot() });
        final Enumeration<TreePath> en = getExpandedDescendants(root);

        while (en.hasMoreElements()) {
            expendedPaths.add(en.nextElement());
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void resetExpansion() {
        final InternalDBTreeModel model = (InternalDBTreeModel)getModel();
        final List<TreePath> pathCopy = new ArrayList<TreePath>(expendedPaths);

        if (pathCopy.isEmpty()) {
            // root should always be expanded
            final TreePath root = new TreePath(new Object[] { model.getRoot() });
            pathCopy.add(root);
        }

        for (final TreePath tp : pathCopy) {
            expandPath(tp);
        }
//        try {
//            EventQueue.invokeAndWait(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        final List<TreePath> pathCopy = new ArrayList<TreePath>(expendedPaths);
//
//                        if (pathCopy.isEmpty()) {
//                            // root should always be expanded
//                            final TreePath root = new TreePath(new Object[] { model.getRoot() });
//                            pathCopy.add(root);
//                        }
//
//                        for (final TreePath tp : pathCopy) {
//                            expandPath(tp);
//                        }
//                    }
//                });
//        } catch (Exception ex) {
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void addFolder(final String name) {
        final TreePath selectionPath = getSelectionPath();

        if ((selectionPath != null) && (selectionPath.getLastPathComponent() instanceof DBFolder)) {
            final DBFolder folder = (DBFolder)selectionPath.getLastPathComponent();
            DBFolder newFolder = new DBFolder(folder.getName() + "->" + name);
            int count = 0;

            while (folder.contains(newFolder)) {
                newFolder = new DBFolder(folder.getName() + "->" + name + "_" + (++count));
            }

            folder.addChildren(newFolder);
            Statement st = null;

            try {
                st = ((InternalDBTreeModel)getModel()).getConnection().createStatement();
                st.execute("insert into \"" + H2FeatureServiceFactory.SORT_TABLE_NAME
                            + "\" (folder, table, position) VALUES ('"
                            + folder.getName() + "', '" + folder.getName() + "->" + newFolder
                            + "', (select max(position) + 1 from \"" + H2FeatureServiceFactory.SORT_TABLE_NAME
                            + "\"))");
            } catch (Exception e) {
                LOG.error("Error while removing folder", e);
            } finally {
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while removing folder", ex);
                    }
                }
            }
            ((InternalDBTreeModel)getModel()).fireTreeStructureChanged();
        } else {
            ((InternalDBTreeModel)getModel()).addFolder(name);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ergs  DOCUMENT ME!
     */
    public static void main(final String[] ergs) {
        final String a = "asd->sada->dfds";

        a.split("->");
    }

    /**
     * Provides the table information of the selected paths.
     *
     * @return  the DBTableInformation of the selected paths
     */
    public DBTableInformation[] getDBTableInformationOfSelectionPath() {
        final List<DBEntry> entries = new ArrayList<DBEntry>();

        final TreePath[] paths = getSelectionPaths();

        for (final TreePath path : paths) {
            final Object o = path.getLastPathComponent();

            if (o instanceof DBFolder) {
                entries.add((DBFolder)o);
//                addEntriesToList(entries, (DBFolder)o);
            } else if (o instanceof DBEntry) {
                entries.add((DBEntry)o);
            }
        }

        final DBTableInformation[] databaseTables = new DBTableInformation[entries.size()];

        for (int i = 0; i < entries.size(); ++i) {
            final DBEntry e = entries.get(i);

            if (e instanceof DBFolder) {
                databaseTables[i] = getFolderInformation((DBFolder)e);
            } else {
                databaseTables[i] = new DBTableInformation(e.toString(),
                        databasePath,
                        e.getName(),
                        (e instanceof DBFolder));
            }
        }

        return databaseTables;
    }

    /**
     * Create a DBTableInformation object of the given folder.
     *
     * @param   folder  the folder to convert to a DBTableInformation object
     *
     * @return  the created DBTableInformation object
     */
    private DBTableInformation getFolderInformation(final DBFolder folder) {
        final DBTableInformation databaseTable = new DBTableInformation(folder.toString(),
                databasePath,
                folder.getName(),
                true);

        for (int n = folder.getChildren().size() - 1; n >= 0; --n) {
            final DBEntry entry = folder.getChildren().get(n);

            if (entry instanceof DBFolder) {
                databaseTable.addChild(getFolderInformation((DBFolder)entry));
            } else {
                final DBTableInformation tmp = new DBTableInformation(entry.toString(),
                        databasePath,
                        entry.getName(),
                        false);
                databaseTable.addChild(tmp);
            }
        }

        return databaseTable;
    }

    /**
     * Add all DBEntries of the given folder to the list.
     *
     * @param  entries  the list, the entries should be added in
     * @param  folder   the DBFolder
     */
    private void addEntriesToList(final List<DBEntry> entries, final DBFolder folder) {
        for (final DBEntry entry : folder.getChildren()) {
            if (entry instanceof DBFolder) {
                addEntriesToList(entries, (DBFolder)entry);
            } else {
                entries.add(entry);
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DBTransferHandler extends TransferHandler {

        //~ Instance fields ----------------------------------------------------

        private DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType,
                "SelectionAndCapabilities"); // NOI18N

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DBTransferHandler object.
         */
        public DBTransferHandler() {
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Transferable createTransferable(final JComponent c) {
            final InternalDbTree tree = (InternalDbTree)c;
            final DBTableInformation[] databaseTables = tree.getDBTableInformationOfSelectionPath();

            return new DBTransferable(databaseTables);
        }

        @Override
        public boolean canImport(final TransferHandler.TransferSupport info) {
            final JTree t = (JTree)info.getComponent();
//            boolean b = info.isDrop() && info.isDataFlavorSupported(rowFlavor);

            // Do not allow a drop on the drag source selections
            final JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
            final JTree tree = (JTree)info.getComponent();
            final int dropRow = tree.getRowForPath(dl.getPath());
            final int[] selRows = tree.getSelectionRows();
            for (int i = 0; i < selRows.length; i++) {
                if (selRows[i] == dropRow) {
                    return false;
                }

                if (selRows[i] == 0) {
                    return false;
                }
            }

            // Do not allow a drop on a layer that is not a collection
            final Object targetNode = dl.getPath().getLastPathComponent();

            if (!(targetNode instanceof DBFolder) && !targetNode.equals(t.getModel().getRoot())) {
                return false;
            }

//            t.setCursor( b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            final TreePath p = t.getPathForLocation(
                    info.getDropLocation().getDropPoint().x,
                    info.getDropLocation().getDropPoint().y);
            return ((p != null));
//                            && ((p.getLastPathComponent() instanceof DBFolder)
//                                || p.getLastPathComponent().equals(t.getModel().getRoot())));
//            return b;
        }

        @Override
        public int getSourceActions(final JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(final TransferHandler.TransferSupport info) {
            info.setShowDropLocation(true);
            final JTree target = (JTree)info.getComponent();
            final TreePath p = target.getPathForLocation(
                    info.getDropLocation().getDropPoint().x,
                    info.getDropLocation().getDropPoint().y);
            final JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
            final InternalDBTreeModel model = (InternalDBTreeModel)target.getModel();
            final Connection con = model.getConnection();
            Statement st = null;

            try {
                st = con.createStatement();
                final Object targetFolder = dl.getPath().getLastPathComponent();
                int index = ((dl.getChildIndex() != -1) ? dl.getChildIndex() : 0);

                if ((targetFolder instanceof DBFolder) || targetFolder.equals(model.getRoot())) {
                    DBFolder folder = null;
                    final DBTableInformation[] o = (DBTableInformation[])info.getTransferable()
                                .getTransferData(TREEPATH_FLAVOR);
                    for (final DBTableInformation ti : o) {
                        DBEntry entry;
                        String newName = null;
                        if (targetFolder instanceof DBFolder) {
                            folder = (DBFolder)targetFolder;
                            newName = folder.getName() + "->" + getNameWithoutFolder(ti.getDatabaseTable());
                        } else if (targetFolder.equals(model.getRoot())) {
                            newName = getNameWithoutFolder(ti.getDatabaseTable());
                        }
                        final String targetFolderString = targetFolder.equals(model.getRoot())
                            ? "/" : ((DBFolder)targetFolder).getName();

                        if (ti.isFolder()) {
                            entry = getEntryFromTableInformation(ti);
                            entry.setName(newName);

                            if (!ti.getParentFolder().equals(targetFolderString)) {
                                final List<DBEntry> entries = getDBEntriesFromFolder((DBFolder)entry);

                                for (final DBEntry e : entries) {
                                    final String newTableName = newName + "->"
                                                + e.getName().substring(ti.getDatabaseTable().length() + 2);
                                    final ResultSet rs = con.getMetaData().getTables(null, null, newTableName, null);
                                    if (rs.next()) {
                                        JOptionPane.showMessageDialog(
                                            InternalDbTree.this,
                                            "Tabelle existiert bereits",
                                            "titel",
                                            JOptionPane.WARNING_MESSAGE);
                                        rs.close();
                                        return false;
                                    }
                                    rs.close();
                                }

                                for (final DBEntry e : entries) {
                                    final String oldName = e.getName();
                                    final String newTableName = newName + "->"
                                                + e.getName().substring(ti.getDatabaseTable().length() + 2);
                                    st.execute("alter table \"" + oldName + "\" rename to \"" + newTableName
                                                + "\"");
                                    e.setName(newTableName);
                                }
                            }
                        } else {
                            entry = new DBEntry(newName);
                            if (!ti.getDatabaseTable().equals(newName)) {
                                st.execute("alter table \"" + ti.getDatabaseTable() + "\" rename to \"" + newName
                                            + "\"");
                            }
                        }
                        saveExpandedPaths();
                        final int removeFromPosition = model.remove(ti.getDatabaseTable());

                        if (ti.getParentFolder().equals(targetFolderString)) {
                            if ((removeFromPosition != -1) && (removeFromPosition <= index)) {
                                --index;
                            }
                        }
                        if (targetFolder instanceof DBFolder) {
                            folder.addChildren(entry, index);
                        } else if (targetFolder.equals(model.getRoot())) {
                            model.addEntry(entry, index);
                        }

                        st.execute("delete from \"" + H2FeatureServiceFactory.SORT_TABLE_NAME + "\"");
                        model.saveTableOrder(model.getEntries());

                        // notify the layer, if it is active
                        if (!ti.isFolder()) {
                            final MappingModel mm = CismapBroker.getInstance().getMappingComponent().getMappingModel();
                            if (mm instanceof ActiveLayerModel) {
                                final ActiveLayerModel activeModel = (ActiveLayerModel)mm;
                                final TreeMap<Integer, MapService> map = activeModel.getMapServices();

                                for (final Integer key : map.keySet()) {
                                    final MapService service = map.get(key);

                                    if (service instanceof JDBCFeatureService) {
                                        final JDBCFeatureService jdbcs = (JDBCFeatureService)service;
                                        if (jdbcs.getTableName().equals(ti.getDatabaseTable())) {
                                            jdbcs.setTableName(newName);
                                        }
                                    }
                                }
                            }
                        }

                        model.loadFromDb();
                        model.fireTreeStructureChanged();
                        resetExpansion();
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                LOG.error("Error during drop operation.", e);
            } finally {
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException ex) {
                        LOG.error("cannot close statement", ex);
                    }
                }
            }

            return true;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   folder  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private List<DBEntry> getDBEntriesFromFolder(final DBFolder folder) {
            final List<DBEntry> list = new ArrayList<DBEntry>();

            for (final DBEntry e : folder.getChildren()) {
                if (e instanceof DBFolder) {
                    list.addAll(getDBEntriesFromFolder((DBFolder)e));
                } else {
                    list.add(e);
                }
            }

            return list;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   ti  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private DBEntry getEntryFromTableInformation(final DBTableInformation ti) {
            if (ti.isFolder()) {
                final DBFolder folder = new DBFolder(ti.getDatabaseTable());

                for (final DBTableInformation inf : ti.getChildren()) {
                    folder.addChildren(getEntryFromTableInformation(inf));
                }

                return folder;
            } else {
                return new DBEntry(ti.getDatabaseTable());
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   name  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getNameWithoutFolder(final String name) {
            int start = 0;

            if (name.indexOf("->") != -1) {
                start = name.lastIndexOf("->") + 2;
            }

            return name.substring(start);
        }

        @Override
        protected void exportDone(final JComponent c, final Transferable t, final int act) {
//            c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class InternalDBTreeModel implements TreeModel, StringFilter {

        //~ Instance fields ----------------------------------------------------

        private ConnectionWrapper conn;
        private List<DBEntry> entries = new ArrayList<DBEntry>();
        private String root = "Intern";
        private List<TreeModelListener> listener = new ArrayList<TreeModelListener>();
        private String filterString;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new InternalDBTreeModel object.
         */
        public InternalDBTreeModel() {
            conn = H2FeatureServiceFactory.getDBConnection(databasePath);
            loadFromDb();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        public void loadFromDb() {
            try {
                final ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });
                entries.clear();

                while (rs.next()) {
                    final String name = rs.getString("TABLE_NAME");

                    if (name.equalsIgnoreCase("spatial_ref_sys")
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.LR_META_TABLE_NAME)
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.META_TABLE_NAME)
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.META_TABLE_ATTRIBUTES_NAME)
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.SORT_TABLE_NAME)
                                || name.equalsIgnoreCase("Zeichnungen")
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.SLD_TABLE_NAME)
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.LOCK_TABLE_NAME)) {
                        continue;
                    }

                    final String[] parts = name.split("->");
                    List<DBEntry> parent = entries;
                    String parentFolder = "";

                    for (int i = 0; i < parts.length; ++i) {
                        final String part = parts[i];

                        if (i == (parts.length - 1)) {
                            parent.add(new DBEntry(name));
                        } else {
                            String newFolderName;
                            if (!parentFolder.equals("")) {
                                newFolderName = parentFolder + "->" + part;
                            } else {
                                newFolderName = part;
                            }
                            DBFolder folder = new DBFolder(newFolderName);
                            final int folderIndex = parent.indexOf(folder);

                            if (folderIndex == -1) {
                                parent.add(folder);
                            } else {
                                folder = (DBFolder)parent.get(folderIndex);
                            }

                            if (!parentFolder.equals("")) {
                                parentFolder += "->";
                            }
                            parentFolder += part;

                            parent = folder.getChildren();
                        }
                    }
                }
                addFolder();
                sortTables(entries);
            } catch (Exception e) {
                LOG.error("Error while retrieving meta infos from the db" + databasePath, e);
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void addFolder() {
            final Map<Integer, DBFolder> allEntries = getAllEntriesInFolder("/");

            for (final Integer key : allEntries.keySet()) {
                final DBFolder f = allEntries.get(key);

                if (!entries.contains(f) && !entries.contains(new DBEntry(f.getName()))) {
                    if (key > entries.size()) {
                        entries.add(entries.size(), f);
                    } else {
                        entries.add((key > entries.size()) ? entries.size() : key, f);
                    }
                }
            }

            for (final DBEntry entry : entries) {
                if (entry instanceof DBFolder) {
                    addFolder((DBFolder)entry);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  folder  DOCUMENT ME!
         */
        private void addFolder(final DBFolder folder) {
            final Map<Integer, DBFolder> allEntries = getAllEntriesInFolder(folder.getName());

            for (final Integer key : allEntries.keySet()) {
                final DBFolder f = allEntries.get(key);

                if (!folder.contains(f) && !folder.contains(new DBEntry(f.getName()))) {
                    folder.addChildren(f, key);
                }
            }

            for (final DBEntry entry : folder.getChildren()) {
                if (entry instanceof DBFolder) {
                    addFolder((DBFolder)entry);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   parent  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Map<Integer, DBFolder> getAllEntriesInFolder(final String parent) {
            final Map<Integer, DBFolder> resultMap = new HashMap<Integer, DBFolder>();

            try {
                final Statement st = conn.createStatement();

                final ResultSet rs = st.executeQuery("select table, position from \""
                                + H2FeatureServiceFactory.SORT_TABLE_NAME + "\" where folder = '" + parent + "'");

                while (rs.next()) {
                    final String table = rs.getString(1);
                    if (parent.equals("/")) {
                        if (table.indexOf("->") == -1) {
                            resultMap.put(rs.getInt(2) - 1, new DBFolder(table));
                        }
                    } else {
                        if (table.length() > (parent.length() + 2)) {
                            if (table.substring(parent.length() + 2).indexOf("->") == -1) {
                                resultMap.put(rs.getInt(2) - 1, new DBFolder(table));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while reading folder", e);
            }

            return resultMap;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  parent  DOCUMENT ME!
         */
        public void sortTables(final List<DBEntry> parent) {
            DBEntryComparator comparator = null;
            try {
                comparator = new DBEntryComparator(conn, "/");
                Collections.sort(parent, comparator);

                for (final DBEntry entry : parent) {
                    if (entry instanceof DBFolder) {
                        sortFolder((DBFolder)entry);
                    }
                }
            } catch (Exception e) {
                LOG.error("Cannot sort folder", e);
            } finally {
                if (comparator != null) {
                    comparator.cleanup();
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  parent  DOCUMENT ME!
         */
        public void saveTableOrder(final List<DBEntry> parent) {
            DBEntryComparator comparator = null;
            try {
                comparator = new DBEntryComparator(conn, "/");

                for (final DBEntry entry : parent) {
                    comparator.getPosition(entry);
                }

                for (final DBEntry entry : parent) {
                    if (entry instanceof DBFolder) {
                        saveFolderOrder((DBFolder)entry);
                    }
                }
            } catch (Exception e) {
                LOG.error("Cannot sort folder", e);
            } finally {
                if (comparator != null) {
                    comparator.cleanup();
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   folder  DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        private void saveFolderOrder(final DBFolder folder) throws Exception {
            DBEntryComparator comparator = null;
            try {
                comparator = new DBEntryComparator(conn, folder.getName());
                for (final DBEntry entry : folder.getChildren()) {
                    comparator.getPosition(entry);
                }

                for (final DBEntry entry : folder.getChildren()) {
                    if (entry instanceof DBFolder) {
                        saveFolderOrder((DBFolder)entry);
                    }
                }
            } finally {
                if (comparator != null) {
                    comparator.cleanup();
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   folder  DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        private void sortFolder(final DBFolder folder) throws Exception {
            DBEntryComparator comparator = null;
            try {
                comparator = new DBEntryComparator(conn, folder.getName());
                Collections.sort(folder.getChildren(), comparator);

                for (final DBEntry entry : folder.getChildren()) {
                    if (entry instanceof DBFolder) {
                        sortFolder((DBFolder)entry);
                    }
                }
            } finally {
                if (comparator != null) {
                    comparator.cleanup();
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<DBEntry> getEntries() {
            return entries;
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(final Object parent, final int index) {
            if (parent == root) {
                return filterChildren(entries).get(index);
            } else if (parent instanceof DBFolder) {
                return filterChildren(((DBFolder)parent).getChildren()).get(index);
            } else {
                return null;
            }
        }

        @Override
        public int getChildCount(final Object parent) {
            if (parent == root) {
                return filterChildren(entries).size();
            } else if (parent instanceof DBFolder) {
                return filterChildren(((DBFolder)parent).getChildren()).size();
            } else {
                return 0;
            }
        }

        /**
         * Creates a new list with all elements of the given list, which matchs the filter string.
         *
         * @param   entryList  parent the folder, its children should be determined
         *
         * @return  a list with all children, which considers the filter string
         */
        private List<DBEntry> filterChildren(final List<DBEntry> entryList) {
            final List<DBEntry> entries = new ArrayList<DBEntry>();

            for (final DBEntry entry : entryList) {
                if (fulfilFilterRequirements(entry)) {
                    entries.add(entry);
                }
            }

            return entries;
        }

        /**
         * Checks, if the given entry fulfils the filter requirement.
         *
         * @param   entry  the entry to check
         *
         * @return  true, iff the filter requirement is fulfilled
         */
        private boolean fulfilFilterRequirements(final DBEntry entry) {
            if (entry instanceof DBFolder) {
                if ((filterString == null)
                            || entry.getNameWithoutFolder().toLowerCase().contains(filterString.toLowerCase())) {
                    return true;
                } else {
                    for (final DBEntry e : ((DBFolder)entry).getChildren()) {
                        if (fulfilFilterRequirements(e)) {
                            return true;
                        }
                    }
                }
            } else {
                return (((filterString == null)
                                    || entry.getNameWithoutFolder().toLowerCase().contains(filterString.toLowerCase())));
            }

            return false;
        }

        @Override
        public boolean isLeaf(final Object node) {
            return getChildCount(node) == 0;
        }

        @Override
        public void valueForPathChanged(final TreePath path, final Object newValue) {
            try {
                if ((newValue != null) && !newValue.equals("")) {
                    final DBEntry entry = (DBEntry)path.getLastPathComponent();
                    final Statement st = conn.createStatement();

                    if (entry instanceof DBFolder) {
                        String folderName = newValue.toString();
                        if ((entry.getFolderName() != null) && !entry.getFolderName().equals("")) {
                            folderName = entry.getFolderName() + "->" + folderName;
                        }
                        renameFolder((DBFolder)entry, folderName);
                    } else {
                        String newName = newValue.toString() + "_" + entry.getHash();
                        if ((entry.getFolderName() != null) && !entry.getFolderName().equals("")) {
                            newName = entry.getFolderName() + "->" + newName;
                        }
                        st.execute("alter table \"" + entry.getName() + "\" rename to \"" + newName + "\"");
                        renameExistingService(entry.getName(), newName);
                        entry.setName(newName);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error during rename operation.", e);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  folder   DOCUMENT ME!
         * @param  newName  DOCUMENT ME!
         */
        private void renameFolder(final DBFolder folder, final String newName) {
            try {
                final Statement st = conn.createStatement();

                for (final DBEntry e : folder.getChildren()) {
                    if (e instanceof DBFolder) {
                        renameFolder((DBFolder)e, newName + "->" + e.getNameWithoutFolder());
                    } else {
                        final String name = newName + "->" + e.getNameWithoutFolder();
                        st.execute("alter table \"" + e.getName() + "\" rename to \"" + name + "\"");
                        renameExistingService(e.getName(), name);
                        e.setName(name);
                    }
                }
                folder.setName(newName);
            } catch (Exception e) {
                LOG.error("Error during rename operation.", e);
            }
        }

        /**
         * Rename the currently used layer. Without renaming, the layer cannot be used, caused by a wrong table
         * reference.
         *
         * @param  oldName  DOCUMENT ME!
         * @param  newName  DOCUMENT ME!
         */
        private void renameExistingService(final String oldName, final String newName) {
            final ActiveLayerModel mappingModel = (ActiveLayerModel)CismapBroker.getInstance().getMappingComponent()
                        .getMappingModel();

            final TreeMap treeMap = mappingModel.getMapServices();
            final List<Integer> keyList = new ArrayList<Integer>(treeMap.keySet());
            final Iterator it = keyList.iterator();

            while (it.hasNext()) {
                final Object service = treeMap.get(it.next());

                if (service instanceof H2FeatureService) {
                    final H2FeatureService featureService = (H2FeatureService)service;

                    if ((featureService.getTableName() != null) && featureService.getTableName().equals(oldName)) {
                        featureService.setTableName(newName);
                    }
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        public void addEntry(final DBEntry e) {
            entries.add(e);
            fireTreeStructureChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e      DOCUMENT ME!
         * @param  index  DOCUMENT ME!
         */
        public void addEntry(final DBEntry e, final int index) {
            entries.add(index, e);
            fireTreeStructureChanged();
        }

        @Override
        public int getIndexOfChild(final Object parent, final Object child) {
            if (parent == root) {
                return filterChildren(entries).indexOf(child);
            } else if (parent instanceof DBFolder) {
                return filterChildren(((DBFolder)parent).getChildren()).indexOf(child);
            } else {
                return 0;
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void fireTreeStructureChanged() {
            saveExpandedPaths();
            for (final TreeModelListener l : listener) {
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] { root }));
            }
            resetExpansion();
        }

        @Override
        public void addTreeModelListener(final TreeModelListener l) {
            listener.add(l);
        }

        @Override
        public void removeTreeModelListener(final TreeModelListener l) {
            listener.remove(l);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  name  DOCUMENT ME!
         */
        public void addFolder(final String name) {
            DBFolder folder = new DBFolder(name);
            int count = 0;

            while (entries.contains(folder)) {
                folder = new DBFolder(name + "_" + (++count));
            }

            entries.add(folder);
            Statement st = null;

            try {
                st = ((InternalDBTreeModel)getModel()).getConnection().createStatement();
                st.execute("insert into \"" + H2FeatureServiceFactory.SORT_TABLE_NAME
                            + "\" (folder, table, position) VALUES ('/', '"
                            + folder.getName() + "', (select max(position) + 1 from \""
                            + H2FeatureServiceFactory.SORT_TABLE_NAME + "\"))");
            } catch (Exception e) {
                LOG.error("Error while removing folder", e);
            } finally {
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while removing folder", ex);
                    }
                }
            }
            fireTreeStructureChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param   tableName  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int remove(final String tableName) {
            final String[] parts = tableName.split("->");
            List<DBEntry> parent = entries;
            DBFolder folder = null;

            for (int i = 0; i < parts.length; ++i) {
                final String part = parts[i];

                if (i == (parts.length - 1)) {
                    int position = parent.indexOf(new DBEntry(tableName));

                    if (!parent.remove(new DBEntry(tableName))) {
                        position = parent.indexOf(new DBFolder(tableName));
                        parent.remove(new DBFolder(tableName));
                    }

                    return position;
                } else {
                    folder = ((folder == null) ? new DBFolder(part) : new DBFolder(folder.getName() + "->" + part));
                    final int folderIndex = parent.indexOf(folder);

                    if (folderIndex == -1) {
                        break;
                    } else {
                        folder = (DBFolder)parent.get(folderIndex);
                    }

                    parent = folder.getChildren();
                }
            }

            fireTreeStructureChanged();

            return -1;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  folderToRemove  tableName DOCUMENT ME!
         */
        public void removeFolder(final DBFolder folderToRemove) {
            for (final DBEntry e : folderToRemove.getChildren()) {
                if (e instanceof DBFolder) {
                    removeFolder((DBFolder)e);
                } else {
                    remove(e.getName());
                }
            }

            final String[] parts = folderToRemove.getName().split("->");
            List<DBEntry> parent = entries;
            DBFolder folder = null;

            for (int i = 0; i < parts.length; ++i) {
                final String part = parts[i];

                if (i == (parts.length - 1)) {
                    parent.remove(folderToRemove);
                } else {
                    folder = ((folder == null) ? new DBFolder(part) : new DBFolder(folder.getName() + "->" + part));
                    final int folderIndex = parent.indexOf(folder);

                    if (folderIndex == -1) {
                        break;
                    } else {
                        folder = (DBFolder)parent.get(folderIndex);
                    }

                    parent = folder.getChildren();
                }
            }

            fireTreeStructureChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Connection getConnection() {
            return conn;
        }

        @Override
        public void setFilterString(final String filterString) {
            this.filterString = filterString;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AddFolderItem extends InternalDbMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AddFolderItem object.
         */
        public AddFolderItem() {
            super(NbBundle.getMessage(AddFolderItem.class, "InternalDbTree.AddFolderItem.text"));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            InternalDbTree.this.addFolder(
                NbBundle.getMessage(
                    AddFolderItem.class,
                    "InternalDbTree.AddFolderItem.addFolder"));
        }

        @Override
        public boolean isVisible(final TreePath[] path) {
            if ((path != null) && (path.length == 1)) {
                return (path[0].getLastPathComponent() instanceof DBFolder)
                            || path[0].getLastPathComponent().equals(InternalDbTree.this.getModel().getRoot());
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class RemoveItem extends InternalDbMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveItem object.
         */
        public RemoveItem() {
            super(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.RemoveItem.refreshText().both"));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = InternalDbTree.this.getSelectionPaths();

            for (final TreePath tp : tps) {
                final DBEntry entry = (DBEntry)tp.getLastPathComponent();
                if (tp.getLastPathComponent() instanceof DBEntry) {
                    InternalDbTree.this.removeEntry(entry);
                }
            }
        }

        @Override
        public boolean isVisible(final TreePath[] paths) {
            if (paths != null) {
                boolean visible = true;

                for (final TreePath path : paths) {
                    if (path.getLastPathComponent().equals(InternalDbTree.this.getModel().getRoot())) {
                        visible = false;
                        break;
                    }
                }

                return visible;
            } else {
                return false;
            }
        }

        @Override
        public void refreshText(final TreePath[] paths) {
            if (paths != null) {
                boolean folder = false;
                boolean theme = false;

                for (final TreePath path : paths) {
                    if (path.getLastPathComponent() instanceof DBFolder) {
                        folder = true;
                    } else if (path.getLastPathComponent() instanceof DBEntry) {
                        theme = true;
                    }
                }

                if (folder && theme) {
                    setText(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.RemoveItem.refreshText().both"));
                } else if (folder) {
                    setText(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.RemoveItem.refreshText().folder"));
                } else if (theme) {
                    setText(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.RemoveItem.refreshText().theme"));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ZoomTo extends InternalDbMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ZoomTo object.
         */
        public ZoomTo() {
            super(NbBundle.getMessage(ZoomTo.class, "InternalDbTree.ZoomTo.refreshText().both"));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            zoomToExtent();
        }

        /**
         * Zooms the map to the extent of the selected elements of the given InternalDbTree,
         */
        private void zoomToExtent() {
            final DBTableInformation[] infos = InternalDbTree.this.getDBTableInformationOfSelectionPath();
            Geometry geom = null;
            final List<DBTableInformation> infoList = new ArrayList<DBTableInformation>();

            for (final DBTableInformation info : infos) {
                if (info.isFolder()) {
                    infoList.addAll(getChildren(info));
                } else {
                    infoList.add(info);
                }
            }

            for (final DBTableInformation dbInfo : infoList) {
                try {
                    final H2FeatureService layer = new H2FeatureService(dbInfo.getName(),
                            dbInfo.getDatabasePath(),
                            dbInfo.getDatabaseTable(),
                            null);
                    layer.initAndWait();
                    final Geometry envelope = ((H2FeatureServiceFactory)layer.getFeatureFactory()).getEnvelope();

                    if (envelope != null) {
                        if (geom == null) {
                            geom = envelope;
                        } else {
                            geom = geom.union(envelope);
                        }
                    }
                } catch (Exception ex) {
                    LOG.error("Error while creating H2FeatureService", ex);
                }
            }

            if (geom != null) {
                CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(new XBoundingBox(geom));
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   infoFolder  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private List<DBTableInformation> getChildren(final DBTableInformation infoFolder) {
            final List<DBTableInformation> infoList = new ArrayList<DBTableInformation>();

            if (infoFolder.getChildren() != null) {
                for (final DBTableInformation info : infoFolder.getChildren()) {
                    if (info.isFolder()) {
                        infoList.addAll(getChildren(info));
                    } else {
                        infoList.add(info);
                    }
                }
            }
            return infoList;
        }

        @Override
        public void refreshText(final TreePath[] paths) {
            if (paths != null) {
                boolean folder = false;
                boolean theme = false;

                for (final TreePath path : paths) {
                    if (path.getLastPathComponent() instanceof DBFolder) {
                        folder = true;
                    } else if (path.getLastPathComponent() instanceof DBEntry) {
                        theme = true;
                    } else {
                        folder = true;
                    }
                }

                if (folder && theme) {
                    setText(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.ZoomTo.refreshText().both"));
                } else if (folder) {
                    setText(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.ZoomTo.refreshText().folder"));
                } else if (theme) {
                    setText(NbBundle.getMessage(RemoveItem.class, "InternalDbTree.ZoomTo.refreshText().theme"));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AddLinearReferencing extends InternalDbMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AddLinearReferencing object.
         */
        public AddLinearReferencing() {
            super(NbBundle.getMessage(AddLinearReferencing.class, "InternalDbTree.AddLinearReferencing.text"));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = InternalDbTree.this.getSelectionPaths();

            for (final TreePath tp : tps) {
                if ((tp.getLastPathComponent() instanceof DBEntry)
                            && !(tp.getLastPathComponent() instanceof DBFolder)) {
                    try {
                        final DBEntry entry = (DBEntry)tp.getLastPathComponent();
                        final H2FeatureService service = new H2FeatureService(
                                entry.getNameWithoutFolder(),
                                databasePath,
                                entry.getName(),
                                null);
                        final LinearReferencingDialog dialog = new LinearReferencingDialog(
                                StaticSwingTools.getParentFrame(InternalDbTree.this),
                                true,
                                service);
                        dialog.setSize(645, 260);
                        dialog.pack();
                        StaticSwingTools.showDialog(dialog);
                    } catch (Exception ex) {
                        LOG.error(
                            "Error while creating a H2 service instance.",
                            ex);
                    }
                }
            }
        }

        @Override
        public boolean isVisible(final TreePath[] paths) {
            if (paths != null) {
                boolean visible = true;

                for (final TreePath path : paths) {
                    if ((path.getLastPathComponent() instanceof DBFolder)
                                || path.getLastPathComponent().equals(InternalDbTree.this.getModel().getRoot())) {
                        visible = false;
                        break;
                    }
                }

                return visible;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AddPointGeometry extends InternalDbMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AddPointGeometry object.
         */
        public AddPointGeometry() {
            super(NbBundle.getMessage(AddPointGeometry.class, "InternalDbTree.AddPointGeometry.text"));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = InternalDbTree.this.getSelectionPaths();

            for (final TreePath tp : tps) {
                if ((tp.getLastPathComponent() instanceof DBEntry)
                            && !(tp.getLastPathComponent() instanceof DBFolder)) {
                    try {
                        final DBEntry entry = (DBEntry)tp.getLastPathComponent();
                        final H2FeatureService service = new H2FeatureService(
                                entry.getNameWithoutFolder(),
                                databasePath,
                                entry.getName(),
                                null);
                        final PointReferencingDialog dialog = new PointReferencingDialog(
                                StaticSwingTools.getParentFrame(InternalDbTree.this),
                                true,
                                service);
                        dialog.setSize(645, 260);
                        dialog.pack();
                        StaticSwingTools.showDialog(dialog);
                    } catch (Exception ex) {
                        LOG.error(
                            "Error while creating a H2 service instance.",
                            ex);
                    }
                }
            }
        }

        @Override
        public boolean isVisible(final TreePath[] paths) {
            if (paths != null) {
                boolean visible = true;

                for (final TreePath path : paths) {
                    if ((path.getLastPathComponent() instanceof DBFolder)
                                || path.getLastPathComponent().equals(InternalDbTree.this.getModel().getRoot())) {
                        visible = false;
                        break;
                    }
                }

                return visible;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DBEntryComparator implements Comparator<DBEntry> {

        //~ Instance fields ----------------------------------------------------

        private final PreparedStatement positionStatement;
        private final PreparedStatement maxPosStatement;
        private final PreparedStatement insertStatement;
        private String folder;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DBEntryComparator object.
         *
         * @param   conn    DOCUMENT ME!
         * @param   folder  DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        public DBEntryComparator(final ConnectionWrapper conn, final String folder) throws Exception {
            H2FeatureServiceFactory.createSortMetaTableIfNotExist();
            positionStatement = conn.prepareStatement("select position from \""
                            + H2FeatureServiceFactory.SORT_TABLE_NAME + "\" where folder = ? and table = ?");
            maxPosStatement = conn.prepareStatement("select max(position) from \""
                            + H2FeatureServiceFactory.SORT_TABLE_NAME + "\" where folder = ?");
            insertStatement = conn.prepareStatement("insert into \"" + H2FeatureServiceFactory.SORT_TABLE_NAME
                            + "\" (folder, table, position) VALUES (?, ?, ?)");
            this.folder = folder;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final DBEntry o1, final DBEntry o2) {
            final Integer posO1 = getPosition(o1);
            final Integer posO2 = getPosition(o2);

            return posO1.compareTo(posO2);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   entry  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getPosition(final DBEntry entry) {
            try {
                positionStatement.setString(1, folder);
                positionStatement.setString(2, entry.getName());
                ResultSet rs = positionStatement.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    maxPosStatement.setString(1, folder);
                    rs.close();
                    rs = maxPosStatement.executeQuery();
                    int newPos = 1;

                    if (rs.next()) {
                        newPos = rs.getInt(1) + 1;
                    }
                    rs.close();

                    insertStatement.setString(1, folder);
                    insertStatement.setString(2, entry.getName());
                    insertStatement.setInt(3, newPos);
                    insertStatement.execute();

                    return newPos;
                }
            } catch (Exception e) {
                LOG.error("SQL error: ", e);

                return 0;
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void cleanup() {
            try {
                positionStatement.close();
            } catch (Exception e) {
                LOG.error("Error while closing statement: ", e);
            }
            try {
                maxPosStatement.close();
            } catch (Exception e) {
                LOG.error("Error while closing statement: ", e);
            }
            try {
                insertStatement.close();
            } catch (Exception e) {
                LOG.error("Error while closing statement: ", e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private abstract class InternalDbMenuItem extends JMenuItem implements ActionListener {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new InternalDbMenuItem object.
         *
         * @param  text  DOCUMENT ME!
         */
        public InternalDbMenuItem(final String text) {
            super(text);
            addActionListener(this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   paths  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isVisible(final TreePath[] paths) {
            return (paths != null) && (paths.length > 0);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  paths  DOCUMENT ME!
         */
        public void refreshText(final TreePath[] paths) {
        }
    }
}
