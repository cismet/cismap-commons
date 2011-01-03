/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Originally written by Andrew Rowbottom.
 * Released freely into the public domain, use it how you want, don't blame me.
 * No warranty for this code is taken in any way.
 */
package de.cismet.cismap.commons.raster.wms.googlemaps;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.text.DecimalFormat;

/**
 * A utility class to assist in encoding and decoding google tile references.
 *
 * <p>For reasons of my own longitude is treated as being between -180 and +180 and internally latitude is treated as
 * being from -1 to +1 and then converted to a mercator projection before return.</p>
 *
 * <p>All rectangles are sorted so the width and height are +ve</p>
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class GoogleTileUtils {

    //~ Constructors -----------------------------------------------------------

    /**
     * hidden constructor, this is a Utils class.
     */
    private GoogleTileUtils() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns a buffered image with the corner lat/lon,keyhole id and zoom level written on it.
     *
     * @param   keyholeString  the keyhole string to return the image for.
     *
     * @return  DOCUMENT ME!
     */
    public static BufferedImage getDebugTileFor(final String keyholeString) {
        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        final Graphics g = img.getGraphics();
        g.setColor(Color.gray);
        g.fillRect(0, 0, 256, 256);
        g.setColor(Color.black);

        final int scale = 400 / keyholeString.length();
        g.setFont(new Font("Serif", Font.BOLD, scale));                                   // NOI18N
        g.drawString(keyholeString + " (z=" + getTileZoom(keyholeString) + ")", 10, 200); // NOI18N

        final Rectangle2D r = getLatLong(keyholeString);
        final DecimalFormat df = new DecimalFormat("#.####"); // NOI18N
        g.setFont(new Font("SanSerif", 0, 15));               // NOI18N

        g.drawString(df.format(r.getMinY()) + "," + df.format(r.getMinX()) + " (w:" + df.format(r.getWidth()) + " h:"
                    + df.format(r.getHeight()) + ")",
            10,
            250);                                                                     // NOI18N
        g.drawString(df.format(r.getMaxY()) + "," + df.format(r.getMaxX()), 150, 20); // NOI18N
        g.drawRect(1, 1, 255, 255);
        g.dispose();

        return img;
    }

    /**
     * Returns a buffered image with the corner lat/lon,x,y and zoom level written on it.
     *
     * @param   x     DOCUMENT ME!
     * @param   y     DOCUMENT ME!
     * @param   zoom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BufferedImage getDebugTileFor(final int x, final int y, final int zoom) {
        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        final Graphics g = img.getGraphics();
        g.setColor(Color.gray);
        g.fillRect(0, 0, 256, 256);
        g.setColor(Color.black);

        final int scale = 20;
        g.setFont(new Font("Serif", Font.BOLD, scale));             // NOI18N
        g.drawString("x:" + x + " y:" + y + " z:" + zoom, 10, 200); // NOI18N

        final Rectangle2D r = getLatLong(x, y, zoom);
        final DecimalFormat df = new DecimalFormat("#.####"); // NOI18N
        g.setFont(new Font("SanSerif", 0, 15));               // NOI18N

        g.drawString(df.format(r.getMinY()) + "," + df.format(r.getMinX()) + " (w:" + df.format(r.getWidth()) + " h:"
                    + df.format(r.getHeight()) + ")",
            10,
            250);                                                                     // NOI18N
        g.drawString(df.format(r.getMaxY()) + "," + df.format(r.getMaxX()), 150, 20); // NOI18N
        g.drawRect(1, 1, 255, 255);
        g.dispose();

        return img;
    }

    /**
     * returns a Rectangle2D with x = lon, y = lat, width=lonSpan, height=latSpan for a keyhole string.
     *
     * @param   keyholeStr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    public static Rectangle2D.Double getLatLong(final String keyholeStr) {
        // must start with "t"
        if ((keyholeStr == null) || (keyholeStr.length() == 0) || (keyholeStr.charAt(0) != 't')) {
            throw new RuntimeException("Keyhole string must start with 't'"); // NOI18N
        }

        double lon = -180;     // x
        double lonWidth = 360; // width 360

        // double lat = -90;  // y
        // double latHeight = 180; // height 180
        double lat = -1;
        double latHeight = 2;

        for (int i = 1; i < keyholeStr.length(); i++) {
            lonWidth /= 2;
            latHeight /= 2;

            final char c = keyholeStr.charAt(i);

            switch (c) {
                case 's': {
                    // lat += latHeight;
                    lon += lonWidth;

                    break;
                }

                case 'r': {
                    lat += latHeight;
                    lon += lonWidth;

                    break;
                }

                case 'q': {
                    lat += latHeight;

                    // lon += lonWidth;
                    break;
                }

                case 't': {
                    // lat += latHeight;
                    // lon += lonWidth;
                    break;
                }

                default: {
                    throw new RuntimeException("unknown char '" + c + "' when decoding keyhole string."); // NOI18N
                }
            }
        }

        // convert lat and latHeight to degrees in a transverse mercator projection
        // note that in fact the coordinates go from about -85 to +85 not -90 to 90!
        latHeight += lat;
        latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight))) - (Math.PI / 2);
        latHeight *= (180 / Math.PI);

        lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
        lat *= (180 / Math.PI);

        latHeight -= lat;

        if (lonWidth < 0) {
            lon = lon + lonWidth;
            lonWidth = -lonWidth;
        }

        if (latHeight < 0) {
            lat = lat + latHeight;
            latHeight = -latHeight;
        }

        // lat = Math.asin(lat) * 180 / Math.PI;
        return new Rectangle2D.Double(lon, lat, lonWidth, latHeight);
    }

    /**
     * returns a Rectangle2D with x = lon, y = lat, width=lonSpan, height=latSpan for an x,y,zoom as used by google.
     *
     * @param   x     DOCUMENT ME!
     * @param   y     DOCUMENT ME!
     * @param   zoom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Rectangle2D.Double getLatLong(final int x, final int y, final int zoom) {
        double lon = -180;     // x
        double lonWidth = 360; // width 360

        // double lat = -90;  // y
        // double latHeight = 180; // height 180
        double lat = -1;
        double latHeight = 2;

        final int tilesAtThisZoom = 1 << (17 - zoom);
        lonWidth = 360.0 / tilesAtThisZoom;
        lon = -180 + (x * lonWidth);
        latHeight = -2.0 / tilesAtThisZoom;
        lat = 1 + (y * latHeight);

        // convert lat and latHeight to degrees in a transverse mercator projection
        // note that in fact the coordinates go from about -85 to +85 not -90 to 90!
        latHeight += lat;
        latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight))) - (Math.PI / 2);
        latHeight *= (180 / Math.PI);

        lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
        lat *= (180 / Math.PI);

        latHeight -= lat;

        if (lonWidth < 0) {
            lon = lon + lonWidth;
            lonWidth = -lonWidth;
        }

        if (latHeight < 0) {
            lat = lat + latHeight;
            latHeight = -latHeight;
        }

        return new Rectangle2D.Double(lon, lat, lonWidth, latHeight);
    }

    /**
     * returns a keyhole string for a longitude (x), latitude (y), and zoom.
     *
     * @param   lon   DOCUMENT ME!
     * @param   lat   DOCUMENT ME!
     * @param   zoom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getTileRef(double lon, double lat, int zoom) {
        zoom = 18 - zoom;

        // first convert the lat lon to transverse mercator coordintes.
        if (lon > 180) {
            lon -= 360;
        }

        lon /= 180;

        // convert latitude to a range -1..+1
        lat = Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI;

        double tLat = -1;
        double tLon = -1;
        double lonWidth = 2;
        double latHeight = 2;

        final StringBuffer keyholeString = new StringBuffer("t");

        for (int i = 0; i < zoom; i++) {
            lonWidth /= 2;
            latHeight /= 2;

            if ((tLat + latHeight) > lat) {
                if ((tLon + lonWidth) > lon) {
                    keyholeString.append('t');
                } else {
                    tLon += lonWidth;
                    keyholeString.append('s');
                }
            } else {
                tLat += latHeight;

                if ((tLon + lonWidth) > lon) {
                    keyholeString.append('q');
                } else {
                    tLon += lonWidth;
                    keyholeString.append('r');
                }
            }
        }

        return keyholeString.toString();
    }

    /**
     * returns the Google zoom level for the keyhole string.
     *
     * @param   keyHoleString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static int getTileZoom(final String keyHoleString) {
        return 18 - keyHoleString.length();
    }

    /**
     * Tests.
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        System.out.println(getLatLong(0, 0, 15));
        System.out.println(getLatLong(1, 1, 15));
        System.out.println(getLatLong(2, 2, 15));
        System.out.println(getLatLong(3, 3, 15));
    }
}
