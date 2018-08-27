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
package de.cismet.cismap.commons.rasterservice.georeferencing;

import lombok.Getter;

import org.openide.util.lookup.ServiceProvider;

import java.awt.Component;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.ToolbarComponentDescription;
import de.cismet.cismap.commons.gui.ToolbarComponentsProvider;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.StatusEvent;

import de.cismet.tools.StaticDebuggingTools;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.menu.CidsUiComponent;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = ToolbarComponentsProvider.class)
public class RasterGeoReferencingToolbarComponentProvider extends javax.swing.JPanel
        implements ToolbarComponentsProvider,
            CidsUiComponent {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PLUGIN_NAME = "RASTER_GEO_REFERENCING";

    //~ Instance fields --------------------------------------------------------

    @Getter private final List<ToolbarComponentDescription> toolbarComponents = new ArrayList<>();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnRasterGeoRef;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form RasterGeoReferencingToolbarComponentProvider.
     */
    public RasterGeoReferencingToolbarComponentProvider() {
        initComponents();

        if (CismapBroker.getInstance().isEnableRasterGeoReferencingToolbar()) {
            getToolbarComponents().add(getToolbarComponentDescription());

            CismapBroker.getInstance().addStatusListener(new MapStatusListenerHandler());

            RasterGeoReferencingWizard.getInstance().addListener(new WizardListenerHandler());
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ToolbarComponentDescription getToolbarComponentDescription() {
        return new ToolbarComponentDescription(
                "tlbMain",
                btnRasterGeoRef,
                ToolbarPositionHint.AFTER,
                "cmdPan",
                true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        btnRasterGeoRef = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridBagLayout());

        btnRasterGeoRef.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/rasterservice/georeferencing/georef.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            btnRasterGeoRef,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingToolbarComponentProvider.class,
                "RasterGeoReferencingToolbarComponentProvider.btnRasterGeoRef.text"));                         // NOI18N
        btnRasterGeoRef.setEnabled(false);
        btnRasterGeoRef.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnRasterGeoRefActionPerformed(evt);
                }
            });
        add(btnRasterGeoRef, new java.awt.GridBagConstraints());
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnRasterGeoRefActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRasterGeoRefActionPerformed
        CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.GEO_REF);
    }                                                                                   //GEN-LAST:event_btnRasterGeoRefActionPerformed

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getValue(final String key) {
        if (key.equals(CidsUiComponent.CIDS_ACTION_KEY)) {
            return "RasterGeoReferencingToolbar";
        } else {
            return null;
        }
    }

    @Override
    public Component getComponent() {
        return btnRasterGeoRef;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class MapStatusListenerHandler implements StatusListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void statusValueChanged(final StatusEvent evt) {
            if (StatusEvent.MAPPING_MODE.equals(evt.getName())) {
                if (MappingComponent.GEO_REF.equals(evt.getValue())) {
                    btnRasterGeoRef.setSelected(true);
                    if (!RasterGeoReferencingDialog.getInstance().isVisible()) {
                        StaticSwingTools.showDialog(RasterGeoReferencingDialog.getInstance());
                    }
                } else {
                    btnRasterGeoRef.setSelected(false);
                    RasterGeoReferencingDialog.getInstance().setVisible(false);
                }
                if ((RasterGeoReferencingWizard.getInstance().getHandler() != null)
                            && (RasterGeoReferencingWizard.getInstance().getHandler().getFeature() != null)) {
                    RasterGeoReferencingWizard.getInstance().getHandler().getFeature().transformationChanged();
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class WizardListenerHandler implements RasterGeoReferencingWizardListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void pointSelected(final int position) {
        }

        @Override
        public void coordinateSelected(final int position) {
        }

        @Override
        public void handlerChanged(final RasterGeoReferencingHandler handler) {
            btnRasterGeoRef.setEnabled(handler != null);
        }

        @Override
        public void positionAdded(final int position) {
        }

        @Override
        public void positionRemoved(final int position) {
        }

        @Override
        public void positionChanged(final int position) {
        }

        @Override
        public void transformationChanged() {
        }
    }
}
