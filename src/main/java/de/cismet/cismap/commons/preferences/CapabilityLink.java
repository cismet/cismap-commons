/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.preferences;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CapabilityLink {

    //~ Static fields/initializers ---------------------------------------------

    public static final String OGC = "OGC";                 // NOI18N
    public static final String OGC_DEPRECATED = "OGC-WMS";  // NOI18N
    public static final String SEPARATOR = "SEPARATOR";     // NOI18N
    public static final String MENU = "MENU";               // NOI18N
    public static final String INTERNAL_DB = "INTERNAL-DB"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private String type;
    private String subparent;
    private String link;
    private String alias;
    private String title;
    // contains the version of the represented capabilities document
    private String version;
    private boolean active = false;
    private boolean reverseAxisOrder = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  parent  DOCUMENT ME!
     */
    public CapabilityLink(final Element parent) {
        final Element e = parent.getChild("capabilities");                           // NOI18N
        type = e.getAttribute("type").getValue();                                    // NOI18N
        try {
            active = e.getAttribute("active").getBooleanValue();                     // NOI18N
        } catch (Exception notHandled) {
        }
        try {
            reverseAxisOrder = e.getAttribute("reverseAxisOrder").getBooleanValue(); // NOI18N
        } catch (Exception notHandled) {
            // nothing to do
        }
        alias = e.getAttributeValue("alias");                                        // NOI18N

        link = e.getTextTrim();
        link = CismapBroker.getInstance().urlToAlias(link);
        if (e.getAttribute("version") != null) {
            version = e.getAttribute("version").getValue(); // NOI18N
        }
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type              DOCUMENT ME!
     * @param  link              DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     * @param  active            DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final boolean reverseAxisOrder, final boolean active) {
        this(type, link, reverseAxisOrder, active, null, CismapBroker.getInstance().urlToAlias(link));
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type              DOCUMENT ME!
     * @param  link              DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     * @param  title             DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final boolean reverseAxisOrder, final String title) {
        this(type, link, reverseAxisOrder, title, null);
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type              DOCUMENT ME!
     * @param  link              DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     * @param  title             DOCUMENT ME!
     * @param  subparent         DOCUMENT ME!
     */
    public CapabilityLink(final String type,
            final String link,
            final boolean reverseAxisOrder,
            final String title,
            final String subparent) {
        this.type = type;
        this.link = CismapBroker.getInstance().urlToAlias(link);
        this.title = title;
        this.subparent = subparent;
        this.reverseAxisOrder = reverseAxisOrder;
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type              DOCUMENT ME!
     * @param  link              DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     * @param  active            DOCUMENT ME!
     * @param  subparent         DOCUMENT ME!
     * @param  alias             DOCUMENT ME!
     */
    public CapabilityLink(final String type,
            final String link,
            final boolean reverseAxisOrder,
            final boolean active,
            final String subparent,
            final String alias) {
        this.type = type;
        this.link = link;
        this.active = active;
        this.subparent = subparent;
        this.alias = alias;
        this.reverseAxisOrder = reverseAxisOrder;
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type              DOCUMENT ME!
     * @param  link              DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     * @param  version           DOCUMENT ME!
     * @param  active            DOCUMENT ME!
     * @param  alias             DOCUMENT ME!
     */
    public CapabilityLink(final String type,
            final String link,
            final boolean reverseAxisOrder,
            final String version,
            final boolean active,
            final String alias) {
        this(type, link, active, null);
        this.version = version;
        this.alias = alias;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  type  DOCUMENT ME!
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getLink() {
        if (alias != null) {
            return CismapBroker.getInstance().aliasToUrl(alias);
        } else {
            return CismapBroker.getInstance().aliasToUrl(link);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  link  DOCUMENT ME!
     */
    public void setLink(final String link) {
        this.link = link;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        final Element elem = new Element("capabilities");                            // NOI18N
        if (CismapBroker.getInstance().isAlias(link)) {
            elem.setAttribute("alias", link);                                        // NOI18N
        } else if (CismapBroker.getInstance().isAliasDefinedForUrl(link)) {
            elem.setAttribute("alias", CismapBroker.getInstance().urlToAlias(link)); // NOI18N
        }

        final CDATA cd = new CDATA(CismapBroker.getInstance().aliasToUrl(link));
        elem.addContent(cd);

        elem.setAttribute(new Attribute("type", type));                                  // NOI18N
        if (isReverseAxisOrder()) {
            elem.setAttribute("reverseAxisOrder", String.valueOf(isReverseAxisOrder())); // NOI18N
        }
        if (subparent != null) {
            elem.setAttribute(new Attribute("subparent", subparent));                    // NOI18N
        }
        elem.setAttribute(new Attribute("active", new Boolean(active).toString()));      // NOI18N
        if (version != null) {
            elem.setAttribute(new Attribute("version", version));                        // NOI18N
        }
        return elem;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElementAsListEntry() {
        final Element elem = new Element("capabilitiesList");                            // NOI18N
        final CDATA cd = new CDATA(link);
        elem.addContent(cd);
        elem.setAttribute("titlestring", title);                                         // NOI18N
        if (!isReverseAxisOrder()) {
            elem.setAttribute("reverseAxisOrder", String.valueOf(isReverseAxisOrder())); // NOI18N
        }
        elem.setAttribute(new Attribute("type", type));                                  // NOI18N
        if (subparent != null) {
            elem.setAttribute(new Attribute("subparent", subparent));                    // NOI18N
        }
        if (version != null) {
            elem.setAttribute(new Attribute("version", version));                        // NOI18N
        }
        return elem;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isActive() {
        return active;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  active  DOCUMENT ME!
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSubparent() {
        return subparent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  subparent  DOCUMENT ME!
     */
    public void setSubparent(final String subparent) {
        this.subparent = subparent;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  version  the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the reverseAxisOrder
     */
    public boolean isReverseAxisOrder() {
        return reverseAxisOrder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  reverseAxisOrder  the reverseAxisOrder to set
     */
    public void setReverseAxisOrder(final boolean reverseAxisOrder) {
        this.reverseAxisOrder = reverseAxisOrder;
    }
}
