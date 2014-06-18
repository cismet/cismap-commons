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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
    private Boolean canBeSelected = null;
    private LinkedHashMap<String, Object> container = new LinkedHashMap<String, Object>();
    private Geometry geometry = null;
    private LayerProperties layerProperties;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
    /**
     * /** * Erzeugt ein JDOM-Element, das das DefaultFeatureServiceFeature und dessen Attribute * widerspiegelt. *
     * &#064;return JDOM-Element
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public LayerProperties getLayerProperties() {
        return this.layerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerProperties  DOCUMENT ME!
     */
    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getLinePaint() {
        return this.getStyle().isDrawLine() ? this.getStyle().getLineColor() : null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  linePaint  DOCUMENT ME!
     */
    @Override
    public void setLinePaint(final Paint linePaint) {
        this.getStyle().setLineColor((Color)linePaint);
        this.getStyle().setDrawLine(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getLineWidth() {
        return this.getStyle().getLineWidth();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  width  DOCUMENT ME!
     */
    @Override
    public void setLineWidth(final int width) {
        this.getStyle().setLineWidth(width);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getFillingPaint() {
        return this.getStyle().isDrawFill() ? this.getStyle().getFillColor() : null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fillingStyle  DOCUMENT ME!
     */
    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        this.getStyle().setFillColor((Color)fillingStyle);
        this.getStyle().setDrawFill(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public float getTransparency() {
        return this.getStyle().getAlpha();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  transparrency  DOCUMENT ME!
     */
    @Override
    public void setTransparency(final float transparrency) {
        this.getStyle().setAlpha(transparrency);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return this.getStyle().getPointSymbol();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureAnnotationSymbol  DOCUMENT ME!
     */
    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.getStyle().setPointSymbol(featureAnnotationSymbol);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isHighlightingEnabled() {
        return this.getStyle().isHighlightFeature();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    @Override
    public void setHighlightingEnabled(final boolean enabled) {
        this.getStyle().setHighlightFeature(enabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Geometry getGeometry() {
        return this.geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    @Override
    public void setGeometry(final Geometry geom) {
        this.geometry = geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean canBeSelected() {
        if (canBeSelected == null) {
            if (layerProperties != null && layerProperties.getFeatureService() != null) {
                return layerProperties.getFeatureService().isSelectable();
            } else {
                return true;
            }
        } else {
            return canBeSelected.booleanValue();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isEditable() {
        return this.editable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  editable  DOCUMENT ME!
     */
    @Override
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isHidden() {
        return this.hiding;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hiding  DOCUMENT ME!
     */
    @Override
    public void hide(final boolean hiding) {
        this.hiding = hiding;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getPrimaryAnnotation() {
        return this.primaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isPrimaryAnnotationVisible() {
        return this.getStyle().isDrawLabel();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationVisible(final boolean visible) {
        this.getStyle().setDrawLabel(visible);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Font getPrimaryAnnotationFont() {
        return this.getStyle().getFont();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getPrimaryAnnotationPaint() {
        return this.getStyle().getFontColor();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public double getPrimaryAnnotationScaling() {
        return this.getStyle().getMultiplier();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public float getPrimaryAnnotationJustification() {
        return this.getStyle().getAlignment();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  just  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationJustification(final float just) {
        this.getStyle().setAlignment(just);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getSecondaryAnnotation() {
        return this.secondaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotation  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotation(final String primaryAnnotation) {
        this.primaryAnnotation = primaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationFont  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationFont(final Font primaryAnnotationFont) {
        this.getStyle().setFont(primaryAnnotationFont);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationPaint  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationPaint(final Paint primaryAnnotationPaint) {
        this.getStyle().setFontColor((Color)primaryAnnotationPaint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationScaling  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationScaling(final double primaryAnnotationScaling) {
        this.getStyle().setMultiplier(primaryAnnotationScaling);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  secondaryAnnotation  DOCUMENT ME!
     */
    @Override
    public void setSecondaryAnnotation(final String secondaryAnnotation) {
        this.secondaryAnnotation = secondaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isAutoscale() {
        return this.getStyle().isAutoscale();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  autoScale  DOCUMENT ME!
     */
    @Override
    public void setAutoScale(final boolean autoScale) {
        this.getStyle().setAutoscale(autoScale);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Integer getMinScaleDenominator() {
        return this.getStyle().getMinScale();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Integer getMaxScaleDenominator() {
        return this.getStyle().getMaxScale();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  min  DOCUMENT ME!
     */
    @Override
    public void setMinScaleDenominator(final Integer min) {
        this.getStyle().setMinScale(min);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  max  DOCUMENT ME!
     */
    @Override
    public void setMaxScaleDenominator(final Integer max) {
        this.getStyle().setMaxScale(max);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  halo  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationHalo(final Color halo) {
        this.getStyle().setHalo(halo);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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

    /**
     * DOCUMENT ME!
     *
     * @param  canBeSelected  DOCUMENT ME!
     */
    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = (79 * hash) + this.id;
        hash = (79 * hash) + ((this.getClass().getName() != null) ? this.getClass().getName().hashCode() : 0);
        return hash;
    }

    /**
     * Ändert das dem Namen zugeordnete Property.
     *
     * @param  propertyName   Name des gesuchten Objekts
     * @param  propertyValue  neuer Wert des Properties
     */
    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
        final Object oldValue = container.get(propertyName);
        container.put(propertyName, propertyValue);

        propertyChangeSupport.firePropertyChange(propertyName, oldValue, propertyValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception                      DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void saveChanges() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void undoAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a new PropertyChangeListener.
     *
     * @param  l  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove the given PropertyChangeListener.
     *
     * @param  l  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * fires a propertyChange event.
     *
     * @param  propertyName  the name of the changed property
     * @param  oldValue      the old value
     * @param  newValue      the new value
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
