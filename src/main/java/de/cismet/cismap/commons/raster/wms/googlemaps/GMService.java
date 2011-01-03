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
package de.cismet.cismap.commons.raster.wms.googlemaps;

import edu.umd.cs.piccolo.PNode;

import java.math.BigDecimal;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.RasterMapService;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class GMService extends AbstractWMS implements RasterMapService, RetrievalServiceLayer {

    //~ Static fields/initializers ---------------------------------------------

    private static long TWO_TO_THIRTYTWO = 0x100000000L;
    private static double LONG_DEGREES_PER_PIXEL = 1.0759E-05D;
    private static double LAT_DEGREES_PER_PIXEL = 5.2499999999999997E-06D;
    private static double[] ZOOM_LEVELS = { 0.0D, 2D, 4D, 8D, 16D, 32D, 64D, 128D, 300D, 1000D };

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ImageRetrieval ir;
    private boolean enabled = true;
    private PNode pNode = new PNode();

    //~ Methods ----------------------------------------------------------------

    @Override
    public void retrieve(final boolean forced) {
        if (log.isDebugEnabled()) {
            log.debug("retrieve()"); // NOI18N
        }

        final GMBoundingBox real = new GMBoundingBox();

        final String url = generateURL(new GMBoundingBox(
                    new GMGISPosition(bb.getY1(), bb.getX1()),
                    new GMGISPosition(bb.getY2(), bb.getX2())),
                width,
                height,
                real);

        if ((ir != null) && ir.isAlive() && ir.getUrl().equals(url.toString()) && !forced) {
            if (log.isDebugEnabled()) {
                // mach nix
                // mehrfachaufruf mit der gleichen url = unsinn
                log.debug("multiple invocations with the same url = humbug"); // NOI18N
            }
        } else {
            if ((ir != null) && ir.isAlive()) {
                ir.youngerWMSCall();
                ir.interrupt();
                // retrievalAborted(new RetrievalEvent());
// try {
// ir.join();
// }
// catch (InterruptedException iex){
// log.warn("ir.join() wurde unterbrochen",iex);
// }
            }
            ir = new ImageRetrieval(this);
            ir.setUrl(url);
            if (log.isDebugEnabled()) {
                log.debug("ir.start();");                                     // NOI18N
            }
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }
    }

    @Override
    public Object clone() {
        return new GMService();
    }

    @Override
    public PNode getPNode() {
        return pNode;
    }

    @Override
    public void setPNode(final PNode pNode) {
        this.pNode = pNode;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        ;
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public int getLayerPosition() {
        return -1;
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
    }

    @Override
    public float getTranslucency() {
        return 0.5f;
    }

    @Override
    public void setTranslucency(final float t) {
    }

    @Override
    public String getName() {
        return "GM"; // NOI18N
    }

    @Override
    public void setName(final String name) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param   box           DOCUMENT ME!
     * @param   image_width   DOCUMENT ME!
     * @param   image_height  DOCUMENT ME!
     * @param   result        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String generateURL(final GMBoundingBox box,
            final int image_width,
            final int image_height,
            final GMBoundingBox result) {
        final GMGISPosition position = box.middle();
        final double width = box.width();
        String URL = null;
        int ZoomLevel = 1;
        double Magnification = 0.0D;
        double longtitude = (new BigDecimal(position.getLongitude())).setScale(6, 5).doubleValue();
        double latitude = (new BigDecimal(position.getLatitude())).setScale(6, 5).doubleValue();
        final double pixelwidth = (double)image_width * LONG_DEGREES_PER_PIXEL;
        if (width < pixelwidth) {
            Magnification = ZOOM_LEVELS[0];
        } else if (width < (pixelwidth * ZOOM_LEVELS[1])) {
            Magnification = ZOOM_LEVELS[1];
        } else if (width < (pixelwidth * ZOOM_LEVELS[2])) {
            Magnification = ZOOM_LEVELS[2];
        } else if (width < (pixelwidth * ZOOM_LEVELS[3])) {
            Magnification = ZOOM_LEVELS[3];
        } else if (width < (pixelwidth * ZOOM_LEVELS[4])) {
            Magnification = ZOOM_LEVELS[4];
        } else if (width < (pixelwidth * ZOOM_LEVELS[5])) {
            Magnification = ZOOM_LEVELS[5];
        } else if (width < (pixelwidth * ZOOM_LEVELS[6])) {
            Magnification = ZOOM_LEVELS[6];
        } else if (width < (pixelwidth * ZOOM_LEVELS[7])) {
            Magnification = ZOOM_LEVELS[7];
        } else if (width < (pixelwidth * ZOOM_LEVELS[8])) {
            Magnification = ZOOM_LEVELS[8];
        } else {
            Magnification = ZOOM_LEVELS[9];
        }
        ZoomLevel = (new Double((double)image_width * (1.0D + Magnification))).intValue();
        longtitude *= 1000000D;
        latitude *= 1000000D;
        if (longtitude < 0.0D) {
            longtitude += TWO_TO_THIRTYTWO;
        }
        if (latitude < 0.0D) {
            latitude += TWO_TO_THIRTYTWO;
        }
        URL = "http://maps.google.com/mapdata?latitude_e6=" + (new BigDecimal(latitude)).setScale(0, 5)
                    + "&longitude_e6=" + (new BigDecimal(longtitude)).setScale(0, 5) + "&zm=" + ZoomLevel
                    + "&cc=us&min_priority=2&w=" + image_width + "&h=" + image_height; // NOI18N
        if (Magnification == 0.0D) {
            Magnification = 1.0D;
        }
        final double long_half = (double)(image_width / 2) * (LONG_DEGREES_PER_PIXEL * Magnification);
        final double dpp = 1.075E-05D * Math.cos((position.latitude * 3.1415926535897931D) / 180D);
        final double lat_half = (double)(image_height / 2) * (dpp * Magnification);
        final GMGISPosition topLeft = new GMGISPosition(position.latitude - lat_half, position.longitude - long_half);
        final GMGISPosition bottomRight = new GMGISPosition(position.latitude + lat_half,
                position.longitude
                        + long_half);
        result.setBottomRight(bottomRight);
        result.setTopLeft(topLeft);
        return URL;
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class GMGISPosition {

    //~ Instance fields --------------------------------------------------------

    double longitude;
    double latitude;
    double elevation;
    long timestamp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GMGISPosition object.
     *
     * @param  latitude   DOCUMENT ME!
     * @param  longitude  DOCUMENT ME!
     */
    public GMGISPosition(final double latitude, final double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Creates a new GMGISPosition object.
     *
     * @param  latitude   DOCUMENT ME!
     * @param  longitude  DOCUMENT ME!
     * @param  elevation  DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public GMGISPosition(final double latitude, final double longitude, final double elevation, final long timestamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
        this.timestamp = timestamp;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  elevation  DOCUMENT ME!
     */
    public void setElevation(final double elevation) {
        this.elevation = elevation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  latitude  DOCUMENT ME!
     */
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  longitude  DOCUMENT ME!
     */
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  timestamp  DOCUMENT ME!
     */
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class GMBoundingBox {

    //~ Instance fields --------------------------------------------------------

    private GMGISPosition topLeft;
    private GMGISPosition bottomRight;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GMBoundingBox object.
     */
    public GMBoundingBox() {
        topLeft = null;
        bottomRight = null;
    }

    /**
     * Creates a new GMBoundingBox object.
     *
     * @param  top     DOCUMENT ME!
     * @param  bottom  DOCUMENT ME!
     */
    public GMBoundingBox(final GMGISPosition top, final GMGISPosition bottom) {
        topLeft = null;
        bottomRight = null;
        topLeft = top;
        bottomRight = bottom;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GMGISPosition getBottomRight() {
        return bottomRight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bottomRight  DOCUMENT ME!
     */
    public void setBottomRight(final GMGISPosition bottomRight) {
        this.bottomRight = bottomRight;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GMGISPosition getTopLeft() {
        return topLeft;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  topLeft  DOCUMENT ME!
     */
    public void setTopLeft(final GMGISPosition topLeft) {
        this.topLeft = topLeft;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GMGISPosition middle() {
        return middle(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   box  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static GMGISPosition middle(final GMBoundingBox box) {
        final double west = box.getTopLeft().longitude;
        final double north = box.getTopLeft().latitude;
        final double east = box.getBottomRight().longitude;
        final double south = box.getBottomRight().latitude;
        double middlelat = -1D;
        double middlelong = -1D;
        if (north > south) {
            middlelat = north + ((south - north) / 2D);
        } else {
            middlelat = north + ((north - south) / 2D);
        }
        if (east > west) {
            middlelong = west + ((east - west) / 2D);
        } else {
            middlelong = west + ((west - east) / 2D);
        }
        return new GMGISPosition(middlelat, middlelong);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double width() {
        return width(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   box  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static double width(final GMBoundingBox box) {
        final double west = box.getTopLeft().longitude;
        final double east = box.getBottomRight().longitude;
        double width = -1D;
        if (east > west) {
            width = east - west;
        } else {
            width = west - east;
        }
        return width;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double height() {
        return height(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   box  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static double height(final GMBoundingBox box) {
        final double north = box.getTopLeft().latitude;
        final double south = box.getBottomRight().latitude;
        double height = -1D;
        if (north > south) {
            height = north - south;
        } else {
            height = south - north;
        }
        return height;
    }
}
