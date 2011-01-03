/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;
import java.util.Collection;
import java.util.Vector;

import de.cismet.cismap.commons.*;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FeatureCollection extends ServiceLayer {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void addFeatureCollectionListener(FeatureCollectionListener l);
    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void removeFeatureCollectionListener(FeatureCollectionListener l);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getFeatureCount();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Vector<Feature> getAllFeatures();
    /**
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Feature getFeature(int index);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean areFeaturesEditable();

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void select(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    void select(Collection<Feature> cf);
    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void addToSelection(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    void addToSelection(Collection<Feature> cf);

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void unselect(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    void unselect(Collection<Feature> cf);
    /**
     * DOCUMENT ME!
     */
    void unselectAll();
    /**
     * DOCUMENT ME!
     *
     * @param  quiet  DOCUMENT ME!
     */
    void unselectAll(boolean quiet);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection getSelectedFeatures();
    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isSelected(Feature f);

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void addFeature(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    void addFeatures(Collection<Feature> cf);
    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    void removeFeatures(Collection<Feature> cf);
    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void removeFeature(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    void substituteFeatures(Collection<Feature> cf);
    /**
     * DOCUMENT ME!
     */
    void removeAllFeatures();

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void holdFeature(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void unholdFeature(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isHoldFeature(Feature f);
    /**
     * DOCUMENT ME!
     *
     * @param  holdAll  DOCUMENT ME!
     */
    void setHoldAll(boolean holdAll);

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    void reconsiderFeature(Feature f);
}
