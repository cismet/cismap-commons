/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice.georeferencing;

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * cismet GmbH, Saarbruecken, Germany.
 *
 * <p>... and it just works.**************************************************</p>
 *
 * @version  $Revision$, $Date$
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class TestTransformation {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
//        final List<Point2D> srcPoints = new ArrayList<>();
//        srcPoints.add(new Point2D.Double(88, 372));
//        srcPoints.add(new Point2D.Double(340, 446));
//        srcPoints.add(new Point2D.Double(771, 562));
//        srcPoints.add(new Point2D.Double(98, 822));
//        srcPoints.add(new Point2D.Double(982, 147));
//        final Point2D[] src = srcPoints.toArray(new Point2D[0]);
//
//        final List<Point2D> dstPoints = new ArrayList<>();
//        dstPoints.add(new Point2D.Double(374334, 5681815));
//        dstPoints.add(new Point2D.Double(374368, 5681790));
//        dstPoints.add(new Point2D.Double(374425, 5681749));
//        dstPoints.add(new Point2D.Double(374344, 5681744));
//        dstPoints.add(new Point2D.Double(374444, 5681803));
//        final Point2D[] dst = dstPoints.toArray(new Point2D[0]);
//
//        final WarpTransform2D wt = new WarpTransform2D(src, dst, 1);
////            System.out.println(new Point2D.Double(91, 373) + " => " + wt.transform(new Point2D.Double(91, 373), null));
//        for (final Point2D srcPoint : srcPoints) {
//            System.out.println(srcPoint + " => " + wt.transform(srcPoint, null));
//        }
//    }

        final long a = System.currentTimeMillis();
        final List<PointCoordinatePair> pairs = new ArrayList<PointCoordinatePair>();
        for (int i = 0; i < 5; i++) {
            final PointCoordinatePair pair = new PointCoordinatePair(
                    new Point(i, i),
                    new Coordinate(i + 5, i + 5));
            pairs.add(pair);
        }

        final List<AffineTransform> transforms = new ArrayList<>();
        for (final Object[] arr : RasterGeoReferencingHandler.getCombinations(pairs.toArray(), 3)) {
            final PointCoordinatePair pair0 = (PointCoordinatePair)arr[0];
            final PointCoordinatePair pair1 = (PointCoordinatePair)arr[1];
            final PointCoordinatePair pair2 = (PointCoordinatePair)arr[2];

            final AffineTransformationBuilder builder = new AffineTransformationBuilder(
                    new Coordinate(pair0.getPoint().getX(), pair0.getPoint().getY()),
                    new Coordinate(pair1.getPoint().getX(), pair1.getPoint().getY()),
                    new Coordinate(pair2.getPoint().getX(), pair2.getPoint().getY()),
                    pair0.getCoordinate(),
                    pair1.getCoordinate(),
                    pair2.getCoordinate());

            final AffineTransformation t = builder.getTransformation();
            if (t != null) {
                final double[] matrix = t.getMatrixEntries();
                final AffineTransform transform = new AffineTransform(
                        matrix[0],
                        matrix[3],
                        matrix[1],
                        matrix[4],
                        matrix[2],
                        matrix[5]);
                transforms.add(transform);
                transform.transform(pair2.getPoint(), null);
            }
//            System.out.println(Arrays.toString(arr));
        }

        final AffineTransform transform = RasterGeoReferencingHandler.createAverageTransformation(transforms);
        System.out.println(transform);

        int r = 0;
        for (final PointCoordinatePair pair : pairs) {
            final Point src = pair.getPoint();
            final Point2D dst = transform.transform(src, null);
            System.out.println(r++ + ": "
                        + dst.distance(new Point.Double(pair.getCoordinate().x, pair.getCoordinate().y)));
        }

        final long b = System.currentTimeMillis();
        System.out.println(transforms.size() + ": " + (b - a));
    }
}
