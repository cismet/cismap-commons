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
import com.vividsolutions.jts.linearref.LinearLocation;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class RectangleMath {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   a  DOCUMENT ME!
     * @param   b  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate getMidPoint(final Coordinate a, final Coordinate b) {
        return new Coordinate((a.x + b.x) / 2.0, (a.y + b.y) / 2.0);
    }
    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate getMidPoint(final Coordinate[] c) {
        return getMidPoint(c[0], c[1]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line      DOCUMENT ME!
     * @param   fraction  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate getPointFromStartByFraction(final Coordinate[] line,
            final double fraction) {
        return LinearLocation.pointAlongSegmentByFraction(line[0], line[1], fraction);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line    DOCUMENT ME!
     * @param   start   DOCUMENT ME!
     * @param   length  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate getPointPerpendicular(final Coordinate[] line,
            final Coordinate start,
            final double length) {
        final double dx = line[1].x - line[0].x;
        final double dy = line[1].y - line[0].y;
        final double scale = length / Math.sqrt((dx * dx) + (dy * dy));
        final double x = -dy * scale;
        final double y = dx * scale;
        return new Coordinate(start.x + x, start.y + y);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final Coordinate[] l = new Coordinate[2];
        l[0] = new Coordinate(0, 0);
        l[1] = new Coordinate(1.5, 1.5);

        System.out.println(getPointPerpendicular(l, getPointFromStartByFraction(l, 0.5), 1));
    }
}
