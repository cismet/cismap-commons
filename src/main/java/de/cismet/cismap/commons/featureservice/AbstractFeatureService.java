/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
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
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.rasterservice.FeatureMapService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticXMLTools;
import de.cismet.tools.gui.PointSymbolCreator;
import edu.umd.cs.piccolo.PNode;
import groovy.lang.GroovyShell;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import javax.swing.Icon;
import javax.swing.SwingWorker;
import org.apache.log4j.Priority;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureProperty;
import org.deegree2.model.feature.schema.FeatureType;
import org.deegree2.model.feature.schema.PropertyType;
import org.deegree2.model.spatialschema.JTSAdapter;
import org.jdom.Element;

/**
 *
 * @author spuhl
 */
public abstract class AbstractFeatureService extends AbstractRetrievalService implements MapService, ServiceLayer, RetrievalServiceLayer, FeatureMapService {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    /* determines either the layer is enabled or not */
    private boolean enabled = true;
    /* the bounding box which indicates the features of interest */
    private BoundingBox bb = null;
    /* the position of this layer in the layer hierachy */
    private int layerPosition;
    /* the name of this layer */
    private String name;
    /* determines the transparency of this layer */
    private float translucency = 0.2f;
    /* the linecolor which will be used to draw the features of this layer */
    private Color lineColor = Color.BLACK;
    /* the area color of the geometries */
    private Color fillingColor = new Color(0.2f, 0.2f, 0.2f, 0.7f);
    /* the encoding of the xml documents */
    private String encoding;
    /* maximal allowed amount of features */
    private int maxFeatureCount = 3000;
    /* the list that holds all the attributes of the FeatureService */
    private Vector<FeatureServiceAttribute> attributes = new Vector<FeatureServiceAttribute>();
    /* the Pnode that holds all the features */
    private PNode pNode;
    /* the visibility of this layer */
    private boolean visible = true;
    /* Feature to render the Geometry */
    private CloneableFeature renderingFeature;
    /* defaulttype-constant */
    public static final String DEFAULT_TYPE = "default";
    /* XML-constant of the querypart that should be replaced by a boundingbox */
    public static final String CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER = "<cismapBoundingBoxAsGmlPlaceholder />";
    /* resulting featurearraylist of the retrievalWorker */
    private final ArrayList<CloneableFeature> retrievedResults = new ArrayList<CloneableFeature>();
    /* SwingWorker that retrieves the features in the desired area */
    private FeatureRetrievalWorker frw;
    /* part of the XML-configurationfile to customize a featureservice */
    private Element layerConf = null;
    /* featurearray */
    private Feature[] features = null;
    /* is the featurelayer already initialized or not */
    private Boolean isInitialized = false;
    /* worker that retrieves to define the correct geometry */
    private LayerInitWorker layerInitWorker = null;

