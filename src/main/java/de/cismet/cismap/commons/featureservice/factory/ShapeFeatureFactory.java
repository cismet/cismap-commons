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
import org.deegree.io.shpapi.shape_new.ShapeFileReader;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.style.se.unevaluated.Style;

import org.openide.util.NbBundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URI;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.exceptions.ShapeFileImportAborted;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.features.ShapeInfo;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.util.CrsDeterminer;

import static de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory.DEBUG;

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
    private String shapeCrs = null;
    private Crs crs = CismapBroker.getInstance().getSrs();
    private org.deegree.model.spatialschema.Envelope envelope;
    private FeatureCollection fc = null;
    private String filename;
    private String geometryType = AbstractFeatureService.UNKNOWN;
    private int lastFreeId = -1;
    private ShapeInfo info = null;

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
        this(layerProperties, documentURL, maxCachedFeatureCount, workerThread, styles, null);
    }

    /**
     * Creates a new ShapeFeatureFactory object.
     *
     * @param   layerProperties        DOCUMENT ME!
     * @param   documentURL            DOCUMENT ME!
     * @param   maxCachedFeatureCount  DOCUMENT ME!
     * @param   workerThread           DOCUMENT ME!
     * @param   styles                 DOCUMENT ME!
     * @param   shapeCrs               DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFeatureFactory(final LayerProperties layerProperties,
            final URI documentURL,
            final int maxCachedFeatureCount,
            final SwingWorker workerThread,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles,
            final String shapeCrs) throws Exception {
        this.layerProperties = layerProperties;
        this.documentURI = documentURL;
        this.maxCachedFeatureCount = maxCachedFeatureCount;
        this.styles = styles;
        this.shapeCrs = shapeCrs;

        if (shapeCrs != null) {
            this.featureSrid = CrsTransformer.extractSridFromCrs(shapeCrs);
        }

        try {
            this.parseShapeFile(workerThread);
            this.initialised = true;
        } catch (ShapeFileImportAborted e) {
            // this exception will be handled in the ShapeFileFeatureService
            throw e;
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
        this.fc = shpff.fc;
        this.filename = shpff.filename;
        this.envelope = shpff.envelope;
        this.geometryType = shpff.geometryType;
        this.noGeometryRecognised = shpff.noGeometryRecognised;
        this.errorInGeometryFound = shpff.errorInGeometryFound;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected ShapeFeature createFeatureInstance(final Feature degreeFeature, final int index) throws Exception {
        // dummy method
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   degreeFeature  DOCUMENT ME!
     * @param   shapeInfo      DOCUMENT ME!
     * @param   index          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected ShapeFeature createFeatureInstance(final Feature degreeFeature,
            final ShapeInfo shapeInfo,
            final int index) throws Exception {
        layerName = filename;
        final ShapeFeature shapeFeature = new ShapeFeature(shapeInfo, getStyle(filename));

        // auto generate Ids!
        shapeFeature.setId(index);

        return shapeFeature;
    }

    @Override
    protected void initialiseFeature(final ShapeFeature featureServiceFeature,
            final Feature degreeFeature,
            final boolean evaluateExpressions,
            final int index) throws Exception {
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
        String cpgFilename;
        File cpgFile;

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
     * @return  DOCUMENT ME!
     */
    public String getSldDefinition() {
        String sldFilename;
        File sldFile;

        if (this.documentURI.getPath().endsWith(".shp")) {
            sldFilename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            sldFilename = this.documentURI.getPath();
        }

        sldFile = new File(sldFilename + ".sld");
        if (!sldFile.exists()) {
            sldFile = new File(sldFilename + ".SLD");
        }
        BufferedReader br = null;
        try {
            if (sldFile.exists()) {
                final StringBuilder sld = new StringBuilder();
                br = new BufferedReader(new FileReader(sldFile));
                String tmp = null;

                while ((tmp = br.readLine()) != null) {
                    sld.append(tmp).append("\n");
                }
                final String sldString = sld.toString();

                if (logger.isDebugEnabled()) {
                    logger.debug("sld file with content " + sldString + " found");
                }
                if (!sldString.equals("")) {
                    return sldString;
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No sld file found.");
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading the sld file.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    logger.error("Cannot close sld file", ex);
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
     * @throws  Exception               DOCUMENT ME!
     * @throws  ShapeFileImportAborted  DOCUMENT ME!
     */
    protected synchronized void parseShapeFile(final SwingWorker workerThread) throws Exception {
        filename = new File(documentURI).getName();
        shapeFile = getShapeFile();
        envelope = shapeFile.getFileMBR();

        if (getShapeCrs() == null) {
            setShapeCrs(determineShapeCrs());

            if (getShapeCrs() == null) {
                throw new ShapeFileImportAborted();
            }
            featureSrid = CrsTransformer.extractSridFromCrs(getShapeCrs());
        }

        final Feature degreeFeature = shapeFile.getFeatureByRecNo(1);
        final FeatureType type = degreeFeature.getFeatureType();
        logger.info("SW[" + workerThread + "]: creating " + type.getProperties().length
                    + " featureServiceAttributes from first parsed degree feature");
        featureServiceAttributes = new Vector(type.getProperties().length);

        for (final PropertyType pt : type.getProperties()) {
            featureServiceAttributes.add(
                new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true));
        }

        // create an index file, if it does not alreay exists
        int currentProgress = 0;
        int newProgress;
        String filename;

        if (this.documentURI.getPath().endsWith(".shp")) {
            filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            filename = this.documentURI.getPath();
        }
        createFeatureCollectionIfRequired();

        try {
            if (!shapeFile.hasRTreeIndex()) {
                final int features = shapeFile.getRecordNum();
                final RTree rtree = new RTree(2, 11, filename + ".rti");
                for (int i = 1; i < (features + 1); i++) {
                    Feature feature = null;

                    if (fc != null) {
                        feature = fc.getFeature(i - 1);
                    } else {
                        feature = shapeFile.getFeatureByRecNo(i);
                    }

                    final org.deegree.model.spatialschema.Geometry[] geometries = feature.getGeometryPropertyValues();

                    if (geometries.length == 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("no geometries at recno" + i);
                        }
                        continue;
                    }

                    org.deegree.model.spatialschema.Envelope envelope = null;
                    // TODO: handle geometry=null (allowed in shapefile)
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
        determineGeometryType();
        initialised = true;

        this.cleanup();
    }

    /**
     * Determines the crs of the corresponding prj file.
     *
     * @return  the crs of the corresponding prj file or null, if the initialisation of the shape file should be
     *          cancelled.
     */
    private String determineShapeCrs() {
        String prjFilename;
        File prjFile;

        if (this.documentURI.getPath().endsWith(".shp")) {
            prjFilename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            prjFilename = this.documentURI.getPath();
        }

        prjFile = new File(prjFilename + ".prj");
        if (!prjFile.exists()) {
            prjFile = new File(prjFilename + ".PRJ");
        }

        try {
            if (prjFile.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(prjFile));
                final String crsDefinition = br.readLine();
                br.close();

                if (crsDefinition != null) {
                    final String epsg = CrsDeterminer.getEpsgCode(crsDefinition);

                    if (epsg != null) {
                        return epsg;
                    } else {
                        return CismapBroker.getInstance().getSrs().getCode();
                    }
                } else {
                    logger.warn("The prj file is empty.");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No prj file found.");
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading the prj file.", e);
        }

        if (featureSrid == null) {
            // the featureSrid must be set before the getEnvelope method will be called.
            featureSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
        }
        final BoundingBox currentBBox = CismapBroker.getInstance()
                    .getMappingComponent()
                    .getCurrentBoundingBoxFromCamera();

        if (getEnvelope().intersects(currentBBox.getGeometry(featureSrid))) {
            return CismapBroker.getInstance().getSrs().getCode();
        } else {
            // Ask the user, what crs should be used
            final List<Crs> crsList = CismapBroker.getInstance().getMappingComponent().getCrsList();
            final List<Object> definedMappings = new ArrayList<Object>();

            for (final Crs tmpCrs : crsList) {
                if (tmpCrs.hasEsriDefinition()) {
                    definedMappings.add(new CrsWrapper(tmpCrs));
                }
            }

            final Object userAnswer = JOptionPane.showInputDialog(CismapBroker.getInstance().getMappingComponent(),
                    NbBundle.getMessage(ShapeFeatureFactory.class, "ShapeFeatureFactory.determineShapeCrs.message"),
                    NbBundle.getMessage(ShapeFeatureFactory.class, "ShapeFeatureFactory.determineShapeCrs.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    null,
                    definedMappings.toArray(),
                    definedMappings.get(0));

            if (userAnswer instanceof CrsWrapper) {
                return ((CrsWrapper)userAnswer).getCrs().getCode();
            } else {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void determineGeometryType() {
        try {
            final ShapeFile persShapeFile = getShapeFile();
            final ShapeInfo info = new ShapeInfo(filename, persShapeFile, featureSrid, fc);

            final ShapeFeature featureServiceFeature = createFeatureInstance(null, info, 1);
            this.initialiseFeature(featureServiceFeature, null, false, 1);
            geometryType = featureServiceFeature.getGeometry().getGeometryType();
        } catch (Exception e) {
            logger.error("Cannot determine the geometry type of a shape file.", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public ShapeFile getShapeFile() throws IOException {
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

    /**
     * DOCUMENT ME!
     */
    public void refreshData() {
        try {
            if (info != null) {
                if (info.getFile() != null) {
                    info.getFile().close();
                }
                info.setFile(getShapeFile());
                if (info.getFc() != null) {
                    createFeatureCollectionIfRequired();
                    info.setFc(fc);
                }
            }
        } catch (Exception e) {
            logger.error("Error while refreshing data", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createFeatureCollectionIfRequired() throws Exception {
        String filename;

        if (this.documentURI.getPath().endsWith(".shp")) {
            filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
        } else {
            filename = this.documentURI.getPath();
        }

        final ShapeFileReader reader = new ShapeFileReader(filename);
        final int shapeType = reader.getShapeType();

        if ((shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.MULTIPOINTM)
                    || (shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.POLYLINEM)
                    || (shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.POLYGONM)
                    || (shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.POINTM)
                    || (shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.POLYGONZ)
                    || (shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.POLYLINEZ)
                    || (shapeType == org.deegree.io.shpapi.shape_new.ShapeFile.POINTZ)) {
            final org.deegree.io.shpapi.shape_new.ShapeFile shapeFromReader = reader.read();
            fc = shapeFromReader.getFeatureCollection();
        }
    }

    @Override
    public synchronized void flush() {
        logger.warn("flushing cached features");
        this.lastCreatedfeatureVector.clear();
        this.lastGeom = null;
        this.lastQuery = null;
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
        try {
            parseShapeFile(null);
        } catch (Exception e) {
            logger.error("Errro while parsing shape file", e);
        }
    }

    @Override
    public synchronized List<ShapeFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread) throws TooManyFeaturesException, Exception {
        return createFeatures_internal(query, boundingBox, workerThread, 0, 0, null, true);
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
                    featureSrid);
            final Polygon boundingPolygon = geomFactory.createPolygon(geomFactory.createLinearRing(polyCords), null);

            return boundingPolygon.getEnvelope();
        } catch (Exception e) {
            logger.error("Error whie reading Shape file", e);
        }

        return null;
    }

    @Override
    public int getFeatureCount(final String query, final BoundingBox bb) {
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

            boundingPolygon = (Polygon)CrsTransformer.transformToGivenCrs(boundingPolygon, getShapeCrs());
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
        return createFeatures_internal(query, boundingBox, workerThread, offset, limit, orderBy, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   query              DOCUMENT ME!
     * @param   boundingBox        DOCUMENT ME!
     * @param   workerThread       DOCUMENT ME!
     * @param   offset             DOCUMENT ME!
     * @param   limit              DOCUMENT ME!
     * @param   orderBy            DOCUMENT ME!
     * @param   saveAsLastCreated  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    private synchronized List<ShapeFeature> createFeatures_internal(final String query,
            BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy,
            final boolean saveAsLastCreated) throws TooManyFeaturesException, Exception {
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

            List<ShapeFeature> selectedFeatures;
            final long start = System.currentTimeMillis();
            final Coordinate[] polyCords = new Coordinate[5];
            if (boundingBox == null) {
                boundingBox = new XBoundingBox(getEnvelope());
            }
            polyCords[0] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
            polyCords[1] = new Coordinate(boundingBox.getX1(), boundingBox.getY2());
            polyCords[2] = new Coordinate(boundingBox.getX2(), boundingBox.getY2());
            polyCords[3] = new Coordinate(boundingBox.getX2(), boundingBox.getY1());
            polyCords[4] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
            // The GeometryFactory must use the same srid as the elements in the deegreeFeaturesTree
            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CrsTransformer.extractSridFromCrs(crs.getCode()));
            Polygon boundingPolygon = geomFactory.createPolygon(geomFactory.createLinearRing(polyCords), null);

            boundingPolygon = (Polygon)CrsTransformer.transformToGivenCrs(boundingPolygon, getShapeCrs());
            if (this.checkCancelled(workerThread, " quering spatial index structure")) {
                return null;
            }

            if (featuresAlreadyInMemory(boundingPolygon, query)) {
                selectedFeatures = createFeaturesFromMemory(query, boundingPolygon);
            } else {
                String filename = null;

                if (this.documentURI.getPath().endsWith(".shp")) {
                    filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
                } else {
                    filename = this.documentURI.getPath();
                }

                if (info == null) {
                    info = new ShapeInfo(filename, getShapeFile(), featureSrid, fc);
                }
                final ShapeFile persShapeFile = info.getFile();

                shapeFile = null;
                final int[] recordNumbers = persShapeFile.getGeoNumbersByRect(JTSAdapter.wrap(boundingPolygon)
                                .getEnvelope());

                if (recordNumbers == null) {
                    return new Vector<ShapeFeature>();
                }

                if (this.checkCancelled(workerThread, " quering spatial index structure")) {
                    return null;
                }

                selectedFeatures = new ArrayList<ShapeFeature>(recordNumbers.length);
                int count = 0;

                for (final int record : recordNumbers) {
                    ++count;
                    ShapeFeature featureServiceFeature;

                    if (fc != null) {
                        featureServiceFeature = createFeatureInstance(null, info, record - 1);
                        this.initialiseFeature(featureServiceFeature, null, false, record - 1);
                    } else {
                        featureServiceFeature = createFeatureInstance(null, info, record);
                        this.initialiseFeature(featureServiceFeature, null, false, record);
                    }

                    if (fulfilQuery(query, featureServiceFeature)) {
                        selectedFeatures.add(featureServiceFeature);
                    }
                    if (saveAsLastCreated && (count > 50000)) {
                        break;
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("feature crs: " + getShapeCrs() + " features " + selectedFeatures.size()
                            + " boundingbox: "
                            + boundingPolygon.getEnvelopeInternal());
            }
            // check if thread is canceled .........................................
            if (this.checkCancelled(workerThread, " quering spatial index structure")) {
                return null;
            }
            // check if thread is canceled .........................................

            if (logger.isDebugEnabled()) {
                logger.debug("SW[" + workerThread + "]: quering spatial index for bounding box took "
                            + (System.currentTimeMillis() - start) + " ms");
            }

            if (selectedFeatures.size() > this.getMaxFeatureCount()) {
                throw new TooManyFeaturesException("features in selected area " + selectedFeatures.size()
                            + " exceeds max feature count " + this.getMaxFeatureCount());
            } else if (selectedFeatures.size() == 0) {
                logger.warn("SW[" + workerThread + "]: no features found in selected bounding box");
                return selectedFeatures;
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

            if (saveAsLastCreated) {
                this.updateLastCreatedFeatures(selectedFeatures, boundingPolygon, query);
            }
            // set the SLDs for features
            final List<Style> style = getStyle(layerName);

            if ((style != null) && (selectedFeatures != null)) {
                for (final ShapeFeature f : selectedFeatures) {
                    f.setSLDStyles(style);
                }
            }
            return new Vector<ShapeFeature>(selectedFeatures);
        } finally {
            cleanup();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   query    DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean fulfilQuery(final String query, final ShapeFeature feature) {
        if ((query == null) || query.equals("")) {
            return true;
        }

        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("js");

        try {
            final String dataDefinition = toVariableString(feature);
            String code = query;

            code = dataDefinition + "\n " + code.replace((CharSequence)"app:", (CharSequence)"");
            final Object result = engine.eval(code);

            if (result instanceof Boolean) {
                return (Boolean)result;
            } else {
                logger.error("invalid query");
                return false;
            }
        } catch (Exception e) {
            logger.error("invalid fromula: " + query, e);
            return false;
//            JOptionPane.showMessageDialog(
//                FieldCalculatorDialog.this,
//                e.getMessage(),
//                NbBundle.getMessage(
//                    FieldCalculatorDialog.class,
//                    "FieldCalculatorDialog.btnSearchCancelActionPerformed().error.title"),
//                JOptionPane.ERROR_MESSAGE,
//                null);
        }
    }

    /**
     * Creates a javascript string that contains all properties of the given feature as variable.
     *
     * @param   feature  the feature, that should be translated to javascript
     *
     * @return  the created string
     */
    private String toVariableString(final FeatureServiceFeature feature) {
        final StringBuilder vars = new StringBuilder("");
        final HashMap<String, Object> props = feature.getProperties();

        for (final String propName : props.keySet()) {
            final FeatureServiceAttribute attr = (FeatureServiceAttribute)feature.getLayerProperties()
                        .getFeatureService()
                        .getFeatureServiceAttributes()
                        .get(propName);
            final Class cl = FeatureTools.getClass(attr);
            Object value = props.get(propName);

            if (value instanceof org.deegree.model.spatialschema.Geometry) {
                try {
                    value = JTSAdapter.export((org.deegree.model.spatialschema.Geometry)value);
                } catch (GeometryException ex) {
                    logger.error("Error while converting deegree geometry to jts geometry", ex);
                }
            }
            if (vars.length() > 0) {
                vars.append(";\n");
            }

            vars.append(propName.replace((CharSequence)"app:", (CharSequence)"")).append("=");
            if ((value != null) && (cl.equals(String.class) || cl.equals(Date.class))) {
                vars.append("\"").append(value).append("\"");
            } else {
                vars.append(String.valueOf(value));
            }
        }

        vars.append(";\n");

        return vars.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private int getNextFreeId() throws Exception {
        if (lastFreeId > 0) {
            return ++lastFreeId;
        } else {
            if (fc != null) {
                lastFreeId = fc.size();
            } else {
                final ShapeFile persShapeFile = getShapeFile();
                lastFreeId = persShapeFile.getRecordNum() + 1;
            }

            return lastFreeId;
        }
    }

    @Override
    public FeatureServiceFeature createNewFeature() {
        try {
            ShapeFeature featureServiceFeature;
            final ShapeFile persShapeFile = getShapeFile();
            final ShapeInfo info = new ShapeInfo(filename, persShapeFile, featureSrid, fc);

            try {
                final int freeid = getNextFreeId();
                if (fc != null) {
                    featureServiceFeature = createFeatureInstance(null, info, freeid);
                    this.initialiseFeature(featureServiceFeature, null, false, freeid);
                    // fc.add(featureServiceFeature);
                } else {
                    featureServiceFeature = createFeatureInstance(null, info, freeid);
                    this.initialiseFeature(featureServiceFeature, null, false, freeid);
                }

                return featureServiceFeature;
            } catch (Exception e) {
                logger.error("Error while creating new feature", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while creating new feature", e);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the geometryType
     */
    public String getGeometryType() {
        return geometryType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the shapeCrs
     */
    public String getShapeCrs() {
        return shapeCrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  shapeCrs  the shapeCrs to set
     */
    public void setShapeCrs(final String shapeCrs) {
        this.shapeCrs = shapeCrs;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CrsWrapper {

        //~ Instance fields ----------------------------------------------------

        private Crs crs;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CrsWrapper object.
         *
         * @param  crs  DOCUMENT ME!
         */
        public CrsWrapper(final Crs crs) {
            this.crs = crs;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public String toString() {
            return crs.getShortname();
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
    }
}
