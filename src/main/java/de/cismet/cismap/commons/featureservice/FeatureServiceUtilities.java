/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.tools.StaticHtmlTools;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree2.datatypes.QualifiedName;
import org.deegree2.framework.xml.XMLFragment;
import org.deegree2.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree2.framework.xml.schema.ElementDeclaration;
import org.deegree2.framework.xml.schema.XMLSchema;
import org.deegree2.framework.xml.schema.XSDocument;
import org.deegree2.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree2.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree2.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree2.ogcwebservices.wfs.operation.AbstractWFSRequestDocument;
import org.deegree2.ogcwebservices.wfs.operation.DescribeFeatureTypeDocument;
import org.deegree2.ogcwebservices.wfs.operation.FeatureTypeDescription;
import org.deegree2.ogcwebservices.wfs.operation.WFSGetCapabilitiesDocument;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * The FeatureServiceUtilities class provides various methods to make the FeatureService-
 * funcionality possible.
 * @author nh
 */
public class FeatureServiceUtilities {
    /** Log4J initialisation */
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.featureservice.FeatureServiceUtilities");
    /** WFS namespace-contant */
    public static final Namespace WFS = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs");
    /** OGC namespace-contant */
    public static final Namespace OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");
    /** GML namespace-contant */
    public static final Namespace GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
    /** OWS namespace-contant */
    public static final Namespace OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");
    /** XSD namespace-contant */
    public static final Namespace xsd = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
    /** name of the CismapQuery-element */
    public static final String CISMAP_QUERY = "CismapQuery";
    /** name of the Query-element */
    public static final String QUERY = "Query";
    /** name of the CismapDescribeFeatureType-element */
    public static final String CISMAP_DESCRIBEFEATURETYPE = "CismapDescribeFeatureType";
    /** name of the DescribeFeatureType-element */
    public static final String DESCRIBEFEATURETYPE = "DescribeFeatureType";
    /** name of the CismapGetCapabilities-element */
    public static final String CISMAP_GETCAPABILITIES = "CismapGetCapabilities";
    /** name of the GetCapabilities-element */
    public static final String GETCAPABILITIES = "GetCapabilities";
    /** name of the ServiceIdentification-element */
    public static final String SERVICE_IDENT = "ServiceIdentification";
    /** name of the Filter-element */
    public static final String FILTER = "Filter";
    /** name of the BoundingBox-element */
    public static final String BBOX = "BBOX";
    /** name of the GetFeature-element */
    public static final String GET_FEATURE = "GetFeature";
    /** name of the GetFeature-element */
    public static final String TYPE_NAME = "typeName";
    /** name of the GetFeature-element */
    public static final String DEFAULT_TYPENAME = "TypeName";
    /** name of the GetFeature-element */
    public static final String PROPERTY_NAME = "PropertyName";
    /** typestring of the GML-geometry-type */
    public static final String GEO_PROPERTY_TYPE = "GeometryPropertyType";
    /** typestring of the GML-geometry-type */
    public static final String GEO_PROPERTY_TYPE_WITH_NS = "gml:GeometryPropertyType";
    /** typestring of the string-type */
    public static final String STRING_PROPERTY_TYPE = "string";
    /** typestring of the integer-type */
    public static final String INTEGER_PROPERTY_TYPE = "integer";
    /** name of the name-attribute */
    public static final String XML_NAME_STRING = "name";
    /** name of the type-attribute */
    public static final String XML_TYPE_STRING = "type";
    /** name of the isGeometry-attribute */
    public static final String IS_GEOMETRY = "isGeometry";
    private final URL XML_FILE = getClass().getResource("wfs.xml"); // TODO Auslagern
    private XMLOutputter out = new XMLOutputter();
    private Element rootNode;
    private Element query;

    /**
     * Standard-constructor.
     */
    public FeatureServiceUtilities() {
        log.debug("createStandardQuery()");
        try {
            SAXBuilder builder = new SAXBuilder();
            rootNode = builder.build(XML_FILE).getRootElement();
        } catch (Exception ex) {
            log.error("Fehler beim Parsen des CismapXML-Files", ex);
        }
    }