    /**
     * Creates a new AbstractFeatureService from a XML-element.
     * @param e XML-element with FeatureService-configuration
     * @throws java.lang.Exception
     */
    public AbstractFeatureService(Element e) throws Exception {
        if (e.getName().contains("FeatureServiceLayer")) {
            setLayerConf(e);
        } else {
            if (e.getChild("WebFeatureServiceLayer") != null) {
                setLayerConf(e.getChild("WebFeatureServiceLayer"));
            } else if (e.getChild("DocumentFeatureServiceLayer") != null) {
                setLayerConf(e.getChild("DocumentFeatureServiceLayer"));
            }
        }

        if (layerConf != null) {
            setName(layerConf.getAttributeValue("name"));
            setVisible(new Boolean(layerConf.getAttributeValue("visible")));
            setEnabled(new Boolean(layerConf.getAttributeValue("enabled")));
            setTranslucency(layerConf.getAttribute("translucency").getFloatValue());

            Element xmlAttributes = layerConf.getChild("Attributes");
            attributes = FeatureServiceUtilities.getFeatureServiceAttributes(xmlAttributes);
            setAttributes(attributes);

            DefaultWFSFeature wfsFeature = new DefaultWFSFeature();
            Element fe = layerConf.getChild("renderingFeature").getChild("DefaultWFSFeature");
//                            f.setId(Integer.parseInt(fe.getAttributeValue("id")));
            wfsFeature.setIdExpression(fe.getAttributeValue("idExpression"));
            int lineWidth = Integer.parseInt(fe.getAttributeValue("lineWidth"));
            wfsFeature.setLineWidth(lineWidth);
            wfsFeature.setTransparency(Float.parseFloat(fe.getAttributeValue("transparency")));
            wfsFeature.setPrimaryAnnotation(fe.getAttributeValue("primaryAnnotation"));
            wfsFeature.setSecondaryAnnotation(fe.getAttributeValue("secondaryAnnotation"));
            wfsFeature.setPrimaryAnnotationScaling(Double.parseDouble(fe.getAttributeValue("primaryAnnotationScaling")));
            wfsFeature.setPrimaryAnnotationJustification(Float.parseFloat(fe.getAttributeValue("primaryAnnotationJustification")));
            wfsFeature.setMaxScaleDenominator(Integer.parseInt(fe.getAttributeValue("maxScaleDenominator")));
            wfsFeature.setMinScaleDenominator(Integer.parseInt(fe.getAttributeValue("minScaleDenominator")));
            wfsFeature.setAutoScale(Boolean.parseBoolean(fe.getAttributeValue("autoscale")));
            Color fill = StaticXMLTools.convertXMLElementToColor(fe.getChild("fillingColor").getChild("Color"));
            wfsFeature.setFillingPaint(fill);
            Color line = StaticXMLTools.convertXMLElementToColor(fe.getChild("lineColor").getChild("Color"));
            wfsFeature.setLinePaint(line);
            wfsFeature.setPrimaryAnnotationFont(StaticXMLTools.convertXMLElementToFont(fe.getChild("primaryAnnotationFont").getChild("Font")));
            wfsFeature.setPrimaryAnnotationPaint(StaticXMLTools.convertXMLElementToColor(fe.getChild("primaryAnnotationColor").getChild("Color")));
            wfsFeature.setHighlightingEnabled(Boolean.parseBoolean(fe.getAttributeValue("highlightingEnabled")));
            FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol((line != null), (fill != null), 10, lineWidth, fill, line));
            fas.setSweetSpotX(0.5d);
            fas.setSweetSpotY(0.5d);
            wfsFeature.setPointAnnotationSymbol(fas);
            setRenderingFeature(wfsFeature);
        } else {
            setVisible(true);
            setEnabled(true);
        }
    }

    public AbstractFeatureService(int layerPosition, String name, String encoding, Vector<FeatureServiceAttribute> attributes, PNode pNode, CloneableFeature renderingFeature, FeatureRetrievalWorker frw) throws Exception {
        setLayerPosition(layerPosition);
        setName(name);
        setEncoding(encoding);
        setAttributes(attributes);
        setPNode(pNode);
        setRenderingFeature(renderingFeature);
    }

    /**
     * Constructor that clones the delivered AbstractFeatureService.
     * @param afs FeatureService that should be cloned
     */
    protected AbstractFeatureService(AbstractFeatureService afs) {
        setLayerPosition(afs.getLayerPosition());
        setName(afs.getName());
        setEncoding(afs.getEncoding());
        setPNode(afs.getPNode());
        setAttributes(afs.getAttributes());
        setTranslucency(afs.getTranslucency());
        setEncoding(afs.getEncoding());
        setEnabled(true);

        if (afs.getRenderingFeature() != null) {
            setRenderingFeature(afs.getRenderingFeature());
        } else {
            setRenderingFeature(createDefaultStyleFeature());
        }
        if (afs.getAttributes() != null) {
            Collections.copy(this.attributes, afs.getAttributes());
        } else {
            setAttributes(new Vector<FeatureServiceAttribute>());
        }
    }

    /**
     * Create a new AbstractFeatureService with defaultvalues but the name and attributes.
     * @param name the name of this FeatureService
     * @param attributes vector with all FeatureServiceAttributes of the FeatureService
     * @throws java.lang.Exception
     */
    public AbstractFeatureService(String name, Vector<FeatureServiceAttribute> attributes) throws Exception {
        setName(name);
        setAttributes(attributes);
    }

    /**
     * Modifies the FeatureService if there is a XML-layerconfiguration available,
     * else sets visible and enabled. Calls initConcreteInstance().
     * @throws java.lang.Exception
     */
    private void initLayer() throws Exception {
        if (getRenderingFeature() == null) {
            setRenderingFeature(createDefaultStyleFeature());
        }

        if (attributes == null) {
        }

        if (getFeatures() == null) {
            setFeatures(retrieveFeatures());
        }

        if (getAttributes() == null || getAttributes().size() == 0) {
            log.debug("Keine Feature Attribute vorhanden --> werden erstellt");
            createFeatureAttributes();
        } else {
            //TODO gleichheit abprüfen ? 
            log.debug("Attribute sind bereits vorhanden und müssen nicht erstellt werden");
        }

        initConcreteInstance();
    }

    /**
     * Creates a CloneableFeature with defaultvalues.
     * @return CloneableFeature
     */
    private CloneableFeature createDefaultStyleFeature() {
        DefaultWFSFeature designer = new DefaultWFSFeature();
        designer.setCanBeSelected(false);
        Color defColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        designer.setFillingPaint(defColor);
        designer.setLinePaint(defColor.darker());
        designer.setTransparency(1.0f);
        designer.setPrimaryAnnotation("");
        designer.setPrimaryAnnotationScaling(1d);
        designer.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, 12));
        FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol(true, true, 10, 1, defColor, defColor.darker()));
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
     * Processes the annotation of a feature with groovy and sets an id if available.
     * TODO clear out log-messages because causes lots of them
     * @param cloneableFeature the feature that will be customized
     */
    protected void customizeFeature(CloneableFeature cloneableFeature) {
        //log.debug("customizeFeature"); // TODO delete because causes lots of log-messages
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
            if (expression != null && expression.length() > 0) {
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
            } else {
                //log.info("keine PrimaryAnnotation gesetzt");
            }
            expression = af.getSecondaryAnnotation();
            if (expression != null && expression.length() > 0) {
                expression = expression.replaceAll(":", "_");
                try {
                    af.setSecondaryAnnotation(groovyShell.evaluate(expression).toString());
                } catch (Throwable t) {
                    log.warn("Fehler beim Auswerten der Expression " + expression, t);
                    af.setSecondaryAnnotation(expression);
                }
            } else {
                //log.info("keine SecondaryAnnotation gesetzt");
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

    /**
     * Refreshs all features by replacing their style with the one of the RenderingFeature.
     */
    public void refreshFeatures() {
        ArrayList<CloneableFeature> newRetrievedResults = new ArrayList<CloneableFeature>();
        for (CloneableFeature cf : getRetrievedResults()) {
            CloneableFeature newCf = (CloneableFeature) getRenderingFeature().clone();
            newCf.setGeometry(cf.getGeometry());

            if (cf instanceof PropertyContainer && newCf instanceof PropertyContainer) {
                ((PropertyContainer) newCf).setProperties(((PropertyContainer) cf).getProperties());
            }
            customizeFeature(newCf);
            newRetrievedResults.add(newCf);
        }

        getRetrievedResults().clear();
        getRetrievedResults().addAll(newRetrievedResults);

        RetrievalEvent re = new RetrievalEvent();
        re.setIsComplete(true);
        re.setHasErrors(false);
        re.setRefreshExisting(true);
        re.setRetrievedObject(this.getRetrievedResults());
        re.setRequestIdentifier(System.currentTimeMillis());
        fireRetrievalStarted(re);
        fireRetrievalComplete(re);
    }

    /**
     * Creates a vector with FeatureServiceAttribute of all retrieved features.
     * @param ctm the current time im milliseconds
     * @param currentWorker FeatureRetrievalWorker
     * @return vector with all found FeatureServiceAttributes
     */
    protected void createFeatureAttributes() {
        Vector<FeatureServiceAttribute> tmp = new Vector<FeatureServiceAttribute>();
        try {
            Feature[] fc = getFeatures();
            if (fc != null && fc.length > 0) {
                for (int i = 0; i < fc.length; i++) {
                    FeatureProperty[] featureProperties = fc[i].getProperties();
                    for (FeatureProperty fp : featureProperties) {
                        try {
                            FeatureType type = fc[i].getFeatureType();
                            for (PropertyType pt : type.getProperties()) {
                                //log.fatal("Property Name=" + pt.getName() + " PropertyType=" + pt.getType());
                                //ToDo was ist wenn zwei Geometrien dabei sind
                                FeatureServiceAttribute fsa = new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true);
                                if (!tmp.contains(fsa)) {
                                    tmp.add(fsa);
                                }
                            }
                        } catch (Exception ex) {
                            log.warn("Fehler beim Anlegen eines FeatureServiceAttribute");
                        }
                    }
                }
                setAttributes(tmp);
            } else {
                setAttributes(tmp);
            }
        } catch (Exception ex) {
            log.error("Fehler beim Anlegen der FeatureServiceAttribute", ex);
            setAttributes(tmp);
        }
    }
    Semaphore bouncer = new Semaphore(1);

    /**
     * Creates a new FeatureRetrievalWorker and launchs the retrieval-process.
     * @param forced TODO not used yet
     */
    public void retrieve(boolean forced) {
        log.debug("retrieve Started", new CurrentStackTrace());
        if (frw != null && !frw.isDone()) {
            frw.cancel(true);
            frw = null;
        }

        if (isInitialized) {
            frw = new FeatureRetrievalWorker();
            frw.execute();
        } else {
            synchronized (isInitialized) {
                if (layerInitWorker == null) {
                    layerInitWorker = new LayerInitWorker();
                    layerInitWorker.execute();
                } else {
                    log.info("Layer wird initialisiert --> request wird ignoriert");
                }
            }
        }
    }
    //setFrw(createWorker());        
    class LayerInitWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            initLayer();
            return null;
        }

        @Override
        protected void done() {
            super.done();
            try {
                get();
            } catch (Exception ex) {
                log.error("Fehler beim initalisieren des Layers", ex);
                setIsInitialized(false);
                RetrievalEvent re = new RetrievalEvent();
                re.setRequestIdentifier(System.currentTimeMillis());
                fireRetrievalStarted(re);
                re.setHasErrors(true);
                re.setRetrievedObject("Fehler beim initialisieren des Layers");
                fireRetrievalError(re);
            }
            setIsInitialized(true);
            retrieve(true);
            layerInitWorker = null;
        }
    }
