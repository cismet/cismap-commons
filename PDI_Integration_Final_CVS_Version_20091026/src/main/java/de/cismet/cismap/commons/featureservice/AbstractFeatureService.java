/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.CachingFeatureFactory;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XMLObjectFactory;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.rasterservice.FeatureMapService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.tools.StaticXMLTools;
import edu.umd.cs.piccolo.PNode;
import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.SwingWorker;
import org.jdom.Element;

/**
 *
 * @author Pascal Dihé
 */
public abstract class AbstractFeatureService<FT extends FeatureServiceFeature, QT> extends AbstractRetrievalService implements MapService, ServiceLayer, RetrievalServiceLayer, FeatureMapService, ConvertableToXML, Cloneable
{
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
  /* the linecolor which will be used to draw the features of this layer */
  //private Color lineColor = Color.BLACK;
  /* the area color of the geometries */
  //private Color fillingColor = new Color(0.2f, 0.2f, 0.2f, 0.7f);
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
  /* Feature to render the Geometry */
  //private CloneableFeature renderingFeature;
  /* defaulttype-constant */
  public static final String DEFAULT_TYPE = "default";
  /* resulting featurearraylist of the retrievalWorker */
  //private final Vector<CloneableFeature> retrievedResults = new Vector<CloneableFeature>();
  /* SwingWorker that retrieves the features in the desired area */
  protected FeatureRetrievalWorker featureRetrievalWorker;
  /* part of the XML-configurationfile to customize a featureservice */
  //private Element layerConf = null;
  /* featurearray */
  //private Feature[] features = null;
  /* is the featurelayer already initialized or not */
  protected Boolean initialized = false;
  /* worker that retrieves to define the correct geometry */
  protected LayerInitWorker layerInitWorker = null;
  protected LayerProperties layerProperties = null;
  protected FeatureFactory featureFactory = null;

 /**
   * Creates a new <b>uninitilaised</b> instance of a feature service with layer
   * properties set. The Service is fully initialised upon the first call to the
   * {@code retrieve()} operation.
   *
   * @throws Exception
   */
  public AbstractFeatureService()
  {
    this.setLayerProperties(this.createLayerProperties());
  }

  /**
   * Creates a new AbstractFeatureService from a XML-element. Sets all properties
   * of the XML Element but does <b>not</b> initialise. Since the initialisation
   * may take some time, it is perfomed upon the first call to the
   * {@code retrieve()} operation which is rum from separate thread.
   *
   * @param e XML-element with FeatureService-configuration
   * @throws java.lang.Exception if something went wrong
   * @see isInitialised()
   */
  public AbstractFeatureService(Element e) throws Exception
  {
    //this();
    logger.info("creating new FeatureService instance from xml element '" + e.getName() + "'");
    if (e.getName().equals(this.getFeatureLayerType()))
    {
      this.initFromElement(e);
    } else if (e.getChild(this.getFeatureLayerType()) != null)
    {
      this.initFromElement(e.getChild(this.getFeatureLayerType()));
    } else
    {
      logger.error("FeatureService could not be initailised from xml: unsupported element '" + e.getName() + "'");
      throw new ClassNotFoundException("FeatureService could not be initailised from xml: unsupported element '" + e.getName() + "'");
    }

    if (this.getLayerProperties() == null)
    {
      logger.warn("LayerProperties not properly initialised from XML Element, creating new Properties upon next retrieval");
      this.layerProperties = this.createLayerProperties();
    }

    if (this.getFeatureServiceAttributes() == null || this.getFeatureServiceAttributes().size() == 0)
    {
      logger.warn("FeatureServiceAttributes not properly initialised from XML Element, creating new Attributes upon next retrieval");
    }
  }

  /**
   * Protected Constructor that clones (shallow) the delivered AbstractFeatureService.
   * Attributes, layer properties and feature factories are not cloned deeply.
   * The FeatureService to be cloned should be initilaised.
   *
   * @param afs FeatureService that should be cloned
   */
  protected AbstractFeatureService(AbstractFeatureService afs)
  {
    //this();
    // initilaisation updates also the cloned object!
    if (!afs.isInitialized())
    {
      logger.warn("creating copy of uninitialised feature service");
    }

    this.setLayerPosition(afs.getLayerPosition());
    this.setName(afs.getName());
    this.setEncoding(afs.getEncoding());
    this.setPNode(afs.getPNode() != null ? (PNode) afs.getPNode().clone() : null);
    this.setTranslucency(afs.getTranslucency());
    this.setEncoding(afs.getEncoding());
    this.setEnabled(afs.isEnabled());

    this.layerProperties = afs.getLayerProperties() != null ? afs.getLayerProperties().clone() : null;
    this.featureFactory = afs.getFeatureFactory() != null ? afs.getFeatureFactory().clone() : null;
    
    TreeMap<String, FeatureServiceAttribute> attriuteMap = new TreeMap();
    attriuteMap.putAll(afs.getFeatureServiceAttributes());       
    this.setFeatureServiceAttributes(attriuteMap);

    this.setInitialized(afs.isInitialized());
  }

  /**
   * Create a new <b>uninitialised</b> AbstractFeatureService except for the
   * name and featureServiceAttributes.
   *
   * @param name the name of this FeatureService
   * @param featureServiceAttributes vector with all FeatureServiceAttributes of the FeatureService
   */
  public AbstractFeatureService(String name, Vector<FeatureServiceAttribute> attributes)
  {
    //this();
    this.setName(name);
    this.setFeatureServiceAttributes(attributes);
  }

