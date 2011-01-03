/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget.test;
import java.awt.datatransfer.*;

import javax.swing.tree.TreePath;

/**
 * This represents a TreePath (a node in a JTree) that can be transferred between a drag source and a drop target.
 *
 * @version  $Revision$, $Date$
 */
class CTransferableTreePath implements Transferable {

    //~ Static fields/initializers ---------------------------------------------

    // The type of DnD object being dragged...
    public static final DataFlavor TREEPATH_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreePath"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private TreePath _path;

    private DataFlavor[] _flavors = { TREEPATH_FLAVOR };

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a transferrable tree path object for the specified path.
     *
     * @param  path  DOCUMENT ME!
     */
    public CTransferableTreePath(final TreePath path) {
        _path = path;
    }

    //~ Methods ----------------------------------------------------------------

    // Transferable interface methods...
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return _flavors;
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return java.util.Arrays.asList(_flavors).contains(flavor);
    }

    @Override
    public synchronized Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.isMimeTypeEqual(TREEPATH_FLAVOR.getMimeType())) { // DataFlavor.javaJVMLocalObjectMimeType))
            return _path;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
