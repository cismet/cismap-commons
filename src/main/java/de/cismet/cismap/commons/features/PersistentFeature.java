/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

import org.deegree.style.se.unevaluated.Style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.util.FilePersistenceManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PersistentFeature implements FeatureServiceFeature {

    //~ Instance fields --------------------------------------------------------

    private int id;
    private LayerProperties layerProperties;
    private long containerId;
    private long geometryId;
    private String geomField = null;
    private FilePersistenceManager pm;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistentFeature object.
     */
    public PersistentFeature() {
    }

    /**
     * Creates a new PersistentFeature object.
     *
     * @param  f   DOCUMENT ME!
     * @param  pm  DOCUMENT ME!
     */
    public PersistentFeature(final FeatureServiceFeature f, final FilePersistenceManager pm) {
        id = f.getId();
        layerProperties = f.getLayerProperties();
        this.pm = pm;
        containerId = pm.save(f.getProperties());
        geomField = getGeometryFieldName();

        if (geomField == null) {
            geometryId = pm.save(f.getGeometry());
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGeometryFieldName() {
        if ((getLayerProperties() != null) && (getLayerProperties().getFeatureService() != null)
                    && (getLayerProperties().getFeatureService().getFeatureServiceAttributes() != null)) {
            final Map<String, FeatureServiceAttribute> attributes = getLayerProperties().getFeatureService()
                        .getFeatureServiceAttributes();

            for (final String key : attributes.keySet()) {
                final FeatureServiceAttribute attr = attributes.get(key);

                if (attr.isGeometry()) {
                    return attr.getName();
                }
            }
        }
        return "geom";
    }

    @Override
    public Object clone() {
        final PersistentFeature f = new PersistentFeature();
        f.id = id;
        f.pm = pm;
        f.layerProperties = layerProperties;
        f.containerId = containerId;
        f.geometryId = geometryId;
        return f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FilePersistenceManager getPersistenceManager() {
        return pm;
    }

    @Override
    public Geometry getGeometry() {
        if (geomField == null) {
            return (Geometry)pm.load(geometryId);
        } else {
            return (Geometry)getProperties().get(geomField);
        }
    }

    @Override
    public void setGeometry(final Geometry geom) {
    }

    @Override
    public HashMap getProperties() {
        return (HashMap)pm.load(containerId);
    }

    @Override
    public void setProperties(final HashMap properties) {
    }

    @Override
    public void addProperty(final String propertyName, final Object property) {
    }

    @Override
    public void removeProperty(final String propertyName) {
    }

    @Override
    public Object getProperty(final String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public LayerProperties getLayerProperties() {
        return layerProperties;
    }

    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
    }

    @Override
    public Paint getLinePaint() {
        return Color.BLACK;
    }

    @Override
    public void setLinePaint(final Paint linePaint) {
    }

    @Override
    public int getLineWidth() {
        return 1;
    }

    @Override
    public void setLineWidth(final int width) {
    }

    @Override
    public Paint getFillingPaint() {
        return Color.BLACK;
    }

    @Override
    public void setFillingPaint(final Paint fillingStyle) {
    }

    @Override
    public float getTransparency() {
        return 1.0f;
    }

    @Override
    public void setTransparency(final float transparrency) {
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return null;
    }

    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
    }

    @Override
    public boolean isHighlightingEnabled() {
        return true;
    }

    @Override
    public void setHighlightingEnabled(final boolean enabled) {
    }

    @Override
    public boolean canBeSelected() {
        return true;
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public void setEditable(final boolean editable) {
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void hide(final boolean hiding) {
    }

    @Override
    public String getPrimaryAnnotation() {
        return null;
    }

    @Override
    public boolean isPrimaryAnnotationVisible() {
        return false;
    }

    @Override
    public void setPrimaryAnnotationVisible(final boolean visible) {
    }

    @Override
    public Font getPrimaryAnnotationFont() {
        return null;
    }

    @Override
    public Paint getPrimaryAnnotationPaint() {
        return Color.BLACK;
    }

    @Override
    public double getPrimaryAnnotationScaling() {
        return 1.0;
    }

    @Override
    public float getPrimaryAnnotationJustification() {
        return 0.0f;
    }

    @Override
    public void setPrimaryAnnotationJustification(final float just) {
    }

    @Override
    public String getSecondaryAnnotation() {
        return null;
    }

    @Override
    public void setPrimaryAnnotation(final String primaryAnnotation) {
    }

    @Override
    public void setPrimaryAnnotationFont(final Font primaryAnnotationFont) {
    }

    @Override
    public void setPrimaryAnnotationPaint(final Paint primaryAnnotationPaint) {
    }

    @Override
    public void setPrimaryAnnotationScaling(final double primaryAnnotationScaling) {
    }

    @Override
    public void setSecondaryAnnotation(final String secondaryAnnotation) {
    }

    @Override
    public boolean isAutoscale() {
        return true;
    }

    @Override
    public void setAutoScale(final boolean autoScale) {
    }

    @Override
    public Integer getMinScaleDenominator() {
        return 1;
    }

    @Override
    public Integer getMaxScaleDenominator() {
        return 1;
    }

    @Override
    public void setMinScaleDenominator(final Integer min) {
    }

    @Override
    public void setMaxScaleDenominator(final Integer max) {
    }

    @Override
    public void setPrimaryAnnotationHalo(final Color paint) {
    }

    @Override
    public Color getPrimaryAnnotationHalo() {
        return Color.BLACK;
    }

    @Override
    public void setId(final int id) {
    }

    @Override
    public String getIdExpression() {
        return null;
    }

    @Override
    public void setIdExpression(final String idExpression) {
    }

    @Override
    public void applyStyle(final PFeature pFeature, final WorldToScreenTransform wtst) {
    }

    @Override
    public void setSLDStyles(final List<Style> style) {
    }

    @Override
    public String getName() {
        return "";
    }
}