  /**
   * Initialises the FeatureService instance. If the service has already been
   * initialised, the operation clears the layerProperties, the featureFactory
   * and the featureServiceAttributes and forces a complete re-initialisation.
   *
   * @throws java.lang.Exception if somethin went wrong
   */
  protected void init() throws Exception
  {
    // *should* never happen ....
    if (layerInitWorker == null || layerInitWorker.isDone())
    {
      logger.error("strange synchronisation problem in Layer Initialisation Thread");
      throw new Exception("strange synchronisation problem in Layer Initialisation Thread");
    }

    // check if canceled .......................................................
    if (layerInitWorker.isCancelled())
    {
      logger.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled");
      return;
    }
    //..........................................................................

    if (this.isInitialized() || this.isRefreshNeeded())
    {
      logger.warn("layer already initialised, forcing complete re-initialisation");
      this.setInitialized(false);
      //this.layerProperties = null;
      //this.featureFactory = null;
      //this.featureServiceAttributes = null;
    }

    if (this.getLayerProperties() == null)
    {
      if(DEBUG)logger.debug("init(): LayerProperties not yet set, creating new LayerProperties instance");
      this.layerProperties = this.createLayerProperties();
    } else
    {
      if(DEBUG)logger.debug("init(): Layer Properties already created");
    }

    // check if canceled .......................................................
    if (layerInitWorker.isCancelled())
    {
      logger.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled");
      return;
    }
    //..........................................................................

    if (this.featureFactory == null)
    {
      if(DEBUG)logger.debug("init(): Feature Factory not yet set, creating new Feature Factory instance");
      // create the feature Factory
      // all variables required by the concrete FeatureFactory constructor must
      // have been initialised!
      this.featureFactory = this.createFeatureFactory();
    } else
    {
      if(DEBUG)logger.debug("init(): Feature Factory already created");
    }

    // set common properties of the factory
    // implemntation specific properties must be set in the createFeatureFactory()
    this.featureFactory.setMaxFeatureCount(this.getMaxFeatureCount());
    this.featureFactory.setLayerProperties(layerProperties);
    

    // check if canceled .......................................................
    if (layerInitWorker.isCancelled())
    {
      logger.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled");
      return;
    }
    //..........................................................................

    if (this.getFeatureServiceAttributes() == null || this.getFeatureServiceAttributes().size() == 0)
    {
      if(DEBUG)logger.debug("init(): Feature Service Attributes not yet set, creating new  Feature Service Attribute");
      try
      {
        this.setFeatureServiceAttributes(featureFactory.createAttributes(layerInitWorker));
      } catch (UnsupportedOperationException uoe)
      {
        if(DEBUG)logger.debug("Feature Factory '" + this.getFeatureFactory().getClass().getSimpleName() + "' does not support Attributes");
        if (this.getFeatureServiceAttributes() == null)
        {
          this.setFeatureServiceAttributes(new Vector());
        }
      }
    } else
    {
      if(DEBUG)logger.debug("init(): Feature Service Attributes already created");
    }

    // check if canceled .......................................................
    if (layerInitWorker.isCancelled())
    {
      logger.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled");
      return;
    }
    //..........................................................................


    //idExpression plausibility check
    if (this.getLayerProperties().getIdExpressionType() == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME && this.getLayerProperties().getIdExpression() != null && this.getFeatureServiceAttributes() != null && this.getFeatureServiceAttributes().size() > 0)
    {
      if(DEBUG)logger.debug("checking if property id expression '" + this.getLayerProperties().getIdExpression() + "' is valid");
      boolean found = false;
      for (FeatureServiceAttribute attribute : this.getFeatureServiceAttributes().values())
      {
        if(DEBUG)logger.debug("checking attribute '" + attribute.getName() + "'");
        found = attribute.getName().equals(this.getLayerProperties().getIdExpression());
        if (found)
        {
          if(DEBUG)logger.debug("attribute is valid: " + attribute.getName());
          break;
        }
      }

      if (!found)
      {
        logger.warn("property id expression '" + this.getLayerProperties().getIdExpression() + "' not found in attributes, resetting to undefined");
        this.getLayerProperties().setIdExpression(null, LayerProperties.EXPRESSIONTYPE_UNDEFINED);
      }
    }

    // check if canceled .......................................................
    if (layerInitWorker.isCancelled())
    {
      logger.warn("LIW[" + layerInitWorker.getId() + "]: init is canceled");
      return;
    }
    //..........................................................................


    if(DEBUG)logger.debug("init(): performing additional implementation specific initialisation");
    this.initConcreteInstance();

    //initilaized = true is set in the layerInitWorker
  }

