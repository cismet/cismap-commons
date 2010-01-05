/*
 * DefaultStyledFeature.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 12. Juli 2005, 12:43
 *
 */
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import javax.swing.JLabel;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class DefaultStyledFeature implements StyledFeature, CloneableFeature, AnnotatedFeature {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private com.vividsolutions.jts.geom.Geometry geom = null;
    private Paint fillingStyle = new Color(0.5f, 0.5f, 0.5f, 0.1f);
    private Paint linePaint = Color.black;
    private Paint annotationPaint = Color.BLACK;
    private float transparency = 1f;
    private boolean editable = false;
    private boolean hiding = false;
    private boolean canBeSelected = true;
    private String primaryAnnotation = null;
    private String secondaryAnnotation = null;
    private Font primaryAnnotationFont = null;
    private double primaryAnnotationScale = 1.0;
    private FeatureAnnotationSymbol featureAnnotationSymbol = null;
    private int lineWidth;
    private Integer maxScaleDenominator;
    private Integer minScaleDenominator;
    private boolean autoScale;
    private boolean highlightingEnabled;
    private float justification = JLabel.LEFT_ALIGNMENT;
    private boolean primaryAnnotationVisible = true;

    /** Creates a new instance of DefaultStyledFeature */
    public DefaultStyledFeature() {

    }

    public DefaultStyledFeature(DefaultStyledFeature dsf) {
        geom = dsf.geom;
        fillingStyle = dsf.fillingStyle;
        linePaint = dsf.linePaint;
        transparency = dsf.transparency;
        editable = dsf.editable;
        hiding = dsf.hiding;
        canBeSelected = dsf.canBeSelected;
        primaryAnnotation = dsf.primaryAnnotation;
        secondaryAnnotation = dsf.secondaryAnnotation;
        primaryAnnotationFont = dsf.primaryAnnotationFont;
        primaryAnnotationScale = dsf.primaryAnnotationScale;
        primaryAnnotationVisible=dsf.primaryAnnotationVisible;
        featureAnnotationSymbol = dsf.featureAnnotationSymbol;
        lineWidth = dsf.lineWidth;
        autoScale = dsf.autoScale;
        maxScaleDenominator = dsf.maxScaleDenominator;
        minScaleDenominator = dsf.minScaleDenominator;
        annotationPaint = dsf.annotationPaint;
        highlightingEnabled = dsf.highlightingEnabled;
        justification = dsf.justification;
    }

    public Object clone() {
        DefaultStyledFeature copy = new DefaultStyledFeature();
        if (geom != null) {
            copy.geom = (Geometry) (geom.clone());
        }
        copy.fillingStyle = fillingStyle;
        copy.linePaint = linePaint;
        copy.transparency = transparency;
        copy.editable = editable;
        copy.hiding = hiding;
        copy.canBeSelected = canBeSelected;
        copy.primaryAnnotation = primaryAnnotation;
        copy.secondaryAnnotation = secondaryAnnotation;
        copy.primaryAnnotationFont = primaryAnnotationFont;
        copy.primaryAnnotationScale = primaryAnnotationScale;
        copy.primaryAnnotationVisible=primaryAnnotationVisible;
        copy.featureAnnotationSymbol = featureAnnotationSymbol;
        copy.autoScale = autoScale;
        copy.lineWidth = lineWidth;
        copy.minScaleDenominator = minScaleDenominator;
        copy.maxScaleDenominator = maxScaleDenominator;
        copy.annotationPaint = annotationPaint;
        copy.highlightingEnabled = highlightingEnabled;
        copy.justification = justification;
        return copy;
    }

    public Paint getFillingPaint() {
        return fillingStyle;
    }

    public void setFillingPaint(Paint fillingStyle) {
        this.fillingStyle = fillingStyle;
    }

    public float getTransparency() {
        return transparency;
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
    }

    public void setGeometry(com.vividsolutions.jts.geom.Geometry geom) {
        this.geom = geom;
    }

    public com.vividsolutions.jts.geom.Geometry getGeometry() {
        return geom;
    }

    public java.awt.Paint getLinePaint() {
        return linePaint;
    }

    public void setLinePaint(java.awt.Paint p) {
        linePaint = p;
    }

    public boolean canBeSelected() {
        return canBeSelected;
    }

    public void setCanBeSelected(boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

  @Override
    public void hide(boolean hiding) {
        this.hiding = hiding;
    }

    public boolean isHidden() {
        return hiding;
    }

    public void setFeatureAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.featureAnnotationSymbol = featureAnnotationSymbol;
    }

    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return featureAnnotationSymbol;
    }

    public String getPrimaryAnnotation() {
        return primaryAnnotation;
    }

    public Font getPrimaryAnnotationFont() {
        return primaryAnnotationFont;
    }

    public double getPrimaryAnnotationScaling() {
        return primaryAnnotationScale;
    }

    public String getSecondaryAnnotation() {
        return secondaryAnnotation;
    }

    public void setPrimaryAnnotation(String primaryAnnotation) {
        this.primaryAnnotation = primaryAnnotation;
    }

    public void setPrimaryAnnotationFont(Font primaryAnnotationFont) {
        this.primaryAnnotationFont = primaryAnnotationFont;
    }

    public void setPrimaryAnnotationScaling(double primaryAnnotationSize) {
        this.primaryAnnotationScale = primaryAnnotationSize;
    }

    public void setSecondaryAnnotation(String secondaryAnnotation) {
        this.secondaryAnnotation = secondaryAnnotation;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int width) {
        lineWidth = width;
    }

    public void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.featureAnnotationSymbol = featureAnnotationSymbol;
    }

    public Integer getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    public Integer getMinScaleDenominator() {
        return minScaleDenominator;
    }

    public boolean isAutoscale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public void setMaxScaleDenominator(Integer max) {
        this.maxScaleDenominator = max;
    }

    public void setMinScaleDenominator(Integer min) {
        this.minScaleDenominator = min;
    }

    public Paint getPrimaryAnnotationPaint() {
        return annotationPaint;
    }

    public void setPrimaryAnnotationPaint(Paint primaryAnnotationPaint) {
        annotationPaint = primaryAnnotationPaint;
    }

    public boolean isHighlightingEnabled() {
        return highlightingEnabled;
    }

    public void setHighlightingEnabled(boolean enabled) {
        highlightingEnabled = enabled;
    }

    public float getPrimaryAnnotationJustification() {

        return justification;
    }

    public void setPrimaryAnnotationJustification(float just) {        
        justification = just;
    }

    public boolean isPrimaryAnnotationVisible() {        
        return primaryAnnotationVisible;
    }

  @Override
    public void setPrimaryAnnotationVisible(boolean visible) {        
        primaryAnnotationVisible = visible;
    }
}
