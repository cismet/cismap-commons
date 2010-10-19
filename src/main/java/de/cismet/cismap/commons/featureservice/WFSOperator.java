/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.security.AccessHandler.ACCESS_METHODS;
import de.cismet.security.WebAccessManager;
import de.cismet.tools.StaticHtmlTools;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @deprecated This class should not be used, because the methods do not consider
 * the request version, when they change queries.
 * @author nh
 */
public class WFSOperator {

    /**
     * Log4J Initialisierung
     */
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.raster.wfs.WFSQueryFactory");//NOI18N
    public static final String CISMAP_QUERY = "CismapQuery";//NOI18N
    public static final String QUERY = "Query";//NOI18N
    public static final String CISMAP_DESCRIBEFEATURETYPE = "CismapDescribeFeatureType";//NOI18N
    public static final String DESCRIBEFEATURETYPE = "DescribeFeatureType";//NOI18N
    public static final String CISMAP_GETCAPABILITIES = "CismapGetCapabilities";//NOI18N
    public static final String GETCAPABILITIES = "GetCapabilities";//NOI18N
    public static final String SERVICE_IDENT = "ServiceIdentification";//NOI18N
    public static final String FILTER = "Filter";//NOI18N
    public static final String BBOX = "BBOX";//NOI18N
    public static final String GET_FEATURE = "GetFeature";//NOI18N
    public static final String TYPE_NAME = "typeName";//NOI18N
    public static final String DFT_TYPE_NAME = "TypeName";//NOI18N
    public static final String PROPERTY_NAME = "PropertyName";//NOI18N
    public static final String GEO_PROPERTY_TYPE = "gml:GeometryPropertyType";//NOI18N
    private final URL XML_FILE = getClass().getResource("wfs.xml");//NOI18N
    private Document xmlDoc;
    private Document capabilities;
    private Element rootNode;
    private Element query;
    /**
     * WFS-Namespace-Konstante
     */
    public static final Namespace WFS = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs");//NOI18N
    /**
     * OGC-Namespace-Konstante
     */
    public static final Namespace OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");//NOI18N
    /**
     * GML-Namespace-Konstante
     */
    public static final Namespace GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");//NOI18N
    /**
     * OWS-Namespace-Konstante
     */
    public static final Namespace OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");//NOI18N
    /**
     * XSD-Namespace-Konstante
     */
    public static final Namespace xsd = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");//NOI18N

    /**
     * Standardkonstruktor. Erstellt die XML-Datei mit WFS-Abfragen.
     */
    public WFSOperator() {
        log.debug("createStandardQuery()");//NOI18N
        try {
            SAXBuilder builder = new SAXBuilder();
            xmlDoc = builder.build(XML_FILE);
            rootNode = xmlDoc.getRootElement();
        } catch (Exception ex) {
            log.error("Error during parsing of the CismapXML-Files", ex);//NOI18N
        }
    }

    /**
     * Konstruktor f\u00FCr WFSQueryFactory-Objekte
     * @param typeName der Typname des abzufragenden Features
     */
    public WFSOperator(String typeName) {
        log.debug("createStandardQuery(" + typeName + ")");//NOI18N
        try {
            SAXBuilder builder = new SAXBuilder();
            xmlDoc = builder.build(XML_FILE);
            rootNode = xmlDoc.getRootElement();
            query = rootNode.getChild(CISMAP_QUERY).getChild(GET_FEATURE, WFS);
            query.getChild(QUERY, WFS).getAttribute(TYPE_NAME).setValue(typeName);
        } catch (Exception ex) {
            log.error("Error during the creation of a default query.", ex);//NOI18N
        }
    }

    /**
     * Setzt abzufragende Properties.
     * @param properties Element-Collection mit PropertyNames
     */
    public void setPropertyNames(Collection<Element> properties) {
        getQuery().getChild(QUERY, WFS).removeChildren(PROPERTY_NAME, WFS);
        for (Element e : properties) {
            Element tmp = new Element(PROPERTY_NAME, WFS);
            tmp.setText(e.getAttributeValue("name"));//NOI18N
            getQuery().getChild(QUERY, WFS).addContent(tmp);
        }
    }

