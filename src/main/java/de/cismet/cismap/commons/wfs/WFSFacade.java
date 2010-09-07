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

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import de.cismet.tools.StaticHtmlTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * This class invokes Web Feature services and creates WFS requests.
 * This class considers the WFS version while creating WFS requests and should be the only class that is used
 * to create and modify WFS request. The currently supported WFS versions are 1.0.0 and 1.1.0
 * @author therter
 */
public class WFSFacade {
    private static final Logger logger = Logger.getLogger(WFSFacade.class);
    /** WFS namespace-contant */
    private static final Namespace WFS = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs");//NOI18N
    /** OGC namespace-contant */
    private static final Namespace OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");//NOI18N
    /** GML namespace-contant */
    private static final Namespace GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");//NOI18N
    /** OWS namespace-contant */
    private static final Namespace OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");//NOI18N
    /** XSD namespace-contant */
    private static final Namespace xsd = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");//NOI18N
    /** name of the CismapQuery-element */
    private static final String GET_FEATURE_100 = "getFeatureQuery100";//NOI18N
    private static final String GET_FEATURE_110 = "getFeatureQuery110";//NOI18N
    private static final String CISMET_DESCRIBE_FEATURE_TYPE = "CismapDescribeFeatureType";//NOI18N
    private static final String DESCRIBE_FEATURE_TYPE = "DescribeFeatureType";//NOI18N
    private static final String GET_FEATURE = "GetFeature";//NOI18N
    private static final String QUERY = "Query";//NOI18N
    private static final String FILTER = "Filter";//NOI18N
    private static final String BBOX = "BBOX";//NOI18N
    private static final String TYPENAME = "TypeName";//NOI18N
    private static final String PROPERTY_NAME = "PropertyName";//NOI18N
    private static final String TYPE_NAME_ATTR = "typeName";//NOI18N
    private static final String VERSION_ATTR = "version";//NOI18N
    private static final String MAX_FEATURES_ATTR = "maxFeatures";//NOI18N
    private static final URL XML_FILE = WFSFacade.class.getResource("wfs.xml"); // TODO Auslagern//NOI18N
    public static final String CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER = "<cismapBoundingBoxAsGmlPlaceholder />";
    public static final String SRS_NAME_PLACEHOLDER = "SRSNAME_PLACEHOLDER";
    private WFSCapabilities cap;
    private ResponseParserFactory parserFactory;
    private Element rootNode;

    
    /**
     * this constructor should only be used by the WFSCapabilities classes. Because every WFSFacade is bounded to
     * a capabilities class and should only be used with features of the given capabilities document.
     * 
     * @param cap
     * @param parserFactory
     */
    public WFSFacade(WFSCapabilities cap, ResponseParserFactory parserFactory) {
        this.cap = cap;
        this.parserFactory = parserFactory;
        try {
            SAXBuilder builder = new SAXBuilder();
            rootNode = builder.build(XML_FILE).getRootElement();
        } catch (Exception ex) {
            logger.error("Error during parsing of the CismapXML-Files", ex);//NOI18N
        }
    }

    /**
     * sends a describeFeatureType request to the corresponding wfs
     * @param feature the feature, the request should be sent for
     * @return the wfs response as FeatureTypeDescription object
     * @throws IOException
     */
    public FeatureTypeDescription describeFeatureType(FeatureType feature) throws IOException, BadHttpStatusCodeException {
        String version = cap.getVersion();
        String request;
        XMLOutputter out = new XMLOutputter();
        QName featureName = feature.getName();
        Element requestElement = (Element)rootNode.getChild(CISMET_DESCRIBE_FEATURE_TYPE)
                .getChild(DESCRIBE_FEATURE_TYPE, WFS).clone();

        // set version
        if (! version.equals("1.0.0") && !version.equals("1.1.0") ) { //NOI18N
            logger.error("unknown service version used: " + version + " service" //NOI18N
                    + cap.getURL() + ". Try to use a 1.1.0 request with version string" + version);//NOI18N
        }
        requestElement.getAttribute(VERSION_ATTR).setValue(version);//NOI18N


        // set namespace
        if (featureName.getPrefix() != null && featureName.getNamespaceURI() != null) {
            Namespace ns = Namespace.getNamespace(featureName.getPrefix(), featureName.getNamespaceURI());
            requestElement.addNamespaceDeclaration( ns );
        }

        // set feature type
        requestElement.getChild(TYPENAME, WFS).setText(feature.getPrefixedNameString());

        request = out.outputString(requestElement);
        String response = postRequest(cap.getURL(), request);

        try {
            return parserFactory.getFeatureTypeDescription(response, feature);
        } catch (Exception e) {
            logger.error("Error while parsing the response of a describeFeature request.", e);//NOI18N
            return null;
        }
    }


