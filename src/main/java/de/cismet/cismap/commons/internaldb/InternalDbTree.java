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

import org.apache.log4j.Logger;

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.wrapper.ConnectionWrapper;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.featureservice.JDBCFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class InternalDbTree extends JTree {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(InternalDbTree.class);

    //~ Instance fields --------------------------------------------------------

    private Icon shapeIcon = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png"));

    private String databasePath;
//    private String

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
        getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        setCellRenderer(new DefaultTreeCellRenderer() {

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
                        if ((value instanceof DBEntry) && !(value instanceof DbFolder)) {
                            ((JLabel)c).setIcon(shapeIcon);
                        } else if (value instanceof DBEntry) {
                            if (expanded) {
                                ((JLabel)c).setIcon(getOpenIcon());
                            } else {
                                ((JLabel)c).setIcon(getClosedIcon());
                            }
                        }
                    }

                    return c;
                }
            });
//        transferHandler = new TreeTransferHandler();
//        tree.setTransferHandler(transferHandler);
        setModel(new InternalDBTreeModel());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void addFolder(final String name) {
        ((InternalDBTreeModel)getModel()).addFolder(name);
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
            final JTree tree = (JTree)c;
            final List<DBEntry> entries = new ArrayList<DBEntry>();

            final TreePath[] paths = tree.getSelectionPaths();

            for (final TreePath path : paths) {
                final Object o = path.getLastPathComponent();

                if (o instanceof DbFolder) {
                    addEntriesToList(entries, (DbFolder)o);
                } else if (o instanceof DBEntry) {
                    entries.add((DBEntry)o);
                }
            }

            final DBTableInformation[] databaseTables = new DBTableInformation[entries.size()];

            for (int i = 0; i < entries.size(); ++i) {
                final DBEntry e = entries.get(i);
                databaseTables[i] = new DBTableInformation(e.toString(),
                        databasePath,
                        e.getName(),
                        (e instanceof DbFolder));
            }

            return new DBTransferable(databaseTables);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  entries  DOCUMENT ME!
         * @param  folder   DOCUMENT ME!
         */
        private void addEntriesToList(final List<DBEntry> entries, final DbFolder folder) {
            for (final DBEntry entry : folder.getChildren()) {
                if (entry instanceof DbFolder) {
                    addEntriesToList(entries, (DbFolder)entry);
                } else {
                    entries.add(entry);
                }
            }
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
                            && ((p.getLastPathComponent() instanceof DbFolder)
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

                if ((p != null) && ((targetFolder instanceof DbFolder) || targetFolder.equals(model.getRoot()))) {
                    DbFolder folder = null;
                    final DBTableInformation[] o = (DBTableInformation[])info.getTransferable()
                                .getTransferData(TREEPATH_FLAVOR);
                    for (final DBTableInformation ti : o) {
                        DBEntry entry;
                        String newName = null;
                        if (targetFolder instanceof DbFolder) {
                            folder = (DbFolder)targetFolder;
                            newName = folder.getName() + "->" + getNameWithoutFolder(ti.getDatabaseTable());
                        } else if (targetFolder.equals(model.getRoot())) {
                            newName = getNameWithoutFolder(ti.getDatabaseTable());
                        }
                        if (ti.isFolder()) {
                            entry = new DbFolder(newName);
                        } else {
                            entry = new DBEntry(newName);
                            final Connection con = model.getConnection();
                            final Statement st = con.createStatement();
                            st.execute("alter table \"" + ti.getDatabaseTable() + "\" rename to \"" + newName + "\"");
                            st.close();
                        }
                        model.remove(ti.getDatabaseTable());
                        if (targetFolder instanceof DbFolder) {
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
    private class InternalDBTreeModel implements TreeModel {

        //~ Instance fields ----------------------------------------------------

        private ConnectionWrapper conn;
        private List<DBEntry> entries = new ArrayList<DBEntry>();
        private String root = "Intern";
        private List<TreeModelListener> listener = new ArrayList<TreeModelListener>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new InternalDBTreeModel object.
         */
        public InternalDBTreeModel() {
            try {
                conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                            "jdbc:h2:"
                                    + databasePath));
                final ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });

                while (rs.next()) {
                    final String name = rs.getString("TABLE_NAME");

                    if (name.equalsIgnoreCase("spatial_ref_sys")) {
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
                            DbFolder folder = new DbFolder(newFolderName);
                            final int folderIndex = parent.indexOf(folder);

                            if (folderIndex == -1) {
                                parent.add(folder);
                            } else {
                                folder = (DbFolder)parent.get(folderIndex);
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
                return entries.get(index);
            } else if (parent instanceof DbFolder) {
                return ((DbFolder)parent).getChildren().get(index);
            } else {
                return null;
            }
        }

        @Override
        public int getChildCount(final Object parent) {
            if (parent == root) {
                return entries.size();
            } else if (parent instanceof DbFolder) {
                return ((DbFolder)parent).getChildren().size();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isLeaf(final Object node) {
            if (node == root) {
                return entries.isEmpty();
            } else if (node instanceof DbFolder) {
                return ((DbFolder)node).getChildren().isEmpty();
            } else {
                return true;
            }
        }

        @Override
        public void valueForPathChanged(final TreePath path, final Object newValue) {
            try {
                if ((newValue != null) && !newValue.equals("")) {
                    final DBEntry entry = (DBEntry)path.getLastPathComponent();
                    final Statement st = conn.createStatement();

                    if (entry instanceof DbFolder) {
                        String folderName = newValue.toString();
                        if ((entry.getFolderName() != null) && !entry.getFolderName().equals("")) {
                            folderName = entry.getFolderName() + "->" + folderName;
                        }
                        renameFolder((DbFolder)entry, folderName);
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
        private void renameFolder(final DbFolder folder, final String newName) {
            try {
                final Statement st = conn.createStatement();

                for (final DBEntry e : folder.getChildren()) {
                    if (e instanceof DbFolder) {
                        renameFolder((DbFolder)e, newName + "->" + e.getNameWithoutFolder());
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
                return entries.indexOf(child);
            } else if (parent instanceof DbFolder) {
                return ((DbFolder)parent).getChildren().indexOf(child);
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
            DbFolder folder = new DbFolder(name);
            int count = 0;

            while (entries.contains(folder)) {
                folder = new DbFolder(name + "_" + (++count));
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

            for (int i = 0; i < parts.length; ++i) {
                final String part = parts[i];

                if (i == (parts.length - 1)) {
                    parent.remove(new DBEntry(tableName));
                } else {
                    DbFolder folder = new DbFolder(part);
                    final int folderIndex = parent.indexOf(folder);

                    if (folderIndex == -1) {
                        break;
                    } else {
                        folder = (DbFolder)parent.get(folderIndex);
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
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DbFolder extends DBEntry {

        //~ Instance fields ----------------------------------------------------

        private List<DBEntry> children = new ArrayList<DBEntry>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DbFolder object.
         *
         * @param  name  DOCUMENT ME!
         */
        public DbFolder(final String name) {
            super(name);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the children
         */
        public List<DBEntry> getChildren() {
            return children;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  child  the children to set
         */
        public void addChildren(final DBEntry child) {
            this.children.add(child);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DBEntry {

        //~ Instance fields ----------------------------------------------------

        private String name;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DBEntry object.
         *
         * @param  name  DOCUMENT ME!
         */
        public DBEntry(final String name) {
            this.name = name;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the name
         */
        public String getName() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  name  the name to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public boolean equals(final Object obj) {
            return getClass().equals(obj.getClass()) && name.equals(((DBEntry)obj).name);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getFolderName() {
            int end = 0;

            if (name.indexOf("->") != -1) {
                end = name.lastIndexOf("->");
            }

            if (end == 0) {
                return "";
            } else {
                return name.substring(0, end);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getHash() {
            int start = 0;

            if (name.lastIndexOf("_") != -1) {
                start = name.lastIndexOf("_") + 1;
            }

            if (start == 0) {
                return "";
            } else {
                return name.substring(start);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getNameWithoutFolder() {
            int start = 0;

            if (name.indexOf("->") != -1) {
                start = name.lastIndexOf("->") + 2;
            }

            return name.substring(start);
        }

        @Override
        public String toString() {
            int start = 0;
            int end = name.length();

            if (name.indexOf("->") != -1) {
                start = name.lastIndexOf("->") + 2;
            }
            if (!(this instanceof DbFolder)) {
                if (name.lastIndexOf("_") != -1) {
                    end = name.lastIndexOf("_");
                }
            }

            return name.substring(start, end);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = (71 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
            return hash;
        }
    }
}
