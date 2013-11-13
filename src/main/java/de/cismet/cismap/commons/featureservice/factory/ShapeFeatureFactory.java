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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;

import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.style.se.unevaluated.Style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URI;

import java.nio.charset.Charset;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   pascal
 * @version  $Revision$, $Date$
 */
public class ShapeFeatureFactory extends DegreeFeatureFactory<ShapeFeature, String>
        implements CachingFeatureFactory<ShapeFeature, String> {

    //~ Instance fields --------------------------------------------------------

    protected int maxCachedFeatureCount = Integer.MAX_VALUE;
    protected URI documentURI;
    protected ShapeFile shapeFile;
    protected boolean initialised = false;
    protected STRtree degreeFeaturesTree = null;
    // private Feature[] tempFeatureCollection;
    // private int currentProgress = 0;
    protected Vector<FeatureServiceAttribute> featureServiceAttributes;
//    private Geometry extend;
    private boolean noGeometryRecognised = false;
    private boolean errorInGeometryFound = false;
    private Crs shapeCrs = null;
    private int shapeSrid = 0;
    private Crs crs = CismapBroker.getInstance().getSrs();
    private Geometry envelope;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFeatureFactory object.
     *
     * @param   layerProperties        DOCUMENT ME!
     * @param   documentURL            DOCUMENT ME!
     * @param   maxCachedFeatureCount  DOCUMENT ME!
     * @param   workerThread           DOCUMENT ME!
     * @param   styles                 DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFeatureFactory(final LayerProperties layerProperties,
            final URI documentURL,
            final int maxCachedFeatureCount,
            final SwingWorker workerThread,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) throws Exception {
        this.layerProperties = layerProperties;
        this.documentURI = documentURL;
        this.maxCachedFeatureCount = maxCachedFeatureCount;
        this.styles = styles;

        try {
            this.parseShapeFile(workerThread);
            this.initialised = true;
        } catch (Exception ex) {
            logger.error("SW[" + workerThread + "]: error parsing shape file", ex);
            if (DEBUG && (shapeFile != null)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(shapeFile.getFileMBR());
                }
            }
            this.cleanup();
        }
    }

    /**
     * Creates a new ShapeFeatureFactory object.
     *
     * @param  shpff  DOCUMENT ME!
     */
    protected ShapeFeatureFactory(final ShapeFeatureFactory shpff) {
        super(shpff);
        this.maxCachedFeatureCount = shpff.maxCachedFeatureCount;
        this.documentURI = shpff.documentURI;
        this.shapeFile = shpff.shapeFile;
        this.degreeFeaturesTree = shpff.degreeFeaturesTree;
        this.featureServiceAttributes = new Vector(shpff.featureServiceAttributes);
        this.initialised = shpff.initialised;
        this.crs = shpff.crs;
        this.shapeCrs = shpff.shapeCrs;
        this.shapeSrid = shpff.shapeSrid;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected ShapeFeature createFeatureInstance(final Feature degreeFeature, final int index) throws Exception {
        final String filename = new File(documentURI).getName();
        layerName = filename;
        final ShapeFeature shapeFeature;

        shapeFeature = new ShapeFeature(filename, getStyle(filename));

        // auto generate Ids!
        shapeFeature.setId(index);

        try {
            shapeFeature.setGeometry(JTSAdapter.export(degreeFeature.getGeometryPropertyValues()[geometryIndex]));
        } catch (Exception e) {
            logger.error("Error while parsing the geometry of a feature from a shape file.", e);
            if (degreeFeature.getGeometryPropertyValues().length == 0) {
                noGeometryRecognised = true;
            } else {
                errorInGeometryFound = true;
            }
            shapeFeature.setGeometry(JTSAdapter.export(degreeFeature.getDefaultGeometryPropertyValue()));
        }

        if (shapeFeature.getGeometry() != null) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("srid of feature = " + shapeFeature.getGeometry().getSRID());
//            }
            shapeFeature.getGeometry().setSRID(shapeSrid);
            // store the feature in the spatial index structure
            final Geometry geom = shapeFeature.getGeometry();
            this.degreeFeaturesTree.insert(geom.getEnvelopeInternal(), shapeFeature);

            if (envelope == null) {
                envelope = geom.getEnvelope();
            } else {
                envelope = envelope.getEnvelope().union(geom.getEnvelope());
            }
        }

        return shapeFeature;
    }

    /**
     * DOCUMENT ME!
     */
    protected synchronized void cleanup() {
        if (this.shapeFile != null) {
            shapeFile.close();
            shapeFile = null;
            System.gc();
        }
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Charset getCharsetDefinition() {
        Charset cs = null;
        String cpgFilename = null;
        File cpgFile = null;

        if (this.documentURI.getPath().endsWith(".shp")) {
            cpgFilename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            cpgFilename = this.documentURI.getPath();
        }

        cpgFile = new File(cpgFilename + ".cpg");
        if (!cpgFile.exists()) {
            cpgFile = new File(cpgFilename + ".CPG");
        }

        try {
            if (cpgFile.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(cpgFile));
                final String csName = br.readLine();
                if (logger.isDebugEnabled()) {
                    logger.debug("cpg file with charset " + csName + " found");
                }
                if ((csName != null) && Charset.isSupported(csName)) {
                    cs = Charset.forName(csName);
                } else {
                    logger.warn("The given charset is not supported. Charset: " + csName);
                }
                br.close();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No cpg file found.");
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading the cpg file.");
        }

        return cs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   workerThread  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected synchronized void parseShapeFile(final SwingWorker workerThread) throws Exception {
        logger.info("SW[" + workerThread + "]: initialising ShapeFeatureFactory with document: '" + documentURI + "'");
        final long start = System.currentTimeMillis();
        envelope = null;
        if (shapeCrs == null) {
            shapeCrs = CismapBroker.getInstance().getSrs();
            shapeSrid = CrsTransformer.extractSridFromCrs(shapeCrs.getCode());
        }

        final Charset cs = getCharsetDefinition();

        if (this.documentURI.getPath().endsWith(".shp")) {
            shapeFile = new ShapeFile(this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4),
                    cs);
        } else {
            shapeFile = new ShapeFile(this.documentURI.getPath(), cs);
        }

        final int max = shapeFile.getRecordNum();
        if (DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug("SW[" + workerThread + "]: " + max + " features found in shape file");
            }
        }
//        if (shapeFile.getRecordNum() > this.maxCachedFeatureCount) {
//            logger.error("SW[" + workerThread + "]: number of features in shape file (" + shapeFile.getRecordNum()
//                        + ") exceeds maximum of supported features (" + this.maxCachedFeatureCount + ")");
//            max = this.maxCachedFeatureCount;
//        }
        if (max == 0) {
            logger.error("SW[" + workerThread + "]: no features found in shape file");
            throw new Exception("no features found in shape file '" + this.documentURI + "'");
        }

        // this.tempFeatureCollection = new Feature[max];
        if (max >= 4) {
            this.degreeFeaturesTree = new STRtree(max);
        } else {
            this.degreeFeaturesTree = new STRtree();
        }

        // parse features ........................................................
        int currentProgress = 0;
        int newProgress;
        for (int i = 0; i < max; i++) {
            final Feature degreeFeature = shapeFile.getFeatureByRecNo(i + 1);
            if (i == 0) {
                final FeatureType type = degreeFeature.getFeatureType();
                logger.info("SW[" + workerThread + "]: creating " + type.getProperties().length
                            + " featureServiceAttributes from first parsed degree feature");
                featureServiceAttributes = new Vector(type.getProperties().length);
                for (final PropertyType pt : type.getProperties()) {
                    // ToDo was ist wenn zwei Geometrien dabei sind
                    featureServiceAttributes.add(
                        new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true));
                }
            }

            // create Feature instance fuegt die features dem strtree hinzu
            final ShapeFeature featureServiceFeature = this.createFeatureInstance(degreeFeature, i);
            this.initialiseFeature(featureServiceFeature, degreeFeature, false, i);
            // this.tempFeatureCollection[i] = shapeFile.getFeatureByRecNo(i + 1);

            newProgress = (int)((double)i / (double)max * 100d);
            if ((workerThread != null) && ((newProgress % 5) == 0) && (newProgress > currentProgress)
                        && (newProgress >= 5)) {
                // set to progress to -1 (indeterminate progress bar)
                currentProgress = (newProgress <= 100) ? newProgress : -1;
                if (DEBUG) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SW[" + workerThread + "]: parsing progress: " + currentProgress + "%");
                    }
                }
                workerThread.firePropertyChange("progress", currentProgress - 5, currentProgress);
            }
        }

