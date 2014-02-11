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

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.tools.gui.PointSymbolCreator;

/**
 * BasicStyle implements the Style-interface. It represents a collection of different variables (like colors, sizes,
 * etc.) to configure the StyleDialog.
 *
 * @author   haffkeatcismet
 * @version  $Revision$, $Date$
 */
public class BasicStyle implements Style {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);                                // NOI18N
    protected static final String POINTSYMBOL_FOLDER = "/de/cismet/cismap/commons/featureservice/res/pointsymbols/"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected final Logger logger = Logger.getLogger(this.getClass());
    protected boolean drawFill;
    protected boolean drawLine;
    protected Color colorFill;
    protected Color colorLine;
    protected int lineWidth;
    protected boolean highlightFeature;
    protected float alpha;
    protected String pointSymbolFilename = Style.NO_POINTSYMBOL;
    protected int pointSymbolSize;
    protected boolean drawLabel;
    protected Font font;
    protected Color fontColor;
    protected String attribute;
    protected float alignment;
    protected int minScale;
    protected int maxScale;
    protected double multiplier;
    protected boolean autoscale;
    protected FeatureAnnotationSymbol pointSymbol;
    protected Color halo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates either an empty or default BasicStyle.
     */
    public BasicStyle() {
        this.setDefaultValues();
    }

    /**
     * Creates a new BasicStyle object.
     *
     * @param   element  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public BasicStyle(final Element element) throws Exception {
        this.initFromElement(element);
    }

    /**
     * Creates a new BasicStyle.
     *
     * @param  drawFill          true if the filling should be painted, else false
     * @param  colorFill         the fillingcolor
     * @param  drawLine          true if the line should be painted, else false
     * @param  colorLine         the linecolor
     * @param  lineWidth         the linewidth
     * @param  highlightFeature  true if features should lighten up on mouseover
     * @param  alpha             alpha-value (0=invisible to 1=visible)
     * @param  pointSymbolName   name of the pointsymbol
     * @param  pointSymbolSize   size of the pointsymobl
     * @param  drawLabel         true if the labels should be painted, else false
     * @param  font              fonttype of the labels
     * @param  fontColor         the fontcolor
     * @param  attribute         attribute that be shown as label
     * @param  alignment         alignment of the label
     * @param  minScale          minimum scale at which the label is still visible
     * @param  maxScale          maximum scale at which the label is still visible
     * @param  multiplier        the size multiplier (non-effective if autoscale is enabled)
     * @param  autoscale         true to resize the label according to the zoomlevel, else false
     * @param  halo              DOCUMENT ME!
     */
    public BasicStyle(final boolean drawFill,
            final Color colorFill,
            final boolean drawLine,
            final Color colorLine,
            final int lineWidth,
            final boolean highlightFeature,
            final float alpha,
            final String pointSymbolName,
            final int pointSymbolSize,
            final boolean drawLabel,
            final Font font,
            final Color fontColor,
            final String attribute,
            final float alignment,
            final int minScale,
            final int maxScale,
            final double multiplier,
            final boolean autoscale,
            final Color halo) {
        this.drawFill = drawFill;
        this.drawLine = drawLine;
        this.highlightFeature = highlightFeature;
        this.colorFill = colorFill;
        this.colorLine = colorLine;
        this.lineWidth = lineWidth;
        this.alpha = alpha;
        this.pointSymbolSize = pointSymbolSize;
        this.drawLabel = drawLabel;
        this.font = font;
        this.fontColor = fontColor;
        this.attribute = attribute;
        this.alignment = alignment;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.multiplier = multiplier;
        this.autoscale = autoscale;
        this.halo = halo;
        this.setPointSymbolFilename(pointSymbolName);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Resets this BasicStyle back to default.
     */
    public void setDefaultValues() {
        final Color defaultColor = new Color((int)(Math.random() * 255),
                (int)(Math.random() * 255),
                (int)(Math.random() * 255));
        this.drawFill = true;
        this.drawLine = true;
        this.highlightFeature = true;
        this.colorFill = defaultColor;
        this.colorLine = BasicStyle.darken(defaultColor);
        this.lineWidth = 1;
        this.alpha = 1.0f;
        this.pointSymbolFilename = NO_POINTSYMBOL;
        this.pointSymbolSize = MIN_POINTSYMBOLSIZE;
        this.drawLabel = false;
        this.font = DEFAULT_FONT;
        this.fontColor = Color.BLACK;
        this.attribute = ""; // deprecated//NOI18N
        this.alignment = JLabel.LEFT_ALIGNMENT;
        this.minScale = 0;
        this.maxScale = 2500;
        this.multiplier = 1.0d;
        this.autoscale = true;
        this.pointSymbol = null;
        this.halo = null;
    }

    /**
     * Creates a JDOM-element that represents this BasicStyle.
     *
     * @return  JDOM-Element
     */
    @Override
    public Element toElement() {
        final Element e = new Element(Style.STYLE_ELEMENT);
        final Element fill = new Element(FILL);
        final Element line = new Element(LINE);
        final Element point = new Element(POINTSYMBOL);
        final Element label = new Element(LABEL);

        e.setAttribute(ConvertableToXML.TYPE_ATTRIBUTE, this.getClass().getCanonicalName());

        // fill
        fill.setAttribute(Style.PAINT, Boolean.toString(drawFill));
        if (colorFill != null) {
            fill.setAttribute(Style.COLOR, Integer.toString(colorFill.getRGB()));
        }
        fill.setAttribute(Style.ALPHA, Float.toString(alpha));
        fill.setAttribute(Style.HIGHLIGHT, Boolean.toString(highlightFeature));

        // line
        line.setAttribute(Style.PAINT, Boolean.toString(drawLine));
        if (colorLine != null) {
            line.setAttribute(Style.COLOR, Integer.toString(colorLine.getRGB()));
        }
        line.setAttribute(Style.WIDTH, Integer.toString(lineWidth));

        // pointsymbol
        point.setAttribute(Style.NAME, pointSymbolFilename);
        point.setAttribute(Style.SIZE, Integer.toString(pointSymbolSize));

        // label
        label.setAttribute(PAINT, Boolean.toString(drawLabel));
        label.setAttribute(FAMILY, font.getFamily());
        if (font != null) {
            label.setAttribute(STYLE, Integer.toString(font.getStyle()));
        }
        if (font != null) {
            label.setAttribute(SIZE, Integer.toString(font.getSize()));
        }
        if (fontColor != null) {
            label.setAttribute(COLOR, Integer.toString(fontColor.getRGB()));
        }
        if (halo != null) {
            label.setAttribute(HALO, Integer.toString(halo.getRGB()));
        }
        label.setAttribute(ATTRIBUTE, attribute);
        label.setAttribute(ALIGNMENT, Float.toString(alignment));
        label.setAttribute(MIN_SCALE, Integer.toString(minScale));
        label.setAttribute(MAX_SCALE, Integer.toString(maxScale));
        label.setAttribute(MULTIPLIER, Double.toString(multiplier));
        label.setAttribute(AUTOSCALE, Boolean.toString(autoscale));

        e.addContent(fill);
        e.addContent(line);
        e.addContent(point);
        e.addContent(label);

        return e;
    }

    /**
     * Compares this BasicStyle with the delivered object.
     *
     * @param   o  object that should be compared with this BasicStyle
     *
     * @return  0 if the objects are equal, else -1
     */
    @Override
    public int compareTo(final Object o) {
        if (o instanceof BasicStyle) {
            final BasicStyle bs = (BasicStyle)o;
            if ((highlightFeature == bs.isHighlightFeature()) && (drawFill == bs.isDrawFill())
                        && (colorFill == bs.getFillColor())
                        && (drawLine == bs.isDrawLine())
                        && (colorLine == bs.getLineColor())
                        && (lineWidth == bs.getLineWidth())
                        && (alpha == bs.getAlpha()) && pointSymbolFilename.equals(bs.getPointSymbolFilename())
                        && (drawLabel == bs.isDrawLabel() /*&& attribute.equals(bs.getAnnotationAttribute())*/)
                        && font.getFamily().equals(bs.getFont().getFamily())
                        && (font.getStyle() == bs.getFont().getStyle())
                        && (font.getSize() == bs.getFont().getSize())
                        && (fontColor == bs.getFontColor())
                        && (maxScale == bs.getMaxScale())
                        && (minScale == bs.getMinScale())
                        && (alignment == bs.getAlignment())
                        && (autoscale == bs.isAutoscale())
                        && (multiplier == bs.getMultiplier())
                        && (halo == bs.getHalo())) {
                if (pointSymbolFilename.equals(NO_POINTSYMBOL)) {
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

    /**
     * Creates an exact copy of this BasicStyle.
     *
     * @return  copy of this BasicStyle.
     *
     * @throws  CloneNotSupportedException  DOCUMENT ME!
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return new BasicStyle(
                drawFill,
                colorFill,
                drawLine,
                colorLine,
                lineWidth,
                highlightFeature,
                alpha,
                pointSymbolFilename,
                pointSymbolSize,
                drawLabel,
                font,
                fontColor,
                attribute,
                alignment,
                minScale,
                maxScale,
                multiplier,
                autoscale,
                halo);
    }

    @Override
    public boolean isDrawFill() {
        return drawFill;
    }

    @Override
    public void setDrawFill(final boolean drawFill) {
        final boolean oldDrawFill = this.drawFill;
        this.drawFill = drawFill;
        if ((this.drawFill != oldDrawFill) && this.pointSymbolFilename.equals(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setDrawFill changed, recreating autogenerated pointsymbol"); // NOI18N
            }
            this.pointSymbol = createAutoPointSymbol();
        }
    }

    @Override
    public boolean isDrawLine() {
        return drawLine;
    }

    @Override
    public void setDrawLine(final boolean drawLine) {
        final boolean oldDrawLine = this.drawLine;
        this.drawLine = drawLine;
        if ((this.drawLine != oldDrawLine) && this.pointSymbolFilename.equals(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setDrawLine changed, recreating autogenerated pointsymbol"); // NOI18N
            }
            this.pointSymbol = createAutoPointSymbol();
        }
    }

    @Override
    public boolean isHighlightFeature() {
        return highlightFeature;
    }

    @Override
    public void setHighlightFeature(final boolean highlightFeature) {
        this.highlightFeature = highlightFeature;
    }

    @Override
    public Color getFillColor() {
        return colorFill;
    }

    @Override
    public void setFillColor(final Color colorFill) {
        final Color oldColorFill = this.colorFill;
        this.colorFill = colorFill;
        if (!this.colorFill.equals(oldColorFill) && this.pointSymbolFilename.equals(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setcolorFill changed, recreating autogenerated pointsymbol"); // NOI18N
            }
            this.pointSymbol = createAutoPointSymbol();
        }
    }

    @Override
    public Color getLineColor() {
        return colorLine;
    }

    @Override
    public void setLineColor(final Color colorLine) {
        final Color oldColorLine = this.colorLine;
        this.colorLine = colorLine;
        if (!this.colorLine.equals(oldColorLine) && this.pointSymbolFilename.equals(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setcolorLine  changed, recreating autogenerated pointsymbol"); // NOI18N
            }
            this.pointSymbol = createAutoPointSymbol();
        }
    }

    @Override
    public int getLineWidth() {
        return lineWidth;
    }

    @Override
    public void setLineWidth(final int lineWidth) {
        final int oldLineWidth = this.lineWidth;
        this.lineWidth = lineWidth;
        if ((this.lineWidth != oldLineWidth) && this.pointSymbolFilename.equals(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setLineWidth changed, recreating autogenerated pointsymbol"); // NOI18N
            }
            this.pointSymbol = createAutoPointSymbol();
        }
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(final float alpha) {
        this.alpha = alpha;
    }

    @Override
    public String getPointSymbolFilename() {
        return pointSymbolFilename;
    }

    @Override
    public void setPointSymbolFilename(final String pointSymbolFilename) {
        if (logger.isDebugEnabled()) {
            logger.debug("setPointSymbolFilename: " + pointSymbolFilename); // NOI18N
        }
        this.pointSymbolFilename = pointSymbolFilename;

        if (pointSymbolFilename.equalsIgnoreCase(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("auto creating new pointsymbol");                                              // NOI18N
            }
            this.pointSymbol = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol(
                        true,
                        true,
                        10,
                        1,
                        this.colorFill,
                        this.colorLine));
            this.pointSymbol.setSweetSpotX(0.5d);
            this.pointSymbol.setSweetSpotY(0.5d);
        } else if (!pointSymbolFilename.equalsIgnoreCase(Style.NO_POINTSYMBOL)) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("loading new pointsymbol '" + pointSymbolFilename + "'");                  // NOI18N
                }
                final FeatureAnnotationSymbol featureAnnotationSymbol = new FeatureAnnotationSymbol(
                        new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + pointSymbolFilename)).getImage());
                featureAnnotationSymbol.setSweetSpotX(0.14d);
                featureAnnotationSymbol.setSweetSpotY(1.0d);
                featureAnnotationSymbol.addAttribute("name", pointSymbolFilename);                          // NOI18N
                this.pointSymbol = featureAnnotationSymbol;
            } catch (Throwable t) {
                logger.warn("point symbol '" + pointSymbol + "' could not be found, setting to undefined"); // NOI18N
                this.pointSymbolFilename = Style.NO_POINTSYMBOL;
                this.pointSymbol = null;
            }
        } else {
            this.pointSymbol = null;
        }
    }

    @Override
    public int getPointSymbolSize() {
        return pointSymbolSize;
    }

    @Override
    public void setPointSymbolSize(final int pointSymbolSize) {
        final int oldPointSymbolSize = this.pointSymbolSize;
        if (pointSymbolSize < Style.MIN_POINTSYMBOLSIZE) {
            logger.warn("pointSymbolSize " + pointSymbolSize + " smaller than MIN_POINTSYMBOLSIZE "
                        + MIN_POINTSYMBOLSIZE + ", settign to MIN_POINTSYMBOLSIZE"); // NOI18N
            this.pointSymbolSize = MIN_POINTSYMBOLSIZE;
        } else if (pointSymbolSize > Style.MAX_POINTSYMBOLSIZE) {
            logger.warn("pointSymbolSize " + pointSymbolSize + " lager than MAX_POINTSYMBOLSIZE " + MAX_POINTSYMBOLSIZE
                        + ", settign to MAX_POINTSYMBOLSIZE");                       // NOI18N
            this.pointSymbolSize = MAX_POINTSYMBOLSIZE;
        } else {
            this.pointSymbolSize = pointSymbolSize;
        }

        if ((this.pointSymbolSize != oldPointSymbolSize) && this.pointSymbolFilename.equals(Style.AUTO_POINTSYMBOL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setPointSymbolSize changed, recreating autogenerated pointsymbol"); // NOI18N
            }
            this.pointSymbol = createAutoPointSymbol();
        }
    }

    @Override
    public boolean isDrawLabel() {
        return drawLabel;
    }

    @Override
    public void setDrawLabel(final boolean drawLabel) {
        this.drawLabel = drawLabel;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(final Font font) {
        this.font = font;
    }

    @Override
    public Color getFontColor() {
        return fontColor;
    }

    @Override
    public void setFontColor(final Color fontColor) {
        this.fontColor = fontColor;
    }

    @Override
    public void setHalo(final Color halo) {
        this.halo = halo;
    }

    @Override
    public Color getHalo() {
        return halo;
    }

    @Override
    @Deprecated
    public String getLabel() {
        return attribute;
    }

    @Override
    @Deprecated
    public void setLabel(final String attribute) {
        this.attribute = attribute;
    }

    @Override
    public float getAlignment() {
        return alignment;
    }

    @Override
    public void setAlignment(final float alignment) {
        this.alignment = alignment;
    }

    @Override
    public int getMinScale() {
        return minScale;
    }

    @Override
    public void setMinScale(final int minScale) {
        this.minScale = minScale;
    }

    @Override
    public int getMaxScale() {
        return maxScale;
    }

    @Override
    public void setMaxScale(final int maxScale) {
        this.maxScale = maxScale;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(final double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean isAutoscale() {
        return autoscale;
    }

    @Override
    public void setAutoscale(final boolean autoscale) {
        this.autoscale = autoscale;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        if (element.getName().equals(Style.STYLE_ELEMENT)) {
            try {
                if (element.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) == null) {
                    logger.warn("fromElement: restoring object from deprecated xml element"); // NOI18N
                }

                // Parse Fill
                final Element fill = element.getChild(Style.FILL);
                this.setDrawFill(fill.getAttribute(Style.PAINT).getBooleanValue());
                this.setAlpha(fill.getAttribute(Style.ALPHA).getFloatValue());
                final Color tmpF = new Color(fill.getAttribute(Style.COLOR).getIntValue());
                this.setFillColor(new Color(tmpF.getRed(), tmpF.getGreen(), tmpF.getBlue()));

                try {
                    this.setHighlightFeature(fill.getAttribute(Style.HIGHLIGHT).getBooleanValue());
                } catch (Exception skip) {
                    logger.warn("property '" + Style.HIGHLIGHT + "' not found in xml element"); // NOI18N
                    this.setHighlightFeature(false);
                }

                // Parse Line
                final Element line = element.getChild(Style.LINE);
                this.setDrawLine(line.getAttribute(Style.PAINT).getBooleanValue());
                final Color tmpL = new Color(line.getAttribute(Style.COLOR).getIntValue());
                this.setLineColor(new Color(tmpL.getRed(), tmpL.getGreen(), tmpL.getBlue()));
                this.setLineWidth(line.getAttribute(Style.WIDTH).getIntValue());

                // Parse Pointsymbol
                final Element point = element.getChild(Style.POINTSYMBOL);
                this.setPointSymbolFilename(point.getAttributeValue(Style.NAME));
                this.setPointSymbolSize(point.getAttribute(Style.SIZE).getIntValue());

                // Parse Label
                final Element label = element.getChild(Style.LABEL);
                this.setDrawLabel(label.getAttribute(Style.PAINT).getBooleanValue());
                final Font tmpFont = new Font(label.getAttributeValue(Style.FAMILY),
                        new Integer(label.getAttributeValue(Style.STYLE)),
                        new Integer(label.getAttributeValue(Style.SIZE)));
                this.setFont(tmpFont);

                final Color tmpA = new Color(label.getAttribute(Style.COLOR).getIntValue());
                this.setFontColor(new Color(tmpA.getRed(), tmpA.getGreen(), tmpA.getBlue()));

                if (label.getAttribute(Style.HALO) != null) {
                    final Color tmpHalo = new Color(label.getAttribute(Style.HALO).getIntValue());
                    this.setHalo(new Color(tmpHalo.getRed(), tmpHalo.getGreen(), tmpHalo.getBlue()));
                }

                if (label.getAttribute(Style.ATTRIBUTE) != null) {
                    this.setLabel(label.getAttributeValue(Style.ATTRIBUTE));
                } else {
                    this.setLabel("default"); // NOI18N
                }

                this.setAlignment(label.getAttribute(Style.ALIGNMENT).getFloatValue());
                this.setMaxScale(label.getAttribute(Style.MIN_SCALE).getIntValue());
                this.setMaxScale(label.getAttribute(Style.MAX_SCALE).getIntValue());
                this.setMultiplier(label.getAttribute(Style.MULTIPLIER).getDoubleValue());
                this.setAutoscale(label.getAttribute(Style.AUTOSCALE).getBooleanValue());
            } catch (Exception ex) {
                logger.error("Fehler beim Parsen des Elements zu einem Style", ex); // NOI18N
                throw ex;
            }
        } else {
            logger.warn("the element '" + element.getName() + "' is no valid style element, '" + Style.STYLE_ELEMENT
                        + "' expected");                                            // NOI18N
        }
    }

    @Override
    @Deprecated
    public void setPointSymbol(final FeatureAnnotationSymbol pointSymbol) {
        logger.warn("deprecated operation setPointSymbol called!"); // NOI18N
        this.pointSymbol = pointSymbol;
//    if (pointSymbol.getAttribute("name") != null)
//    {
//      this.setPointSymbolFilename(pointSymbol.getAttribute("name").toString());
//    }
    }

    @Override
    public FeatureAnnotationSymbol getPointSymbol() {
        return this.pointSymbol;
    }

    /**
     * Darkens the delivered color to 60%.
     *
     * @param   c  color that should be darker
     *
     * @return  darkened color
     */
    public static Color darken(final Color c) {
        final int r = new Double(Math.floor(c.getRed() * 0.6d)).intValue();
        final int g = new Double(Math.floor(c.getGreen() * 0.6d)).intValue();
        final int b = new Double(Math.floor(c.getBlue() * 0.6d)).intValue();
        return new Color(r, g, b);
    }

    /**
     * Brightens the delivered color for aprox. 66%;.
     *
     * @param   c  color that should be lighter
     *
     * @return  brightened color
     */
    public static Color lighten(final Color c) {
        int r = new Double(Math.floor(c.getRed() / 0.6d)).intValue();
        if (r > 255) {
            r = 255;
        }
        int g = new Double(Math.floor(c.getGreen() / 0.6d)).intValue();
        if (g > 255) {
            g = 255;
        }
        int b = new Double(Math.floor(c.getBlue() / 0.6d)).intValue();
        if (b > 255) {
            b = 255;
        }
        return new Color(r, g, b);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureAnnotationSymbol createAutoPointSymbol() {
        final FeatureAnnotationSymbol autoPointSymbol = new FeatureAnnotationSymbol(PointSymbolCreator
                        .createPointSymbol(
                            this.drawLine,
                            this.drawFill,
                            this.pointSymbolSize,
                            this.lineWidth,
                            this.colorFill,
                            this.colorLine));
        autoPointSymbol.setSweetSpotX(0.5d);
        autoPointSymbol.setSweetSpotY(0.5d);
        return autoPointSymbol;
    }
}
