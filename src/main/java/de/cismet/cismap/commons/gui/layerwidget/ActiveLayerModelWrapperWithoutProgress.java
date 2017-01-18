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

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * If this wrapper is used, when the model is set, the progress changed events from the ActiveLayerModel are not fired
 * to this model.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ActiveLayerModelWrapperWithoutProgress implements TreeModel, TreeModelListener {

    //~ Instance fields --------------------------------------------------------

    private ActiveLayerModel model;
    private final List<JTree> treesToUpdate = new ArrayList<JTree>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ActiveLayerModelWrapperWithoutProgress object.
     *
     * @param  model  DOCUMENT ME!
     */
    public ActiveLayerModelWrapperWithoutProgress(final ActiveLayerModel model) {
        this.model = model;
        model.addTreeModelWithoutProgressListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getRoot() {
        return getModel().getRoot();
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        return getModel().getChild(parent, index);
    }

    @Override
    public int getChildCount(final Object parent) {
        return getModel().getChildCount(parent);
    }

    @Override
    public boolean isLeaf(final Object node) {
        return getModel().isLeaf(node);
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        getModel().valueForPathChanged(path, newValue);
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        return getModel().getIndexOfChild(parent, child);
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        getModel().addTreeModelWithoutProgressListener(l);
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        getModel().removeTreeModelWithoutProgressListener(l);
    }

    /**
     * Adds a tree to update.
     *
     * <p>Invokes the updateUI() method of the given tree after every fireTreeStructureChanged() invocation. This is
     * required to refresh the path bounds of the tree. Without this refresh, the change of the name of a tree path will
     * cause a display error.</p>
     *
     * @param  tree  the tree to invoke updateUI()
     */
    public void addTreeToUpdate(final JTree tree) {
        treesToUpdate.add(tree);
    }

    /**
     * Removes the given tree.
     *
     * @param  tree  tree to remove
     *
     * @see    addTreeToUpdate(Jtree)
     */
    public void removeTreeToUpdate(final JTree tree) {
        treesToUpdate.remove(tree);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the model
     */
    public ActiveLayerModel getModel() {
        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  model  the model to set
     */
    public void setModel(final ActiveLayerModel model) {
        this.model = model;
    }

    @Override
    public void treeNodesChanged(final TreeModelEvent e) {
        fireUpdateUI();
    }

    @Override
    public void treeNodesInserted(final TreeModelEvent e) {
        fireUpdateUI();
    }

    @Override
    public void treeNodesRemoved(final TreeModelEvent e) {
        fireUpdateUI();
    }

    @Override
    public void treeStructureChanged(final TreeModelEvent e) {
        fireUpdateUI();
    }

    /**
     * DOCUMENT ME!
     */
    private void fireUpdateUI() {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (final JTree tree : treesToUpdate) {
                        tree.updateUI();
                    }
                }
            });
    }
}
