/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.style;

import java.awt.Color;
import java.awt.Font;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author haffkeatcismet
 */
public class StyleFactory {
    private static final Logger log = Logger.getLogger("de.cismet.cismap.commons.featureservice.style.StyleFactory");
    
    public static BasicStyle createDefaultStyle() {
        log.debug("StyleFactory: createDefaultStyle()");
        return new BasicStyle(true);
    }
    
    public static BasicStyle createEmptyStyle() {
        log.debug("StyleFactory: createEmptyStyle()");
        return new BasicStyle(false);
    }
    
    public static BasicStyle createBasicStyle(boolean paintFill, Color fillColor,
            boolean paintLine, Color lineColor, int lineWidth, boolean highlightFeature,
            float alpha, String pointSymbol, int pointSymbolSize, boolean paintLabel, Font font,
            Color fontColor, String attribute, float alignment, int minScale, int maxScale,
            float multiplier, boolean autoscale) {
        log.debug("StyleFactory: createBasicStyle()");
        return new BasicStyle(paintFill, fillColor, paintLine, lineColor, lineWidth, 
                highlightFeature, alpha, pointSymbol, pointSymbolSize, paintLabel, font,
                fontColor, attribute, alignment, minScale, maxScale, multiplier, autoscale);
    }
    
    public static Style createStyle(Element e) {
        log.debug("StyleFactory: createStyle(" + e + ")");
        if (e.getName().equals(Style.STYLE_ELEMENT)) {
            Style style = null;
            try {
                // Parse Fill
                Element fill = e.getChild(Style.FILL);
                boolean paintFill = new Boolean(fill.getAttributeValue(Style.PAINT));
                float alpha = new Float(fill.getAttributeValue(Style.ALPHA));
                Color tmpF = new Color(Integer.parseInt(fill.getAttributeValue(Style.COLOR)));
                Color colorFill = new Color(tmpF.getRed(), tmpF.getGreen(), tmpF.getBlue(), (int) (alpha * 255));
                boolean highlight = false;
                try {
                    highlight = new Boolean(fill.getAttributeValue(Style.HIGHLIGHT));
                } catch (Exception skip) {}
                
                // Parse Line
                Element line = e.getChild(Style.LINE);
                boolean paintLine = new Boolean(line.getAttributeValue(Style.PAINT));
                Color tmpL = new Color(Integer.parseInt(line.getAttributeValue(Style.COLOR)));
                Color colorLine = new Color(tmpL.getRed(), tmpL.getGreen(), tmpL.getBlue(), (int) (alpha * 255));
                int lineWidth = new Integer(line.getAttributeValue(Style.WIDTH));
                
                // Parse Pointsymbol
                Element point = e.getChild(Style.POINTSYMBOL);
                String pointSymbol = point.getAttributeValue(Style.NAME);
                int pointSymbolSize = new Integer(point.getAttributeValue(Style.SIZE));
                
                //Parse Label
                Element label = e.getChild(Style.LABEL);
                boolean paintLabel = new Boolean(label.getAttributeValue(Style.PAINT));
                Font font = new Font(label.getAttributeValue(Style.FAMILY),
                        new Integer(label.getAttributeValue(Style.STYLE)),
                        new Integer(label.getAttributeValue(Style.SIZE)));
                Color tmpA = new Color(Integer.parseInt(label.getAttributeValue(Style.COLOR)));
                Color fontColor = new Color(tmpA.getRed(), tmpA.getGreen(), tmpA.getBlue());
                String attribute = label.getAttributeValue(Style.ATTRIBUTE);
                float alignment = new Float(label.getAttributeValue(Style.ALIGNMENT));
                int minScale = new Integer(label.getAttributeValue(Style.MIN_SCALE));
                int maxScale = new Integer(label.getAttributeValue(Style.MAX_SCALE));
                double multiplier = new Double(label.getAttributeValue(Style.MULTIPLIER));
                boolean autoscale = new Boolean(label.getAttributeValue(Style.AUTOSCALE));

                style = new BasicStyle(paintFill, colorFill, paintLine, colorLine, lineWidth, highlight, alpha, pointSymbol, pointSymbolSize,
                        paintLabel, font, fontColor, attribute, alignment, minScale, maxScale, multiplier, autoscale);
                return style;
            } catch (Exception ex) {
                log.error("Fehler beim Parsen des Elements zu einem Style", ex);
                return null;
            }
        } else {
            log.warn("Das Elements ist kein " + Style.STYLE_ELEMENT);
            return null;
        }
    }
}
