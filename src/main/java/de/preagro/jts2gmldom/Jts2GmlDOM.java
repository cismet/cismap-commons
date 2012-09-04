/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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

import com.vividsolutions.jts.geom.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <p>Class for creating DOM Elements containing valid GML representations of JTS geometries</p>
 *
 * <p>TODO currently doesn't do anything about reference systems</p>
 *
 * @author   nash
 * @version  0.1
 */
public class Jts2GmlDOM {

    //~ Static fields/initializers ---------------------------------------------

    /** logger for debugging purposes. */
    protected static final Logger LOGGER = Logger.getLogger("de.preagro.jts2gmldom");

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

    //~ Instance fields --------------------------------------------------------

    private Document doc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Jts2GmlDOM object.
     *
     * @throws  ParserConfigurationException  DOCUMENT ME!
     * @throws  FactoryConfigurationError     DOCUMENT ME!
     */
    public Jts2GmlDOM() throws ParserConfigurationException, FactoryConfigurationError {
        super();
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   theGeom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element geometryToGmlElement(final Geometry theGeom) throws DOMException {
        // first we need to know the type of the Geometry
        final int geomType = GeomTypes.classifyGeometry(theGeom);
        // then do the handling
        switch (geomType) {
            case GeomTypes.POINT: {
                return pointToGmlElement((Point)theGeom);
            }
            case GeomTypes.LINESTRING: {
                return lineStringToGmlElement((LineString)theGeom);
            }
            case GeomTypes.LINEARRING: {
                return linearRingToGmlElement((LinearRing)theGeom);
            }
            case GeomTypes.POLYGON: {
                return polygonToGmlElement((Polygon)theGeom);
            }
            case GeomTypes.GEOMETRYCOLLECTION: {
                return geometryCollectionToGmlElement((GeometryCollection)theGeom);
            }
            case GeomTypes.MULTIPOINT: {
                return multiPointToGmlElement((MultiPoint)theGeom);
            }
            case GeomTypes.MULTILINESTRING: {
                return multiLineStringToGmlElement((MultiLineString)theGeom);
            }
            case GeomTypes.MULTIPOLYGON: {
                return multiPolygonToGmlElement((MultiPolygon)theGeom);
            }
            case GeomTypes.UNKNOWN:
            default: {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   localName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String gmlQualifiedTag(final String localName) {
        return GML_PREFIX.concat(PREFIX_SEPARATOR).concat(localName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   elementName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    protected Element createElement(final String elementName) throws DOMException {
        return doc.createElementNS(GML_NAMESPACE, gmlQualifiedTag(elementName));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   el    DOCUMENT ME!
     * @param   text  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    protected void addTextToElement(final Element el, final String text) throws DOMException {
        final Node n = doc.createTextNode(text);
        el.appendChild(n);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   thePoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getFormattedCoord(final Coordinate thePoint) {
        return getFormattedCoord(thePoint, DEFAULT_LIST_SEPARATOR);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theCoord   DOCUMENT ME!
     * @param   separator  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getFormattedCoord(final Coordinate theCoord, final String separator) {
        // force non-scientific notation and up to 8 decimal places (should be
        // enough!)
        // HACK: also force ENGLISH locale
        // TODO internationalisation: should be able to specify locale (but then
        // also need to sort out separators and so on for the lists based on
        // locale)
        final DecimalFormat formatter = new DecimalFormat(
                EIGHT_DP_NUMBER_FORMAT,
                new DecimalFormatSymbols(Locale.ENGLISH));
        return (formatter.format(theCoord.x) + separator
                        + formatter.format(theCoord.y)
                        + (Double.isNaN(theCoord.z) ? "" : (separator + formatter.format(theCoord.z))));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theCoords  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getFormattedCoordList(final Coordinate[] theCoords) {
        return getFormattedCoordList(theCoords, DEFAULT_TUPLE_SEPARATOR,
                DEFAULT_LIST_SEPARATOR);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theCoords       DOCUMENT ME!
     * @param   tupleSeparator  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getFormattedCoordList(final Coordinate[] theCoords, final String tupleSeparator) {
        return getFormattedCoordList(theCoords, tupleSeparator,
                DEFAULT_LIST_SEPARATOR);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theCoords       DOCUMENT ME!
     * @param   tupleSeparator  DOCUMENT ME!
     * @param   listSeparator   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getFormattedCoordList(final Coordinate[] theCoords,
            final String tupleSeparator,
            final String listSeparator) {
        String coordList = EMPTY_STRING;
        for (int i = 0; i < theCoords.length; i++) {
            coordList += ((i > 0) ? listSeparator : EMPTY_STRING)
                        + getFormattedCoord(theCoords[i], tupleSeparator);
        }
        return coordList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   thePoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element pointToGmlElement(final Point thePoint) throws DOMException {
        final Element pointElement = createElement(POINT);
        final Element posElement = createElement(POS);
        addTextToElement(posElement,
            getFormattedCoord(thePoint.getCoordinate()));
        pointElement.appendChild(posElement);
        return pointElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theLineString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element lineStringToGmlElement(final LineString theLineString) throws DOMException {
        return linearThingToGmlElement(theLineString, LINESTRING);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theLinearRing  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element linearRingToGmlElement(final LinearRing theLinearRing) throws DOMException {
        return linearThingToGmlElement(theLinearRing, LINEARRING);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theLinearThing  DOCUMENT ME!
     * @param   elementName     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    protected Element linearThingToGmlElement(final LineString theLinearThing, final String elementName)
            throws DOMException {
        final Element lineStringElement = createElement(elementName);
        final Element posListElement = createElement(POSLIST);
        addTextToElement(posListElement, getFormattedCoordList(theLinearThing.getCoordinates()));
        lineStringElement.appendChild(posListElement);
        return lineStringElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   thePolygon  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element polygonToGmlElement(final Polygon thePolygon) throws DOMException {
        final Element polygonElement = createElement(POLYGON);
        polygonElement.appendChild(createMemberElement(thePolygon.getExteriorRing(), EXTERIOR));
        final int numInteriors = thePolygon.getNumInteriorRing();
        for (int i = 0; i < numInteriors; i++) {
            polygonElement.appendChild(createMemberElement(thePolygon.getInteriorRingN(i), INTERIOR));
        }
        return polygonElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theGeoms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element geometryCollectionToGmlElement(final GeometryCollection theGeoms) throws DOMException {
        return collectionToGmlElement(theGeoms, MULTIGEOMETRY, GEOMETRYMEMBER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theGeoms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element multiPointToGmlElement(final MultiPoint theGeoms) throws DOMException {
        return collectionToGmlElement(theGeoms, MULTIPOINT, POINTMEMBER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theGeoms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element multiLineStringToGmlElement(final MultiLineString theGeoms) throws DOMException {
        return collectionToGmlElement(theGeoms, MULTICURVE, CURVEMEMBER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theGeoms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    public Element multiPolygonToGmlElement(final MultiPolygon theGeoms) throws DOMException {
        return collectionToGmlElement(theGeoms, MULTISURFACE, SURFACEMEMBER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theGeoms         DOCUMENT ME!
     * @param   elementName      DOCUMENT ME!
     * @param   associationName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DOMException  DOCUMENT ME!
     */
    protected Element collectionToGmlElement(final GeometryCollection theGeoms,
            final String elementName,
            final String associationName) throws DOMException {
        final Element collectionElement = createElement(elementName);
        final int numGeoms = theGeoms.getNumGeometries();
        for (int i = 0; i < numGeoms; i++) {
            collectionElement.appendChild(createMemberElement(theGeoms.getGeometryN(i), associationName));
        }
        return collectionElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   theGeom          DOCUMENT ME!
     * @param   associationName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Element createMemberElement(final Geometry theGeom, final String associationName) {
        final Element geometryElement = geometryToGmlElement(theGeom);
        final Element associationElement = createElement(associationName);
        associationElement.appendChild(geometryElement);
        return associationElement;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class GeomTypes {

        //~ Static fields/initializers -----------------------------------------

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

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   theGeom  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public static int classifyGeometry(final Geometry theGeom) {
            final String geomType = theGeom.getGeometryType();
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
