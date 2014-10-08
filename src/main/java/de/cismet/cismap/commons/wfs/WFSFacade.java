/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 therter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.wfs;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;

import de.cismet.commons.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

/**
 * This class invokes Web Feature services and creates WFS requests. This class considers the WFS version while creating
 * WFS requests and should be the only class that is used to create and modify WFS request. The currently supported WFS
 * versions are 1.0.0 and 1.1.0
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WFSFacade {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(WFSFacade.class);
    /** WFS namespace-contant. */
    private static final Namespace WFS = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs"); // NOI18N
    /** OGC namespace-contant. */
    private static final Namespace OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc"); // NOI18N
    /** GML namespace-contant. */
    private static final Namespace GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml"); // NOI18N
    /** OWS namespace-contant. */
    private static final Namespace OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows"); // NOI18N
    /** XSD namespace-contant. */
    private static final Namespace xsd = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema"); // NOI18N
    /** name of the CismapQuery-element. */
    private static final String GET_FEATURE_100 = "getFeatureQuery100";                     // NOI18N
    private static final String GET_FEATURE_110 = "getFeatureQuery110";                     // NOI18N
    private static final String CISMET_DESCRIBE_FEATURE_TYPE = "CismapDescribeFeatureType"; // NOI18N
    private static final String DESCRIBE_FEATURE_TYPE = "DescribeFeatureType";              // NOI18N
    private static final String GET_FEATURE = "GetFeature";                                 // NOI18N
    private static final String QUERY = "Query";                                            // NOI18N
    private static final String FILTER = "Filter";                                          // NOI18N
    private static final String BBOX = "BBOX";                                              // NOI18N
    private static final String TYPENAME = "TypeName";                                      // NOI18N
    private static final String PROPERTY_NAME = "PropertyName";                             // NOI18N
    private static final String TYPE_NAME_ATTR = "typeName";                                // NOI18N
    private static final String VERSION_ATTR = "version";                                   // NOI18N
    private static final String MAX_FEATURES_ATTR = "maxFeatures";                          // NOI18N
    private static final URL XML_FILE = WFSFacade.class.getResource("wfs.xml");             // TODO Auslagern//NOI18N
    public static final String CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER = "<cismapBoundingBoxAsGmlPlaceholder />";
    public static final String CISMAP_RESULT_TYPE_PLACEHOLDER = "cismapResultTypePlaceholder";
    public static final String SRS_NAME_PLACEHOLDER = "SRSNAME_PLACEHOLDER";

    //~ Instance fields --------------------------------------------------------

    private WFSCapabilities cap;
    private ResponseParserFactory parserFactory;
    private Element rootNode;

    //~ Constructors -----------------------------------------------------------

    /**
     * this constructor should only be used by the WFSCapabilities classes. Because every WFSFacade is bounded to a
     * capabilities class and should only be used with features of the given capabilities document.
     *
     * @param  cap            DOCUMENT ME!
     * @param  parserFactory  DOCUMENT ME!
     */
    public WFSFacade(final WFSCapabilities cap, final ResponseParserFactory parserFactory) {
        this.cap = cap;
        this.parserFactory = parserFactory;
        try {
            final SAXBuilder builder = new SAXBuilder();
            rootNode = builder.build(XML_FILE).getRootElement();
        } catch (Exception ex) {
            logger.error("Error during parsing of the CismapXML-Files", ex); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * sends a describeFeatureType request to the corresponding wfs.
     *
     * @param   feature  the feature, the request should be sent for
     *
     * @return  the wfs response as FeatureTypeDescription object
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    public FeatureTypeDescription describeFeatureType(final FeatureType feature) throws IOException, Exception {
        final String version = cap.getVersion();
        final String request;
        final XMLOutputter out = new XMLOutputter();
        final QName featureName = feature.getName();
        final Element requestElement = (Element)rootNode.getChild(CISMET_DESCRIBE_FEATURE_TYPE)
                    .getChild(DESCRIBE_FEATURE_TYPE, WFS)
                    .clone();

        // set version
        if (!version.equals("1.0.0") && !version.equals("1.1.0")) {                                     // NOI18N
            logger.error("unknown service version used: " + version + " service"                        // NOI18N
                        + cap.getURL() + ". Try to use a 1.1.0 request with version string" + version); // NOI18N
        }
        requestElement.getAttribute(VERSION_ATTR).setValue(version);                                    // NOI18N

        // set namespace
        if ((featureName.getPrefix() != null) && (featureName.getNamespaceURI() != null)) {
            final Namespace ns = Namespace.getNamespace(featureName.getPrefix(), featureName.getNamespaceURI());
            requestElement.addNamespaceDeclaration(ns);
        }

        // set feature type
        requestElement.getChild(TYPENAME, WFS).setText(feature.getPrefixedNameString());

        request = out.outputString(requestElement);
        final String response = postRequest(cap.getURL(), request);

        try {
            return parserFactory.getFeatureTypeDescription(response, feature);
        } catch (Exception e) {
            logger.error("Error while parsing the response of a describeFeature request.", e); // NOI18N
            return null;
        }
    }

    /**
     * Returns a template for a getFeature request. The template contains placeholders for the bounding box, the srs
     * name and the property names. To replace the placeholders, use the following static methods:<br />
     * setGetFeatureBoundingBox setGeometry changePropertyNames setMaxFeatureCount
     *
     * @param   feature  the request template will be created for this feature
     *
     * @return  a getFeature request template. The WFS version will be considered.
     */
    public Element getGetFeatureQuery(final FeatureType feature) {
        final String version = cap.getVersion();
        Element request;

        if (version.equals("1.0.0")) {                                            // NOI18N
            request = getFeature100Request(feature);
        } else if (version.equals("1.1.0")) {                                     // NOI18N
            request = getFeature110Request(feature);
        } else {
            logger.error("unknown service version used: " + version + " service"  // NOI18N
                        + cap.getURL() + ". Try to use a version 1.1.0 request"); // NOI18N
            request = getFeature110Request(feature);
        }

        return request;
    }

    /**
     * Set the bounding box and the srs of the given getFeature request and returns the resulting request as string.
     *
     * @param   query             the getFeature request template.
     * @param   bbox              the bounding box that should be used in the getFeature request
     * @param   feature           the type of the feature from the given query
     * @param   mapCrs            the crs of the map, the features should be shown on
     * @param   reverseAxisOrder  if true, the axis order will be lat/lon
     *
     * @return  the new getFeature request
     */
    public String setGetFeatureBoundingBox(final String query,
            final XBoundingBox bbox,
            final FeatureType feature,
            final String mapCrs,
            final boolean reverseAxisOrder) {
        return setGetFeatureBoundingBox(query, bbox, feature, mapCrs, reverseAxisOrder, false);
    }

    /**
     * Set the bounding box and the srs of the given getFeature request and returns the resulting request as string.
     *
     * @param   query             the getFeature request template.
     * @param   bbox              the bounding box that should be used in the getFeature request
     * @param   feature           the type of the feature from the given query
     * @param   mapCrs            the crs of the map, the features should be shown on
     * @param   reverseAxisOrder  if true, the axis order will be lat/lon
     * @param   onlyFeatureCount  the resultType attribute will be set to hits, iif onlyFeatureCount is true
     *
     * @return  the new getFeature request
     */
    public String setGetFeatureBoundingBox(final String query,
            final XBoundingBox bbox,
            final FeatureType feature,
            final String mapCrs,
            final boolean reverseAxisOrder,
            final boolean onlyFeatureCount) {
        String request;
        String envelope;
        final String crs = getOptimalCrsForFeature(feature, mapCrs);
        final Geometry geom = CrsTransformer.transformToGivenCrs(bbox.getGeometry(), crs);
        final XBoundingBox tbbox = new XBoundingBox(geom);
        final String resultType = (onlyFeatureCount ? "hits" : "results");

        if (logger.isDebugEnabled()) {
            logger.debug("optimal crs: " + crs);
        }

        if ((cap.getVersion() != null) && cap.getVersion().equals("1.0.0")) {                                 // NOI18N
            envelope = "<gml:Box><gml:coord><gml:X>" + tbbox.getX1() + "</gml:X><gml:Y>" + tbbox.getY1()      // NOI18N
                        + "</gml:Y></gml:coord>" + "<gml:coord><gml:X>" + tbbox.getX2()                       // NOI18N
                        + "</gml:X><gml:Y>" + tbbox.getY2() + "</gml:Y></gml:coord>" + "</gml:Box>";          // NOI18N
        } else if ((cap.getVersion() != null) && cap.getVersion().equals("1.1.0")) {                          // NOI18N
            if (reverseAxisOrder) {
                envelope = "<gml:Envelope><gml:lowerCorner>" + tbbox.getY1()                                  // NOI18N
                            + " " + tbbox.getX1() + "</gml:lowerCorner>" + "<gml:upperCorner>"                // NOI18N
                            + tbbox.getY2() + " " + tbbox.getX2() + "</gml:upperCorner>" + "</gml:Envelope>"; // NOI18N
            } else {
                envelope = "<gml:Envelope><gml:lowerCorner>" + tbbox.getX1()                                  // NOI18N
                            + " " + tbbox.getY1() + "</gml:lowerCorner>" + "<gml:upperCorner>"                // NOI18N
                            + tbbox.getX2() + " " + tbbox.getY2() + "</gml:upperCorner>" + "</gml:Envelope>"; // NOI18N
            }
        } else {
            logger.error("unknown service version used: " + cap.getVersion()                                  // NOI18N
                        + ". Try to use a version 1.1.0 request");                                            // NOI18N
            if (reverseAxisOrder) {
                envelope = "<gml:Envelope><gml:lowerCorner>" + tbbox.getY1()                                  // NOI18N
                            + " " + tbbox.getX1() + "</gml:lowerCorner>" + "<gml:upperCorner>"                // NOI18N
                            + tbbox.getY2() + " " + tbbox.getX2() + "</gml:upperCorner>" + "</gml:Envelope>"; // NOI18N
            } else {
                envelope = "<gml:Envelope><gml:lowerCorner>" + tbbox.getX1()                                  // NOI18N
                            + " " + tbbox.getY1() + "</gml:lowerCorner>" + "<gml:upperCorner>"                // NOI18N
                            + tbbox.getX2() + " " + tbbox.getY2() + "</gml:upperCorner>" + "</gml:Envelope>"; // NOI18N
            }
        }

        request = query.toString().replaceAll(CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER, envelope);
        request = request.replaceAll(SRS_NAME_PLACEHOLDER, crs);
        request = request.replaceAll(CISMAP_RESULT_TYPE_PLACEHOLDER, resultType);

        return request;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   mapSrs   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getOptimalCrsForFeature(final FeatureType feature, final String mapSrs) {
        String desiredSrs = mapSrs;

        if (desiredSrs == null) {
            desiredSrs = CismapBroker.getInstance().getSrs().getCode();
        }

        for (final String tmpSrs : feature.getSupportedSRS()) {
            if (tmpSrs.equals(desiredSrs)) {
                return tmpSrs;
            }
        }

        if (feature.getDefaultSRS() != null) {
            return feature.getDefaultSRS();
        } else {
            return mapSrs;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   request  DOCUMENT ME!
     * @param   cap      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureType extractRequestedFeatureType(final String request, final WFSCapabilities cap) {
        try {
            final SAXBuilder builder = new SAXBuilder();
            final Element root = builder.build(new StringReader(request)).getRootElement();
            final Attribute att = root.getChild(QUERY, WFS).getAttribute(TYPE_NAME_ATTR);
            if (att != null) {
                return extractFeatureTypeFromCap(att.getValue(), cap);
            }
        } catch (Exception ex) {
            logger.error("Error during parsing of the wfs request. The feature type cannot be recognized.", ex); // NOI18N
        }

        logger.error(
            "The feature type cannot be extracted from the wfs request. So the supported crs cannot be determined exactly."); // NOI18N

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureName  DOCUMENT ME!
     * @param   cap          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureType extractFeatureTypeFromCap(final String featureName, final WFSCapabilities cap) {
        try {
            if (featureName != null) {
                final Iterator<FeatureType> featureIterator = cap.getFeatureTypeList().iterator();
                final String localFeatureName = featureName.substring(featureName.indexOf(":") + 1);

                while (featureIterator.hasNext()) {
                    final FeatureType feature = featureIterator.next();
                    if (feature.getName().getLocalPart().equals(localFeatureName)) {
                        return feature;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error while discovering the feature list.", ex); // NOI18N
        }

        logger.error("The feature type with the name " + featureName + " cannot be found."); // NOI18N
        return null;
    }

    /**
     * Replaces the current max feature count with the given max feature count in the given getFeature request.
     *
     * @param  wfsQuery         query getFeature request as JDOM-element
     * @param  maxFeatureCount  properties collection of strings
     * @param  version          the version of the wfs request that is contained in the query paramaeter
     */
    public static void setMaxFeatureCount(final Element wfsQuery, final int maxFeatureCount, String version) {
        if (version == null) {
            logger.error("version string is null. Try to use version 1.1.0", new Exception());
            version = "1.1.0";
        }
        if (!(version.equals("1.0.0") || version.equals("1.1.0"))) {
            logger.error("unknown wfs version: " + version // NOI18N
                        + ". Try to handle this version like version 1.1.0"); // NOI18N
        }
        if (logger.isDebugEnabled()) {
            logger.debug("setting may maxFeatureCount of WFS Query to " + maxFeatureCount); // NOI18N
        }

        if (wfsQuery != null) {
            wfsQuery.setAttribute(MAX_FEATURES_ATTR, String.valueOf(maxFeatureCount));
        } else {
            logger.warn("could not set maxFeatureCount, query not yet initialised"); // NOI18N
        }
    }

    /**
     * Replaces the current property names with the given property names in the getFeature query.
     *
     * @param  query       getFeature request as JDOM-element
     * @param  properties  the property names
     * @param  version     the version of the wfs request that is contained in the query paramaeter
     */
    public static void changePropertyNames(final Element query, final Collection<String> properties, String version) {
        if (version == null) {
            logger.error("version string is null. Try to use version 1.1.0", new Exception());
            version = "1.1.0";
        }
        if (!(version.equals("1.0.0") || version.equals("1.1.0"))) {          // NOI18N
            logger.error("unknown wfs version: " + version                    // NOI18N
                        + ". Try to handle this version like version 1.1.0"); // NOI18N
        }
        query.getChild(QUERY, WFS).removeChildren(PROPERTY_NAME, WFS);

        for (final String s : properties) {
            final Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(s);
            query.getChild(QUERY, WFS).addContent(tmp);
        }
    }

    /**
     * Replaces the geometry-property in the given getFeature query template.
     *
     * @param  query    getFeature request as JDOM-element
     * @param  geoName  the name of the geometry
     * @param  version  the version of the wfs request that is contained in the query paramaeter
     */
    public static void setGeometry(final Element query, final String geoName, String version) {
        if (version == null) {
            logger.error("version string is null. Try to use version 1.1.0", new Exception());
            version = "1.1.0";
        }
        if (!(version.equals("1.0.0") || version.equals("1.1.0"))) {          // NOI18N
            logger.error("unknown wfs version: " + version                    // NOI18N
                        + ". Try to handle this version like version 1.1.0"); // NOI18N
        }
        query.getChild(QUERY, WFS)
                .getChild(FILTER, OGC)
                .getChild(BBOX, OGC)
                .getChild(PROPERTY_NAME, OGC)
                .setText(geoName);
    }

    /**
     * Creates a getFeature request template for the given feature type. The request template is conform to a WFS of the
     * version 1.0.0
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  null, if the given feature has no geometry attribute
     */
    private Element getFeature100Request(final FeatureType feature) {
        final Element requestElement = (Element)rootNode.getChild(GET_FEATURE_100).getChild(GET_FEATURE, WFS).clone();
        final QName featureName = feature.getName();

        if (feature.getNameOfGeometryAtrtibute() == null) {
            return null;
        }

        // set the feature name
        final Element query = requestElement.getChild(QUERY, WFS);
        query.getAttribute(TYPE_NAME_ATTR).setValue(feature.getPrefixedNameString());

        if ((featureName.getNamespaceURI() != null) && (featureName.getPrefix() != null)) {
            final Namespace ns = Namespace.getNamespace(featureName.getPrefix(), featureName.getNamespaceURI());
            requestElement.addNamespaceDeclaration(ns);
        }

        // set geometry
        final Element propertyElement = query.getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC);
        propertyElement.setText(feature.getNameOfGeometryAtrtibute());

        for (final FeatureServiceAttribute attribute : feature.getFeatureAttributes()) {
            final Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(attribute.getName());
            query.addContent(tmp);
        }
        return requestElement;
    }

    /**
     * Creates a getFeature request template for the given feature type. The request template is conform to a WFS of the
     * version 1.1.0
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  null, if the given property has no geometry attribute
     */
    private Element getFeature110Request(final FeatureType feature) {
        final Element requestElement = (Element)rootNode.getChild(GET_FEATURE_110).getChild(GET_FEATURE, WFS).clone();
        final QName featureName = feature.getName();

        if (feature.getNameOfGeometryAtrtibute() == null) {
            return null;
        }

        // set the feature name
        final Element query = requestElement.getChild(QUERY, WFS);
        query.getAttribute(TYPE_NAME_ATTR).setValue(feature.getPrefixedNameString());

        if ((featureName.getNamespaceURI() != null) && (featureName.getPrefix() != null)) {
            final Namespace ns = Namespace.getNamespace(featureName.getPrefix(), featureName.getNamespaceURI());
            requestElement.addNamespaceDeclaration(ns);
        }

        // set geometry
        final Element propertyElement = query.getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC);
        propertyElement.setText(feature.getNameOfGeometryAtrtibute());

        for (final FeatureServiceAttribute attribute : feature.getFeatureAttributes()) {
            final Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(attribute.getName());
            query.addContent(tmp);
        }
        return requestElement;
    }

    /**
     * Sends a http Post request to the server and returns the response of the server as String.
     *
     * @param   serverURL  URL of the server
     * @param   request    Request-String
     *
     * @return  server response as string or null, if an error occurs
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    private String postRequest(final URL serverURL, final String request) throws IOException, Exception {
        logger.info("post request (" + serverURL + ")"); // NOI18N

        final InputStream resp = WebAccessManager.getInstance()
                    .doRequest(serverURL, request, ACCESS_METHODS.POST_REQUEST);
        final char[] buffer = new char[256];
        final StringBuilder response = new StringBuilder();
        final BufferedReader br = new BufferedReader(new InputStreamReader(resp));
        int count = br.read(buffer, 0, buffer.length);

        while (count != -1) {
            response.append(buffer, 0, count);
            count = br.read(buffer, 0, buffer.length);
        }
        br.close();

        return response.toString();
    }
}
