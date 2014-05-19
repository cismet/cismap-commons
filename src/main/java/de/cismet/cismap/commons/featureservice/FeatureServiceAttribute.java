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
package de.cismet.cismap.commons.featureservice;

import org.jdom.Element;

import java.util.Arrays;

import de.cismet.cismap.commons.ConvertableToXML;

/**
 * Describes the attributes of all Features available in a certain layer.
 *
 * @author   Sebastian Puhl
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class FeatureServiceAttribute implements ConvertableToXML, Cloneable {

    //~ Static fields/initializers ---------------------------------------------

    // public static final String STRING = "string";
    // public static final String INTEGER = "integer";
    // public static final String GEOMETRY = "geometry";
    public static final String IS_SELECTED = "isSelected"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String type;
    private String name;
    private boolean geometry;
    private boolean selected;
    private boolean visible = true;
    private boolean nameElement = false;
    private String alias;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureServiceAttribute object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public FeatureServiceAttribute(final Element e) throws Exception {
        this.initFromElement(e);
    }

    /**
     * Creates a new FeatureServiceAttribute object.
     *
     * @param  name        DOCUMENT ME!
     * @param  type        DOCUMENT ME!
     * @param  isSelected  DOCUMENT ME!
     */
    public FeatureServiceAttribute(final String name, final String type, final boolean isSelected) {
        setName(name);
        // type.substring(4) removes the namespace gml:
        if (FeatureServiceUtilities.isElementOfGeometryType(type)) {
            setGeometry(true);
        }
        setSelected(isSelected);
        setType(type);
    }

    /**
     * Creates a new FeatureServiceAttribute object.
     *
     * @param  featureServiceAttribute  DOCUMENT ME!
     */
    protected FeatureServiceAttribute(final FeatureServiceAttribute featureServiceAttribute) {
        this(new String(featureServiceAttribute.getName()),
            String.valueOf(featureServiceAttribute.getType()),
            featureServiceAttribute.isSelected());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAlias() {
        if (alias == null) {
            return "";
        } else {
            return alias;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  alias  DOCUMENT ME!
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

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
     * @param  isGeometry  DOCUMENT ME!
     */
    public void setGeometry(final boolean isGeometry) {
        this.geometry = isGeometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isGeometry() {
        return geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isSelected  DOCUMENT ME!
     */
    public void setSelected(final boolean isSelected) {
        this.selected = isSelected;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isNameElement() {
        return nameElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nameElement  visible DOCUMENT ME!
     */
    public void setNameElement(final boolean nameElement) {
        this.nameElement = nameElement;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureServiceAttribute other = (FeatureServiceAttribute)obj;
        if ((this.type != other.type) && ((this.type == null) || !this.type.equals(other.type))) {
            return false;
        }
        if ((this.name != other.name) && ((this.name == null) || !this.name.equals(other.name))) {
            return false;
        }
        if ((this.alias != other.alias) && ((this.alias == null) || !this.alias.equals(other.alias))) {
            return false;
        }
        if (this.geometry != other.geometry) {
            return false;
        }
        if (this.selected != other.selected) {
            return false;
        }
        if (this.visible != other.visible) {
            return false;
        }
        if (this.nameElement != other.nameElement) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (71 * hash) + ((this.type != null) ? this.type.hashCode() : 0);
        hash = (71 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        hash = (71 * hash) + (this.geometry ? 1 : 0);
        hash = (71 * hash) + (this.selected ? 1 : 0);
        return hash;
    }

    @Override
    public Element toElement() {
        final Element featureServiceAttribute = new Element(FeatureServiceAttribute.class.getSimpleName());

        featureServiceAttribute.setAttribute(ConvertableToXML.TYPE_ATTRIBUTE, this.getClass().getCanonicalName());
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_NAME_STRING, getName());
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_ALIAS_STRING, getAlias());
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.IS_GEOMETRY, String.valueOf(isGeometry()));
        featureServiceAttribute.setAttribute(IS_SELECTED, String.valueOf(selected));
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.IS_VISIBLE, String.valueOf(visible));
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.IS_NAME_ELEMENT, String.valueOf(nameElement));
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_TYPE_STRING, getType());
        return featureServiceAttribute;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        if (element.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) == null) {
            log.warn("fromElement: restoring object from deprecarted xml element"); // NOI18N
        }

        this.setName(element.getAttributeValue(FeatureServiceUtilities.XML_NAME_STRING));
        this.setAlias(element.getAttributeValue(FeatureServiceUtilities.XML_ALIAS_STRING));
        this.setType(element.getAttributeValue(FeatureServiceUtilities.XML_TYPE_STRING));

        final boolean newSelected = (element.getAttributeValue(IS_SELECTED) != null)
            ? Boolean.valueOf(element.getAttributeValue(IS_SELECTED)) : true;
        this.setSelected(newSelected);

        final boolean newVisible = (element.getAttributeValue(FeatureServiceUtilities.IS_VISIBLE) != null)
            ? Boolean.valueOf(element.getAttributeValue(FeatureServiceUtilities.IS_VISIBLE)) : true;
        this.setVisible(newVisible);

        final boolean newNameElement = (element.getAttributeValue(FeatureServiceUtilities.IS_NAME_ELEMENT) != null)
            ? Boolean.valueOf(element.getAttributeValue(FeatureServiceUtilities.IS_NAME_ELEMENT)) : false;
        this.setNameElement(newNameElement);

        final boolean newGeometry = (this.getType() != null) && (FeatureServiceUtilities.isElementOfGeometryType(type));
        this.setGeometry(newGeometry);
    }

    @Override
    public FeatureServiceAttribute clone() {
        return new FeatureServiceAttribute(this);
    }
}
