/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.cismet.cismap.commons.gui.MapListener;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.veto.VetoException;
import de.cismet.veto.VetoListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class DefaultFeatureCollection implements FeatureCollection, MapListener {

    //~ Instance fields --------------------------------------------------------

    protected List<Feature> features = new ArrayList<Feature>();
    protected final Set<FeatureCollectionListener> listeners = new HashSet<FeatureCollectionListener>();
    protected LinkedHashSet<Feature> holdFeatures = new LinkedHashSet<Feature>();
    protected Set<Feature> selectedFeatures = Collections.newSetFromMap(new ConcurrentHashMap<Feature, Boolean>());
    protected boolean holdAll = false;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean singleSelection = false;
    private ArrayList<VetoListener> vetoListeners = new ArrayList<VetoListener>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of DefaultFeatureCollection.
     */
    public DefaultFeatureCollection() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Wird eigentlich nicht mehr benoetigt.
     *
     * @param  feature  DOCUMENT ME!
     * @param  index    DOCUMENT ME!
     */
    public void setFeatureAt(final Feature feature, final int index) {
        if (!features.contains(feature)) {
            features.add(index, feature);
        }
    }

    @Override
    public Feature getFeature(final int index) {
        try {
            if (index < features.size()) {
                return features.get(index);
            } else {
                log.warn("feature does not exist anymore:" + index); // NOI18N
                return null;
            }
        } catch (Exception e) {
            log.fatal("error in getFeature:" + index, e);            // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  vetoListener  DOCUMENT ME!
     */
    public void addVetoableSelectionListener(final VetoListener vetoListener) {
        if (vetoListener != null) {
            vetoListeners.add(vetoListener);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  vetoListener  DOCUMENT ME!
     */
    public void removeVetoableSelectionListener(final VetoListener vetoListener) {
        if (vetoListener != null) {
            vetoListeners.remove(vetoListener);
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
    }

    @Override
    public void addFeatureCollectionListener(final FeatureCollectionListener l) {
        listeners.add(l);
    }

    @Override
    public void removeFeatureCollectionListener(final FeatureCollectionListener l) {
        listeners.remove(l);
    }

    @Override
    public void setTranslucency(final float t) {
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public List<Feature> getAllFeatures() {
        return features;
    }

    @Override
    public int getFeatureCount() {
        return features.size();
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
    }

    @Override
    public int getLayerPosition() {
        return 0;
    }

    @Override
    public String getName() {
        return "DefaultFeatureCollection"; // NOI18N
    }

    @Override
    public float getTranslucency() {
        return 1.0f;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean areFeaturesEditable() {
        final List<Feature> vf = new ArrayList<Feature>(features);

        for (final Feature f : vf) {
            if (f.isEditable()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void unholdFeature(final Feature f) {
        holdFeatures.remove(f);
        final Set<Feature> v = new HashSet<Feature>(2);
        v.add(f);
        fireFeaturesChanged(v);
    }

    @Override
    public void holdFeature(final Feature f) {
        try {
            holdFeatures.add(f);
            final Set<Feature> v = new HashSet<Feature>(2);
            v.add(f);
            fireFeaturesChanged(v);
        } catch (Throwable t) {
            log.error("Error during hold", t); // NOI18N
        }
    }

    @Override
    public boolean isHoldFeature(final Feature f) {
        return holdFeatures.contains(f);
    }

    @Override
    public void setHoldAll(final boolean holdAll) {
        this.holdAll = holdAll;
        if (holdAll) {
            holdFeatures.addAll(features);
        } else {
            holdFeatures.clear();
        }
        fireFeaturesChanged(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean assertNoVeto() {
        for (final VetoListener curVetoListener : vetoListeners) {
            try {
                curVetoListener.veto();
            } catch (VetoException ex) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void select(final Feature f) {
        if (!assertNoVeto()) {
            return;
        }
        enforceSelect(f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    private void enforceSelect(final Feature f) {
        if (log.isDebugEnabled()) {
            log.debug("select(Feature f):" + f); // NOI18N
        }
        enforceUnselectAll();
        selectedFeatures.add(f);
        fireSelectionChanged(f);
    }

    @Override
    public void select(final Collection<Feature> cf) {
        if (!assertNoVeto()) {
            return;
        }
        enforceSelect(cf);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    private void enforceSelect(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("select(Collection<Feature> cf):" + cf); // NOI18N
        }
        enforceUnselectAll(true);
        selectedFeatures.addAll(cf);
        fireSelectionChanged(cf);
    }

    @Override
    public void addToSelection(final Feature f) {
        if (!assertNoVeto()) {
            return;
        }
        enforceAddToSelection(f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    private void enforceAddToSelection(final Feature f) {
        final Set<Feature> v = new HashSet<Feature>(2);
        v.add(f);
        enforceAddToSelection(v);
    }

    @Override
    public void addToSelection(final Collection<Feature> cf) {
        if (assertNoVeto()) {
            enforceAddToSelection(cf);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    private void enforceAddToSelection(final Collection<Feature> cf) {
        selectedFeatures.addAll(cf);
        fireSelectionChanged(cf);
    }

    @Override
    public void unselect(final Feature f) {
        if (assertNoVeto()) {
            if (log.isDebugEnabled()) {
                log.debug("unselect(Feature f):" + f); // NOI18N
            }
            enforceUnselect(f);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    private void enforceUnselect(final Feature f) {
        if (log.isDebugEnabled()) {
            log.debug("unselect(Feature f):" + f); // NOI18N
        }
        selectedFeatures.remove(f);
        fireSelectionChanged(f);
    }

    @Override
    public void unselectAll() {
        if (assertNoVeto()) {
            enforceUnselectAll();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void enforceUnselectAll() {
        enforceUnselectAll(false);
    }

    @Override
    public void unselectAll(final boolean quiet) {
        if (assertNoVeto()) {
            enforceUnselectAll(quiet);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  quiet  DOCUMENT ME!
     */
    private void enforceUnselectAll(final boolean quiet) {
        if (selectedFeatures.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("unselectAll()"); // NOI18N
            }
            final LinkedHashSet<Feature> lhs = new LinkedHashSet<Feature>(selectedFeatures);
            selectedFeatures.clear();
            if (!quiet) {
                fireSelectionChanged(lhs);
            }
        }
    }

    @Override
    public void unselect(final Collection<Feature> cf) {
        if (assertNoVeto()) {
            enforceUnselect(cf);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    private void enforceUnselect(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("unselect(Collection<Feature> cf):" + cf); // NOI18N
        }
        selectedFeatures.removeAll(cf);
        fireSelectionChanged();
    }

    @Override
    public Collection getSelectedFeatures() {
        return new LinkedHashSet<Feature>(selectedFeatures);
    }

    @Override
    public boolean isSelected(final Feature f) {
        return selectedFeatures.contains(f);
    }

    @Override
    public void removeFeature(final Feature f) {
        if (log.isDebugEnabled()) {
            log.debug("before removal:" + features);        // NOI18N
        }
        boolean removed = false;
        if (log.isDebugEnabled()) {
            log.debug("featureToRemove: " + f);             // NOI18N
            log.debug("Feature sizes: " + features.size()); // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("Features: " + features);             // NOI18N
        }
        removed = features.remove(f);
        if (log.isDebugEnabled()) {
            log.debug("feature removed: " + removed);       // NOI18N
        }
        removed = holdFeatures.remove(f);
        if (log.isDebugEnabled()) {
            log.debug("holdFeature removed: " + removed);   // NOI18N
        }
        removed = selectedFeatures.remove(f);
        if (log.isDebugEnabled()) {
            log.debug("selected removed: " + removed);      // NOI18N
        }
        final Set<Feature> v = new HashSet<Feature>(2);
        v.add(f);
        fireFeaturesRemoved(v);
        if (log.isDebugEnabled()) {
            log.debug("after removal:" + features);         // NOI18N
        }
    }

    @Override
    public void removeFeatures(final Collection<Feature> cf) {
        features.removeAll(cf);
        holdFeatures.removeAll(cf);
        fireFeaturesRemoved(cf);
    }

    @Override
    public void addFeature(final Feature f) {
        if (log.isDebugEnabled()) {
            log.debug("addFeature(Feature f):" + f);                                                                      // NOI18N
        }
        if ((f != null) && (f.getGeometry() != null) && !features.contains(f)) {
            features.add(f);
            final Set<Feature> v = new HashSet<Feature>(2);
            v.add(f);
            checkForAndCorrectDoubleNaming(f);
            fireFeaturesAdded(v);
        } else {
            log.warn(
                "Feature was not added. It is either null or getGeometry() is null or it is already in the Collection."); // NOI18N
        }
    }

    @Override
    public void addFeatures(final Collection<? extends Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("addFeatures(Collection<Feature> cf):" + cf); // NOI18N
        }
        final List<Feature> v = new ArrayList<Feature>();
        for (final Feature f : cf) {
            if (!features.contains(f)) {
                features.add(f);
                if (holdAll) {
                    holdFeatures.add(f);
                }
                v.add(f);
                checkForAndCorrectDoubleNaming(f);
            }
        }

        if (v.size() > 0) {
            fireFeaturesAdded(v);
        }
    }

    @Override
    public void substituteFeatures(final Collection<Feature> cf) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("substitute: delete all");                        // NOI18N
            }
            removeAllFeatures();
            if (log.isDebugEnabled()) {
                log.debug("substitute: add:" + cf);                         // NOI18N
            }
            addFeatures(cf);
        } catch (Exception e) {
            log.error("Error in substituteFeatures new features:" + cf, e); // NOI18N
        }
        // select(f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void checkForAndCorrectDoubleNaming(final Feature feature) {
        if (feature instanceof PreventNamingDuplicates) {
            final PreventNamingDuplicates changeableFeature = (PreventNamingDuplicates)feature;
            final ArrayList<PreventNamingDuplicates> list = new ArrayList<PreventNamingDuplicates>();

            for (final Feature f : features) {
                if (f.getClass().equals(feature.getClass()) && (f instanceof PreventNamingDuplicates)) {
                    list.add((PreventNamingDuplicates)f);
                }
            }

            if (list.size() > 1) {
                final Map<PreventNamingDuplicates, Integer> sameNames = new HashMap<PreventNamingDuplicates, Integer>();

                for (final PreventNamingDuplicates f : list) {
                    if (f.getOriginalName().equals(changeableFeature.getOriginalName())) {
                        sameNames.put(f, f.getNumber());
                    }
                }

                if (sameNames.size() < 2) {
                    changeableFeature.setNumber(1);
                } else {
                    int i = 1;
                    sameNames.remove(changeableFeature);

                    while ((i < 10000) && sameNames.containsValue(i)) {
                        ++i;
                    }

                    changeableFeature.setNumber(i);
                }
            } else {
                changeableFeature.setNumber(1);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void clear() {
        holdFeatures.clear();
        removeAllFeatures();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Feature> getHoldFeatures() {
        return holdFeatures;
    }

    @Override
    public void removeAllFeatures() {
        final Set<Feature> cf = new HashSet<Feature>(features);
        features.clear();
        fireAllFeaturesRemoved(cf);

        final ArrayList<Feature> list = new ArrayList<Feature>();

        for (final Feature f : holdFeatures) {
            if (f instanceof FeatureGroup) {
                list.addAll(FeatureGroups.expandAll((FeatureGroup)f));
            } else {
                list.add(f);
            }
        }

        addFeatures(list);
    }

    @Override
    public void reconsiderFeature(final Feature f) {
        final Set<Feature> reconsiderF = new HashSet<Feature>(2);
        reconsiderF.add(f);

        if (log.isDebugEnabled()) {
            log.debug("reconsiderFeature(Feature f):" + f, new CurrentStackTrace()); // NOI18N
        }

        checkForAndCorrectDoubleNaming(f);

        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            it.next().featureReconsiderationRequested(new FeatureCollectionEvent(this, reconsiderF));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    public void fireFeaturesAdded(final Collection<Feature> cf) {
        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            final FeatureCollectionListener curListener = it.next();
            if (curListener instanceof MappingComponent) {
                if (log.isDebugEnabled()) {
                    log.debug("adding featuresTo Map"); // NOI18N
                }
                ((MappingComponent)curListener).addFeaturesToMap(cf.toArray(new Feature[0]));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    public void fireFeaturesRemoved(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("fireFeaturesRemoved"); // NOI18N
        }

        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            it.next().featuresRemoved(new FeatureCollectionEvent(this, cf));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    public void fireAllFeaturesRemoved(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("fireAllFeaturesRemoved"); // NOI18N
        }

        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            it.next().allFeaturesRemoved(new FeatureCollectionEvent(this, cf));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    public void fireFeaturesChanged(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("fireFeaturesChanged"); // NOI18N
        }

        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            it.next().featuresChanged(new FeatureCollectionEvent(this, cf));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Iterator<FeatureCollectionListener> getListenerIterator() {
        final Iterator<FeatureCollectionListener> it;
        synchronized (listeners) {
            it = new HashSet<FeatureCollectionListener>(listeners).iterator();
        }

        return it;
    }

    /**
     * DOCUMENT ME!
     */
    public void fireSelectionChanged() {
        fireSelectionChanged((Collection<Feature>)null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    public void fireSelectionChanged(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("fireSelectionChanged"); // NOI18N
        }

        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            it.next().featureSelectionChanged(new FeatureCollectionEvent(this, cf));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    public void fireSelectionChanged(final Feature f) {
        final Set<Feature> v = new HashSet<Feature>(2);
        v.add(f);
        fireSelectionChanged(v);
    }

    @Override
    public void setName(final String name) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  c  DOCUMENT ME!
     */
    public void removeFeaturesByInstance(final Class c) {
        final List<Feature> af = new ArrayList<Feature>(features);

        for (final Feature f : af) {
            if (c.isInstance(f)) {
                removeFeature(f);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSingleSelection() {
        return singleSelection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  singleSelection  DOCUMENT ME!
     */
    public void setSingleSelection(final boolean singleSelection) {
        this.singleSelection = singleSelection;
    }

    @Override
    public void featuresAddedToMap(final Collection<Feature> cf) {
        if (log.isDebugEnabled()) {
            log.debug("fireFeaturesAddedToMap"); // NOI18N
        }

        final Iterator<FeatureCollectionListener> it = getListenerIterator();
        while (it.hasNext()) {
            it.next().featuresAdded(new FeatureCollectionEvent(this, cf));
        }
    }

    @Override
    public boolean contains(final Feature feature) {
        return features.contains(feature);
    }
}