// <editor-fold defaultstate="collapsed" desc="Abstract Methods">
//abstract protected Vector<FeatureServiceAttribute> createFeatureAttributes(final long ctm,final FeatureRetrievalWorker currentWorker) throws Exception;
    abstract protected void initConcreteInstance() throws Exception;

    abstract protected Feature[] retrieveFeatures(final long ctm, final FeatureRetrievalWorker currentWorker)
            throws Exception;

    protected Feature[] retrieveFeatures() throws Exception {
        return retrieveFeatures(System.currentTimeMillis(), null);
    }

    abstract protected String getFeatureLayerType();

    abstract protected void addConcreteElement(Element e);

    abstract public Icon getLayerIcon(
            int type);

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Setters & Getters">
    /**
     * Packs the properties of the AbstractFeatureService as JDom-element. 
     * @return JDom-element that outlines this AbstractFeatureService
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    public void setIsInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public Element getElement() {
        Element conf = new Element(getFeatureLayerType());
        conf.setAttribute("name", getName());
        conf.setAttribute("visible", new Boolean(getPNode().getVisible()).toString());
        conf.setAttribute("enabled", new Boolean(isEnabled()).toString());
        conf.setAttribute("translucency", new Float(getTranslucency()).toString());

        addConcreteElement(conf);

        //getRenderingFeature()
        if (getRenderingFeature() instanceof ConvertableToXML) {
            conf.addContent(new Element("renderingFeature").addContent(((ConvertableToXML) getRenderingFeature()).getElement()));
        }

//getWfsQuery()

//getAttributes
        Element attrib = new Element("Attributes");
        for (FeatureServiceAttribute e : getAttributes()) {
            attrib.addContent(e.getElement());
        }

        conf.addContent(attrib);
        return conf;
    }

    public Element getLayerConf() {
        return layerConf;
    }

    public void setLayerConf(Element layerConf) {
        this.layerConf = layerConf;
    }

    public Feature[] getFeatures() {
        return features;
    }

    public void setFeatures(Feature[] features) {
        this.features = features;
    }

    /**
     * Returns a list of all attributes of this featureservice
     */
    public Vector<FeatureServiceAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Setter for the attributes of the featureservice.
     * @param attributes attributes to set
     */
    public void setAttributes(Vector<FeatureServiceAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * This Method is used to set the bounding box to determine which features should
     * be retrieved
     * @param bb the bounding box that indicates the area of interest
     */
    public BoundingBox getBoundingBox() {
        return bb;
    }

    public void setBoundingBox(BoundingBox bb) {
        this.bb = bb;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Color getFillingColor() {
        return fillingColor;
    }

    public void setFillingColor(Color fillingColor) {
        this.fillingColor = fillingColor;
    }

//    public AbstractFeatureRetrievalWorker getFrw() {
//        return frw;
//    }
//
//    public void setFrw(AbstractFeatureRetrievalWorker frw) {
//        this.frw = frw;
//    }
    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public int getMaxFeatureCount() {
        return maxFeatureCount;
    }

    public void setMaxFeatureCount(int maxFeatureCount) {
        this.maxFeatureCount = maxFeatureCount;
    }

    public CloneableFeature getRenderingFeature() {
        return renderingFeature;
    }

    public void setRenderingFeature(CloneableFeature renderingFeature) {
        this.renderingFeature = renderingFeature;
    }

    public ArrayList<CloneableFeature> getRetrievedResults() {
        return retrievedResults;
    }

    /**
     * Deliveres the transparency value of the Featues
     * @return the translucency value
     */
    public float getTranslucency() {
        return translucency;
    }

    /**
     * Setter for the transparency value
     * @param t the new transparency value
     */
    public void setTranslucency(float t) {
        this.translucency = t;
    }

    /**
     * Setter for the name of the AbstractFeatureService
     * @param name the new name that will be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method delivers the name of the layer
     * @return the name of the layer
     */
    public String getName() {
        return name;
    }

    /**
     * This method delivers the postion of the layer in the layer hierachy
     * @return the postion of the layer in the layer hierarchy
     */
    public int getLayerPosition() {
        return layerPosition;
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
     * Returns if the layer is enabled or disabled
     * @return either true if the layer is enabled or false if its not
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the Layer
     * @param enabled true enables the layer, false disables it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public PNode getPNode() {
        return pNode;
    }

    public void setPNode(PNode pNode) {
        this.pNode = pNode;
    }

    @Override
    public String toString() {
        return getName();
    }
// </editor-fold>
    class FeatureRetrievalWorker extends SwingWorker<ArrayList<CloneableFeature>, CloneableFeature> {

        private int progress = 0;
        private int size = 0;
        private final long ctm = System.currentTimeMillis();
        private String errorMessage = "";
        private Boolean blocker = new Boolean(true);

        public FeatureRetrievalWorker() {
        }

        public long getCtm() {
            return ctm;
        }

        public Boolean getBlocker() {
            return blocker;
        }

        public void setBlocker(Boolean blocker) {
            this.blocker = blocker;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        protected void processFeatures() throws Exception {
            long start = System.currentTimeMillis();
            int geometryIndex = GeometryHeuristics.findBestGeometryIndex(features[0]);
            for (int i = 0; i < size; i++) {
                if (isCancelled()) {
                    log.debug("doInBackground (FeatureService) is canceled");
                    return;
                }

                Feature current = features[i];

                if (AbstractFeatureService.this instanceof StaticFeatureService) {
                    Coordinate[] polyCords = new Coordinate[5];
                    polyCords[0] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY1());
                    polyCords[1] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY2());
                    polyCords[2] = new Coordinate(getBoundingBox().getX2(), getBoundingBox().getY2());
                    polyCords[3] = new Coordinate(getBoundingBox().getX2(), getBoundingBox().getY1());
                    polyCords[4] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY1());
                    Polygon boundingPolygon = (new GeometryFactory()).createPolygon((new GeometryFactory()).createLinearRing(polyCords), null);

                    if (!(JTSAdapter.export(current.getDefaultGeometryPropertyValue())).intersects(boundingPolygon)) {
                        //log.debug("Feature ist nicht in boundingbox");
                        continue;
                    }
                }
                //Feature stukturieren
                String id = current.getId();

                CloneableFeature cloneableFeature = (CloneableFeature) getRenderingFeature().clone();
                try {
                    cloneableFeature.setGeometry(JTSAdapter.export(current.getGeometryPropertyValues()[geometryIndex]));
                } catch (Exception e) {
                    cloneableFeature.setGeometry(JTSAdapter.export(current.getDefaultGeometryPropertyValue()));
                }
                if (cloneableFeature instanceof PropertyContainer) {
                    PropertyContainer propertyContainerFeature = (PropertyContainer) cloneableFeature;
                    FeatureProperty[] featureProperties = current.getProperties();
                    for (FeatureProperty fp : featureProperties) {
                        propertyContainerFeature.addProperty(fp.getName().getAsString(), fp.getValue());
                    }
                // log.debug(propertyContainerFeature.getProperties());
                }
                customizeFeature(cloneableFeature);

                getRetrievedResults().add(cloneableFeature);
                publish(cloneableFeature);
            }

            if (log.isEnabledFor(Priority.INFO)) {
                log.info(getRetrievedResults().size() + " Features im gew\u00E4hlten Ausschnitt");
            }

            if (getRetrievedResults().size() > getMaxFeatureCount()) {
                setErrorMessage("Mehr als " + getMaxFeatureCount() + " Features k\u00F6nnen nicht dargestellt werden.");
                throw new Exception(getErrorMessage());
            }

            long stop = System.currentTimeMillis();
            if (log.isEnabledFor(Priority.INFO)) {
                log.info(((stop - start) / 1000.0) + " Sekunden dauerte das Umwandeln in das interne Feature Format");
            }

            if (isCancelled()) {
                log.debug("doInBackground (AbstractFeatureService) is canceled");
                return;
            }
        }

        protected ArrayList<CloneableFeature> doInBackground() throws Exception {
            try {
                getRetrievedResults().clear();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        RetrievalEvent r = new RetrievalEvent();
                        r.setRequestIdentifier(getCtm());
                        fireRetrievalStarted(r);
                    }
                });

                if (isCancelled()) {
                    log.debug("doInBackground (AbstractFeatureService) is canceled");
                    return null;
                }

                if (!(AbstractFeatureService.this instanceof StaticFeatureService)) {
                    setFeatures(null);
                    setFeatures(retrieveFeatures(getCtm(), this));
                }

                if (isCancelled()) {
                    log.debug("doInBackground (AbstractFeatureService) is canceled");
                    return null;
                }

                if (getFeatures() == null || getFeatures().length == 0) {
                    log.info("Keine Features vorhanden");
                    return null;
                } else {
                    setSize(getFeatures().length);
                    log.debug("Anzahl Features insgesamt: " + getSize());
                }
                processFeatures();
                return getRetrievedResults();

            } catch (Throwable ex) {
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
                setErrorMessage(message);
                throw new Exception("Fehler im FeatureRetrievalWorker", ex);
            }
        }

        protected void process(ArrayList<CloneableFeature> chunks) {
            for (CloneableFeature feature : chunks) {
                if (isCancelled()) {
                    break;
                }
                RetrievalEvent re = new RetrievalEvent();
                re.setIsComplete(false);
                double percentage = ((double) getProgress()) / ((double) getSize()) * 100.0;
                re.setPercentageDone(percentage);
                re.setRequestIdentifier(getCtm());
                fireRetrievalProgress(re);
            }
        }

        @Override
        protected void done() {
            log.debug("FeatureRetieverWorker done");
            if (isCancelled()) {
                log.debug("FeatureRetrieverWorker canceled (done)");
                setErrorMessage("Die Anfrage wurde abgebrochen");
                RetrievalEvent re = new RetrievalEvent();
                re.setHasErrors(false);
                fireRetrievalAborted(re);
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
                    re.setRequestIdentifier(getCtm());
                    fireRetrievalComplete(re);
                } else {
                    log.debug("FeatureRetrieverWorker brachte keine Ergebnisse");
                    //setErrorMessage("Feature Request brachte keine Ergebnisse");
                    RetrievalEvent re = new RetrievalEvent();
                    re.setHasErrors(false);
                    re.setRetrievedObject(new ArrayList<CloneableFeature>());
                    re.setRequestIdentifier(getCtm());
                    fireRetrievalComplete(re);
                }
            } catch (Exception ex) {
                log.debug("Fehler im FeatureRetrieverWorker (done): ", ex);
                setErrorMessage("Fehler beim Abrufen der Features");
                cancelRequest();
            }
        }

        protected void cancelRequest() {
            RetrievalEvent re = new RetrievalEvent();
            re.setHasErrors(true);
            re.setRetrievedObject(getErrorMessage());
            re.setRequestIdentifier(getCtm());
            fireRetrievalError(re);
        }
    }
}
