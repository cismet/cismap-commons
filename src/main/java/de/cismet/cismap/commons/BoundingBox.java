/*----------------    FILE HEADER  ------------------------------------------
 * This file is part of cismap (http://cismap.sourceforge.net)
 *
 * Copyright (C) 2004 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 *
 * Created on 9. September 2004, 10:58
 *SOURCEFORGETEST
 *
 *
 *
 *
 */
package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import edu.umd.cs.piccolo.util.PBounds;
import java.io.Serializable;
import org.apache.log4j.Logger;
//import org.deegree.gml.GMLGeometry;
//import org.deegree.model.geometry.GM_Envelope;
//import org.deegree.model.geometry.GM_Exception;
//import org.deegree.model.geometry.GM_Object;
//import org.deegree_impl.model.geometry.GMLAdapter;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**

 * Class to store a simple BoundingBox

 * @author  hell

 */

public class BoundingBox implements Cloneable,Serializable{
    private transient final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private double x1=-1;
    private double y1=-1;
    private double x2=-1;
    private double y2=-1;

    /**
     * Creates a new instance of BoundingBox
     * @param x1 x coordinate of the lower left point
     * @param y1 y coordinate of the lower left point
     * @param x2 x coordinate of the upper right point
     * @param y2 y coordinate of the upper right point
     */
    public BoundingBox(double x1,double y1,double x2,double y2) {
        if (x1<=x2) {
            this.x1=x1;
            this.x2=x2;
        }
        else {
            this.x1=x2;
            this.x2=x1;
        }
        if (y1<=y2) {
            this.y1=y1;
            this.y2=y2;
        }
        else {
            this.y1=y2;
            this.y2=y1;
        }
    }

    public BoundingBox(Element parent) throws DataConversionException{
        Element conf=parent.getChild("BoundingBox");//NOI18N
        setX1(conf.getAttribute("x1").getDoubleValue());//NOI18N
        setY1(conf.getAttribute("y1").getDoubleValue());//NOI18N
        setX2(conf.getAttribute("x2").getDoubleValue());//NOI18N
        setY2(conf.getAttribute("y2").getDoubleValue());//NOI18N
    }

    public BoundingBox(Geometry geom) {
        Geometry bb=geom.getEnvelope();
        if (bb instanceof Point) {
            setX1(((Point)bb).getX());
            setX2(((Point)bb).getX());
            setY1(((Point)bb).getY());
            setY2(((Point)bb).getY());
        }
        else if (bb instanceof Polygon) {
            //minx,miny
            setX1(((Polygon)bb).getExteriorRing().getCoordinateN(0).x);
            setY1(((Polygon)bb).getExteriorRing().getCoordinateN(0).y);
            //maxx,maxy
            setX2(((Polygon)bb).getExteriorRing().getCoordinateN(2).x);
            setY2(((Polygon)bb).getExteriorRing().getCoordinateN(2).y);
        }
        else {
            log.fatal("BoundingBox was not created by jtsGeometry:"+geom);//NOI18N
        }
    }


//  the classes GM_Object and GM_Envelope are not contained in the new cismetDeegree jar
//    public BoundingBox(GMLGeometry geom) {
//        try {
//            GM_Object gmo=GMLAdapter.wrap(geom);
//            GM_Envelope gme=gmo.getEnvelope();
//            setX1(gme.getMin().getX());
//            setX2(gme.getMax().getX());
//            setY1(gme.getMin().getY());
//            setY2(gme.getMax().getY());
//        }
//
//        catch (GM_Exception gmEx) {
//            log.error("Error during creating BoundingBox from GML",gmEx);//NOI18N
//        }
//    }

    /**
     * Empty Constructor
     */
    public BoundingBox() {
    }

     /**
     * Getter for property x1.
     * @return Value of property x1.
     */
    public double getX1() {
        if (x1<=x2) {
            return x1;
        }
        else {
            return x2;
        }
    }

    /**
     * Setter for property x1.
     * @param x1 New value of property x1.
     */
    public void setX1(double x1) {
        this.x1 = x1;
    }

    /**
     * Getter for property y1.
     * @return Value of property y1.
     */
    public double getY1() {
        if (y1<=y2){
            return y1;
        }
        else {
            return y2;
        }
    }

