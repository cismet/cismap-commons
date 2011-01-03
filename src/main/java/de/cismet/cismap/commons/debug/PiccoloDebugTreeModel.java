/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.debug;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;

import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PiccoloDebugTreeModel implements TreeModel {

    //~ Instance fields --------------------------------------------------------

    PCanvas pc;
    Vector<TreeModelListener> treeModelListener = new Vector<TreeModelListener>();
    Object root = "DebugTree"; // NOI18N
    Vector<String> secondLevel = new Vector<String>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PiccoloDebugTreeModel.
     *
     * @param  pc  DOCUMENT ME!
     */
    public PiccoloDebugTreeModel(final PCanvas pc) {
        this.pc = pc;
        secondLevel.add(org.openide.util.NbBundle.getMessage(
                PiccoloDebugTreeModel.class,
                "PiccoloDebugTreeModel.PiccoloDebugTreeModel(PCanvas).secondLevel.cameras")); // NOI18N
        secondLevel.add(org.openide.util.NbBundle.getMessage(
                PiccoloDebugTreeModel.class,
                "PiccoloDebugTreeModel.PiccoloDebugTreeModel(PCanvas).secondLevel.layer"));   // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

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
        return false;
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
        if (parent == root) {
            return pc.getCamera().getChildrenCount();
        } else if (parent instanceof PNode) {
            return ((PNode)parent).getChildrenCount();
        } else {
            return 0;
        }
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
    public void valueForPathChanged(final TreePath path, final Object newValue) {
    }

    /**
     * Removes a listener previously added with <code>addTreeModelListener</code>.
     *
     * @param  l  the listener to remove
     *
     * @see    #addTreeModelListener
     */
    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        treeModelListener.remove(l);
    }

    /**
     * Adds a listener for the <code>TreeModelEvent</code> posted after the tree changes.
     *
     * @param  l  the listener to add
     *
     * @see    #removeTreeModelListener
     */
    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        treeModelListener.add(l);
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
        if (parent == root) {
            return pc.getCamera().getChild(index);
        } else if (parent instanceof PNode) {
            return ((PNode)parent).getChild(index);
        } else {
            return "UUPS"; // NOI18N
        }
    }

    /**
     * Returns the root of the tree. Returns <code>null</code> only if the tree has no nodes.
     *
     * @return  the root of the tree
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /**
     * Returns the index of child in parent. If either <code>parent</code> or <code>child</code> is <code>null</code>,
     * returns -1. If either <code>parent</code> or <code>child</code> don't belong to this tree model, returns -1.
     *
     * @param   parent  a note in the tree, obtained from this data source
     * @param   child   the node we are interested in
     *
     * @return  the index of the child in the parent, or -1 if either <code>child</code> or <code>parent</code> are
     *          <code>null</code> or don't belong to this tree model
     */
    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        if (parent == root) {
            return 0;
        } else if (parent instanceof PNode) {
            return ((PNode)parent).getChildrenReference().indexOf(child);
        } else {
            return -1;
        }
    }
}
