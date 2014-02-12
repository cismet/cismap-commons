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
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * Default implementation of a FeatureServiceFeature.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class DefaultFeatureServiceFeature implements FeatureServiceFeature {

    //~ Instance fields --------------------------------------------------------

    protected Logger logger = Logger.getLogger(this.getClass());

    // private final static org.apache.log4j.Logger logger =
    // org.apache.log4j.Logger.getLogger(DefaultFeatureServiceFeature.class);
    private int id = -1;
    private String primaryAnnotation;
    private String secondaryAnnotation;
    private boolean hiding = false;
    private boolean editable = false;
    private boolean canBeSelected = true;
    private LinkedHashMap<String, Object> container = new LinkedHashMap<String, Object>();
    private Geometry geometry = null;
    private LayerProperties layerProperties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new uninitialised instance of DefaultFeatureServiceFeature. The id is set to -1, editable is set to
     * false, canBeSelected is set to true, hiding is set to false, any other properties set to null.
     */
    public DefaultFeatureServiceFeature() {
    }

    /**
     * Initialises a new DefaultFeatureServiceFeature instance from an existing FeatureServiceFeature object. The
     * properties of the FeatureServiceFeature will be cloned.
     *
     * @param  feature  layerProperties LayerProperties to be used for initialisation
     *
     * @see    FeatureServiceFeature#clone(Object)
     */
    public DefaultFeatureServiceFeature(final FeatureServiceFeature feature) {
        this.setId(id);
        this.setPrimaryAnnotation(new String(feature.getPrimaryAnnotation()));
        this.setSecondaryAnnotation(new String(feature.getSecondaryAnnotation()));
        this.hide(feature.isHidden());
        this.setEditable(feature.isEditable());
        this.setCanBeSelected(feature.canBeSelected());

        if ((feature.getProperties() != null) && (feature.getProperties().size() > 0)) {
            // TODO: deep cloning of hashmap?
            this.container = new LinkedHashMap(feature.getProperties());
        }

        if (feature.getGeometry() != null) {
            this.setGeometry((Geometry)feature.getGeometry().clone());
        }

        if (feature.getLayerProperties() != null) {
            this.setLayerProperties((LayerProperties)feature.getLayerProperties().clone());
        }
    }

    /**
     * Creates a new initialised instance of DefaultFeatureServiceFeature. Editable is set to false, canBeSelected is
     * set to true, hiding is set to false.
     *
     * @param  id               the unique (within the layer or the feature collection) id of the feature
     * @param  geometry         the geometry of the feature
     * @param  layerProperties  (shared) layer properties object
     */
    public DefaultFeatureServiceFeature(final int id, final Geometry geometry, final LayerProperties layerProperties) {
        this.setId(id);
        this.setGeometry(geometry);
        this.setLayerProperties(layerProperties);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object clone() {
        return new DefaultFeatureServiceFeature(this);
    }

    /**
     * Fügt das übergebene Objekt dem PropertyContainer unter gegebenem Namen ein.
     *
     * @param  propertyName  Name und gleichzeitig Schlüssel
     * @param  property      einzufügendes Objekt
     */
    @Override
    public void addProperty(final String propertyName, final Object property) {
        container.put(propertyName, property);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  HashMap mit Properties
     */
    @Override
    public HashMap getProperties() {
        return container;
    }

    /**
     * Ersetzt den alten PropertieContainer mit einer neuen HashMap.
     *
     * @param  properties  neue Hashmap
     */
    @Override
    public void setProperties(final HashMap properties) {
        container = new LinkedHashMap(properties);
    }

    /**
     * Liefert die dem Namen zugeordnete Property.
     *
     * @param   propertyName  Name des gesuchten Objekts
     *
     * @return  Objekt aus der Hashmap
     */
    @Override
    public Object getProperty(final String propertyName) {
        return container.get(propertyName);
    }

    /**
     * Entfernt die dem Namen zugeordnete Property aus der Hashmap.
     *
     * @param  propertyName  Name des zu löschenden Objekts
     */
    @Override
    public void removeProperty(final String propertyName) {
        container.remove(propertyName);
    }

    /**
     * Liefert die ID des DefaultWFSFeatures.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Setzt die ID des DefaultWFSFeatures neu.
     *
     * @param  id  neue ID
     */
    @Override
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Liefert den ID-Ausdruck des DefaultWFSFeatures.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getIdExpression() {
        return this.layerProperties.getIdExpression();
    }

    /**
     * Setzt den ID-Ausdruck des DefaultWFSFeatures neu.
     *
     * @param  idExpression  neuer ID-Ausdruck
     */
    @Override
    public void setIdExpression(final String idExpression) {
        this.layerProperties.setIdExpression(idExpression, layerProperties.getIdExpressionType());
    }

//  /**
//   * Erzeugt ein JDOM-Element, das das DefaultFeatureServiceFeature und dessen Attribute
//   * widerspiegelt.
//   * @return JDOM-Element
//   */
//  @Override
//  public Element toElement()
//  {
//    Element element = new Element("DefaultFeatureServiceFeature");
//    element.setAttribute("id", new Integer(getId()).toString());
//    element.setAttribute("idExpression", getIdExpression());
//    element.setAttribute("lineWidth", new Integer(getLineWidth()).toString());
//    element.setAttribute("transparency", new Float(getTransparency()).toString());
//    element.setAttribute("highlightingEnabled", new Boolean(isHighlightingEnabled()).toString());
//    element.setAttribute("primaryAnnotation", getPrimaryAnnotation());
//    element.setAttribute("primaryAnnotationScaling", new Double(getPrimaryAnnotationScaling()).toString());
//    element.setAttribute("primaryAnnotationJustification", new Float(getPrimaryAnnotationJustification()).toString());
//    element.setAttribute("minScaleDenominator", getMinScaleDenominator().toString());
//    element.setAttribute("maxScaleDenominator", getMaxScaleDenominator().toString());
//    element.setAttribute("autoscale", new Boolean(isAutoscale()).toString());
//    if (getFillingPaint() != null)
//    {
//      element.addContent(new Element("fillingColor").addContent(StaticXMLTools.convertColorToXML((Color) getFillingPaint())));
//    }
//    if (getLinePaint() != null)
//    {
//      element.addContent(new Element("lineColor").addContent(StaticXMLTools.convertColorToXML((Color) getLinePaint())));
//    }
//    element.addContent(new Element("primaryAnnotationFont").addContent(StaticXMLTools.convertFontToXML(getPrimaryAnnotationFont())));
//    element.addContent(new Element("primaryAnnotationColor").addContent(StaticXMLTools.convertColorToXML((Color) getPrimaryAnnotationPaint())));
//    return element;
//  }
//  @Override
//  public void initFromElement(Element element) throws Exception
//  {
//    throw new UnsupportedOperationException("Not supported yet.");
//
//  }
    @Override
    public LayerProperties getLayerProperties() {
        return this.layerProperties;
    }

    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
    }

    @Override
    public Paint getLinePaint() {
        return this.getStyle().isDrawLine() ? this.getStyle().getLineColor() : null;
    }

    @Override
    public void setLinePaint(final Paint linePaint) {
        this.getStyle().setLineColor((Color)linePaint);
        this.getStyle().setDrawLine(true);
    }

    @Override
    public int getLineWidth() {
        return this.getStyle().getLineWidth();
    }

    @Override
    public void setLineWidth(final int width) {
        this.getStyle().setLineWidth(width);
    }

    @Override
    public Paint getFillingPaint() {
        return this.getStyle().isDrawFill() ? this.getStyle().getFillColor() : null;
    }

    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        this.getStyle().setFillColor((Color)fillingStyle);
        this.getStyle().setDrawFill(true);
    }

    @Override
    public float getTransparency() {
        return this.getStyle().getAlpha();
    }

    @Override
    public void setTransparency(final float transparrency) {
        this.getStyle().setAlpha(transparrency);
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return this.getStyle().getPointSymbol();
    }

    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.getStyle().setPointSymbol(featureAnnotationSymbol);
    }

    @Override
    public boolean isHighlightingEnabled() {
        return this.getStyle().isHighlightFeature();
    }

    @Override
    public void setHighlightingEnabled(final boolean enabled) {
        this.getStyle().setHighlightFeature(enabled);
    }

    @Override
    public Geometry getGeometry() {
        return this.geometry;
    }

    @Override
    public void setGeometry(final Geometry geom) {
        this.geometry = geom;
    }

    @Override
    public boolean canBeSelected() {
        return this.canBeSelected;
    }

    @Override
    public boolean isEditable() {
        return this.editable;
    }

    @Override
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isHidden() {
        return this.hiding;
    }

    @Override
    public void hide(final boolean hiding) {
        this.hiding = hiding;
    }

    @Override
    public String getPrimaryAnnotation() {
        return this.primaryAnnotation;
    }

    @Override
    public boolean isPrimaryAnnotationVisible() {
        return this.getStyle().isDrawLabel();
    }

    @Override
    public void setPrimaryAnnotationVisible(final boolean visible) {
        this.getStyle().setDrawLabel(visible);
    }

    @Override
    public Font getPrimaryAnnotationFont() {
        return this.getStyle().getFont();
    }

    @Override
    public Paint getPrimaryAnnotationPaint() {
        return this.getStyle().getFontColor();
    }

    @Override
    public double getPrimaryAnnotationScaling() {
        return this.getStyle().getMultiplier();
    }

    @Override
    public float getPrimaryAnnotationJustification() {
        return this.getStyle().getAlignment();
    }

    @Override
    public void setPrimaryAnnotationJustification(final float just) {
        this.getStyle().setAlignment(just);
    }

    @Override
    public String getSecondaryAnnotation() {
        return this.secondaryAnnotation;
    }

    @Override
    public void setPrimaryAnnotation(final String primaryAnnotation) {
        this.primaryAnnotation = primaryAnnotation;
    }

    @Override
    public void setPrimaryAnnotationFont(final Font primaryAnnotationFont) {
        this.getStyle().setFont(primaryAnnotationFont);
    }

    @Override
    public void setPrimaryAnnotationPaint(final Paint primaryAnnotationPaint) {
        this.getStyle().setFontColor((Color)primaryAnnotationPaint);
    }

    @Override
    public void setPrimaryAnnotationScaling(final double primaryAnnotationScaling) {
        this.getStyle().setMultiplier(primaryAnnotationScaling);
    }

    @Override
    public void setSecondaryAnnotation(final String secondaryAnnotation) {
        this.secondaryAnnotation = secondaryAnnotation;
    }

    @Override
    public boolean isAutoscale() {
        return this.getStyle().isAutoscale();
    }

    @Override
    public void setAutoScale(final boolean autoScale) {
        this.getStyle().setAutoscale(autoScale);
    }

    @Override
    public Integer getMinScaleDenominator() {
        return this.getStyle().getMinScale();
    }

    @Override
    public Integer getMaxScaleDenominator() {
        return this.getStyle().getMaxScale();
    }

    @Override
    public void setMinScaleDenominator(final Integer min) {
        this.getStyle().setMinScale(min);
    }

    @Override
    public void setMaxScaleDenominator(final Integer max) {
        this.getStyle().setMaxScale(max);
    }

    @Override
    public void setPrimaryAnnotationHalo(final Color halo) {
        this.getStyle().setHalo(halo);
    }

    @Override
    public Color getPrimaryAnnotationHalo() {
        return this.getStyle().getHalo();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Style getStyle() {
        return this.layerProperties.getStyle();
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj.getClass().getName().equals(getClass().getName())) {
            final DefaultFeatureServiceFeature other = (DefaultFeatureServiceFeature)obj;

            if (getId() != -1) {
                return getId() == other.getId();
            }
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (79 * hash) + this.id;
        hash = (79 * hash) + ((this.getClass().getName() != null) ? this.getClass().getName().hashCode() : 0);
        return hash;
    }
}