    /**
     * Setter for property y1.
     * @param y1 New value of property y1.
     */
    public void setY1(double y1) {
        this.y1 = y1;
    }

    /**
     * Getter for property x2.
     * @return Value of property x2.
     */
    public double getX2() {
        if (x2>x1) {
            return x2;
        }
        else {
            return x1;
        }
    }


    /**
     * Setter for property x2.
     * @param x2 New value of property x2.
     */
    public void setX2(double x2) {
        this.x2 = x2;
    }

    /**
     * Getter for property y2.
     * @return Value of property y2.
     */
    public double getY2() {
        if (y2>y1) {
            return y2;
        }
        else {
            return y1;
        }

    }

    /**
     * Setter for property y2.
     * @param y2 New value of property y2.
     */
    public void setY2(double y2) {
        this.y2 = y2;
    }


    /**
     * This Method return the BoundingBox like (1.11,2.22,3.33,4.44)
     * @return String value
     */
    public String toString() {

        return "("+round(getX1())+","+round(getY1())+","+round(getX2())+","+round(getY2())+")";//NOI18N
    }

    /**
     * This Method return the BoundingBox like -179.99999999999997,-105.48710130136223,176.35443037974682,114.6141645214226
     * @return String to assemble URL
     */
    public String getURLString() {
        return getX1()+","+getY1()+","+getX2()+","+getY2();//NOI18N
    }

    public String getGeometryFromTextLineString(){
        return "LINESTRING("+getX1()+" "+getY1()+","+getX2()+" "+getY2()+")";//NOI18N
    }
    /**
     * This Method return the BoundingBox like BOX3D(-179.99999999999997,-105.48710130136223,176.35443037974682,114.6141645214226)
     * @return String to work in GeometryFromText
     */
    public String getGeometryFromTextCompatibleString() {
        return "BOX3D("+getURLString()+")";//NOI18N
    }

    public Logger getLog() {
        return log;
    }


    public PBounds getPBounds(WorldToScreenTransform wtst) {
        return new PBounds(wtst.getScreenX(x1),wtst.getScreenY(y2),x2-x1,wtst.getScreenY(y1)-wtst.getScreenY(y2));
    }


    /**
     * return true if the given boundingBox has the same values
     * @param bb BoundingBox to be checked
     * @return true if the two Boxes are the same
     */
    boolean equals(BoundingBox bb) {
        return (getX1()==bb.getX1()&&getX2()==bb.getX2()&&getY1()==bb.getY1()&&getY2()==bb.getY2());
    }

    /**
     * Clone Method
     * @return a BoundingBox with the same values
     */
    public Object clone() {
        return new BoundingBox(x1,y1,x2,y2);
    }

    /**
     *
     *     Rounds a double to #.00
     *     @param d The double
     *     @return the new double
     *
     */
    public static String round(double d)   {
        double dd=((double)(Math.round(d*100)))/100;
        String pattern ="0.00";//NOI18N
        java.text.DecimalFormat myFormatter = new java.text.DecimalFormat(pattern);
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator('.');
        myFormatter.setDecimalFormatSymbols(symbols);
        return myFormatter.format(d);
    }

    public Element getJDOMElement() {
        Element e=new Element("BoundingBox");//NOI18N
        e.setAttribute("x1",new Double(x1).toString());//NOI18N
        e.setAttribute("y1",new Double(y1).toString());//NOI18N
        e.setAttribute("x2",new Double(x2).toString());//NOI18N
        e.setAttribute("y2",new Double(y2).toString());//NOI18N
        return e;
    }
//    public Geometry getGeometry() {
////        GeometryFactory gf=new GeometryFactory();
////        LinearRing lr=gf.c
////        gf.createPolygon()
//    }
    public double getWidth() {
        return Math.abs(x2-x1);
    }

    public double getHeight() {
        return Math.abs(y2-y1);
    }

   public String toGmlString() {
       return "<gml:Box><gml:coord><gml:X>"+getX1()+"</gml:X><gml:Y>"+getY1()+"</gml:Y></gml:coord>"+//NOI18N
                                   "<gml:coord><gml:X>"+getX2()+"</gml:X><gml:Y>"+getY2()+"</gml:Y></gml:coord>"+//NOI18N
                 "</gml:Box>";//NOI18N
   }
}