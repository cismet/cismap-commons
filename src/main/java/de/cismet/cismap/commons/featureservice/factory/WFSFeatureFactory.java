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

import org.apache.commons.httpclient.methods.PostMethod;

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;

import org.jdom.Element;

import org.w3c.dom.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.net.URL;

import java.util.List;
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

    //~ Instance fields --------------------------------------------------------

    protected String hostname = null;
    protected FeatureType featureType;
    private Crs crs;

    //~ Constructors -----------------------------------------------------------

    /**
     * private Vector<WFSFeature> wfsFeatureVector = new Vector(); private PostMethod httppost; private
     * InputStreamReader reader;
     *
     * @param  layerProperties  DOCUMENT ME!
     * @param  hostname         DOCUMENT ME!
     * @param  featureType      wfsVersion DOCUMENT ME!
     * @param  crs              DOCUMENT ME!
     */
    public WFSFeatureFactory(final LayerProperties layerProperties,
            final String hostname,
            final FeatureType featureType,
            final Crs crs) {
        logger.info("initialising WFSFeatureFactory with hostname: '" + hostname + "'");
        this.layerProperties = layerProperties;
        this.hostname = hostname;
        this.featureType = featureType;
        this.crs = crs;
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
            final String postString = facade.setGetFeatureBoundingBox(query, bbox, featureType, getCrs().getCode());
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
        return new WFSFeature();
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
    public int getFeatureCount(final BoundingBox bb) {
        final XBoundingBox bbox = new XBoundingBox(bb.getX1(),
                bb.getY1(),
                bb.getX2(),
                bb.getY2(),
                getCrs().getCode(),
                getCrs().isMetric());
        final WFSFacade facade = featureType.getWFSCapabilities().getServiceFacade();
        final Element queryElement = facade.getGetFeatureQuery(featureType);
        final String query = FeatureServiceUtilities.elementToString(queryElement);
        final String postString = facade.setGetFeatureBoundingBox(query, bbox, featureType, getCrs().getCode(), true);
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
