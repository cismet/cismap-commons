package de.cismet.cismap.commons.preferences;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;

public class CapabilityLink {
    public static final String OGC = "OGC";//NOI18N
    public static final String OGC_DEPRECATED = "OGC-WMS";//NOI18N
    public static final String SEPARATOR = "SEPARATOR";//NOI18N
    public static final String MENU = "MENU";//NOI18N
    private String type;
    private String subparent;
    private String link;
    private String title;
    private boolean active = false;

    public CapabilityLink(String type, String link, boolean active, String subparent) {
        this.type = type;
        this.link = link;
        this.active = active;
        this.subparent = subparent;
    }

    public CapabilityLink(String type, String link, boolean active) {
        this(type, link, active, null);
    }

    public CapabilityLink(String type, String link, String title) {
        this(type, link, title, null);
    }

    public CapabilityLink(String type, String link, String title, String subparent) {
        this.type = type;
        this.link = link;
        this.title = title;
        this.subparent = subparent;
    }

    public CapabilityLink(Element parent) {
        Element e = parent.getChild("capabilities");//NOI18N
        type = e.getAttribute("type").getValue();//NOI18N
        try {
            active = e.getAttribute("active").getBooleanValue();//NOI18N
        } catch (Exception notHandled) {
        }
        link = e.getTextTrim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Element getElement() {
        Element elem = new Element("capabilities");//NOI18N
        CDATA cd = new CDATA(link);
        elem.addContent(cd);
        elem.setAttribute(new Attribute("type", type));//NOI18N
        if (subparent != null) {
            elem.setAttribute(new Attribute("subparent", subparent));//NOI18N
        }
        elem.setAttribute(new Attribute("active", new Boolean(active).toString()));//NOI18N
        return elem;
    }

    public Element getElementAsListEntry() {
        Element elem = new Element("capabilitiesList");//NOI18N
        CDATA cd = new CDATA(link);
        elem.addContent(cd);
        elem.setAttribute("titlestring", title);//NOI18N
        elem.setAttribute(new Attribute("type", type));//NOI18N
        if (subparent != null) {
            elem.setAttribute(new Attribute("subparent", subparent));//NOI18N
        }
        return elem;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSubparent() {
        return subparent;
    }

    public void setSubparent(String subparent) {
        this.subparent = subparent;
    }
}
