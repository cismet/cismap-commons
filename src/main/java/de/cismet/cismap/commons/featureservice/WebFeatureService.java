/*
 * SimpleWebFeatureService.java
 *
 * Created on 17. November 2006, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
// TODO Internationalisieren
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.PropertyContainer;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.features.AnnotatedByPropertyFeature;
import de.cismet.cismap.commons.features.AnnotatedFeature;
import de.cismet.cismap.commons.features.CloneableFeature;
import de.cismet.cismap.commons.features.DefaultWFSFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.featureservice.style.StyleDialog;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.rasterservice.FeatureMapService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticHtmlTools;
import de.cismet.tools.gui.PointSymbolCreator;
import edu.umd.cs.piccolo.PNode;
import groovy.lang.GroovyShell;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Priority;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.FeatureProperty;
import org.deegree2.model.feature.GMLFeatureCollectionDocument;
import org.deegree2.model.spatialschema.JTSAdapter;
import org.jdom.Element;

/**
 * This class provides access to a Web Feature service. Requests will be send to a
 * WFS instance. The response will be parsed and transformed to an internal
 * features representation. These internal features will be send to all registered
 * listeners
 * @author Sebastian Puhl
 */
public class WebFeatureService extends AbstractRetrievalService implements MapService, ServiceLayer, RetrievalServiceLayer, FeatureMapService {

    // <editor-fold defaultstate="collapsed" desc="Declaration ">
    /**
     * The static logger variable
     */
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    /**
     * determines either the layer is enabled or not
     */
    boolean enabled = true;
    /**
     * the bounding box which indicates the features of interest
     */
    BoundingBox bb = null;
    /**
     * the position of this layer in the layer hierachy
     */
    private int layerPosition;
    /**
     * the feature retrieval thread
     */
    private FeatureRetrievalWorker frw;
    /**
     * the name of this layer
     */
    private String name;
    /**
     * determines the transparency of this layer
     */
    private float translucency = 0.2f;
    /**
     * the linecolor which will be used to draw the features of this layer
     */
    //private Color lineColor=new Color(0.6f,0.6f,0.6f,0.7f);
    private Color lineColor = Color.BLACK;
    /**
     * the area color of the geometries
     */
    private Color fillingColor = new Color(0.2f, 0.2f, 0.2f, 0.7f);
    /**
     * the encoding of the xml documents
     */
    private String encoding;
    /**
     * the request which will be send to the WFS
     */
    private String wfsQueryString;
    private Element wfsQuery;
    /**
     * maximal allowed amount of features
     */
    private int maxFeatureCount = 1000;
    /**
     * the hostname of the WFS server
     */
    private String hostname;
    /**
     * the list that holds all the attributes of the wfs-featuretype
     */
    private List<Element> attributes;
    /**
     *the Pnode that holds all the features
     */
    private PNode pNode;
    private boolean visible = true;
    /**
     * Feature to render the Geometry
     */
    private CloneableFeature renderingFeature;
    public static final String DEFAULT_TYPE = "default";
    public static final String CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER = "<cismapBoundingBoxAsGmlPlaceholder />";
    final ArrayList<CloneableFeature> retrievedResults = new ArrayList<CloneableFeature>();

    // </editor-fold>
    /**
     * Standard constructor, needed for cloning a SimpleWebFeatureService
     */
    public WebFeatureService() {
        log.debug("Default Constructor called");
    }

