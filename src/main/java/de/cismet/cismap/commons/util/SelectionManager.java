/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RepaintEvent;
import de.cismet.cismap.commons.retrieval.RepaintListener;

/**
 * Determines the selected features and sort them by their corresponding service.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectionManager implements FeatureCollectionListener, ListSelectionListener {

    //~ Instance fields --------------------------------------------------------

    // this maps contains pre-calculated values
    private final Map<AbstractFeatureService, Set<Feature>> selectedFeatures =
        new HashMap<AbstractFeatureService, Set<Feature>>();
    private final Map<AbstractFeatureService, Integer> modifiableFeaturesCount =
        new HashMap<AbstractFeatureService, Integer>();

    // The selected elements of this table will be considered
    private final HashMap<AbstractFeatureService, AttributeTable> consideredAttributeTables =
        new HashMap<AbstractFeatureService, AttributeTable>();
    private List<AbstractFeatureService> editableServices = new ArrayList<AbstractFeatureService>();
    private final List<SelectionChangedListener> listener = new ArrayList<SelectionChangedListener>();
    private boolean selectionChangeInProgress = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionManager object.
     */
    private SelectionManager() {
        editableServices = Collections.synchronizedList(editableServices);
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        mc.getFeatureCollection().addFeatureCollectionListener(this);
        mc.addRepaintListener(new RepaintListener() {

                @Override
                public void repaintStart(final RepaintEvent e) {
                }

                @Override
                public void repaintComplete(final RepaintEvent e) {
                    if (e.getRetrievalEvent().getRetrievalService() instanceof AbstractFeatureService) {
                        synchronizeSelectionWithMap(
                            (AbstractFeatureService)e.getRetrievalEvent().getRetrievalService());
                    }
                }

                @Override
                public void repaintError(final RepaintEvent e) {
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * synchronizes the table selection with the PFeatures in the map.
     *
     * @param  service  DOCUMENT ME!
     */
    private void synchronizeSelectionWithMap(final AbstractFeatureService service) {
        final List<PFeature> features = new ArrayList<PFeature>();
        if (service.getPNode() != null) {
            features.addAll(service.getPNode().getChildrenReference());
        }
        final List<Feature> selectedServiceFeatures = getSelectedFeatures(service);

        if (selectedServiceFeatures != null) {
            final int[] selectedFeatureIds = new int[selectedServiceFeatures.size()];
            final List<Feature> featuresToSelect = new ArrayList<Feature>();
            final List<Feature> featuresToUnselect = new ArrayList<Feature>();
            int index = -1;

            for (final Feature f : selectedServiceFeatures) {
                if (f instanceof FeatureWithId) {
                    selectedFeatureIds[++index] = ((FeatureWithId)f).getId();
                }
            }

            Arrays.sort(selectedFeatureIds);
            final SelectionListener sl = (SelectionListener)CismapBroker.getInstance().getMappingComponent()
                        .getInputEventListener()
                        .get(MappingComponent.SELECT);

            for (final PFeature pfeature : features) {
                final Feature feature = pfeature.getFeature();

                if (feature instanceof FeatureWithId) {
                    final boolean selected = Arrays.binarySearch(
                            selectedFeatureIds,
                            ((FeatureWithId)feature).getId()) >= 0;

                    if (selected != pfeature.isSelected()) {
                        pfeature.setSelected(selected);
                    }
                    if (selected) {
                        sl.addSelectedFeature(pfeature);
                        featuresToSelect.add(feature);
                    } else {
                        sl.removeSelectedFeature(pfeature);
                        featuresToUnselect.add(feature);
                    }
                }
            }
            selectionChangeInProgress = true;
            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addToSelection(featuresToSelect);
            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unselect(featuresToUnselect);
            selectionChangeInProgress = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureList  DOCUMENT ME!
     */
    public void addSelectedFeatures(final List<? extends Feature> featureList) {
        setSelectedFeatures(featureList, false, true);
        fireSelectionChangedEvent();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureList  DOCUMENT ME!
     */
    public void setSelectedFeatures(final List<? extends Feature> featureList) {
        setSelectedFeatures(featureList, true, true);
        fireSelectionChangedEvent();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service      DOCUMENT ME!
     * @param  featureList  DOCUMENT ME!
     */
    public void setSelectedFeaturesForService(final AbstractFeatureService service,
            final List<? extends Feature> featureList) {
        selectedFeatures.put(service, null);
        removeSelectionFromMap(service);

        for (final Feature f : featureList) {
            if (f instanceof DefaultFeatureServiceFeature) {
                final DefaultFeatureServiceFeature fsf = (DefaultFeatureServiceFeature)f;

                if ((fsf.getLayerProperties() != null) && (fsf.getLayerProperties().getFeatureService() != null)) {
                    Set<Feature> list = selectedFeatures.get(fsf.getLayerProperties().getFeatureService());

                    if (list == null) {
                        list = new HashSet<Feature>();
                        selectedFeatures.put(fsf.getLayerProperties().getFeatureService(), list);
                    }

                    list.add(fsf);
                }
            }
        }

        synchronizeSelectionWithMap(service);

        fireSelectionChangedEvent();
    }

    /**
     * Removes the given feature from the selection.
     *
     * @param  feature  the feature to remove
     */
    public void removeSelectedFeatures(final Feature feature) {
        removeSelectedFeatures(Collections.nCopies(1, feature));
    }

    /**
     * Removes the given features from the selection.
     *
     * @param  featureList  the features to remove
     */
    public void removeSelectedFeatures(final List<? extends Feature> featureList) {
        final Map<AbstractFeatureService, TreeSet<DefaultFeatureServiceFeature>> selectedFeaturesToRemove =
            new HashMap<AbstractFeatureService, TreeSet<DefaultFeatureServiceFeature>>();

        if (featureList == null) {
            return;
        }

        // save the features ordered by the corresponding service
        for (final Feature f : featureList) {
            if (f instanceof DefaultFeatureServiceFeature) {
                final DefaultFeatureServiceFeature fsf = (DefaultFeatureServiceFeature)f;

                if ((fsf.getLayerProperties() != null) && (fsf.getLayerProperties().getFeatureService() != null)) {
                    TreeSet<DefaultFeatureServiceFeature> list = selectedFeaturesToRemove.get(
                            fsf.getLayerProperties().getFeatureService());

                    if (list == null) {
                        list = new TreeSet<DefaultFeatureServiceFeature>();
                        selectedFeaturesToRemove.put(fsf.getLayerProperties().getFeatureService(), list);
                    }

                    list.add(fsf);
                }
            }
        }

        for (final AbstractFeatureService service : selectedFeaturesToRemove.keySet()) {
            final TreeSet<DefaultFeatureServiceFeature> list = selectedFeaturesToRemove.get(service);
            final Set<Feature> features = selectedFeatures.get(service);

            if (features != null) {
                for (final DefaultFeatureServiceFeature f : list) {
                    if (features.contains(f)) {
                        features.remove(f);
                    }
                }
            }

            // remove selected features from the map
            final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
            final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
            final List<PFeature> sel = sl.getAllSelectedPFeatures();
            final List<Feature> toBeUnselected = new ArrayList<Feature>();

            for (final PFeature feature : sel) {
                if (feature.getFeature() instanceof FeatureServiceFeature) {
                    final FeatureServiceFeature fsf = (FeatureServiceFeature)feature.getFeature();

                    if ((fsf.getLayerProperties() != null) && featureList.contains(fsf)
                                && feature.isSelected()) {
                        feature.setSelected(false);
                        sl.removeSelectedFeature(feature);
                        toBeUnselected.add(feature.getFeature());
                    }
                }
            }
            selectionChangeInProgress = true;
            ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                    .unselect(
                        toBeUnselected);
            selectionChangeInProgress = false;
            fireSelectionChangedEvent();
        }
    }

    /**
     * Set the selected features of a specific service.
     *
     * @param  featureList         DOCUMENT ME!
     * @param  removeOldSelection  service DOCUMENT ME!
     * @param  syncMap             DOCUMENT ME!
     */
    private void setSelectedFeatures(final List<? extends Feature> featureList,
            final boolean removeOldSelection,
            final boolean syncMap) {
        if (removeOldSelection) {
            selectedFeatures.clear();

            if (syncMap) {
                removeSelectionFromMap();
            }
        }

        for (final Feature f : featureList) {
            if (f instanceof DefaultFeatureServiceFeature) {
                final DefaultFeatureServiceFeature fsf = (DefaultFeatureServiceFeature)f;

                if ((fsf.getLayerProperties() != null) && (fsf.getLayerProperties().getFeatureService() != null)) {
                    Set<Feature> list = selectedFeatures.get(fsf.getLayerProperties().getFeatureService());

                    if (list == null) {
                        list = new HashSet<Feature>();
                        selectedFeatures.put(fsf.getLayerProperties().getFeatureService(), list);
                    }

                    list.add(fsf);
                }
            }
        }

        if (syncMap) {
            for (final AbstractFeatureService service : selectedFeatures.keySet()) {
                synchronizeSelectionWithMap(service);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void removeSelectionFromMap() {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> sel = sl.getAllSelectedPFeatures();
        final List<Feature> toBeUnselected = new ArrayList<Feature>();

        for (final PFeature feature : sel) {
            if (feature.isSelected()) {
                feature.setSelected(false);
                sl.removeSelectedFeature(feature);
                toBeUnselected.add(feature.getFeature());
            }
        }

        selectionChangeInProgress = true;
        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection()).unselect(
            toBeUnselected);
        selectionChangeInProgress = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    private void removeSelectionFromMap(final AbstractFeatureService service) {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> sel = sl.getAllSelectedPFeatures();
        final List<Feature> toBeUnselected = new ArrayList<Feature>();

        for (final PFeature feature : sel) {
            if (feature.getFeature() instanceof FeatureServiceFeature) {
                final FeatureServiceFeature fsf = (FeatureServiceFeature)feature.getFeature();

                if ((fsf.getLayerProperties() != null) && fsf.getLayerProperties().getFeatureService().equals(service)
                            && feature.isSelected()) {
                    feature.setSelected(false);
                    sl.removeSelectedFeature(feature);
                    toBeUnselected.add(feature.getFeature());
                }
            }
        }
        selectionChangeInProgress = true;
        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection()).unselect(
            toBeUnselected);
        selectionChangeInProgress = false;
    }

    /**
     * DOCUMENT ME!
     */
    public void clearSelection() {
        selectedFeatures.clear();

        removeSelectionFromMap();

        fireSelectionChangedEvent();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public void clearSelection(final AbstractFeatureService service) {
        selectedFeatures.put(service, null);

        removeSelectionFromMap(service);

        fireSelectionChangedEvent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Feature> getSelectedFeatures() {
        final List<Feature> features = new ArrayList<Feature>();

        for (final AbstractFeatureService service : selectedFeatures.keySet()) {
            final Set<Feature> serviceFeatures = selectedFeatures.get(service);

            if (serviceFeatures != null) {
                features.addAll(serviceFeatures);
            }
        }

        return features;
    }

    /**
     * Returns the selected features of the given service.
     *
     * @param   service  all selected features of this service will be returned
     *
     * @return  The selected features of the given service
     */
    public List<Feature> getSelectedFeatures(final AbstractFeatureService service) {
        final Set set = selectedFeatures.get(service);

        if (set != null) {
            return new ArrayList<Feature>(set);
        } else {
            return null;
        }
    }

    /**
     * Returns the instance of the SelectionManager and creates a new one, if required.
     *
     * @return  The instance of the SelectionManager
     */
    public static SelectionManager getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * Determines the number of selected features for the given service.
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  The number of selected features of the given service
     */
    public Integer getSelectedFeaturesCount(final AbstractFeatureService service) {
        if (service == null) {
            return null;
        }
        final Set<Feature> features = selectedFeatures.get(service);

        if (features == null) {
            return 0;
        } else {
            return features.size();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  The number of modifiable features of the given service
     */
    public Integer getModifiableFeaturesCount(final AbstractFeatureService service) {
        if (service == null) {
            return null;
        }

        if (editableServices.contains(service)) {
            return getSelectedFeaturesCount(service);
        } else {
            return null;
        }
    }

    /**
     * Switch the processing mode of the given service.
     *
     * @param  service  DOCUMENT ME!
     */
    public void switchProcessingMode(final AbstractFeatureService service) {
        if (editableServices.contains(service)) {
            editableServices.remove(service);
        } else {
            editableServices.add(service);
        }
        fireSelectionChangedEvent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  All modifiable services
     */
    public List<AbstractFeatureService> getEditableServices() {
        return editableServices;
    }

    /**
     * Adds an AttributeTable that should be considered, when the selected features will be determined.
     *
     * @param  table  DOCUMENT ME!
     */
    public void addConsideredAttributeTable(final AttributeTable table) {
        consideredAttributeTables.put(table.getFeatureService(), table);

        table.addListSelectionListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  table  DOCUMENT ME!
     */
    public void removeConsideredAttributeTable(final AttributeTable table) {
        consideredAttributeTables.remove(table.getFeatureService());
        table.removeListSelectionListener(this);
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
//        for (final AttributeTable table : consideredAttributeTables.values()) {
//            table.applySelection(this, new ArrayList<Feature>(), true);
//        }
//
//        selectedStandaloneFeatures.clear();
        if (selectionChangeInProgress) {
            return;
        }
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        final boolean featuresAdded = sl.isFeatureAdded();

        if ((fce != null) && (fce.getFeatureCollection() != null)
                    && (fce.getFeatureCollection().getSelectedFeatures() != null)) {
            final List<Feature> selectedMapFeatures = new ArrayList<Feature>((Collection<Feature>)
                    fce.getFeatureCollection().getSelectedFeatures());

            setSelectedFeatures(selectedMapFeatures, !featuresAdded, false);

            if (sl.getLastUnselectedFeatures() != null) {
                final Set<Feature> deselectedFeatures = new HashSet<Feature>();
                deselectedFeatures.addAll(sl.getLastUnselectedFeatures());
                deselectedFeatures.removeAll(selectedMapFeatures);
                final List<Feature> featuresToDeselect = new ArrayList(deselectedFeatures);

                if (!featuresToDeselect.isEmpty()) {
                    removeSelectedFeatures(featuresToDeselect);
                }
            }
        }

        fireSelectionChangedEvent();
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
//        if (!e.getValueIsAdjusting()) {
//            fireSelectionChangedEvent();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addSelectionChangedListener(final SelectionChangedListener l) {
        this.listener.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void removeSelectionChangedListener(final SelectionChangedListener l) {
        this.listener.remove(l);
    }

    /**
     * DOCUMENT ME!
     */
    private void fireSelectionChangedEvent() {
        final SelectionChangedEvent e = new SelectionChangedEvent(this);

        for (final SelectionChangedListener l : listener) {
            l.selectionChanged(e);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        static final SelectionManager INSTANCE = new SelectionManager();
    }
}
