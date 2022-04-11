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
package de.cismet.cismap.commons.featureservice;

import org.deegree.datatypes.Types;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;

/**
 * The FeatureServiceUtilities class provides various methods to make the FeatureService- funcionality possible.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class FeatureServiceUtilities {

    //~ Static fields/initializers ---------------------------------------------

    private static final String ENCODING_STRING = "encoding='";
    private static final String ALTERNATE_ENCODING_STRING = "encoding=\"";

    public static final String[] GEO_PROPERTY_TYPES = {
            "GeometryPropertyType",
            "GeometricPrimtivePropertyType",
            "PointPropertyType",
            "CurvePropertyType",
            "SurfacePropertyType",
            "SolidPropertyType",
            "PointPropertyType",
            "CompositeCurveType",
            "CompositeSurfaceType",
            "CompositeSolidType",
            "GeometricComplexPropertyType",
            "MultiGeometryPropertyType",
            "MultiPointPropertyType",
            "MultiCurvePropertyType",
            "MultiSurfacePropertyType",
            "MultiSolidPropertyType",
            "MultiGeometryPropertyType",
            "MultiLineStringPropertyType",
            "GEOMETRY",
            String.valueOf(Types.GEOMETRY),
            String.valueOf(Types.MULTICURVE),
            String.valueOf(Types.MULTIGEOMETRY),
            String.valueOf(Types.MULTIPOINT),
            String.valueOf(Types.MULTISURFACE),
            String.valueOf(Types.POINT),
            String.valueOf(Types.CURVE),
            String.valueOf(Types.SURFACE),
            String.valueOf(Types.OTHER) // H2 Geometry type
        };
    /** Log4J initialisation. */
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.featureservice.FeatureServiceUtilities"); // NOI18N
    /** typestring of the string-type. */
    public static final String STRING_PROPERTY_TYPE = "string"; // NOI18N
    /** typestring of the integer-type. */
    public static final String INTEGER_PROPERTY_TYPE = "integer"; // NOI18N
    /** name of the name-attribute. */
    public static final String XML_NAME_STRING = "name"; // NOI18N
    /** name of the alias-attribute. */
    public static final String XML_ALIAS_STRING = "alias"; // NOI18N
    /** name of the type-attribute. */
    public static final String XML_TYPE_STRING = "type"; // NOI18N
    /** name of the isGeometry-attribute. */
    public static final String IS_GEOMETRY = "isGeometry"; // NOI18N
    /** name of the isVisible-attribute. */
    public static final String IS_VISIBLE = "isVisible"; // NOI18N
    /** name of the toName-attribute. */
    public static final String IS_NAME_ELEMENT = "isNameElement"; // NOI18N
    /** name of the GetFeature-element. */
    public static final String GET_FEATURE = "GetFeature"; // NOI18N
    /** WFS namespace-contant. */
    public static final Namespace WFS = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs"); // NOI18N

