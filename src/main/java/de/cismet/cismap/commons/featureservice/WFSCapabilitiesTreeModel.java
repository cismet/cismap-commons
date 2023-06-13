/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.featureservice;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import de.cismet.cismap.commons.capabilities.AbstractCapabilitiesTreeModel;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;

/**
 * Das WFSCapabilitiesTreeModel liegt hinter dem WFSCapabilitiesTree und gibt vor was dieser anzeigt.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class WFSCapabilitiesTreeModel extends AbstractCapabilitiesTreeModel {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private WFSCapabilities capabilities;
    private TreeMap<FeatureType, Vector<FeatureServiceAttribute>> nodes;
    private Vector listener = new Vector();

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt ein neues WFSCapabilitiesTreeModel.
     *
     * @param   capabilities  WFSCapabilites-Objekt
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    public WFSCapabilitiesTreeModel(final WFSCapabilities capabilities) throws IOException, Exception {
        this.nodes = FeatureServiceUtilities.getElementDeclarations(capabilities);
        this.capabilities = capabilities;
        if (log.isDebugEnabled()) {
            log.debug("nodes: " + nodes.size());
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns all attributes of the given ElementDeclaration-object.
     *
     * @param   feature  dec ElementDeclaration which childs are requested
     *
     * @return  ElementDeclaration-array or null
     */
    public Vector<FeatureServiceAttribute> getChildren(final FeatureType feature) {
        return nodes.get(feature);
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
        if (node instanceof WFSCapabilities) {
            return false;
        } else {
            if ((node instanceof FeatureType) && (nodes.get(node) != null)) {
                return false;
            } else {
                return true;
            }
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
        if (parent instanceof WFSCapabilities) {
            if (filterString != null) {
                return getFilteredNodes().size();
            } else {
                return nodes.size();
            }
        } else {
            return ((parent instanceof FeatureType) && (nodes.get(parent) != null)) ? nodes.get(parent).size() : 0;
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
        final int childs = getChildCount(parent);
        if (childs <= 0) {
            return null;
        } else {
            if (parent instanceof WFSCapabilities) {
                int correctedIndex = index;

                if (index >= childs) {
                    correctedIndex = index - childs;
                }

                if (filterString != null) {
                    final List<FeatureType> allValidNodes = getFilteredNodes();

                    return allValidNodes.get(correctedIndex);
                } else {
                    return nodes.keySet().toArray()[correctedIndex];
                }
            } else {
                if (index < childs) {
                    return ((parent instanceof FeatureType) && (nodes.get(parent) != null))
                        ? nodes.get(parent).get(index) : null;
                } else {
                    return ((parent instanceof FeatureType) && (nodes.get(parent) != null))
                        ? nodes.get(parent).get(index - childs) : null;
                }
            }
        }
    }

    /**
     * Returns the root of the tree. Returns <code>null</code> only if the tree has no nodes.
     *
     * @return  the root of the tree
     */
    @Override
    public Object getRoot() {
        return capabilities;
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WFSCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<FeatureType> getFilteredNodes() {
        final List<FeatureType> allValidNodes = new ArrayList<FeatureType>();

        for (final FeatureType feature : nodes.keySet()) {
            if (((feature.getTitle() != null)
                            && (feature.getTitle().toLowerCase().indexOf(filterString.toLowerCase()) != -1))
                        || ((feature.getName() != null)
                            && (feature.getName().toString().toLowerCase().indexOf(filterString.toLowerCase()) != -1))
                        || (containsFilterString(feature.getKeywords()))) {
                allValidNodes.add(feature);
            }
        }

        return allValidNodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   keywords  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean containsFilterString(final String[] keywords) {
        if (keywords != null) {
            for (final String tmp : keywords) {
                if ((tmp != null) && (tmp.toLowerCase().indexOf(filterString.toLowerCase()) != -1)) {
                    return true;
                }
            }
        }

        return false;
    }
}
