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

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import org.deegree.commons.utils.Pair;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.style.persistence.sld.SLDParser;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import org.openide.util.Exceptions;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import javax.xml.stream.XMLInputFactory;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XMLObjectFactory;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.CachingFeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.interaction.DefaultQueryButtonAction;
import de.cismet.cismap.commons.rasterservice.FeatureMapService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;

import de.cismet.tools.StaticXMLTools;

/**
 * DOCUMENT ME!
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public abstract class AbstractFeatureService<FT extends FeatureServiceFeature, QT> extends AbstractRetrievalService
        implements MapService,
            ServiceLayer,
            RetrievalServiceLayer,
            FeatureMapService,
            ConvertableToXML,
            Cloneable,
            SLDStyledLayer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(AbstractFeatureService.class);

    /* defaulttype-constant */
    public static final String DEFAULT_TYPE = "default"; // NOI18N
    public static final List<DefaultQueryButtonAction> SQL_QUERY_BUTTONS = new ArrayList<DefaultQueryButtonAction>();

    static {
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("<>"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("Like"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(">"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(">="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("And"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("<"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("<="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("Or"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("_", 1));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("%", 1));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("()") {

                {
                    posCorrection = -1;
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (queryTextArea.getSelectionEnd() == 0) {
                        super.actionPerformed(e);
                    } else {
                        final int start = queryTextArea.getSelectionStart();
                        final int end = queryTextArea.getSelectionEnd();
                        queryTextArea.insert("(", start);
                        queryTextArea.insert(")", end + 1);
                        // jTextArea1.setCaretPosition(end + 2);
                        if (start == end) {
                            CorrectCarret(posCorrection);
                        } else {
                            CorrectCarret((short)2);
                        }
                    }
                }
            });
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("Not"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("Is"));
    }

    //~ Instance fields --------------------------------------------------------

    /* determines either the layer is enabled or not */
    // NOI18N

    /* determines either the layer is enabled or not */
    protected boolean enabled = true;
    /* the bounding box which indicates the features of interest */
    protected BoundingBox bb = null;
    /* the position of this layer in the layer hierachy */
    protected int layerPosition;
    /* the name of this layer */
    protected String name;
    /* determines the transparency of this layer */
    protected float translucency = 0.2f;
    /* the encoding of the xml documents */
    protected String encoding;
    /* maximal allowed amount of features, default is 1000 */
    protected int maxFeatureCount = 1000;
    /* the list that holds all the featureServiceAttributes of the FeatureService */
    protected Map<String, FeatureServiceAttribute> featureServiceAttributes;
    /* the Pnode that holds all the features */
    protected PNode pNode;
    /* the visibility of this layer */
    protected boolean visible = true;
    /* SwingWorker that retrieves the features in the desired area */
    protected FeatureRetrievalWorker featureRetrievalWorker;
    /* is the featurelayer already initialized or not */
    protected Boolean initialized = false;
    /* worker that retrieves to define the correct geometry */
    protected LayerInitWorker layerInitWorker = null;
    protected LayerProperties layerProperties = null;
    protected FeatureFactory featureFactory = null;
    String sldDefinition;
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    Legends legends = new Legends();
    /* the list that holds the names of the featureServiceAttributes of the FeatureService in the specified order */
    protected List<String> orderedFeatureServiceAttributes;
    protected List<DefaultQueryButtonAction> queryButtons = new ArrayList<DefaultQueryButtonAction>(SQL_QUERY_BUTTONS);
    String sldDefinition;
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    Legends legends = new Legends();
    private boolean initialisationError = false;
    private Element initElement = null;
    private boolean selectable = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new <b>uninitilaised</b> instance of a feature service with layer properties set. The Service is fully
     * initialised upon the first call to the {@code retrieve()} operation.
     */
    public AbstractFeatureService() {
        // TODO: mayor design flaw as the usage of an abstract method during construction time can lead to indeterminate
        // state
        this.setLayerProperties(this.createLayerProperties());
    }

    /**
     * Creates a new AbstractFeatureService from a XML-element. Sets all properties of the XML Element but does <b>
     * not</b> initialise. Since the initialisation may take some time, it is perfomed upon the first call to the
     * {@code retrieve()} operation which is rum from separate thread.
     *
     * @param   e  XML-element with FeatureService-configuration
     *
     * @throws  Exception               java.lang.Exception if something went wrong
     * @throws  ClassNotFoundException  DOCUMENT ME!
     *
     * @see     isInitialised()
     */
    public AbstractFeatureService(final Element e) throws Exception {
        // this();
        LOG.info("creating new FeatureService instance from xml element '" + e.getName() + "'");                      // NOI18N
        if (e.getName().equals(this.getFeatureLayerType())) {
            this.initFromElement(e);
        } else if (e.getChild(this.getFeatureLayerType()) != null) {
            this.initFromElement(e.getChild(this.getFeatureLayerType()));
        } else {
            LOG.error("FeatureService could not be initailised from xml: unsupported element '" + e.getName() + "'"); // NOI18N
            throw new ClassNotFoundException("FeatureService could not be initailised from xml: unsupported element '"
                        + e.getName() + "'");                                                                         // NOI18N
        }

        if (this.getLayerProperties() == null) {
            LOG.warn(
                "LayerProperties not properly initialised from XML Element, creating new Properties upon next retrieval"); // NOI18N
            this.layerProperties = this.createLayerProperties();
        }

        if ((this.getFeatureServiceAttributes() == null) || (this.getFeatureServiceAttributes().size() == 0)) {
            LOG.warn(
                "FeatureServiceAttributes not properly initialised from XML Element, creating new Attributes upon next retrieval"); // NOI18N
        }
    }

    /**
     * Create a new <b>uninitialised</b> AbstractFeatureService except for the name and featureServiceAttributes.
     *
     * @param  name        the name of this FeatureService
     * @param  attributes  featureServiceAttributes vector with all FeatureServiceAttributes of the FeatureService
     */
    public AbstractFeatureService(final String name, final List<FeatureServiceAttribute> attributes) {
        // this();
        this.setName(name);
        this.setFeatureServiceAttributes(attributes);
    }

    /**
     * Protected Constructor that clones (shallow) the delivered AbstractFeatureService. Attributes, layer properties
     * and feature factories are not cloned deeply. The FeatureService to be cloned should be initilaised.
     *
     * @param  afs  FeatureService that should be cloned
     */
    protected AbstractFeatureService(final AbstractFeatureService afs) {
        // this();
        // initilaisation updates also the cloned object!
        if (!afs.isInitialized()) {
            LOG.warn("creating copy of uninitialised feature service"); // NOI18N
        }

        this.setLayerPosition(afs.getLayerPosition());
        this.setName(afs.getName());
        this.setEncoding(afs.getEncoding());
        // The cloned featureService and the origin featureService should not use the same pnode,
        // because this would lead to problems, if the cloned layer and the origin layer are
        // used in 2 different MappingComponents
// this.setPNode(afs.getPNode());
        this.setTranslucency(afs.getTranslucency());
        this.setEncoding(afs.getEncoding());
        this.setEnabled(afs.isEnabled());

        this.layerProperties = (afs.getLayerProperties() != null) ? afs.getLayerProperties().clone() : null;
        this.featureFactory = (afs.getFeatureFactory() != null) ? afs.getFeatureFactory().clone() : null;

        final TreeMap<String, FeatureServiceAttribute> attriuteMap = new TreeMap();
        attriuteMap.putAll(afs.getFeatureServiceAttributes());
        this.setFeatureServiceAttributes(attriuteMap);

        this.setInitialized(afs.isInitialized());
        this.setInitialisationError(afs.getInitialisationError());
        this.setInitElement(afs.getInitElement());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialises the FeatureService instance. If the service has already been initialised, the operation clears the
     * layerProperties, the featureFactory and the featureServiceAttributes and forces a complete re-initialisation.
     *
     * @throws  Exception  java.lang.Exception if somethin went wrong
     */
    protected void init() throws Exception {
        // *should* never happen ....
        if ((layerInitWorker == null) || layerInitWorker.isDone()) {
            LOG.error("strange synchronisation problem in Layer Initialisation Thread");           // NOI18N
            throw new Exception("strange synchronisation problem in Layer Initialisation Thread"); // NOI18N
        }

        // check if canceled .......................................................
        if (layerInitWorker.isCancelled()) {
            LOG.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled"); // NOI18N
            return;
        }
        // ..........................................................................

        if (this.isInitialized() || this.isRefreshNeeded()) {
            LOG.warn("layer already initialised, forcing complete re-initialisation"); // NOI18N
            this.setInitialized(false);
            featureFactory = null;
            featureServiceAttributes = null;
            // this.layerProperties = null;
            // this.featureFactory = null;
            // this.featureServiceAttributes = null;
        }

        if (this.getLayerProperties() == null) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("init(): LayerProperties not yet set, creating new LayerProperties instance"); // NOI18N
                }
            }
            this.layerProperties = this.createLayerProperties();
        } else {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("init(): Layer Properties already created");                                   // NOI18N
                }
            }
        }

        // check if canceled .......................................................
        if (layerInitWorker.isCancelled()) {
            LOG.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled"); // NOI18N
            return;
        }
        // ..........................................................................

        if (this.featureFactory == null) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("init(): Feature Factory not yet set, creating new Feature Factory instance"); // NOI18N
                }
            }
            // create the feature Factory
            // all variables required by the concrete FeatureFactory constructor must
            // have been initialised!
            this.featureFactory = this.createFeatureFactory();
        } else {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("init(): Feature Factory already created"); // NOI18N
                }
            }
        }

        // set common properties of the factory
        // implemntation specific properties must be set in the createFeatureFactory()
        this.featureFactory.setMaxFeatureCount(this.getMaxFeatureCount());
        this.featureFactory.setLayerProperties(layerProperties);

        // check if canceled .......................................................
        if (layerInitWorker.isCancelled()) {
            LOG.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled"); // NOI18N
            return;
        }
        // ..........................................................................

        if ((this.getFeatureServiceAttributes() == null) || (this.getFeatureServiceAttributes().size() == 0)) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        "init(): Feature Service Attributes not yet set, creating new  Feature Service Attribute"); // NOI18N
                }
            }
            try {
                this.setFeatureServiceAttributes(featureFactory.createAttributes(layerInitWorker));
            } catch (UnsupportedOperationException uoe) {
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Feature Factory '" + this.getFeatureFactory().getClass().getSimpleName()
                                    + "' does not support Attributes");                                             // NOI18N
                    }
                }
                if (this.getFeatureServiceAttributes() == null) {
                    this.setFeatureServiceAttributes(new ArrayList());
                }
            }
        } else {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("init(): Feature Service Attributes already created");                                // NOI18N
                }
            }
        }

        // check if canceled .......................................................
        if (layerInitWorker.isCancelled()) {
            LOG.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled"); // NOI18N
            return;
        }
        // ..........................................................................

        // idExpression plausibility check
        if ((this.getLayerProperties().getIdExpressionType() == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME)
                    && (this.getLayerProperties().getIdExpression() != null)
                    && (this.getFeatureServiceAttributes() != null)
                    && (this.getFeatureServiceAttributes().size() > 0)) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("checking if property id expression '" + this.getLayerProperties().getIdExpression()
                                + "' is valid");                                       // NOI18N
                }
            }
            boolean found = false;
            for (final FeatureServiceAttribute attribute : this.getFeatureServiceAttributes().values()) {
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("checking attribute '" + attribute.getName() + "'"); // NOI18N
                    }
                }
                found = attribute.getName().equals(this.getLayerProperties().getIdExpression());
                if (found) {
                    if (DEBUG) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("attribute is valid: " + attribute.getName());   // NOI18N
                        }
                    }
                    break;
                }
            }

            if (!found) {
                LOG.warn("property id expression '" + this.getLayerProperties().getIdExpression()
                            + "' not found in attributes, resetting to undefined"); // NOI18N
                this.getLayerProperties().setIdExpression(null, LayerProperties.EXPRESSIONTYPE_UNDEFINED);
            }
        }

        // check if canceled .......................................................
        if (layerInitWorker.isCancelled()) {
            LOG.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled"); // NOI18N
            return;
        }
        // ..........................................................................

        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("init(): performing additional implementation specific initialisation"); // NOI18N
            }
        }
        this.initConcreteInstance();

        // initilaized = true is set in the layerInitWorker
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void initAndWait() throws Exception {
        layerInitWorker = new LayerInitWorker();
        init();
        layerInitWorker = null;
    }

    /**
     * Creates an instance of a service specific LayerProperties implementation.
     *
     * @return  layer properties to be used
     */
    protected abstract LayerProperties createLayerProperties();

    /**
     * Creates an instance of a service specific FeatureFactory implementation. All variables required by the concrete
     * FeatureFactory, e.g. the Layer Properties must have been initialised before this operation is invoked.
     *
     * @return  the constructed FeatureFactory
     *
     * @throws  Exception  id the costruction failed
     */
    protected abstract FeatureFactory createFeatureFactory() throws Exception;

    /**
     * Get the value of featureFactory.
     *
     * @return  the value of featureFactory
     */
    public FeatureFactory getFeatureFactory() {
        return featureFactory;
    }

    /**
     * Get the value of query.
     *
     * @return  the value of query
     */
    public abstract QT getQuery();

    /**
     * Set the value of query.
     *
     * @param  query  new value of query
     */
    public abstract void setQuery(QT query);

    /**
     * Cancels the retrievel or the initialisation threads.
     *
     * @param   workerThread  DOCUMENT ME!
     *
     * @return  {@code true} if a running thread was found and canceled
     */
    protected boolean cancel(final SwingWorker workerThread) {
        boolean canceled = false;

        if ((workerThread != null) && (!workerThread.isDone() || !workerThread.isCancelled())) {
            if (workerThread != null) {
                LOG.warn("canceling Worker Thread: " + workerThread);    // NOI18N
                final boolean cancel = workerThread.cancel(true);
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Worker Thread: " + workerThread + " canceled: " + cancel + " ("
                                    + workerThread.isCancelled() + ")"); // NOI18N
                    }
                }
            }
            canceled = true;
        }

        return canceled;
    }

    /**
     * Creates a new LayerInitWorker or FeatureRetrievalWorker and launches the initialisation or retrieval-process
     * depending wheter the layer has already been initialised or the forced parameter is set to true. A
     * re-initialisation clears the attribute and layer properties cache and thus resets any saved properties to default
     * values.
     *
     * @param  forced  forces a re-initialisation of the layer
     */
    @Override
    public synchronized void retrieve(final boolean forced) {
        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("retrieve started (forced = " + forced + ")");       // NOI18N
            }
        }
        if ((featureRetrievalWorker != null) && !featureRetrievalWorker.isDone()) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("old retrieval thread still running, trying to cancel '"
                                + featureRetrievalWorker.getId() + "' (already canceled = "
                                + featureRetrievalWorker.isCancelled() + ")"); // NOI18N
                }
            }
            final FeatureRetrievalWorker currentWorker = featureRetrievalWorker;
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
            synchronized (featureFactory) {
                if (featureFactory instanceof AbstractFeatureFactory) {
                    ((AbstractFeatureFactory)featureFactory).waitUntilInterruptedIsAllowed();
                }
                cancel(currentWorker);
            }
