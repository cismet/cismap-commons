/*
 * PiccoloDebugTreeModel.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 20. Juli 2006, 17:11
 *
 */

package de.cismet.cismap.commons.debug;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class PiccoloDebugTreeModel implements TreeModel{
    private static final ResourceBundle I18N = ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");
    PCanvas pc;
    Vector<TreeModelListener> treeModelListener=new Vector<TreeModelListener>();
    Object root=I18N.getString("de.cismet.cismap.commons.debug.PiccoloDebugTreeModel.root");
    Vector<String> secondLevel= new Vector<String>();
    
    
    /** Creates a new instance of PiccoloDebugTreeModel */
    public PiccoloDebugTreeModel(PCanvas pc) {
        this.pc=pc;
        secondLevel.add(I18N.getString("de.cismet.cismap.commons.debug.PiccoloDebugTreeModel.secondLevel.cameras"));
        secondLevel.add(I18N.getString("de.cismet.cismap.commons.debug.PiccoloDebugTreeModel.secondLevel.layer"));
    }
    
    /**
     * Returns <code>true</code> if <code>node</code> is a leaf.
     * It is possible for this method to return <code>false</code>
     * even if <code>node</code> has no children.
     * A directory in a filesystem, for example,
     * may contain no files; the node representing
     * the directory is not a leaf, but it also has no children.
     *
     *
     * @param node  a node in the tree, obtained from this data source
     * @return true if <code>node</code> is a leaf
     */
    public boolean isLeaf(Object node) {
        return false;
    }
    
    /**
     * Returns the number of children of <code>parent</code>.
     * Returns 0 if the node
     * is a leaf or if it has no children.  <code>parent</code> must be a node
     * previously obtained from this data source.
     *
     *
     * @param parent  a node in the tree, obtained from this data source
     * @return the number of children of the node <code>parent</code>
     */
    public int getChildCount(Object parent) {
        if (parent==root) {
            return pc.getCamera().getChildrenCount();
        } else if (parent instanceof PNode) {
            return ((PNode)parent).getChildrenCount();
        }
        else {
            return 0;
        }
    }
    
    /**
     * Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>.
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     *
     *
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
    
    /**
     * Removes a listener previously added with
     * <code>addTreeModelListener</code>.
     *
     *
     * @param l       the listener to remove
     * @see #addTreeModelListener
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListener.remove(l);
    }
    
    /**
     * Adds a listener for the <code>TreeModelEvent</code>
     * posted after the tree changes.
     *
     *
     * @param l       the listener to add
     * @see #removeTreeModelListener
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListener.add(l);
    }
    
    /**
     * Returns the child of <code>parent</code> at index <code>index</code>
     * in the parent's
     * child array.  <code>parent</code> must be a node previously obtained
     * from this data source. This should not return <code>null</code>
     * if <code>index</code>
     * is a valid index for <code>parent</code> (that is <code>index >= 0 &&
     * index < getChildCount(parent</code>)).
     *
     *
     * @param parent  a node in the tree, obtained from this data source
     * @return the child of <code>parent</code> at index <code>index</code>
     */
    public Object getChild(Object parent, int index) {
        if (parent==root) {
            return pc.getCamera().getChild(index);
        } else if (parent instanceof PNode) {
            return ((PNode)parent).getChild(index);
        }
        else {
            return I18N.getString("de.cismet.cismap.commons.debug.PiccoloDebugTreeModel.getChild.return");
        }
    }
    
    /**
     * Returns the root of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     *
     *
     * @return the root of the tree
     */
    public Object getRoot() {
        return root;
    }
    
    /**
     * Returns the index of child in parent.  If either <code>parent</code>
     * or <code>child</code> is <code>null</code>, returns -1.
     * If either <code>parent</code> or <code>child</code> don't
     * belong to this tree model, returns -1.
     *
     *
     * @param parent a note in the tree, obtained from this data source
     * @param child the node we are interested in
     * @return the index of the child in the parent, or -1 if either
     *    <code>child</code> or <code>parent</code> are <code>null</code>
     *    or don't belong to this tree model
     */
    public int getIndexOfChild(Object parent, Object child) {
        if (parent==root) {
            return 0;
        }
        else if (parent instanceof PNode) {
            return ((PNode)parent).getChildrenReference().indexOf(child);
        }
        else {
            return -1;
        }
    }
    
    
}
