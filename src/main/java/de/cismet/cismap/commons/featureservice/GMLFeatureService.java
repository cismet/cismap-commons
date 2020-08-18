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

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.GMLFeatureFactory;

import de.cismet.commons.wms.capabilities.Layer;

/**
 * Document FeatureService that supports GML Documents.
 *
 * @author   Sebastian Puhl
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class GMLFeatureService extends DocumentFeatureService<FeatureServiceFeature, String>
        implements LayerInfoProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(GMLFeatureService.class);

    public static final String GML_FEATURELAYER_TYPE = "GMLFeatureServiceLayer"; // NOI18N
    public static final Map<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();

    static {
        layerIcons.put(
            LAYER_ENABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerGml.png")));                   // NOI18N
        layerIcons.put(
            LAYER_ENABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerGmlInvisible.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGml.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGmlInvisible.png"))); // NOI18N
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GMLFeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GMLFeatureService(final Element e) throws Exception {
        super(e);
    }

    /**
     * Creates a new GMLFeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   documentURI   DOCUMENT ME!
     * @param   documentSize  DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GMLFeatureService(final String name,
            final URI documentURI,
            final long documentSize,
            final List<FeatureServiceAttribute> attributes) throws Exception {
        super(name, documentURI, documentSize, attributes);
    }

    /**
     * Creates a new GMLFeatureService object.
     *
     * @param  gfs  DOCUMENT ME!
     */
    protected GMLFeatureService(final GMLFeatureService gfs) {
        super(gfs);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Icon getLayerIcon(final int type) {
        return layerIcons.get(type);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();

        // IDs of documents can be autogenerated (faster)!
        defaultLayerProperties.setIdExpression(null, LayerProperties.EXPRESSIONTYPE_UNDEFINED);
        defaultLayerProperties.setFeatureService(this);
        return defaultLayerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        return new GMLFeatureFactory(this.getLayerProperties(),
                this.getDocumentURI(),
                this.maxSupportedFeatureCount,
                this.layerInitWorker);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getQuery() {
        // LOG.warn("unexpected call to getQuery, not supported by this service");
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  query  DOCUMENT ME!
     */
    @Override
    public void setQuery(final String query) {
        LOG.warn("unexpected call to setQuery, not supported by this service:\n" + query); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    protected void initConcreteInstance() throws Exception {
        // nothing to do here
    }

    /**
     * DOCUMENT ME!
     *
     * @param  documentURI  DOCUMENT ME!
     */
    @Override
    public void setDocumentURI(final URI documentURI) {
        super.setDocumentURI(documentURI);
        if (this.getFeatureFactory() != null) {
            ((GMLFeatureFactory)this.getFeatureFactory()).setDocumentURI(documentURI);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected String getFeatureLayerType() {
        return GML_FEATURELAYER_TYPE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object clone() {
        LOG.info("cloning service " + this.getName()); // NOI18N
        return new GMLFeatureService(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getLayerURI() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getServerURI() {
        return documentURI.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isLayerQuerySelected() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selected  DOCUMENT ME!
     */
    @Override
    public void setLayerQuerySelected(final boolean selected) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isQueryable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Layer getLayerInformation() {
        return null;
    }
}
