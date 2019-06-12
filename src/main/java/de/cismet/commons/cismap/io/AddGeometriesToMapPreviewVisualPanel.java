/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import com.vividsolutions.jts.geom.GeometryCollection;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.EventQueue;

import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

import de.cismet.commons.concurrency.CismetConcurrency;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.0
 */
public class AddGeometriesToMapPreviewVisualPanel extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AddGeometriesToMapPreviewVisualPanel.class);

    //~ Instance fields --------------------------------------------------------

    private final transient AddGeometriesToMapPreviewWizardPanel model;

    private final transient ChangeListener modelChangeL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JPanel pnlPreview = new javax.swing.JPanel();
    private final transient de.cismet.commons.gui.progress.BusyStatusPanel pnlStatus =
        new de.cismet.commons.gui.progress.BusyStatusPanel();
    private final transient de.cismet.cismap.commons.gui.MappingComponent previewMap =
        new de.cismet.cismap.commons.gui.MappingComponent();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form AddGeometriesToMapPreviewVisualPanel.
     *
     * @param  model  DOCUMENT ME!
     */
    public AddGeometriesToMapPreviewVisualPanel(final AddGeometriesToMapPreviewWizardPanel model) {
        this.model = model;

        initComponents();

        modelChangeL = new ModelChangeListener();
        model.addChangeListener(WeakListeners.change(modelChangeL, model));

        this.setName(NbBundle.getMessage(
                AddGeometriesToMapPreviewVisualPanel.class,
                "AddGeometriesToMapPreviewVisualPanel.<init>(AddGeometryToMapPreviewWizardPanel).panelName")); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AddGeometriesToMapPreviewWizardPanel getModel() {
        return model;
    }

    /**
     * DOCUMENT ME!
     */
    private void initMap() {
        // map is locked

        final double buffer;
        if (model.getCurrentCrs().isMetric()) {
            buffer = 5.0;
        } else {
            buffer = 0.001;
        }

        // TODO: use proper executor
        CismetConcurrency.getInstance("cismap-commons") // NOI18N
        .getDefaultExecutor().execute(new SwingWorker<XBoundingBox, Void>() {

                @Override
                protected XBoundingBox doInBackground() throws Exception {
                    Thread.currentThread().setName("AddGeometriesToMapPreviewVisualPanel initMap()");
                    try {
                        // home bbox for the current crs
                        final XBoundingBox box = new XBoundingBox(
                                model.getGeometry().getEnvelope().buffer(buffer));
                        final CrsTransformer transformer = new CrsTransformer(model.getCurrentCrs().getCode());

                        return transformer.transformBoundingBox(box);
                    } catch (final Exception e) {
                        LOG.warn(
                            "cannot create home bbox for current crs, preview most likely without background layer", // NOI18N
                            e);

                        return null;
                    }
                }

                @Override
                protected void done() {
                    XBoundingBox homeBbox = null;
                    try {
                        homeBbox = get(300, TimeUnit.MILLISECONDS);
                    } catch (final Exception ex) {
                        LOG.warn("cannot retrieve home boundingbox, preview unusable", ex); // NOI18N
                    }

                    final XBoundingBox box = new XBoundingBox(model.getGeometry().getEnvelope().buffer(buffer));
                    final ActiveLayerModel mappingModel = (ActiveLayerModel)previewMap.getMappingModel();
                    mappingModel.setSrs(model.getCurrentCrs());
                    mappingModel.addHome(box);
                    if (homeBbox != null) {
                        mappingModel.addHome(homeBbox);
                    }

                    final String previewUrl = model.getPreviewUrl();

                    // background map cannot be initialised without proper url
                    if (previewUrl != null) {
                        final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(previewUrl));
                        swms.setName("background"); // NOI18N
                        mappingModel.addLayer(swms);
                    }

                    previewMap.setMappingModel(mappingModel);
                    previewMap.setAnimationDuration(0);
                    previewMap.gotoInitialBoundingBox();
                    previewMap.setInteractionMode(MappingComponent.ZOOM);
                    previewMap.setInteractionMode("MUTE"); // NOI18N
                    if (model.hasMultipleGeometries() && (model.getGeometry() instanceof GeometryCollection)) {
                        final GeometryCollection gc = (GeometryCollection)model.getGeometry();

                        for (int i = 0; i < gc.getNumGeometries(); ++i) {
                            final Feature dsf = new PureNewFeature(gc.getGeometryN(i));
                            previewMap.getFeatureCollection().addFeature(dsf);
                        }
                    } else {
                        final Feature dsf = new PureNewFeature(model.getGeometry());
                        previewMap.getFeatureCollection().addFeature(dsf);
                    }
                    previewMap.setAnimationDuration(300);

                    // finally when all configurations are done the map may animate again
                    previewMap.unlock();
                    previewMap.zoomToFeatureCollection();
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void clearMap() {
        assert EventQueue.isDispatchThread() : "may only be accessed in EDT"; // NOI18N

        previewMap.getFeatureCollection().removeAllFeatures();
        final ActiveLayerModel mappingModel = new ActiveLayerModel();
        previewMap.setMappingModel(mappingModel);
        previewMap.lock();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        pnlPreview.setBorder(javax.swing.BorderFactory.createTitledBorder(
                NbBundle.getMessage(
                    AddGeometriesToMapPreviewVisualPanel.class,
                    "AddGeometriesToMapPreviewVisualPanel.pnlPreview.border.title"))); // NOI18N
        pnlPreview.setOpaque(false);
        pnlPreview.setLayout(new java.awt.BorderLayout());
        pnlPreview.add(previewMap, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(pnlPreview, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(pnlStatus, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    // TODO: use property change listener for more fine grained control
    private final class ModelChangeListener implements ChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void stateChanged(final ChangeEvent e) {
            // maybe we should use propertychangelistener to be able to specifically react to updates
            if (e.getSource() instanceof AddGeometriesToMapPreviewWizardPanel) {
                pnlStatus.setBusy(model.isBusy());
                pnlStatus.setStatusMessage(model.getStatusMessage());

                // TODO proper initialisation so that overview is loaded and zoom is done here
                if (model.getGeometry() == null) {
                    clearMap();
                } else {
                    // do zoom to geometry instead of init
                    initMap();
                }
            }
        }
    }
}
