/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Crs {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger(Crs.class);

    //~ Instance fields --------------------------------------------------------

    private String code;
    private String shortname;
    private String name;
    private boolean metric;
    private boolean selected;
    private boolean hideInCrsSwitcher;
    private String esriDefinition;
    private boolean defaultCrs;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Crs object.
     */
    public Crs() {
    }

    /**
     * Creates a new Crs object.
     *
     * @param  elem  DOCUMENT ME!
     */
    public Crs(final Element elem) {
        this.shortname = elem.getAttribute("shortname").getValue();
        this.name = elem.getAttribute("name").getValue();
        this.code = elem.getAttribute("code").getValue();
        try {
            this.metric = elem.getAttribute("metric").getBooleanValue();
        } catch (DataConversionException e) {
            log.error("attribute metric of element crs must be e boolean. The current value is "
                        + elem.getAttribute("selected").getValue(),
                e);
        }

        try {
            this.selected = elem.getAttribute("selected").getBooleanValue();
        } catch (DataConversionException e) {
            log.error("attribute selected of element crs must be e boolean. The current value is "
                        + elem.getAttribute("selected").getValue(),
                e);
        }

        try {
            final Attribute attr = elem.getAttribute("defaultCrs");

            if (attr != null) {
                this.defaultCrs = attr.getBooleanValue();
            }
        } catch (DataConversionException e) {
            log.error("attribute defaultCrs of element crs must be e boolean. The current value is "
                        + elem.getAttribute("defaultCrs").getValue(),
                e);
        }

        try {
            final Attribute attr = elem.getAttribute("hideInCrsSwitcher");

            if (attr != null) {
                this.hideInCrsSwitcher = elem.getAttribute("hideInCrsSwitcher").getBooleanValue();
            }
        } catch (DataConversionException e) {
            log.error("attribute hideForChooser of element crs must be e boolean. The current value is "
                        + elem.getAttribute("hideInCrsSwitcher").getValue(),
                e);
        }

        esriDefinition = elem.getTextTrim();
    }

    /**
     * Creates a new Crs object.
     *
     * @param  code       DOCUMENT ME!
     * @param  shortname  DOCUMENT ME!
     * @param  name       DOCUMENT ME!
     * @param  metric     DOCUMENT ME!
     * @param  selected   DOCUMENT ME!
     */
    public Crs(final String code,
            final String shortname,
            final String name,
            final boolean metric,
            final boolean selected) {
        this.code = code;
        this.shortname = shortname;
        this.name = name;
        this.metric = metric;
        this.selected = selected;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getJDOMElement() {
        final Element e = new Element("crs");                                   // NOI18N
        e.setAttribute("shortname", shortname);                                 // NOI18N
        e.setAttribute("name", name);                                           // NOI18N
        e.setAttribute("code", code);                                           // NOI18N
        e.setAttribute("metric", String.valueOf(metric));                       // NOI18N
        e.setAttribute("selected", String.valueOf(selected));                   // NOI18N
        e.setAttribute("defaultCrs", String.valueOf(defaultCrs));                   // NOI18N
        e.setAttribute("hideInCrsSwitcher", String.valueOf(hideInCrsSwitcher)); // NOI18N
        e.setText(esriDefinition);
        return e;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the srs code (for example: EPSG:31466)
     */
    public String getCode() {
        return code;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  code  the srs cods to set (for example: EPSG:31466)
     */
    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  shortname  the shortname to set
     */
    public void setShortname(final String shortname) {
        this.shortname = shortname;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the name
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the metric
     */
    public boolean isMetric() {
        return metric;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metric  the metric to set
     */
    public void setMetric(final boolean metric) {
        this.metric = metric;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selected  the selected to set
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Crs) {
            final Crs other = (Crs)obj;
            return other.code.equals(code);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (53 * hash) + ((this.code != null) ? this.code.hashCode() : 0);
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hideForScale
     */
    public boolean isHideInCrsSwitcher() {
        return hideInCrsSwitcher;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hideInCrsSwitcher  hideForChooser hideForScale the hideForScale to set
     */
    public void setHideInCrsSwitcher(final boolean hideInCrsSwitcher) {
        this.hideInCrsSwitcher = hideInCrsSwitcher;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the esri definition for this crs
     */
    public String getEsriDefinition() {
        return esriDefinition;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  esriDefinition  the esri definition for this crs
     */
    public void setEsriDefinition(final String esriDefinition) {
        this.esriDefinition = esriDefinition;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  true, if a esri defintion is set
     */
    public boolean hasEsriDefinition() {
        return (this.esriDefinition != null) && !this.esriDefinition.equals("");
    }

    /**
     * @return the defaultCrs
     */
    public boolean isDefaultCrs() {
        return defaultCrs;
    }

    /**
     * @param defaultCrs the defaultCrs to set
     */
    public void setDefaultCrs(boolean defaultCrs) {
        this.defaultCrs = defaultCrs;
    }
}