//        CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(new XBoundingBox(extend));
        this.cleanup();
        if (logger.isDebugEnabled()) {
            logger.debug("parsing, converting and initialising " + max + " shape features took "
                        + (System.currentTimeMillis() - start) + " ms");
        }
    }

    @Override
    public synchronized void flush() {
        logger.warn("flushing cached features");
        this.lastCreatedfeatureVector.clear();
        System.gc();
    }

    @Override
    public int getMaxCachedFeatureCount() {
        if (this.degreeFeaturesTree != null) {
            return this.degreeFeaturesTree.getNodeCapacity();
        } else {
            return this.maxCachedFeatureCount;
        }
    }

    @Override
    public void setMaxCachedFeatureCount(final int maxCachedFeatureCount) {
//        this.maxCachedFeatureCount = maxCachedFeatureCount;
    }

    /**
     * Get the value of documentURL.
     *
     * @return  the value of documentURL
     */
    public URI getDocumentURI() {
        return documentURI;
    }

    /**
     * Set the value of documentURL.
     *
     * @param  documentURI  new value of documentURL
     */
    public synchronized void setDocumentURI(final URI documentURI) {
        this.documentURI = documentURI;
    }

    @Override
    public synchronized List<ShapeFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread) throws TooManyFeaturesException, Exception {
        return createFeatures(query, boundingBox, workerThread, 0, 0, null);
    }

    @Override
    public synchronized Vector<FeatureServiceAttribute> createAttributes(final SwingWorker workerThread)
            throws TooManyFeaturesException, Exception {
        if ((this.featureServiceAttributes == null) || (this.featureServiceAttributes.size() == 0)) {
            logger.warn("SW[" + workerThread + "]: Factory not correctly initialised, parsing shape file");
            this.parseShapeFile(workerThread);
        }

        if ((this.featureServiceAttributes == null) || (this.featureServiceAttributes.size() == 0)) {
            logger.error("SW[" + workerThread + "]: no attributes could be found in shape file");
            throw new Exception("no attributes could be found in shape file '" + this.documentURI + "'");
        }

        return this.featureServiceAttributes;
    }

    @Override
    protected boolean isGenerateIds() {
        return true;
    }
