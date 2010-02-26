/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons;

import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

/**
 *
 * @author thorsten
 */
public class SimpleGetFeatureInfoUrl extends SimpleWmsGetMapUrl {

    public static final String X_TOKEN = "<cismap:x>";
    public static final String Y_TOKEN = "<cismap:y>";
    private String xToken;
    private String yToken;
    
    int x=0;
    int y=0;
    
    public SimpleGetFeatureInfoUrl(String urlTemplate) {
        super(urlTemplate);
        xToken=X_TOKEN;
        yToken=Y_TOKEN;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

  

    @Override
    public String toString() {
        String url=super.toString();
        url=url.replaceAll(xToken, x+"");
        url=url.replaceAll(yToken, y+"");
        return url;
        
    }
    
    
}
