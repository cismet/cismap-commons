/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.features.WFSFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.security.AccessHandler.ACCESS_METHODS;
import de.cismet.security.WebAccessManager;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.apache.commons.httpclient.methods.PostMethod;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;

/**
 * A FeatureFactory that creates WFSFeatures obtained from a Web Feature Service.<br/>
 * The factory is non-caching, which means that each request to {@code createFeatures}
 * leads to a new WFS request, even if the bounding box is the same. However, it
 * is possible to obtain the features created by the latesd WFS request via the
 * {@code getLastCreatedFeatures()} operation.
 *
 * @author Pascal Dihé
 */
public class WFSFeatureFactory extends DegreeFeatureFactory<WFSFeature, String>
{
  protected String hostname = null;
  protected String wfsVersion;

  protected WFSFeatureFactory(WFSFeatureFactory wfsff)
  {
    super(wfsff);
    this.hostname = wfsff.hostname;
    this.wfsVersion = wfsff.wfsVersion;
  }

  //private Vector<WFSFeature> wfsFeatureVector = new Vector();
  //private PostMethod httppost;
  //private InputStreamReader reader;
  public WFSFeatureFactory(LayerProperties layerProperties, String hostname, String wfsVersion)
  {
    logger.info("initialising WFSFeatureFactory with hostname: '" + hostname + "'");
    this.layerProperties = layerProperties;
    this.hostname = hostname;
    this.wfsVersion = wfsVersion;
  }

  public void setHostname(String hostname)
  {
    this.hostname = hostname;
  }

  //TODO: Track Progress?
  @Override
  public Vector<WFSFeature> createFeatures(String query, BoundingBox boundingBox, SwingWorker workerThread) throws TooManyFeaturesException, Exception
  {
    //this.lastCreatedfeatureVector.clear();
    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "createFeatures()"))
    {
      return null;
    }
    // check if canceled .......................................................

    String postString = WFSFacade.setGetFeatureBoundingBox(query, boundingBox, wfsVersion);

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "creating post string"))
    {
      return null;
    }
    // check if canceled .......................................................

    logger.debug("FRW[" + workerThread + "]: WFS Query: \n"+postString);

    long start = System.currentTimeMillis();

    InputStream respIs = WebAccessManager.getInstance().doRequest(new URL(hostname), postString, ACCESS_METHODS.POST_REQUEST);

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "executing http request"))
    {
      return null;
    }
    // check if canceled .......................................................

    logger.info("FRW[" + workerThread + "]: WFS request took " + ((System.currentTimeMillis() - start)) + " ms");


    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(respIs));

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "creating InputStreamReader"))
    {
      return null;
    }
    // check if canceled .......................................................

    GMLFeatureCollectionDocument featureCollectionDocument = new GMLFeatureCollectionDocument();
    FeatureCollection featureCollection;

    try
    {
      start = System.currentTimeMillis();

      // check if canceled .......................................................
      if (this.checkCancelled(workerThread, "creating GMLFeatureCollectionDocument"))
      {
        return null;
      }
      // check if canceled .......................................................

      featureCollectionDocument.load(reader, "http://dummyID");

      // check if canceled .......................................................
      if (this.checkCancelled(workerThread, "loading features"))
      {
        return null;
      }
      // check if canceled .......................................................

      // getFeatureCount() stimmt nicht mit der zahl der geparsten features überein!?
      /*if (featureCollectionDocument.getFeatureCount() > this.getMaxFeatureCount())
      {
      throw new TooManyFeaturesException("feature in feature document " + featureCollectionDocument.getFeatureCount() + " exceeds max feature count " + this.getMaxFeatureCount());
      } 
      else
       */
      if (featureCollectionDocument.getFeatureCount() == 0)
      {
        logger.warn("FRW[" + workerThread + "]: no features found before parsing");
        //if(DEBUG)logger.debug(featureCollectionDocument.getAsString());
        return null;
      }

      if (DEBUG)
      {
        logger.debug("FRW[" + workerThread + "]: parsing " + featureCollectionDocument.getFeatureCount() + " features");
      }

//      StringWriter sw = new StringWriter();
//      featureCollectionDocument.write(sw);
      featureCollection = featureCollectionDocument.parse();

      // check if canceled .......................................................
      if (this.checkCancelled(workerThread, "parsing features"))
      {
        return null;
      }
      // check if canceled .......................................................

      logger.info("FRW[" + workerThread + "]: parsing " + featureCollection.size() + " features took " + ((System.currentTimeMillis() - start)) + " ms");

      if (featureCollection.size() > this.getMaxFeatureCount())
      {
        throw new TooManyFeaturesException("FRW[" + workerThread + "]: feature in feature document " + featureCollection.size() + " exceeds max feature count " + this.getMaxFeatureCount());
      } else if (featureCollection.size() == 0)
      {
        logger.warn("FRW[" + workerThread + "]: no features found after parsing");
        return null;
      }

      Vector<WFSFeature> features = processFeatureCollection(workerThread, featureCollection.toArray(), true);

      //check if thread is canceled .........................................
      if (this.checkCancelled(workerThread, " saving LastCreatedFeatures "))
      {
        return null;
      }
      // check if thread is canceled .........................................

      this.updateLastCreatedFeatures(features);
      return features;

    } catch (Exception t)
    {
      logger.error("FRW[" + workerThread + "]: error parsing features: " + t.getMessage(), t);
      throw t;
    }
  }

  @Override
  public Vector createAttributes(
          SwingWorker workerThread) throws TooManyFeaturesException, UnsupportedOperationException, Exception
  {
    throw new UnsupportedOperationException("LIW[" + workerThread + "]: WFSFeatureFactory does not support Attributes");
  }

  protected void cleanup(InputStreamReader reader, PostMethod httppost)
  {
    if (reader != null)
    {
      try
      {
        reader.close();
      } catch (Exception silent)
      {
      }
      reader = null;
    }

    if (httppost != null)
    {
      httppost.releaseConnection();
      httppost =
              null;
    }

  }

  @Override
  protected WFSFeature createFeatureInstance(
          Feature degreeFeature, int index) throws Exception
  {
    return new WFSFeature();
  }

  @Override
  protected boolean isGenerateIds()
  {
    return false;
  }

  @Override
  public WFSFeatureFactory clone()
  {
    return new WFSFeatureFactory(this);
  }
}