    /**
     * Returns a template for a getFeature request. The template contains placeholders
     * for the bounding box, the srs name and the property names.
     * To replace the placeholders, use the following static methods:<br />
     * setGetFeatureBoundingBox
     * setGeometry
     * changePropertyNames
     * setMaxFeatureCount
     * @param feature the request template will be created for this feature
     * @return a getFeature request template. The WFS version will be considered.
     */
    public Element getGetFeatureQuery(FeatureType feature) {
        String version = cap.getVersion();
        Element request;

        if (version.equals("1.0.0")) { //NOI18N
            request = getFeature100Request(feature);
        } else if (version.equals("1.1.0")) { //NOI18N
            request = getFeature110Request(feature);
        } else {
            logger.error("unknown service version used: " + version + " service" //NOI18N
                    + cap.getURL() + ". Try to use a version 1.1.0 request");//NOI18N
            request = getFeature110Request(feature);
        }

        return request;
    }


    /**
     * Set the bounding box and the srs of the given getFeature request and returns the resulting
     * request as string.
     * @param query the getFeature request template.
     * @param bbox the bounding box that should be used in the getFeature request
     * @param version the version of the wfs
     * @return the new getFeature request
     */
    public static String setGetFeatureBoundingBox(String query, BoundingBox bbox, String version) {
        String request;
        String envelope;
        
        if (version != null && version.equals("1.0.0")) { //NOI18N
           envelope = "<gml:Box><gml:coord><gml:X>"+ bbox.getX1() + "</gml:X><gml:Y>" + bbox.getY1() //NOI18N
                   + "</gml:Y></gml:coord>" + "<gml:coord><gml:X>" + bbox.getX2() //NOI18N
                   + "</gml:X><gml:Y>" + bbox.getY2() + "</gml:Y></gml:coord>" + "</gml:Box>";//NOI18N
        } else if (version != null && version.equals("1.1.0")) { //NOI18N
            envelope = "<gml:Envelope><gml:lowerCorner>" + bbox.getX1() //NOI18N
                           + " " + bbox.getY1() + "</gml:lowerCorner>" + "<gml:upperCorner>" //NOI18N
                           + bbox.getX2() + " " + bbox.getY2() + "</gml:upperCorner>" + "</gml:Envelope>";//NOI18N
        } else {
            logger.error("unknown service version used: " + version //NOI18N
                    + ". Try to use a version 1.1.0 request");//NOI18N
            envelope = "<gml:Envelope><gml:lowerCorner>" + bbox.getX1() //NOI18N
                           + " " + bbox.getY1() + "</gml:lowerCorner>" + "<gml:upperCorner>" //NOI18N
                           + bbox.getX2() + " " + bbox.getY2() + "</gml:upperCorner>" + "</gml:Envelope>";//NOI18N
        }
        
        request = query.toString().replaceAll(CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER, envelope);
        request = request.replaceAll(SRS_NAME_PLACEHOLDER, CismapBroker.getInstance().getSrs().getCode());

        return request;
    }
    
