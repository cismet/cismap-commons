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

import org.deegree.io.rtree.HyperBoundingBox;
import org.deegree.io.rtree.HyperPoint;
import org.deegree.io.rtree.RTree;
import org.deegree.io.rtree.RTreeException;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.JTSAdapter;

import org.openide.util.Exceptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URI;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.features.ShapeInfo;
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
    // private Feature[] tempFeatureCollection;
    // private int currentProgress = 0;
    protected Vector<FeatureServiceAttribute> featureServiceAttributes;
//    private Geometry extend;
    private boolean noGeometryRecognised = false;
    private boolean errorInGeometryFound = false;
    private Crs shapeCrs = null;
    private int shapeSrid = 0;
    private Crs crs = CismapBroker.getInstance().getSrs();
    private org.deegree.model.spatialschema.Envelope envelope;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFeatureFactory object.
     *
     * @param   layerProperties        DOCUMENT ME!
     * @param   documentURL            DOCUMENT ME!
     * @param   maxCachedFeatureCount  DOCUMENT ME!
     * @param   workerThread           DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFeatureFactory(final LayerProperties layerProperties,
            final URI documentURL,
            final int maxCachedFeatureCount,
            final SwingWorker workerThread) throws Exception {
        this.layerProperties = layerProperties;
        this.documentURI = documentURL;
        this.maxCachedFeatureCount = maxCachedFeatureCount;

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
        this.featureServiceAttributes = new Vector(shpff.featureServiceAttributes);
        this.initialised = shpff.initialised;
        this.crs = shpff.crs;
        this.shapeCrs = shpff.shapeCrs;
        this.shapeSrid = shpff.shapeSrid;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected ShapeFeature createFeatureInstance(final Feature degreeFeature, final int index) throws Exception {
        //dummy method
        return null;
    }
    
    
    protected ShapeFeature createFeatureInstance(final Feature degreeFeature, final ShapeInfo shapeInfo, final int index) throws Exception {
        final String filename = new File(documentURI).getName();
//        layerName = filename;
        final ShapeFeature shapeFeature;

        shapeFeature = new ShapeFeature(shapeInfo);
//        shapeFeature = new ShapeFeature(shapeInfo, getStyle(filename));

        // auto generate Ids!
        shapeFeature.setId(index);

        return shapeFeature;
    }

    @Override
    protected void initialiseFeature(ShapeFeature featureServiceFeature, Feature degreeFeature, boolean evaluateExpressions, int index) throws Exception {
        // perform standard initialisation
        featureServiceFeature.setLayerProperties(this.getLayerProperties());

        if (evaluateExpressions) {
            this.evaluateExpressions(featureServiceFeature, index);
        }
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
        if (shapeCrs == null) {
            shapeCrs = CismapBroker.getInstance().getSrs();
            shapeSrid = CrsTransformer.extractSridFromCrs(shapeCrs.getCode());
        }

        shapeFile = getShapeFile();

        final Feature degreeFeature = shapeFile.getFeatureByRecNo(1);
        final FeatureType type = degreeFeature.getFeatureType();
        logger.info("SW[" + workerThread + "]: creating " + type.getProperties().length
                    + " featureServiceAttributes from first parsed degree feature");
        featureServiceAttributes = new Vector(type.getProperties().length);
        for (final PropertyType pt : type.getProperties()) {
            // ToDo was ist wenn zwei Geometrien dabei sind
            featureServiceAttributes.add(
                new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true));
        }

        envelope = shapeFile.getFileMBR();
        // create an index file, if it does not alreay exists
        int currentProgress = 0;
        int newProgress = 0;
        String filename = null;

        if (this.documentURI.getPath().endsWith(".shp")) {
            filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            filename = this.documentURI.getPath();
        }

        try {
            if (!shapeFile.hasRTreeIndex()) {
                final int features = shapeFile.getRecordNum();
                final RTree rtree = new RTree(2, 11, filename + ".rti");
                for (int i = 1; i < (features + 1); i++) {
                    final Feature feature = shapeFile.getFeatureByRecNo(i);

                    final org.deegree.model.spatialschema.Geometry[] geometries = feature.getGeometryPropertyValues();

                    if (geometries.length == 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("no geometries at recno" + i);
                        }
                        continue;
                    }

                    org.deegree.model.spatialschema.Envelope envelope = null;
                    // TODO: deal with more than one geometry; handle geometry=null (allowed
                    // in shapefile)
                    envelope = (feature.getDefaultGeometryPropertyValue()).getEnvelope();
                    if (envelope == null) { // assume a Point-geometry
                        if (geometries[0] instanceof org.deegree.model.spatialschema.Point) {
                            final org.deegree.model.spatialschema.Point pnt = (org.deegree.model.spatialschema.Point)
                                geometries[0];
                            envelope = org.deegree.model.spatialschema.GeometryFactory.createEnvelope(pnt.getX(),
                                    pnt.getY(),
                                    pnt.getX(),
                                    pnt.getY(),
                                    null);
                        }
                    }
                    final HyperBoundingBox box = new HyperBoundingBox(
                            new HyperPoint(envelope.getMin().getAsArray()),
                            new HyperPoint(envelope.getMax().getAsArray()));
                    rtree.insert(new Integer(i), box);

                    // refresh progress bar
                    newProgress = (int)((double)i / (double)features * 100d);
                    if ((workerThread != null) && ((newProgress % 5) == 0) && (newProgress > currentProgress)
                                && (newProgress >= 5)) {
                        // set to progress to -1 (indeterminate progress bar)
                        currentProgress = (newProgress <= 100) ? newProgress : -1;

                        workerThread.firePropertyChange("progress", currentProgress - 5, currentProgress);
                    }
                }

                rtree.close();
            }
        } catch (final RTreeException e) {
            logger.error("The index file cannot be created. The corresponding folder is possibly write protected.", e);
        }
        initialised = true;

        this.cleanup();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private ShapeFile getShapeFile() throws IOException {
        cleanup();
        final Charset cs = getCharsetDefinition();
        String filename = null;

        if (this.documentURI.getPath().endsWith(".shp")) {
            filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            filename = this.documentURI.getPath();
        }

        shapeFile = new ShapeFile(filename, cs);

        return shapeFile;
    }

    @Override
    public synchronized void flush() {
        logger.warn("flushing cached features");
        this.lastCreatedfeatureVector.clear();
        System.gc();
    }

    /**
     * is not used and not usable.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getMaxCachedFeatureCount() {
        try {
            return getShapeFile().getRecordNum();
        } catch (IOException ex) {
            logger.error("Cannot open the shape file", ex);
            return 0;
        } finally {
            cleanup();
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
        try {
            if (envelope == null) {
                envelope = getShapeFile().getFileMBR();
                cleanup();
            }
            final Coordinate[] polyCords = new Coordinate[5];
            polyCords[0] = new Coordinate(envelope.getMin().getX(), envelope.getMin().getY());
            polyCords[1] = new Coordinate(envelope.getMin().getX(), envelope.getMax().getY());
            polyCords[2] = new Coordinate(envelope.getMax().getX(), envelope.getMax().getY());
            polyCords[3] = new Coordinate(envelope.getMax().getX(), envelope.getMin().getY());
            polyCords[4] = new Coordinate(envelope.getMin().getX(), envelope.getMin().getY());
            // The GeometryFactory must use the same srid as the elements in the deegreeFeaturesTree
            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    shapeSrid);
            final Polygon boundingPolygon = geomFactory.createPolygon(geomFactory.createLinearRing(polyCords), null);

            return boundingPolygon.getEnvelope();
        } catch (Exception e) {
            logger.error("Error whie reading Shape file", e);
        }

        return null;
    }

    @Override
    public int getFeatureCount(final BoundingBox bb) {
        try {
            final Coordinate[] polyCords = new Coordinate[5];
            polyCords[0] = new Coordinate(bb.getX1(), bb.getY1());
            polyCords[1] = new Coordinate(bb.getX1(), bb.getY2());
            polyCords[2] = new Coordinate(bb.getX2(), bb.getY2());
            polyCords[3] = new Coordinate(bb.getX2(), bb.getY1());
            polyCords[4] = new Coordinate(bb.getX1(), bb.getY1());
            // The GeometryFactory must use the same srid as the elements in the deegreeFeaturesTree
            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CrsTransformer.extractSridFromCrs(crs.getCode()));
            Polygon boundingPolygon = geomFactory.createPolygon(geomFactory.createLinearRing(polyCords), null);

            boundingPolygon = (Polygon)CrsTransformer.transformToGivenCrs(boundingPolygon, shapeCrs.getCode());
            // List<ShapeFeature> selectedFeatures =
            // this.degreeFeaturesTree.query(boundingPolygon.getEnvelopeInternal());

            return getShapeFile().getGeoNumbersByRect(JTSAdapter.wrap(boundingPolygon).getEnvelope()).length;
        } catch (final Exception e) {
            logger.error("Error while determining the feature count", e);
        } finally {
            cleanup();
        }

        return 0;
    }

    @Override
    public synchronized List<ShapeFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
        try {
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
            
            final Charset cs = getCharsetDefinition();
            String filename = null;

            if (this.documentURI.getPath().endsWith(".shp")) {
                filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
            } else {
                filename = this.documentURI.getPath();
            }

            ShapeFile persShapeFile = getShapeFile();;            
            
            shapeFile = null;
            final int[] recordNumbers = shapeFile.getGeoNumbersByRect(JTSAdapter.wrap(boundingPolygon).getEnvelope());

            List<ShapeFeature> selectedFeatures = new ArrayList<ShapeFeature>(recordNumbers.length);
            ShapeInfo info = new ShapeInfo(filename, persShapeFile, shapeSrid);

            if (recordNumbers.length < 15000) {
                for (final int record : recordNumbers) {
                    final Feature f = persShapeFile.getFeatureByRecNo(record);
                    final ShapeFeature featureServiceFeature = createFeatureInstance(f, info, record);
                    this.initialiseFeature(featureServiceFeature, f, false, record);
                    selectedFeatures.add(featureServiceFeature);
                }
            }

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
                        + " features selected by bounding box out of " + this.shapeFile.getRecordNum()
                        + " in spatial index");
            if (logger.isDebugEnabled()) {
                logger.debug("SW[" + workerThread + "]: quering spatial index for bounding box took "
                            + (System.currentTimeMillis() - start) + " ms");
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
            return new Vector<ShapeFeature>(selectedFeatures);
        } finally {
            cleanup();
        }
    }
}
