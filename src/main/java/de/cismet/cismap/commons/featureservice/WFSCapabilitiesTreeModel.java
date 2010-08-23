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
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.capabilities.AbstractCapabilitiesTreeModel;
import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * Das WFSCapabilitiesTreeModel liegt hinter dem WFSCapabilitiesTree und gibt vor
 * was dieser anzeigt.
 * @author nh
 */
public class WFSCapabilitiesTreeModel extends AbstractCapabilitiesTreeModel {
    private transient final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private WFSCapabilities capabilities;
    private HashMap<FeatureType, Vector<FeatureServiceAttribute>> nodes;
    private Vector listener = new Vector();

    /**
     * Erzeugt ein neues WFSCapabilitiesTreeModel.
     * @param capabilities WFSCapabilites-Objekt
     */
    public WFSCapabilitiesTreeModel(WFSCapabilities capabilities) throws IOException, BadHttpStatusCodeException {
        this.nodes = FeatureServiceUtilities.getElementDeclarations(capabilities);
        this.capabilities = capabilities;
        log.debug("nodes: " + nodes.size());
    }

    /**
     * Returns all attributes of the given ElementDeclaration-object.
     * @param dec ElementDeclaration which childs are requested
     * @return ElementDeclaration-array or null
     */
    public Vector<FeatureServiceAttribute> getChildren(FeatureType feature) {
        return nodes.get(feature);
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
        if (node instanceof WFSCapabilities) {
            return false;
        } else {
            if (nodes.get(node) != null) {
                return false;
            } else {
                return true;
            }
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
        if (parent instanceof WFSCapabilities) {
            return nodes.size();
        } else {
            return nodes.get(parent) != null ? nodes.get(parent).size() : 0;
        }
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
        int childs = getChildCount(parent);
        if (childs <= 0) {
            return null;
        } else {
            if (parent instanceof WFSCapabilities) {
                if (index < childs) {
                    return nodes.keySet().toArray()[index];
                } else {
                    return nodes.keySet().toArray()[index - childs];
                }
            } else {
                if (index < childs) {
                    return nodes.get(parent) != null ? nodes.get(parent).get(index) : null;
                } else {
                    return nodes.get(parent) != null ? nodes.get(parent).get(index - childs) : null;
                }
            }
        }
    }

    /**
     * Returns the root of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     * 
     * @return  the root of the tree
     */
    public Object getRoot() {
        return capabilities;
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

    public WFSCapabilities getCapabilities() {
        return capabilities;
    }

    
}
