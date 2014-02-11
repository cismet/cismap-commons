/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public interface AnnotatedFeature {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPrimaryAnnotation();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isPrimaryAnnotationVisible();

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    void setPrimaryAnnotationVisible(boolean visible);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Font getPrimaryAnnotationFont();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Paint getPrimaryAnnotationPaint();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getPrimaryAnnotationScaling();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getPrimaryAnnotationJustification();

    /**
     * DOCUMENT ME!
     *
     * @param  just  DOCUMENT ME!
     */
    void setPrimaryAnnotationJustification(float just);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getSecondaryAnnotation();

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotation  DOCUMENT ME!
     */
    void setPrimaryAnnotation(String primaryAnnotation);

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationFont  DOCUMENT ME!
     */
    void setPrimaryAnnotationFont(Font primaryAnnotationFont);

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationPaint  DOCUMENT ME!
     */
    void setPrimaryAnnotationPaint(Paint primaryAnnotationPaint);

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationScaling  DOCUMENT ME!
     */
    void setPrimaryAnnotationScaling(double primaryAnnotationScaling);

    /**
     * DOCUMENT ME!
     *
     * @param  secondaryAnnotation  DOCUMENT ME!
     */
    void setSecondaryAnnotation(String secondaryAnnotation);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isAutoscale();
    /**
     * DOCUMENT ME!
     *
     * @param  autoScale  DOCUMENT ME!
     */
    void setAutoScale(boolean autoScale);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Integer getMinScaleDenominator();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Integer getMaxScaleDenominator();

    /**
     * DOCUMENT ME!
     *
     * @param  min  DOCUMENT ME!
     */
    void setMinScaleDenominator(Integer min);
    /**
     * DOCUMENT ME!
     *
     * @param  max  DOCUMENT ME!
     */
    void setMaxScaleDenominator(Integer max);

    /**
     * DOCUMENT ME!
     *
     * @param  paint  DOCUMENT ME!
     */
    void setPrimaryAnnotationHalo(Color paint);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Color getPrimaryAnnotationHalo();
}
