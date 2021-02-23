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
package de.cismet.cismap.commons.featureservice.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.httpclient.methods.PostMethod;

import org.deegree.gml.feature.GMLFeatureReader;
import org.deegree.model.feature.DefaultFeature;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.JTSAdapter;

import org.jdom.Element;

import org.w3c.dom.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.WFSFeature;
import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;

import de.cismet.commons.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

/**
 * A FeatureFactory that creates WFSFeatures obtained from a Web Feature Service.<br/>
 * The factory is non-caching, which means that each request to {@code createFeatures} leads to a new WFS request, even
 * if the bounding box is the same. However, it is possible to obtain the features created by the latesd WFS request via
 * the {@code getLastCreatedFeatures()} operation.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class WFSFeatureFactory extends DegreeFeatureFactory<WFSFeature, String> {

    //~ Static fields/initializers ---------------------------------------------

    private static final String ENCODING_STRING = "encoding='";
    private static final String ALTERNATE_ENCODING_STRING = "encoding=\"";

    //~ Instance fields --------------------------------------------------------

    protected String hostname = null;
    protected FeatureType featureType;
    private Crs crs;
    private boolean reverseAxisOrder = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * private Vector<WFSFeature> wfsFeatureVector = new Vector(); private PostMethod httppost; private
     * InputStreamReader reader;
     *
     * @param  layerProperties  DOCUMENT ME!
     * @param  hostname         DOCUMENT ME!
     * @param  featureType      wfsVersion DOCUMENT ME!
     * @param  crs              DOCUMENT ME!
     * @param  styles           DOCUMENT ME!
     */
    public WFSFeatureFactory(final LayerProperties layerProperties,
            final String hostname,
            final FeatureType featureType,
            final Crs crs,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) {
        this(layerProperties, hostname, featureType, crs, styles, false);
    }

    /**
     * private Vector<WFSFeature> wfsFeatureVector = new Vector(); private PostMethod httppost; private
     * InputStreamReader reader;
     *
     * @param  layerProperties   DOCUMENT ME!
     * @param  hostname          DOCUMENT ME!
     * @param  featureType       wfsVersion DOCUMENT ME!
     * @param  crs               DOCUMENT ME!
     * @param  styles            DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     */
    public WFSFeatureFactory(final LayerProperties layerProperties,
            final String hostname,
            final FeatureType featureType,
            final Crs crs,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles,
            final boolean reverseAxisOrder) {
        logger.info("initialising WFSFeatureFactory with hostname: '" + hostname + "'");
        this.layerProperties = layerProperties;
        this.hostname = hostname;
        this.featureType = featureType;
        this.crs = crs;
        this.styles = styles;
        this.reverseAxisOrder = reverseAxisOrder;
    }

    /**
     * Creates a new WFSFeatureFactory object.
     *
     * @param  wfsff  DOCUMENT ME!
     */
    protected WFSFeatureFactory(final WFSFeatureFactory wfsff) {
        super(wfsff);
        this.hostname = wfsff.hostname;
        this.featureType = wfsff.featureType;
        this.crs = wfsff.crs;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  hostname  DOCUMENT ME!
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * TODO: Track Progress?
     *
     * @param   query         DOCUMENT ME!
     * @param   boundingBox   DOCUMENT ME!
     * @param   workerThread  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    @Override
    public Vector<WFSFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread) throws TooManyFeaturesException, Exception {
        return createFeatures_internal(query, boundingBox, workerThread, true);
    }

    /**
     * TODO: Track Progress?
     *
     * @param   query              DOCUMENT ME!
     * @param   boundingBox        DOCUMENT ME!
     * @param   workerThread       DOCUMENT ME!
     * @param   saveAsLastCreated  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    private Vector<WFSFeature> createFeatures_internal(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final boolean saveAsLastCreated) throws TooManyFeaturesException, Exception {
        // check if canceled .......................................................
        if (this.checkCancelled(workerThread, "createFeatures()")) {
            return null;
        }
        // check if canceled .......................................................
        final XBoundingBox bbox = new XBoundingBox(boundingBox.getX1(),
                boundingBox.getY1(),
                boundingBox.getX2(),
                boundingBox.getY2(),
                getCrs().getCode(),
                getCrs().isMetric());

        long start = System.currentTimeMillis();
        final Vector<WFSFeature> features;

        if (featuresAlreadyInMemory(bbox.getGeometry(), query)) {
            features = createFeaturesFromMemory(query, bbox.getGeometry());
        } else {
            final WFSFacade facade = featureType.getWFSCapabilities().getServiceFacade();
            final String postString = facade.setGetFeatureBoundingBox(
                    query,
                    bbox,
                    featureType,
                    getCrs().getCode(),
                    reverseAxisOrder);
            featureSrid = CrsTransformer.extractSridFromCrs(WFSFacade.getOptimalCrsForFeature(
                        featureType,
                        getCrs().getCode()));

            // check if canceled .......................................................
            if (this.checkCancelled(workerThread, "creating post string")) {
                return null;
            }
            // check if canceled .......................................................
            if (logger.isDebugEnabled()) {
                logger.debug("FRW[" + workerThread + "]: Host name: " + hostname + "\nWFS Query: \n" + postString);
            }

            final InputStream respIs = WebAccessManager.getInstance()
                        .doRequest(new URL(hostname), postString, ACCESS_METHODS.POST_REQUEST);

            // check if canceled .......................................................
            if (this.checkCancelled(workerThread, "executing http request")) {
                return null;
            }
            // check if canceled .......................................................

            logger.info("FRW[" + workerThread + "]: WFS request took " + (System.currentTimeMillis() - start) + " ms");

            final InputStreamReader reader = new InputStreamReader(new BufferedInputStream(respIs));

            // check if canceled .......................................................
            if (this.checkCancelled(workerThread, "creating InputStreamReader")) {
                return null;
            }
            // check if canceled .......................................................
            final GMLFeatureCollectionDocument featureCollectionDocument = new GMLFeatureCollectionDocument();
            final FeatureCollection featureCollection;

            try {
                start = System.currentTimeMillis();

                // check if canceled .......................................................
                if (this.checkCancelled(workerThread, "creating GMLFeatureCollectionDocument")) {
                    return null;
                }
                // check if canceled .......................................................

                // debug
                final StringBuilder res = new StringBuilder();
                String charset = null;
                String tmp;
                final BufferedReader br = new BufferedReader(reader);

                while ((tmp = br.readLine()) != null) {
                    if (charset != null) {
                        try {
                            res.append(new String(tmp.getBytes(), charset));
                        } catch (UnsupportedEncodingException e) {
                            logger.error("Unsupported encoding found: " + charset, e);
                            res.append(new String(tmp.getBytes()));
                        }
                    } else {
                        charset = checkForCharset(tmp);
                        if (charset != null) {
                            try {
                                res.append(new String(tmp.getBytes(), charset));
                            } catch (UnsupportedEncodingException e) {
                                logger.error("Unsupported encoding found: " + charset, e);
                                res.append(new String(tmp.getBytes()));
                            }
                        } else {
                            res.append(new String(tmp.getBytes()));
                        }
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("wfs response: " + res.toString());
                }
                final StringReader re = new StringReader(res.toString());
                // debug

                featureCollectionDocument.load(re, "http://dummyID");

                // check if canceled .......................................................
                if (this.checkCancelled(workerThread, "loading features")) {
                    return null;
                }
                // check if canceled .......................................................

                // getFeatureCount() stimmt nicht mit der zahl der geparsten features überein!?
                /*
                 * if (featureCollectionDocument.getFeatureCount() > this.getMaxFeatureCount()) { throw new
                 * TooManyFeaturesException("feature in feature document " + featureCollectionDocument.getFeatureCount()
                 * + " exceeds max feature count " + this.getMaxFeatureCount()); } else
                 */
                if (featureCollectionDocument.getFeatureCount() == 0) {
                    logger.warn("FRW[" + workerThread + "]: no features found before parsing");
                    // if(DEBUG)logger.debug(featureCollectionDocument.getAsString());
                    return null;
                }

                if (DEBUG) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("FRW[" + workerThread + "]: parsing " + featureCollectionDocument
                                    .getFeatureCount()
                                    + " features");
                    }
                }

                // StringWriter sw = new StringWriter();
                // featureCollectionDocument.write(sw);
                featureCollection = featureCollectionDocument.parse();

                // check if canceled .......................................................
                if (this.checkCancelled(workerThread, "parsing features")) {
                    return null;
                }
                // check if canceled .......................................................

                if ((featureCollection.size() == 1) && (featureCollection.getFeature(0).getName() != null)
                            && featureCollection.getFeature(0).getName().getLocalName().equals("ExceptionText")) {
                    logger.warn(
                        "The wfs response contains only one feature with the name ExceptionText. "
                                + "So an error occured. Trying to extract the error message.");
                    try {
                        final String errorMessage = featureCollectionDocument.getRootElement()
                                    .getFirstChild()
                                    .getFirstChild()
                                    .getTextContent();

                        throw new Exception(errorMessage);
                    } catch (NullPointerException e) {
                        logger.error("Cannot extract the error message from the wfs response.");
                        throw new Exception(
                            "The wfs replies with an Exception, but the error text cannot be extracted.");
                    }
                }

                logger.info("FRW[" + workerThread + "]: parsing " + featureCollection.size() + " features took "
                            + (System.currentTimeMillis() - start) + " ms");

                if (featureCollection.size() > this.getMaxFeatureCount()) {
                    throw new TooManyFeaturesException("FRW[" + workerThread + "]: feature in feature document "
                                + featureCollection.size() + " exceeds max feature count " + this.getMaxFeatureCount());
                } else if (featureCollection.size() == 0) {
                    logger.warn("FRW[" + workerThread + "]: no features found after parsing");
                    return null;
                }

                features = processFeatureCollection(
                        workerThread,
                        featureCollection.toArray(),
                        true);
            } catch (Exception t) {
                logger.error("FRW[" + workerThread + "]: error parsing features: " + t.getMessage(), t);
                throw t;
            }
        }

        // check if thread is canceled .........................................
        if (this.checkCancelled(workerThread, " saving LastCreatedFeatures ")) {
            return null;
        }
        // check if thread is canceled .........................................

        if (saveAsLastCreated) {
            this.updateLastCreatedFeatures(features, boundingBox.getGeometry(featureSrid), query);
        }

        return features;
    }

    /**
     * Checks, if the given string contains the charset.
     *
     * @param   data  DOCUMENT ME!
     *
     * @return  the charset contained in the given string or null if no charset is contained
     */
    private String checkForCharset(final String data) {
        int index = data.indexOf(ENCODING_STRING);

        if (index == -1) {
            index = data.indexOf(ALTERNATE_ENCODING_STRING);
        }

        if (index != -1) {
            final String subdata = data.substring(index + ENCODING_STRING.length());
            index = subdata.indexOf("'");

            if (index == -1) {
                index = subdata.indexOf("\"");
            }

            if (index != -1) {
                try {
                    final String charsetName = subdata.substring(0, index);
                    Charset.forName(charsetName);

                    return charsetName;
                } catch (Exception e) {
                    // no valid charset name. Nothing to do
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   workerThread  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException       DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     * @throws  Exception                      DOCUMENT ME!
     */
    @Override
    public Vector createAttributes(final SwingWorker workerThread) throws TooManyFeaturesException,
        UnsupportedOperationException,
        Exception {
        throw new UnsupportedOperationException("LIW[" + workerThread
                    + "]: WFSFeatureFactory does not support Attributes");
    }

    @Override
    protected void initialiseFeature(final WFSFeature featureServiceFeature,
            final Feature degreeFeature,
            final boolean evaluateExpressions,
            final int index) throws Exception {
        // perform standard initialisation
        featureServiceFeature.setLayerProperties(this.getLayerProperties());

        // creating geometry
        if (featureServiceFeature.getGeometry() == null) {
            try {
//                final DefaultFeature f = ((DefaultFeature)degreeFeature.getProperties()[5].getValue()).getProperties()[0];
                Geometry geom = JTSAdapter.export(
                        degreeFeature.getGeometryPropertyValues()[geometryIndex]);
                if (reverseAxisOrder) {
                    geom = reverseGeometryCoordinates(geom);
                }
                featureServiceFeature.setGeometry(geom);
            } catch (Exception e) {
                Geometry geom = JTSAdapter.export(
                        degreeFeature.getGeometryPropertyValues()[geometryIndex]);
                if (reverseAxisOrder) {
                    geom = reverseGeometryCoordinates(geom);
                }
                featureServiceFeature.setGeometry(geom);
            }
        }

        if ((featureServiceFeature.getGeometry() != null) && (featureSrid != null)) {
            featureServiceFeature.getGeometry().setSRID(featureSrid);
        }

        // adding properties
        final FeatureProperty[] featureProperties = degreeFeature.getProperties();
        for (final FeatureProperty fp : featureProperties) {
            if (featureServiceFeature.getProperty(fp.getName().getAsString()) == null) {
                featureServiceFeature.addProperty(fp.getName().getAsString(), fp.getValue());
            }
        }

        if (evaluateExpressions) {
            this.evaluateExpressions(featureServiceFeature, index);
        }
    }

    /**
     * The axis order of the coordinates of the given geometry will be changed.
     *
     * @param   g  the geometry to change the axis order
     *
     * @return  the given geometry with a changed axis order.
     */
    private Geometry reverseGeometryCoordinates(final Geometry g) {
        g.apply(new CoordinateFilter() {

                @Override
                public void filter(final Coordinate crdnt) {
                    final double newX = crdnt.y;
                    crdnt.y = crdnt.x;
                    crdnt.x = newX;
                }
            });
        g.geometryChanged();
        return g;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  reader    DOCUMENT ME!
     * @param  httppost  DOCUMENT ME!
     */
    protected void cleanup(InputStreamReader reader, PostMethod httppost) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception silent) {
            }
            reader = null;
        }

        if (httppost != null) {
            httppost.releaseConnection();
            httppost = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   degreeFeature  DOCUMENT ME!
     * @param   index          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    protected WFSFeature createFeatureInstance(final Feature degreeFeature, final int index) throws Exception {
        final WFSFeature f = new WFSFeature();
        String name = null;

        if ((layerProperties != null) && (layerProperties.getFeatureService() != null)) {
            name = layerProperties.getFeatureService().getName();
        }

        if ((name == null) && (featureType != null) && (featureType.getName() != null)) {
            name = featureType.getName().getPrefix() + ":" + featureType.getName().getLocalPart();
        }

        f.setSLDStyles(getStyle(name));

        return f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected boolean isGenerateIds() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public WFSFeatureFactory clone() {
        return new WFSFeatureFactory(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the crs
     */
    public Crs getCrs() {
        return crs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  the crs to set
     */
    public void setCrs(final Crs crs) {
        this.crs = crs;
    }

    @Override
    public int getFeatureCount(final String query, final BoundingBox bb) {
        final XBoundingBox bbox = new XBoundingBox(bb.getX1(),
                bb.getY1(),
                bb.getX2(),
                bb.getY2(),
                getCrs().getCode(),
                getCrs().isMetric());
        final WFSFacade facade = featureType.getWFSCapabilities().getServiceFacade();
        final Element queryElement = facade.getGetFeatureQuery(featureType);
        String wfsQuery = query;

        if (wfsQuery == null) {
            wfsQuery = FeatureServiceUtilities.elementToString(queryElement);
        }

        final String postString = facade.setGetFeatureBoundingBox(
                wfsQuery,
                bbox,
                featureType,
                getCrs().getCode(),
                reverseAxisOrder,
                true);
        featureSrid = CrsTransformer.extractSridFromCrs(WFSFacade.getOptimalCrsForFeature(
                    featureType,
                    getCrs().getCode()));

        if (logger.isDebugEnabled()) {
            logger.debug("Host name: " + hostname + "\nWFS Query: \n" + postString);
        }
        final long start = System.currentTimeMillis();

        try {
            final InputStream respIs = WebAccessManager.getInstance()
                        .doRequest(new URL(hostname), postString, ACCESS_METHODS.POST_REQUEST);

            logger.info("WFS request took " + (System.currentTimeMillis() - start) + " ms");

            final InputStreamReader reader = new InputStreamReader(new BufferedInputStream(respIs));

            final GMLFeatureCollectionDocument featureCollectionDocument = new GMLFeatureCollectionDocument();
            final FeatureCollection featureCollection;

            // debug
            String res = "";
            String tmp;
            final BufferedReader br = new BufferedReader(reader);
            while ((tmp = br.readLine()) != null) {
                res += tmp;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("wfs response: " + res);
            }
            final StringReader re = new StringReader(res);

            final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = fac.newDocumentBuilder();
            final Document doc = builder.parse(new ByteArrayInputStream(res.getBytes("UTF-8")));
            final String numberOfFeatures = doc.getDocumentElement().getAttribute("numberOfFeatures");

            return Integer.parseInt(numberOfFeatures);
//            featureCollectionDocument.load(re, "http://dummyID");
//
//            return featureCollectionDocument.getFeatureCount();
        } catch (Exception t) {
            logger.error("error parsing features: " + t.getMessage(), t);
            return 0;
        }
    }

    @Override
    public List<WFSFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
        return createFeatures_internal(query, boundingBox, workerThread, false);
    }
}
