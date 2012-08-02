///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package de.cismet.cismap.commons.gui.piccolo;
//
//import com.vividsolutions.jts.geom.Coordinate;
//import edu.umd.cs.piccolo.event.PInputEvent;
//import edu.umd.cs.piccolo.util.PDimension;
//import edu.umd.cs.piccolox.util.PLocator;
//import java.awt.geom.Point2D;
//
///**
// *
// * @author jruiz
// */
//public class RectanglePHandle extends PHandle {
//
//    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
//    private PFeature pfeature;
//    public static enum Corners { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT };
//    private Corners corner;
//
//    public RectanglePHandle(final PFeature pfeature, final Corners corner) {
//        super(new PLocator() {
//
//            @Override
//            public double locateX() {
//                switch (corner) {
//                    case TOP_LEFT:
//                        return pfeature.getBounds().getOrigin().getX();
//                    case TOP_RIGHT:
//                        return pfeature.getBounds().getMaxX();
//                    case BOTTOM_LEFT:
//                        return pfeature.getBounds().getOrigin().getX();
//                    case BOTTOM_RIGHT:
//                        return pfeature.getBounds().getMaxX();
//                    default:
//                        return 0d;
//                }
//            }
//
//            @Override
//            public double locateY() {
//                switch (corner) {
//                    case TOP_LEFT:
//                        return pfeature.getBounds().getOrigin().getY();
//                    case TOP_RIGHT:
//                        return pfeature.getBounds().getOrigin().getY();
//                    case BOTTOM_LEFT:
//                        return pfeature.getBounds().getMaxY();
//                    case BOTTOM_RIGHT:
//                        return pfeature.getBounds().getMaxY();
//                    default:
//                        return 0d;
//                }
//            }
//        }, pfeature.getViewer());
//
//        this.pfeature = pfeature;
//        this.corner = corner;
//    }
//
//    @Override
//    public void dragHandle(PDimension aLocalDimension, PInputEvent pInputEvent) {
//        int n = pfeature.getXp().length - 1;
//
//        Point2D dragPoint = (Point2D) pInputEvent.getPosition();
//        double a = startPoint.getX() - dragPoint.getX();
//        double b = startPoint.getY() - dragPoint.getY();
//        double startX = startPoint.getX();
//        double startY = startPoint.getY();
//
//        Coordinate[] coordArr = createEllipseCoordinates(n, a, b, pInputEvent.isControlDown(), pInputEvent.isShiftDown());
//        for (int i = 0; i < coordArr.length; i++) {
//            pfeature.moveCoordinateToNewPiccoloPosition(i, (float)(startX - coordArr[i].x), (float)(startY - coordArr[i].y));
//        }
//
//        relocateHandle();
//    }
//
//    public static Coordinate[] createEllipseCoordinates(int numOfEdges, double a, double b, boolean isCentered, boolean isCircle) {
//        Coordinate[] coordArr = new Coordinate[numOfEdges+1];
//
//        if (isCircle) {
//            boolean aNeg = a < 0;
//            boolean bNeg = b < 0;
//
//            a = Math.abs(a);
//            b = Math.abs(b);
//
//            // a = b = max (a & b)
//            if (a > b) {
//                b = a;
//            } else {
//                a = b;
//            }
//
//            a = aNeg ? -a : a;
//            b = bNeg ? -b : b;
//        }
//
//        // einmal im Kreis herum
//        for (int i = 0; i <= numOfEdges; i++) {
//
//            // Winkelgrad des jeweiligen Punktes berechnen
//            float degrees = i * (360f / numOfEdges);
//
//            // Koordinaten des jeweiligen Punktes berechnen
//            double x;
//            double y;
//            if (isCentered) { // zentriert
//                x = a * Math.cos(Math.toRadians(degrees));
//                y = b * Math.sin(Math.toRadians(degrees));
//            } else { // innerhalb der gezogenen boundingbox
//                x = (a / 2) + (a / 2) * Math.cos(Math.toRadians(degrees));
//                y = (b / 2) + (b / 2) * Math.sin(Math.toRadians(degrees));
//            }
//
//            // Koordinaten in das Array einfügen
//            coordArr[i] = new Coordinate(x, y);
//        }
//
//        return coordArr;
//    }
//
//}
