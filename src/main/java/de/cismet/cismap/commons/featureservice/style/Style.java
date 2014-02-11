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
package de.cismet.cismap.commons.featureservice.style;

import java.awt.Color;
import java.awt.Font;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * The Style interface provides miscellaneous constants that are needed by different style-oriented classes. It extends
 * the interfaces ConvertableToXML and Comparable that are mandatory for a working BasicStyle.
 *
 * @author   haffkeatcismet
 * @version  $Revision$, $Date$
 */
public interface Style extends ConvertableToXML, Comparable, Cloneable {

    //~ Instance fields --------------------------------------------------------

    // JDOM-elementconstants
    // TODO: use english String constants instead of "Keins", "Punkt" and so on
    String STYLE_ELEMENT = "StyleHistoryElement"; // NOI18N
    String FILL = "Fill";                         // NOI18N
    String LINE = "Line";                         // NOI18N
    String LABEL = "Label";                       // NOI18N
    String POINTSYMBOL = "Pointsymbol";           // NOI18N
    String NO_POINTSYMBOL = "Keins";              // NOI18N
    String AUTO_POINTSYMBOL = "Punkt";            // NOI18N
    int MIN_POINTSYMBOLSIZE = 5;
    int MAX_POINTSYMBOLSIZE = 50;

    // JDOM-attributes
    String NAME = "name";             // NOI18N
    String PAINT = "paint";           // NOI18N
    String COLOR = "color";           // NOI18N
    String HALO = "halo";             // NOI18N
    String WIDTH = "width";           // NOI18N
    String ALPHA = "alpha";           // NOI18N
    String HIGHLIGHT = "highlight";   // NOI18N
    String SIZE = "size";             // NOI18N
    String FAMILY = "family";         // NOI18N
    String STYLE = "style";           // NOI18N
    String ATTRIBUTE = "attribute";   // NOI18N
    String ALIGNMENT = "alignment";   // NOI18N
    String MIN_SCALE = "minscale";    // NOI18N
    String MAX_SCALE = "maxscale";    // NOI18N
    String MULTIPLIER = "multiplier"; // NOI18N
    String AUTOSCALE = "autoscale";   // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    //J-
    Object clone() throws CloneNotSupportedException;
    //J+

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isDrawFill();
    /**
     * DOCUMENT ME!
     *
     * @param  drawFill  DOCUMENT ME!
     */
    void setDrawFill(boolean drawFill);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isDrawLine();
    /**
     * DOCUMENT ME!
     *
     * @param  drawLine  DOCUMENT ME!
     */
    void setDrawLine(boolean drawLine);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isHighlightFeature();
    /**
     * DOCUMENT ME!
     *
     * @param  highlight  DOCUMENT ME!
     */
    void setHighlightFeature(boolean highlight);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Color getFillColor();
    /**
     * DOCUMENT ME!
     *
     * @param  colorFill  DOCUMENT ME!
     */
    void setFillColor(Color colorFill);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Color getLineColor();
    /**
     * DOCUMENT ME!
     *
     * @param  colorLine  DOCUMENT ME!
     */
    void setLineColor(Color colorLine);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getLineWidth();
    /**
     * DOCUMENT ME!
     *
     * @param  lineWidth  DOCUMENT ME!
     */
    void setLineWidth(int lineWidth);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getAlpha();
    /**
     * DOCUMENT ME!
     *
     * @param  alpha  DOCUMENT ME!
     */
    void setAlpha(float alpha);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureAnnotationSymbol getPointSymbol();
    /**
     * DOCUMENT ME!
     *
     * @param  pointSymbol  DOCUMENT ME!
     */
    @Deprecated
    void setPointSymbol(FeatureAnnotationSymbol pointSymbol);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPointSymbolFilename();
    /**
     * DOCUMENT ME!
     *
     * @param  pointSymbolFilename  DOCUMENT ME!
     */
    void setPointSymbolFilename(String pointSymbolFilename);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getPointSymbolSize();
    /**
     * DOCUMENT ME!
     *
     * @param  pointSymbolSize  DOCUMENT ME!
     */
    void setPointSymbolSize(int pointSymbolSize);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isDrawLabel();
    /**
     * DOCUMENT ME!
     *
     * @param  drawLabel  DOCUMENT ME!
     */
    void setDrawLabel(boolean drawLabel);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Font getFont();
    /**
     * DOCUMENT ME!
     *
     * @param  font  DOCUMENT ME!
     */
    void setFont(Font font);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Color getFontColor();
    /**
     * DOCUMENT ME!
     *
     * @param  fontColor  DOCUMENT ME!
     */
    void setFontColor(Color fontColor);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getLabel();
    /**
     * DOCUMENT ME!
     *
     * @param  label  DOCUMENT ME!
     */
    void setLabel(String label);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getAlignment();
    /**
     * DOCUMENT ME!
     *
     * @param  alignment  DOCUMENT ME!
     */
    void setAlignment(float alignment);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getMinScale();
    /**
     * DOCUMENT ME!
     *
     * @param  minScale  DOCUMENT ME!
     */
    void setMinScale(int minScale);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getMaxScale();
    /**
     * DOCUMENT ME!
     *
     * @param  maxScale  DOCUMENT ME!
     */
    void setMaxScale(int maxScale);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getMultiplier();
    /**
     * DOCUMENT ME!
     *
     * @param  multiplier  DOCUMENT ME!
     */
    void setMultiplier(double multiplier);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isAutoscale();
    /**
     * DOCUMENT ME!
     *
     * @param  autoscale  DOCUMENT ME!
     */
    void setAutoscale(boolean autoscale);

    /**
     * DOCUMENT ME!
     *
     * @param  halo  DOCUMENT ME!
     */
    void setHalo(Color halo);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Color getHalo();
}
