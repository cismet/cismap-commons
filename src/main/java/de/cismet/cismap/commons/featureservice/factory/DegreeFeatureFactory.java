/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureProgressListener;
import org.deegree2.model.feature.FeatureProperty;
import org.deegree2.model.spatialschema.JTSAdapter;

/**
 * Abstract Base class of features factories that make use of the degree framework
 * to read features documents.
 *
 * @author Pascal Dihé
 */
public abstract class DegreeFeatureFactory<FT extends FeatureServiceFeature, QT> extends AbstractFeatureFactory<FT, QT>
{
  protected int geometryIndex;

  public DegreeFeatureFactory()
  {
    super();
  }

  protected DegreeFeatureFactory(DegreeFeatureFactory dff)
  {
    super(dff);
    this.geometryIndex = dff.geometryIndex;
  }

  /**
   * Processes a degree feature collection and fills the provided vector with 
   * feature service features of custom type {@code FT}.
   * 
   * @param workerThread thread that is observed
   * @param featureCollection collection of degree features to be processed
   * @param featureVector vector of 
   * @return {@code true} if the operation completed successfully, {@code false} otherwise
   */
  protected Vector<FT> processFeatureCollection(SwingWorker workerThread, Feature[] featureCollection, boolean evaluateExpressions) throws Exception
  {
    if (DEBUG)
    {
      logger.debug("SW[" + workerThread + "]: converting " + featureCollection.length + " degree features to FeatureServiceFeatures");
    }
    long start = System.currentTimeMillis();
    int i = 0;
    geometryIndex = GeometryHeuristics.findBestGeometryIndex(featureCollection[0]);

    Vector<FT> featureVector = new Vector(featureCollection.length);

    for (Feature degreeFeature : featureCollection)
    {
      // check if canceled .......................................................
      if (this.checkCancelled(workerThread, "converting degree features (" + i + ")"))
      {
        return featureVector;
      }
      // check if canceled .......................................................
      //int progress = (int) (((double) featureCollection.length / (double) i) * 100d);

      //FIXME: use feature.getId() if idExpression is undefined? Feature ID may be a string!
      //if(DEBUG)logger.debug("Degree Feature ID: '" + degreeFeature.getId() + "'");

      FT featureServiceFeature = this.createFeatureInstance(degreeFeature, i);
      this.initialiseFeature(featureServiceFeature, degreeFeature, evaluateExpressions, i);
      featureVector.add(featureServiceFeature);
      i++;
    }

    logger.info("SW[" + workerThread + "]: converting " + featureCollection.length + " degree features took " + ((System.currentTimeMillis() - start)) + " ms");
    return featureVector;
  }

  /**
   * Perform standard initialisation of a newly created feature.<br/>
   * In gereal, this operation is invokded by the {@code processFeatureCollection}
   * operation.
   *
   * @see #processFeatureCollection(javax.swing.SwingWorker, org.deegree2.model.feature.Feature[], boolean)
   * @param newFeature feature to be initialised
   */
  protected void initialiseFeature(FT featureServiceFeature, Feature degreeFeature, boolean evaluateExpressions, int index) throws Exception
  {
    //perform standard initilaisation
      featureServiceFeature.setLayerProperties(this.getLayerProperties());

      // creating geometry
      if (featureServiceFeature.getGeometry() == null)
      {
        try
        {
          featureServiceFeature.setGeometry(JTSAdapter.export(degreeFeature.getGeometryPropertyValues()[geometryIndex]));
        } catch (Exception e)
        {
          featureServiceFeature.setGeometry(JTSAdapter.export(degreeFeature.getDefaultGeometryPropertyValue()));
        }
      }

      // adding properties
      if (featureServiceFeature.getProperties() == null || featureServiceFeature.getProperties().isEmpty())
      {
        // set the properties
        FeatureProperty[] featureProperties = degreeFeature.getProperties();
        //if(DEBUG)if(DEBUG)logger.debug("setting " + featureProperties.length + "properties");
        for (FeatureProperty fp : featureProperties)
        {
          //if(DEBUG)if(DEBUG)logger.debug("setting '" + fp.getName().getAsString() + "' = '" + fp.getValue() + "'");
          featureServiceFeature.addProperty(fp.getName().getAsString(), fp.getValue());
        }
      }

      if (evaluateExpressions)
      {
        this.evaluateExpressions(featureServiceFeature, index);
      }
  }

  /**
   * Creates an instance of the custom FeatureServiceFeature types and may
   * perform a custom initialisation with properties of the degree feature.<br/>
   *
   * @see #processFeatureCollection(javax.swing.SwingWorker, org.deegree2.model.feature.Feature[], boolean)
   * @param degreeFeature the degree feature that may be used for custom initialisation
   * @param index index of the current processing step, can be used for id generation
   * @return the newly creates FeatureServiceFeature
   */
  protected abstract FT createFeatureInstance(Feature degreeFeature, int index) throws Exception;

  /**
   * FeatureProgressListener used to track parsing progress of documents.
   */
  protected class ParsingProgressListener implements FeatureProgressListener
  {
    private final int progressThreshold;
    private final int featureCount;
    private final SwingWorker workerThread;
    private int currentProgress = 0;

    public ParsingProgressListener(SwingWorker workerThread, int featureCount, int progressThreshold)
    {
      this.progressThreshold = progressThreshold;
      this.workerThread = workerThread;
      this.featureCount = featureCount;
    }

    @Override
    public void featureProgress(int progress)
    {
      if(DEBUG)logger.debug("real feature parsing progress: " + progress);
      int newProgress = (int) ((double) progress / (double) featureCount * progressThreshold);
      if (workerThread != null && newProgress > currentProgress)
      {
        //set to progress to -1 (indeterminate progress bar)
        currentProgress = newProgress < progressThreshold ? newProgress : -1;
        if (DEBUG)
        {
          logger.debug("SW[" + workerThread + "]: passing progress: " + currentProgress + "%");
        }

        workerThread.firePropertyChange("progress", currentProgress - 5, currentProgress);
      }
    }
  }
}
