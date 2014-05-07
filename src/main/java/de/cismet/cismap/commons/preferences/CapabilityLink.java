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
    private String title;
    // contains the version of the represented capabilities document
    private String version;
    private boolean active = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  parent  DOCUMENT ME!
     */
    public CapabilityLink(final Element parent) {
        final Element e = parent.getChild("capabilities");       // NOI18N
        type = e.getAttribute("type").getValue();                // NOI18N
        try {
            active = e.getAttribute("active").getBooleanValue(); // NOI18N
        } catch (Exception notHandled) {
        }
        link = e.getTextTrim();
        if (e.getAttribute("version") != null) {
            version = e.getAttribute("version").getValue();      // NOI18N
        }
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type    DOCUMENT ME!
     * @param  link    DOCUMENT ME!
     * @param  active  DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final boolean active) {
        this(type, link, active, null);
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type   DOCUMENT ME!
     * @param  link   DOCUMENT ME!
     * @param  title  DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final String title) {
        this(type, link, title, null);
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type       DOCUMENT ME!
     * @param  link       DOCUMENT ME!
     * @param  active     DOCUMENT ME!
     * @param  subparent  DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final boolean active, final String subparent) {
        this.type = type;
        this.link = link;
        this.active = active;
        this.subparent = subparent;
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type     DOCUMENT ME!
     * @param  link     DOCUMENT ME!
     * @param  version  DOCUMENT ME!
     * @param  active   DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final String version, final boolean active) {
        this(type, link, active, null);
        this.version = version;
    }

    /**
     * Creates a new CapabilityLink object.
     *
     * @param  type       DOCUMENT ME!
     * @param  link       DOCUMENT ME!
     * @param  title      DOCUMENT ME!
     * @param  subparent  DOCUMENT ME!
     */
    public CapabilityLink(final String type, final String link, final String title, final String subparent) {
        this.type = type;
        this.link = link;
        this.title = title;
        this.subparent = subparent;
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
        return link;
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
        final Element elem = new Element("capabilities");                           // NOI18N
        final CDATA cd = new CDATA(link);
        elem.addContent(cd);
        elem.setAttribute(new Attribute("type", type));                             // NOI18N
        if (subparent != null) {
            elem.setAttribute(new Attribute("subparent", subparent));               // NOI18N
        }
        elem.setAttribute(new Attribute("active", new Boolean(active).toString())); // NOI18N
        if (version != null) {
            elem.setAttribute(new Attribute("version", version));                   // NOI18N
        }
        return elem;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElementAsListEntry() {
        final Element elem = new Element("capabilitiesList");         // NOI18N
        final CDATA cd = new CDATA(link);
        elem.addContent(cd);
        elem.setAttribute("titlestring", title);                      // NOI18N
        elem.setAttribute(new Attribute("type", type));               // NOI18N
        if (subparent != null) {
            elem.setAttribute(new Attribute("subparent", subparent)); // NOI18N
        }
        if (version != null) {
            elem.setAttribute(new Attribute("version", version));     // NOI18N
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
}