//                }
//            }).start();
        }

        if (!this.isEnabled() && !this.isVisible()) {
            LOG.warn("Service '" + this.getName() + "' is disabled and invisible, ignoring retrieve() request"); // NOI18N
            return;
        }

        // Initialisierung bereits vorgenommen, d.h. es gibt z.B. Feature Service Attribute
        if (this.isInitialized() && !this.isRefreshNeeded()                                                  /*&& !forced*/) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Layer already initialized, starting feature retrieval");                      // NOI18N
                }
            }
            if (forced && (getFeatureFactory() instanceof CachingFeatureFactory)) {
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("retrieval forced, flushing cache");                                       // NOI18N
                    }
                }
                if ((featureRetrievalWorker != null) && !featureRetrievalWorker.isDone()) {
                    LOG.warn("must wait until thread '" + featureRetrievalWorker
                                + "' is finished before flushing cache");                                    // NOI18N
                    while (!featureRetrievalWorker.isDone()) {
                    }
                    if (DEBUG) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("thread '" + featureRetrievalWorker + "'is finished, flushing cache"); // NOI18N
                        }
                    }
                }
                ((CachingFeatureFactory)getFeatureFactory()).flush();
            }

            this.featureRetrievalWorker = new FeatureRetrievalWorker();
            featureRetrievalWorker.execute();
        } else {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Layer not yet initialized (" + this.initialized + ") or refresh needed ("
                                + this.isRefreshNeeded() + "), starting LayerInitWorker"); // NOI18N
                }
            }
            if (layerInitWorker == null) {
                layerInitWorker = new LayerInitWorker();
                layerInitWorker.execute();
            } else {
                LOG.warn("Layer wird z.Z. initialisiert --> request wird ignoriert");      // NOI18N
            }
        }
    }

    /**
     * This operation is invoked after the default initialisation. Implementation classes may implement this method to
     * perform addditional initialisations.
     *
     * @throws  Exception  if the initialisation fails
     */
    protected abstract void initConcreteInstance() throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract String getFeatureLayerType();

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract Icon getLayerIcon(int type);

    /**
     * Packs the properties of the AbstractFeatureService as JDom-element.
     *
     * @return  JDom-element that outlines this AbstractFeatureService
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isInitialized  DOCUMENT ME!
     */
    protected void setInitialized(final boolean isInitialized) {
        this.initialized = isInitialized;
    }

    /**
     * Returns a list of all featureServiceAttributes of this featureservice.
     *
     * @return  DOCUMENT ME!
     */
    public Map<String, FeatureServiceAttribute> getFeatureServiceAttributes() {
        return this.featureServiceAttributes;
    }

    /**
     * Setter for the featureServiceAttributes of the featureservice.
     *
     * @param  featureServiceAttributes  featureServiceAttributes to set
     */
    public void setFeatureServiceAttributes(final Map<String, FeatureServiceAttribute> featureServiceAttributes) {
        this.featureServiceAttributes = featureServiceAttributes;
    }

    /**
     * Returns a list of all featureServiceAttributes of this featureservice.
     *
     * @return  DOCUMENT ME!
     */
    public List<String> getOrderedFeatureServiceAttributes() {
        return this.orderedFeatureServiceAttributes;
    }

    /**
     * Setter for the featureServiceAttributes of the featureservice.
     *
     * @param  orderedFeatureServiceAttributes  featureServiceAttributes to set
     */
    public void setOrderedFeatureServiceAttributes(final List<String> orderedFeatureServiceAttributes) {
        this.orderedFeatureServiceAttributes = orderedFeatureServiceAttributes;
    }

    /**
     * Setter for the featureServiceAttributes of the featureservice.
     *
     * @param  featureServiceAttributesVector  featureServiceAttributes to set
     */
    protected void setFeatureServiceAttributes(final List<FeatureServiceAttribute> featureServiceAttributesVector) {
        if (featureServiceAttributesVector != null) {
            if (this.featureServiceAttributes == null) {
                this.featureServiceAttributes = new HashMap(featureServiceAttributesVector.size());
                this.orderedFeatureServiceAttributes = new ArrayList<String>();
            } else {
                this.featureServiceAttributes.clear();
                this.orderedFeatureServiceAttributes.clear();
            }

            for (final FeatureServiceAttribute fsa : featureServiceAttributesVector) {
                this.orderedFeatureServiceAttributes.add(fsa.getName());
                this.featureServiceAttributes.put(fsa.getName(), fsa);
            }
        }
    }

    /**
     * This Method is used to set the bounding box to determine which features should be retrieved.
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getBoundingBox() {
        return bb;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bb  DOCUMENT ME!
     */
    @Override
    public void setBoundingBox(final BoundingBox bb) {
        this.bb = bb;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  encoding  DOCUMENT ME!
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getMaxFeatureCount() {
        return maxFeatureCount;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  maxFeatureCount  DOCUMENT ME!
     */
    public void setMaxFeatureCount(final int maxFeatureCount) {
        this.maxFeatureCount = maxFeatureCount;
        if (this.getFeatureFactory() != null) {
            this.getFeatureFactory().setMaxFeatureCount(maxFeatureCount);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LayerProperties getLayerProperties() {
        return layerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerProperties  DOCUMENT ME!
     */
    public void setLayerProperties(final LayerProperties layerProperties) {
        setLayerProperties(layerProperties, true);
    }

    /**
     * Sets the new layer properties of the service and.
     *
     * @param  layerProperties  DOCUMENT ME!
     * @param  refreshFeatures  DOCUMENT ME!
     */
    public void setLayerProperties(final LayerProperties layerProperties, final boolean refreshFeatures) {
        this.layerProperties = layerProperties;

        if (this.featureFactory != null) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("setLayerProperties: new layer properties are also applied to all cached features!"); // NOI18N
                }
            }
            // layer properties are appiled to last created features
            if ((featureRetrievalWorker != null) && !featureRetrievalWorker.isDone()) {
                LOG.warn("must wait until thread '" + featureRetrievalWorker
                            + "' is finished before applying new layer properties");   // NOI18N
                while (!featureRetrievalWorker.isDone()) {
                    // wait ....
                }
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("thread '" + featureRetrievalWorker
                                    + "' is finished, applying new layer properties"); // NOI18N
                    }
                }
            }

            this.featureFactory.setLayerProperties(layerProperties);
            if (refreshFeatures) {
                refreshFeatures();
            }
        }
    }

    /**
     * Sets the new layer properties of the service and.
     */
    public void refresh() {
        if (this.featureFactory != null) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("setLayerProperties: new layer properties are also applied to all cached features!"); // NOI18N
                }
            }
            // layer properties are applied to last created features
            if ((featureRetrievalWorker != null) && !featureRetrievalWorker.isDone()) {
                LOG.warn("must wait until thread '" + featureRetrievalWorker
                            + "' is finished before applying new layer properties");   // NOI18N
                while (!featureRetrievalWorker.isDone()) {
                    // wait ....
                }
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("thread '" + featureRetrievalWorker
                                    + "' is finished, applying new layer properties"); // NOI18N
                    }
                }
            }

            this.featureFactory.setLayerProperties(layerProperties);
            refreshFeatures();
        }
    }

    /**
     * Deliveres the transparency value of the Featues.
     *
     * @return  the translucency value
     */
    @Override
    public float getTranslucency() {
        return translucency;
    }

    /**
     * Setter for the transparency value.
     *
     * @param  t  the new transparency value
     */
    @Override
    public void setTranslucency(final float t) {
        this.translucency = t;
    }

    /**
     * Setter for the name of the AbstractFeatureService.
     *
     * @param  name  the new name that will be set
     */
    @Override
    public void setName(final String name) {
        this.name = name;
        if (featureFactory != null) {
            featureFactory.setLayerName(name);
        }
    }

    /**
     * This method delivers the name of the layer.
     *
     * @return  the name of the layer
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * This method delivers the postion of the layer in the layer hierachy.
     *
     * @return  the postion of the layer in the layer hierarchy
     */
    @Override
    public int getLayerPosition() {
        return layerPosition;
    }

    /**
     * Sets the layer postion. Dependet on this value the layer will be positioned at top of other layers or behind
     * other layers
     *
     * @param  layerPosition  The integer value which determines the postion in the layer hierarchy
     */
    @Override
    public void setLayerPosition(final int layerPosition) {
        this.layerPosition = layerPosition;
    }

    /**
     * Returns if the layer is enabled or disabled.
     *
     * @return  either true if the layer is enabled or false if its not
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the Layer.
     *
     * @param  enabled  true enables the layer, false disables it
     */
    @Override
    public void setEnabled(final boolean enabled) {
        if (!enabled) {
            if (!this.canBeDisabled()) {
                LOG.warn("Service '" + this.getName() + "' cannot be disabled"); // NOI18N
            } else {
                this.enabled = false;
                //
            }
        } else {
            this.enabled = true;
        }
    }

    /**
     * This method checks either a layer can be disabled or not.
     *
     * @return  true if the layer can be disabled or false if not
     */
    @Override
    public boolean canBeDisabled() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  height  DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     */
    @Override
    public void setSize(final int height, final int width) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public PNode getPNode() {
        return pNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pNode  DOCUMENT ME!
     */
    @Override
    public void setPNode(final PNode pNode) {
        this.pNode = pNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Element toElement() {
        final Element element = new Element(getFeatureLayerType());
        element.setAttribute("name", getName());                                                    // NOI18N
        element.setAttribute("type", this.getClass().getCanonicalName());                           // NOI18N
        element.setAttribute("visible", Boolean.valueOf(getPNode().getVisible()).toString());       // NOI18N
        element.setAttribute("enabled", Boolean.valueOf(isEnabled()).toString());                   // NOI18N
        element.setAttribute("translucency", new Float(getTranslucency()).toString());              // NOI18N
        element.setAttribute("maxFeatureCount", new Integer(this.getMaxFeatureCount()).toString()); // NOI18N
        element.setAttribute("layerPosition", new Integer(this.getLayerPosition()).toString());     // NOI18N

        if ((this.getFeatureServiceAttributes() != null) && (this.getFeatureServiceAttributes().size() > 0)) {
            final Element attrib = new Element("Attributes");                    // NOI18N
            for (final String key : getOrderedFeatureServiceAttributes()) {
                final FeatureServiceAttribute e = getFeatureServiceAttributes().get(key);
                attrib.addContent(e.toElement());
            }
            element.addContent(attrib);
        } else {
            LOG.warn("FeatureServiceAttributes are null and will not be saved"); // NOI18N
        }

        if (this.getLayerProperties() != null) {
            final Element layerPropertiesElement = this.getLayerProperties().toElement();
            element.addContent(layerPropertiesElement);
        } else {
            LOG.warn("Layer Properties are null and will not be saved"); // NOI18N
        }
        try {
            final Document sldDoc = new org.jdom.input.SAXBuilder().build(getSLDDefiniton());
            element.addContent(sldDoc.detachRootElement());
        } catch (JDOMException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   element  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public void initFromElement(Element element) throws Exception {
        if (element == null) {
            element = this.getInitElement();
        } else {
            this.setInitElement((Element)element.clone());
        }

        if (element.getAttributeValue("name") != null)                                                  // NOI18N
        {
            this.setName(element.getAttributeValue("name"));                                            // NOI18N
        }
        if (element.getAttributeValue("visible") != null)                                               // NOI18N
        {
            this.setVisible(Boolean.valueOf(element.getAttributeValue("visible")));                     // NOI18N
        }
        if (element.getAttributeValue("enabled") != null)                                               // NOI18N
        {
            this.setEnabled(Boolean.valueOf(element.getAttributeValue("enabled")));                     // NOI18N
        }
        if (element.getAttributeValue("translucency") != null)                                          // NOI18N
        {
            this.setTranslucency(element.getAttribute("translucency").getFloatValue());                 // NOI18N
        }
        if (element.getAttributeValue("maxFeatureCount") != null)                                       // NOI18N
        {
            this.setMaxFeatureCount(element.getAttribute("maxFeatureCount").getIntValue());             // NOI18N
        }
        if (element.getAttributeValue("layerPosition") != null)                                         // NOI18N
        {
            this.setLayerPosition(element.getAttribute("layerPosition").getIntValue());                 // NOI18N
        }
        if (element.getAttributeValue("maxFeatureCount") != null)                                       // NOI18N
        {
            element.setAttribute("maxFeatureCount", new Integer(this.getMaxFeatureCount()).toString()); // NOI18N
        }
        if (element.getAttributeValue("layerPosition") != null)                                         // NOI18N
        {
            element.setAttribute("layerPosition", new Integer(this.getLayerPosition()).toString());     // NOI18N
        }

        final Element xmlAttributes = element.getChild("Attributes"); // NOI18N
        if (xmlAttributes != null) {
            featureServiceAttributes = FeatureServiceUtilities.getFeatureServiceAttributes(xmlAttributes);
            orderedFeatureServiceAttributes = FeatureServiceUtilities.getOrderedFeatureServiceAttributes(xmlAttributes);
            this.setFeatureServiceAttributes(featureServiceAttributes);
        }

        if (element.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) == null) {
            LOG.warn("fromElement: restoring object from deprecated xml element");              // NOI18N
            try {
                this.fromOldElement(element);
            } catch (final Exception e) {
                LOG.warn("could not restore deprecated configuration: \n" + e.getMessage(), e); // NOI18N
            }
        } else if (element.getChild("LayerProperties") != null)                                 // NOI18N
        {
            LayerProperties restoredLayerProperties = null;
            try {
                final Element layerPropertiesElement = element.getChild(LayerProperties.LAYER_PROPERTIES_ELEMENT);
                restoredLayerProperties = (LayerProperties)XMLObjectFactory.restoreObjectfromElement(
                        layerPropertiesElement);
                restoredLayerProperties.setFeatureService(this);
            } catch (Exception t) {
                LOG.error("could not restore generic style element '"                           // NOI18N
                            + element.getChild("LayerProperties").getAttribute(ConvertableToXML.TYPE_ATTRIBUTE)
                            + "': \n" + t.getMessage(),
                    t);                                                                         // NOI18N
            }
            this.layerProperties = restoredLayerProperties;
        } else {
            LOG.warn("no layer properties ");                                                   // NOI18N
        }
        final Element sldStyle = element.getChild(
                "StyledLayerDescriptor",
                Namespace.getNamespace("http://www.opengis.net/sld"));
        if (sldStyle != null) {
            sldDefinition = new org.jdom.output.XMLOutputter().outputString(sldStyle);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param       element  old XML Configuration
     *
     * @deprecated  DOCUMENT ME!
     */
    @Deprecated
    private void fromOldElement(final Element element) {
        final DefaultFeatureServiceFeature wfsFeature = new DefaultFeatureServiceFeature();
        final Element renderingFeature = element.getChild("renderingFeature").getChild("DefaultWFSFeature");            // NOI18N
        wfsFeature.setIdExpression(renderingFeature.getAttributeValue("idExpression"));                                 // NOI18N
        final int lineWidth = Integer.parseInt(renderingFeature.getAttributeValue("lineWidth"));                        // NOI18N
        wfsFeature.setLineWidth(lineWidth);
        wfsFeature.setTransparency(Float.parseFloat(renderingFeature.getAttributeValue("transparency")));               // NOI18N
        wfsFeature.setPrimaryAnnotation(renderingFeature.getAttributeValue("primaryAnnotation"));                       // NOI18N
        wfsFeature.setSecondaryAnnotation(renderingFeature.getAttributeValue("secondaryAnnotation"));                   // NOI18N
        wfsFeature.setPrimaryAnnotationScaling(Double.parseDouble(
                renderingFeature.getAttributeValue("primaryAnnotationScaling")));                                       // NOI18N
        wfsFeature.setPrimaryAnnotationJustification(Float.parseFloat(
                renderingFeature.getAttributeValue("primaryAnnotationJustification")));                                 // NOI18N
        wfsFeature.setMaxScaleDenominator(Integer.parseInt(renderingFeature.getAttributeValue("maxScaleDenominator"))); // NOI18N
        wfsFeature.setMinScaleDenominator(Integer.parseInt(renderingFeature.getAttributeValue("minScaleDenominator"))); // NOI18N
        wfsFeature.setAutoScale(Boolean.parseBoolean(renderingFeature.getAttributeValue("autoscale")));                 // NOI18N

        // color kann null sein (fill disabled oder line disabled)
        Color fill = null;
        Color line = null;

        if (renderingFeature.getChild("fillingColor") != null)                                                           // NOI18N
        {
            fill = StaticXMLTools.convertXMLElementToColor(renderingFeature.getChild("fillingColor").getChild("Color")); // NOI18N
        }
        wfsFeature.setFillingPaint(fill);

        if (renderingFeature.getChild("lineColor") != null)                                                           // NOI18N
        {
            line = StaticXMLTools.convertXMLElementToColor(renderingFeature.getChild("lineColor").getChild("Color")); // NOI18N
        }
        wfsFeature.setLinePaint(line);

        wfsFeature.setPrimaryAnnotationFont(StaticXMLTools.convertXMLElementToFont(
                renderingFeature.getChild("primaryAnnotationFont").getChild("Font")));   // NOI18N
        wfsFeature.setPrimaryAnnotationPaint(StaticXMLTools.convertXMLElementToColor(
                renderingFeature.getChild("primaryAnnotationColor").getChild("Color"))); // NOI18N
        wfsFeature.setHighlightingEnabled(Boolean.parseBoolean(
                renderingFeature.getAttributeValue("highlightingEnabled")));             // NOI18N
        wfsFeature.getLayerProperties().getStyle().setPointSymbolFilename(Style.AUTO_POINTSYMBOL);

        this.layerProperties = wfsFeature.getLayerProperties();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  initialisationError  DOCUMENT ME!
     */
    public void setInitialisationError(final boolean initialisationError) {
        this.initialisationError = initialisationError;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getInitialisationError() {
        return this.initialisationError;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<DefaultQueryButtonAction> getQueryButtons() {
        return queryButtons;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String decoratePropertyName(final String name) {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     * @param   value       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String decoratePropertyValue(final String columnName, final String value) {
        return "'" + value + "'";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   boundingBox  DOCUMENT ME!
     * @param   offset       DOCUMENT ME!
     * @param   limit        DOCUMENT ME!
     * @param   orderBy      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public List retrieveFeatures(final BoundingBox boundingBox, final int offset, final int limit, final String orderBy)
            throws Exception {
        if (!initialized) {
            initConcreteInstance();
        }
        return getFeatureFactory().createFeatures(getQuery(), boundingBox, layerInitWorker);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   boundingBox  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getFeatureCount(final BoundingBox boundingBox) {
        if (boundingBox == null) {
            return getFeatureFactory().getFeatureCount(this.bb);
        } else {
            return getFeatureFactory().getFeatureCount(boundingBox);
        }
    }

    /**
     * This operation class the {@code createFeatures()} operation of the current FeatureFactory. Implementation classes
     * may override this method to pass additional parameters to the {@code createFeatures()} operation of the specific
     * FeatureFactory implementation.
     *
     * @param   worker  the current worker thred that is observed
     *
     * @return  the FeatureServiceFeatures created by the current Factory
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected List<FT> retrieveFeatures(final FeatureRetrievalWorker worker) throws Exception {
        if (initialisationError
                    || ((this instanceof WebFeatureService) && (((WebFeatureService)this).getFeature() == null))) {
            if (((this instanceof WebFeatureService) && (((WebFeatureService)this).getFeature() == null))) {
                initialisationError = true;
            }
            initFromElement(null);
            setInitialized(false);
            featureFactory = createFeatureFactory();
            this.featureFactory.setMaxFeatureCount(this.getMaxFeatureCount());
            this.featureFactory.setLayerProperties(layerProperties);
            initConcreteInstance();
            if (initialisationError) {
                throw new Exception(getErrorObject().toString());
            }
        }
        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("FRW[" + worker.getId() + "]: retrieveFeatures started"); // NOI18N
            }
        }
        // check if canceled .......................................................
        if (worker.isCancelled()) {
            LOG.warn("FRW[" + worker.getId() + "]: retrieveFeatures is canceled"); // NOI18N
            return null;
        }
        // check if canceled .......................................................

        final long start = System.currentTimeMillis();
        final List<FT> features = getFeatureFactory().createFeatures(this.getQuery(), this.getBoundingBox(), worker);
        if (features != null) {
            LOG.info("FRW[" + worker.getId() + "]: " + features.size() + " features retrieved in "
                        + (System.currentTimeMillis() - start) + " ms");                                        // NOI18N
        } else {
            LOG.warn("FRW[" + worker.getId() + "]: no features found (canceled=" + worker.isCancelled() + ")"); // NOI18N
        }

        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("FRW[" + worker.getId() + "]: retrieveFeatures completed"); // NOI18N
            }
        }
        return features;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the initElement
     */
    public Element getInitElement() {
        return initElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  initElement  the initElement to set
     */
    public void setInitElement(final Element initElement) {
        this.initElement = initElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectable  DOCUMENT ME!
     */
    public void setSelectable(final boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSelectable() {
        return this.selectable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isEditable() {
        return false;
    }
    
    @Override
    public Reader getSLDDefiniton() {
        return (sldDefinition == null) ? null // new InputStreamReader(getClass().getResourceAsStream("/testSLD.xml"))
                                       : new StringReader(sldDefinition);
    }

    @Override
    public void setSLDInputStream(final String inputStream) {
        if ((inputStream == null) || inputStream.isEmpty()) {
            sldDefinition = null;
            featureFactory.setSLDStyle(null);
            return;
        }
        sldDefinition = inputStream;
        final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = parseSLD(new StringReader(
                    inputStream));
        if ((styles == null) || styles.isEmpty()) {
            return;
        }
        featureFactory.setSLDStyle(styles);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   input  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> parseSLD(final Reader input) {
        Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = null;
        try {
            styles = SLDParser.getStyles(factory.createXMLStreamReader(input));
        } catch (Exception ex) {
            LOG.error("Fehler in der SLD", ex);
        }
        if (styles == null) {
            LOG.info("SLD Parser funtkioniert nicht");
        }
        return styles;
    }

    @Override
    public Pair<Integer, Integer> getLegendSize(final int nr) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            return getLegendSize((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0));
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   style  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Pair<Integer, Integer> getLegendSize(final org.deegree.style.se.unevaluated.Style style) {
        return legends.getLegendSize(style);
    }

    @Override
    public Pair<Integer, Integer> getLegendSize() {
        return getLegendSize(0);
    }

    @Override
    public List<Pair<Integer, Integer>> getLegendSizes() {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        final List<Pair<Integer, Integer>> sizes = new LinkedList<Pair<Integer, Integer>>();
        for (final org.deegree.style.se.unevaluated.Style style : styles) {
            sizes.add(getLegendSize(style));
        }
        return sizes;
    }

    @Override
    public void getLegend(final int width, final int height, final Graphics2D g2d) {
        getLegend(0, width, height, g2d);
    }

    @Override
    public void getLegend(final int nr, final int width, final int height, final Graphics2D g2d) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            getLegend((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0),
                width,
                height,
                g2d);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  style   DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  g2d     DOCUMENT ME!
     */
    private void getLegend(final org.deegree.style.se.unevaluated.Style style,
            final int width,
            final int height,
            final Graphics2D g2d) {
        legends.paintLegend(style,
            width,
            height,
            g2d);
    }

    @Override
    public void getLegends(final List<Pair<Integer, Integer>> sizes, final Graphics2D[] g2d) {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        for (int i = 0; i < styles.size(); i++) {
            legends.paintLegend(styles.get(i), sizes.get(i).first, sizes.get(i).second, g2d[i]);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshFeatures() {
        final List<FT> lastCreatedFeatures = this.featureFactory.getLastCreatedFeatures();
        if (lastCreatedFeatures.size() > 0) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(lastCreatedFeatures.size()
                                + " last created features refreshed, fiering retrival event"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final RetrievalEvent re = new RetrievalEvent();
                        re.setIsComplete(true);
                        re.setHasErrors(false);
                        re.setRefreshExisting(true);
                        re.setRetrievedObject(lastCreatedFeatures);
                        re.setRequestIdentifier(System.currentTimeMillis());
                        fireRetrievalStarted(re);
                        fireRetrievalComplete(re);
                    }
                });
        } else {
            LOG.warn("no last created features that could be refreshed found"); // NOI18N
        }
    }

    @Override
    public Pair<Integer, Integer> getLegendSize(final int nr) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            return getLegendSize((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0));
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   style  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Pair<Integer, Integer> getLegendSize(final org.deegree.style.se.unevaluated.Style style) {
        return legends.getLegendSize(style);
    }

    @Override
    public Pair<Integer, Integer> getLegendSize() {
        return getLegendSize(0);
    }

    @Override
    public List<Pair<Integer, Integer>> getLegendSizes() {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        final List<Pair<Integer, Integer>> sizes = new LinkedList<Pair<Integer, Integer>>();
        for (final org.deegree.style.se.unevaluated.Style style : styles) {
            sizes.add(getLegendSize(style));
        }
        return sizes;
    }

    @Override
    public void getLegend(final int width, final int height, final Graphics2D g2d) {
        getLegend(0, width, height, g2d);
    }

    @Override
    public void getLegend(final int nr, final int width, final int height, final Graphics2D g2d) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            getLegend((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0),
                width,
                height,
                g2d);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  style   DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  g2d     DOCUMENT ME!
     */
    private void getLegend(final org.deegree.style.se.unevaluated.Style style,
            final int width,
            final int height,
            final Graphics2D g2d) {
        legends.paintLegend(style,
            width,
            height,
            g2d);
    }

    @Override
    public void getLegends(final List<Pair<Integer, Integer>> sizes, final Graphics2D[] g2d) {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        for (int i = 0; i < styles.size(); i++) {
            legends.paintLegend(styles.get(i), sizes.get(i).first, sizes.get(i).second, g2d[i]);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshFeatures() {
        final List<FT> lastCreatedFeatures = this.featureFactory.getLastCreatedFeatures();
        if (lastCreatedFeatures.size() > 0) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(lastCreatedFeatures.size()
                                + " last created features refreshed, fiering retrival event"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final RetrievalEvent re = new RetrievalEvent();
                        re.setIsComplete(true);
                        re.setHasErrors(false);
                        re.setRefreshExisting(true);
                        re.setRetrievedObject(lastCreatedFeatures);
                        re.setRequestIdentifier(System.currentTimeMillis());
                        fireRetrievalStarted(re);
                        fireRetrievalComplete(re);
                    }
                });
        } else {
            LOG.warn("no last created features that could be refreshed found"); // NOI18N
        }
    }

    @Override
    public Pair<Integer, Integer> getLegendSize(final int nr) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            return getLegendSize((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0));
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   style  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Pair<Integer, Integer> getLegendSize(final org.deegree.style.se.unevaluated.Style style) {
        return legends.getLegendSize(style);
    }

    @Override
    public Pair<Integer, Integer> getLegendSize() {
        return getLegendSize(0);
    }

    @Override
    public List<Pair<Integer, Integer>> getLegendSizes() {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        final List<Pair<Integer, Integer>> sizes = new LinkedList<Pair<Integer, Integer>>();
        for (final org.deegree.style.se.unevaluated.Style style : styles) {
            sizes.add(getLegendSize(style));
        }
        return sizes;
    }

    @Override
    public void getLegend(final int width, final int height, final Graphics2D g2d) {
        getLegend(0, width, height, g2d);
    }

    @Override
    public void getLegend(final int nr, final int width, final int height, final Graphics2D g2d) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            getLegend((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0),
                width,
                height,
                g2d);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  style   DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  g2d     DOCUMENT ME!
     */
    private void getLegend(final org.deegree.style.se.unevaluated.Style style,
            final int width,
            final int height,
            final Graphics2D g2d) {
        legends.paintLegend(style,
            width,
            height,
            g2d);
    }

    @Override
    public void getLegends(final List<Pair<Integer, Integer>> sizes, final Graphics2D[] g2d) {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        for (int i = 0; i < styles.size(); i++) {
            legends.paintLegend(styles.get(i), sizes.get(i).first, sizes.get(i).second, g2d[i]);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshFeatures() {
        final List<FT> lastCreatedFeatures = this.featureFactory.getLastCreatedFeatures();
        if (lastCreatedFeatures.size() > 0) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(lastCreatedFeatures.size()
                                + " last created features refreshed, fiering retrival event"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final RetrievalEvent re = new RetrievalEvent();
                        re.setIsComplete(true);
                        re.setHasErrors(false);
                        re.setRefreshExisting(true);
                        re.setRetrievedObject(lastCreatedFeatures);
                        re.setRequestIdentifier(System.currentTimeMillis());
                        fireRetrievalStarted(re);
                        fireRetrievalComplete(re);
                    }
                });
        } else {
            LOG.warn("no last created features that could be refreshed found"); // NOI18N
        }
    }

    @Override
    public Pair<Integer, Integer> getLegendSize(final int nr) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            return getLegendSize((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0));
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   style  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Pair<Integer, Integer> getLegendSize(final org.deegree.style.se.unevaluated.Style style) {
        return legends.getLegendSize(style);
    }

    @Override
    public Pair<Integer, Integer> getLegendSize() {
        return getLegendSize(0);
    }

    @Override
    public List<Pair<Integer, Integer>> getLegendSizes() {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        final List<Pair<Integer, Integer>> sizes = new LinkedList<Pair<Integer, Integer>>();
        for (final org.deegree.style.se.unevaluated.Style style : styles) {
            sizes.add(getLegendSize(style));
        }
        return sizes;
    }

    @Override
    public void getLegend(final int width, final int height, final Graphics2D g2d) {
        getLegend(0, width, height, g2d);
    }

    @Override
    public void getLegend(final int nr, final int width, final int height, final Graphics2D g2d) {
        if (featureFactory instanceof AbstractFeatureFactory) {
            final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
            getLegend((org.deegree.style.se.unevaluated.Style)aff.getStyle(aff.layerName).get(0),
                width,
                height,
                g2d);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  style   DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  g2d     DOCUMENT ME!
     */
    private void getLegend(final org.deegree.style.se.unevaluated.Style style,
            final int width,
            final int height,
            final Graphics2D g2d) {
        legends.paintLegend(style,
            width,
            height,
            g2d);
    }

    @Override
    public void getLegends(final List<Pair<Integer, Integer>> sizes, final Graphics2D[] g2d) {
        final AbstractFeatureFactory aff = ((AbstractFeatureFactory)featureFactory);
        final List<org.deegree.style.se.unevaluated.Style> styles = aff.getStyle(aff.layerName);
        for (int i = 0; i < styles.size(); i++) {
            legends.paintLegend(styles.get(i), sizes.get(i).first, sizes.get(i).second, g2d[i]);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshFeatures() {
        final List<FT> lastCreatedFeatures = this.featureFactory.getLastCreatedFeatures();
        if (lastCreatedFeatures.size() > 0) {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(lastCreatedFeatures.size()
                                + " last created features refreshed, fiering retrival event"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final RetrievalEvent re = new RetrievalEvent();
                        re.setIsComplete(true);
                        re.setHasErrors(false);
                        re.setRefreshExisting(true);
                        re.setRetrievedObject(lastCreatedFeatures);
                        re.setRequestIdentifier(System.currentTimeMillis());
                        fireRetrievalStarted(re);
                        fireRetrievalComplete(re);
                    }
                });
        } else {
            LOG.warn("no last created features that could be refreshed found"); // NOI18N
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Feature Retrieval Thread started by the {@code retrieve()} operation.
     *
     * @version  $Revision$, $Date$
     */
    protected class FeatureRetrievalWorker extends SwingWorker<List<FT>, FT> implements PropertyChangeListener {

        //~ Instance fields ----------------------------------------------------

        private final long id = System.nanoTime();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureRetrievalWorker object.
         */
        public FeatureRetrievalWorker() {
            this.addPropertyChangeListener(this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public long getId() {
            return this.id;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        @Override
        protected List<FT> doInBackground() throws Exception {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("FRW[" + this.getId() + "]: doInBackground() started"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final RetrievalEvent r = new RetrievalEvent();
                        r.setRequestIdentifier(getId());
                        r.setPercentageDone(-1);
                        fireRetrievalStarted(r);
                    }
                });

            // check if canceled .......................................................
            if (this.isCancelled()) {
                LOG.warn("FRW[" + this.getId() + "]: doInBackground() canceled"); // NOI18N
                return null;
            }
            // check if canceled .......................................................

            final List<FT> features = retrieveFeatures(this);
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("FRW[" + this.getId() + "]: doInBackground() completed"); // NOI18N
                }
            }
            return features;
        }

        /**
         * DOCUMENT ME!
         */
        @Override
        protected void done() {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("FRW[" + this.getId() + "]: done()"); // NOI18N
                }
            }
            // check if canceled .......................................................
            if (this.isCancelled()) {
                LOG.warn("FRW[" + this.getId() + "]:  canceled (done)"); // NOI18N
                final RetrievalEvent re = new RetrievalEvent();
                re.setRequestIdentifier(this.getId());
                re.setPercentageDone(0);
                re.setHasErrors(false);
                fireRetrievalAborted(re);
                return;
            }
            // check if canceled .......................................................

            try {
                List<FT> results = null;
                if (!this.isCancelled()) {
                    results = this.get();
                }

                if (results != null) {
                    if (DEBUG) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("FRW[" + this.getId() + "]: " + results.size() + " features created"); // NOI18N
                        }
                    }
                    AbstractFeatureService.this.setRefreshNeeded(false);
                    final RetrievalEvent re = new RetrievalEvent();
                    re.setRequestIdentifier(getId());
                    re.setIsComplete(true);
                    re.setHasErrors(false);
                    re.setRetrievedObject(results);
                    fireRetrievalComplete(re);
                } else {
                    LOG.warn("FRW[" + this.getId() + "]: FeatureRetrieverWorker brachte keine Ergebnisse (canceled="
                                + this.isCancelled() + ")");                                                 // NOI18N
                    // setErrorMessage("Feature Request brachte keine Ergebnisse");
                    final RetrievalEvent re = new RetrievalEvent();
                    re.setHasErrors(false);

                    re.setRequestIdentifier(getId());
                    if (this.isCancelled()) {
                        fireRetrievalAborted(re);
                    } else {
                        re.setRetrievedObject(new ArrayList<FT>());
                        fireRetrievalComplete(re);
                    }
                }
            } catch (final Exception e) {
                LOG.error("FRW[" + this.getId() + "]: Fehler im FeatureRetrieverWorker (done): \n" + e.getMessage(),
                    e); // NOI18N

                final RetrievalEvent re = new RetrievalEvent();
                re.setRequestIdentifier(this.getId());
                re.setPercentageDone(0);
                re.setHasErrors(true);
                re.setRetrievedObject(e.getMessage());
                fireRetrievalError(re);
            }
        }

        /**
         * Fires a RetrievalEvent on progress update.
         *
         * @param  evt  DOCUMENT ME!
         */
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("progress")) // NOI18N
            {
                final int progress = (Integer)evt.getNewValue();
                // AbstractFeatureService.this.setProgress(progress);
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("FRW[" + this.getId() + "]: FeatureRetrieverWorker progress: " + progress); // NOI18N
                    }
                }

                final RetrievalEvent re = new RetrievalEvent();
                re.setRequestIdentifier(this.getId());
                re.setIsComplete(progress != 100);
                re.setPercentageDone(progress);
                AbstractFeatureService.this.fireRetrievalProgress(re);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String toString() {
            return String.valueOf(this.getId());
        }
    }

    /**
     * Initialisiert den Layer.
     *
     * @version  $Revision$, $Date$
     */
    protected class LayerInitWorker extends SwingWorker<Void, Void> implements PropertyChangeListener {

        //~ Instance fields ----------------------------------------------------

        private final long id = System.nanoTime();
        // private final long id = System.currentTimeMillis();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LayerInitWorker object.
         */
        public LayerInitWorker() {
            this.addPropertyChangeListener(this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public long getId() {
            return this.id;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        @Override
        protected Void doInBackground() throws Exception {
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("LIW[" + this.getId() + "]: doInBackground() started"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final RetrievalEvent r = new RetrievalEvent();
                        r.setPercentageDone(-1);
                        r.setRequestIdentifier(getId());
                        r.setInitialisationEvent(true);
                        fireRetrievalStarted(r);
                    }
                });

            init();
            return null;
        }

        /**
         * DOCUMENT ME!
         */
        @Override
        protected void done() {
            AbstractFeatureService.this.setRefreshNeeded(false);
            if (DEBUG) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("LIW[" + this.getId() + "]: done()"); // NOI18N
                }
            }
            // check if canceled .......................................................
            if (isCancelled()) {
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("LIW[" + this.getId() + "]: canceled (done)"); // NOI18N
                    }
                }
                setInitialized(false);

                final RetrievalEvent re = new RetrievalEvent();
                re.setInitialisationEvent(true);
                re.setPercentageDone(0);
                re.setRequestIdentifier(this.getId());
                re.setHasErrors(false);
                fireRetrievalAborted(re);
                return;
            }
            // check if canceled .......................................................

            try {
                get();

                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("LIW[" + this.getId() + "]: finished"); // NOI18N
                    }
                }
                AbstractFeatureService.this.setRefreshNeeded(false);
                final RetrievalEvent re = new RetrievalEvent();
                re.setInitialisationEvent(true);
                re.setPercentageDone(100);
                re.setRequestIdentifier(getId());
                re.setIsComplete(true);
                re.setHasErrors(false);
                re.setRetrievedObject(null);
                fireRetrievalComplete(re);
            } catch (final Exception e) {
                LOG.error("LIW[" + this.getId() + "]: Fehler beim initalisieren des Layers: " + e.getMessage(), e); // NOI18N
                setInitialized(false);

                final RetrievalEvent re = new RetrievalEvent();
                re.setInitialisationEvent(true);
                re.setPercentageDone(0);
                re.setRequestIdentifier(this.getId());
                fireRetrievalStarted(re);
                re.setHasErrors(true);
                re.setRetrievedObject(e.getMessage());
                fireRetrievalError(re);
                return;
            }

            setInitialized(true);
            layerInitWorker = null;

            // start initial retrieval
            retrieve(false);
        }

        /**
         * Fires a RetrievalEvent on progress update.
         *
         * @param  evt  DOCUMENT ME!
         */
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("progress")) // NOI18N
            {
                final int progress = (Integer)evt.getNewValue();
                // AbstractFeatureService.this.setProgress(progress);
                if (DEBUG) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("LIW[" + this.getId() + "]: LayerInitWorker progress: " + progress); // NOI18N
                    }
                }

                final RetrievalEvent re = new RetrievalEvent();
                re.setInitialisationEvent(true);
                re.setRequestIdentifier(this.getId());
                re.setIsComplete(progress != 100);
                re.setPercentageDone(progress);
                AbstractFeatureService.this.fireRetrievalProgress(re);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String toString() {
            return String.valueOf(this.getId());
        }
    }
}
