/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.style;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import org.jdom.Element;

/**
 *
 * @author haffkeatcismet
 */
public class BasicStyle implements Style {
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private boolean paintFill;
    private boolean paintLine;
    private Color colorFill;
    private Color colorLine;
    private int lineWidth;
    private boolean highlightFeature;
    private float alpha;
    private String pointSymbol;
    private int pointSymbolSize;
//    private boolean paintFillPattern;
//    private boolean paintLinePattern;
//    private FillPattern patternFill;
//    private FillPattern patternLine;
    private boolean paintLabel;
    private Font font;
    private Color fontColor;
    private String attribute;
    private float alignment;
    private int minScale;
    private int maxScale;
    private double multiplier;
    private boolean autoscale;

    public BasicStyle(boolean defaultValues) {
        if (defaultValues) {
            fillWithDefaultValues();
        }
    }
    
    private void fillWithDefaultValues() {
        this.paintFill = true;
        this.paintLine = true;
        this.highlightFeature = false;
        this.colorFill = new Color(0,180,0);
        this.colorLine = new Color(0,110,0);
        this.lineWidth = 1;
        this.alpha = 1.0f;
        this.pointSymbol = NO_POINTSYMBOL;
        this.pointSymbolSize = MIN_POINTSYMBOLSIZE;
        this.paintLabel = false;
        this.font = DEFAULT_FONT;
        this.fontColor = Color.BLACK;
        this.attribute = "";
        this.alignment = JLabel.LEFT_ALIGNMENT;
        this.minScale = 0;
        this.maxScale = 2500;
        this.multiplier = 1.0d;
        this.autoscale = true;
    }

    public BasicStyle(boolean paintFill, Color colorFill, boolean paintLine, 
            Color colorLine, int lineWidth, boolean highlightFeature, 
             float alpha, String pointSymbol, int pointSymbolSize, boolean paintLabel,
             Font font, Color fontColor, String attribute, float alignment, int minScale,
             int maxScale, double multiplier, boolean autoscale) {
        this.paintFill = paintFill;
        this.paintLine = paintLine;
        this.highlightFeature = highlightFeature;
        this.colorFill = colorFill;
        this.colorLine = colorLine;
        this.lineWidth = lineWidth;
        this.alpha = alpha;
        this.pointSymbol = pointSymbol;
        this.pointSymbolSize = pointSymbolSize;
        this.paintLabel = paintLabel;
        this.font = font;
        this.fontColor = fontColor;
        this.attribute = attribute;
        this.alignment = alignment;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.multiplier = multiplier;
        this.autoscale = autoscale;
    }
    
    public Element getElement() {
        Element e = new Element(Style.STYLE_ELEMENT);
        Element fill = new Element(FILL);
        Element line = new Element(LINE);
        Element point = new Element(POINTSYMBOL);
        Element label = new Element(LABEL);
        
        // fill
        fill.setAttribute(Style.PAINT, new Boolean(paintFill).toString());
        fill.setAttribute(Style.COLOR, new Integer(colorFill.getRGB()).toString());
        fill.setAttribute(Style.ALPHA, new Float(alpha).toString());
        fill.setAttribute(Style.HIGHLIGHT, new Boolean(highlightFeature).toString());
        
        // line
        line.setAttribute(Style.PAINT, new Boolean(paintLine).toString());
        line.setAttribute(Style.COLOR, new Integer(colorLine.getRGB()).toString());
        line.setAttribute(Style.WIDTH, new Integer(lineWidth).toString());
        
        // pointsymbol
        point.setAttribute(Style.NAME, pointSymbol);
        if (pointSymbol.equals(NO_POINTSYMBOL)) {
            point.setAttribute(Style.SIZE, new Integer(pointSymbolSize).toString());
        } else {
            point.setAttribute(Style.SIZE, new Integer(MIN_POINTSYMBOLSIZE).toString());
        }
        
        // label
        label.setAttribute(PAINT, new Boolean(paintLabel).toString());
        label.setAttribute(FAMILY, font.getFamily());
        label.setAttribute(STYLE, new Integer(font.getStyle()).toString());
        label.setAttribute(SIZE, new Integer(font.getSize()).toString());
        label.setAttribute(COLOR, new Integer(fontColor.getRGB()).toString());
        label.setAttribute(ATTRIBUTE, attribute);
        label.setAttribute(ALIGNMENT, new Float(alignment).toString());
        label.setAttribute(MIN_SCALE, new Integer(minScale).toString());
        label.setAttribute(MAX_SCALE, new Integer(maxScale).toString());
        label.setAttribute(MULTIPLIER, new Float(multiplier).toString());
        label.setAttribute(AUTOSCALE, new Boolean(autoscale).toString());
        
        e.addContent(fill);
        e.addContent(line);
        e.addContent(point);
        e.addContent(label);
        
        return e;
    }