    public static WebFeatureService createSimpleWebFeatureServiceExample() {
        WebFeatureService s = new WebFeatureService();

        DefaultWFSFeature designer = new DefaultWFSFeature();
        designer.setCanBeSelected(false);
        //designer.setFillingStyle(new Color(200,100,50));
        designer.setFillingPaint(Color.GREEN);
        designer.setLinePaint(Color.BLACK);
        designer.setLineWidth(1);
        designer.setTransparency(0.7f);
        designer.setPrimaryAnnotation("if (app:flurstn!=\"0\") {return app:flurstz + \" / \" + app:flurstn;} else {return app:flurstz;}");
        designer.setPrimaryAnnotationScaling(1d);
        designer.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, 12));
        designer.setLineWidth(1);
        designer.setMaxScaleDenominator(2500);
        designer.setMinScaleDenominator(0);
        designer.setAutoScale(true);
        designer.setHighlightingEnabled(true);
        designer.setIdExpression("app:gid");

        s.wfsQueryString = "<wfs:GetFeature version=\"1.1.0\" outputFormat=\"text/xml; subtype=gml/3.1.1\"  xmlns:wfs=\"http://www.opengis.net/wfs\"  xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<wfs:Query xmlns:app=\"http://www.deegree.org/app\" typeName=\"app:wfs_flurstuecke\">" +
                "<wfs:PropertyName>app:the_geom</wfs:PropertyName>" +
                "<wfs:PropertyName>app:gem</wfs:PropertyName>" +
                "<wfs:PropertyName>app:gid</wfs:PropertyName>" +
                "<wfs:PropertyName>app:flur</wfs:PropertyName>" +
                "<wfs:PropertyName>app:flurstz</wfs:PropertyName>" +
                "<wfs:PropertyName>app:flurstn</wfs:PropertyName>" +
                "<wfs:PropertyName>app:hist_ab</wfs:PropertyName> " +
                "<Filter xmlns=\"http://www.opengis.net/ogc\">" +
                "<BBOX> <PropertyName>app:the_geom</PropertyName> " + CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER + " </BBOX>" +
                "</Filter>" +
                "</wfs:Query>" +
                "</wfs:GetFeature>";
        s.hostname = "http://zoidberg.cismet-intra.de:8081/deegree2/ogcwebservice";
        s.enabled = true;
        s.name = "ExampleSimpleWebfeatureLayer";
        s.maxFeatureCount = 4000;
        s.setRenderingFeature(designer);


        return s;
    }

    public WebFeatureService(Element e) {
        Element wfsLayerConf;
        if (e.getName().equals("WebFeatureServiceLayer")) {
            wfsLayerConf = e;
        } else {
            wfsLayerConf = e.getChild("WebFeatureServiceLayer");
        }
        setName(wfsLayerConf.getAttributeValue("name"));
        setVisible(new Boolean(wfsLayerConf.getAttributeValue("visible")));
        setEnabled(new Boolean(wfsLayerConf.getAttributeValue("enabled")));
        setTranslucency(new Float(wfsLayerConf.getAttributeValue("translucency")));
        CapabilityLink cl = new CapabilityLink(wfsLayerConf);
        setHostname(cl.getLink());

    }

    public Element getElement() {
        Element layerConf = new Element("WebFeatureServiceLayer");
        layerConf.setAttribute("name", getName());
        layerConf.setAttribute("visible", new Boolean(getPNode().getVisible()).toString());
        layerConf.setAttribute("enabled", new Boolean(isEnabled()).toString());
        layerConf.setAttribute("translucency", new Float(getTranslucency()).toString());


        CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, hostname, false);
        layerConf.addContent(capLink.getElement());
        //getAttributes()


        //getRenderingFeature()
        if (getRenderingFeature() instanceof ConvertableToXML) {
            layerConf.addContent(new Element("renderingFeature").addContent(((ConvertableToXML) getRenderingFeature()).getElement()));
        }


        //getWfsQuery()
        layerConf.addContent(getWfsQuery().detach());

        //getAttributes
        Element attrib = new Element("Attributes");
        for (Element e : attributes) {
            attrib.addContent(((Element) e.clone()).detach());
        }
        layerConf.addContent(attrib);

