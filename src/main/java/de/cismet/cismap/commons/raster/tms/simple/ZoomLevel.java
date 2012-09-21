/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.raster.tms.simple;

/**
 *
 * @author cschmidt
 */
public class ZoomLevel {
    private int zoomLevel = 0;
    private double resolution = 0.0;
    private int scaleDenominator = 0;
        
        
        public ZoomLevel(int zoomLevel, double resolution, int scale){
            this.zoomLevel = zoomLevel;
            this.resolution = resolution;
            this.scaleDenominator = scale;
        }

        public int getZoomLevel() {
            return zoomLevel;
        }

        public void setZoomLevel(int zoomLevel) {
            this.zoomLevel = zoomLevel;
        }

        public double getResolution() {
            return resolution;
        }

        public void setResolution(double resolution) {
            this.resolution = resolution;
        }

        public int getScale() {
            return scaleDenominator;
        }

        public void setScale(int scale) {
            this.scaleDenominator = scale;
        }
        
        @Override
        public boolean equals(Object o){
            if(o == null && !(o instanceof ZoomLevel))
                return false;
            else if(((ZoomLevel)o).getScale() != this.getScale())
                return false;
            else
                return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 13 * hash + this.zoomLevel;
            hash = 13 * hash + (int) (Double.doubleToLongBits(this.resolution) ^ (Double.doubleToLongBits(this.resolution) >>> 32));
            hash = 13 * hash + this.scaleDenominator;
            return hash;
        }
}