    /**
     * Constructor that needs a name in order to create a DescribeFeatureRequest.
     * @param typeName name of the requested featuretype
     */
    public FeatureServiceUtilities(String typeName) {
        log.debug("createStandardQuery(" + typeName + ")");
        try {
            SAXBuilder builder = new SAXBuilder();
            rootNode = builder.build(XML_FILE).getRootElement();
            query = rootNode.getChild(CISMAP_QUERY).getChild(GET_FEATURE, WFS);
            query.getChild(QUERY, WFS).getAttribute(TYPE_NAME).setValue(typeName);
        } catch (Exception ex) {
            log.error("Fehler beim Erstellen eines Standardquery", ex);
        }
    }

    /**
     * Sets the propertyname that should be retrieved in the query.
     * @param properties collection of elements
     */
    public void setPropertyNames(Collection<Element> properties) {
        getQuery().getChild(QUERY, WFS).removeChildren(PROPERTY_NAME, WFS);
        for (Element e : properties) {
            Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(e.getAttributeValue("name"));
            getQuery().getChild(QUERY, WFS).addContent(tmp);
        }
    }

    /**
     * Replaces the current with the delivered propertynames in the given query.
     * @param query query as JDOM-element
     * @param properties collection of strings
     */
    public static Element changePropertyNames(Element query, Collection<String> properties) {
        query.getChild(QUERY, WFS).removeChildren(PROPERTY_NAME, WFS);
        for (String s : properties) {
            Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(s);
            query.getChild(QUERY, WFS).addContent(tmp);
        }
        return query;
    }

