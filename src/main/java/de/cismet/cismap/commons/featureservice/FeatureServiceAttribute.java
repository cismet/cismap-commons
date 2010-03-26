/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.ConvertableToXML;
import org.jdom.Element;

/**
 * Describes the attributes of all Features available in a certain layer.
 *
 * @author Sebastian Puhl
 * @author Pascal Dihé
 */
public class FeatureServiceAttribute implements ConvertableToXML, Cloneable
{
  private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
  //public static final String STRING = "string";
  //public static final String INTEGER = "integer";
  //public static final String GEOMETRY = "geometry";
  public static final String IS_SELECTED = "isSelected";//NOI18N
  private String type;
  private String name;
  private boolean geometry;
  private boolean selected;

  public FeatureServiceAttribute(String name, String type, boolean isSelected)
  {
    setName(name);
    if (type.equals(FeatureServiceUtilities.GEO_PROPERTY_TYPE_WITH_NS) ||
            type.equals(Integer.toString(DocumentFeatureService.GML_GEOMETRY_TYPE)))
    {
      setGeometry(true);
    }
    setSelected(isSelected);
    setType(type);
  }

  public FeatureServiceAttribute(Element e) throws Exception
  {
    this.initFromElement(e);
  }

  protected FeatureServiceAttribute(FeatureServiceAttribute featureServiceAttribute)
  {
    this(new String(featureServiceAttribute.getName()), String.valueOf(featureServiceAttribute.getType()), featureServiceAttribute.isSelected());
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public void setGeometry(boolean isGeometry)
  {
    this.geometry = isGeometry;
  }

  public boolean isGeometry()
  {
    return geometry;
  }

  public boolean isSelected()
  {
    return selected;
  }

  public void setSelected(boolean isSelected)
  {
    this.selected = isSelected;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final FeatureServiceAttribute other = (FeatureServiceAttribute) obj;
    if (this.type != other.type && (this.type == null || !this.type.equals(other.type)))
    {
      return false;
    }
    if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
    {
      return false;
    }
    if (this.geometry != other.geometry)
    {
      return false;
    }
    if (this.selected != other.selected)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 71 * hash + (this.geometry ? 1 : 0);
    hash = 71 * hash + (this.selected ? 1 : 0);
    return hash;
  }

  @Override
  public Element toElement()
  {
    Element featureServiceAttribute = new Element(FeatureServiceAttribute.class.getSimpleName());

    featureServiceAttribute.setAttribute(ConvertableToXML.TYPE_ATTRIBUTE, this.getClass().getCanonicalName());
    featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_NAME_STRING, getName());
    featureServiceAttribute.setAttribute(FeatureServiceUtilities.IS_GEOMETRY, String.valueOf(isGeometry()));
    featureServiceAttribute.setAttribute(IS_SELECTED, String.valueOf(selected));
    featureServiceAttribute.setAttribute(FeatureServiceUtilities.XML_TYPE_STRING, getType());
    return featureServiceAttribute;
  }

  @Override
  public void initFromElement(Element element) throws Exception
  {
    if(element.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) == null)
    {
      log.warn("fromElement: restoring object from deprecarted xml element");//NOI18N
    }

    this.setName(element.getAttributeValue(FeatureServiceUtilities.XML_NAME_STRING));
    this.setType(element.getAttributeValue(FeatureServiceUtilities.XML_TYPE_STRING));
    
    boolean newSelected = element.getAttributeValue(IS_SELECTED) != null ? Boolean.valueOf(element.getAttributeValue(IS_SELECTED)) : true;
    this.setSelected(newSelected);
    
    boolean newGeometry = this.getType() != null && (this.getType().equals(FeatureServiceUtilities.GEO_PROPERTY_TYPE_WITH_NS) || this.getType().equals(Integer.toString(DocumentFeatureService.GML_GEOMETRY_TYPE)));
    this.setGeometry(newGeometry);
  }

  @Override
  public FeatureServiceAttribute clone()
  {
    return new FeatureServiceAttribute(this);
  }
}