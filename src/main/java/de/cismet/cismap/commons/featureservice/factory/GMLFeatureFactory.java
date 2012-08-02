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
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.GMLFeatureCollectionDocument;
import org.deegree2.model.feature.schema.FeatureType;
import org.deegree2.model.feature.schema.PropertyType;
import org.deegree2.model.spatialschema.JTSAdapter;

/**
 * Feature Factory that supports of GML documents.<br/>
 * For the reading of GML documents Degree2 Libraries are used.
 *
 * @author Pascal Dihé
 */
public class GMLFeatureFactory extends DegreeFeatureFactory<DefaultFeatureServiceFeature, String> implements CachingFeatureFactory<DefaultFeatureServiceFeature, String>
{

  protected int maxCachedFeatureCount = 150000;
  protected URI documentURI;
  protected GMLFeatureCollectionDocument gmlDocument;
  protected boolean initialised = false;
  protected STRtree degreeFeaturesTree = null;
  protected Vector<FeatureServiceAttribute> featureServiceAttributes;
  protected BufferedReader documentReader;

  protected GMLFeatureFactory(GMLFeatureFactory gmlff)
  {
    super(gmlff);
    this.maxCachedFeatureCount = gmlff.maxCachedFeatureCount;
    this.documentURI = gmlff.documentURI;
    this.gmlDocument = gmlff.gmlDocument;
    this.degreeFeaturesTree = gmlff.degreeFeaturesTree;
    this.featureServiceAttributes = new Vector(gmlff.featureServiceAttributes);
    this.initialised = gmlff.initialised;
  }

  public GMLFeatureFactory(LayerProperties layerProperties, URI documentURL, int maxCachedFeatureCount, SwingWorker workerThread) throws Exception
  {
    this.layerProperties = layerProperties;
    this.documentURI = documentURL;
    this.maxCachedFeatureCount = maxCachedFeatureCount;

    try
    {
      this.parseGMLFile(workerThread);
      this.initialised = true;
    } catch (Exception ex)
    {
      logger.error("SW[" + workerThread + "]: error parsing gml file", ex);
      if (DEBUG && gmlDocument != null)
      {
        logger.debug(gmlDocument.getAsString());
      }
      this.cleanup();
    }
  }

  @Override
  protected DefaultFeatureServiceFeature createFeatureInstance(Feature degreeFeature, int index) throws Exception
  {
    DefaultFeatureServiceFeature gmlFeature = new DefaultFeatureServiceFeature();

    // auto generate Ids!
    gmlFeature.setId(index);

    try
    {
      gmlFeature.setGeometry(JTSAdapter.export(degreeFeature.getGeometryPropertyValues()[geometryIndex]));
    } catch (Exception e)
    {
      gmlFeature.setGeometry(JTSAdapter.export(degreeFeature.getDefaultGeometryPropertyValue()));
    }

    // store the feature in the spatial index structure
    this.degreeFeaturesTree.insert(gmlFeature.getGeometry().getEnvelopeInternal(), gmlFeature);
    return gmlFeature;
  }

  protected synchronized void cleanup()
  {
    if (this.documentReader != null)
    {
      try
      {
        documentReader.close();
      } catch (IOException ex)
      {
      }
      documentReader = null;
      System.gc();
    }

    this.gmlDocument = null;
  }

  @Override
  public boolean isLazy()
  {
    return false;
  }

