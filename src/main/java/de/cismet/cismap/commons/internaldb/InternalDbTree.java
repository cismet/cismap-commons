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

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.JDBCFeatureService;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.StringFilter;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerMenuItem;
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
        model.removeFolder(folder);

        model.fireTreeStructureChanged();
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
                return;
            }
            final InternalDBTreeModel model = (InternalDBTreeModel)getModel();
            final Connection con = model.getConnection();
            final Statement st = con.createStatement();
            H2FeatureService.removeTableIfExists(entry.getName());

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
     *
     * @param  name  DOCUMENT ME!
     */
    public void addFolder(final String name) {
        final TreePath selectionPath = getSelectionPath();

        if ((selectionPath != null) && (selectionPath.getLastPathComponent() instanceof DBFolder)) {
            final DBFolder folder = (DBFolder)selectionPath.getLastPathComponent();
            DBFolder newFolder = new DBFolder(name);
            int count = 0;

            while (folder.contains(newFolder)) {
                newFolder = new DBFolder(name + "_" + (++count));
            }

            folder.addChildren(newFolder);
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

//            t.setCursor( b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            final TreePath p = t.getPathForLocation(
                    info.getDropLocation().getDropPoint().x,
                    info.getDropLocation().getDropPoint().y);
            return ((p != null)
                            && ((p.getLastPathComponent() instanceof DBFolder)
                                || p.getLastPathComponent().equals(t.getModel().getRoot())));
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

            try {
                final InternalDBTreeModel model = (InternalDBTreeModel)target.getModel();
                final Object targetFolder = p.getLastPathComponent();

                if ((p != null) && ((targetFolder instanceof DBFolder) || targetFolder.equals(model.getRoot()))) {
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
                        if (ti.isFolder()) {
                            entry = new DBFolder(newName);
                        } else {
                            entry = new DBEntry(newName);
                            final Connection con = model.getConnection();
                            final Statement st = con.createStatement();
                            st.execute("alter table \"" + ti.getDatabaseTable() + "\" rename to \"" + newName + "\"");
                            st.close();
                        }
                        model.remove(ti.getDatabaseTable());
                        if (targetFolder instanceof DBFolder) {
                            folder.addChildren(entry);
                        } else if (targetFolder.equals(model.getRoot())) {
                            model.addEntry(entry);
                        }

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
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                LOG.error("Error during drop operation.", e);
            }

            return true;
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
            try {
                conn = H2FeatureServiceFactory.getDBConnection(databasePath);
                final ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });

                while (rs.next()) {
                    final String name = rs.getString("TABLE_NAME");

                    if (name.equalsIgnoreCase("spatial_ref_sys")
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.LR_META_TABLE_NAME)
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.META_TABLE_NAME)
                                || name.equalsIgnoreCase(H2FeatureServiceFactory.META_TABLE_ATTRIBUTES_NAME)
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
            } catch (Exception e) {
                LOG.error("Error while retrieving meta infos from the db" + databasePath, e);
            }
        }

        //~ Methods ------------------------------------------------------------

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
                        e.setName(name);
                    }
                }
                folder.setName(newName);
            } catch (Exception e) {
                LOG.error("Error during rename operation.", e);
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
            for (final TreeModelListener l : listener) {
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] { root }));
            }
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
            fireTreeStructureChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  tableName  DOCUMENT ME!
         */
        public void remove(final String tableName) {
            final String[] parts = tableName.split("->");
            List<DBEntry> parent = entries;
            DBFolder folder = null;

            for (int i = 0; i < parts.length; ++i) {
                final String part = parts[i];

                if (i == (parts.length - 1)) {
                    parent.remove(new DBEntry(tableName));
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

            for (int i = 0; i < parts.length; ++i) {
                final String part = parts[i];

                if (i == (parts.length - 1)) {
                    parent.remove(folderToRemove);
                } else {
                    DBFolder folder = new DBFolder(part);
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
