/*
 * DefaultFeatureCollection.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 8. Mai 2006, 12:11
 *
 */
package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.gui.MapListener;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.veto.VetoException;
import de.cismet.veto.VetoListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class DefaultFeatureCollection implements FeatureCollection, MapListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected Vector<Feature> features = new Vector<Feature>();
    protected Vector<FeatureCollectionListener> listeners = new Vector<FeatureCollectionListener>();
    protected LinkedHashSet<Feature> holdFeatures = new LinkedHashSet<Feature>();
    protected LinkedHashSet<Feature> selectedFeatures = new LinkedHashSet<Feature>();
    protected boolean holdAll = false;
    private boolean singleSelection = false;
    private ArrayList<VetoListener> vetoListeners = new ArrayList<VetoListener>();

    /** Creates a new instance of DefaultFeatureCollection */
    public DefaultFeatureCollection() {
    }

    //Wird eigentlich nicht mehr benoetigt
    public void setFeatureAt(Feature feature, int index) {
        if (!features.contains(feature)) {
            features.add(index, feature);
        }
    }

    public Feature getFeature(int index) {
        try {
            return features.get(index);
        } catch (Exception e) {
            log.fatal("error in getFeature:" + index, e);//NOI18N
            return null;
        }
    }

    public void addVetoableSelectionListener(VetoListener vetoListener) {
        if (vetoListener != null) {
            vetoListeners.add(vetoListener);
        }
    }

    public void removeVetoableSelectionListener(VetoListener vetoListener) {
        if (vetoListener != null) {
            vetoListeners.remove(vetoListener);
        }
    }

    public void setEnabled(boolean enabled) {
    }

    public void addFeatureCollectionListener(FeatureCollectionListener l) {
        listeners.add(l);
    }

    public void removeFeatureCollectionListener(FeatureCollectionListener l) {
        listeners.remove(l);
    }

    public void setTranslucency(float t) {
    }

    public boolean canBeDisabled() {
        return false;
    }

    public Vector getAllFeatures() {
        return features;
    }

    public int getFeatureCount() {
        return features.size();
    }

    public void setLayerPosition(int layerPosition) {
    }

    public int getLayerPosition() {
        return 0;
    }

    public String getName() {
        return "DefaultFeatureCollection";//NOI18N
    }

    public float getTranslucency() {
        return 1.0f;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean areFeaturesEditable() {
        Vector<Feature> vf = new Vector<Feature>(features);
        for (Feature f : vf) {
            if (f.isEditable()) {
                return true;
            }
        }
        return false;
    }

    public void unholdFeature(Feature f) {
        holdFeatures.remove(f);
        Vector v = new Vector();
        v.add(f);
        fireFeaturesChanged(v);
    }

    public void holdFeature(Feature f) {
        try {
            holdFeatures.add(f);
            Vector v = new Vector();
            v.add(f);
            fireFeaturesChanged(v);
        } catch (Throwable t) {
            log.error("Error during hold", t);//NOI18N
        }

    }

    public boolean isHoldFeature(Feature f) {
        return holdFeatures.contains(f);
    }

    public void setHoldAll(boolean holdAll) {
        this.holdAll = holdAll;
        if (holdAll) {
            holdFeatures.addAll(features);
        } else {
            holdFeatures.clear();
        }
        fireFeaturesChanged(features);
    }

    private boolean assertNoVeto() {
        for (VetoListener curVetoListener : vetoListeners) {
            try {
                curVetoListener.veto();
            } catch (VetoException ex) {
                return false;
            }
        }
        return true;
    }

    public void select(Feature f) {
        if (!assertNoVeto()) {
            return;
        }
        enforceSelect(f);
    }

    private void enforceSelect(Feature f) {
        log.debug("select(Feature f):" + f);//NOI18N
        enforceUnselectAll();
        selectedFeatures.add(f);
        fireSelectionChanged(f);
    }

    public void select(Collection<Feature> cf) {
        if (!assertNoVeto()) {
            return;
        }
        enforceSelect(cf);
    }

    private void enforceSelect(Collection<Feature> cf) {
        log.debug("select(Collection<Feature> cf):" + cf);//NOI18N
        enforceUnselectAll(true);
        selectedFeatures.addAll(cf);
        fireSelectionChanged(cf);
    }

    public void addToSelection(Feature f) {
        if (!assertNoVeto()) {
            return;
        }
        enforceAddToSelection(f);
    }

    private void enforceAddToSelection(Feature f) {
        Vector<Feature> v = new Vector<Feature>();
        v.add(f);
        enforceAddToSelection(v);
    }

    public void addToSelection(Collection<Feature> cf) {
//        if (singleSelection) {
//            unselectAll();
//        }
        if (!assertNoVeto()) {
            return;
        }
        enforceAddToSelection(cf);
    }

    private void enforceAddToSelection(Collection<Feature> cf) {
        selectedFeatures.addAll(cf);
        fireSelectionChanged(cf);
    }

    public void unselect(Feature f) {
        if (!assertNoVeto()) {
            return;
        }
        log.debug("unselect(Feature f):" + f);//NOI18N
        enforceUnselect(f);
    }

    private void enforceUnselect(Feature f) {
        log.debug("unselect(Feature f):" + f);//NOI18N
        selectedFeatures.remove(f);
        fireSelectionChanged(f);
    }

    public void unselectAll() {
        if (!assertNoVeto()) {
            return;
        }
        enforceUnselectAll();
    }

    private void enforceUnselectAll() {
        enforceUnselectAll(false);
    }

    public void unselectAll(boolean quiet) {
        if (!assertNoVeto()) {
            return;
        }
        enforceUnselectAll(quiet);
    }

    private void enforceUnselectAll(boolean quiet) {
        if (selectedFeatures.size() > 0) {
            log.debug("unselectAll()");//NOI18N
            LinkedHashSet<Feature> lhs = new LinkedHashSet<Feature>(selectedFeatures);
            selectedFeatures.clear();
            if (!quiet) {
                fireSelectionChanged(lhs);
            }
        }
    }

    public void unselect(Collection<Feature> cf) {
        if (!assertNoVeto()) {
            return;
        }
        enforceUnselect(cf);
    }

    private void enforceUnselect(Collection<Feature> cf) {
        log.debug("unselect(Collection<Feature> cf):" + cf);//NOI18N
        selectedFeatures.removeAll(cf);
        fireSelectionChanged();
    }

    public Collection getSelectedFeatures() {
        return new LinkedHashSet<Feature>(selectedFeatures);
    }

    public boolean isSelected(Feature f) {
        return selectedFeatures.contains(f);
    }

    public void removeFeature(Feature f) {
        log.debug("before removal:" + features);//NOI18N
        boolean removed = false;
        log.debug("featureToRemove: " + f);//NOI18N
        log.debug("Feature sizes: " + features.size());//NOI18N
        log.debug("Features: " + features);//NOI18N
        removed = features.remove(f);
        log.debug("feature removed: " + removed);//NOI18N
        removed = holdFeatures.remove(f);
        log.debug("holdFeature removed: " + removed);//NOI18N
        removed = selectedFeatures.remove(f);
        log.debug("selected removed: " + removed);//NOI18N
        Vector v = new Vector();
        v.add(f);
        fireFeaturesRemoved(v);
        log.debug("after removal:" + features);//NOI18N
    }

    public void removeFeatures(Collection<Feature> cf) {
        features.removeAll(cf);
        holdFeatures.removeAll(cf);
        fireFeaturesRemoved(cf);
    }

    public void addFeature(Feature f) {
        log.debug("addFeature(Feature f):" + f);//NOI18N
        if (f != null && f.getGeometry() != null && !features.contains(f)) {
            features.add(f);
            Vector v = new Vector();
            v.add(f);
            fireFeaturesAdded(v);
        } else {
            log.warn("Feature was not added. It is either null or getGeometry() is null or it is already in the Collection.");//NOI18N
        }

    }

    public void addFeatures(Collection<Feature> cf) {
        log.debug("addFeatures(Collection<Feature> cf):" + cf);//NOI18N
        Vector<Feature> v = new Vector<Feature>();
        for (Feature f : cf) {
            if (!features.contains(f)) {
                features.add(f);
                if (holdAll) {
                    holdFeatures.add(f);
                }
                v.add(f);
            }
        }
        if (v.size() > 0) {
            fireFeaturesAdded(v);
        }
    }

    public void substituteFeatures(Collection<Feature> cf) {
        try {
            log.debug("substitute: delete all");//NOI18N
            removeAllFeatures();
            log.debug("substitute: add:" + cf);//NOI18N
            addFeatures(cf);
            Feature f = (Feature) cf.toArray()[cf.size() - 1];
        } catch (Exception e) {
            log.error("Error in substituteFeatures new features:" + cf, e);//NOI18N
        }
        //select(f);
    }

    public void clear() {
        holdFeatures.clear();
        removeAllFeatures();
    }

    public Collection<Feature> getHoldFeatures() {
        return holdFeatures;
    }

    public void removeAllFeatures() {
        Vector<Feature> cf = new Vector<Feature>(features);
        features.removeAllElements();
        fireAllFeaturesRemoved(cf);
        addFeatures(holdFeatures);
    }

    public void reconsiderFeature(Feature f) {
//        boolean hold=isHoldFeature(f);
//        boolean selected=selectedFeatures.contains(f);
//        removeFeature(f);
//        addFeature(f);
//        if (hold) {
//            //nicht mit Holdfeature, da sonst Endlosschleife
//            holdFeatures.add(f);
//        }
//        select(f);
        Vector v = new Vector();
        v.add(f);
        log.debug("reconsiderFeature(Feature f):" + f, new CurrentStackTrace());//NOI18N
        Vector<FeatureCollectionListener> listenersCopy = new Vector<FeatureCollectionListener>(listeners); //No concurrentModification possible
        for (Iterator<FeatureCollectionListener> it = listenersCopy.iterator(); it.hasNext();) {
            it.next().featureReconsiderationRequested(new FeatureCollectionEvent(this, v));
        }


    }

    public void fireFeaturesAdded(Collection<Feature> cf) {
        for (FeatureCollectionListener curListener : listeners) {
            if (curListener instanceof MappingComponent) {
                log.debug("adding featuresTo Map");//NOI18N
                ((MappingComponent) curListener).addFeaturesToMap(cf.toArray(new Feature[0]));
            }
        }
    }

    public void fireFeaturesRemoved(Collection<Feature> cf) {
        Vector<FeatureCollectionListener> listenersCopy = new Vector<FeatureCollectionListener>(listeners); //No concurrentModification possible
        log.debug("fireFeaturesRemoved");//NOI18N
        for (Iterator<FeatureCollectionListener> it = listenersCopy.iterator(); it.hasNext();) {
            it.next().featuresRemoved(new FeatureCollectionEvent(this, cf));
        }
    }

    public void fireAllFeaturesRemoved(Collection<Feature> cf) {
        Vector<FeatureCollectionListener> listenersCopy = new Vector<FeatureCollectionListener>(listeners); //No concurrentModification possible
        log.debug("fireAllFeaturesRemoved");//NOI18N
        for (Iterator<FeatureCollectionListener> it = listenersCopy.iterator(); it.hasNext();) {
            it.next().allFeaturesRemoved(new FeatureCollectionEvent(this, cf));
        }
    }

    public void fireFeaturesChanged(Collection<Feature> cf) {
        Vector<FeatureCollectionListener> listenersCopy = new Vector<FeatureCollectionListener>(listeners); //No concurrentModification possible
        log.debug("fireFeaturesChanged");//NOI18N
        for (Iterator<FeatureCollectionListener> it = listenersCopy.iterator(); it.hasNext();) {
            it.next().featuresChanged(new FeatureCollectionEvent(this, cf));
        }
    }

    public void fireSelectionChanged() {
        fireSelectionChanged((Collection<Feature>) null);
    }

    public void fireSelectionChanged(Collection<Feature> cf) {
        Vector<FeatureCollectionListener> listenersCopy = new Vector<FeatureCollectionListener>(listeners); //No concurrentModification possible
        log.debug("fireSelectionChanged");//NOI18N
        for (Iterator<FeatureCollectionListener> it = listenersCopy.iterator(); it.hasNext();) {
            it.next().featureSelectionChanged(new FeatureCollectionEvent(this, cf));
        }
    }

    public void fireSelectionChanged(Feature f) {
        Vector<Feature> v = new Vector<Feature>();
        v.add(f);
        fireSelectionChanged(v);
    }

    public void setName(String name) {
    }

    public void removeFeaturesByInstance(Class c) {
        Vector<Feature> af = new Vector<Feature>(features);
        for (Feature f : af) {
            if (c.isInstance(f)) {
                removeFeature(f);
            }
        }

    }

    public boolean isSingleSelection() {
        return singleSelection;
    }

    public void setSingleSelection(boolean singleSelection) {
        this.singleSelection = singleSelection;
    }

    public void featuresAddedToMap(Collection<Feature> cf) {
        log.debug("fireFeaturesAddedToMap");//NOI18N
        Vector<FeatureCollectionListener> listenersCopy = new Vector<FeatureCollectionListener>(listeners); //No concurrentModification possible
        for (Iterator<FeatureCollectionListener> it = listenersCopy.iterator(); it.hasNext();) {
            it.next().featuresAdded(new FeatureCollectionEvent(this, cf));
        }
    }
}
