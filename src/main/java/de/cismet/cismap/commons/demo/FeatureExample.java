/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FeatureExample.java
 *
 * Created on 4. M\u00E4rz 2005, 17:18
 */
package de.cismet.cismap.commons.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import de.cismet.cismap.commons.features.AnnotatedFeature;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.styling.TextStyle;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class FeatureExample implements AnnotatedFeature {

    //~ Instance fields --------------------------------------------------------

    private com.vividsolutions.jts.geom.Geometry geom = null;
    private String name = ""; // NOI18N
    private int art = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FeatureExample.
     */
    public FeatureExample() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public com.vividsolutions.jts.geom.Geometry getGeometry() {
        return geom;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    public void setGeometry(final com.vividsolutions.jts.geom.Geometry geom) {
        this.geom = geom;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    public void setGeom(final com.vividsolutions.jts.geom.Geometry geom) {
        this.geom = geom;
    }

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
    public int getArt() {
        return art;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  art  DOCUMENT ME!
     */
    public void setArt(final int art) {
        this.art = art;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getLabelText() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.awt.Stroke getLineStyle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TextStyle getTextStyle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getToolTipText() {
        return org.openide.util.NbBundle.getMessage(
                FeatureExample.class,
                "FeatureExample.getToolTipText().return",
                new Object[] { name }); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getTransparency() {
        return 1.0f;
    }

    @Override
    public String toString() {
        final String retValue;
        retValue = super.toString();
        return retValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.awt.Paint getLinePaint() {
        return Color.black;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.awt.Paint getFillingStyle() {
        switch (art) {
            case 1: {
                return new java.awt.Color(162, 76, 41, 150);   // Dach
            }
            case 2: {
                return new java.awt.Color(106, 122, 23, 150);  // Gr\u00FCndach
            }
            case 3: {
                return new java.awt.Color(120, 129, 128, 150); // versiegelte Fl\u00E4che
            }
            case 4: {
                return new java.awt.Color(159, 155, 108, 150); // \u00D6kopflaster
            }
            case 5: {
                return new java.awt.Color(138, 134, 132, 150); // st\u00E4dtische Strassenflaeche
            }
            case 6: {
                return new java.awt.Color(126, 91, 71, 150);   // staedtische Strassenflaeche Oekopflaster
            }
            default: {
                return null;
            }
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean canBeSelected() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isEditable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  editable  DOCUMENT ME!
     */
    public void setEditable(final boolean editable) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hiding  DOCUMENT ME!
     */
    public void hide(final boolean hiding) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isHidden() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public String getPrimaryLabelText() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public String getSecondaryLabelText() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public String getPrimaryAnnotation() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Font getPrimaryAnnotationFont() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public double getPrimaryAnnotationScaling() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public String getSecondaryAnnotation() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setPrimaryAnnotation(final String primaryAnnotation) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setPrimaryAnnotationFont(final Font primaryAnnotationFont) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setPrimaryAnnotationScaling(final double primaryAnnotationSize) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setSecondaryAnnotation(final String secondaryAnnotation) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Integer getMaxScaleDenominator() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Integer getMinScaleDenominator() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isAutoscale() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public int getLineWidth() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fillingStyle  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void setFillingStyle(final Paint fillingStyle) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   width  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void setLineWidth(final int width) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureAnnotationSymbol  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   transparrency  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void setTransparency(final float transparrency) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setAutoScale(final boolean autoScale) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setMaxScaleDenominator(final Integer max) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setMinScaleDenominator(final Integer min) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Paint getPrimaryAnnotationPaint() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setPrimaryAnnotationPaint(final Paint primaryAnnotationPaint) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public float getPrimaryAnnotationJustification() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setPrimaryAnnotationJustification(final float just) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isPrimaryAnnotationVisible() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setPrimaryAnnotationVisible(final boolean visible) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
