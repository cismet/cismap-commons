package de.cismet.cismap.commons;

import org.apache.log4j.Logger;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author therter
 */
public class Crs {
    private static final Logger log = Logger.getLogger(Crs.class);
    private String code;
    private String shortname;
    private String name;
    private boolean metric;
    private boolean selected;


    public Crs() {}

    public Crs(String code, String shortname, String name, boolean metric, boolean selected) {
        this.code = code;
        this.shortname = shortname;
        this.name = name;
        this.metric = metric;
        this.selected = selected;
    }


    public Crs(Element elem) {
        this.shortname = elem.getAttribute("shortname").getValue();
        this.name = elem.getAttribute("name").getValue();
        this.code = elem.getAttribute("code").getValue();
        try {
            this.metric = elem.getAttribute("metric").getBooleanValue();
        } catch (DataConversionException e) {
            log.error("attribute selected of element crs must be e boolean. The current value is "
                    + elem.getAttribute("selected").getValue(), e);
        }

        try {
            this.selected = elem.getAttribute("selected").getBooleanValue();
        } catch (DataConversionException e) {
            log.error("attribute selected of element crs must be e boolean. The current value is "
                    + elem.getAttribute("selected").getValue(), e);
        }
    }

//    public Element toElement() {
//
//    }

    /**
     * @return the srs code (for example: EPSG:31466)
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the srs cods to set (for example: EPSG:31466)
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * @param shortname the shortname to set
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the metric
     */
    public boolean isMetric() {
        return metric;
    }

    /**
     * @param metric the metric to set
     */
    public void setMetric(boolean metric) {
        this.metric = metric;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return code;
    }
}