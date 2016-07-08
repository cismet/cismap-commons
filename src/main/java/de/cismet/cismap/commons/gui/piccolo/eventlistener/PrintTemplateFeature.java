/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import java.awt.Stroke;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.printing.Resolution;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.printing.Template;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class PrintTemplateFeature extends DefaultStyledFeature implements XStyledFeature {

    //~ Static fields/initializers ---------------------------------------------

    public static final double DEFAULT_JAVA_RESOLUTION_IN_DPI = 72d;
    public static final double MILLIMETER_OF_AN_INCH = 25.4d;
    public static final double INCH_OF_A_MILLIMETER = 0.039d;
    public static final double MILLIMETER_OF_A_METER = 1000d;

    //~ Instance fields --------------------------------------------------------

    Template template;
    Resolution resolution;
    Scale scale;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return "Druckbereich ";
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Point getTemplateCenter() {
        return getGeometry().getCentroid();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  template  DOCUMENT ME!
     */
    public void setTemplate(final Template template) {
        this.template = template;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  resolution  DOCUMENT ME!
     */
    public void setResolution(final Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Scale getScale() {
        return scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getRealScaleDenominator() {
        final Coordinate[] corrds = getGeometry().getCoordinates();
        // take former points (0,0) and (X,0) from template rectangle
        final Coordinate p0 = corrds[0];
        final Coordinate p1 = corrds[1];

        final double realwidth = Math.sqrt(((p0.x - p1.x) * (p0.x - p1.x)) + ((p0.y - p1.y) * (p0.y - p1.y)));

        final double paperwidth = template.getMapWidth() / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH
                    / MILLIMETER_OF_A_METER;

        final double denom = realwidth / paperwidth;

        return Math.round(denom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scale  DOCUMENT ME!
     */
    public void setScale(final Scale scale) {
        this.scale = scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getRotationAngle() {
        if (getGeometry() != null) {
            final Coordinate[] corrds = getGeometry().getCoordinates();
            // take former points (0,0) and (X,0) from template rectangle
            final Coordinate point00 = corrds[0];
            final Coordinate pointX0 = corrds[1];
            // determine tangens
            final double oppositeLeg = pointX0.y - point00.y;
            final double adjacentLeg = pointX0.x - point00.x;
            final double tangens = oppositeLeg / adjacentLeg;
            // handle quadrant detection, map to range [-180, 180] degree
            double result = (adjacentLeg > 0) ? 0d : 180d;
            result = (oppositeLeg > 0) ? -result : result;
            // calculate rotation angle in degree
            result -= Math.toDegrees(Math.atan(tangens));
////        round to next full degree
//        return Math.round(result);
            return result;
        } else {
            return -1;
        }
    }

    @Override
    public ImageIcon getIconImage() {
        return null;
    }

    @Override
    public String getType() {
        return ((template != null) ? template.getTitle() : "");
    }

    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        return null;
    }

    @Override
    public Stroke getLineStyle() {
        return null;
    }

    @Override
    public String getName() {
        return "Druckbereich";
    }
}