    /**
    * Replaces the current max feature count with the given max feature count in the given getFeature request.
    * @param query getFeature request as JDOM-element
    * @param properties collection of strings
    * @param version the version of the wfs request that is contained in the query paramaeter
    */
    public static void setMaxFeatureCount(Element wfsQuery, int maxFeatureCount, String version) {
        if (version == null) {
            logger.error("version string is null. Try to use version 1.1.0", new Exception());
            version = "1.1.0";
        }
        if ( ! (version.equals( "1.0.0" ) || version.equals("1.1.0")) ) {
            logger.error("unknown wfs version: " + version //NOI18N
                    + ". Try to handle this version like version 1.1.0");//NOI18N
        }
        logger.debug("setting may maxFeatureCount of WFS Query to " + maxFeatureCount);//NOI18N

        if(wfsQuery != null) {
            wfsQuery.setAttribute(MAX_FEATURES_ATTR, String.valueOf(maxFeatureCount));
        } else {
            logger.warn("could not set maxFeatureCount, query not yet initialised");//NOI18N
        }
    }


    /**
    * Replaces the current property names with the given property names in the getFeature query.
    * @param query getFeature request as JDOM-element
    * @param properties the property names
    * @param version the version of the wfs request that is contained in the query paramaeter
    */
    public static void changePropertyNames(Element query, Collection<String> properties, String version) {
        if (version == null) {
            logger.error("version string is null. Try to use version 1.1.0", new Exception());
            version = "1.1.0";
        }
        if ( ! (version.equals( "1.0.0" ) || version.equals("1.1.0")) ) {//NOI18N
            logger.error("unknown wfs version: " + version //NOI18N
                    + ". Try to handle this version like version 1.1.0");//NOI18N
        }
        query.getChild(QUERY, WFS).removeChildren(PROPERTY_NAME, WFS);
        
        for (String s : properties) {
            Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(s);
            query.getChild(QUERY, WFS).addContent(tmp);
        }
    }


