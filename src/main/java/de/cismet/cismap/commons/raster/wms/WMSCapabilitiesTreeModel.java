/*
 * WMSCapabilitiesTreeModel.java
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
 * Created on 14. Oktober 2005, 11:42
 *
 */

package de.cismet.cismap.commons.raster.wms;

import de.cismet.cismap.commons.capabilities.AbstractCapabilitiesTreeModel;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Style;
import java.util.Vector;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WMSCapabilitiesTreeModel extends AbstractCapabilitiesTreeModel{
    private WMSCapabilities capabilities=null;
    private String subparent=null;
    private Vector listener=new Vector();
    public WMSCapabilitiesTreeModel(WMSCapabilities capabilities) {
        this(capabilities,null);
    } 
    public WMSCapabilitiesTreeModel(WMSCapabilities capabilities,String subparent) {
        this.capabilities=capabilities;
        this.subparent=subparent;
    }
    
    /** Creates a new instance of WMSCapabilitiesTreeModel */
    private WMSCapabilitiesTreeModel() {
    }

    public WMSCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Returns <code>true</code> if <code>node</code> is a leaf.
     * It is possible for this method to return <code>false</code>
     * even if <code>node</code> has no children.
     * A directory in a filesystem, for example,
     * may contain no files; the node representing
     * the directory is not a leaf, but it also has no children.
     * 
     * @param   node  a node in the tree, obtained from this data source
     * @return  true if <code>node</code> is a leaf
     */
    public boolean isLeaf(Object node) {
        if ((node instanceof Layer && ((Layer)node).getChildren().length==0 && ((Layer)node).getStyles().length==0)||
            (node instanceof Style)
            ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the number of children of <code>parent</code>.
     * Returns 0 if the node
     * is a leaf or if it has no children.  <code>parent</code> must be a node
     * previously obtained from this data source.
     * 
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the number of children of the node <code>parent</code>
     */
    public int getChildCount(Object parent) {
        if (parent instanceof Layer) {
            int layerChilds=((Layer)parent).getChildren().length;
            int styleChilds=((Layer)parent).getStyles().length;
            return layerChilds+styleChilds;
        }
        return 0;
    }

    /**
     * Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>. 
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     * 
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
    }

    /**
     * Removes a listener previously added with
     * <code>addTreeModelListener</code>.
     * 
     * @see     #addTreeModelListener
     * @param   l       the listener to remove
     */
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {
        listener.remove(l);
    }

    /**
     * Adds a listener for the <code>TreeModelEvent</code>
     * posted after the tree changes.
     * 
     * @param   l       the listener to add
     * @see     #removeTreeModelListener
     */
    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
        listener.add(l);
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
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the child of <code>parent</code> at index <code>index</code>
     */
    public Object getChild(Object parent, int index) {
        if (parent instanceof Layer) {
            int layerChilds=((Layer)parent).getChildren().length;
            if (index<layerChilds) {
                return ((Layer)parent).getChildren()[index];
            }
            else {
                return ((Layer)parent).getStyles()[index-layerChilds];
            }
        }
        return null;
    }

    /**
     * Returns the root of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     * 
     * @return  the root of the tree
     */
    public Object getRoot() {
        if (subparent!=null) {
            return getLayerByTitle(capabilities.getLayer(), subparent);
        }
        else {
            return capabilities.getLayer();
        }
    }

    
    private Layer getLayerByTitle(Layer layer,String title) {
        if (layer.getTitle()!=null&&layer.getTitle().equals(title)) {
            return layer;
        }
        else {
            Layer[] larr=layer.getChildren();
            for (Layer l:larr){
                Layer test=getLayerByTitle(l, title);
                if (test!=null) {
                    return test;
                }
            }
            return null;
        }
    }
    
    /**
     * Returns the index of child in parent.  If <code>parent</code>
     * is <code>null</code> or <code>child</code> is <code>null</code>,
     * returns -1.
     * 
     * @param parent a note in the tree, obtained from this data source
     * @param child the node we are interested in
     * @return the index of the child in the parent, or -1 if either
     *    <code>child</code> or <code>parent</code> are <code>null</code>
     */
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }
    
}