  /**
   * Creates a CloneableFeature with defaultvalues.
   * @return CloneableFeature
   */
//  private CloneableFeature createDefaultStyleFeature()
//  {
//    DefaultFeatureServiceFeature designer = new DefaultFeatureServiceFeature();
//    designer.setCanBeSelected(false);
//    Color defColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
//    designer.setFillingPaint(defColor);
//    designer.setLinePaint(defColor.darker());
//    designer.setTransparency(1.0f);
//    designer.setPrimaryAnnotation("");
//    designer.setPrimaryAnnotationScaling(1d);
//    designer.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, 12));
//    FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol(true, true, 10, 1, defColor, defColor.darker()));
//    fas.setSweetSpotX(0.5d);
//    fas.setSweetSpotY(0.5d);
//    designer.setPointAnnotationSymbol(fas);
//    designer.setLineWidth(1);
//    designer.setMaxScaleDenominator(2500);
//    designer.setMinScaleDenominator(0);
//    designer.setAutoScale(true);
//    designer.setHighlightingEnabled(false);
//    designer.setIdExpression(this.getDefaultIdExpression());
//    return designer;
//  }
  /**
   *  Determines which id expression is applicable to the features served by
   *  the implementing FeatureService. E.g. a WFS FeatureService may return
   *  "app:gid".
   *
   * @return the default id expression applied to the features served by the FeatureService
   */
  //protected abstract String getDefaultIdExpression();
  /**
   * Refreshs all features by replacing their style with the one of the RenderingFeature.
   */
  /*public void refreshFeatures()
  {
  ArrayList<CloneableFeature> newRetrievedResults = new ArrayList<CloneableFeature>();
  for (CloneableFeature cf : getRetrievedResults())
  {
  CloneableFeature newCf = (CloneableFeature) getRenderingFeature().clone();
  newCf.setGeometry(cf.getGeometry());

  if (cf instanceof PropertyContainer && newCf instanceof PropertyContainer)
  {
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
  }*/
  /**
   * Creates a vector with FeatureServiceAttribute of all retrieved features.
   *
   * @param ctm the current time im milliseconds
   * @param currentWorker FeatureRetrievalWorker
   * @return vector with all found FeatureServiceAttributes
   */
//  protected void createFeatureAttributes()
//  {
//    Vector<FeatureServiceAttribute> tmp = new Vector<FeatureServiceAttribute>();
//    try
//    {
//      Feature[] fc = getFeatures();
//      if (fc != null && fc.length > 0)
//      {
//        for (int i = 0; i < fc.length; i++)
//        {
//          FeatureProperty[] featureProperties = fc[i].getProperties();
//          for (FeatureProperty fp : featureProperties)
//          {
//            try
//            {
//              FeatureType type = fc[i].getFeatureType();
//              for (PropertyType pt : type.getProperties())
//              {
//                //logger.fatal("Property Name=" + pt.getName() + " PropertyType=" + pt.getType());
//                //ToDo was ist wenn zwei Geometrien dabei sind
//                FeatureServiceAttribute fsa = new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true);
//                if (!tmp.contains(fsa))
//                {
//                  tmp.add(fsa);
//                }
//              }
//            } catch (Exception ex)
//            {
//              logger.warn("Fehler beim Anlegen eines FeatureServiceAttribute");
//            }
//          }
//        }
//        setFeatureServiceAttributes(tmp);
//      } else
//      {
//        setFeatureServiceAttributes(tmp);
//      }
//    } catch (Exception ex)
//    {
//      logger.error("Fehler beim Anlegen der FeatureServiceAttribute", ex);
//      setFeatureServiceAttributes(tmp);
//    }
//  }
//
  /**
   * Creates an instance of a service specific LayerProperties implementation
   *
   * @return layer properties to be used
   */
  protected abstract LayerProperties createLayerProperties();

  /**
   * Creates an instance of a service specific FeatureFactory implementation.
   * All variables required by the concrete FeatureFactory, e.g. the Layer 
   * Properties must have been initialised before this operation is invoked.
   *
   * @return the constructed FeatureFactory
   * @throws Exception id the costruction failed
   */
  protected abstract FeatureFactory createFeatureFactory() throws Exception;

  /**
   * Get the value of featureFactory
   *
   * @return the value of featureFactory
   */
  public FeatureFactory getFeatureFactory()
  {
    return featureFactory;
  }

  /**
   * Get the value of query
   *
   * @return the value of query
   */
  public abstract QT getQuery();

  /**
   * Set the value of query
   *
   * @param query new value of query
   */
  public abstract void setQuery(QT query);

  /**
   * Cancels the retrievel or the initialisation threads.
   *
   * @return {@code true} if a running thread was found and canceled
   */
  protected boolean cancel(SwingWorker workerThread)
  {
    boolean canceled = false;

//    if (this.layerInitWorker != null && !this.layerInitWorker.isDone())
//    {
//      if(this.layerInitWorker != null)logger.warn("canceling LayerInitWorker: " + layerInitWorker.getId());
//      if(this.layerInitWorker != null)layerInitWorker.cancel(true);
//      layerInitWorker = null;
//      canceled = true;
//    }

    if (workerThread != null && (!workerThread.isDone() || !workerThread.isCancelled()))
    {
      if (workerThread != null)
      {
        logger.warn("canceling Worker Thread: " + workerThread);
      }
      if (workerThread != null)
      {
        boolean cancel = workerThread.cancel(true);
        if(DEBUG)logger.debug("Worker Thread: " + workerThread + " canceled: " + cancel + " (" + workerThread.isCancelled() + ")");
      }
      canceled = true;
    }

    return canceled;
  }

