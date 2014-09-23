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
package de.cismet.cismap.commons.util;

import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Stroke;

import java.util.LinkedList;
import java.util.List;

import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;

/**
 * This class contains some static methods, which are usable to handle the SLD style.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SLDStyleUtil {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   style  the style list
     *
     * @return  a BasicStyle object that only contains fill and stroke values
     */
    public static BasicStyle getBasicStyleFromSLDStyle(final List<org.deegree.style.se.unevaluated.Style> style) {
        BasicStyle basicStyle = null;

        Fill fill = null;
        Stroke stroke = null;

        if ((style != null) && (style.size() > 0)) {
            for (final LinkedList<Styling> st : style.get(0).getBases()) {
                for (final Styling styling : st) {
                    if (styling instanceof PolygonStyling) {
                        final PolygonStyling s = (PolygonStyling)styling;
                        fill = s.fill;
                        stroke = s.stroke;
                    } else if (styling instanceof LineStyling) {
                        final LineStyling s = (LineStyling)styling;
                        stroke = s.stroke;
                    } else if (styling instanceof PointStyling) {
                        final PointStyling s = (PointStyling)styling;
                    }
                }
            }

            if ((fill != null) || (stroke != null)) {
                basicStyle = new BasicStyle();
                basicStyle.setDrawFill(fill != null);
                basicStyle.setDrawLine(stroke != null);

                if (fill != null) {
                    basicStyle.setFillColor(fill.color);
                }

                if (stroke != null) {
                    basicStyle.setLineColor(stroke.color);
                    basicStyle.setLineWidth((int)stroke.width);
                }
            }
        }

        return basicStyle;
    }
}
