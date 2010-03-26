/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.style;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import java.awt.Color;
import java.awt.Font;

/**
 * The Style interface provides miscellaneous constants that are needed by different
 * style-oriented classes. 
 * It extends the interfaces ConvertableToXML and Comparable that are mandatory for a 
 * working BasicStyle.
 * @author haffkeatcismet
 */
public interface Style extends ConvertableToXML, Comparable, Cloneable {
    // JDOM-elementconstants
    //TODO: use english String constants instead of "Keins", "Punkt" and so on
    public static final String STYLE_ELEMENT = "StyleHistoryElement";//NOI18N
    public static final String FILL = "Fill";//NOI18N
    public static final String LINE = "Line";//NOI18N
    public static final String LABEL = "Label";//NOI18N
    public static final String POINTSYMBOL = "Pointsymbol";//NOI18N
    public static final String NO_POINTSYMBOL = "Keins";//NOI18N
    public static final String AUTO_POINTSYMBOL = "Punkt";//NOI18N
    public static final int MIN_POINTSYMBOLSIZE = 5;
    public static final int MAX_POINTSYMBOLSIZE = 50;
    
    // JDOM-attributes
    public static final String NAME = "name";//NOI18N
    public static final String PAINT = "paint";//NOI18N
    public static final String COLOR = "color";//NOI18N
    public static final String WIDTH = "width";//NOI18N
    public static final String ALPHA = "alpha";//NOI18N
    public static final String HIGHLIGHT = "highlight";//NOI18N
    public static final String SIZE = "size";//NOI18N
    public static final String FAMILY = "family";//NOI18N
    public static final String STYLE = "style";//NOI18N
    public static final String ATTRIBUTE = "attribute";//NOI18N
    public static final String ALIGNMENT = "alignment";//NOI18N
    public static final String MIN_SCALE = "minscale";//NOI18N
    public static final String MAX_SCALE = "maxscale";//NOI18N
    public static final String MULTIPLIER = "multiplier";//NOI18N
    public static final String AUTOSCALE = "autoscale";//NOI18N
    
    public Object clone() throws CloneNotSupportedException;
    
    public boolean isDrawFill();
    public void setDrawFill(boolean drawFill);
    public boolean isDrawLine();
    public void setDrawLine(boolean drawLine);
    public boolean isHighlightFeature();
    public void setHighlightFeature(boolean highlight);
    public Color getFillColor();
    public void setFillColor(Color colorFill);
    public Color getLineColor();
    public void setLineColor(Color colorLine);
    public int getLineWidth();
    public void setLineWidth(int lineWidth);
    public float getAlpha();
    public void setAlpha(float alpha);
    public FeatureAnnotationSymbol getPointSymbol();
    @Deprecated
    public void setPointSymbol(FeatureAnnotationSymbol pointSymbol);
    public String getPointSymbolFilename();
    public void setPointSymbolFilename(String pointSymbolFilename);
    public int getPointSymbolSize();
    public void setPointSymbolSize(int pointSymbolSize);

    public boolean isDrawLabel();
    public void setDrawLabel(boolean drawLabel);
    public Font getFont();
    public void setFont(Font font);
    public Color getFontColor();
    public void setFontColor(Color fontColor);
    public String getLabel();
    public void setLabel(String label);
    public float getAlignment();
    public void setAlignment(float alignment);
    public int getMinScale();
    public void setMinScale(int minScale);
    public int getMaxScale();
    public void setMaxScale(int maxScale);
    public double getMultiplier();
    public void setMultiplier(double multiplier);
    public boolean isAutoscale();
    public void setAutoscale(boolean autoscale);
}
