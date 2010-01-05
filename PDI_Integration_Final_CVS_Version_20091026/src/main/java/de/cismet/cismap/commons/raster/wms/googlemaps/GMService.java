/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.raster.wms.googlemaps;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import edu.umd.cs.piccolo.PNode;
import java.math.BigDecimal;

/**
 *
 * @author hell
 */
public class GMService extends AbstractWMS implements RasterMapService, RetrievalServiceLayer {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ImageRetrieval ir;
    private boolean enabled = true;
    private PNode pNode = new PNode();

    @Override
    public void retrieve(boolean forced) {
        log.debug("retrieve()");
        
        GMBoundingBox real=new GMBoundingBox();
        
        String url=generateURL(new GMBoundingBox(new GMGISPosition(bb.getY1(),bb.getX1()),new GMGISPosition(bb.getY2(),bb.getX2())),width,height,real);
        
        if (ir != null && ir.isAlive() && ir.getUrl().equals(url.toString()) && !forced) {
            //mach nix 
            //mehrfachaufruf mit der gleichen url = unsinn
            log.debug("mehrfachaufruf mit der gleichen url = unsinn");
        } else {
            if (ir != null && ir.isAlive()) {
                ir.youngerWMSCall();
                ir.interrupt();
                //retrievalAborted(new RetrievalEvent());
//                try {
//                    ir.join();   
//                }
//                catch (InterruptedException iex){
//                    log.warn("ir.join() wurde unterbrochen",iex);
//                }
            }
            ir = new ImageRetrieval(this);
            ir.setUrl(url);
            log.debug("ir.start();");
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }

    }

    @Override
    public Object clone() {
        return new GMService();
    }

    public PNode getPNode() {
        return pNode;
    }

    public void setPNode(PNode pNode) {
        this.pNode = pNode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        ;
    }

    public boolean canBeDisabled() {
        return true;
    }

    public int getLayerPosition() {
        return -1;
    }

    public void setLayerPosition(int layerPosition) {
    }

    public float getTranslucency() {
        return 0.5f;
    }

    public void setTranslucency(float t) {
    }

    public String getName() {
        return "GM";
    }

    public void setName(String name) {
    }

    public static String generateURL(GMBoundingBox box, int image_width, int image_height, GMBoundingBox result) {
        GMGISPosition position = box.middle();
        double width = box.width();
        String URL = null;
        int ZoomLevel = 1;
        double Magnification = 0.0D;
        double longtitude = (new BigDecimal(position.getLongitude())).setScale(6, 5).doubleValue();
        double latitude = (new BigDecimal(position.getLatitude())).setScale(6, 5).doubleValue();
        double pixelwidth = (double) image_width * LONG_DEGREES_PER_PIXEL;
        if (width < pixelwidth) {
            Magnification = ZOOM_LEVELS[0];
        } else if (width < pixelwidth * ZOOM_LEVELS[1]) {
            Magnification = ZOOM_LEVELS[1];
        } else if (width < pixelwidth * ZOOM_LEVELS[2]) {
            Magnification = ZOOM_LEVELS[2];
        } else if (width < pixelwidth * ZOOM_LEVELS[3]) {
            Magnification = ZOOM_LEVELS[3];
        } else if (width < pixelwidth * ZOOM_LEVELS[4]) {
            Magnification = ZOOM_LEVELS[4];
        } else if (width < pixelwidth * ZOOM_LEVELS[5]) {
            Magnification = ZOOM_LEVELS[5];
        } else if (width < pixelwidth * ZOOM_LEVELS[6]) {
            Magnification = ZOOM_LEVELS[6];
        } else if (width < pixelwidth * ZOOM_LEVELS[7]) {
            Magnification = ZOOM_LEVELS[7];
        } else if (width < pixelwidth * ZOOM_LEVELS[8]) {
            Magnification = ZOOM_LEVELS[8];
        } else {
            Magnification = ZOOM_LEVELS[9];
        }
        ZoomLevel = (new Double((double) image_width * (1.0D + Magnification))).intValue();
        longtitude *= 1000000D;
        latitude *= 1000000D;
        if (longtitude < 0.0D) {
            longtitude += TWO_TO_THIRTYTWO;
        }
        if (latitude < 0.0D) {
            latitude += TWO_TO_THIRTYTWO;
        }
        URL = "http://maps.google.com/mapdata?latitude_e6=" + (new BigDecimal(latitude)).setScale(0, 5) + "&longitude_e6=" + (new BigDecimal(longtitude)).setScale(0, 5) + "&zm=" + ZoomLevel + "&cc=us&min_priority=2&w=" + image_width + "&h=" + image_height;
        if (Magnification == 0.0D) {
            Magnification = 1.0D;
        }
        double long_half = (double) (image_width / 2) * (LONG_DEGREES_PER_PIXEL * Magnification);
        double dpp = 1.075E-05D * Math.cos((position.latitude * 3.1415926535897931D) / 180D);
        double lat_half = (double) (image_height / 2) * (dpp * Magnification);
        GMGISPosition topLeft = new GMGISPosition(position.latitude - lat_half, position.longitude - long_half);
        GMGISPosition bottomRight = new GMGISPosition(position.latitude + lat_half, position.longitude + long_half);
        result.setBottomRight(bottomRight);
        result.setTopLeft(topLeft);
        return URL;
    }
    private static long TWO_TO_THIRTYTWO = 0x100000000L;
    private static double LONG_DEGREES_PER_PIXEL = 1.0759E-05D;
    private static double LAT_DEGREES_PER_PIXEL = 5.2499999999999997E-06D;
    private static double ZOOM_LEVELS[] = {
        0.0D, 2D, 4D, 8D, 16D, 32D, 64D, 128D, 300D, 1000D
    };
}

