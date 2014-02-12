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
 * If this wrapper is used, when the model is set, the progress changed events from the ActiveLayerModel are not fired
 * to this model.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ActiveLayerModelWrapperWithoutProgress implements TreeModel {

    //~ Instance fields --------------------------------------------------------

    private ActiveLayerModel model;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ActiveLayerModelWrapperWithoutProgress object.
     *
     * @param  model  DOCUMENT ME!
     */
    public ActiveLayerModelWrapperWithoutProgress(final ActiveLayerModel model) {
        this.model = model;
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
}
