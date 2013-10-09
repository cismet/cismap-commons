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


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.util.DnDUtils;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
class TreeTransferHandler extends TransferHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(TreeTransferHandler.class);

    //~ Instance fields --------------------------------------------------------

    private DataFlavor nodesFlavor;
    private DataFlavor[] flavors = new DataFlavor[1];
    private List<TreePath> nodesToRemove;
    private DataFlavor fromCapabilityWidget = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType,
            "SelectionAndCapabilities"); // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TreeTransferHandler object.
     */
    public TreeTransferHandler() {
        try {
            final String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                        + ";class=\"" + javax.swing.tree.TreePath[].class.getName()
                        + "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean canImport(final TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return true;
        }
        // Do not allow a drop on the drag source selections
        final JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        final JTree tree = (JTree)support.getComponent();
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

        if (!(targetNode instanceof LayerCollection) && !targetNode.equals("Layer")) {
            return false;
        }

        return true;
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        final JTree tree = (JTree)c;
        final TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            // Make up a node array for transfer and
            // another for the nodes that will be removed in
            // exportDone after a successful drop.
            final List<TreePath> toTransfer = new ArrayList<TreePath>();
            final List<TreePath> toRemove = new ArrayList<TreePath>();
            final TreePath path = paths[0];
            toTransfer.add(copy(path));
            toRemove.add(path);
            for (int i = 1; i < paths.length; i++) {
                final TreePath next = paths[i];

                toTransfer.add(copy(next));
                toRemove.add(next);
            }
            final TreePath[] nodes = toTransfer.toArray(new TreePath[toTransfer.size()]);
            nodesToRemove = toRemove;
            return new NodesTransferable(nodes);
        }
        return null;
    }

    /**
     * Copy used in createTransferable.
     *
     * @param   path  path the path to copy
     *
     * @return  A copy of the given TreePath
     */
    private TreePath copy(final TreePath path) {
        return new TreePath(path.getPath());
    }

    @Override
    protected void exportDone(final JComponent source, final Transferable data, final int action) {
        if ((action & MOVE) == MOVE) {
//            final JTree tree = (JTree)source;
//            final ActiveLayerModel model = (ActiveLayerModel)tree.getModel();
//            // Remove nodes saved in nodesToRemove in createTransferable.
//            for (int i = 0; i < nodesToRemove.size(); i++) {
//                final Object parent = nodesToRemove.get(i).getParentPath().getLastPathComponent();
//
//                if (parent.equals(model.getRoot())) {
//                    model.removeLayer(nodesToRemove.get(i));
//                } else if (parent instanceof LayerCollection) {
//                    ((LayerCollection)parent).remove(nodesToRemove.get(i).getLastPathComponent());
//                }
//            }
//
//            model.fireTreeStructureChanged(this, new Object[] { model.getRoot() }, null, null);
        }
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(final TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        // Get drop location info.
        final JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        final int childIndex = dl.getChildIndex();
        final TreePath dest = dl.getPath();
        final Object parent = dest.getLastPathComponent();
        final JTree tree = (JTree)support.getComponent();
        final ActiveLayerModel model = (ActiveLayerModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex; // DropMode.INSERT
        if (childIndex == -1) { // DropMode.ON
            index = model.getChildCount(parent);
        }

        if (support.isDataFlavorSupported(nodesFlavor)) {
            // Es handelt sich um eine MOVE Aktion --> Die Drag Operation wurde aus dem Themenbaum gestartet
            TreePath[] nodes = null;
            try {
                final Transferable t = support.getTransferable();
                nodes = (TreePath[])t.getTransferData(nodesFlavor);
            } catch (UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch (java.io.IOException ioe) {
                System.out.println("I/O error: " + ioe.getMessage());
            }

            for (int i = 0; i < nodes.length; i++) {
                final TreePath parentPath = nodes[i].getParentPath();
                final Object layer = nodes[i].getLastPathComponent();

                // The index must be decreased, if the layer is moved to a higher row number in the same folder
                if (parentPath.getLastPathComponent().equals(model.getRoot())) {
                    if (model.getIndexOfChild(model.getRoot(), layer) > -1) {
                        if (model.getIndexOfChild(model.getRoot(), layer) < index) {
                            --index;
                        }
                    }
                } else if (parentPath.getLastPathComponent() instanceof LayerCollection) {
                    final LayerCollection parentCollection = (LayerCollection)parentPath.getLastPathComponent();
                    if (parentCollection.indexOf(layer) > -1) {
                        if (parentCollection.indexOf(layer) < index) {
                            --index;
                        }
                    }
                }

                model.moveLayer(parentPath, dest, index, layer);
            }
            return true;
        } else {
            return dropPerformed(support, model, index);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   support           DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean dropPerformed(final TransferHandler.TransferSupport support,
            final ActiveLayerModel activeLayerModel,
            final int index) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Drop with this flavors:" + support.getDataFlavors()); // NOI18N
        }
        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || support.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
            try {
                List<File> data = null;
                final Transferable transferable = support.getTransferable();
                if (support.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Drop is unix drop");                      // NOI18N
                    }

                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Drop is Mac drop xxx"
                                        + transferable.getTransferData(DataFlavor.javaFileListFlavor)); // NOI18N
                        }

                        data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    } catch (Exception e) {
                        // transferable.getTransferData(DataFlavor.javaFileListFlavor) will throw an
                        // UnsupportedFlavorException on Linux
                        if (data == null) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Drop is Linux drop"); // NOI18N
                            }
                            data = DnDUtils.textURIListToFileList((String)transferable.getTransferData(
                                        DnDUtils.URI_LIST_FLAVOR));
                        }
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Drop is windows drop");       // NOI18N
                    }
                    data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Drag&Drop File List: " + data); // NOI18N
                }
                if (data != null) {
                    for (final File currentFile : data) {
                        // NO HARDCODING
                        try {
                            LOG.info("DocumentUri: " + currentFile.toURI()); // NOI18N

                            final DocumentFeatureService dfs = DocumentFeatureServiceFactory
                                        .createDocumentFeatureService(currentFile);
                            activeLayerModel.addLayer(dfs, index);

                            if (dfs instanceof ShapeFileFeatureService) {
                                new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            do {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (final InterruptedException e) {
                                                    // nothing to do
                                                }
                                            } while (!dfs.isInitialized());

                                            if (((ShapeFileFeatureService)dfs).isErrorInGeometryFound()) {
                                                LOG.error("Error in shape geometry found.");
                                            } else if (((ShapeFileFeatureService)dfs).isNoGeometryRecognised()) {
                                                LOG.error("No geometry in shape recognised.");
                                            }
                                        }
                                    }).start();

                                return true;
                            }
                        } catch (final Exception ex) {
                            LOG.error("Error during creation of a FeatureServices", ex); // NOI18N
                        }
                    }
                } else {
                    LOG.warn("No files available");                                      // NOI18N
                }
            } catch (final Exception ex) {
                LOG.error("Failure during drag & drop opertation", ex);                  // NOI18N
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class NodesTransferable implements Transferable {

        //~ Instance fields ----------------------------------------------------

        TreePath[] nodes;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NodesTransferable object.
         *
         * @param  nodes  DOCUMENT ME!
         */
        public NodesTransferable(final TreePath[] nodes) {
            this.nodes = nodes;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
