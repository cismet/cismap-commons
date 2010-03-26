/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;

/**
 *
 * @author nh
 */
public class CoordinateContainer {
    public static final String POINT = "POINT";//NOI18N
    public static final String LINESTRING = "LINESTRING";//NOI18N
    public static final String LINEARRING = "LINEARRING";//NOI18N
    public static final String POLYGON = "POLYGON";//NOI18N
    public static final String MULTIPOLYGON = "MULTIPOLYGON";//NOI18N

    private Geometry geo;
    private GeometryCollection geoColl;
    private String type;
    private Coordinate[] coordArr;
    private float[] xp;
    private float[] yp;
    
    public CoordinateContainer(Geometry geo) {
        if (geo instanceof Point) {
            type = POINT;
        } else if (geo instanceof LineString) {
            type = LINESTRING;
        } else if (geo instanceof LinearRing) {
            type = LINEARRING;
        } else if (geo instanceof Polygon) {
            type = POLYGON;
        }
        this.geo = geo;
    }
    
    public CoordinateContainer(GeometryCollection geoColl) {
        type = MULTIPOLYGON;
        this.geoColl = geoColl;
    }
    
    public void removeCoordinate() {
        
    }
    
    public void duplicateCoordinate() {
        
    }
    
    public void insertCoordinate() {
        
    }
    
    public Coordinate[] getCoordinates() {
        // TODO nur f\u00FCr einfache Typen ???
        return geo.getCoordinates();
    }
    
    public Coordinate[] getSubCoordiantes(int index) {
        // TODO soll ein bestimmtes Coordinate-Array zur\u00FCckgeben, z.B. das
        // 3. Polygon eines Multipolygons oder das 5. Loch eines Polygons
        return new Coordinate[1];
    }
    
    public ArrayList<Coordinate[]> getAllCoordinates() {
        return new ArrayList();
    }
    
    public Coordinate[] getMergedCoordinates() {
        // TODO falls komplexer Geometrietyp, dann koordinaten aus Coordinate-Array
        // in einem einzigen Array hintereinander zusammengefasst
        // falls einfacher Geometrietyp, dann selbes Ergebnis wie getCoordinates()
        return new Coordinate[1];
    }
    
    public float[] getXp() {
        return xp;
    }
    
    public float[] getYp() {
        return yp;
    }
    
    public void moveCoordinate(int index, Coordinate newValue) {
        // TODO zus\u00E4tzliche Methode mit 2 Indizes ??
    }
    
    public void syncGeometry() {
        // TODO vielleicht ab sofort \u00FCberfl\u00FCssig, da alles konsitent gehalten wird ??
    }
    
    private Coordinate[] transformCoordinateArr(Coordinate[] coordinateArr) {
        // TODO ben\u00F6tigt Offsets !!!
        return new Coordinate[1];
    }
    
}