    /**
    * Replaces the geometry-property in the given getFeature query template.
    * @param query getFeature request as JDOM-element
    * @param geoName the name of the geometry
    * @param version the version of the wfs request that is contained in the query paramaeter
    */
    public static void setGeometry(Element query, String geoName, String version) {
        if (version == null) {
            logger.error("version string is null. Try to use version 1.1.0", new Exception());
            version = "1.1.0";
        }
        if ( ! (version.equals( "1.0.0" ) || version.equals("1.1.0")) ) {//NOI18N
            logger.error("unknown wfs version: " + version //NOI18N
                    + ". Try to handle this version like version 1.1.0");//NOI18N
        }
        query.getChild(QUERY, WFS).getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC).setText(geoName);
    }


    /**
     * Creates a getFeature request template for the given feature type.
     * The request template is conform to a WFS of the version 1.0.0
     *
     * @param feature
     * @return null, if the given feature has no geometry attribute
     */
    private Element getFeature100Request(FeatureType feature) {
        Element requestElement = (Element)rootNode.getChild(GET_FEATURE_100).getChild(GET_FEATURE, WFS).clone();
        QName featureName = feature.getName();

        if (feature.getNameOfGeometryAtrtibute() == null) {
            return null;
        }

        // set the feature name
        Element query = requestElement.getChild(QUERY, WFS);
        query.getAttribute(TYPE_NAME_ATTR).setValue( feature.getPrefixedNameString() );

        if (featureName.getNamespaceURI() != null && featureName.getPrefix() != null) {
            Namespace ns = Namespace.getNamespace(featureName.getPrefix(), featureName.getNamespaceURI());
            requestElement.addNamespaceDeclaration(ns);
        }

        // set geometry
        Element propertyElement = query.getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC);
        propertyElement.setText(feature.getNameOfGeometryAtrtibute());

        for (FeatureServiceAttribute attribute : feature.getFeatureAttributes()) {
            Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(attribute.getName());
            query.addContent(tmp);
        }
        return requestElement;
    }


    /**
     * Creates a getFeature request template for the given feature type.
     * The request template is conform to a WFS of the version 1.1.0
     *
     * @param feature
     * @return null, if the given property has no geometry attribute
     */
    private Element getFeature110Request(FeatureType feature) {
        Element requestElement = (Element)rootNode.getChild(GET_FEATURE_110).getChild(GET_FEATURE, WFS).clone();
        QName featureName = feature.getName();

        if (feature.getNameOfGeometryAtrtibute() == null) {
            return null;
        }
        
        // set the feature name
        Element query = requestElement.getChild(QUERY, WFS);
        query.getAttribute(TYPE_NAME_ATTR).setValue( feature.getPrefixedNameString() );

        if (featureName.getNamespaceURI() != null && featureName.getPrefix() != null) {
            Namespace ns = Namespace.getNamespace(featureName.getPrefix(), featureName.getNamespaceURI());
            requestElement.addNamespaceDeclaration(ns);
        }
        
        // set geometry
        Element propertyElement = query.getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC);
        propertyElement.setText(feature.getNameOfGeometryAtrtibute());

        for (FeatureServiceAttribute attribute : feature.getFeatureAttributes()) {
            Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(attribute.getName());
            query.addContent(tmp);
        }
        return requestElement;
    }



    /**
     * Sends a http Post request to the server and returns the response of the server
     * as String
     * @param serverURL URL of the server
     * @param request Request-String
     * @return server response as string or null, if an error occurs
     */
    private String postRequest(URL serverURL, String request) throws IOException, BadHttpStatusCodeException {
        logger.info("post request (" + serverURL + ")");//NOI18N
        // create HTTP-client
        HttpClient client = new HttpClient();

        // is currently a proxy set
        String proxySet = System.getProperty("proxySet");//NOI18N

        // if proxy exists ...
        if (proxySet != null && proxySet.equals("true")) {//NOI18N
            logger.debug("Proxy existent");//NOI18N
            logger.debug("ProxyHost:" + System.getProperty("http.proxyHost"));//NOI18N
            logger.debug("ProxyPort:" + System.getProperty("http.proxyPort"));//NOI18N
            try {
                // add existing proxy to HTTP-client
                client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"),//NOI18N
                        Integer.parseInt(System.getProperty("http.proxyPort")));//NOI18N
            } catch (Exception ex) {
                logger.error("Set proxy in HTTP-Client failed", ex);//NOI18N
            }

        } else { // do nothing
            logger.debug("no Proxy");//NOI18N
        }

        // create new POST-method with the server-URL
        PostMethod httppost = new PostMethod(serverURL.toString());
        logger.debug("ServerURL = " + httppost.getURI().toString());//NOI18N

        // create HTML from request & change charset to ISO-8859-1
        logger.debug("WFS Query = " + StaticHtmlTools.stringToHTMLString(request));//NOI18N
        String modifiedString = new String(request.getBytes("UTF-8"), "ISO-8859-1");//NOI18N
        httppost.setRequestEntity(new StringRequestEntity(modifiedString));

        try {
            // send POST-method to server
            client.executeMethod(httppost);

            // if response == OK
            if (httppost.getStatusCode() == HttpStatus.SC_OK) {
                logger.debug("Server handled the request and sends the response");//NOI18N
                logger.debug("read InputStream");//NOI18N
                char[] buffer = new char[256];
                StringBuilder response = new StringBuilder();
                BufferedReader br = new BufferedReader( new InputStreamReader(httppost.getResponseBodyAsStream()) );
                int count = br.read(buffer, 0, buffer.length);

                while ( count != -1 ) {
                    response.append(buffer, 0, count);
                    count = br.read(buffer, 0, buffer.length);
                }
                br.close();

                return response.toString();
            } else {
                logger.error("Unexpected failure: " + httppost.getStatusLine().toString());//NOI18N
                throw new BadHttpStatusCodeException("Unexpected failure: " + httppost.getStatusLine().toString());
            }
        } finally {
            httppost.releaseConnection();
        }
    }
}
