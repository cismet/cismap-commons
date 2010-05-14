/*
 * Created 27.10.2005 15:33:24 by nash
 *
 * Class for creating DOM Elements containing valid GML representations of JTS geometries
 *
 * 2005 Ed Nash / Universitt Rostock
 *
 * License: GPL (see http://www.gnu.org/licenses/gpl.txt)
 */
package de.preagro.jts2gmldom;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * <p>
 * Class for creating DOM Elements containing valid GML representations of JTS
 * geometries
 * </p>
 * <p>
 * TODO currently doesn't do anything about reference systems
 * </p>
 * 
 * @author nash
 * 
 * @version 0.1
 */
public class Jts2GmlDOM {
    /**
     * logger for debugging purposes
     */
    protected static final Logger LOGGER = Logger
            .getLogger("de.preagro.jts2gmldom");

    private static final String EMPTY_STRING = "";
    
    public static final String EIGHT_DP_NUMBER_FORMAT = "0.########";
    
    public static final String GML_NAMESPACE = "http://www.opengis.net/gml";

    public static final String GML_PREFIX = "gml";
    
    public static final String PREFIX_SEPARATOR = ":";

    public static final String DEFAULT_LIST_SEPARATOR = " ";

    public static final String DEFAULT_TUPLE_SEPARATOR = ",";

    public static final String POINT = "Point";

    public static final String POS = "pos";

    public static final String LINESTRING = "LineString";

    public static final String LINEARRING = "LinearRing";

    public static final String POSLIST = "posList";

    public static final String POLYGON = "Polygon";

    public static final String EXTERIOR = "exterior";

    public static final String INTERIOR = "interior";

    public static final String MULTIGEOMETRY = "MultiGeometry";

    public static final String GEOMETRYMEMBER = "geometryMember";

    public static final String MULTICURVE = "MultiCurve";

    public static final String CURVEMEMBER = "curveMember";

    public static final String MULTIPOINT = "MultiPoint";

    public static final String POINTMEMBER = "pointMember";

    public static final String MULTISURFACE = "MultiSurface";

    public static final String SURFACEMEMBER = "surfaceMember";

    private Document doc;

    /**
     *  
     */
    public Jts2GmlDOM() throws ParserConfigurationException,
            FactoryConfigurationError {
        super();
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
    }

    public Element geometryToGmlElement(Geometry theGeom) throws DOMException {
        // first we need to know the type of the Geometry
        int geomType = GeomTypes.classifyGeometry(theGeom);
        // then do the handling
        switch (geomType) {
        case GeomTypes.POINT:
            return pointToGmlElement((Point) theGeom);
        case GeomTypes.LINESTRING:
            return lineStringToGmlElement((LineString) theGeom);
        case GeomTypes.LINEARRING:
            return linearRingToGmlElement((LinearRing) theGeom);
        case GeomTypes.POLYGON:
            return polygonToGmlElement((Polygon) theGeom);
        case GeomTypes.GEOMETRYCOLLECTION:
            return geometryCollectionToGmlElement((GeometryCollection) theGeom);
        case GeomTypes.MULTIPOINT:
            return multiPointToGmlElement((MultiPoint) theGeom);
        case GeomTypes.MULTILINESTRING:
            return multiLineStringToGmlElement((MultiLineString) theGeom);
        case GeomTypes.MULTIPOLYGON:
            return multiPolygonToGmlElement((MultiPolygon) theGeom);
        case GeomTypes.UNKNOWN:
        default:
            return null;
        }
    }

    public static String gmlQualifiedTag(String localName) {
        return GML_PREFIX.concat(PREFIX_SEPARATOR).concat(localName);
    }

    protected Element createElement(String elementName) throws DOMException {
        return doc.createElementNS(GML_NAMESPACE, gmlQualifiedTag(elementName));
    }

    protected void addTextToElement(Element el, String text)
            throws DOMException {
        Node n = doc.createTextNode(text);
        el.appendChild(n);
    }

    protected String getFormattedCoord(Coordinate thePoint) {
        return getFormattedCoord(thePoint, DEFAULT_LIST_SEPARATOR);
    }

    protected String getFormattedCoord(Coordinate theCoord, String separator) {
        // force non-scientific notation and up to 8 decimal places (should be
        // enough!)
        // HACK: also force ENGLISH locale
        // TODO internationalisation: should be able to specify locale (but then
        // also need to sort out separators and so on for the lists based on
        // locale)
        DecimalFormat formatter = new DecimalFormat(EIGHT_DP_NUMBER_FORMAT,
                new DecimalFormatSymbols(Locale.ENGLISH));
        return (formatter.format(theCoord.x) + separator
                + formatter.format(theCoord.y) + (Double.isNaN(theCoord.z) ? ""
                : separator + formatter.format(theCoord.z)));
    }

    protected String getFormattedCoordList(Coordinate[] theCoords) {
        return getFormattedCoordList(theCoords, DEFAULT_TUPLE_SEPARATOR,
                DEFAULT_LIST_SEPARATOR);
    }

    protected String getFormattedCoordList(Coordinate[] theCoords,
            String tupleSeparator) {
        return getFormattedCoordList(theCoords, tupleSeparator,
                DEFAULT_LIST_SEPARATOR);
    }