//  public static void main(String args[])
//  {
//    BasicConfigurator.configure();
//
//    try
//    {
//      ShapeFeatureFactory shapeFeatureFactory = new ShapeFeatureFactory(new DefaultLayerProperties(), new URL("file:///P:/Street3D/SHAPE/mini.shp"));
//      shapeFeatureFactory.logger.setLevel(org.apache.log4j.Level.ALL);
//      shapeFeatureFactory.logger.info("OK");
//    } catch (Throwable t)
//    {
//      t.printStackTrace();
//    }
//  }

    @Override
    public ShapeFeatureFactory clone() {
        return new ShapeFeatureFactory(this);
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
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final Crs crs) {
        this.crs = crs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the envelope of the currently loaded shape file
     */
    public Geometry getEnvelope() {
        envelope.setSRID(shapeSrid);
        return envelope;
    }

    @Override
    public int getFeatureCount(final BoundingBox bb) {
        return this.degreeFeaturesTree.size();
    }

    @Override
    public synchronized List<ShapeFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
        if (!this.initialised) {
            logger.warn("SW[" + workerThread + "]: Factory not correclty initialised, parsing shape file");
            this.parseShapeFile(workerThread);
            this.initialised = true;

            // check if thread is canceled .........................................
            if (this.checkCancelled(workerThread, " initialisation")) {
                return null;
            }
            // check if thread is canceled .........................................
        }

        // this.lastCreatedfeatureVector.clear();

        final long start = System.currentTimeMillis();
        final Coordinate[] polyCords = new Coordinate[5];
        polyCords[0] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
        polyCords[1] = new Coordinate(boundingBox.getX1(), boundingBox.getY2());
        polyCords[2] = new Coordinate(boundingBox.getX2(), boundingBox.getY2());
        polyCords[3] = new Coordinate(boundingBox.getX2(), boundingBox.getY1());
        polyCords[4] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
        // The GeometryFactory must use the same srid as the elements in the deegreeFeaturesTree
        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(crs.getCode()));
        Polygon boundingPolygon = geomFactory.createPolygon(geomFactory.createLinearRing(polyCords), null);

        boundingPolygon = (Polygon)CrsTransformer.transformToGivenCrs(boundingPolygon, shapeCrs.getCode());
        List<ShapeFeature> selectedFeatures = this.degreeFeaturesTree.query(boundingPolygon.getEnvelopeInternal());
        if (logger.isDebugEnabled()) {
            logger.debug("feature crs: " + shapeCrs.getCode() + " features " + selectedFeatures.size()
                        + " boundingbox: "
                        + boundingPolygon.getEnvelopeInternal());
        }
        // check if thread is canceled .........................................
        if (this.checkCancelled(workerThread, " quering spatial index structure")) {
            return null;
        }
        // check if thread is canceled .........................................

        logger.info("SW[" + workerThread + "]: " + selectedFeatures.size()
                    + " features selected by bounding box out of " + this.degreeFeaturesTree.size()
                    + " in spatial index");
        if (DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug("SW[" + workerThread + "]: quering spatial index for bounding box took "
                            + (System.currentTimeMillis() - start) + " ms");
            }
        }

        if (selectedFeatures.size() > this.getMaxFeatureCount()) {
            throw new TooManyFeaturesException("features in selected area " + selectedFeatures.size()
                        + " exceeds max feature count " + this.getMaxFeatureCount());
        } else if (selectedFeatures.size() == 0) {
            logger.warn("SW[" + workerThread + "]: no features found in selected bounding box");
            return null;
        }

        if ((orderBy != null) && (orderBy.length > 0)) {
            sortFeatureList(selectedFeatures, orderBy);
        }

        if (offset > 0) {
            selectedFeatures = selectedFeatures.subList(offset, selectedFeatures.size());
        }

        if ((limit > 0) && (selectedFeatures.size() > limit)) {
            selectedFeatures = selectedFeatures.subList(0, limit);
        }

        this.reEvaluteExpressions(selectedFeatures, workerThread);

        // check if thread is canceled .........................................
        if (this.checkCancelled(workerThread, " saving LastCreatedFeatures ")) {
            return null;
        }
        // check if thread is canceled .........................................

        this.updateLastCreatedFeatures(selectedFeatures);
        // set the SLDs for features
        final List<Style> style = getStyle(layerName);

        if ((style != null) && (selectedFeatures != null)) {
            for (final ShapeFeature f : selectedFeatures) {
                f.setSLDStyles(style);
            }
        }
        return new Vector<ShapeFeature>(selectedFeatures);
    }
}
