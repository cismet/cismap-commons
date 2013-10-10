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

import org.deegree.style.se.unevaluated.Style;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.*;

/**
 * Factory class that creates a collection of features from arbitrary data sources (e.g. from a WFS, da database
 * connection, a file, etc.) and assigns (if applicable) a shared LayerProperties object to each feature.<br/>
 * Implementation classes have to assure that the FeatureFactory is properly initialised, e.g. a database connection is
 * established, a WFS url is set, etc. by providing respective constructors or initialisation methods that must be
 * invoked before the first call to {@code createFeatures()} can happen.
 *
 * @param    <FT>  The Type of the Feature created by this Factory
 * @param    <QT>  The Type of the query used to select the features to be created
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public interface FeatureFactory<FT extends FeatureServiceFeature, QT> extends Cloneable {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns instances of features {@code FT} that match the optional query and that fall into the optional
     * BoundingBox.<br/>
     * Applies layerProperties (if set) on all Features (if the type of the feature implements the interface
     * {@code InheritsLayerProperties}). The SwingWorker instance can be used to set the progress of the operation and
     * to check the worker thread was canceled. The state of the features list after a the operation was canceled is
     * undefined.
     *
     * @param   query         optional query of type {@code QT} to select the features to be returned
     * @param   boundingBox   optional BoundingBox to restrict the features to be returned
     * @param   workerThread  an optional worker thread that is observed
     *
     * @return  a list of matching features of type {@code FT}
     *
     * @throws  TooManyFeaturesException  if the maximum number of features is reached during processing
     * @throws  Exception                 if something went wrong during parsing
     */
    List<FT> createFeatures(QT query, BoundingBox boundingBox, SwingWorker workerThread)
            throws TooManyFeaturesException, Exception;

    /**
     * Returns instances of features {@code FT} that match the optional query and that fall into the optional
     * BoundingBox.<br/>
     * Applies layerProperties (if set) on all Features (if the type of the feature implements the interface
     * {@code InheritsLayerProperties}). The SwingWorker instance can be used to set the progress of the operation and
     * to check the worker thread was canceled. The state of the features list after a the operation was canceled is
     * undefined.
     *
     * @param   query         optional query of type {@code QT} to select the features to be returned
     * @param   boundingBox   optional BoundingBox to restrict the features to be returned
     * @param   workerThread  an optional worker thread that is observed
     * @param   offset        the start index
     * @param   limit         a limit
     * @param   orderBy       the attributes, the features should be ordered by
     *
     * @return  a list of matching features of type {@code FT}
     *
     * @throws  TooManyFeaturesException  if the maximum number of features is reached during processing
     * @throws  Exception                 if something went wrong during parsing
     */
    List<FT> createFeatures(QT query,
            BoundingBox boundingBox,
            SwingWorker workerThread,
            int offset,
            int limit,
            FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception;

    /**
     * This operation can be used to retrieve the last created features without the need to invoke the
     * {@code createFeatures()} operation. It returns a <b>copy <b>of the internal features vector of the factory to
     * provent concurrent modification exceptions.</b></b>
     *
     * @return  the features created during the last call to {@code createFeatures()}
     */
    List<FT> getLastCreatedFeatures();

    /**
     * Method that does not create FeatureServiceAttributes. In general, FeatureServiceAttributes need only to be
     * created when the layer is initialized.
     *
     * @param   workerThread  DOCUMENT ME!
     *
     * @return  a list of FeatureServiceAttributes.
     *
     * @throws  TooManyFeaturesException       if the maximum number of features is reached during processing
     * @throws  UnsupportedOperationException  if the factory does not support the creation attributes
     * @throws  Exception                      if something went wrong during parsing
     */
    List<FeatureServiceAttribute> createAttributes(SwingWorker workerThread) throws TooManyFeaturesException,
        UnsupportedOperationException,
        Exception;

    /**
     * Sets the layerProperties that are applied to all features constructed. If the list of the last created features
     * is not empty and any of the expressions of the layer properties changed, the new expressions are applied to the
     * last created features and will be automatically applied to all newly created features.
     *
     * @param  layerProperties  new LayerProperties
     */
    void setLayerProperties(LayerProperties layerProperties);

    /**
     * Returns the layerProperties that are applied to all features constructed.
     *
     * @return  the LayerProperties or null if not set
     */
    LayerProperties getLayerProperties();

    /**
     * This method must be implemented, if
     * {@link de.cismet.cismap.commons.featureservice.AbstractFeatureService#isEditable() }.
     *
     * @return  a new object with a valid id
     */
    FeatureServiceFeature createNewFeature();

    /**
     * Returns the maximum number of features that can be <b>returned</b> by this feature factory. Since the number of
     * features returned is resticted by the specified BoundingBox a FeatureFactory implementation may be able to
     * allocate more features as specified by {@code maxFeatureCount}.
     *
     * @return  the maximum number of features or -1 if not set.
     *
     * @see     CachingFeatureFactory
     */
    int getMaxFeatureCount();

    /**
     * Sets the maximum number of features that can be returned by this feature factory and that fall into the specified
     * BoundinfBox respectively. If during processing of the BoundinfBox the maximum number is reached the factory
     * throws a TooManyFeaturesException.
     *
     * @param  maxFeatureCount  the maximum number of features
     */
    void setMaxFeatureCount(int maxFeatureCount);

    //J-
    /**
     * DOCUMENT ME!
     *
     * @return   DOCUMENT ME!
     */
    FeatureFactory clone();
    //J+

    /**
     * DOCUMENT ME!
     *
     * @param  styles  DOCUMENT ME!
     */
    void setSLDStyle(Map<String, LinkedList<Style>> styles);

    /**
     * DOCUMENT ME!
     *
     * @param   bb  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getFeatureCount(BoundingBox bb);

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Exception that is thrown when the features to be process by a feature factory exceeds the maximum number of
     * features supported.
     *
     * @version  $Revision$, $Date$
     */
    public class TooManyFeaturesException extends Exception {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TooManyFeaturesException object.
         *
         * @param  message  DOCUMENT ME!
         */
        public TooManyFeaturesException(final String message) {
            super(message);
        }
    }
}