  /**
   * Creates a new LayerInitWorker or FeatureRetrievalWorker and launches the
   * initialisation or retrieval-process depending wheter the layer has already
   * been initialised or the forced parameter is set to true. A re-initialisation
   * clears the attribute and layer properties cache and thus resets any saved
   * properties to default values.
   *
   * @param forced forces a re-initialisation of the layer
   */
  @Override
  public synchronized void retrieve(boolean forced)
  {
    if(DEBUG)logger.debug("retrieve started (forced = " + forced + ")");
    if (featureRetrievalWorker != null && !featureRetrievalWorker.isDone())
    {
      if(DEBUG)logger.debug("old retrieval thread still running, trying to cancel '" + featureRetrievalWorker.getId() + "' (already canceled = " + featureRetrievalWorker.isCancelled() + ")");
      this.cancel(featureRetrievalWorker);
    }

    if(!this.isEnabled() && !this.isVisible())
    {
      logger.warn("Service '" + this.getName() + "' is disabled and invisible, ignoring retrieve() request");
      return;
    }

    // Initialisierung bereits vorgenommen, d.h. es gibt z.B. Feature Service Attribute
    if (this.isInitialized() && !this.isRefreshNeeded()/*&& !forced*/)
    {
      if(DEBUG)logger.debug("Layer already initialized, starting feature retrieval");
      if (forced && getFeatureFactory() instanceof CachingFeatureFactory)
      {
        if(DEBUG)logger.debug("retrieval forced, flushing cache");
        if (featureRetrievalWorker != null && !featureRetrievalWorker.isDone())
        {
          logger.warn("must wait until thread '" + featureRetrievalWorker + "' is finished before flushing cache");
          while (!featureRetrievalWorker.isDone())
          {
          }
          if(DEBUG)logger.debug("thread '" + featureRetrievalWorker + "'is finished, flushing cache");
        }
        ((CachingFeatureFactory) getFeatureFactory()).flush();
      }

      this.featureRetrievalWorker = new FeatureRetrievalWorker();
      featureRetrievalWorker.execute();

    } else
    {
      if(DEBUG)logger.debug("Layer not yet initialized ("+this.initialized+") or refresh needed ("+this.isRefreshNeeded()+"), starting LayerInitWorker");
      if (layerInitWorker == null)
      {
        layerInitWorker = new LayerInitWorker();
        layerInitWorker.execute();
      } else
      {
        logger.warn("Layer wird z.Z. initialisiert --> request wird ignoriert");
      }
    }
  }

// <editor-fold defaultstate="collapsed" desc="Abstract Methods">
//abstract protected Vector<FeatureServiceAttribute> createFeatureAttributes(final long ctm,final FeatureRetrievalWorker currentWorker) throws Exception;
  /**
   * This operation is invoked after the default initialisation. Implementation
   * classes may implement this merzhod to perform addditional initialisations.
   *
   * @throws Exception if the initialisation fails
   */
  abstract protected void initConcreteInstance() throws Exception;

//  abstract protected Feature[] retrieveFeatures(final long ctm, final FeatureRetrievalWorker currentWorker)
//          throws Exception;
//
//  protected Feature[] retrieveFeatures() throws Exception
//  {
//    return retrieveFeatures(System.currentTimeMillis(), null);
//  }
  /**
   *
   * @return
   */
  abstract protected String getFeatureLayerType();

  //abstract protected void addConcreteElement(Element e);
  /**
   *
   * @param type
   * @return
   */
  abstract public Icon getLayerIcon(int type);

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="Setters & Getters">
  /**
   * Packs the properties of the AbstractFeatureService as JDom-element.
   * @return JDom-element that outlines this AbstractFeatureService
   */
  public boolean isInitialized()
  {
    return initialized;
  }

  protected void setInitialized(boolean isInitialized)
  {
    this.initialized = isInitialized;
  }

//  public Element getLayerConf()
//  {
//    return layerConf;
//  }
//
//  public void setLayerConf(Element layerConf)
//  {
//    this.layerConf = layerConf;
//  }
//  @Deprecated
//  public Feature[] getFeatures()
//  {
//    return features;
//  }
//
//  @Deprecated
//  public void setFeatures(Feature[] features)
//  {
//    this.features = features;
//  }
  /**
   * Returns a list of all featureServiceAttributes of this featureservice
   */
  public Map<String, FeatureServiceAttribute> getFeatureServiceAttributes()
  {
    return this.featureServiceAttributes;
  }

  /**
   * Setter for the featureServiceAttributes of the featureservice.
   * @param featureServiceAttributes featureServiceAttributes to set
   */
  public void setFeatureServiceAttributes(Map<String, FeatureServiceAttribute> featureServiceAttributes)
  {
    this.featureServiceAttributes = featureServiceAttributes;
  }

  /**
   * Setter for the featureServiceAttributes of the featureservice.
   * @param featureServiceAttributesVector featureServiceAttributes to set
   */
  protected void setFeatureServiceAttributes(Vector<FeatureServiceAttribute> featureServiceAttributesVector)
  {
    if (featureServiceAttributesVector != null)
    {
      if (this.featureServiceAttributes == null)
      {
        this.featureServiceAttributes = new HashMap(featureServiceAttributesVector.size());
      } else
      {
        this.featureServiceAttributes.clear();
      }

      for (FeatureServiceAttribute fsa : featureServiceAttributesVector)
      {
        this.featureServiceAttributes.put(fsa.getName(), fsa);
      }
    }
  }

