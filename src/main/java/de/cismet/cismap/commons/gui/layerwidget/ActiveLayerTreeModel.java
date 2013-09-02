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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ActiveLayerTreeModel implements TreeModel {

    //~ Instance fields --------------------------------------------------------

    private ActiveLayerModel activeLayerModel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ActiveLayerTreeModel object.
     *
     * @param  activeLayerModel  DOCUMENT ME!
     */
    public ActiveLayerTreeModel(final ActiveLayerModel activeLayerModel) {
        this.activeLayerModel = activeLayerModel;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getRoot() {
        return activeLayerModel.getRoot();
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        return activeLayerModel.getChild(parent, index);
    }

    @Override
    public int getChildCount(final Object parent) {
        return activeLayerModel.getChildCount(parent);
    }

    @Override
    public boolean isLeaf(final Object node) {
        return activeLayerModel.isLeaf(node);
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        activeLayerModel.valueForPathChanged(path, newValue);
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        return activeLayerModel.getIndexOfChild(parent, child);
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        activeLayerModel.addTreeModelListener(l);
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        activeLayerModel.removeTreeModelListener(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ActiveLayerModel getActiveLayerModel() {
        return activeLayerModel;
    }
}
