/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import java.awt.Paint;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public interface StyledFeature extends Feature {

    //~ Methods ----------------------------------------------------------------

    /**
     * public java.awt.Stroke getLineStyle();
     *
     * @return  DOCUMENT ME!
     */
    java.awt.Paint getLinePaint();

    /**
     * DOCUMENT ME!
     *
     * @param  linePaint  DOCUMENT ME!
     */
    void setLinePaint(Paint linePaint);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getLineWidth();

    /**
     * DOCUMENT ME!
     *
     * @param  width  DOCUMENT ME!
     */
    void setLineWidth(int width);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    java.awt.Paint getFillingPaint();

    /**
     * DOCUMENT ME!
     *
     * @param  fillingStyle  DOCUMENT ME!
     */
    void setFillingPaint(Paint fillingStyle);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getTransparency();

    /**
     * DOCUMENT ME!
     *
     * @param  transparrency  DOCUMENT ME!
     */
    void setTransparency(float transparrency);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureAnnotationSymbol getPointAnnotationSymbol();

    /**
     * DOCUMENT ME!
     *
     * @param  featureAnnotationSymbol  DOCUMENT ME!
     */
    void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isHighlightingEnabled();

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    void setHighlightingEnabled(boolean enabled);
}
