/*
 * SimpleWebFeatureService.java
 *
 * Created on 17. November 2006, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.WFSFeatureFactory;
import de.cismet.cismap.commons.features.WFSFeature;
import de.cismet.cismap.commons.featureservice.style.StyleDialog;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
//import org.deegree2.model.feature.Feature;
//import org.deegree2.model.feature.FeatureCollection;
//import org.deegree2.model.feature.GMLFeatureCollectionDocument;
import org.jdom.Element;

/**
 * This class provides access to a Web Feature service. Requests will be send to a
 * WFS instance. The response will be parsed and transformed to an internal
 * features representation. These internal features will be send to all registered
 * listeners
 * @author Sebastian Puhl
 */
public class WebFeatureService extends AbstractFeatureService<WFSFeature,String>
{
  public static final String WFS_FEATURELAYER_TYPE = "WebFeatureServiceLayer";
  public static final HashMap<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();

  static
  {
    layerIcons.put(LAYER_ENABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfs.png")));
    layerIcons.put(LAYER_ENABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerWfsInvisible.png")));
    layerIcons.put(LAYER_DISABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfs.png")));
    layerIcons.put(LAYER_DISABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfsInvisible.png")));
  }
  /**
   * the request which will be send to the WFS
   */
  private String wfsQueryString;
  private Element wfsQueryElement;
  /**
   * the hostname of the WFS server
   */
  private String hostname;

  /**
   * Protected Constructor that clones (shallow) the delivered WebFeatureService.
   * Attributes, layer properties and feature factories are not cloned deeply.
   * The WebFeatureService to be cloned should be initilaised.
   *
   * @param afs FeatureService that should be cloned
   */
  protected WebFeatureService(WebFeatureService wfs)
  {
    super(wfs);
    this.setHostname(wfs.getHostname());
    this.setQueryElement(wfs.getQueryElement());
    // overwrite with customised query if applicable
    this.setQuery(wfs.getQuery());
    this.maxFeatureCount = 2500;
  }

  /**
   * Create a new <b>uninitialised</b> AbstractFeatureService except for the
   * attributes provided
   *
   * @param name the name of this FeatureService
   * @param featureServiceAttributes vector with all FeatureServiceAttributes of the FeatureService
   * @param host hostname of the WFS server
   * @param query the request which will be send to the WFS
   * @throws Exception if something went wrong
   */
  public WebFeatureService(String name, String host, Element query, Vector<FeatureServiceAttribute> attributes) throws Exception
  {
    super(name, attributes);
    setQueryElement(query);
    setHostname(host);

    // defaults for new services
    this.setTranslucency(0.2f);
    this.setMaxFeatureCount(2900);
  }

  public WebFeatureService(Element e) throws Exception
  {
    super(e);
  }

  @Override
  protected void initConcreteInstance() throws Exception
  {
    this.layerProperties.setQueryType(LayerProperties.QUERYTYPE_XML);
  }

  @Override
  public void setMaxFeatureCount(int maxFeatureCount)
  {
    super.setMaxFeatureCount(maxFeatureCount);
    if (this.wfsQueryElement != null)
    {
      logger.debug("setting max features of WFS query to "+(maxFeatureCount+100));
      FeatureServiceUtilities.setMaxFeatureCount(this.wfsQueryElement, maxFeatureCount+100);
      this.wfsQueryString = FeatureServiceUtilities.elementToString(this.wfsQueryElement);
    }
  }

  @Override
  public Element toElement()
  {
    Element parentElement = super.toElement();

    CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, hostname, false);
    parentElement.addContent(capLink.getElement());
    parentElement.addContent(getQueryElement().detach());

    return parentElement;
  }

  @Override
  public void initFromElement(Element element) throws Exception
  {
    super.initFromElement(element);
    CapabilityLink cp = new CapabilityLink(element);   
    Element query = element.getChild(FeatureServiceUtilities.GET_FEATURE, FeatureServiceUtilities.WFS);
    // query string is not saved!
    this.setQuery(null);
    this.setQueryElement(query);
    this.setHostname(cp.getLink());
  }

  /**
   * This method creates an one-to-one hard copy of the SimpleWebFeatureService
   * @return the copy of the SimpleWebFeatureService
   */
  @Override
  public Object clone()
  {
    return new WebFeatureService(this);
  }

  public void removeAllListeners()
  {
    listeners.clear();
  }

  /**
   * Delivers the host-string of the FeatureService
   * @return hostname as string
   */
  public String getHostname()
  {
    return hostname;
  }

  /**
   * Setter for the host-string of the FeatureService
   * @param hostname hostname to set
   */
  protected void setHostname(String hostname)
  {
    this.hostname = hostname;
    if (this.getFeatureFactory() != null)
    {
      ((WFSFeatureFactory) this.getFeatureFactory()).setHostname(hostname);
    }
  }

  @Override
  public String getQuery()
  {
    return wfsQueryString;
  }

  @Override
  public void setQuery(String wfsQueryString)
  {
    logger.debug("setting the string representation of the WFS query (will not be saved)");
    this.wfsQueryString = wfsQueryString;
  }

  public Element getQueryElement()
  {
    return wfsQueryElement;
  }

  /**
   * Sets a new wfsQuery Element and overwites the string query.
   *
   * @param wfsQuery
   */
  public void setQueryElement(Element wfsQuery)
  {
    logger.debug("setting the XML Element representation of the WFS query (will be saved)");
    this.wfsQueryElement = wfsQuery;
    // overwrite string representation of query
    if (this.wfsQueryElement != null)
    {
      //+1 reich nicht aus, daher +100
      FeatureServiceUtilities.setMaxFeatureCount(this.wfsQueryElement, maxFeatureCount+100);
      this.wfsQueryString = FeatureServiceUtilities.elementToString(wfsQuery);
    }
  }

  @Override
  protected String getFeatureLayerType()
  {
    return WFS_FEATURELAYER_TYPE;
  }

  @Override
  public Icon getLayerIcon(int type)
  {
    return layerIcons.get(type);
  }

  @Override
  protected LayerProperties createLayerProperties()
  {
    DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();

    // very slow:
    //defaultLayerProperties.setPrimaryAnnotationExpression("if (app:flurstn!=\"0\") {return app:flurstz + \" / \" + app:flurstn;} else {return app:flurstz;}", LayerProperties.EXPRESSIONTYPE_GROOVY);
    defaultLayerProperties.setPrimaryAnnotationExpression("app:flurstz", LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
    defaultLayerProperties.getStyle().setMultiplier(1d);
    defaultLayerProperties.getStyle().setFont(new Font("sansserif", Font.PLAIN, 12));
    defaultLayerProperties.setIdExpression("app:gid", LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
    defaultLayerProperties.setQueryType(LayerProperties.QUERYTYPE_XML);

    return defaultLayerProperties;
  }

  @Override
  protected FeatureFactory createFeatureFactory() throws Exception
  {
    return new WFSFeatureFactory(this.getLayerProperties(), this.getHostname());
  }

  /**
   * Sets the Layer properties but does not refresh the cached features.
   *
   * @param layerProperties
   */
  public void setLayerPropertiesWithoutUpdate(LayerProperties layerProperties)
  {
    this.layerProperties = layerProperties;
    this.featureFactory.setLayerProperties(layerProperties);
  }
}