    protected String getFormattedCoordList(Coordinate[] theCoords,
            String tupleSeparator, String listSeparator) {
        String coordList = EMPTY_STRING;
        for (int i = 0; i < theCoords.length; i++) {
            coordList += ((i > 0) ? listSeparator : EMPTY_STRING)
                    + getFormattedCoord(theCoords[i], tupleSeparator);
        }
        return coordList;
    }

    public Element pointToGmlElement(Point thePoint) throws DOMException {
        Element pointElement = createElement(POINT);
        Element posElement = createElement(POS);
        addTextToElement(posElement,
                getFormattedCoord(thePoint.getCoordinate()));
        pointElement.appendChild(posElement);
        return pointElement;
    }

    public Element lineStringToGmlElement(LineString theLineString)
            throws DOMException {
        return linearThingToGmlElement(theLineString, LINESTRING);
    }

    public Element linearRingToGmlElement(LinearRing theLinearRing)
            throws DOMException {
        return linearThingToGmlElement(theLinearRing, LINEARRING);
    }

    protected Element linearThingToGmlElement(LineString theLinearThing,
            String elementName) throws DOMException {
        Element lineStringElement = createElement(elementName);
        Element posListElement = createElement(POSLIST);
        addTextToElement(posListElement, getFormattedCoordList(theLinearThing
                .getCoordinates()));
        lineStringElement.appendChild(posListElement);
        return lineStringElement;
    }

    public Element polygonToGmlElement(Polygon thePolygon) throws DOMException {
        Element polygonElement = createElement(POLYGON);
        polygonElement.appendChild(createMemberElement(thePolygon
                .getExteriorRing(), EXTERIOR));
        int numInteriors = thePolygon.getNumInteriorRing();
        for (int i = 0; i < numInteriors; i++) {
            polygonElement.appendChild(createMemberElement(thePolygon
                    .getInteriorRingN(i), INTERIOR));
        }
        return polygonElement;
    }

    public Element geometryCollectionToGmlElement(GeometryCollection theGeoms)
            throws DOMException {
        return collectionToGmlElement(theGeoms, MULTIGEOMETRY, GEOMETRYMEMBER);
    }

    public Element multiPointToGmlElement(MultiPoint theGeoms)
            throws DOMException {
        return collectionToGmlElement(theGeoms, MULTIPOINT, POINTMEMBER);
    }

    public Element multiLineStringToGmlElement(MultiLineString theGeoms)
            throws DOMException {
        return collectionToGmlElement(theGeoms, MULTICURVE, CURVEMEMBER);
    }

    public Element multiPolygonToGmlElement(MultiPolygon theGeoms)
            throws DOMException {
        return collectionToGmlElement(theGeoms, MULTISURFACE, SURFACEMEMBER);
    }

    protected Element collectionToGmlElement(GeometryCollection theGeoms,
            String elementName, String associationName) throws DOMException {
        Element collectionElement = createElement(elementName);
        int numGeoms = theGeoms.getNumGeometries();
        for (int i = 0; i < numGeoms; i++) {
            collectionElement.appendChild(createMemberElement(theGeoms
                    .getGeometryN(i), associationName));
        }
        return collectionElement;
    }

    protected Element createMemberElement(Geometry theGeom,
            String associationName) {
        Element geometryElement = geometryToGmlElement(theGeom);
        Element associationElement = createElement(associationName);
        associationElement.appendChild(geometryElement);
        return associationElement;
    }

    private static class GeomTypes {
        public static final int UNKNOWN = 0;

        public static final String POINT_NAME = "Point";

        public static final int POINT = 10;

        public static final String LINESTRING_NAME = "LineString";

        public static final int LINESTRING = 20;

        public static final String LINEARRING_NAME = "LinearRing";

        public static final int LINEARRING = 21;

        public static final String POLYGON_NAME = "Polygon";

        public static final int POLYGON = 30;

        public static final String GEOMETRYCOLLECTION_NAME = "GeometryCollection";

        public static final int GEOMETRYCOLLECTION = 40;

        public static final String MULTIPOINT_NAME = "MultiPoint";

        public static final int MULTIPOINT = 41;

        public static final String MULTILINESTRING_NAME = "MultiLineString";

        public static final int MULTILINESTRING = 42;

        public static final String MULTIPOLYGON_NAME = "MultiPolygon";

        public static final int MULTIPOLYGON = 43;

        public static int classifyGeometry(Geometry theGeom) {
            String geomType = theGeom.getGeometryType();
            if (geomType.equals(POINT_NAME)) {
                return POINT;
            } else if (geomType.equals(LINESTRING_NAME)) {
                return LINESTRING;
            } else if (geomType.equals(LINEARRING_NAME)) {
                return LINEARRING;
            } else if (geomType.equals(POLYGON_NAME)) {
                return POLYGON;
            } else if (geomType.equals(GEOMETRYCOLLECTION_NAME)) {
                return GEOMETRYCOLLECTION;
            } else if (geomType.equals(MULTIPOINT_NAME)) {
                return MULTIPOINT;
            } else if (geomType.equals(MULTILINESTRING_NAME)) {
                return MULTILINESTRING;
            } else if (geomType.equals(MULTIPOLYGON_NAME)) {
                return MULTIPOLYGON;
            } else {
                return UNKNOWN;
            }
        }

    }
}
