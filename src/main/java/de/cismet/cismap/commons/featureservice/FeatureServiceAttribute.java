/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import org.jdom.Element;

/**
 *
 * @author spuhl
 */
public class FeatureServiceAttribute {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    //public static final String STRING = "string";
    //public static final String INTEGER = "integer";
    //public static final String GEOMETRY = "geometry";
    public static final String IS_SELECTED = "isSelected";
    private String type;
    private String name;
    private boolean isGeometry;
    private boolean isSelected;

    public FeatureServiceAttribute(String name, String type, boolean isSelected) throws Exception {
        setName(name);
        if (type.equals(FeatureServiceUtilities.GEO_PROPERTY_TYPE_WITH_NS) ||
                type.equals(Integer.toString(DocumentFeatureService.GML_GEOMETRY_TYPE))) {
            setIsGeometry(true);
        }
        setIsSelected(isSelected);
        setType(type);
    }

    public FeatureServiceAttribute(Element e) throws Exception {
        this(e.getAttributeValue(FeatureServiceUtilities.XML_NAME_STRING),
                e.getAttributeValue(FeatureServiceUtilities.XML_TYPE_STRING),
                (e.getAttributeValue(IS_SELECTED) != null ? new Boolean(e.getAttributeValue(IS_SELECTED)) : new Boolean(true)));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIsGeometry(boolean isGeometry) {
        this.isGeometry = isGeometry;
    }

    public boolean isGeometry() {
        return isGeometry;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Element getElement() {
        Element featureServiceAttribute = new Element(FeatureServiceAttribute.class.getSimpleName());
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_NAME_STRING, getName());
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.IS_GEOMETRY, new Boolean(isGeometry()).toString());
        featureServiceAttribute.setAttribute(IS_SELECTED, new Boolean(isSelected).toString());
        featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_TYPE_STRING, getType());
        return featureServiceAttribute;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureServiceAttribute other = (FeatureServiceAttribute) obj;
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.isGeometry != other.isGeometry) {
            return false;
        }
        if (this.isSelected != other.isSelected) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 71 * hash + (this.isGeometry ? 1 : 0);
        hash = 71 * hash + (this.isSelected ? 1 : 0);
        return hash;
    }
}