class GMGISPosition {

    public GMGISPosition(double latitude, double longitude, double elevation, long timestamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
        this.timestamp = timestamp;
    }

    public GMGISPosition(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    double longitude;
    double latitude;
    double elevation;
    long timestamp;
}

class GMBoundingBox {

    public GMBoundingBox() {
        topLeft = null;
        bottomRight = null;
    }

    public GMBoundingBox(GMGISPosition top, GMGISPosition bottom) {
        topLeft = null;
        bottomRight = null;
        topLeft = top;
        bottomRight = bottom;
    }

    public GMGISPosition getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(GMGISPosition bottomRight) {
        this.bottomRight = bottomRight;
    }

    public GMGISPosition getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(GMGISPosition topLeft) {
        this.topLeft = topLeft;
    }

    public GMGISPosition middle() {
        return middle(this);
    }

    public static GMGISPosition middle(GMBoundingBox box) {
        double west = box.getTopLeft().longitude;
        double north = box.getTopLeft().latitude;
        double east = box.getBottomRight().longitude;
        double south = box.getBottomRight().latitude;
        double middlelat = -1D;
        double middlelong = -1D;
        if (north > south) {
            middlelat = north + (south - north) / 2D;
        } else {
            middlelat = north + (north - south) / 2D;
        }
        if (east > west) {
            middlelong = west + (east - west) / 2D;
        } else {
            middlelong = west + (west - east) / 2D;
        }
        return new GMGISPosition(middlelat, middlelong);
    }

    public double width() {
        return width(this);
    }

    public static double width(GMBoundingBox box) {
        double west = box.getTopLeft().longitude;
        double east = box.getBottomRight().longitude;
        double width = -1D;
        if (east > west) {
            width = east - west;
        } else {
            width = west - east;
        }
        return width;
    }

    public double height() {
        return height(this);
    }

    public static double height(GMBoundingBox box) {
        double north = box.getTopLeft().latitude;
        double south = box.getBottomRight().latitude;
        double height = -1D;
        if (north > south) {
            height = north - south;
        } else {
            height = south - north;
        }
        return height;
    }
    private GMGISPosition topLeft;
    private GMGISPosition bottomRight;
}
