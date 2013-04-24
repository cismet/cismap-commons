/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.EventQueue;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.0
 */
public class AddGeometriesToMapPreviewVisualPanel extends JPanel {

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

        this.setName("Map preview");
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
        assert EventQueue.isDispatchThread() : "may only be accessed in EDT"; // NOI18N

        // map is locked

        final double buffer;
        if (model.getCurrentCrs().isMetric()) {
            buffer = 5.0;
        } else {
            buffer = 0.001;
        }

        final XBoundingBox box = new XBoundingBox(model.getGeometry().getEnvelope().buffer(buffer));
        final ActiveLayerModel mappingModel = (ActiveLayerModel)previewMap.getMappingModel();
        mappingModel.setSrs(model.getCurrentCrs());
        mappingModel.addHome(box);

        // FIXME: hardcoded for testing purposes
// final String previewUrl = model.getPreviewUrl();
        final String previewUrl =
            "http://S102X284:8399/arcgis/services/ALKIS-EXPRESS/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=FALSE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=2,4,5,6,7,8,10,11,12,13,14,16,17,18,19,20,21,22,23,25,26,27,28,29,30,31,32,33,34,35,36,37,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100&STYLES=&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS=<cismap:srs>";

        // background map cannot be initialised without proper url
        if (previewUrl != null) {
            final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(previewUrl));
            swms.setName("background"); // NOI18N
            mappingModel.addLayer(swms);
        }

        final Feature dsf = new PureNewFeature(model.getGeometry());
        previewMap.setMappingModel(mappingModel);
        previewMap.setAnimationDuration(0);
        previewMap.gotoInitialBoundingBox();
        previewMap.setInteractionMode(MappingComponent.ZOOM);
        previewMap.setInteractionMode("MUTE"); // NOI18N
        previewMap.getFeatureCollection().addFeature(dsf);
        previewMap.setAnimationDuration(300);

        // finally when all configurations are done the map may animate again
        previewMap.unlock();
        previewMap.zoomToFeatureCollection();
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
