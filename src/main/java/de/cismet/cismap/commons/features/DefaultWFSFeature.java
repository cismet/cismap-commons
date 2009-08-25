/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.PropertyContainer;
import de.cismet.tools.StaticXMLTools;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.jdom.Element;

/**
 *
 * @author thorsten
 */
public class DefaultWFSFeature extends DefaultStyledFeature implements PropertyContainer, AnnotatedByPropertyFeature, FeatureWithId, ConvertableToXML {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    
    private LinkedHashMap<String, Object> container = new LinkedHashMap<String, Object>();
    private int id;
    private String idExpression;   

    /**
     * @return eine Kopie des DefaultWFSFeature.
     */
    @Override
    public Object clone() {
        DefaultStyledFeature dsf = (DefaultStyledFeature) super.clone();
        DefaultWFSFeature dwfsFeature = new DefaultWFSFeature(dsf);
        dwfsFeature.container = new LinkedHashMap(container);
        dwfsFeature.id = id;
        dwfsFeature.idExpression = idExpression;
        return dwfsFeature;
    }

    /**
     * Fügt das übergebene Objekt dem PropertyContainer unter gegebenem Namen ein.
     * @param propertyName Name und gleichzeitig Schlüssel
     * @param property einzufügendes Objekt
     */
    public void addProperty(String propertyName, Object property) {
        container.put(propertyName, property);
    }

    /**
     * Konstruktor für DefaultWFSFeatures.
     */
    public DefaultWFSFeature() {
        super();
        setCanBeSelected(false);
    }

    /**
     * Konstruktor für DefaultWFSFeatures aus einem DefaultStyledFeature.
     * @param dsf DefaultStyledFeature
     */
    private DefaultWFSFeature(DefaultStyledFeature dsf) {
        super(dsf);
    }

    /**
     * @return HashMap mit Properties
     */
    public HashMap getProperties() {
        return container;
    }

    /**
     * Ersetzt den alten PropertieContainer mit einer neuen HashMap.
     * @param properties neue Hashmap
     */
    public void setProperties(HashMap properties) {
        container = new LinkedHashMap(properties);
    }

    /**
     * Liefert die dem Namen zugeordnete Property.
     * @param propertyName Name des gesuchten Objekts
     * @return Objekt aus der Hashmap
     */
    public Object getProperty(String propertyName) {
        return container.get(propertyName);
    }

    /**
     * Entfernt die dem Namen zugeordnete Property aus der Hashmap.
     * @param propertyName Name des zu löschenden Objekts
     */
    public void removeProperty(String propertyName) {
        container.remove(propertyName);
    }

    /**
     * Liefert die ID des DefaultWFSFeatures.
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die ID des DefaultWFSFeatures neu.
     * @param id neue ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Liefert den ID-Ausdruck des DefaultWFSFeatures.
     */
    public String getIdExpression() {
        return idExpression;
    }

    /**
     * Setzt den ID-Ausdruck des DefaultWFSFeatures neu.
     * @param idExpression neuer ID-Ausdruck
     */
    public void setIdExpression(String idExpression) {
        this.idExpression = idExpression;
    }

    /**
     * Erzeugt ein JDOM-Element, das das DefaultWFSFeature und dessen Attribute
     * widerspiegelt.
     * @return JDOM-Element
     */
    public Element getElement() {
        Element element = new Element("DefaultWFSFeature");
        element.setAttribute("id", new Integer(getId()).toString());
        element.setAttribute("idExpression", getIdExpression());
        element.setAttribute("lineWidth", new Integer(getLineWidth()).toString());
        element.setAttribute("transparency", new Float(getTransparency()).toString());
        element.setAttribute("highlightingEnabled", new Boolean(isHighlightingEnabled()).toString());
        element.setAttribute("primaryAnnotation", getPrimaryAnnotation());
        element.setAttribute("primaryAnnotationScaling", new Double(getPrimaryAnnotationScaling()).toString());
        element.setAttribute("primaryAnnotationJustification", new Float(getPrimaryAnnotationJustification()).toString());
        element.setAttribute("minScaleDenominator", getMinScaleDenominator().toString());
        element.setAttribute("maxScaleDenominator", getMaxScaleDenominator().toString());
        element.setAttribute("autoscale", new Boolean(isAutoscale()).toString());
        element.addContent(new Element("fillingColor").addContent(StaticXMLTools.convertColorToXML((Color) getFillingPaint())));
        element.addContent(new Element("lineColor").addContent(StaticXMLTools.convertColorToXML((Color) getLinePaint())));
        element.addContent(new Element("primaryAnnotationFont").addContent(StaticXMLTools.convertFontToXML(getPrimaryAnnotationFont())));
        element.addContent(new Element("primaryAnnotationColor").addContent(StaticXMLTools.convertColorToXML((Color) getPrimaryAnnotationPaint())));
        return element;
    }

}
