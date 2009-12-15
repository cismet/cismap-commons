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
 *
 * @author jruiz
 */
public class EllipsePHandle extends PHandle {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PFeature pfeature;
    private Point2D startPoint;

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

    @Override
    public void dragHandle(PDimension aLocalDimension, PInputEvent pInputEvent) {
        int n = pfeature.getXp().length - 1;

        Point2D dragPoint = (Point2D) pInputEvent.getPosition();
        double a = startPoint.getX() - dragPoint.getX();
        double b = startPoint.getY() - dragPoint.getY();
        double startX = startPoint.getX();
        double startY = startPoint.getY();

        Coordinate[] coordArr = createEllipseCoordinates(n, a, b, pInputEvent.isControlDown(), pInputEvent.isShiftDown());
        for (int i = 0; i < coordArr.length; i++) {
            pfeature.moveCoordinateToNewPiccoloPosition(i, (float)(startX - coordArr[i].x), (float)(startY - coordArr[i].y));
        }

        relocateHandle();
    }

    public static Coordinate[] createEllipseCoordinates(int numOfEdges, double a, double b, boolean isCentered, boolean isCircle) {
        Coordinate[] coordArr = new Coordinate[numOfEdges+1];

        if (isCircle) {
            boolean aNeg = a < 0;
            boolean bNeg = b < 0;

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
            float degrees = i * (360f / numOfEdges);
            
            // Koordinaten des jeweiligen Punktes berechnen
            double x;
            double y;
            if (isCentered) { // zentriert
                x = a * Math.cos(Math.toRadians(degrees));
                y = b * Math.sin(Math.toRadians(degrees));
            } else { // innerhalb der gezogenen boundingbox
                x = (a / 2) + (a / 2) * Math.cos(Math.toRadians(degrees));
                y = (b / 2) + (b / 2) * Math.sin(Math.toRadians(degrees));
            }

            // Koordinaten in das Array einfügen
            coordArr[i] = new Coordinate(x, y);
        }

        return coordArr;
    }

    //TODO move to a static geometryutils class
    public static double area(PFeature pfeature) {
        double area = 0;

        int n = pfeature.getXp().length;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += pfeature.getXp()[i] * pfeature.getYp()[j];
            area -= pfeature.getXp()[j] * pfeature.getYp()[i];
        }
        area /= 2f;
        return area;
    }

    //TODO move to a static geometryutils class
    public static Point2D centroid(PFeature pfeature) {
        double cx = 0;
        double cy = 0;

        int n = pfeature.getXp().length;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double factor = (pfeature.getXp()[i] * pfeature.getYp()[j] - pfeature.getXp()[j] * pfeature.getYp()[i]);
            cx += (pfeature.getXp()[i] + pfeature.getXp()[j]) * factor;
            cy += (pfeature.getYp()[i] + pfeature.getYp()[j]) * factor;
        }

        double factor = 1 / (6.0f * area(pfeature));
        cx *= factor;
        cy *= factor;

        return new Point2D.Double(cx, cy);
    }
}
