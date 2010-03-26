/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.deegree2.model.feature.Feature;
import org.deegree2.io.shpapi.ShapeFile;
import org.deegree2.model.feature.schema.FeatureType;
import org.deegree2.model.feature.schema.PropertyType;
import org.deegree2.model.spatialschema.JTSAdapter;

/**
 *
 * @author pascal
 */
public class ShapeFeatureFactory extends DegreeFeatureFactory<ShapeFeature, String> implements CachingFeatureFactory<ShapeFeature, String>
{
  protected int maxCachedFeatureCount = 150000;
  protected URI documentURI;
  protected ShapeFile shapeFile;
  protected boolean initialised = false;
  protected STRtree degreeFeaturesTree = null;
  //private Feature[] tempFeatureCollection;
  //private int currentProgress = 0;
  protected Vector<FeatureServiceAttribute> featureServiceAttributes;

  protected ShapeFeatureFactory(ShapeFeatureFactory shpff)
  {
    super(shpff);
    this.maxCachedFeatureCount = shpff.maxCachedFeatureCount;
    this.documentURI = shpff.documentURI;
    this.shapeFile = shpff.shapeFile;
    this.degreeFeaturesTree = shpff.degreeFeaturesTree;
    this.featureServiceAttributes = new Vector(shpff.featureServiceAttributes);
    this.initialised = shpff.initialised;
  }

  public ShapeFeatureFactory(LayerProperties layerProperties, URI documentURL, int maxCachedFeatureCount, SwingWorker workerThread) throws Exception
  {
    this.layerProperties = layerProperties;
    this.documentURI = documentURL;
    this.maxCachedFeatureCount = maxCachedFeatureCount;

    try
    {
      this.parseShapeFile(workerThread);
      this.initialised = true;
    } catch (Exception ex)
    {
      logger.error("SW[" + workerThread + "]: error parsing shape file", ex);//NOI18N
      if (DEBUG && shapeFile != null)
      {
        logger.debug(shapeFile.getFileMBR());
      }
      this.cleanup();
    }
  }

  @Override
  protected ShapeFeature createFeatureInstance(Feature degreeFeature, int index) throws Exception
  {
    ShapeFeature shapeFeature = new ShapeFeature();

    // auto generate Ids!
    shapeFeature.setId(index);

    try
    {
      shapeFeature.setGeometry(JTSAdapter.export(degreeFeature.getGeometryPropertyValues()[geometryIndex]));
    } catch (Exception e)
    {
      shapeFeature.setGeometry(JTSAdapter.export(degreeFeature.getDefaultGeometryPropertyValue()));
    }

    // store the feature in the spatial index structure
    this.degreeFeaturesTree.insert(shapeFeature.getGeometry().getEnvelopeInternal(), shapeFeature);
    return shapeFeature;
  }

  protected synchronized void cleanup()
  {
    if (this.shapeFile != null)
    {
      shapeFile.close();
      shapeFile = null;
      System.gc();
    }
  }

  @Override
  public boolean isLazy()
  {
    return false;
  }

  protected synchronized void parseShapeFile(SwingWorker workerThread) throws Exception
  {
    logger.info("SW[" + workerThread + "]: initialising ShapeFeatureFactory with document: '" + documentURI + "'");//NOI18N
    long start = System.currentTimeMillis();

    if (this.documentURI.getPath().endsWith(".shp"))//NOI18N
    {
      shapeFile = new ShapeFile(this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4));
    } else
    {
      shapeFile = new ShapeFile(this.documentURI.getPath());
    }

    int max = shapeFile.getRecordNum();
    if (DEBUG)
    {
      logger.debug("SW[" + workerThread + "]: " + max + " features found in shape file");//NOI18N
    }
    if (shapeFile.getRecordNum() > this.maxCachedFeatureCount)
    {
      logger.error("SW[" + workerThread + "]: number of features in shape file (" + shapeFile.getRecordNum() + ") exceeds maximum of supported features (" + this.maxCachedFeatureCount + ")");//NOI18N
      max = this.maxCachedFeatureCount;
    }
    if (max == 0)
    {
      logger.error("SW[" + workerThread + "]: no features found in shape file");//NOI18N
      throw new Exception("no features found in shape file '" + this.documentURI + "'");//NOI18N
    }

    //this.tempFeatureCollection = new Feature[max];
    this.degreeFeaturesTree = new STRtree(max);

    // parse features ........................................................
    int currentProgress = 0;
    for (int i = 0; i < max; i++)
    {
      Feature degreeFeature = shapeFile.getFeatureByRecNo(i + 1);
      if (i == 0)
      {
        FeatureType type = degreeFeature.getFeatureType();
        logger.info("SW[" + workerThread + "]: creating "+type.getProperties().length+" featureServiceAttributes from first parsed degree feature");//NOI18N
        featureServiceAttributes = new Vector(type.getProperties().length);
        for (PropertyType pt : type.getProperties())
        {
          //ToDo was ist wenn zwei Geometrien dabei sind
          featureServiceAttributes.add(
                  new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true));
        }
      }

      // create Feature instance fuegt die features dem strtree hinzu
      ShapeFeature featureServiceFeature = this.createFeatureInstance(degreeFeature, i);
      this.initialiseFeature(featureServiceFeature, degreeFeature, false, i);
      //this.tempFeatureCollection[i] = shapeFile.getFeatureByRecNo(i + 1);


