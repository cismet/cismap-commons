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

import java.awt.Graphics2D;

import java.io.File;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.ShapeFeatureFactory;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
//Todo optimieren wann welche Features geladen werden z.B. bei 150 MB file
public class ShapeFileFeatureService extends DocumentFeatureService<ShapeFeature, String> {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ShapeFileFeatureService.class);
    public static final Map<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();
    public static final String SHAPE_FEATURELAYER_TYPE = "ShapeFeatureServiceLayer"; // NOI18N

    static {
        layerIcons.put(
            LAYER_ENABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png")));                   // NOI18N
        layerIcons.put(
            LAYER_ENABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png"))); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    private boolean noGeometryRecognised = false;
    private boolean errorInGeometryFound = false;
    private boolean fileNotFound = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFileFeatureService(final Element e) throws Exception {
        super(e);
        checkFile();
    }

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   documentURI   DOCUMENT ME!
     * @param   documentSize  DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFileFeatureService(final String name,
            final URI documentURI,
            final long documentSize,
            final List<FeatureServiceAttribute> attributes) throws Exception {
        super(name, documentURI, documentSize, attributes);
        this.maxFeatureCount = Integer.MAX_VALUE;
        checkFile();
    }

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param  sfs  DOCUMENT ME!
     */
    protected ShapeFileFeatureService(final ShapeFileFeatureService sfs) {
        super(sfs);
        checkFile();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void checkFile() {
        final File file = new File(documentURI);

        if (!file.exists()) {
            fileNotFound = true;
        } else {
            fileNotFound = false;
        }
    }

    @Override
    public Icon getLayerIcon(final int type) {
        return layerIcons.get(type);
    }

    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
        // defaultLayerProperties.setIdExpression("app:ID", LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
        // IDs of documents can be autogenerated (faster)!
        defaultLayerProperties.setIdExpression(null, LayerProperties.EXPRESSIONTYPE_UNDEFINED);
        defaultLayerProperties.setFeatureService(this);
        return defaultLayerProperties;
    }

    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        final ShapeFeatureFactory sff = new ShapeFeatureFactory(this.getLayerProperties(),
                this.getDocumentURI(),
                this.maxSupportedFeatureCount,
                this.layerInitWorker,
                parseSLD(getSLDDefiniton()));
        noGeometryRecognised = sff.isNoGeometryRecognised();
        errorInGeometryFound = sff.isErrorInGeometryFound();
        // sff.setSLDStyle();
        return sff;
    }

    @Override
    public String getQuery() {
        // LOG.warn("unexpected call to getQuery, not supported by this service");
        return null;
    }

    @Override
    public void setQuery(final String query) {
        LOG.warn("unexpected call to setQuery, not supported by this service:\n" + query); // NOI18N
    }

    @Override
    protected void initConcreteInstance() throws Exception {
        // nothing to do here
    }

    @Override
    public void setDocumentURI(final URI documentURI) {
        super.setDocumentURI(documentURI);
        if (this.getFeatureFactory() != null) {
            ((ShapeFeatureFactory)this.getFeatureFactory()).setDocumentURI(documentURI);
        }
        checkFile();
    }

    @Override
    protected String getFeatureLayerType() {
        return SHAPE_FEATURELAYER_TYPE;
    }

    @Override
    public Object clone() {
        LOG.info("cloning service " + this.getName()); // NOI18N
        return new ShapeFileFeatureService(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the noGeometryRecognised
     */
    public boolean isNoGeometryRecognised() {
        return noGeometryRecognised;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the errorInGeometryFound
     */
    public boolean isErrorInGeometryFound() {
        return errorInGeometryFound;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the fileNotFound
     */
    public boolean isFileNotFound() {
        return fileNotFound;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final Crs crs) {
        ((ShapeFeatureFactory)featureFactory).setCrs(crs);
    }

// breaks DocumentFeatureServiceFactory
//  @Override
//  protected String getFeatureLayerType()
//  {
//    return SHAPE_FEATURELAYER_TYPE;
//  }
}
