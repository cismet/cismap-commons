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

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RepaintEvent;
import de.cismet.cismap.commons.retrieval.RepaintListener;

import de.cismet.commons.concurrency.CismetExecutors;

import static java.lang.Thread.interrupted;

/**
 * Determines the selected features and sort them by their corresponding service.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectionManager implements FeatureCollectionListener, ListSelectionListener {

    //~ Instance fields --------------------------------------------------------

    // this maps contains pre-calculated values
    private final Map<AbstractFeatureService, List<Feature>> selectedFeatures =
        new HashMap<AbstractFeatureService, List<Feature>>();
    private final Map<AbstractFeatureService, Integer> selectedFeaturesCount =
        new HashMap<AbstractFeatureService, Integer>();
    private final Map<AbstractFeatureService, Integer> modifiableFeaturesCount =
        new HashMap<AbstractFeatureService, Integer>();

    // contains selected features, which was not selected by a table or the map
    private final Map<AbstractFeatureService, List<Feature>> selectedStandaloneFeatures =
        new HashMap<AbstractFeatureService, List<Feature>>();
    private UpdateSelectionThread selectionUpdateThread = null;
    private final ExecutorService executor = CismetExecutors.newSingleThreadExecutor();
    // The selected elements of this table will be considered
    private final HashMap<AbstractFeatureService, AttributeTable> consideredAttributeTables =
        new HashMap<AbstractFeatureService, AttributeTable>();
    private List<AbstractFeatureService> editableServices = new ArrayList<AbstractFeatureService>();
    private final List<SelectionChangedListener> listener = new ArrayList<SelectionChangedListener>();
    private final List<AbstractFeatureService> syncWithMap = new ArrayList<AbstractFeatureService>();

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
        features.addAll(service.getPNode().getChildrenReference());
        final List<Feature> selectedServiceFeatures = getSelectedFeatures(service);

        if (selectedServiceFeatures != null) {
            final int[] selectedFeatureIds = new int[selectedServiceFeatures.size()];
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
                    } else {
                        sl.removeSelectedFeature(pfeature);
                    }
                }
            }
        }
    }

    /**
     * Set the selected features of a specific service.
     *
     * @param  service      DOCUMENT ME!
     * @param  featureList  DOCUMENT ME!
     */
    public void setSelectedStandaloneFeatures(final AbstractFeatureService service, final List<Feature> featureList) {
        selectedStandaloneFeatures.put(service, featureList);

        final AttributeTable table = consideredAttributeTables.get(service);

        if (table != null) {
            table.applySelection(featureList);
        }
        syncWithMap.add(service);
        refreshSelectedFeatureCounts();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Feature> getSelectedFeatures() {
        final List<Feature> features = new ArrayList<Feature>();

        for (final AbstractFeatureService service : selectedFeatures.keySet()) {
            final List<Feature> serviceFeatures = selectedFeatures.get(service);

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
        return selectedFeatures.get(service);
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
        return selectedFeaturesCount.get(service);
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
        return modifiableFeaturesCount.get(service);
    }

    /**
     * Switch the processing ode of the given service.
     *
     * @param  service  DOCUMENT ME!
     */
    public void switchProcessingMode(final AbstractFeatureService service) {
        if (editableServices.contains(service)) {
            editableServices.remove(service);
        } else {
            editableServices.add(service);
        }
        refreshSelectedFeatureCounts();
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
        selectedStandaloneFeatures.clear();
        refreshSelectedFeatureCounts();
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            refreshSelectedFeatureCounts();
        }
    }

    /**
     * Determines the selected features.
     */
    private synchronized void refreshSelectedFeatureCounts() {
        if (selectionUpdateThread != null) {
            selectionUpdateThread.interrupt();
        }

        selectionUpdateThread = new UpdateSelectionThread();
        // The selection of the features has not changed, yet. Wait until
        // all other selection listeners were notified.
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    executor.submit(selectionUpdateThread);
                }
            });
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

    /**
     * Determines the selected features.
     *
     * @version  $Revision$, $Date$
     */
    private class UpdateSelectionThread extends Thread {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UpdateSelectionThread object.
         */
        public UpdateSelectionThread() {
            super("UpdateSelectionThread");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            final ActiveLayerModel layerModel = (ActiveLayerModel)CismapBroker.getInstance().getMappingComponent()
                        .getMappingModel();
            final TreeMap<Integer, MapService> mapTree = layerModel.getMapServices();
            boolean interrupted = false;

            for (final MapService service : mapTree.values()) {
                int modCount = 0;

                if ((service instanceof AbstractFeatureService)
                            && !consideredAttributeTables.containsKey((AbstractFeatureService)service)) {
                    final AbstractFeatureService featureService = (AbstractFeatureService)service;
                    final boolean serviceInEditMode = editableServices.contains(featureService);
                    final List<Feature> selectedFeaturesList = new ArrayList<Feature>();
                    selectedFeatures.put(featureService, selectedFeaturesList);

                    if (selectedStandaloneFeatures.get(featureService) != null) {
                        selectedFeaturesList.addAll(selectedStandaloneFeatures.get(featureService));
                    }

                    for (final Object featureObject : featureService.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;
                        if (interrupted()) {
                            interrupted = true;
                            break;
                        }

                        if (feature.isSelected()) {
                            selectedFeaturesList.add(feature.getFeature());

                            if (serviceInEditMode) {
                                if (feature.getFeature() instanceof PermissionProvider) {
                                    if (((PermissionProvider)feature.getFeature()).hasWritePermissions()) {
                                        ++modCount;
                                    }
                                } else {
                                    ++modCount;
                                }
                            }
                        }
                    }

                    if (interrupted) {
                        break;
                    }

                    selectedFeaturesCount.put(featureService, selectedFeaturesList.size());

                    if (serviceInEditMode && (selectedFeaturesList.size() > 0)) {
                        modifiableFeaturesCount.put(featureService, modCount);
                    } else {
                        modifiableFeaturesCount.remove(featureService);
                    }
                }
            }

            for (final AttributeTable t : consideredAttributeTables.values()) {
                final AbstractFeatureService service = t.getFeatureService();
                final int featureCount = t.getSelectedFeatureCount();
                final List<Feature> featureList = new ArrayList<Feature>();
                featureList.addAll(t.getSelectedFeatures());
                selectedFeatures.put(service, featureList);

                selectedFeaturesCount.put(service, featureCount);

                if (t.isProcessingModeActive() && (featureCount > 0)) {
                    int modCount = 0;

                    for (final FeatureServiceFeature f : t.getSelectedFeatures()) {
                        if (interrupted) {
                            interrupted = true;
                            break;
                        }
                        if (f instanceof PermissionProvider) {
                            if (((PermissionProvider)f).hasWritePermissions()) {
                                ++modCount;
                            }
                        }
                    }
                    if (interrupted) {
                        break;
                    }
                    modifiableFeaturesCount.put(service, modCount);
                } else {
                    modifiableFeaturesCount.remove(service);
                }
            }

            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        fireSelectionChangedEvent();

                        if (!syncWithMap.isEmpty()) {
                            final List<AbstractFeatureService> syncList = new ArrayList<AbstractFeatureService>(
                                    syncWithMap);
                            syncWithMap.clear();

                            for (final AbstractFeatureService service : syncList) {
                                synchronizeSelectionWithMap(service);
                            }
                        }
                    }
                });
        }
    }
}
