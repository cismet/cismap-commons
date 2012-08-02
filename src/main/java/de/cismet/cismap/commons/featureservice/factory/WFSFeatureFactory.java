/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.features.WFSFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory.TooManyFeaturesException;
import de.cismet.tools.StaticHtmlTools;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.GMLFeatureCollectionDocument;

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
  /* XML-constant of the querypart that should be replaced by a boundingbox */

  public static final String CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER = "<cismapBoundingBoxAsGmlPlaceholder />";
  protected String hostname = null;

  protected WFSFeatureFactory(WFSFeatureFactory wfsff)
  {
    super(wfsff);
    this.hostname = wfsff.hostname;
  }

  //private Vector<WFSFeature> wfsFeatureVector = new Vector();
  //private PostMethod httppost;
  //private InputStreamReader reader;
  public WFSFeatureFactory(LayerProperties layerProperties, String hostname)
  {
    logger.info("initialising WFSFeatureFactory with hostname: '" + hostname + "'");
    this.layerProperties = layerProperties;
    this.hostname = hostname;
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

    String postString = query.toString().replaceAll(CISMAP_BOUNDING_BOX_AS_GML_PLACEHOLDER, boundingBox.toGmlString());

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "creating post string"))
    {
      return null;
    }
    // check if canceled .......................................................

    logger.debug("FRW[" + workerThread + "]: WFS Query: \n"+postString);
    PostMethod httppost = new PostMethod(hostname);
    httppost.setRequestEntity(new StringRequestEntity(postString));
    if (DEBUG)
    {
      logger.debug("FRW[" + workerThread + "]: Feature post request: <br><pre>" + StaticHtmlTools.stringToHTMLString(postString) + "</pre>");
    }

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "creating http request"))
    {
      this.cleanup(null, httppost);
      return null;
    }
    // check if canceled .......................................................

    long start = System.currentTimeMillis();

    HttpClient client = new HttpClient();
    String proxySet = System.getProperty("proxySet");
    if (proxySet != null && proxySet.equals("true"))
    {
      if (DEBUG)
      {
        logger.debug("proxyIs Set");
        logger.debug("ProxyHost:" + System.getProperty("http.proxyHost"));
        logger.debug("ProxyPort:" + System.getProperty("http.proxyPort"));
      }

      try
      {
        client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
      } catch (Exception e)
      {
        logger.warn("Problem while setting proxy: " + e.getMessage(), e);
      }
    }
    client.executeMethod(httppost);

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "executing http request"))
    {
      this.cleanup(null, httppost);
      return null;
    }
    // check if canceled .......................................................

    logger.info("FRW[" + workerThread + "]: WFS request took " + ((System.currentTimeMillis() - start)) + " ms");
    int code = httppost.getStatusCode();

    if (httppost.getStatusCode() != HttpStatus.SC_OK)
    {
      logger.error("FRW[" + workerThread + "]: connection failed with HTTP Status Code '" + httppost.getStatusCode() + "'");
      httppost.releaseConnection();
      Exception ex = new Exception("FRW[" + workerThread + "]: Statuscode: " + code + " nicht bekannt transaktion abgebrochen");
      ex.initCause(new Throwable("FRW[" + workerThread + "]: Statuscode: " + code + " nicht bekannt transaktion abgebrochen"));
      throw ex;
    }

    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(httppost.getResponseBodyAsStream()));

    // check if canceled .......................................................
    if (this.checkCancelled(workerThread, "creating InputStreamReader"))
    {
      this.cleanup(reader, httppost);
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
        this.cleanup(reader, httppost);
        return null;
      }
      // check if canceled .......................................................

      featureCollectionDocument.load(reader, "http://dummyID");

      // check if canceled .......................................................
      if (this.checkCancelled(workerThread, "loading features"))
      {
        this.cleanup(reader, httppost);
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
        this.cleanup(reader, httppost);
        return null;
      }

      if (DEBUG)
      {
        logger.debug("FRW[" + workerThread + "]: parsing " + featureCollectionDocument.getFeatureCount() + " features");
      }

      featureCollection = featureCollectionDocument.parse();

      // check if canceled .......................................................
      if (this.checkCancelled(workerThread, "parsing features"))
      {
        this.cleanup(reader, httppost);
        return null;
      }
      // check if canceled .......................................................

      logger.info("FRW[" + workerThread + "]: parsing " + featureCollection.size() + " features took " + ((System.currentTimeMillis() - start)) + " ms");
      this.cleanup(reader, httppost);

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
