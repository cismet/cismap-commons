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
 * DOCUMENT ME!
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class CoordinateContainer {

    //~ Static fields/initializers ---------------------------------------------

    public static final String POINT = "POINT";               // NOI18N
    public static final String LINESTRING = "LINESTRING";     // NOI18N
    public static final String LINEARRING = "LINEARRING";     // NOI18N
    public static final String POLYGON = "POLYGON";           // NOI18N
    public static final String MULTIPOLYGON = "MULTIPOLYGON"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private Geometry geo;
    private GeometryCollection geoColl;
    private String type;
    private Coordinate[] coordArr;
    private float[] xp;
    private float[] yp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CoordinateContainer object.
     *
     * @param  geo  DOCUMENT ME!
     */
    public CoordinateContainer(final Geometry geo) {
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

    /**
     * Creates a new CoordinateContainer object.
     *
     * @param  geoColl  DOCUMENT ME!
     */
    public CoordinateContainer(final GeometryCollection geoColl) {
        type = MULTIPOLYGON;
        this.geoColl = geoColl;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void removeCoordinate() {
    }

    /**
     * DOCUMENT ME!
     */
    public void duplicateCoordinate() {
    }

    /**
     * DOCUMENT ME!
     */
    public void insertCoordinate() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate[] getCoordinates() {
        // TODO nur f\u00FCr einfache Typen ???
        return geo.getCoordinates();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate[] getSubCoordiantes(final int index) {
        // TODO soll ein bestimmtes Coordinate-Array zur\u00FCckgeben, z.B. das
        // 3. Polygon eines Multipolygons oder das 5. Loch eines Polygons
        return new Coordinate[1];
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ArrayList<Coordinate[]> getAllCoordinates() {
        return new ArrayList();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate[] getMergedCoordinates() {
        // TODO falls komplexer Geometrietyp, dann koordinaten aus Coordinate-Array
        // in einem einzigen Array hintereinander zusammengefasst
        // falls einfacher Geometrietyp, dann selbes Ergebnis wie getCoordinates()
        return new Coordinate[1];
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float[] getXp() {
        return xp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float[] getYp() {
        return yp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  index     DOCUMENT ME!
     * @param  newValue  DOCUMENT ME!
     */
    public void moveCoordinate(final int index, final Coordinate newValue) {
        // TODO zus\u00E4tzliche Methode mit 2 Indizes ??
    }

    /**
     * DOCUMENT ME!
     */
    public void syncGeometry() {
        // TODO vielleicht ab sofort \u00FCberfl\u00FCssig, da alles konsitent gehalten wird ??
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coordinateArr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate[] transformCoordinateArr(final Coordinate[] coordinateArr) {
        // TODO ben\u00F6tigt Offsets !!!
        return new Coordinate[1];
    }
}
