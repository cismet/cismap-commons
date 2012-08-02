/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.raster.tms.simple;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;

/**
 *
 * @author cschmidt
 */
public class TileObject {

    private final int zoomLevel;
    private final int xKoordinate;
    private final int yKoordinate;
//    private int tileSize = 256;
    private final String SLASH = "/";//NOI18N
    private final String version;
    private final String host;
    private final String imageType;
    private final String layer;
    private final String tmsRequest;
    private InputStream inStream;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public TileObject(int zoomLevel, int xKoordinate, int yKoordinate, String host, String version, String layer, String imageType) {
        this.zoomLevel = zoomLevel;
        this.xKoordinate = xKoordinate;
        this.yKoordinate = yKoordinate;
        this.version = version;
        this.host = host;
        this.imageType = imageType;
        this.layer = layer;
        tmsRequest = host + SLASH + version + SLASH + layer + SLASH + zoomLevel + SLASH +
                xKoordinate + SLASH + yKoordinate + imageType;
    }

//    public TileObject(int zoomLevel, int xKoodrinate, int yKoordinate,
//            String host, String version, String layer, String imageType,
//            int tileSize) {
//       this.zoomLevel = zoomLevel;
//        this.xKoordinate = xKoodrinate;
//        this.yKoordinate = yKoordinate;
//        this.version = version;
//        this.host = host;
//        this.imageType = imageType;
//        this.layer = layer;
//        tmsRequest = host + SLASH + version + SLASH + layer + SLASH + zoomLevel + SLASH +
//                xKoodrinate + SLASH + yKoordinate + imageType;
//        
//        
//    }

    @Override
    public boolean equals(Object tObj) {
        if (!(tObj instanceof TileObject)) {
            return false;
        }
        if (tObj == null) {
            return false;
        }
        if (!(((TileObject) tObj).zoomLevel == this.zoomLevel)) {
            return false;
        }
        if (!(this.xKoordinate == ((TileObject) tObj).xKoordinate)) {
            return false;
        }
        if (!(this.yKoordinate == ((TileObject) tObj).yKoordinate)) {
            return false;
        }        
        if (!(this.imageType.equals(((TileObject) tObj).imageType))) {
            return false;
        }
        if (!(this.host.equals(((TileObject) tObj).host))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        int multiplikator = 37;
        result = multiplikator * result + (int) zoomLevel;
        result = multiplikator * result + (int) xKoordinate;
        result = multiplikator * result + (int) yKoordinate;        
        result = multiplikator * result + this.host.hashCode();
        result = multiplikator * result + this.imageType.hashCode();

        return result;

    }

    public BufferedImage loadTile() throws Exception {
        try {
//            System.setProperty("http.proxyHost", "");
//            System.setProperty("http.proxyPort", "");
            URL url = new URL(getTmsRequest());
            inStream = url.openStream();
            BufferedImage img = ImageIO.read(ImageIO.createImageInputStream(inStream));
            return img;            
        } catch(IIOException ex){
            log.error("IIOException for URL: "+getTmsRequest(),ex);//NOI18N
            return null;
        } catch (Throwable t) {        
            log.error("Throable for URL: "+getTmsRequest(),t);//NOI18N
            return null;
        } finally {
            if (getInStream() != null) {
                try {
                    getInStream().close();
                } catch (IOException ex) {
                    //ignore
                    }
            }
        }
    }
    

    public int getZoomLevel() {
        return this.zoomLevel;
    }

    public int getXKoordinate() {
        return this.xKoordinate;
    }

    public int getYKoordinate() {
        return this.yKoordinate;
    }

    public String getTmsRequest() {
        return this.tmsRequest;
    }

    public Point2D getOffset(double realWorldX1, double realWorldY1, double bbX1, double bbY2, double meter_pro_tile) {
        double tileX = realWorldX1 + xKoordinate * meter_pro_tile;
        double tileY = realWorldY1 + (yKoordinate + 1) * meter_pro_tile;

        double offsetXinMeter = bbX1 - tileX;
        double offsetYinMeter = tileY - bbY2;

        Point2D.Double point = new Point2D.Double(offsetXinMeter, offsetYinMeter);

        return point;
    }

    public InputStream getInStream() {
        return this.inStream;
    }

    public void cancel() {
        if (inStream != null) {
            try {                
                inStream.close();                
            } catch (IOException ex) {
            }
        }
    }
    
}