  /**
   * This Method is used to set the bounding box to determine which features should
   * be retrieved
   * @param bb the bounding box that indicates the area of interest
   */
  public BoundingBox getBoundingBox()
  {
    return bb;
  }

  @Override
  public void setBoundingBox(BoundingBox bb)
  {
    this.bb = bb;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

// public Color getFillingColor()
//  {
//    return fillingColor;
//  }
//
//  public void setFillingColor(Color fillingColor)
//  {
//    this.fillingColor = fillingColor;
//  }
//    public AbstractFeatureRetrievalWorker getFrw() {
//        return featureRetrievalWorker;
//    }
//
//    public void setFrw(AbstractFeatureRetrievalWorker featureRetrievalWorker) {
//        this.featureRetrievalWorker = featureRetrievalWorker;
//    }
//  public Color getLineColor()
//  {
//    return lineColor;
//  }
//
//  public void setLineColor(Color lineColor)
//  {
//    this.lineColor = lineColor;
//  }
  public int getMaxFeatureCount()
  {
    return maxFeatureCount;
  }

  public void setMaxFeatureCount(int maxFeatureCount)
  {
    this.maxFeatureCount = maxFeatureCount;
    if (this.getFeatureFactory() != null)
    {
      this.getFeatureFactory().setMaxFeatureCount(maxFeatureCount);
    }
  }

//  public CloneableFeature getRenderingFeature()
//  {
//    return renderingFeature;
//  }
//  public void setRenderingFeature(CloneableFeature renderingFeature)
//  {
//    this.renderingFeature = renderingFeature;
//  }
//  public ArrayList<CloneableFeature> getRetrievedResults()
//  {
//    return retrievedResults;
//  }
  public LayerProperties getLayerProperties()
  {
    return layerProperties;
  }

  /**
   * Sets th enew layer properties of the service and
   * @param layerProperties
   */
  public void setLayerProperties(LayerProperties layerProperties)
  {
    this.layerProperties = layerProperties;

    if (this.featureFactory != null)
    {
      if(DEBUG)logger.debug("setLayerProperties: new layer properties are also applied to all cached features!");
      // layer properties are appiled to last created features
      if (featureRetrievalWorker != null && !featureRetrievalWorker.isDone())
      {
        logger.warn("must wait until thread '" + featureRetrievalWorker + "' is finished before applying new layer properties");
        while (!featureRetrievalWorker.isDone())
        {
          // wait ....
        }
        if(DEBUG)logger.debug("thread '" + featureRetrievalWorker + "' is finished, applying new layer properties");
      }

      this.featureFactory.setLayerProperties(layerProperties);
      final Vector<FT> lastCreatedFeatures = this.featureFactory.getLastCreatedFeatures();
      if (lastCreatedFeatures.size() > 0)
      {
        if(DEBUG)logger.debug(lastCreatedFeatures.size() + " last created features refreshed, fiering retrival event");
        EventQueue.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(true);
            re.setHasErrors(false);
            re.setRefreshExisting(true);
            re.setRetrievedObject(lastCreatedFeatures);
            re.setRequestIdentifier(System.currentTimeMillis());
            fireRetrievalStarted(re);
            fireRetrievalComplete(re);
          }
        });
      } else
      {
        logger.warn("no last created features that could be refreshed found");
      }
    }
  }

  /**
   * Deliveres the transparency value of the Featues
   * @return the translucency value
   */
  @Override
  public float getTranslucency()
  {
    return translucency;
  }

  /**
   * Setter for the transparency value
   * @param t the new transparency value
   */
  @Override
  public void setTranslucency(float t)
  {
    this.translucency = t;
  }

  /**
   * Setter for the name of the AbstractFeatureService
   * @param name the new name that will be set
   */
  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * This method delivers the name of the layer
   * @return the name of the layer
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * This method delivers the postion of the layer in the layer hierachy
   * @return the postion of the layer in the layer hierarchy
   */
  @Override
  public int getLayerPosition()
  {
    return layerPosition;
  }

  /**
   * Sets the layer postion. Dependet on this value the layer will be positioned at
   * top of other layers or behind other layers
   * @param layerPosition The integer value which determines the postion in the layer hierarchy
   */
  @Override
  public void setLayerPosition(int layerPosition)
  {
    this.layerPosition = layerPosition;
  }

  /**
   * Returns if the layer is enabled or disabled
   * @return either true if the layer is enabled or false if its not
   */
  @Override
  public boolean isEnabled()
  {
    return enabled;
  }

  /**
   * Enables or disables the Layer
   * @param enabled true enables the layer, false disables it
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    if(!enabled)
    {
      if(!this.canBeDisabled())
      {
        logger.warn("Service '" + this.getName() + "' cannot be disabled");
      }
      else
      {
        this.enabled = false;
        //
      }
    }
    else
    {
      this.enabled = true;
    }
  }

  /**
   * This method checks either a layer can be disabled or not
   * @return true if the layer can be disabled or false if not
   */
  @Override
  public boolean canBeDisabled()
  {
    return true;
  }

  @Override
  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  @Override
  public void setSize(int height, int width)
  {
  }

  @Override
  public PNode getPNode()
  {
    return pNode;
  }

  @Override
  public void setPNode(PNode pNode)
  {
    this.pNode = pNode;
  }

  @Override
  public String toString()
  {
    return this.getName();
  }

  @Override
  public Element toElement()
  {
    Element element = new Element(getFeatureLayerType());
    element.setAttribute("name", getName());
    element.setAttribute("type", this.getClass().getCanonicalName());
    element.setAttribute("visible", new Boolean(getPNode().getVisible()).toString());
    element.setAttribute("enabled", new Boolean(isEnabled()).toString());
    element.setAttribute("translucency", new Float(getTranslucency()).toString());
    element.setAttribute("maxFeatureCount", new Integer(this.getMaxFeatureCount()).toString());
    element.setAttribute("layerPosition", new Integer(this.getLayerPosition()).toString());

    if (this.getFeatureServiceAttributes() != null && this.getFeatureServiceAttributes().size() > 0)
    {
      Element attrib = new Element("Attributes");
      for (FeatureServiceAttribute e : getFeatureServiceAttributes().values())
      {
        attrib.addContent(e.toElement());
      }
      element.addContent(attrib);
    }
    else
    {
      logger.warn("FeatureServiceAttributes are null and will not be saved");
    }

    if (this.getLayerProperties() != null)
    {
      Element layerPropertiesElement = this.getLayerProperties().toElement();
      element.addContent(layerPropertiesElement);
    }
    else
    {
      logger.warn("Layer Properties are null and will not be saved");
    }

    return element;
  }

  @Override
  public void initFromElement(Element element) throws Exception
  {
    if (element.getAttributeValue("name") != null)
    {
      this.setName(element.getAttributeValue("name"));
    }
    if (element.getAttributeValue("visible") != null)
    {
      this.setVisible(new Boolean(element.getAttributeValue("visible")));
    }
    if (element.getAttributeValue("enabled") != null)
    {
      this.setEnabled(new Boolean(element.getAttributeValue("enabled")));
    }
    if (element.getAttributeValue("translucency") != null)
    {
      this.setTranslucency(element.getAttribute("translucency").getFloatValue());
    }
    if (element.getAttributeValue("maxFeatureCount") != null)
    {
      this.setMaxFeatureCount(element.getAttribute("maxFeatureCount").getIntValue());
    }
    if (element.getAttributeValue("layerPosition") != null)
    {
      this.setLayerPosition(element.getAttribute("layerPosition").getIntValue());
    }
    if (element.getAttributeValue("maxFeatureCount") != null)
    {
      element.setAttribute("maxFeatureCount", new Integer(this.getMaxFeatureCount()).toString());
    }
    if (element.getAttributeValue("layerPosition") != null)
    {
      element.setAttribute("layerPosition", new Integer(this.getLayerPosition()).toString());
    }

    Element xmlAttributes = element.getChild("Attributes");
    if (xmlAttributes != null)
    {
      featureServiceAttributes = FeatureServiceUtilities.getFeatureServiceAttributes(xmlAttributes);
      this.setFeatureServiceAttributes(featureServiceAttributes);
    }

    if (element.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) == null)
    {
      logger.warn("fromElement: restoring object from deprecated xml element");
      try
      {
               this.fromOldElement(element);
      }
      catch(Throwable t)
      {
        logger.warn("could not restore deprecated configuration: \n"+t.getMessage(),t);
      }
    } else if (element.getChild("LayerProperties") != null)
    {
      LayerProperties restoredLayerProperties = null;
      try
      {
        Element layerPropertiesElement = (Element)element.getChild(LayerProperties.LAYER_PROPERTIES_ELEMENT);
        restoredLayerProperties = (LayerProperties) XMLObjectFactory.restoreObjectfromElement(layerPropertiesElement);
      } catch (Throwable t)
      {
        logger.error("could not restore generic style element '" +
                element.getChild("LayerProperties").getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) + "': \n" + t.getMessage(), t);
      }
      this.layerProperties = restoredLayerProperties;
    }
    else
  {
    logger.warn("no layer properties ");
  }
  }


  /**
   * @param element old XML Configuration
   * @deprecated
   */
  @Deprecated
  private void fromOldElement(Element element)
  {
    //setName(element.getAttributeValue("name"));
    //setVisible(new Boolean(element.getAttributeValue("visible")));
    //setEnabled(new Boolean(element.getAttributeValue("enabled")));
    //setTranslucency(element.getAttribute("translucency").getFloatValue());

    // Element xmlAttributes = element.getChild("Attributes");
    //featureServiceAttributes = FeatureServiceUtilities.getFeatureServiceAttributes(xmlAttributes);
    //setFeatureServiceAttributes(featureServiceAttributes);

    DefaultFeatureServiceFeature wfsFeature = new DefaultFeatureServiceFeature();
    Element renderingFeature = element.getChild("renderingFeature").getChild("DefaultWFSFeature");
    //f.setId(Integer.parseInt(renderingFeature.getAttributeValue("id")));
    wfsFeature.setIdExpression(renderingFeature.getAttributeValue("idExpression"));
    int lineWidth = Integer.parseInt(renderingFeature.getAttributeValue("lineWidth"));
    wfsFeature.setLineWidth(lineWidth);
    wfsFeature.setTransparency(Float.parseFloat(renderingFeature.getAttributeValue("transparency")));
    wfsFeature.setPrimaryAnnotation(renderingFeature.getAttributeValue("primaryAnnotation"));
    wfsFeature.setSecondaryAnnotation(renderingFeature.getAttributeValue("secondaryAnnotation"));
    wfsFeature.setPrimaryAnnotationScaling(Double.parseDouble(renderingFeature.getAttributeValue("primaryAnnotationScaling")));
    wfsFeature.setPrimaryAnnotationJustification(Float.parseFloat(renderingFeature.getAttributeValue("primaryAnnotationJustification")));
    wfsFeature.setMaxScaleDenominator(Integer.parseInt(renderingFeature.getAttributeValue("maxScaleDenominator")));
    wfsFeature.setMinScaleDenominator(Integer.parseInt(renderingFeature.getAttributeValue("minScaleDenominator")));
    wfsFeature.setAutoScale(Boolean.parseBoolean(renderingFeature.getAttributeValue("autoscale")));

    // color kann null sein (fill disabled oder line disabled)
    Color fill = null;
    Color line = null;

    if (renderingFeature.getChild("fillingColor") != null)
    {
      fill = StaticXMLTools.convertXMLElementToColor(renderingFeature.getChild("fillingColor").getChild("Color"));
    }
    wfsFeature.setFillingPaint(fill);

    if (renderingFeature.getChild("lineColor") != null)
    {
      line = StaticXMLTools.convertXMLElementToColor(renderingFeature.getChild("lineColor").getChild("Color"));
    }
    wfsFeature.setLinePaint(line);

    wfsFeature.setPrimaryAnnotationFont(StaticXMLTools.convertXMLElementToFont(renderingFeature.getChild("primaryAnnotationFont").getChild("Font")));
    wfsFeature.setPrimaryAnnotationPaint(StaticXMLTools.convertXMLElementToColor(renderingFeature.getChild("primaryAnnotationColor").getChild("Color")));
    wfsFeature.setHighlightingEnabled(Boolean.parseBoolean(renderingFeature.getAttributeValue("highlightingEnabled")));
    wfsFeature.getLayerProperties().getStyle().setPointSymbolFilename(Style.AUTO_POINTSYMBOL);

    //FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol((line != null), (fill != null), 10, lineWidth, fill, line));
    //fas.setSweetSpotX(0.5d);
    //fas.setSweetSpotY(0.5d);
    //wfsFeature.setPointAnnotationSymbol(fas);

    this.layerProperties = wfsFeature.getLayerProperties();
  }

  /**
   * This operation class the {@code createFeatures()} operation of the current
   * FeatureFactory. Implementation classes may override this method to pass
   * additional parameters to the {@code createFeatures()} operation of the
   * specific FeatureFactory implementation.
   *
   * @param worker the current worker thred that is observed
   * @return the FeatureServiceFeatures created by the current Factory
   */
  protected Vector<FT> retrieveFeatures(final FeatureRetrievalWorker worker) throws Exception
  {
    if(DEBUG)logger.debug("FRW[" + worker.getId() + "]: retrieveFeatures started");
    // check if canceled .......................................................
    if (worker.isCancelled())
    {
      logger.warn("FRW[" + worker.getId() + "]: retrieveFeatures is canceled");
      return null;
    }
    // check if canceled .......................................................

    long start = System.currentTimeMillis();
    final Vector<FT> features = getFeatureFactory().createFeatures(this.getQuery(), this.getBoundingBox(), worker);
    if (features != null)
    {
      logger.info("FRW[" + worker.getId() + "]: " + features.size() + " features retrieved in " + (System.currentTimeMillis() - start) + " ms");
    } else
    {
      logger.warn("FRW[" + worker.getId() + "]: no features found (canceled=" + worker.isCancelled() + ")");
    }

    if(DEBUG)logger.debug("FRW[" + worker.getId() + "]: retrieveFeatures completed");
    return features;
  }

  /**
   * Feature Retrieval Thread started by the {@code retrieve()} operation
   */
  protected class FeatureRetrievalWorker extends SwingWorker<Vector<FT>, FT> implements PropertyChangeListener
  {
    private final long id = System.nanoTime();
    //private final long id = System.currentTimeMillis();

    public long getId()
    {
      return this.id;
    }

    public FeatureRetrievalWorker()
    {
      this.addPropertyChangeListener(this);
    }

    @Override
    protected Vector<FT> doInBackground() throws Exception
    {
      if(DEBUG)logger.debug("FRW[" + this.getId() + "]: doInBackground() started");
      EventQueue.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          RetrievalEvent r = new RetrievalEvent();
          r.setRequestIdentifier(getId());
          r.setPercentageDone(-1);
          fireRetrievalStarted(r);
        }
      });

      // check if canceled .......................................................
      if (this.isCancelled())
      {
        logger.warn("FRW[" + this.getId() + "]: doInBackground() canceled");
        return null;
      }
      // check if canceled .......................................................

      Vector<FT> features = retrieveFeatures(this);
      if(DEBUG)logger.debug("FRW[" + this.getId() + "]: doInBackground() completed");
      return features;
    }

    @Override
    protected void done()
    {
      if(DEBUG)logger.debug("FRW[" + this.getId() + "]: done()");
      // check if canceled .......................................................
      if (this.isCancelled())
      {
        logger.warn("FRW[" + this.getId() + "]:  canceled (done)");
        RetrievalEvent re = new RetrievalEvent();
        re.setRequestIdentifier(this.getId());
        re.setPercentageDone(0);
        re.setHasErrors(false);
        fireRetrievalAborted(re);
        return;
      }
      // check if canceled .......................................................

      try
      {
        Vector<FT> results = null;
        if (!this.isCancelled())
        {
          results = this.get();
        }

        if (results != null)
        {
          if(DEBUG)logger.debug("FRW[" + this.getId() + "]: " + results.size() + " features created");
          AbstractFeatureService.this.setRefreshNeeded(false);
          RetrievalEvent re = new RetrievalEvent();
          re.setRequestIdentifier(getId());
          re.setIsComplete(true);
          re.setHasErrors(false);
          re.setRetrievedObject(results);
          fireRetrievalComplete(re);
        } else
        {
          logger.warn("FRW[" + this.getId() + "]: FeatureRetrieverWorker brachte keine Ergebnisse (canceled=" + this.isCancelled() + ")");
          //setErrorMessage("Feature Request brachte keine Ergebnisse");
          RetrievalEvent re = new RetrievalEvent();
          re.setHasErrors(false);

          re.setRequestIdentifier(getId());
          if (this.isCancelled())
          {
            fireRetrievalAborted(re);
          } else
          {
            re.setRetrievedObject(new Vector<FT>());
            fireRetrievalComplete(re);
          }
        }
      } catch (Throwable t)
      {
        logger.error("FRW[" + this.getId() + "]: Fehler im FeatureRetrieverWorker (done): \n" + t.getMessage(), t);

        RetrievalEvent re = new RetrievalEvent();
        re.setRequestIdentifier(this.getId());
        re.setPercentageDone(0);
        re.setHasErrors(true);
        re.setRetrievedObject(t.getMessage());
        fireRetrievalError(re);
      }
    }

    /**
     * Fires a RetrievalEvent on progress update.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getPropertyName().equals("progress"))
      {
        int progress = (Integer) evt.getNewValue();
        //AbstractFeatureService.this.setProgress(progress);
        if(DEBUG)logger.debug("FRW[" + this.getId() + "]: FeatureRetrieverWorker progress: "+progress);

        RetrievalEvent re = new RetrievalEvent();
        re.setRequestIdentifier(this.getId());
        re.setIsComplete(progress != 100);
        re.setPercentageDone(progress);
        AbstractFeatureService.this.fireRetrievalProgress(re);
      }
    }

    @Override
    public String toString()
    {
      return String.valueOf(this.getId());
    }
  }

  /**
   * Initialisiert den Layer
   */
  protected class LayerInitWorker extends SwingWorker<Void, Void> implements PropertyChangeListener
  {
    private final long id = System.nanoTime();
    //private final long id = System.currentTimeMillis();

    public LayerInitWorker()
    {
      this.addPropertyChangeListener(this);
    }

    public long getId()
    {
      return this.id;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
      if(DEBUG)logger.debug("LIW[" + this.getId() + "]: doInBackground() started");
      EventQueue.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          RetrievalEvent r = new RetrievalEvent();
          r.setPercentageDone(-1);
          r.setRequestIdentifier(getId());
          r.setInitialisationEvent(true);
          fireRetrievalStarted(r);
        }
      });

      init();
      return null;
    }

    @Override
    protected void done()
    {
      AbstractFeatureService.this.setRefreshNeeded(false);
      if(DEBUG)logger.debug("LIW[" + this.getId() + "]: done()");
      // check if canceled .......................................................
      if (isCancelled())
      {
        if(DEBUG)logger.debug("LIW[" + this.getId() + "]: canceled (done)");
        setInitialized(false);

        RetrievalEvent re = new RetrievalEvent();
        re.setInitialisationEvent(true);
        re.setPercentageDone(0);
        re.setRequestIdentifier(this.getId());
        re.setHasErrors(false);
        fireRetrievalAborted(re);
        return;
      }
      // check if canceled .......................................................

      try
      {
        get();

        if(DEBUG)logger.debug("LIW[" + this.getId() + "]: finished");
        AbstractFeatureService.this.setRefreshNeeded(false);
        RetrievalEvent re = new RetrievalEvent();
        re.setInitialisationEvent(true);
        re.setPercentageDone(100);
        re.setRequestIdentifier(getId());
        re.setIsComplete(true);
        re.setHasErrors(false);
        re.setRetrievedObject(null);
        fireRetrievalComplete(re);

      } catch (Throwable t)
      {
        logger.error("LIW[" + this.getId() + "]: Fehler beim initalisieren des Layers: "+t.getMessage(), t);
        setInitialized(false);

        RetrievalEvent re = new RetrievalEvent();
        re.setInitialisationEvent(true);
        re.setPercentageDone(0);
        re.setRequestIdentifier(this.getId());
        fireRetrievalStarted(re);
        re.setHasErrors(true);
        re.setRetrievedObject(t.getMessage());
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
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getPropertyName().equals("progress"))
      {
        int progress = (Integer) evt.getNewValue();
        //AbstractFeatureService.this.setProgress(progress);
        if(DEBUG)logger.debug("LIW[" + this.getId() + "]: LayerInitWorker progress: " + progress);

        RetrievalEvent re = new RetrievalEvent();
        re.setInitialisationEvent(true);
        re.setRequestIdentifier(this.getId());
        re.setIsComplete(progress != 100);
        re.setPercentageDone(progress);
        AbstractFeatureService.this.fireRetrievalProgress(re);
      }
    }

    @Override
    public String toString()
    {
      return String.valueOf(this.getId());
    }
  }
}
