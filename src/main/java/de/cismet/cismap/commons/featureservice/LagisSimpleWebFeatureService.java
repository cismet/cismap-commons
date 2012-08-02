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
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import edu.umd.cs.piccolo.PNode;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Priority;
import org.deegree2.model.feature.FeatureProperty;
import org.deegree2.datatypes.QualifiedName;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.FeatureProperty;
import org.deegree2.model.feature.GMLFeatureCollectionDocument;
import org.deegree2.model.spatialschema.JTSAdapter;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * This class provides access to a Web Feature service. Requests will be send to a
 * WFS instance. The response will be parsed and transformed to an internal
 * features representation. These internal features will be send to all registered
 * listeners
 * @author Sebastian Puhl
 */
public class LagisSimpleWebFeatureService {
//public class LagisSimpleWebFeatureService extends AbstractRetrievalService implements MapService,ServiceLayer{
//    
//    /**
//     * The static logger variable
//     */
//    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
//    /**
//     * determines either the layer is enabled or not
//     */
//    boolean enabled=true;
//    /**
//     * the bounding box which indicates the features of interest
//     */
//    BoundingBox bb=null;
//    /**
//     * the position of this layer in the layer hierachy
//     */
//    private int layerPosition;
//    /**
//     * the feature retrieval thread
//     */
//    private FeatureRetrieval fr;
//    /**
//     * the name of this layer
//     */
//    private String name;
//    /**
//     * determines the transparency of this layer
//     */
//    private float translucency=0.2f;
//    /**
//     * the linecolor which will be used to draw the features of this layer
//     */
//    //private Color lineColor=new Color(0.6f,0.6f,0.6f,0.7f);
//    private Color lineColor=Color.BLACK;
//    /**
//     * the area color of the geometries
//     */
//    private Color fillingColor=new Color(0.2f,0.2f,0.2f,0.7f);
//    /**
//     * the encoding of the xml documents
//     */
//    private String encoding;
//    /**
//     * the request which will be send to the WFS
//     */
//    Element wfsRequest;
//    /**
//     * maximal allowed amount of features
//     */
//    private static final int MAX_FEATURE_COUNT=1000;
//    
//    /**
//     * the hostname of the WFS server
//     */
//    private String hostname;
//    
//    
//    /**
//     *the Pnode that holds all the features
//     */
//    private PNode pNode;
//    
//    /**
//     * Standard constructor, needed for cloning a SimpleWebFeatureService
//     */
//    public LagisSimpleWebFeatureService() {
//    }
//    
//    public static final String KEY_GEMARKUNG ="GEMARKUNG";
//    public static final String KEY_FLUR ="FLUR";
//    public static final String KEY_FLURSTZ ="Z\u00C4HLER";
//    public static final String KEY_FLURSTN ="NENNER";
//    public static final String KEY_FLURSTUECK_SCHLUESSEL ="SCHLUESSEL";
//    
//    /**
//     * Creates a new SimpleWebFeature Service from the given XML elment configuration
//     * snippet
//     * @param preferences the XML configuration snippet
//     */
//    public LagisSimpleWebFeatureService(Element preferences) {
//        Attribute layerPositionAttr=preferences.getAttribute("layerPosition");
//        if (layerPositionAttr!=null) {
//            try {
//                layerPosition=layerPositionAttr.getIntValue();
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(LayerPosition)", e);
//            }
//        }
//        
//        Attribute enabledAttr=preferences.getAttribute("enabled");
//        if (enabledAttr!=null){
//            try {
//                enabled=enabledAttr.getBooleanValue();
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(enabled)", e);
//            }
//        }
//        
//        Attribute nameAttr=preferences.getAttribute("name");
//        if (nameAttr!=null){
//            try {
//                name=nameAttr.getValue();
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(name)", e);
//            }
//        }
//        
//        Attribute translucencyAttr=preferences.getAttribute("translucency");
//        if (translucencyAttr!=null){
//            try {
//                translucency=translucencyAttr.getFloatValue();
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(translucency)", e);
//            }
//        }
//        
//        
//        
//        Attribute lineColorAttr=preferences.getAttribute("lineColor");
//        if (lineColorAttr!=null){
//            try {
//                String lineColorString=lineColorAttr.getValue();
//                String[] rgb=lineColorString.split(",");
//                Color c=new Color(new Integer(rgb[0]).intValue(),
//                        new Integer(rgb[1]).intValue(),
//                        new Integer(rgb[2]).intValue(),
//                        (int)(255*translucency));
//                lineColor=c;
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(lineColor)", e);
//            }
//        }
//        
//        Attribute fillingColorAttr=preferences.getAttribute("fillingColor");
//        if (fillingColorAttr!=null){
//            try {
//                String fillingColorString=fillingColorAttr.getValue();
//                String[] rgb=fillingColorString.split(",");
//                Color c=new Color(new Integer(rgb[0]).intValue(),
//                        new Integer(rgb[1]).intValue(),
//                        new Integer(rgb[2]).intValue(),
//                        (int)(255*translucency));
//                fillingColor=c;
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(fillingColor)", e);
//            }
//        }
//        
//        wfsRequest = preferences.getChild("WFSRequest").getChild("GetFeature", Namespace.getNamespace("wfs","http://www.opengis.net/wfs"));
//        if (wfsRequest==null) log.warn("Fehler beim Auslesen der Preferences(wfsRequest)");
//        
//        Element enc = preferences.getChild("Encoding");
//        if(enc != null){
//            encoding = enc.getText();
//        } else {
//            log.warn("Fehler beim Auslesen der Preferences(encoding)");
//        }
//        
//        Element hostAttr=preferences.getChild("Host");
//        if (hostAttr!=null){
//            try {
//                hostname= hostAttr.getText();
//            } catch (Exception e) {
//                log.warn("Fehler beim Auslesen der Preferences(Host)", e);
//            }
//        }
//        
//    }
//    
//    /**
//     * This method is called when any component need WFS data
//     * @param forced the boolean parameter forced determines if a request should be uncondinal
//     * executed and retrieved from the WFS
//     */
//    public void retrieve(boolean forced) {
//        
//        if (fr!=null&&fr.isAlive()) {
//            fr.interrupt();
//            fireRetrievalAborted(new RetrievalEvent());
//        }
//        
//        fr=new FeatureRetrieval();
//        fr.start();
//    }
//    
//    /**
//     * This method creates an one-to-one hard copy of the SimpleWebFeatureService
//     * @return the copy of the SimpleWebFeatureService
//     */
//    public Object clone() {
//        LagisSimpleWebFeatureService s =new LagisSimpleWebFeatureService();
//        s.bb=bb;
//        s.wfsRequest=wfsRequest;
//        s.encoding = encoding;
//        s.enabled=enabled;
//        s.fillingColor=fillingColor;
//        s.fr=fr;
//        s.layerPosition=layerPosition;
//        s.lineColor=lineColor;
//        s.listeners=new Vector(listeners);
//        s.name=name;
//        s.translucency=translucency;
//        return s;
//    }
//    
//    /**
//     * This Method is used to set the bounding box to determine which features should
//     * be retrieved
//     * @param bb the bounding box that indicates the area of interest
//     */
//    public void setBoundingBox(BoundingBox bb) {
//        this.bb = bb;
//    }
//    
//    /**
//     * Deliveres the transparency value of the Featues
//     * @return the translucency value
//     */
//    public float getTranslucency() {
//        return translucency;
//    }
//    
//    /**
//     * Setter for the name of the SimpleWebFeatureService
//     * @param name the new name that will be set
//     */
//    public void setName(String name) {
//        this.name=name;
//    }
//    
//    /**
//     * Setter for the transparency value
//     * @param t the new transparency value
//     */
//    public void setTranslucency(float t) {
//        this.translucency = t;
//    }
//    
//    /**
//     * Sets the layer postion. Dependet on this value the layer will be positioned at
//     * top of other layers or behind other layers
//     * @param layerPosition The integer value which determines the postion in the layer hierarchy
//     */
//    public void setLayerPosition(int layerPosition) {
//        this.layerPosition = layerPosition;
//    }
//    
//    /**
//     * Enables or disables the WFS Layer
//     * @param enabled true enables the layer, false disables it
//     */
//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//    }
//    
//    /**
//     * Returns if the layer is enabled or disabled
//     * @return either true if the layer is enabled or false if its not
//     */
//    public boolean isEnabled() {
//        return enabled;
//    }
//    
//    /**
//     * This method delivers the name of the layer
//     * @return the name of the layer
//     */
//    public String getName() {
//        return name;
//    }
//    
//    /**
//     * This method delivers the postion of the layer in the layer hierachy
//     * @return the postion of the layer in the layer hierarchy
//     */
//    public int getLayerPosition() {
//        return layerPosition;
//    }
//    
//    /**
//     * This method checks either a layer can be disabled or not
//     * @return true if the layer can be disabled or false if not
//     */
//    public boolean canBeDisabled() {
//        return true;
//    }
//    
//    
//    
//    /**
//     * The retrieval thread which contains the retrieval logic. This thread does the
//     * main work.
//     */
//    private class FeatureRetrieval extends Thread {
//        /**
//         * The run method of the retrieval thread. This method is called whenever the thread
//         * is started.
//         */
//        public void run() {
//            try {
//                FeatureCollection featuresCollection = null;
//                long ctm=System.currentTimeMillis();
//                RetrievalEvent r=new RetrievalEvent();
//                r.setRequestIdentifier(ctm);
//                if (isInterrupted()) {
//                    return;
//                }
//                fireRetrievalStarted(r);
//                Element request = (Element)wfsRequest.clone();
//                // inserts the current boundigbox in the wfsrequest
//                List coords = request.getChild("Query", Namespace.getNamespace("wfs","http://www.opengis.net/wfs")).getChild("Filter", Namespace.getNamespace("","http://www.opengis.net/ogc")).getChild("BBOX", Namespace.getNamespace("","http://www.opengis.net/ogc")).getChild("Box", Namespace.getNamespace("gml","http://www.opengis.net/gml")).getChildren();
//                Element coord1 = (Element) coords.get(0);
//                Element coord2 = (Element) coords.get(1);
//                coord1.getChild("X", Namespace.getNamespace("gml","http://www.opengis.net/gml")).setText(Double.toString(bb.getX1()));
//                coord1.getChild("Y", Namespace.getNamespace("gml","http://www.opengis.net/gml")).setText(Double.toString(bb.getY1()));
//                coord2.getChild("X", Namespace.getNamespace("gml","http://www.opengis.net/gml")).setText(Double.toString(bb.getX2()));
//                coord2.getChild("Y", Namespace.getNamespace("gml","http://www.opengis.net/gml")).setText(Double.toString(bb.getY2()));
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    return;
//                }
//                
//                Document doc = new Document();
//                doc.setRootElement(request);
//                XMLOutputter out = new XMLOutputter();
//                out.setEncoding(encoding);
//                String postString  = out.outputString(doc);
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    return;
//                }
//                HttpClient client = new HttpClient();
//                String proxySet = System.getProperty("proxySet");
//                if(proxySet != null && proxySet.equals("true")){
//                    log.debug("proxyIs Set");
//                    log.debug("ProxyHost:"+System.getProperty("http.proxyHost"));
//                    log.debug("ProxyPort:"+System.getProperty("http.proxyPort"));
//                    try {
//                        client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
//                    } catch(Exception e){
//                        log.error("Problem while setting proxy",e);
//                    }
//                }
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    return;
//                }
//                PostMethod httppost = new PostMethod(hostname);
//                
//                httppost.setRequestEntity(new StringRequestEntity(postString));
//                log.debug("Feature post request: "+postString);                
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    httppost.releaseConnection();
//                    return;
//                }
//                
//                
//                long start = System.currentTimeMillis();
//                client.executeMethod(httppost);
//                long stop = System.currentTimeMillis();
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    httppost.releaseConnection();
//                    return;
//                }
//                if(log.isEnabledFor(Priority.INFO)) log.info(((stop-start)/1000.0)+" Sekunden dauerte das getFeature Request ");
//                int code = httppost.getStatusCode();
//                
//                if (code == HttpStatus.SC_OK) {
//                    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(httppost.getResponseBodyAsStream()));
//                    if (isInterrupted()) {
//                        fireRetrievalAborted(r);
//                        httppost.releaseConnection();
//                        return;
//                    }
//                    featuresCollection = parse(reader);
//                    if (isInterrupted()) {
//                        fireRetrievalAborted(r);
//                        httppost.releaseConnection();
//                        return;
//                    }
//                    if(featuresCollection == null){
//                        RetrievalEvent re = new RetrievalEvent();
//                        re.setHasErrors(true);
//                        re.setRetrievedObject("Fehler beim parsen des WFS Response");
//                        re.setRequestIdentifier(ctm);
//                        fireRetrievalError(re);
//                        httppost.releaseConnection();
//                        reader.close();
//                        return;
//                    }
//                    httppost.releaseConnection();
//                } else {
//                    Exception ex = new Exception("Statuscode: "+code+" nicht bekannt transaktion abgebrochen");
//                    ex.initCause(new Throwable("Statuscode: "+code+" nicht bekannt transaktion abgebrochen"));
//                    throw ex;
//                }
//                
//                
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    return;
//                }
//                
//                if(log.isEnabledFor(Priority.INFO)) log.info(featuresCollection.size()+" Features im gew�hlten Ausschnitt");
//                Feature[] features = featuresCollection.toArray();
//                int size = features.length;
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    httppost.releaseConnection();
//                    return;
//                }
//                if(size > MAX_FEATURE_COUNT){
//                    RetrievalEvent re = new RetrievalEvent();
//                    re.setHasErrors(true);
//                    re.setRetrievedObject("Mehr als "+MAX_FEATURE_COUNT+" Features k\u00F6nnen nicht dargestellt werden.");
//                    re.setRequestIdentifier(ctm);
//                    fireRetrievalError(re);
//                    return;
//                }
//                
//                if (isInterrupted()) {
//                    fireRetrievalAborted(r);
//                    return;
//                }
//                
//                Vector<DefaultFeatureServiceFeature> retrievedResults=new Vector<DefaultFeatureServiceFeature>();
//                int progress=0;
//                start = System.currentTimeMillis();                
//                for (int i=0;i<size;i++){
//                    if(isInterrupted()){
//                        fireRetrievalAborted(r);
//                        return;
//                    }
//                    
//                    Feature current = features[i];
//                    //Feature stukturieren
//                    String id = current.getId();
//                    
//                    String type = "Flurstueck";
//                    String groupingKey = "lala";//current.getProperties(new QualifiedName("app","Flurstueck",new URL("http://www.deegree.org/app").toURI()))[0].getValue().toString();
//                    //String fb = current.getProperties(new QualifiedName("app","F",new URL("http://www.deegree.org/app").toURI()))[0].getValue().toString();
//                    //TODO
//                    //log.fatal("Flur: "+current.getAttribute("flur"));
//                    StringBuffer name = new StringBuffer("Flurstueck:");
//                    HashMap flurstueckSchluessel = new HashMap();
//                    if(current.getProperties()!= null){
//                        //for(FeatureProperty prop:current.getProperties()){
//                            //log.fatal("moik: "+prop.getName());
//                            //TODO logfile oder sonst was \u00FCber capabilites!!!!
//                            try{
//                                FeatureProperty gem = current.getProperties(new QualifiedName("app","gem",new URL("http://www.deegree.org/app").toURI()))[0];
//                                FeatureProperty flur = current.getProperties(new QualifiedName("app","flur",new URL("http://www.deegree.org/app").toURI()))[0];
//                                FeatureProperty flurstz = current.getProperties(new QualifiedName("app","flurstz",new URL("http://www.deegree.org/app").toURI()))[0];
//                                FeatureProperty flurstn = current.getProperties(new QualifiedName("app","flurstn",new URL("http://www.deegree.org/app").toURI()))[0];                                
//                                if(gem  != null){
//                                    flurstueckSchluessel.put(KEY_GEMARKUNG,gem.getValue());
//                                    if(gem.getValue()!= null){
//                                        name.append(" "+gem.getValue());
//                                    }
//                                }
//                                if(flur!= null){
//                                    flurstueckSchluessel.put(KEY_FLUR,flur.getValue());
//                                    if(flur.getValue()!= null){
//                                        name.append(" "+flur.getValue());
//                                    }
//                                }
//                                if(flurstz != null){
//                                    flurstueckSchluessel.put(KEY_FLURSTZ,flurstz.getValue());
//                                    if(flurstz.getValue()!= null){
//                                        name.append(" "+flurstz.getValue());
//                                    }
//                                }
//                                if(flurstn != null){
//                                    flurstueckSchluessel.put(KEY_FLURSTN,flurstn.getValue());
//                                    if(flurstn.getValue()!= null){
//                                        name.append("/"+flurstn.getValue());
//                                    }
//                                }
//                                
//                                
//                            } catch(Exception ex){
//                                log.error("Fehler beim abfragen der Properties",ex);
//                                name = new StringBuffer("Schl\u00FCssel nicht komplett");
//                            }
//                            
////                            if(prop.getName().equals("gem")){
////
////                            }
////                            if(prop.getName().equals("flur")){
////
////                            }
////                            if(prop.getName().equals("flurstn")){
////
////                            }
////                            if(prop.getName().equals("flurstz")){
////
////                            }
//                            
//                        //}
//                    }
//                    
//                    DefaultFeatureServiceFeature sf = new DefaultFeatureServiceFeature();
//                    sf.setId(4711);//Integer.parseInt(id.split("_")[1]));
//                    sf.setGeometry(JTSAdapter.export(current.getDefaultGeometryPropertyValue()));
//                    sf.setFeatureService(LagisSimpleWebFeatureService.this);
//                    sf.setFeatureType(type);
//                    sf.setGroupingKey(groupingKey);
//                    sf.setObjectName(name.toString());
//                    sf.setFillingStyle(fillingColor);
//                    sf.setLinePaint(lineColor);
//                    sf.setClientProperties(flurstueckSchluessel);
//                    retrievedResults.add(sf);
//                    progress++;
//                    RetrievalEvent re=new RetrievalEvent();
//                    re.setIsComplete(false);
//                    double percentage=((double)progress)/((double)size)*100.0;
//                    re.setPercentageDone(percentage);
//                    re.setRequestIdentifier(ctm);
//                    fireRetrievalProgress(re);
//                }
//                stop = System.currentTimeMillis();                
//                if(log.isEnabledFor(Priority.INFO)) log.info(((stop-start)/1000.0)+" Sekunden dauerte das Umwandeln in das interne Feature Format");
//                
//                if(isInterrupted()){
//                    RetrievalEvent re=new RetrievalEvent();
//                    re.setIsComplete(false);
//                    re.setRequestIdentifier(ctm);
//                    fireRetrievalAborted(re);
//                } else {
//                    RetrievalEvent re=new RetrievalEvent();
//                    re.setIsComplete(true);
//                    re.setHasErrors(false);
//                    re.setRetrievedObject(retrievedResults);
//                    re.setRequestIdentifier(ctm);
//                    fireRetrievalComplete(re);
//                }
//            } catch(Exception ex){
//                //debug only
//                ex.printStackTrace();
//                RetrievalEvent re=new RetrievalEvent();
//                re.setHasErrors(true);
//                String message;
//                if (ex.getMessage()==null||ex.getMessage().equalsIgnoreCase("null")) {
//                    try {
//                        message=ex.getMessage();
//                    } catch (Throwable t) {
//                        message="Nicht zuordnungsf�higer Fehler (e.getCause()==null";
//                    }
//                } else {
//                    message=ex.getMessage();
//                }
//                re.setRetrievedObject(message);
//                fireRetrievalError(re);
//            }
//        }
//        
//        
//        /**
//         * This method parses the features out of the XML response
//         * @param reader The reader which contains the FeatureCollection which should be parsed
//         * @return the parsed FeatureCollection
//         */
//        public FeatureCollection parse(InputStreamReader reader){
//            try {
//                log.debug("start parsing");
//                long start = System.currentTimeMillis();
//                GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
//                doc.load(reader,"http://dummyID");
//                
//                FeatureCollection tmp = doc.parse();
//                long stop = System.currentTimeMillis();
//                log.info(((stop-start)/1000.0)+" Sekunden dauerte das parsen");
//                return tmp;
//            } catch (Exception e) {
//                log.error("Fehler beim parsen der Features.",e);
//                //e.printStackTrace();
//            }
//            return null;
//        }
//        
//        
//    }
//
//    public PNode getPNode() {
//        return pNode;
//    }
//
//    public void setPNode(PNode pNode) {
//        this.pNode = pNode;
//    }
//    
//    
//    
//    
    
    
}