      int newProgress = (int) ((double) i / (double) max * 100d);
      if (workerThread != null && newProgress > currentProgress && newProgress >= 5 && newProgress % 5 == 0)
      {
        // set to progress to -1 (indeterminate progress bar)
        currentProgress = newProgress < 100 ? newProgress : -1;
        if (DEBUG)
        {
          logger.debug("SW[" + workerThread + "]: parsing progress: " + currentProgress + "%");//NOI18N
        }
        workerThread.firePropertyChange("progress", currentProgress - 5, currentProgress);//NOI18N
      }
    }

    this.cleanup();

    logger.info("parsing, converting and initialising " + max + " shape features took " + ((System.currentTimeMillis() - start)) + " ms");//NOI18N
  }

  @Override
  public synchronized void flush()
  {
    logger.warn("flushing cached features");//NOI18N
    this.lastCreatedfeatureVector.clear();
    //this.initialised = false;
    //this.tempFeatureCollection = null;
    //this.degreeFeaturesTree = null;
    //this.shapeFile = null;
    System.gc();

//    try
//    {
//      this.parseShapeFile(null);
//
//    } catch (Exception ex)
//    {
//      logger.error("error parsing shape file", ex);
//      if (DEBUG && shapeFile != null)
//      {
//        logger.debug(shapeFile.getFileMBR());
//      }
//      this.cleanup();
//    }
  }

  @Override
  public int getMaxCachedFeatureCount()
  {
    return this.maxCachedFeatureCount;
  }

  @Override
  public void setMaxCachedFeatureCount(int maxCachedFeatureCount)
  {
    this.maxCachedFeatureCount = maxCachedFeatureCount;
  }

  /**
   * Get the value of documentURL
   *
   * @return the value of documentURL
   */
  public URI getDocumentURI()
  {
    return documentURI;
  }

  /**
   * Set the value of documentURL
   *
   * @param documentURL new value of documentURL
   */
  public synchronized void setDocumentURI(URI documentURI)
  {
    this.documentURI = documentURI;
  }

  @Override
  public synchronized Vector<ShapeFeature> createFeatures(String query, BoundingBox boundingBox, SwingWorker workerThread) throws TooManyFeaturesException, Exception
  {
    if (!this.initialised)
    {
      logger.warn("SW[" + workerThread + "]: Factory not correclty initialised, parsing shape file");//NOI18N
      this.parseShapeFile(workerThread);
      this.initialised = true;

      //check if thread is canceled .........................................
      if (this.checkCancelled(workerThread, " initialisation"))//NOI18N
      {
        return null;
      }
      // check if thread is canceled .........................................
    }

    //this.lastCreatedfeatureVector.clear();

    long start = System.currentTimeMillis();
    Coordinate[] polyCords = new Coordinate[5];
    polyCords[0] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
    polyCords[1] = new Coordinate(boundingBox.getX1(), boundingBox.getY2());
    polyCords[2] = new Coordinate(boundingBox.getX2(), boundingBox.getY2());
    polyCords[3] = new Coordinate(boundingBox.getX2(), boundingBox.getY1());
    polyCords[4] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
    Polygon boundingPolygon = (new GeometryFactory()).createPolygon((new GeometryFactory()).createLinearRing(polyCords), null);

    List<ShapeFeature> selectedFeatures = this.degreeFeaturesTree.query(boundingPolygon.getEnvelopeInternal());

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " quering spatial index structure"))//NOI18N
    {
      return null;
    }
    // check if thread is canceled .........................................

    logger.info("SW[" + workerThread + "]: " + selectedFeatures.size() + " features selected by bounding box out of " + this.degreeFeaturesTree.size() + " in spatial index");//NOI18N
    if (DEBUG)
    {
      logger.debug("SW[" + workerThread + "]: quering spatial index for bounding box took " + ((System.currentTimeMillis() - start)) + " ms");//NOI18N
    }

    if (selectedFeatures.size() > this.getMaxFeatureCount())
    {
      throw new TooManyFeaturesException("features in selected area " + selectedFeatures.size() + " exceeds max feature count " + this.getMaxFeatureCount());//NOI18N
    } else if (selectedFeatures.size() == 0)
    {
      logger.warn("SW[" + workerThread + "]: no features found in selected bounding box");//NOI18N
      return null;
    }

    this.reEvaluteExpressions(selectedFeatures, workerThread);

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " saving LastCreatedFeatures "))//NOI18N
    {
      return null;
    }
    // check if thread is canceled .........................................

    this.updateLastCreatedFeatures(selectedFeatures);
    return new Vector<ShapeFeature>(selectedFeatures);
  }

  @Override
  public synchronized Vector<FeatureServiceAttribute> createAttributes(SwingWorker workerThread) throws TooManyFeaturesException, Exception
  {
    if (this.featureServiceAttributes == null || this.featureServiceAttributes.size() == 0)
    {
      logger.warn("SW[" + workerThread + "]: Factory not correctopy initialised, parsing shape file");//NOI18N
      this.parseShapeFile(workerThread);
    }

    if (this.featureServiceAttributes == null || this.featureServiceAttributes.size() == 0)
    {
      logger.error("SW[" + workerThread + "]: no attributes could be found in shape file");//NOI18N
      throw new Exception("no attributes could be found in shape file '" + this.documentURI + "'");//NOI18N
    }

    return this.featureServiceAttributes;
  }

  @Override
  protected boolean isGenerateIds()
  {
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
  public ShapeFeatureFactory clone()
  {
    return new ShapeFeatureFactory(this);
  }
}
