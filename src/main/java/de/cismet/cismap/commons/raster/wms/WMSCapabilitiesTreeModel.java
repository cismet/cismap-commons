/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;

import java.util.Vector;

import de.cismet.cismap.commons.capabilities.AbstractCapabilitiesTreeModel;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Style;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WMSCapabilitiesTreeModel extends AbstractCapabilitiesTreeModel {

    //~ Instance fields --------------------------------------------------------

    private WMSCapabilities capabilities = null;
    private String subparent = null;
    private Vector listener = new Vector();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WMSCapabilitiesTreeModel object.
     *
     * @param  capabilities  DOCUMENT ME!
     */
    public WMSCapabilitiesTreeModel(final WMSCapabilities capabilities) {
        this(capabilities, null);
    }
    /**
     * Creates a new WMSCapabilitiesTreeModel object.
     *
     * @param  capabilities  DOCUMENT ME!
     * @param  subparent     DOCUMENT ME!
     */
    public WMSCapabilitiesTreeModel(final WMSCapabilities capabilities, final String subparent) {
        this.capabilities = capabilities;
        this.subparent = subparent;
    }

    /**
     * Creates a new instance of WMSCapabilitiesTreeModel.
     */
    private WMSCapabilitiesTreeModel() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WMSCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilities  DOCUMENT ME!
     */
    public void setCapabilities(final WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Returns <code>true</code> if <code>node</code> is a leaf. It is possible for this method to return <code>
     * false</code> even if <code>node</code> has no children. A directory in a filesystem, for example, may contain no
     * files; the node representing the directory is not a leaf, but it also has no children.
     *
     * @param   node  a node in the tree, obtained from this data source
     *
     * @return  true if <code>node</code> is a leaf
     */
    @Override
    public boolean isLeaf(final Object node) {
        if (((node instanceof Layer) && (((Layer)node).getChildren().length == 0)
                        && (((Layer)node).getStyles().length == 0))
                    || (node instanceof Style)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the number of children of <code>parent</code>. Returns 0 if the node is a leaf or if it has no children.
     * <code>parent</code> must be a node previously obtained from this data source.
     *
     * @param   parent  a node in the tree, obtained from this data source
     *
     * @return  the number of children of the node <code>parent</code>
     */
    @Override
    public int getChildCount(final Object parent) {
        if (parent instanceof Layer) {
            final int layerChilds = ((Layer)parent).getChildren().length;
            final int styleChilds = ((Layer)parent).getStyles().length;
            return layerChilds + styleChilds;
        }
        return 0;
    }

    /**
     * Messaged when the user has altered the value for the item identified by <code>path</code> to <code>
     * newValue</code>. If <code>newValue</code> signifies a truly new value the model should post a <code>
     * treeNodesChanged</code> event.
     *
     * @param  path      path to the node that the user has altered
     * @param  newValue  the new value from the TreeCellEditor
     */
    @Override
    public void valueForPathChanged(final javax.swing.tree.TreePath path, final Object newValue) {
    }

    /**
     * Removes a listener previously added with <code>addTreeModelListener</code>.
     *
     * @param  l  the listener to remove
     *
     * @see    #addTreeModelListener
     */
    @Override
    public void removeTreeModelListener(final javax.swing.event.TreeModelListener l) {
        listener.remove(l);
    }

    /**
     * Adds a listener for the <code>TreeModelEvent</code> posted after the tree changes.
     *
     * @param  l  the listener to add
     *
     * @see    #removeTreeModelListener
     */
    @Override
    public void addTreeModelListener(final javax.swing.event.TreeModelListener l) {
        listener.add(l);
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code> in the parent's child array. <code>
     * parent</code> must be a node previously obtained from this data source. This should not return <code>null</code>
     * if <code>index</code> is a valid index for <code>parent</code> (that is <code>index >= 0 && index <
     * getChildCount(parent</code>)).
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @param   index   DOCUMENT ME!
     *
     * @return  the child of <code>parent</code> at index <code>index</code>
     */
    @Override
    public Object getChild(final Object parent, final int index) {
        if (parent instanceof Layer) {
            final int layerChilds = ((Layer)parent).getChildren().length;
            if (index < layerChilds) {
                return ((Layer)parent).getChildren()[index];
            } else {
                return ((Layer)parent).getStyles()[index - layerChilds];
            }
        }
        return null;
    }

    /**
     * Returns the root of the tree. Returns <code>null</code> only if the tree has no nodes.
     *
     * @return  the root of the tree
     */
    @Override
    public Object getRoot() {
        final Layer rootLayer;

        if (subparent != null) {
            rootLayer = getLayerByTitle(capabilities.getLayer(), subparent);
        } else {
            rootLayer = capabilities.getLayer();
        }

        if (rootLayer != null) {
            rootLayer.setFilterString(filterString);
        }

        return rootLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layer  DOCUMENT ME!
     * @param   title  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Layer getLayerByTitle(final Layer layer, final String title) {
        if ((layer.getTitle() != null) && layer.getTitle().equals(title)) {
            return layer;
        } else {
            final Layer[] larr = layer.getChildren();
            for (final Layer l : larr) {
                final Layer test = getLayerByTitle(l, title);
                if (test != null) {
                    return test;
                }
            }
            return null;
        }
    }

    /**
     * Returns the index of child in parent. If <code>parent</code> is <code>null</code> or <code>child</code> is <code>
     * null</code>, returns -1.
     *
     * @param   parent  a note in the tree, obtained from this data source
     * @param   child   the node we are interested in
     *
     * @return  the index of the child in the parent, or -1 if either <code>child</code> or <code>parent</code> are
     *          <code>null</code>
     */
    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        return 0;
    }
}
