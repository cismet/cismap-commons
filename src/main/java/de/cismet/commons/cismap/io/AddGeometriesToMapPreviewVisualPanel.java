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

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
                if (model.getGeometry() != null) {
                    // do zoom to geometry instead of init
                    initMap();
                }
            }
        }
    }
}
