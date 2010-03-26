/*
 * FeatureExample.java
 *
 * Created on 4. M\u00E4rz 2005, 17:18
 */

package de.cismet.cismap.commons.demo;

import de.cismet.cismap.commons.features.AnnotatedFeature;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.styling.TextStyle;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

/**
 *
 *
 * 
 * @author hell
 */
public class FeatureExample implements AnnotatedFeature{

    private com.vividsolutions.jts.geom.Geometry geom=null;
    private String name="";//NOI18N
    private int art=-1;
    
    /** Creates a new instance of FeatureExample */
    public FeatureExample() {
    }

    public com.vividsolutions.jts.geom.Geometry getGeometry() {
        return geom;
    }
    public void setGeometry(com.vividsolutions.jts.geom.Geometry geom){
        this.geom=geom;
    }
    public void setGeom(com.vividsolutions.jts.geom.Geometry geom) {
        this.geom = geom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArt() {
        return art;
    }

    public void setArt(int art) {
        this.art = art;
    }

    public String getLabelText() {
        return name;
    }

    public java.awt.Stroke getLineStyle() {
        return null;
    }

    public TextStyle getTextStyle() {
        return null;
    }

    public String getToolTipText() {
        return org.openide.util.NbBundle.getMessage(FeatureExample.class, "FeatureExample.getToolTipText().return", new Object[] {name});
    }

    public float getTransparency() {
        return 1.0f;
    }

    public String toString() {
        String retValue;
        retValue = super.toString();
        return retValue;
    }
    
    public java.awt.Paint getLinePaint() {
        return Color.black;
    }    
    public java.awt.Paint getFillingStyle() {
         switch (art) {
             case 1: return new java.awt.Color(162,76,41,150);//Dach
             case 2: return new java.awt.Color(106,122,23,150);//Gr\u00FCndach
             case 3: return new java.awt.Color(120,129,128,150);//versiegelte Fl\u00E4che
             case 4: return new java.awt.Color(159,155,108,150);//\u00D6kopflaster
             case 5: return new java.awt.Color(138,134,132,150);//st\u00E4dtische Strassenflaeche
             case 6: return new java.awt.Color(126,91,71,150);//staedtische Strassenflaeche Oekopflaster
             default: return null;
         }
    }
    public boolean canBeSelected() {
        return true;
    }

    public boolean isEditable() {
        return false;
    }

    public void setEditable(boolean editable) {
    }

    public void hide(boolean hiding) {
    }

    public boolean isHidden() {
        return false;
    }
  
    
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public String getPrimaryLabelText() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public String getSecondaryLabelText() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public String getPrimaryAnnotation() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public Font getPrimaryAnnotationFont() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public double getPrimaryAnnotationScaling() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public String getSecondaryAnnotation() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPrimaryAnnotation(String primaryAnnotation) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPrimaryAnnotationFont(Font primaryAnnotationFont) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPrimaryAnnotationScaling(double primaryAnnotationSize) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setSecondaryAnnotation(String secondaryAnnotation) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public Integer getMaxScaleDenominator() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public Integer getMinScaleDenominator() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public boolean isAutoscale() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public int getLineWidth() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setFillingStyle(Paint fillingStyle) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setLineWidth(int width) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setTransparency(float transparrency) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setAutoScale(boolean autoScale) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setMaxScaleDenominator(Integer max) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setMinScaleDenominator(Integer min) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public Paint getPrimaryAnnotationPaint() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPrimaryAnnotationPaint(Paint primaryAnnotationPaint) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public float getPrimaryAnnotationJustification() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPrimaryAnnotationJustification(float just) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public boolean isPrimaryAnnotationVisible() {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    public void setPrimaryAnnotationVisible(boolean visible) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    
    
}