    /**
     * Durchsucht das übergebene WFSQuery nach vorhandenen Attributen (PropertyNames)
     * und liefert diese als String-Vector zurück.
     * @param query zu durchsuchendes Query
     * @return Vector<String> mit gefundenen Attributen
     */
    public static Vector<String> getPropertyNamesFromQuery(Element query) {
        Vector<String> result = new Vector<String>();
        try {
            for (Object o : query.getChild(QUERY, WFS).getChildren(PROPERTY_NAME, WFS)) {
                if (o instanceof Element) {
                    result.add(((Element) o).getText());
                }
            }
            log.debug(result);
        } catch (Exception ex) {
            log.error("Error in getPropertyNamesFromQuery()");//NOI18N
            return new Vector();
        }
        return result;
    }

    /**
     * Ersetzt die bisher in der Query vorhandenen Properties durch die der
     * übergebenen String-Collection.
     * @param query WFSQuery als JDOM-Element
     * @param properties String-Collection mit PropertyNames
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
     * Setzt den Geometrie-Namen.
     * @param geo Element mit dem Geometrie-Namen
     */
    public void setGeometry(Element e) {
        for (Element tmp : (List<Element>) e.getChildren()) {
            if (tmp.getAttributeValue("type").equals(GEO_PROPERTY_TYPE)) {//NOI18N
                getQuery().getChild(QUERY, WFS).getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC).setText(tmp.getAttributeValue("name"));//NOI18N
                break;
            }
        }
    }

    /**
     * Setzt den Geometrie-Namen.
     * @param query WFSQuery als JDOM-Element
     * @param s String mit dem Geometrie-Namen
     */
    public static void setGeometry(Element query, String s) {
        query.getChild(QUERY, WFS).getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC).setText(s);
    }

    /**
     * Liefert den im Query-Element gesetzten Geometrienamen.
     * @param query WFSQuery als JDOM-Element
     * @return String mit dem Geometrie-Namen
     */
    public static String getGeometry(Element query) {
        return query.getChild(QUERY, WFS).getChild(FILTER, OGC).getChild(BBOX, OGC).getChild(PROPERTY_NAME, OGC).getTextTrim();
    }

    /**
     * Liefert einen String aus einem JDOM-Element mittels XMLOutputter.
     * @param e JDOM-Element
     * @return XML, das das JDOM-Element repr\u00E4sentiert
     */
    public static String elementToString(Element e) {
        if (e == null) {
            return "";//NOI18N
        } else {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            return out.outputString(e);
        }
    }

    /**
     * Liefert das fertige GetFeature-Query.
     * @return Query als JDOM-Element
     */
    public Element getQuery() {
        return query;
    }

    /**
     * Erzeugt einen String der einen DescribeFeatureType-Request in XML repr\u00E4sentiert.
     * @param name der Name des Features das beschrieben werden soll
     * @return DescribeFeatureType-Request als String
     */
    public String createDescribeFeatureTypeRequest(String name) {
        log.debug("Create DescribeFeatureTypeRequest for " + name);//NOI18N
        Element describeFeatType = rootNode.getChild(CISMAP_DESCRIBEFEATURETYPE).getChild(DESCRIBEFEATURETYPE, WFS);
        describeFeatType.getChild(DFT_TYPE_NAME, WFS).setText(name);
        return elementToString(describeFeatType);
    }

    /**
     * Erzeugt einen String der einen GetCapabilities-Request in XML repr\u00E4sentiert.
     * @return GetCapabilities-Request als String
     */
    private String createGetCapabilitiesRequest() {
        log.debug("Create GetCapabilitiesRequest");//NOI18N
        return elementToString(rootNode.getChild(CISMAP_GETCAPABILITIES).getChild(GETCAPABILITIES, WFS));
    }

    /**
     * Erstellt ein WFSCapabilities-Objekt per HTTP-POST-Request.
     * @param server URL des WFS-Servers
     * @return WFSCapabilities-Objekt
     */
    public WFSCapabilities parseWFSCapabilites(URL server) throws Exception {
        XMLOutputter out = new XMLOutputter();
        capabilities = doRequest(server, createGetCapabilitiesRequest());
        String buf = out.outputString(capabilities);
        WFSCapabilitiesDocument wfsDoc = new WFSCapabilitiesDocument();
        wfsDoc.load(new StringReader(buf), "http://test0r");//NOI18N
        return (WFSCapabilities) wfsDoc.parseCapabilities();
    }
    
    public WFSCapabilities parseWFSCapabilites(BufferedReader reader) throws Exception {        
        WFSCapabilitiesDocument wfsDoc = new WFSCapabilitiesDocument();
        wfsDoc.load(reader, "http://test0r");//NOI18N
        return (WFSCapabilities) wfsDoc.parseCapabilities();
    }

    /**
     * Liefert, falls in den Capabilities vorhanden, einen Namen f\u00FCr den WFS
     * oder einen Standardnamen.
     * @return Name des WFS oder "WFS", falls keiner gefunden
     */
    public String getServiceName() {
        Element id = capabilities.getRootElement().getChild(SERVICE_IDENT, OWS);
        if (id.getChild("Title", OWS) != null && !id.getChild("Title", OWS).getText().equals("")) {//NOI18N
            return id.getChild("Title", OWS).getText();//NOI18N
        } else if (id.getChild("Abstract", OWS) != null && !id.getChild("Abstract", OWS).getText().equals("")) {//NOI18N
            return id.getChild("Abstract", OWS).getText();//NOI18N
        } else {
            return id.getChild("ServiceType", OWS).getText();//NOI18N
        }
    }

    /**
     * Erzeugt aus einem JDOM-Document einen String.
     * @param doc JDOM-Document
     * @return das JDOM-Documents als String
     */
    public static String parseDocumentToString(Document doc) {
        log.debug("parseDocumentToString()");//NOI18N
        XMLOutputter out = new XMLOutputter();
        return out.outputString(doc);
    }

    /**
     * Erstellt aus der ServerURL und der FeatureTypeList eine Liste mit allen
     * FeatureTypen und deren Parametern aus JDOM-Elementen.
     * @param postURL URL des WFS-Servers
     * @param featTypes FeatureTypeList der WFSCapabilties
     * @return Liste mit allen zum Namen gefundenen Attributen
     */
    public List<Element> getElements(URL postURL, FeatureTypeList featTypes) throws Exception {
        log.debug("getElements()");//NOI18N
        Document doc = null;

        // Ergebnisliste erstellen
        List<Element> list = new LinkedList<Element>();

        // Hole Elemente + Parameter f\u00FCr alle FeatureTypes
        for (int i = 0; i < featTypes.getFeatureTypes().length; i++) {
            WFSFeatureType ft = featTypes.getFeatureTypes()[i];
            String name = ft.getName().getAsString();
            Element element = null;

            // Wenn noch keine Abfrage gemacht wurde, hole DescribeFeatureType
            if (doc == null) {
                doc = doRequest(postURL, createDescribeFeatureTypeRequest(name));
            }

            // Versuche im bestehenden Document das Element zu finden
            element = getAttributes(name, doc);

            // Wenn es nicht gefunden wurde, hole ein neues Document per DescribeFeatureType
            if (element == null) {
                doc = doRequest(postURL, createDescribeFeatureTypeRequest(name));
                element = getAttributes(name, doc);
            }
            if (checkElementHasGeometry(element)) {
                list.add(element);
            }
        }
        return list;
    }

    /**
     * Testet das Element, ob es einen Geometrietyp besitzt. Besitzt es keinen,
     * so ist es auch f\u00FCr die Anzeige als FeatureLayer uninteressant.
     * @param element das zu pr\u00FCfende Element
     * @return true wenn Geometrietyp gefunden, sonst false
     */
    private boolean checkElementHasGeometry(Element element) {
        for (Element e : (List<Element>) element.getChildren()) {
            if (e.getAttributeValue("type").equals(GEO_PROPERTY_TYPE)) {//NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     * Versucht im gegebenen JDOM-Document das Element mit dem Namen zu finden.
     * @param name gesuchter Elementname
     * @param doc JDOM-Document mit XML-Informationen
     * @return Element-Objekt falls gefunden, sonst null
     */
    private static Element getAttributes(String name, Document doc) {
        // Gefundenes Element zur Zur\u00FCckgabe
        Element result = null;

        String shortName = deleteApp(name);
        String type = null;

        try {
            for (Object o : doc.getContent()) {
                if (o instanceof Element) {
                    Element root = (Element) o;
                    String prefix = null;
                    // Iteriere \u00FCber alle Element-Objekte die Kinder des Roots sind
                    for (Object child : root.getChildren("element", xsd)) {//NOI18N
                        Element e = (Element) child;
                        // Pr\u00FCfe jedes Kind des Root-Knotens, ob der Name \u00FCbereinstimmt
                        if (e.getAttributeValue("name") != null && e.getAttributeValue("name").equals(shortName)) {//NOI18N
                            // Wenn ja, dann speichere ihn tempor\u00E4r und springe aus der Schleife
                            log.debug(">> Element with name = \"" + name + "\" found");//NOI18N
                            e.setAttribute("name", name);//NOI18N
                            result = e;
                            prefix = e.getAttributeValue("type").substring(0, e.getAttributeValue("type").indexOf(":") + 1);//NOI18N
                            type = deleteApp(e.getAttributeValue("type"));//NOI18N
                            log.debug(">> searched Typ = \"" + e.getAttributeValue("type") + "\"");//NOI18N
                            break;
                        }
                    }
                    // Iteriere \u00FCber alle complexType-Elemente die Kinder des Roots sind
                    for (Object child : ((Element) root).getChildren("complexType", xsd)) {//NOI18N
                        Element comp = (Element) child;
                        // Pr\u00FCfe, ob der Name des complexType mit dem gesuchten Typ \u00FCbereinstimmt
                        if (comp.getAttributeValue("name").equals(type)) {//NOI18N
                            // Wenn ja, dann gib die Attribute des complexTypes zur\u00FCck
                            List l = comp.getChild("complexContent", xsd).getChild("extension", xsd).getChild("sequence", xsd).getChildren("element", xsd);//NOI18N
                            while (l.size() > 0) {
                                Element neu = (Element)((Element) l.get(0)).detach();
                                neu.setAttribute("name", prefix + neu.getAttributeValue("name"));//NOI18N
                                result.addContent(neu);
                            }
                            log.debug("OK, result = " + result);//NOI18N
                            return result;
                        }
                    }
                }
                
            }
        } catch (Exception ex) {
            log.error("Error at getElements()", ex);//NOI18N
        }
        return null;
    }

    /**
     * L\u00F6scht das erste Auftreten von "app:" aus einem String.
     * @param s der zu bearbeitende String
     * @return String ohne "app:"
     */
    public static String deleteApp(String s) {
        if (s.startsWith("app:")) {//NOI18N
            return s.replaceAll("app:", "");//NOI18N
        } else {
            return s;
        }
    }

    /**
     * Stellt einen POST-Request an die Server-URL und gibt einen InputStream zur\u00FCck
     * aus dem die Antwort des Servers ausgelesen werden kann.
     * @param serverURL URL des anzusprechenden Servers
     * @param request Request-String
     * @return Serverantwort als InputStream
     */
    public static Document doRequest(URL serverURL, String request) throws Exception {
//        log.info("HTTPCommunicator.doRequest()");
//        // HTTP-Client erstellen
//        HttpClient client = new HttpClient();
//
//        // Hole den Status, ob momentan ein Proxy gesetzt ist
//        String proxySet = System.getProperty("proxySet");
//
//        // Proxy vorhanden ...
//        if (proxySet != null && proxySet.equals("true")) {
//            log.debug("Proxy vorhanden");
//            log.debug("ProxyHost:" + System.getProperty("http.proxyHost"));
//            log.debug("ProxyPort:" + System.getProperty("http.proxyPort"));
//            try {
//                // F\u00FCge den vorhandenen Proxy dem HTTP-Client hinzu
//                client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"),
//                        Integer.parseInt(System.getProperty("http.proxyPort")));
//            } catch (Exception ex) {
//                log.error("Proxy im HTTP-Client setzen fehlgeschlagen", ex);
//            }
//
//        } else { // sonst tue nichts
//            log.debug("kein Proxy");
//        }
//
//        // Erstelle neue POST-Methode mit der Server-URL
//        PostMethod httppost = new PostMethod(serverURL.toString());
//        log.debug("ServerURL = " + httppost.getURI().toString());
//
//        // Requeststring speichern, damit das Original nicht ver\u00E4ndert wird.
        String poststring = request;
//
//        // Erstelle HTML aus dem Request und \u00E4ndere sein Charset auf ISO
        log.debug("WFS Query = " + StaticHtmlTools.stringToHTMLString(poststring));//NOI18N
        //String modifiedString = new String(poststring.getBytes("UTF-8"), "ISO-8859-1");
//        httppost.setRequestEntity(new StringRequestEntity(modifiedString));

        try {
            //POST-Methode an den Server schicken
            //client.executeMethod(httppost);
            final InputStream result = WebAccessManager.getInstance().doRequest(serverURL, new StringReader(poststring), ACCESS_METHODS.POST_REQUEST);
            // Falls Antwort == OK
            //if (httppost.getStatusCode() == HttpStatus.SC_OK) {
            log.debug("Server has processed request and responds");//NOI18N
            log.debug("parse InputStream");//NOI18N
            SAXBuilder builder = new SAXBuilder();
            return builder.build(new InputStreamReader(result, Charset.forName("UTF-8")));//NOI18N
//            } else {
//                log.error("Unexpected failure: " + httppost.getStatusLine().toString());
//            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return null;
    }
}