//  /** OGC namespace-contant */
//  public static final Namespace OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");//NOI18N
//  /** GML namespace-contant */
//  public static final Namespace GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");//NOI18N
//  /** OWS namespace-contant */
//  public static final Namespace OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");//NOI18N
//  /** XSD namespace-contant */
//  public static final Namespace xsd = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");//NOI18N
//  /** name of the CismapQuery-element */
//  public static final String CISMAP_QUERY = "CismapQuery";//NOI18N
//  /** name of the Query-element */
//  public static final String QUERY = "Query";//NOI18N
//  /** name of the CismapDescribeFeatureType-element */
//  public static final String CISMAP_DESCRIBEFEATURETYPE = "CismapDescribeFeatureType";//NOI18N
//  /** name of the DescribeFeatureType-element */
//  public static final String DESCRIBEFEATURETYPE = "DescribeFeatureType";//NOI18N
//  /** name of the CismapGetCapabilities-element */
//  public static final String CISMAP_GETCAPABILITIES = "CismapGetCapabilities";//NOI18N
//  /** name of the GetCapabilities-element */
//  public static final String GETCAPABILITIES = "GetCapabilities";//NOI18N
//  /** name of the ServiceIdentification-element */
//  public static final String SERVICE_IDENT = "ServiceIdentification";//NOI18N
//  /** name of the Filter-element */
//  public static final String FILTER = "Filter";//NOI18N
//  /** name of the BoundingBox-element */
//  public static final String BBOX = "BBOX";//NOI18N
//  /** name of the GetFeature-element */
//  public static final String TYPE_NAME = "typeName";//NOI18N
//  /** name of the GetFeature-element */
//  public static final String DEFAULT_TYPENAME = "TypeName";//NOI18N
//  /** name of the GetFeature-element */
//  public static final String PROPERTY_NAME = "PropertyName";//NOI18N
//  /** typestring of the GML-geometry-type */
//  public static final String GEO_PROPERTY_TYPE = "GeometryPropertyType";//NOI18N
//  /** typestring of the GML-geometry-type */
//  public static final String GEO_PROPERTY_TYPE_WITH_NS = "gml:GeometryPropertyType";//NOI18N
//  /** name of the maxFeatures-attribute */
//  public final static String MAX_FEATURES = "maxFeatures";//NOI18N

    static {
        Arrays.sort(GEO_PROPERTY_TYPES);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a string from a JDOM-element.
     *
     * @param   e  JDOM-element
     *
     * @return  XML-string
     */
    public static String elementToString(final Element e) {
        if (e == null) {
            return "";                                                           // NOI18N
        } else {
            final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat()); // NOI18N
            return out.outputString(e);
        }
    }

    /**
     * Returns name of the FeatureService if existing in the capabilities.
     *
     * @param   cap  DOCUMENT ME!
     *
     * @return  name of the FeatureService or the ServiceTypename, if no name found
     */
    public static String getServiceName(final WFSCapabilities cap) {
        String name = cap.getService().getTitle();

        if ((name == null) || name.equals("")) {
            name = cap.getService().getAbstract();
        }
        if ((name == null) || name.equals("")) {
            name = cap.getService().getName();
        }

        return name;
    }

    /**
     * Creates a string from a JDOM-document.
     *
     * @param   doc  JDOM-cocument
     *
     * @return  das String which represents the document
     */
    public static String parseDocumentToString(final Document doc) {
        if (log.isDebugEnabled()) {
            log.debug("parseDocumentToString()"); // NOI18N
        }
        final XMLOutputter out = new XMLOutputter();
        return out.outputString(doc);
    }

    /**
     * Creates a list of JDOM-elements containing all attributes of each FeatureType from the FeatureTypeList.
     *
     * @param   cap  postURL URL of the servers
     *
     * @return  List all FeatureTypes as element with their attributes as children
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    public static HashMap<FeatureType, Vector<FeatureServiceAttribute>> getElementDeclarations(
            final WFSCapabilities cap) throws IOException, Exception {
        if (log.isDebugEnabled()) {
            log.debug("getElementDeclarations(" + cap.getURL() + ")"); // NOI18N
        }
        // create hashmap that will be returned
        final HashMap<FeatureType, Vector<FeatureServiceAttribute>> result =
            new HashMap<FeatureType, Vector<FeatureServiceAttribute>>();

        for (final FeatureType ft : cap.getFeatureTypeList()) {
            final Vector<FeatureServiceAttribute> attributes = ft.getFeatureAttributes();
            if (attributes != null) {
                result.put(ft, attributes);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   typeName  e
     *
     * @return  true, if and only if the given type is a geometry type
     */
    public static boolean isElementOfGeometryType(final String typeName) {
        String localTypeName = typeName;

        // remove the namespace
        if (localTypeName.indexOf(":") != -1) {
            localTypeName = localTypeName.substring(localTypeName.indexOf(":") + 1);
        }

        final boolean res = (Arrays.binarySearch(GEO_PROPERTY_TYPES, localTypeName) >= 0);

        return res;
    }

    /**
     * Creates FeatureServiceAttributes by parsing the children of the delivered JDOM-element.
     *
     * @param   describeFeatureXML  JDOM-element
     *
     * @return  vector with FeatureServiceAttributes
     */
    public static HashMap<String, FeatureServiceAttribute> getFeatureServiceAttributes(
            final Element describeFeatureXML) {
        final HashMap<String, FeatureServiceAttribute> fsaMap = new HashMap(describeFeatureXML.getChildren().size());
        for (final Element currentElement : (List<Element>)describeFeatureXML.getChildren()) {
            try {
                final FeatureServiceAttribute fsa = new FeatureServiceAttribute(currentElement);
                fsaMap.put(fsa.getName(), fsa);
            } catch (Exception ex) {
                log.warn("An element could not be parsed as attribute: " + currentElement, ex); // NOI18N
            }
        }
        return fsaMap;
    }

    /**
     * Creates the ordered FeatureServiceAttributes list by parsing the children of the delivered JDOM-element.
     *
     * @param   describeFeatureXML  JDOM-element
     *
     * @return  list with the ordered FeatureServiceAttributes
     */
    public static List<String> getOrderedFeatureServiceAttributes(
            final Element describeFeatureXML) {
        final List<String> fsaList = new ArrayList(describeFeatureXML.getChildren().size());
        for (final Element currentElement : (List<Element>)describeFeatureXML.getChildren()) {
            try {
                final FeatureServiceAttribute fsa = new FeatureServiceAttribute(currentElement);
                fsaList.add(fsa.getName());
            } catch (Exception ex) {
                log.warn("An element could not be parsed as attribute: " + currentElement, ex); // NOI18N
            }
        }
        return fsaList;
    }

    /**
     * Reads the given stream and uses the charset that was given in the first line (if there is any).
     *
     * @param   is  reader is reader DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static StringBuilder readInputStream(final InputStream is) throws IOException {
        final StringBuilder res = new StringBuilder();
        final String charset = null;
        String tmp;
        final byte[] byteTmp = new byte[128];
        int count;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while ((count = is.read(byteTmp)) != -1) {
            bos.write(byteTmp, 0, count);
        }

        final byte[] bytesArray = bos.toByteArray();
        bos.close();

        final InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytesArray));
        final BufferedReader br = new BufferedReader(reader);

        while ((tmp = br.readLine()) != null) {
            res.append(tmp);
        }

        return res;
    }

    /**
     * Checks, if the given string contains the charset.
     *
     * @param   data  DOCUMENT ME!
     *
     * @return  the charset contained in the given string or null if no charset is contained
     */
    private static String checkForCharset(final String data) {
        int index = data.indexOf(ENCODING_STRING);

        if (index == -1) {
            index = data.indexOf(ALTERNATE_ENCODING_STRING);
        }

        if (index != -1) {
            final String subdata = data.substring(index + ENCODING_STRING.length());
            index = subdata.indexOf("'");

            if (index == -1) {
                index = subdata.indexOf("\"");
            }

            if (index != -1) {
                try {
                    final String charsetName = subdata.substring(0, index);
                    Charset.forName(charsetName);

                    return charsetName;
                } catch (Exception e) {
                    // no valid charset name. Nothing to do
                }
            }
        }

        return null;
    }
}
