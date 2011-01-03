/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;

import javax.swing.JLabel;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class DefaultStyledFeature implements StyledFeature, CloneableFeature, AnnotatedFeature {

    //~ Instance fields --------------------------------------------------------

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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of DefaultStyledFeature.
     */
    public DefaultStyledFeature() {
    }

    /**
     * Creates a new DefaultStyledFeature object.
     *
     * @param  dsf  DOCUMENT ME!
     */
    public DefaultStyledFeature(final DefaultStyledFeature dsf) {
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
        primaryAnnotationVisible = dsf.primaryAnnotationVisible;
        featureAnnotationSymbol = dsf.featureAnnotationSymbol;
        lineWidth = dsf.lineWidth;
        autoScale = dsf.autoScale;
        maxScaleDenominator = dsf.maxScaleDenominator;
        minScaleDenominator = dsf.minScaleDenominator;
        annotationPaint = dsf.annotationPaint;
        highlightingEnabled = dsf.highlightingEnabled;
        justification = dsf.justification;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object clone() {
        final DefaultStyledFeature copy = new DefaultStyledFeature();
        if (geom != null) {
            copy.geom = (Geometry)(geom.clone());
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
        copy.primaryAnnotationVisible = primaryAnnotationVisible;
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

    @Override
    public Paint getFillingPaint() {
        return fillingStyle;
    }

    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        this.fillingStyle = fillingStyle;
    }

    @Override
    public float getTransparency() {
        return transparency;
    }

    @Override
    public void setTransparency(final float transparency) {
        this.transparency = transparency;
    }

    @Override
    public void setGeometry(final com.vividsolutions.jts.geom.Geometry geom) {
        this.geom = geom;
    }

    @Override
    public com.vividsolutions.jts.geom.Geometry getGeometry() {
        return geom;
    }

    @Override
    public java.awt.Paint getLinePaint() {
        return linePaint;
    }

    @Override
    public void setLinePaint(final java.awt.Paint p) {
        linePaint = p;
    }

    @Override
    public boolean canBeSelected() {
        return canBeSelected;
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    @Override
    public void hide(final boolean hiding) {
        this.hiding = hiding;
    }

    @Override
    public boolean isHidden() {
        return hiding;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureAnnotationSymbol  DOCUMENT ME!
     */
    public void setFeatureAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.featureAnnotationSymbol = featureAnnotationSymbol;
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return featureAnnotationSymbol;
    }

    @Override
    public String getPrimaryAnnotation() {
        return primaryAnnotation;
    }

    @Override
    public Font getPrimaryAnnotationFont() {
        return primaryAnnotationFont;
    }

    @Override
    public double getPrimaryAnnotationScaling() {
        return primaryAnnotationScale;
    }

    @Override
    public String getSecondaryAnnotation() {
        return secondaryAnnotation;
    }

    @Override
    public void setPrimaryAnnotation(final String primaryAnnotation) {
        this.primaryAnnotation = primaryAnnotation;
    }

    @Override
    public void setPrimaryAnnotationFont(final Font primaryAnnotationFont) {
        this.primaryAnnotationFont = primaryAnnotationFont;
    }

    @Override
    public void setPrimaryAnnotationScaling(final double primaryAnnotationSize) {
        this.primaryAnnotationScale = primaryAnnotationSize;
    }

    @Override
    public void setSecondaryAnnotation(final String secondaryAnnotation) {
        this.secondaryAnnotation = secondaryAnnotation;
    }

    @Override
    public int getLineWidth() {
        return lineWidth;
    }

    @Override
    public void setLineWidth(final int width) {
        lineWidth = width;
    }

    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.featureAnnotationSymbol = featureAnnotationSymbol;
    }

    @Override
    public Integer getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    @Override
    public Integer getMinScaleDenominator() {
        return minScaleDenominator;
    }

    @Override
    public boolean isAutoscale() {
        return autoScale;
    }

    @Override
    public void setAutoScale(final boolean autoScale) {
        this.autoScale = autoScale;
    }

    @Override
    public void setMaxScaleDenominator(final Integer max) {
        this.maxScaleDenominator = max;
    }

    @Override
    public void setMinScaleDenominator(final Integer min) {
        this.minScaleDenominator = min;
    }

    @Override
    public Paint getPrimaryAnnotationPaint() {
        return annotationPaint;
    }

    @Override
    public void setPrimaryAnnotationPaint(final Paint primaryAnnotationPaint) {
        annotationPaint = primaryAnnotationPaint;
    }

    @Override
    public boolean isHighlightingEnabled() {
        return highlightingEnabled;
    }

    @Override
    public void setHighlightingEnabled(final boolean enabled) {
        highlightingEnabled = enabled;
    }

    @Override
    public float getPrimaryAnnotationJustification() {
        return justification;
    }

    @Override
    public void setPrimaryAnnotationJustification(final float just) {
        justification = just;
    }

    @Override
    public boolean isPrimaryAnnotationVisible() {
        return primaryAnnotationVisible;
    }

    @Override
    public void setPrimaryAnnotationVisible(final boolean visible) {
        primaryAnnotationVisible = visible;
    }
}
