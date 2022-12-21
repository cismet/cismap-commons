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
package de.cismet.cismap.commons.tools;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Contains some useful geometry processing operations.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GeometryUtils.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a dummy geometry of the given type.
     *
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  a dummy geometry of the given type
     */
    public static Geometry createDummyGeometry(final String geometryType) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), -1);

        if (geometryType.equalsIgnoreCase("Point")) {
            return factory.createPoint(new Coordinate(1, 2));
        } else if (geometryType.equalsIgnoreCase("MultiPoint")) {
            return factory.createMultiPoint(new Coordinate[] { new Coordinate(1, 2), new Coordinate(2, 2) });
        } else if (geometryType.equalsIgnoreCase("LineString")) {
            return factory.createLineString(new Coordinate[] { new Coordinate(1, 2), new Coordinate(2, 2) });
        } else if (geometryType.equalsIgnoreCase("MultiLineString")) {
            final LineString ls1 = factory.createLineString(
                    new Coordinate[] { new Coordinate(1, 2), new Coordinate(2, 2) });
            final LineString ls2 = factory.createLineString(
                    new Coordinate[] { new Coordinate(3, 3), new Coordinate(4, 3) });

            return factory.createMultiLineString(new LineString[] { ls1, ls2 });
        } else if (geometryType.equalsIgnoreCase("Polygon")) {
            return factory.createPolygon(
                    new Coordinate[] {
                        new Coordinate(1, 2),
                        new Coordinate(2, 2),
                        new Coordinate(2, 3),
                        new Coordinate(1, 3),
                        new Coordinate(1, 2)
                    });
        } else if (geometryType.equalsIgnoreCase("MultiPolygon")) {
            final Polygon p = factory.createPolygon(
                    new Coordinate[] {
                        new Coordinate(1, 2),
                        new Coordinate(2, 2),
                        new Coordinate(2, 3),
                        new Coordinate(1, 3),
                        new Coordinate(1, 2)
                    });

            return factory.createMultiPolygon(new Polygon[] { p });
        }

        return null;
    }

    /**
     * Convert the given 2d/3d geometry to a 2d geometry.
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry force2d(final Geometry g) {
        if (g != null) {
            g.apply(new CoordinateFilter() {

                    @Override
                    public void filter(final Coordinate coord) {
                        coord.y = 0.0;
                    }
                });

            g.geometryChanged();
        }

        return g;
    }

    /**
     * The axis order of the coordinates of the given geometry will be changed.
     *
     * @param   g  the geometry to change the axis order
     *
     * @return  the given geometry with a changed axis order.
     */
    public static Geometry reverseGeometryCoordinates(final Geometry g) {
        g.apply(new CoordinateFilter() {

                @Override
                public void filter(final Coordinate crdnt) {
                    final double newX = crdnt.y;
                    crdnt.y = crdnt.x;
                    crdnt.x = newX;
                }
            });
        g.geometryChanged();
        return g;
    }

    /**
     * Determines the shape geometry type. In a shape file, every geometry type is described by one byte.
     *
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  the shape geometry type.
     */
    public static byte getShpGeometryType(final String geometryType) {
        if (geometryType.equalsIgnoreCase("Point")) {
            return 1;
        } else if (geometryType.equalsIgnoreCase("MultiPoint")) {
            return 8;
        } else if (geometryType.equalsIgnoreCase("LineString")) {
            return 3;
        } else if (geometryType.equalsIgnoreCase("MultiLineString")) {
            return 3;
        } else if (geometryType.equalsIgnoreCase("Polygon")) {
            return 5;
        } else if (geometryType.equalsIgnoreCase("MultiPolygon")) {
            return 5;
        }

        return 0;
    }

    /**
     * Removes all content from the given shp or shx file, so that the file contains only the header.
     *
     * @param   fileName    the file to clear
     * @param   shpGeoType  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void clearShpOrShxFile(final String fileName, final int shpGeoType) throws IOException {
        final File origFile = new File(fileName);

        if (origFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            origFile.delete();

            try {
                is = GeometryUtils.class.getResourceAsStream(
                        "/de/cismet/watergis/gui/actions/emptyShapeTemplate.shp");
                os = new FileOutputStream(new File(fileName));
                int b;
                int index = 0;

                while ((b = is.read()) != -1) {
                    if (index == 32) {
                        os.write(shpGeoType);
                    } else {
                        os.write(b);
                    }
                    ++index;
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + origFile.getAbsolutePath(), e);
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + fileName, e);
                }
            }
        }
    }

    /**
     * Removes all content from the given shp or shx file, so that the file contains only the header.
     *
     * @param   fileName  the file to clear
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void clearDbfFile(final String fileName) throws IOException {
        File origFile = new File(fileName);

        if (origFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            origFile.renameTo(new File(fileName + ".backup"));
            origFile = new File(fileName + ".backup");

            try {
                is = new FileInputStream(origFile);
                os = new FileOutputStream(new File(fileName));
                int content;
                int byteCounter = 0;
                int tmpLength = 0;
                int length = 1000;

                while ((content = is.read()) != -1) {
                    ++byteCounter;
                    if (byteCounter == 5) {
                        // set the object count to 0
                        os.write(0x0);
                        continue;
                    }
                    if (byteCounter == 9) {
                        // byte 9/10 contain the position of first data record
                        tmpLength = content;
                    }
                    if (byteCounter == 10) {
                        tmpLength += content
                                    << 8;
                        length = tmpLength;
                    }
                    os.write(content);
                    if ((byteCounter >= (length - 1)) && (content == 0xd)) { // 0xd is the last byte of
                                                                             // the header
                        break;
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + origFile.getAbsolutePath(), e);
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + fileName, e);
                }
                origFile.delete();
            }
        }
    }
}
