/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.preferences;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CapabilitiesPreferences {

    //~ Static fields/initializers ---------------------------------------------

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CapabilitiesPreferences.class);

    //~ Instance fields --------------------------------------------------------

    private TreeMap<Integer, CapabilityLink> capabilities = new TreeMap<Integer, CapabilityLink>();
    private CapabilitiesListTreeNode capabilitiesListTree;
    private boolean searchActivated;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CapabilitiesPreferences object.
     */
    public CapabilitiesPreferences() {
    }
    /**
     * Creates a new instance of CapabilitiesPreferences.
     *
     * @param  serverParent  DOCUMENT ME!
     * @param  localParent   DOCUMENT ME!
     */
    public CapabilitiesPreferences(final Element serverParent, final Element localParent) {
        final Element serverRoot = serverParent.getChild("cismapCapabilitiesPreferences"); // NOI18N
        final Element clientRoot = localParent.getChild("cismapCapabilitiesPreferences");  // NOI18N

        if (serverRoot != null) {
            final Attribute searchActive = serverRoot.getAttribute("searchPanelActivated");

            if (searchActive != null) {
                try {
                    searchActivated = searchActive.getBooleanValue();
                } catch (DataConversionException e) {
                    log.warn("Invalid value for attribute searchPanelActivated found", e);
                }
            }
        }

        if (clientRoot != null) {
            final Attribute searchActive = clientRoot.getAttribute("searchPanelActivated");

            if (searchActive != null) {
                try {
                    searchActivated = searchActive.getBooleanValue();
                } catch (DataConversionException e) {
                    log.warn("Invalid value for attribute searchPanelActivated found", e);
                }
            }

            final List caps = clientRoot.getChildren("capabilities"); // NOI18N
            final Iterator<Element> it = caps.iterator();
            int counter = 0;

            while (it.hasNext()) {
                try {
                    final Element elem = it.next();
                    final String type = elem.getAttribute("type").getValue();      // NOI18N
                    final String link = elem.getTextTrim();
                    final String subparent = elem.getAttributeValue("subparent");  // NOI18N
                    boolean active = false;
                    try {
                        active = elem.getAttribute("active").getBooleanValue();
                    } catch (Exception unhandled) {
                    }                                                              // NOI18N
                    capabilities.put(new Integer(counter++), new CapabilityLink(type, link, active, subparent));
                } catch (Throwable t) {
                    log.warn("Error while reading the CapabilityPreferences.", t); // NOI18N
                }
            }
        }

        // capabilitiesList auslesen und in Baum speichern
        capabilitiesListTree = createCapabilitiesListTreeNode(null, serverRoot);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Erzeugt rekursiv aus einem JDom-Element einen CapabilitiesList-Knoten samt CapabilitiesList und Unterknoten.
     *
     * @param   nodetitle  Title des CapabilitiesList-Knotens
     * @param   element    JDom-Element
     *
     * @return  CapabilitiesList-Knoten
     */
    private static CapabilitiesListTreeNode createCapabilitiesListTreeNode(final String nodetitle,
            final Element element) {
        final CapabilitiesListTreeNode node = new CapabilitiesListTreeNode();
        int listCounter = 0;

        node.setTitle(nodetitle);

        final TreeMap<Integer, CapabilityLink> capabilitiesList = new TreeMap<Integer, CapabilityLink>();
        for (final Element elem : (List<Element>)element.getChildren("capabilitiesList")) { // NOI18N
            try {
                final String type = elem.getAttribute("type").getValue();                   // NOI18N
                final String title = elem.getAttribute("titlestring").getValue();           // NOI18N
                boolean reverseAxisOrder = false;
                final Attribute reverseAxisOrderElement = elem.getAttribute("reverseAxisOrder");

                if (reverseAxisOrderElement != null) {
                    reverseAxisOrder = reverseAxisOrderElement.getBooleanValue();
                }

                if (type.equals(CapabilityLink.MENU)) {
                    // Unterknoten erzeugen
                    node.addSubnode(createCapabilitiesListTreeNode(title, elem));
                } else {
                    // CapabilitiesList-Eintrag erzeugen
                    final String link = elem.getTextTrim();
                    final String subparent = elem.getAttributeValue("subparent");  // NOI18N
                    capabilitiesList.put(new Integer(listCounter++),
                        new CapabilityLink(type, link, reverseAxisOrder, title, subparent));
                }
            } catch (Throwable t) {
                log.warn("Error while reading the CapabilityListPreferences.", t); // NOI18N
            }
        }

        // CapabilitiesList
        node.setCapabilitiesList(capabilitiesList);

        // fertig
        return node;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreeMap<Integer, CapabilityLink> getCapabilities() {
        return capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilities  DOCUMENT ME!
     */
    public void setCapabilities(final TreeMap<Integer, CapabilityLink> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CapabilitiesListTreeNode getCapabilitiesListTree() {
        return capabilitiesListTree;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the searchActivated
     */
    public boolean isSearchActivated() {
        return searchActivated;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchActivated  the searchActivated to set
     */
    public void setSearchActivated(final boolean searchActivated) {
        this.searchActivated = searchActivated;
    }
}