  protected synchronized void parseGMLFile(SwingWorker workerThread) throws Exception
  {
    logger.info("SW[" + workerThread + "]: initialising GMLFeatureFactory with document: '" + documentURI + "'");
    long start = System.currentTimeMillis();

    this.documentReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.documentURI))));
    this.gmlDocument = new GMLFeatureCollectionDocument();
    this.gmlDocument.load(this.documentReader, "http://dummyID");
    this.gmlDocument.getFeatureCount();

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " initialising gml document "))
    {
      this.cleanup();
      return;
    }
    // check if thread is canceled .........................................

    int max = this.gmlDocument.getFeatureCount();
    if (DEBUG)
    {
      logger.debug("SW[" + workerThread + "]: " + max + " features found in gml file");
    }
    if (max > this.maxCachedFeatureCount)
    {
      logger.error("SW[" + workerThread + "]: number of features in gml file (" + max + ") exceeds maximum of supported features (" + this.maxCachedFeatureCount + ")");
      max = this.maxCachedFeatureCount;
    }
    if (max == 0)
    {
      logger.error("SW[" + workerThread + "]: no features found in gml file");
      throw new Exception("no features found in gml file '" + this.documentURI + "'");
    }

    this.degreeFeaturesTree = new STRtree(max);

    // parse features ........................................................

    ParsingProgressListener progressListener = new ParsingProgressListener(workerThread, max, 100);
    this.gmlDocument.addFeatureProgressListener(progressListener);
    FeatureCollection featureCollection = gmlDocument.parse();
    if(DEBUG) logger.debug("SW[" + workerThread + "]: " + featureCollection.size() + " features parsed");
    this.cleanup();

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " parsing gml document "))
    {
      return;
    }
    // check if thread is canceled .........................................

    if (featureCollection.size() > 0)
    {
      FeatureType type = featureCollection.getFeatureType();
      logger.info("SW[" + workerThread + "]: creating " + type.getProperties().length + " featureServiceAttributes from first parsed degree feature");
      featureServiceAttributes = new Vector(type.getProperties().length);
      for (PropertyType pt : type.getProperties())
      {
        //ToDo was ist wenn zwei Geometrien dabei sind
        featureServiceAttributes.add(
                new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()), true));
      }
    }
    else
    {
      logger.error("could not create feature service attributes, no valid gml fetures found");
    }

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " creating feature service attributes"))
    {
      return;
    }
    // check if thread is canceled .........................................

    this.processFeatureCollection(workerThread, featureCollection.toArray(), initialised);

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " processing parsed features"))
    {
      return;
    }
    // check if thread is canceled .........................................

    logger.info("parsing, converting and initialising " + max + " gml features took " + ((System.currentTimeMillis() - start)) + " ms");
  }

  @Override
  public synchronized void flush()
  {
    logger.warn("flushing cached features");
    this.lastCreatedfeatureVector.clear();
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
  public synchronized Vector<DefaultFeatureServiceFeature> createFeatures(String query, BoundingBox boundingBox, SwingWorker workerThread) throws TooManyFeaturesException, Exception
  {
    if (!this.initialised)
    {
      logger.warn("SW[" + workerThread + "]: Factory not correclty initialised, parsing gml file");
      this.parseGMLFile(workerThread);
      this.initialised = true;

      //check if thread is canceled .........................................
      if (this.checkCancelled(workerThread, " initialisation"))
      {
        return null;
      }
      // check if thread is canceled .........................................
    }

    long start = System.currentTimeMillis();
    Coordinate[] polyCords = new Coordinate[5];
    polyCords[0] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
    polyCords[1] = new Coordinate(boundingBox.getX1(), boundingBox.getY2());
    polyCords[2] = new Coordinate(boundingBox.getX2(), boundingBox.getY2());
    polyCords[3] = new Coordinate(boundingBox.getX2(), boundingBox.getY1());
    polyCords[4] = new Coordinate(boundingBox.getX1(), boundingBox.getY1());
    Polygon boundingPolygon = (new GeometryFactory()).createPolygon((new GeometryFactory()).createLinearRing(polyCords), null);

    List<DefaultFeatureServiceFeature> selectedFeatures = this.degreeFeaturesTree.query(boundingPolygon.getEnvelopeInternal());

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " quering spatial index structure"))
    {
      return null;
    }
    // check if thread is canceled .........................................

    logger.info("SW[" + workerThread + "]: " + selectedFeatures.size() + " features selected by bounding box out of " + this.degreeFeaturesTree.size() + " in spatial index");
    if (DEBUG)
    {
      logger.debug("SW[" + workerThread + "]: quering spatial index for bounding box took " + ((System.currentTimeMillis() - start)) + " ms");
    }

    if (selectedFeatures.size() > this.getMaxFeatureCount())
    {
      throw new TooManyFeaturesException("features in selected area " + selectedFeatures.size() + " exceeds max feature count " + this.getMaxFeatureCount());
    } else if (selectedFeatures.size() == 0)
    {
      logger.warn("SW[" + workerThread + "]: no features found in selected bounding box");
      return null;
    }

    this.reEvaluteExpressions(selectedFeatures, workerThread);

    //check if thread is canceled .........................................
    if (this.checkCancelled(workerThread, " saving LastCreatedFeatures "))
    {
      return null;
    }
    // check if thread is canceled .........................................

    this.updateLastCreatedFeatures(selectedFeatures);
    return new Vector<DefaultFeatureServiceFeature>(selectedFeatures);
  }

  @Override
  public synchronized Vector<FeatureServiceAttribute> createAttributes(SwingWorker workerThread) throws TooManyFeaturesException, Exception
  {
    if (this.featureServiceAttributes == null || this.featureServiceAttributes.size() == 0)
    {
      logger.warn("SW[" + workerThread + "]: Factory not correctopy initialised, parsing gml file");
      this.parseGMLFile(workerThread);
    }

    if (this.featureServiceAttributes == null || this.featureServiceAttributes.size() == 0)
    {
      logger.error("SW[" + workerThread + "]: no attributes could be found in gml file");
      throw new Exception("no attributes could be found in gml file '" + this.documentURI + "'");
    }

    return this.featureServiceAttributes;
  }

  @Override
  protected boolean isGenerateIds()
  {
    return true;
  }

  @Override
  public GMLFeatureFactory clone()
  {
    return new GMLFeatureFactory(this);
  }

//  public static void main(String args[])
//  {
//    BasicConfigurator.configure();
//
//    try
//    {
//      Logger.getLogger(GMLFeatureFactory.class).setLevel(org.apache.log4j.Level.ALL);
//      GMLFeatureFactory gmlFeatureFactory = new GMLFeatureFactory(
//              new DefaultLayerProperties(), new URI("file:///D:/W/fs.gml"), 50000, null);
//      gmlFeatureFactory.logger.info("OK");
//    } catch (Throwable t)
//    {
//      t.printStackTrace();
//    }
//  }
}
