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
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class EllipsePHandle extends PHandle {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EllipsePHandle.class);

    //~ Instance fields --------------------------------------------------------

    private final PFeature pfeature;

    private Point2D startPoint;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new EllipsePHandle object.
     *
     * @param  pfeature  DOCUMENT ME!
     */
    public EllipsePHandle(final PFeature pfeature) {
        super(new PLocator() {

                @Override
                public double locateX() {
                    return pfeature.getBounds().getMaxX();
                }

                @Override
                public double locateY() {
                    return pfeature.getBounds().getMaxY();
                }
            }, pfeature.getViewer());

        this.pfeature = pfeature;
        this.startPoint = pfeature.getBounds().getOrigin();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void dragHandle(final PDimension aLocalDimension, final PInputEvent pInputEvent) {
        final int n = pfeature.getCoordArr(0, 0).length - 1;

        final Point2D dragPoint = (Point2D)pInputEvent.getPosition();
        final double a = startPoint.getX() - dragPoint.getX();
        final double b = startPoint.getY() - dragPoint.getY();
        final double startX = startPoint.getX();
        final double startY = startPoint.getY();

        final Coordinate[] coordArr = createEllipseCoordinates(
                n,
                a,
                b,
                pInputEvent.isControlDown(),
                pInputEvent.isShiftDown());
        for (int i = 0; i < coordArr.length; i++) {
            pfeature.moveCoordinateToNewPiccoloPosition(
                0,
                0,
                i,
                (float)(startX - coordArr[i].x),
                (float)(startY - coordArr[i].y));
        }

        relocateHandle();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   numOfEdges  DOCUMENT ME!
     * @param   a           DOCUMENT ME!
     * @param   b           DOCUMENT ME!
     * @param   isCentered  DOCUMENT ME!
     * @param   isCircle    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate[] createEllipseCoordinates(final int numOfEdges,
            double a,
            double b,
            final boolean isCentered,
            final boolean isCircle) {
        final Coordinate[] coordArr = new Coordinate[numOfEdges + 1];

        if (isCircle) {
            final boolean aNeg = a < 0;
            final boolean bNeg = b < 0;

            a = Math.abs(a);
            b = Math.abs(b);

            // a = b = max (a & b)
            if (a > b) {
                b = a;
            } else {
                a = b;
            }

            a = aNeg ? -a : a;
            b = bNeg ? -b : b;
        }

        // einmal im Kreis herum
        for (int i = 0; i <= numOfEdges; i++) {
            // Winkelgrad des jeweiligen Punktes berechnen
            final float degrees = i * (360f / numOfEdges);

            // Koordinaten des jeweiligen Punktes berechnen
            double x;
            double y;
            if (isCentered) { // zentriert
                x = a * Math.cos(Math.toRadians(degrees));
                y = b * Math.sin(Math.toRadians(degrees));
            } else {          // innerhalb der gezogenen boundingbox
                x = (a / 2) + ((a / 2) * Math.cos(Math.toRadians(degrees)));
                y = (b / 2) + ((b / 2) * Math.sin(Math.toRadians(degrees)));
            }

            // Koordinaten in das Array einfügen
            coordArr[i] = new Coordinate(x, y);
        }

        return coordArr;
    }
}
