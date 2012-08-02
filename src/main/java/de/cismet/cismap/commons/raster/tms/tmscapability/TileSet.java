/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.raster.tms.tmscapability;

import de.cismet.cismap.commons.BoundingBox;
import java.util.Arrays;

/**
 *
 * @author cschmidt
 */
public class TileSet {
    private final String version;
    private final String host;
    private final String srs;
    private final BoundingBox boundingBox;
    private final Double[] resolutions;
    private final int width;
    private final int height;
    private final String format;
    private final String layer;
    private final String style;
    
    public TileSet(String version, String host, String srs, BoundingBox boundingBox, Double[] resolutions, 
            int width, int height, String format, String layer, String style){
        this.version = version;
        this.host = host;
        this.srs = srs;
        this.boundingBox = boundingBox;
        this.resolutions = resolutions;
        this.width = width;
        this.height = height;
        this.format = format;
        this.layer = layer;
        this.style = style;
    }

    public String getSRS() {
        return srs;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Double[] getResolutions() {
        return resolutions;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getFormat() {
        return format;
    }

    public String getLayer() {
        return layer;
    }

    public String getVersion() {
        return version;
    }

    public String getHost() {
        return host;
    }
    
     public String getStyle() {
        return style;
    }
    
    @Override
    public String toString(){
        return getLayer();
    }
    
//    @Override
//    public int hashCode(){
//        int result = 17;
//        int multiplikator = 37;
//        
//       result = multiplikator * result + getBoundingBox().hashCode();
//       result = multiplikator * result + getFormat().hashCode();
//       result = multiplikator * result + getHeight();
//       result = multiplikator * result + getWidth();
//       result = multiplikator * result + getHost().hashCode();
//       result = multiplikator * result + getLayer().hashCode();
//       result = multiplikator * result + Arrays.hashCode(getResolutions());
//       result = multiplikator * result + getSRS().hashCode();
//       result = multiplikator * result + getVersion().hashCode();
//       result = multiplikator * result + getStyle().hashCode();
//        
//        return result;
//    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TileSet other = (TileSet) obj;
        if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) {
            return false;
        }
        if (this.host != other.host && (this.host == null || !this.host.equals(other.host))) {
            return false;
        }
        if (this.srs != other.srs && (this.srs == null || !this.srs.equals(other.srs))) {
            return false;
        }
        if (this.boundingBox != other.boundingBox && (this.boundingBox == null || !this.boundingBox.equals(other.boundingBox))) {
            return false;
        }
        if (this.resolutions != other.resolutions && (this.resolutions == null || !Arrays.deepEquals(this.resolutions,other.resolutions))) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        if (this.format != other.format && (this.format == null || !this.format.equals(other.format))) {
            return false;
        }
        if (this.layer != other.layer && (this.layer == null || !this.layer.equals(other.layer))) {
            return false;
        }
        if (this.style != other.style && (this.style == null || !this.style.equals(other.style))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 83 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 83 * hash + (this.srs != null ? this.srs.hashCode() : 0);
        hash = 83 * hash + (this.boundingBox != null ? this.boundingBox.hashCode() : 0);
        hash = 83 * hash + (this.resolutions != null ? this.resolutions.hashCode() : 0);
        hash = 83 * hash + this.width;
        hash = 83 * hash + this.height;
        hash = 83 * hash + (this.format != null ? this.format.hashCode() : 0);
        hash = 83 * hash + (this.layer != null ? this.layer.hashCode() : 0);
        hash = 83 * hash + (this.style != null ? this.style.hashCode() : 0);
        return hash;
    }

   

}
