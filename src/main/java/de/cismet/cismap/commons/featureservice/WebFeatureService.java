/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleWebFeatureService.java
 *
 * Created on 17. November 2006, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;
//import org.deegree2.model.feature.Feature;
//import org.deegree2.model.feature.FeatureCollection;
//import org.deegree2.model.feature.GMLFeatureCollectionDocument;

import org.jdom.Element;

import java.awt.Font;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.WFSFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.WFSFeatureFactory;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;

/**
 * This class provides access to a Web Feature service. Requests will be send to a WFS instance. The response will be
 * parsed and transformed to an internal features representation. These internal features will be send to all registered
 * listeners
 *
 * @author   Sebastian Puhl
 * @version  $Revision$, $Date$
 */
public class WebFeatureService extends AbstractFeatureService<WFSFeature, String> {

    //~ Static fields/initializers ---------------------------------------------

    public static final String WFS_FEATURELAYER_TYPE = "WebFeatureServiceLayer"; // NOI18N
    public static final HashMap<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();

    static {
        layerIcons.put(
            LAYER_ENABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerWfs.png")));                   // NOI18N
        layerIcons.put(
            LAYER_ENABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerWfsInvisible.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfs.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerWfsInvisible.png"))); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    /** the request which will be send to the WFS. */
    private String wfsQueryString;
    private Element wfsQueryElement;
    /** the hostname of the WFS server. */
    private String hostname;

    /** the version of the wfs. */
    private String version;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WebFeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public WebFeatureService(final Element e) throws Exception {
        super(e);
    }

    /**
     * Create a new <b>uninitialised</b> AbstractFeatureService except for the attributes provided.
     *
     * @param   name        the name of this FeatureService
     * @param   host        hostname of the WFS server
     * @param   query       the request which will be send to the WFS
     * @param   attributes  featureServiceAttributes vector with all FeatureServiceAttributes of the FeatureService
     * @param   version     DOCUMENT ME!
     *
     * @throws  Exception  if something went wrong
     */
    public WebFeatureService(final String name,
            final String host,
            final Element query,
            final Vector<FeatureServiceAttribute> attributes,
            final String version) throws Exception {
        super(name, attributes);
        this.version = version;
        setQueryElement(query);
        setHostname(host);

        // defaults for new services
        this.setTranslucency(0.2f);
        this.setMaxFeatureCount(2900);
    }

    /**
     * Protected Constructor that clones (shallow) the delivered WebFeatureService. Attributes, layer properties and
     * feature factories are not cloned deeply. The WebFeatureService to be cloned should be initilaised.
     *
     * @param  wfs  FeatureService that should be cloned
     */
    protected WebFeatureService(final WebFeatureService wfs) {
        super(wfs);
        this.setHostname(wfs.getHostname());
        this.setQueryElement(wfs.getQueryElement());
        // overwrite with customised query if applicable
        this.setQuery(wfs.getQuery());
        this.maxFeatureCount = 2500;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void initConcreteInstance() throws Exception {
        this.layerProperties.setQueryType(LayerProperties.QUERYTYPE_XML);
    }

    @Override
    public void setMaxFeatureCount(final int maxFeatureCount) {
        super.setMaxFeatureCount(maxFeatureCount);
        if (this.wfsQueryElement != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("setting max features of WFS query to " + (maxFeatureCount + 100)); // NOI18N
            }
            WFSFacade.setMaxFeatureCount(this.wfsQueryElement, maxFeatureCount + 100, getVersion());
            this.wfsQueryString = FeatureServiceUtilities.elementToString(this.wfsQueryElement);
        }
    }

    @Override
    public Element toElement() {
        final Element parentElement = super.toElement();

        final CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, hostname, getVersion(), false);
        parentElement.addContent(capLink.getElement());
        parentElement.addContent(getQueryElement().detach());

        return parentElement;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        super.initFromElement(element);
        final CapabilityLink cp = new CapabilityLink(element);
        final Element query = element.getChild(FeatureServiceUtilities.GET_FEATURE, FeatureServiceUtilities.WFS);
        // query string is not saved!
        this.setQuery(null);
        this.setQueryElement(query);
        this.setHostname(cp.getLink());
        this.setVersion(cp.getVersion());
    }

    /**
     * This method creates an one-to-one hard copy of the SimpleWebFeatureService.
     *
     * @return  the copy of the SimpleWebFeatureService
     */
    @Override
    public Object clone() {
        return new WebFeatureService(this);
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Delivers the host-string of the FeatureService.
     *
     * @return  hostname as string
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Setter for the host-string of the FeatureService.
     *
     * @param  hostname  hostname to set
     */
    protected void setHostname(final String hostname) {
        this.hostname = hostname;
        if (this.getFeatureFactory() != null) {
            ((WFSFeatureFactory)this.getFeatureFactory()).setHostname(hostname);
        }
    }

    @Override
    public String getQuery() {
        return wfsQueryString;
    }

    @Override
    public void setQuery(final String wfsQueryString) {
        if (logger.isDebugEnabled()) {
            logger.debug("setting the string representation of the WFS query (will not be saved)"); // NOI18N
        }
        this.wfsQueryString = wfsQueryString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getQueryElement() {
        return wfsQueryElement;
    }

    /**
     * Sets a new wfsQuery Element and overwites the string query.
     *
     * @param  wfsQuery  DOCUMENT ME!
     */
    public void setQueryElement(final Element wfsQuery) {
        if (logger.isDebugEnabled()) {
            logger.debug("setting the XML Element representation of the WFS query (will be saved)"); // NOI18N
        }
        this.wfsQueryElement = wfsQuery;
        // overwrite string representation of query
        if (this.wfsQueryElement != null) {
            // +1 reich nicht aus, daher +100
            WFSFacade.setMaxFeatureCount(this.wfsQueryElement, maxFeatureCount + 100, getVersion());
            this.wfsQueryString = FeatureServiceUtilities.elementToString(wfsQuery);
        }
    }

    @Override
    protected String getFeatureLayerType() {
        return WFS_FEATURELAYER_TYPE;
    }

    @Override
    public Icon getLayerIcon(final int type) {
        return layerIcons.get(type);
    }

    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();

        // very slow: defaultLayerProperties.setPrimaryAnnotationExpression("if (app:flurstn!=\"0\") {return app:flurstz
        // + \" / \" + app:flurstn;} else {return app:flurstz;}", LayerProperties.EXPRESSIONTYPE_GROOVY);
        defaultLayerProperties.setPrimaryAnnotationExpression(
            "app:flurstz",
            LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);                                               // NOI18N
        defaultLayerProperties.getStyle().setMultiplier(1d);
        defaultLayerProperties.getStyle().setFont(new Font("sansserif", Font.PLAIN, 12));               // NOI18N
        defaultLayerProperties.setIdExpression("app:gid", LayerProperties.EXPRESSIONTYPE_PROPERTYNAME); // NOI18N
        defaultLayerProperties.setQueryType(LayerProperties.QUERYTYPE_XML);

        return defaultLayerProperties;
    }

    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        return new WFSFeatureFactory(this.getLayerProperties(), this.getHostname(), this.getVersion());
    }

    /**
     * Sets the Layer properties but does not refresh the cached features.
     *
     * @param  layerProperties  DOCUMENT ME!
     */
    public void setLayerPropertiesWithoutUpdate(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
        this.featureFactory.setLayerProperties(layerProperties);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the version of the referenced wfs
     */
    public String getVersion() {
        return version;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  version  the version of the referenced wfs to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