    public int compareTo(Object o) {
        if (o instanceof BasicStyle) {
            BasicStyle bs = (BasicStyle) o;
            if (highlightFeature == bs.isHighlightFeature()
                    && paintFill == bs.isPaintFill()
                    && colorFill == bs.getFillColor()
                    && paintLine == bs.isPaintLine()
                    && colorLine == bs.getLineColor()
                    && lineWidth == bs.getLineWidth()
                    && alpha == bs.getAlpha()
                    && pointSymbol.equals(bs.getPointSymbol())
                    && paintLabel == bs.isPaintLabel()
                    && attribute.equals(bs.getAttribute())
                    && font.getFamily().equals(bs.getFont().getFamily())
                    && font.getStyle() == bs.getFont().getStyle()
                    && font.getSize() == bs.getFont().getSize()
                    && fontColor == bs.getFontColor()
                    && maxScale == bs.getMaxScale()
                    && minScale == bs.getMinScale()
                    && alignment == bs.getAlignment()
                    && autoscale == bs.isAutoscale()
                    && multiplier == bs.getMultiplier()) {
                if (pointSymbol.equals(NO_POINTSYMBOL)) {
                    if (pointSymbolSize == bs.getPointSymbolSize()) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new BasicStyle(paintFill, colorFill, paintLine, colorLine, lineWidth, highlightFeature,
                alpha, pointSymbol, pointSymbolSize, paintLabel, font, fontColor, attribute, alignment,
                minScale, maxScale, multiplier, autoscale);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Getter & Setter">

    public boolean isPaintFill() {
        return paintFill;
    }

    public void setPaintFill(boolean paintFill) {
        this.paintFill = paintFill;
    }

    public boolean isPaintLine() {
        return paintLine;
    }

    public void setPaintLine(boolean paintLine) {
        this.paintLine = paintLine;
    }

    public boolean isHighlightFeature() {
        return highlightFeature;
    }

    public void setHighlightFeature(boolean highlightFeature) {
        this.highlightFeature = highlightFeature;
    }

    public Color getFillColor() {
        return colorFill;
    }

    public void setFillColor(Color colorFill) {
        this.colorFill = colorFill;
    }

    public Color getLineColor() {
        return colorLine;
    }

    public void setLineColor(Color colorLine) {
        this.colorLine = colorLine;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public String getPointSymbol() {
        return pointSymbol;
    }

    public void setPointSymbol(String pointSymbol) {
        this.pointSymbol = pointSymbol;
    }

    public int getPointSymbolSize() {
        return pointSymbolSize;
    }

    public void setPointSymbolSize(int pointSymbolSize) {
        this.pointSymbolSize = pointSymbolSize;
    }
    
    public boolean isPaintLabel() {
        return paintLabel;
    }

    public void setPaintLabel(boolean paintLabel) {
        this.paintLabel = paintLabel;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public float getAlignment() {
        return alignment;
    }

    public void setAlignment(float alignment) {
        this.alignment = alignment;
    }

    public int getMinScale() {
        return minScale;
    }

    public void setMinScale(int minScale) {
        this.minScale = minScale;
    }

    public int getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isAutoscale() {
        return autoscale;
    }

    public void setAutoscale(boolean autoscale) {
        this.autoscale = autoscale;
    }
    
    // </editor-fold>

}