//        Iterator lit = getWMSLayers().iterator();
//        while (lit.hasNext()) {
//            Object elem = lit.next();
//            if (elem instanceof WMSLayer) {
//                WMSLayer wmsLayer = (WMSLayer) elem;
//                Element wmsLayerConf = new Element("wmsLayer");
//                wmsLayerConf.setAttribute("name", wmsLayer.getOgcCapabilitiesLayer().getName());
//                wmsLayerConf.setAttribute("enabled", new Boolean(wmsLayer.isEnabled()).toString());
//                try {
//                    wmsLayerConf.setAttribute("style", wmsLayer.getSelectedStyle().getName());
//                } catch (Exception e) {
//                }
//                wmsLayerConf.setAttribute("info", new Boolean(wmsLayer.isQuerySelected()).toString());
//                layerConf.addContent(wmsLayerConf);
//            }
//        }
        return layerConf;
    }

    public WebFeatureService(String name, String host, Element query, List attributes) {
        setName(name);
        setWfsQuery(query);
        setHostname(host);
        setAttributes(attributes);
        setEnabled(true);
        maxFeatureCount = 3000;
        setRenderingFeature(createDefaultStyleFeature());
    }

    private CloneableFeature createDefaultStyleFeature() {
        DefaultWFSFeature designer = new DefaultWFSFeature();
        designer.setCanBeSelected(false);
        Color defColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        designer.setFillingPaint(defColor);
        designer.setLinePaint(StyleDialog.darken(defColor));
        designer.setTransparency(1.0f);
//        designer.setPrimaryAnnotation("app:flurstn");
        designer.setPrimaryAnnotationScaling(1d);
        designer.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, 12));
        FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol(true, true, 10, 1, defColor, StyleDialog.darken(defColor)));
        fas.setSweetSpotX(0.5d);
        fas.setSweetSpotY(0.5d);
        designer.setPointAnnotationSymbol(fas);
        designer.setLineWidth(1);
        designer.setMaxScaleDenominator(2500);
        designer.setMinScaleDenominator(0);
        designer.setAutoScale(true);
        designer.setHighlightingEnabled(false);
        designer.setIdExpression("app:gid");
        return designer;
    }

    /**
     * This method is called when any component need WFS data
     * @param forced the boolean parameter forced determines if a request should be uncondinal
     * executed and retrieved from the WFS
     */
    public void retrieve(boolean forced) {
        log.debug("retrieve Started", new CurrentStackTrace());
        if (frw != null && !frw.isDone()) {
            frw.cancel(true);
            frw = null;
        }
        frw = new FeatureRetrievalWorker();
        frw.execute();
    }

    /**
     * This method creates an one-to-one hard copy of the SimpleWebFeatureService
     * @return the copy of the SimpleWebFeatureService
     */
    public Object clone() {
        WebFeatureService s = new WebFeatureService();
        s.bb = bb;
        s.wfsQuery = wfsQuery;
        s.hostname = hostname;
        s.wfsQueryString = wfsQueryString;
        s.encoding = encoding;
        s.enabled = enabled;
        s.renderingFeature = renderingFeature;
        s.fillingColor = fillingColor;
        s.frw = frw;
        s.layerPosition = layerPosition;
        s.lineColor = lineColor;
        s.listeners = new Vector(listeners);
        s.name = name;
        s.translucency = translucency;
        return s;
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * This Method is used to set the bounding box to determine which features should
     * be retrieved
     * @param bb the bounding box that indicates the area of interest
     */
    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    /**
     * Deliveres the transparency value of the Featues
     * @return the translucency value
     */
    public float getTranslucency() {
        return translucency;
    }

    /**
     * Setter for the name of the SimpleWebFeatureService
     * @param name the new name that will be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Delivers the host-string of the FeatureService
     * @return hostname as string
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Setter for the host-string of the FeatureService
     * @param hostname hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Setter for the attributes of the featureservice.
     * @param attributes attributes to set
     */
    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns a list of all attributes of this featureservice
     */
    public List<Element> getAttributes() {
        return attributes;
    }

    /**
     * Setter for the transparency value
     * @param t the new transparency value
     */
    public void setTranslucency(float t) {
        this.translucency = t;
    }

    /**
     * Sets the layer postion. Dependet on this value the layer will be positioned at
     * top of other layers or behind other layers
     * @param layerPosition The integer value which determines the postion in the layer hierarchy
     */
    public void setLayerPosition(int layerPosition) {
        this.layerPosition = layerPosition;
    }

    /**
     * Enables or disables the WFS Layer
     * @param enabled true enables the layer, false disables it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns if the layer is enabled or disabled
     * @return either true if the layer is enabled or false if its not
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * This method delivers the name of the layer
     * @return the name of the layer
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * This method delivers the postion of the layer in the layer hierachy
     * @return the postion of the layer in the layer hierarchy
     */
    public int getLayerPosition() {
        return layerPosition;
    }

    /**
     * This method checks either a layer can be disabled or not
     * @return true if the layer can be disabled or false if not
     */
    public boolean canBeDisabled() {
        return true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSize(int height, int width) {
    }

    public CloneableFeature getRenderingFeature() {
        return this.renderingFeature;
    }

    public void setRenderingFeature(CloneableFeature renderingFeature) {
        this.renderingFeature = renderingFeature;
    }

    public String getWfsQueryString() {
        if (this.wfsQueryString == null) {
            return WFSOperator.elementToString(wfsQuery);
        } else {
            return wfsQueryString;
        }
    }

    public boolean isQueryStringOverwritingElementQuery() {
        return wfsQueryString != null;
    }

    public void setWfsQueryString(String wfsQueryString) {
//        if (wfsQueryString != null) {
//            wfsQuery = null;
//        }
        this.wfsQueryString = wfsQueryString;
    }

    private void customizeFeature(CloneableFeature cloneableFeature) {
        log.debug("customizeFeature");
        GroovyShell groovyShell = new GroovyShell();
        if (cloneableFeature instanceof PropertyContainer) {
            PropertyContainer propertyContainerFeature = (PropertyContainer) cloneableFeature;
            for (Object key : propertyContainerFeature.getProperties().keySet()) {
                Object property = propertyContainerFeature.getProperty(key.toString());
                groovyShell.setVariable(key.toString().replaceAll(":", "_"), property);
            }
        // log.debug(propertyContainerFeature.getProperties());
        }


        if ((cloneableFeature instanceof AnnotatedFeature && cloneableFeature instanceof PropertyContainer && cloneableFeature instanceof AnnotatedByPropertyFeature)) {
            AnnotatedFeature af = (AnnotatedFeature) cloneableFeature;

            String expression = af.getPrimaryAnnotation();
            expression = expression.replaceAll(":", "_");
//                        String val=propertyContainerFeature.getProperty(key).toString();
//                        if (val!=null) {
//                            af.setPrimaryAnnotation(val);
//                        }
            try {
                af.setPrimaryAnnotation(groovyShell.evaluate(expression).toString());
            } catch (Throwable t) {
                log.warn("Fehler beim Auswerten der Expression " + expression, t);
                af.setPrimaryAnnotation(expression);
            }
            expression = af.getSecondaryAnnotation();
            if (expression != null) {
                expression = expression.replaceAll(":", "_");
                try {
                    af.setSecondaryAnnotation(groovyShell.evaluate(expression).toString());
                } catch (Throwable t) {
                    log.warn("Fehler beim Auswerten der Expression " + expression, t);
                    af.setSecondaryAnnotation(expression);
                }
            }
        }

        if ((cloneableFeature instanceof PropertyContainer && cloneableFeature instanceof FeatureWithId)) {
            FeatureWithId fwid = (FeatureWithId) cloneableFeature;
            try {
                PropertyContainer propertyContainerFeature = (PropertyContainer) cloneableFeature;
                fwid.setId(new Integer(propertyContainerFeature.getProperty(fwid.getIdExpression()).toString()).intValue());
            } catch (Exception e) {
                fwid.setId(-1);
                log.warn("Fehler beim Zuordnen der Id", e);
            }
        }
    }

    class FeatureRetrievalWorker extends SwingWorker<ArrayList<CloneableFeature>, CloneableFeature> {

        private int progress = 0;
        private int size = 0;
        private final long ctm = System.currentTimeMillis();
        private boolean hadErrors = false;
        private String errorMessage = "";

        public FeatureRetrievalWorker() {
        }

        protected ArrayList<CloneableFeature> doInBackground() throws Exception {
            try {
                retrievedResults.clear();

                FeatureCollection featuresCollection = null;
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        RetrievalEvent r = new RetrievalEvent();
                        r.setRequestIdentifier(ctm);
                        fireRetrievalStarted(r);
                    }
                });

                if (isCancelled()) {
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }

                // inserts the current boundigbox in the wfsrequest
                //List coords = request.getChild("Query", Namespace.getNamespace("wfs","http://www.opengis.net/wfs")).getChild("Filter", Namespace.getNamespace("","http://www.opengis.net/ogc")).getChild("And", Namespace.getNamespace("","http://www.opengis.net/ogc")).getChild("BBOX", Namespace.getNamespace("","http://www.opengis.net/ogc")).getChild("Box", Namespace.getNamespace("gml","http://www.opengis.net/gml")).getChildren();
                //TODO only instantiate once

                if (isCancelled()) {
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }


                String postString = getWfsQueryString().replaceAll(CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER, bb.toGmlString());
                if (isCancelled()) {
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }
                HttpClient client = new HttpClient();
                String proxySet = System.getProperty("proxySet");
                if (proxySet != null && proxySet.equals("true")) {
                    log.debug("proxyIs Set");
                    log.debug("ProxyHost:" + System.getProperty("http.proxyHost"));
                    log.debug("ProxyPort:" + System.getProperty("http.proxyPort"));
                    try {
                        client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
                    } catch (Exception e) {
                        log.error("Problem while setting proxy", e);
                    }
                }
                if (isCancelled()) {
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }
                PostMethod httppost = new PostMethod(hostname);
                httppost.setRequestEntity(new StringRequestEntity(postString));
                log.debug("Feature post request: <br><pre>" + StaticHtmlTools.stringToHTMLString(postString) + "</pre>", new CurrentStackTrace());
                //log.debug("Feature post request: "+"<br><pre>"+postString+"</pre>");
                if (isCancelled()) {
                    httppost.releaseConnection();
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }

                long start = System.currentTimeMillis();
                client.executeMethod(httppost);
                long stop = System.currentTimeMillis();
                if (isCancelled()) {
                    httppost.releaseConnection();
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }
                if (log.isEnabledFor(Priority.INFO)) {
                    log.info(((stop - start) / 1000.0) + " Sekunden dauerte das getFeature Request ");
                }
                int code = httppost.getStatusCode();

                if (code == HttpStatus.SC_OK) {
                    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(httppost.getResponseBodyAsStream()), Charset.forName("UTF-8"));
                    if (isCancelled()) {
                        httppost.releaseConnection();
                        log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                        return null;
                    }
                    log.debug("Parse jetzt das Ergebniss");
                    featuresCollection = parse(reader, ctm);
                    log.debug("Parsen fertig");
                    if (isCancelled()) {
                        httppost.releaseConnection();
                        reader.close();
                        log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                        return null;
                    }
                    if (featuresCollection == null) {
                        hadErrors = true;
                        errorMessage = "Fehler beim Parsen des WFS Ergebnisses";
                        client.executeMethod(httppost);
                        log.error("Die Anfrage: \n\"" + StaticHtmlTools.stringToHTMLString(postString) + "\" lieferte folgendes Ergebniss:\n\"" + StaticHtmlTools.stringToHTMLString(httppost.getResponseBodyAsString()) + "\"");
                        httppost.releaseConnection();
                        reader.close();
                        return null;
                    }
                    httppost.releaseConnection();
                    reader.close();
                } else {
                    httppost.releaseConnection();

                    Exception ex = new Exception("Statuscode: " + code + " nicht bekannt transaktion abgebrochen");
                    ex.initCause(new Throwable("Statuscode: " + code + " nicht bekannt transaktion abgebrochen"));
                    throw ex;
                }


                if (isCancelled()) {
                    httppost.releaseConnection();
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }

                if (log.isEnabledFor(Priority.INFO)) {
                    log.info(featuresCollection.size() + " Features im gew�hlten Ausschnitt");
                }
                Feature[] features = featuresCollection.toArray();
                size = features.length;
                if (isCancelled()) {
                    httppost.releaseConnection();
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }
                if (size > maxFeatureCount) {
                    httppost.releaseConnection();
                    hadErrors = true;
                    errorMessage = "Mehr als " + maxFeatureCount + " Features k\u00F6nnen nicht dargestellt werden.";
                    return null;
                }

                if (isCancelled()) {
                    httppost.releaseConnection();
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }



                start = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    if (isCancelled()) {
                        httppost.releaseConnection();
                        log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                        return null;
                    }

                    Feature current = features[i];
                    //Feature stukturieren
                    String id = current.getId();

                    CloneableFeature cloneableFeature = (CloneableFeature) renderingFeature.clone();
                    cloneableFeature.setGeometry(JTSAdapter.export(current.getDefaultGeometryPropertyValue()));
                    if (cloneableFeature instanceof PropertyContainer) {
                        PropertyContainer propertyContainerFeature = (PropertyContainer) cloneableFeature;
                        FeatureProperty[] featureProperties = current.getProperties();
                        for (FeatureProperty fp : featureProperties) {
                            propertyContainerFeature.addProperty(fp.getName().getAsString(), fp.getValue());
                        }
                    // log.debug(propertyContainerFeature.getProperties());
                    }
                    customizeFeature(cloneableFeature);

                    retrievedResults.add(cloneableFeature);
                    publish(cloneableFeature);
                }

                stop = System.currentTimeMillis();
                if (log.isEnabledFor(Priority.INFO)) {
                    log.info(((stop - start) / 1000.0) + " Sekunden dauerte das Umwandeln in das interne Feature Format");
                }

                if (isCancelled()) {
                    httppost.releaseConnection();
                    log.debug("doInBackground (SimpleWebFeatureService) is canceled");
                    return null;
                }

                return retrievedResults;

            } catch (Throwable ex) {
                //debug only


                String message;
                if (ex.getMessage() == null || ex.getMessage().equalsIgnoreCase("null")) {
                    try {
                        message = ex.getMessage();
                    } catch (Throwable t) {
                        message = "Nicht zuordnungsf\u00E4higer Fehler (e.getCause()==null)";
                    }
                } else {
                    message = ex.getMessage();
                }
                log.error("Fehler im FeatureRetrievalWorker: ", ex);
                hadErrors = true;
                errorMessage = message;
                return null;
            }
        }

        /**
         * This method parses the features out of the XML response
         * @param reader The reader which contains the FeatureCollection which should be parsed
         * @return the parsed FeatureCollection
         */
        public FeatureCollection parse(InputStreamReader reader, long time) {

            try {
//                String result="";
//                BufferedReader br=new BufferedReader(reader);
//                
//                String line="";
//                while ((line=br.readLine())!=null) {
//                    result+=line;
//                }
//                
//                log.fatal(StaticHtmlTools.stringToHTMLString(result));
//                log.debug("start parsing");
                long start = System.currentTimeMillis();
//                if (isCancelled()) {
//                    log.debug("doInBackground (parse) is canceled");
//                    return null;
//                }
//                ByteArrayInputStream str = new ByteArrayInputStream(result.getBytes());
//                InputStreamReader resultreader=new InputStreamReader(str);
//                
                GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
                if (isCancelled()) {
                    log.debug("doInBackground (parse) is canceled");
                    return null;
                }
//                doc.load(resultreader, "http://dummyID");
                doc.load(reader, "http://dummyID");
                if (isCancelled()) {
                    log.debug("doInBackground (parse) is canceled");
                    return null;
                }
                FeatureCollection tmp = doc.parse();
                if (isCancelled()) {
                    log.debug("doInBackground (parse) is canceled");
                    return null;
                }
                long stop = System.currentTimeMillis();
                log.info(((stop - start) / 1000.0) + " Sekunden dauerte das Parsen");
                reader.close();
                return tmp;
            } catch (Throwable e) {
                log.error("Fehler beim Parsen der Features.", e);
                hadErrors = true;
                errorMessage = "Fehler beim Bearbeiten der WFS Ergebnisse";
                try {
                    reader.close();
                } catch (Exception silent) {
                }
            //e.printStackTrace();
            }
            return null;
        }

        protected void process(ArrayList<CloneableFeature> chunks) {
            for (CloneableFeature feature : chunks) {
                if (isCancelled()) {
                    break;
                }
                RetrievalEvent re = new RetrievalEvent();
                re.setIsComplete(false);
                double percentage = ((double) progress) / ((double) size) * 100.0;
                re.setPercentageDone(percentage);
                re.setRequestIdentifier(ctm);
                fireRetrievalProgress(re);
            }
        }

        @Override
        protected void done() {
            log.debug("FeatureRetieverWorker done");
            if (isCancelled()) {
                log.debug("FeatureRetrieverWorker canceled (done)");
                errorMessage = "Die WFS Anfrage wurde abgebrochen";
                RetrievalEvent re = new RetrievalEvent();
                re.setHasErrors(false);
                fireRetrievalAborted(re);
                return;
            }
            if (hadErrors) {
                cancelRequest();
                return;
            }
            try {
                ArrayList<CloneableFeature> results = get();
                if (results != null) {
                    log.debug("FeatureRetrieverWorker brachte " + results.size() + " Ergebnisse");
                    RetrievalEvent re = new RetrievalEvent();
                    re.setIsComplete(true);
                    re.setHasErrors(false);

                    re.setRetrievedObject(results);
                    re.setRequestIdentifier(ctm);
                    fireRetrievalComplete(re);
                } else {
                    log.debug("FeatureRetrieverWorker brachte keine Ergebnisse");
                    errorMessage = "WFS Request brachte keine Ergebnisse";
                    cancelRequest();
                }
            } catch (Exception ex) {
                log.debug("Fehler im FeatureRetrieverWorker (done): ", ex);
                errorMessage = "Fehler beim Verarbeiten der WFS Ergebnisse";
                cancelRequest();
            }
        }

        private void cancelRequest() {
            RetrievalEvent re = new RetrievalEvent();
            re.setHasErrors(true);
            re.setRetrievedObject(errorMessage);
            re.setRequestIdentifier(ctm);
            fireRetrievalError(re);
        }
    }

    public void refreshFeatures() {
        ArrayList<CloneableFeature> newRetrievedResults = new ArrayList<CloneableFeature>();

        for (CloneableFeature cf : retrievedResults) {
            CloneableFeature newCf = (CloneableFeature) this.renderingFeature.clone();
            newCf.setGeometry(cf.getGeometry());

            if (cf instanceof PropertyContainer && newCf instanceof PropertyContainer) {
                ((PropertyContainer) newCf).setProperties(((PropertyContainer) cf).getProperties());
            }

            customizeFeature(newCf);
            newRetrievedResults.add(newCf);
        }

        retrievedResults.clear();
        retrievedResults.addAll(newRetrievedResults);

        RetrievalEvent re = new RetrievalEvent();
        re.setIsComplete(true);
        re.setHasErrors(false);
        re.setRefreshExisting(true);
        re.setRetrievedObject(this.retrievedResults);
        re.setRequestIdentifier(System.currentTimeMillis());
        fireRetrievalStarted(re);
        fireRetrievalComplete(re);
    }

    public PNode getPNode() {
        return pNode;
    }

    public void setPNode(PNode pNode) {
        this.pNode = pNode;
    }

    public Element getWfsQuery() {
        return wfsQuery;
    }

    public void setWfsQuery(Element wfsQuery) {
        if (wfsQuery != null) {
            wfsQueryString = null;
        }
        this.wfsQuery = wfsQuery;

    }
}
