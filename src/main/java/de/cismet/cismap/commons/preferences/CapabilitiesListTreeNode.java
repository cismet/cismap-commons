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
package de.cismet.cismap.commons.preferences;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CapabilitiesListTreeNode {

    //~ Instance fields --------------------------------------------------------

    private TreeMap<Integer, CapabilityLink> capabilitiesList = new TreeMap<Integer, CapabilityLink>();
    private LinkedList<CapabilitiesListTreeNode> subnodes = new LinkedList<CapabilitiesListTreeNode>();
    private String title = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt einen CapabilitiesList-Knoten.
     */
    public CapabilitiesListTreeNode() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Gibt die CapabilitiesList des Knotens zurück.
     *
     * @return  CapabilitiesList des Knotens
     */
    public TreeMap<Integer, CapabilityLink> getCapabilitiesList() {
        return capabilitiesList;
    }

    /**
     * Setzt die CapabilitiesList des Knotens.
     *
     * @param  capabilitiesList  DOCUMENT ME!
     */
    public void setCapabilitiesList(final TreeMap<Integer, CapabilityLink> capabilitiesList) {
        this.capabilitiesList = capabilitiesList;
    }

    /**
     * Fügt dem Knoten einen Unterknoten hinzu.
     *
     * @param  subnode  Unterknoten
     */
    public void addSubnode(final CapabilitiesListTreeNode subnode) {
        subnodes.add(subnode);
    }

    /**
     * Gibt die Liste der Unterknoten zurück.
     *
     * @return  Liste der Unterknoten
     */
    public List<CapabilitiesListTreeNode> getSubnodes() {
        return (List<CapabilitiesListTreeNode>)subnodes.clone();
    }

    /**
     * Gibt den Titel des CapabilitiesList-Knotens zurück.
     *
     * @return  Titel des CapabilitiesList-Knotens
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setzt den Titel des CapabilitiesList-Knotens.
     *
     * @param  title  Title des Knotens
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
