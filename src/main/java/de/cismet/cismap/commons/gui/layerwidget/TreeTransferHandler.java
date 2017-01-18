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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TreeTransferHandler extends TransferHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(TreeTransferHandler.class);

    //~ Instance fields --------------------------------------------------------

    private DataFlavor nodesFlavor;
    private DataFlavor[] flavors = new DataFlavor[1];
    private List<TreePath> nodesToRemove;

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

        if ((targetNode instanceof LayerCollection) && containsDescendantPath(dl.getPath(), tree.getSelectionPaths())) {
            return false;
        }

        return true;
    }

    /**
     * Checks, if one of the source paths is a descendent path of the target path.
     *
     * @param   target      the parent path
     * @param   sourcePath  the possible descendent pathes
     *
     * @return  true, iff one of the source paths is a descendent path of the target path.
     */
    private boolean containsDescendantPath(final TreePath target, final TreePath[] sourcePath) {
        for (final TreePath path : sourcePath) {
            if (path.isDescendant(target)) {
                return true;
            }
        }

        return false;
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
        final ActiveLayerModel model = (ActiveLayerModel)((ActiveLayerModelWrapperWithoutProgress)tree.getModel())
                    .getModel();
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
            return dropPerformed(support, model, index, tree);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   support           DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean dropPerformed(final TransferHandler.TransferSupport support,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final JComponent parent) {
        return LayerDropUtils.drop(support, activeLayerModel, index, parent);
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
