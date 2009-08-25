/*
 * WorldToScreenTransform.java
 * Copyright (C) 2005 by:
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
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 10. Januar 2006, 15:36
 *
 */

package de.cismet.cismap.commons;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WorldToScreenTransform {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    double xHome,yHome;
    /** Creates a new instance of WorldToScreenTransform */
    public WorldToScreenTransform(double x,double y) {
        xHome=x;
        yHome=y;
        log.debug("WorldToScreenTransform(x="+xHome+",y="+yHome+")");
    }
    
    
    public double getSourceX(double x) {
        return getWorldX(x);
    }
    public double getSourceY(double y) {
        return getWorldY(y);
    }
    public double getDestX(double x) {
        return getScreenX(x);
    }
    public double getDestY(double y) {
        return getScreenY(y);
    }
    
    public double getWorldX(double screenX) {
        return screenX+xHome;
    }
    public double getWorldY(double screenY) {
        return yHome-screenY;
    }
    public double getScreenX(double worldX) {
        return worldX-xHome;
    }
    public double getScreenY(double worldY) {
        return (-1.0)*(worldY-yHome);
    }
    
    
    private static String xyToScreen(WorldToScreenTransform wtst,double x, double y){
        return wtst.getScreenX(x)+","+wtst.getScreenY(y);
    }
    
    public static void main(String[] args) {
        WorldToScreenTransform wtst=new WorldToScreenTransform(-180,90);
        System.out.println(xyToScreen(wtst, -180,90));
        System.out.println(xyToScreen(wtst, -180,-90));
        System.out.println(xyToScreen(wtst, 180,-90));
        System.out.println(xyToScreen(wtst, 180,90));


//        System.out.println(wtst.getWorldX(wtst.getScreenX(0)));
//        System.out.println(wtst.getWorldY(wtst.getScreenY(0)));
        
        
    }
    
    public String toString() {
        return "de.cismet.cismap.commons.WorldToScreenTransform: xHome:"+xHome+ " yHome:"+yHome;
    }
    
    
    
    
}

