/*
 * SimpleWmsGetMapUrl.java
 *
 * Created on 14. M\u00E4rz 2005, 16:00
 */

package de.cismet.cismap.commons.raster.wms.simple;

import de.cismet.cismap.commons.rasterservice.*;

/**
 * Einfache Klasse um einen WMS Aufruf zu parametrisieren
 * nur eine WMS String mitschneiden und dann einfach die Breite, Hoehe und <br>
 * die Bounding Box durch eindeutige (und nicht zu kurze) Token ersetzen. Fertig :-)
 * <br><br>
 *Bsp:<br>
 *<code> http://geoportal.wuppertal.de/wms/wms?null&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&WIDTH=<cids:width>&HEIGHT=<cids:height>&BBOX=<cids:boundingBox>&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=false&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=R102:DGK5</code>
 *
 * @author hell
 */
public class SimpleWmsGetMapUrl {
    protected String urlTemplate;
    
    private String widthToken;
    private String heightToken;
    private String boundingBoxToken;
    
    private int width=0;
    private int height=0;
    
    double x1=0.0;
    double y1=0.0;
    double x2=0.0;
    double y2=0.0;
    
    public static String WIDTH_TOKEN="<cismap:width>";
    public static String HEIGHT_TOKEN="<cismap:height>";
    public static String BOUNDING_BOX_TOKEN="<cismap:boundingBox>";
    
    
    /** Creates a new instance of SimpleWmsGetMapUrl */
    public SimpleWmsGetMapUrl(String urlTemplate,String widthToken,String heightToken,String boundingBoxToken) {
        this.urlTemplate=urlTemplate;
        this.widthToken=widthToken;
        this.heightToken=heightToken;
        this.boundingBoxToken=boundingBoxToken;
    }
    public SimpleWmsGetMapUrl(String urlTemplate) {
        this.urlTemplate=urlTemplate;
        this.widthToken=WIDTH_TOKEN;
        this.heightToken=HEIGHT_TOKEN;
        this.boundingBoxToken=BOUNDING_BOX_TOKEN;
    }
    
    public void setWidth(int width) {
        this.width=width;
    }
    
    public void setHeight(int height) {
        this.height=height;
    }
    
    public void setX1(double coord) {
        x1=coord;
    }
    public void setY1(double coord) {
        y1=coord;
    }
    public void setX2(double coord) {
        x2=coord;
    }
    public void setY2(double coord) {
        y2=coord;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }
    
    public String toString() {
        String url=urlTemplate.replaceAll(widthToken, new Integer(width).toString());
        url=url.replaceAll(heightToken, new Integer(height).toString());
        url=url.replaceAll(boundingBoxToken, 
                new Double(x1).toString()+","+
                new Double(y1).toString()+","+
                new Double(x2).toString()+","+
                new Double(y2).toString());
        return url;
    
    }
    
    public static void main(String[] args) {
        SimpleWmsGetMapUrl test=new SimpleWmsGetMapUrl("http://geoportal.wuppertal.de/wms/wms?null&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&WIDTH=<cids:width>&HEIGHT=<cids:height>&BBOX=<cids:boundingBox>&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=false&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=R102:DGK5",
                "<cids:width>","<cids:height>","<cids:boundingBox>");
        test.setWidth(47);
        test.setHeight(11);
        test.setX1(1.1);
        test.setY1(2.2);
        test.setX2(3.3);
        test.setY2(4.4);
        System.out.println(test);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