    /**
     * Replaces the geometry-property in the query.
     * @param e JDOM-element whose type-attribute is the geometryname
     */
    public void setGeometry(String s) {
        getQuery().getChild(QUERY, WFS).getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC).setText(s);
    }

    /**
     * Replaces the geometry-property in the given query.
     * @param query query as JDOM-element
     * @param geoName geometryname
     */
    public static void setGeometry(Element query, String geoName) {
        query.getChild(QUERY, WFS).getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC).setText(geoName);
    }

    /**
     * Creates a string from a JDOM-element.
     * @param e JDOM-element
     * @return XML-string
     */
    public static String elementToString(Element e) {
        if (e == null) {
            return "";
        } else {
            XMLOutputter out = new XMLOutputter("    ", true);
            out.setTextTrim(true);
            return out.outputString(e);
        }
    }

    /**
     * Returns the getFeature-query.
     * @return JDOM-element
     */
    public Element getQuery() {
        return query;
    }

    /**
     * Creates a string that represents a DescribeFeatureType-request for the given featuretypename.
     * @param name name of the requested featuretype
     * @return DescribeFeatureType-request as String
     */
    public DescribeFeatureTypeDocument createDescribeFeatureTypeRequest(String name) throws Exception {
        log.debug("Erstelle DescribeFeatureTypeRequest f\u00FCr " + name);
        Element describeFeatType = rootNode.getChild(CISMAP_DESCRIBEFEATURETYPE).getChild(DESCRIBEFEATURETYPE, WFS);
        describeFeatType.getChild(DEFAULT_TYPENAME, WFS).setText(name);
        DescribeFeatureTypeDocument dftDoc = new DescribeFeatureTypeDocument();
        dftDoc.load(new StringReader(out.outputString(describeFeatType)), "http://test0r");
        return dftDoc;
    }

    /**
     * Creates a string that represents a GetCapabilities-request.
     * @return GetCapabilities-request as String
     */
    private WFSGetCapabilitiesDocument createGetCapabilitiesRequest() throws Exception {
        log.debug("Erstelle GetCapabilitiesRequest");
        WFSGetCapabilitiesDocument gcDoc = new WFSGetCapabilitiesDocument();
        gcDoc.load(new StringReader(out.outputString(rootNode.getChild(CISMAP_GETCAPABILITIES).getChild(GETCAPABILITIES, WFS))), "http://test0r");
        return gcDoc;
    }

    /**
     * Sends a getCapabilities-requests to the given server-URL, then tries to parse
     * the returned JDOM-document into a WFSCapabilitiesDocument.
     * @param server URL of the server
     * @return WFSCapabilities-object
     */
    public WFSCapabilitiesDocument getWFSCapabilitesDocument(URL server) throws Exception {
        WFSCapabilitiesDocument wfsDoc = new WFSCapabilitiesDocument();
        wfsDoc.load(new StringReader(postRequest(server, createGetCapabilitiesRequest()).getAsString()), "http://test0r");
        return wfsDoc;
    }

    /**
     * Returns name of the FeatureService if existing in the capabilities.
     * @return name of the FeatureService or the ServiceTypename, if no name found
     */
    public static String getServiceName(WFSCapabilitiesDocument capDoc) {
        String name;
        try {
            name = capDoc.getRootElement().getElementsByTagName("ows:Title").item(0).getFirstChild().getNodeValue();
        } catch (Exception titleNotFound) {
            log.warn("Titel des FeatureService ist nicht vorhanden", titleNotFound);
            try {
                name = capDoc.getRootElement().getElementsByTagName("ows:Abstract").item(0).getFirstChild().getNodeValue();
            } catch (Exception abstractNotFound) {
                log.warn("Abstract des FeatureService ist nicht vorhanden", abstractNotFound);
                try {
                    name = capDoc.getRootElement().getElementsByTagName("ows:ServiceType").item(0).getFirstChild().getNodeValue();
                } catch (Exception servicetypeNotFound) {
                    log.warn("ServiceType des FeatureService ist nicht vorhanden", servicetypeNotFound);
                    name = null;
                }
            }
        }
        return name;
//        ServiceIdentification si = capabilities.getServiceIdentification();
//        if (si.getTitle() != null && !si.getTitle().equals("")) {
//            return si.getTitle();
//        } else if (si.getAbstract() != null && !si.getAbstract().equals("")) {
//            return si.getAbstract();
//        } else {
//            return si.getServiceType().getCode();
//        }
    }

    /**
     * Creates a string from a JDOM-document.
     * @param doc JDOM-cocument
     * @return das String which represents the document
     */
    public static String parseDocumentToString(Document doc) {
        log.debug("parseDocumentToString()");
        XMLOutputter out = new XMLOutputter();
        return out.outputString(doc);
    }

    /**
     * Creates a list of JDOM-elements containing all attributes of each FeatureType from
     * the FeatureTypeList.
     * @param postURL URL of the servers
     * @param featTypes FeatureTypeList from the WFSCapabilties
     * @return List all FeatureTypes as element with their attributes as children
     */
    public HashMap<ElementDeclaration, Vector<FeatureServiceAttribute>> getElementDeclarations(URL postURL, FeatureTypeList featTypes) throws Exception {
        log.debug("getElementDeclarations(" + postURL + ")");
        FeatureTypeDescription featTypeDesc = null;

        // create hashmap that will be returned
        HashMap<ElementDeclaration, Vector<FeatureServiceAttribute>> result = new HashMap<ElementDeclaration, Vector<FeatureServiceAttribute>>();

        for (WFSFeatureType ft : featTypes.getFeatureTypes()) {
            try {
                log.debug("get complextypes for " + ft.getName().getAsString());
                featTypeDesc = new FeatureTypeDescription(postRequest(postURL, createDescribeFeatureTypeRequest(ft.getName().getAsString())));
                XMLFragment xmlFrag = featTypeDesc.getFeatureTypeSchema();
                if (xmlFrag.hasSchema()) {
                    XSDocument xsDoc = new XSDocument();
                    xsDoc.setRootElement(xmlFrag.getRootElement());
                    XMLSchema xmlSchema = xsDoc.parseXMLSchema();

                    // check if the FeatureType-name is in the current FeatureTypeDescription
                    ElementDeclaration requestedElement = xmlSchema.getElementDeclaration(ft.getName());
                    if (requestedElement.getName().getNamespace() != null) { // if FeatureType-name found
                        QualifiedName typeName = xmlSchema.getElementDeclaration(ft.getName()).getType().getName();
                        ComplexTypeDeclaration compTypeDec = xmlSchema.getComplexTypeDeclaration(typeName);
                        if (getFirstGeometryName(compTypeDec.getElements()) != null) {
                            Vector<FeatureServiceAttribute> fsaVector = new Vector<FeatureServiceAttribute>(compTypeDec.getElements().length);
                            for (ElementDeclaration e : compTypeDec.getElements()) {
                                fsaVector.add(new FeatureServiceAttribute(ft.getName().getPrefix()+ ":" + e.getName().getLocalName(), e.getType().getName().getAsString(), true));
                            }
                            ElementDeclaration newRequestedElement = new ElementDeclaration(
                                    new QualifiedName(ft.getName().getPrefix(), requestedElement.getName().getLocalName(), xmlSchema.getTargetNamespace()),
                                    requestedElement.isAbstract(), requestedElement.getType(),
                                    requestedElement.getMinOccurs(), requestedElement.getMaxOccurs(),
                                    requestedElement.getSubstitutionGroup() != null ? requestedElement.getSubstitutionGroup().getName() : null);
                            result.put(newRequestedElement, fsaVector);
                        }
                        log.debug("complextypes found: " + compTypeDec.getElements());
                    }
                }
            } catch (Throwable ex) {
                log.fatal("Error in getElementDeclarations", ex);
            }
        }
        return result;
    }

    /**
     * Returns the name of the first found geometry in the FeatureServiceAttribute-vector.
     * @param elements FeatureServiceAttribute-vector that will be searched
     * @return FeatureServiceAttribute-name or null
     */
    public static String getFirstGeometryName(Vector<FeatureServiceAttribute> elements) {
        for (FeatureServiceAttribute e : elements) {
            if (e.isGeometry()) {
                return e.getName();
            }
        }
        return null;
    }

    /**
     * Returns the name of the first found geometry in the ElementDeclaration-array.
     * @param elements ElementDeclaration-array that will be searched
     * @return ElementDeclaration-name or null
     */
    public static String getFirstGeometryName(ElementDeclaration[] elements) {
        for (ElementDeclaration e : elements) {
            if (e.getType().getName().getLocalName().equals(GEO_PROPERTY_TYPE)) {
                return e.getName().getAsString();
            }
        }
        return null;
    }

    /**
     * Stellt einen POST-Request an die Server-URL und gibt einen InputStream zur\u00FCck
     * aus dem die Antwort des Servers ausgelesen werden kann.
     * @param serverURL URL des anzusprechenden Servers
     * @param request Request-String
     * @return Serverantwort als InputStream
     */
    public static XMLFragment postRequest(URL serverURL, AbstractWFSRequestDocument request) throws Exception {
        log.info("FeatureServiceUtilities.postRequest()");
        // create HTTP-client
        HttpClient client = new HttpClient();

        // is currently a proxy set
        String proxySet = System.getProperty("proxySet");

        // if proxy exists ...
        if (proxySet != null && proxySet.equals("true")) {
            log.debug("Proxy vorhanden");
            log.debug("ProxyHost:" + System.getProperty("http.proxyHost"));
            log.debug("ProxyPort:" + System.getProperty("http.proxyPort"));
            try {
                // add existing proxy to HTTP-client
                client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"),
                        Integer.parseInt(System.getProperty("http.proxyPort")));
            } catch (Exception ex) {
                log.error("Proxy im HTTP-Client setzen fehlgeschlagen", ex);
            }

        } else { // do nothing
            log.debug("kein Proxy");
        }

        // create new POST-method with the server-URL
        PostMethod httppost = new PostMethod(serverURL.toString());
        log.debug("ServerURL = " + httppost.getURI().toString());

        // save requeststring so the original won't be changed 
        String poststring = request.toString();

        // create HTML from request & change charset to ISO-8859-1
        log.debug("WFS Query = " + StaticHtmlTools.stringToHTMLString(poststring));
        String modifiedString = new String(poststring.getBytes("UTF-8"), "ISO-8859-1");
        httppost.setRequestEntity(new StringRequestEntity(modifiedString));

        try {
            // send POST-method to server
            client.executeMethod(httppost);

            // if response == OK
            if (httppost.getStatusCode() == HttpStatus.SC_OK) {
                log.debug("Server hat Request bearbeitet und antwortet");
                log.debug("InputStream parsen");
                XMLFragment fragment = new XMLFragment();
                fragment.load(httppost.getResponseBodyAsStream(), "http://fake");
                log.debug("Erfolgreich geparst: " + fragment.getAsString());
                return fragment;
            } else {
                log.error("Unexpected failure: " + httppost.getStatusLine().toString());
            }
        } finally {
            httppost.releaseConnection();
        }
        return null;
    }

    /**
     * Creates FeatureServiceAttributes by parsing the children of the delivered JDOM-element.
     * @param describeFeatureXML JDOM-element
     * @return vector with FeatureServiceAttributes
     */
    public static Vector<FeatureServiceAttribute> getFeatureServiceAttributes(Element describeFeatureXML) {
        Vector<FeatureServiceAttribute> fsa = new Vector<FeatureServiceAttribute>();
        for (Element currentElement : (List<Element>) describeFeatureXML.getChildren()) {
            try {
                fsa.add(new FeatureServiceAttribute(currentElement));
            } catch (Exception ex) {
                log.warn("Ein Element konnte nicht als Attribut geparsed werden: " + currentElement);
            }
        }
        return fsa;
    }
}
