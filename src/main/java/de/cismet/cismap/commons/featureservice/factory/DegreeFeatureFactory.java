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

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProgressListener;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.JTSAdapter;

import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.*;

/**
 * Abstract Base class of features factories that make use of the degree framework to read features documents.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public abstract class DegreeFeatureFactory<FT extends FeatureServiceFeature, QT>
        extends AbstractFeatureFactory<FT, QT> {

    //~ Instance fields --------------------------------------------------------

    protected int geometryIndex;
    protected Integer featureSrid = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DegreeFeatureFactory object.
     */
    public DegreeFeatureFactory() {
        super();
    }

    /**
     * Creates a new DegreeFeatureFactory object.
     *
     * @param  dff  DOCUMENT ME!
     */
    protected DegreeFeatureFactory(final DegreeFeatureFactory dff) {
        super(dff);
        this.geometryIndex = dff.geometryIndex;
        this.featureSrid = dff.featureSrid;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Processes a degree feature collection and fills the provided vector with feature service features of custom type
     * {@code FT}.
     *
     * @param   workerThread         thread that is observed
     * @param   featureCollection    collection of degree features to be processed
     * @param   evaluateExpressions  featureVector vector of
     *
     * @return  {@code true} if the operation completed successfully, {@code false} otherwise
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected Vector<FT> processFeatureCollection(final SwingWorker workerThread,
            final Feature[] featureCollection,
            final boolean evaluateExpressions) throws Exception {
        if (DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug("SW[" + workerThread + "]: converting " + featureCollection.length
                            + " degree features to FeatureServiceFeatures");
            }
        }
        final long start = System.currentTimeMillis();
        int i = 0;
        geometryIndex = GeometryHeuristics.findBestGeometryIndex(featureCollection[0]);

        final Vector<FT> featureVector = new Vector(featureCollection.length);

        for (final Feature degreeFeature : featureCollection) {
            // check if canceled .......................................................
            if (this.checkCancelled(workerThread, "converting degree features (" + i + ")")) {
                return featureVector;
            }
            // check if canceled .......................................................
            // int progress = (int) (((double) featureCollection.length / (double) i) * 100d);

            // FIXME: use feature.getId() if idExpression is undefined? Feature ID may be a string!
            // if(DEBUG)logger.debug("Degree Feature ID: '" + degreeFeature.getId() + "'");

            final FT featureServiceFeature = this.createFeatureInstance(degreeFeature, i);
            this.initialiseFeature(featureServiceFeature, degreeFeature, evaluateExpressions, i);
            featureVector.add(featureServiceFeature);
            i++;
        }

        logger.info("SW[" + workerThread + "]: converting " + featureCollection.length + " degree features took "
                    + (System.currentTimeMillis() - start) + " ms");
        return featureVector;
    }

    /**
     * Perform standard initialisation of a newly created feature.<br/>
     * In gereal, this operation is invokded by the {@code processFeatureCollection} operation.
     *
     * @param   featureServiceFeature  DOCUMENT ME!
     * @param   degreeFeature          feature to be initialised
     * @param   evaluateExpressions    DOCUMENT ME!
     * @param   index                  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     *
     * @see     #processFeatureCollection(javax.swing.SwingWorker, org.deegree2.model.feature.Feature[], boolean)
     */
    protected void initialiseFeature(final FT featureServiceFeature,
            final Feature degreeFeature,
            final boolean evaluateExpressions,
            final int index) throws Exception {
        // perform standard initilaisation
        featureServiceFeature.setLayerProperties(this.getLayerProperties());

        // creating geometry
        if (featureServiceFeature.getGeometry() == null) {
            try {
                featureServiceFeature.setGeometry(JTSAdapter.export(
                        degreeFeature.getGeometryPropertyValues()[geometryIndex]));
            } catch (Exception e) {
                featureServiceFeature.setGeometry(JTSAdapter.export(degreeFeature.getDefaultGeometryPropertyValue()));
            }
        }

        if ((featureServiceFeature.getGeometry() != null) && (featureSrid != null)) {
            featureServiceFeature.getGeometry().setSRID(featureSrid);
        }

        // adding properties
        if ((featureServiceFeature.getProperties() == null) || featureServiceFeature.getProperties().isEmpty()) {
            // set the properties
            final FeatureProperty[] featureProperties = degreeFeature.getProperties();
            // if(DEBUG)if(DEBUG)logger.debug("setting " + featureProperties.length + "properties");
            for (final FeatureProperty fp : featureProperties) {
                // if(DEBUG)if(DEBUG)logger.debug("setting '" + fp.getName().getAsString() + "' = '" + fp.getValue() +
                // "'");
                featureServiceFeature.addProperty(fp.getName().getAsString(), fp.getValue());
            }
        }

        if (evaluateExpressions) {
            this.evaluateExpressions(featureServiceFeature, index);
        }
    }

    /**
     * Creates an instance of the custom FeatureServiceFeature types and may perform a custom initialisation with
     * properties of the degree feature.<br/>
     *
     * @param   degreeFeature  the degree feature that may be used for custom initialisation
     * @param   index          index of the current processing step, can be used for id generation
     *
     * @return  the newly creates FeatureServiceFeature
     *
     * @throws  Exception  DOCUMENT ME!
     *
     * @see     #processFeatureCollection(javax.swing.SwingWorker, org.deegree2.model.feature.Feature[], boolean)
     */
    protected abstract FT createFeatureInstance(Feature degreeFeature, int index) throws Exception;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * FeatureProgressListener used to track parsing progress of documents.
     *
     * @version  $Revision$, $Date$
     */
    protected class ParsingProgressListener implements FeatureProgressListener {

        //~ Instance fields ----------------------------------------------------

        private final int progressThreshold;
        private final int featureCount;
        private final SwingWorker workerThread;
        private int currentProgress = 0;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ParsingProgressListener object.
         *
         * @param  workerThread       DOCUMENT ME!
         * @param  featureCount       DOCUMENT ME!
         * @param  progressThreshold  DOCUMENT ME!
         */
        public ParsingProgressListener(final SwingWorker workerThread,
                final int featureCount,
                final int progressThreshold) {
            this.progressThreshold = progressThreshold;
            this.workerThread = workerThread;
            this.featureCount = featureCount;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  progress  DOCUMENT ME!
         */
        @Override
        public void featureProgress(final int progress) {
            if (DEBUG) {
                if (logger.isDebugEnabled()) {
                    logger.debug("real feature parsing progress: " + progress);
                }
            }
            final int newProgress = (int)((double)progress / (double)featureCount * progressThreshold);
            if ((workerThread != null) && (newProgress > currentProgress)) {
                // set to progress to -1 (indeterminate progress bar)
                currentProgress = (newProgress < progressThreshold) ? newProgress : -1;
                if (DEBUG) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SW[" + workerThread + "]: passing progress: " + currentProgress + "%");
                    }
                }

                workerThread.firePropertyChange("progress", currentProgress - 5, currentProgress);
            }
        }
    }
}
