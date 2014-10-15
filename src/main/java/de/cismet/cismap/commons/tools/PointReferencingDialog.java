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
package de.cismet.cismap.commons.tools;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * This dialog allows to add point geometrie.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PointReferencingDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PointReferencingDialog.class);

    //~ Instance fields --------------------------------------------------------

    private H2FeatureService service;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butApply;
    private javax.swing.JButton butCancel;
    private javax.swing.JComboBox cbFrom;
    private javax.swing.JComboBox cbTill;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JLabel lblTill;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form LinearReferencingDialog.
     *
     * @param  parent   DOCUMENT ME!
     * @param  modal    DOCUMENT ME!
     * @param  service  DOCUMENT ME!
     */
    public PointReferencingDialog(final java.awt.Frame parent,
            final boolean modal,
            final H2FeatureService service) {
        super(parent, modal);
        this.service = service;
        initComponents();
        final List<String> fields = getAllFieldNames(Number.class);
        cbFrom.setModel(new DefaultComboBoxModel(fields.toArray()));
        cbTill.setModel(new DefaultComboBoxModel(fields.toArray()));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   cl  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> getAllFieldNames(final Class<?> cl) {
        Map<String, FeatureServiceAttribute> attributeMap = service.getFeatureServiceAttributes();
        final List<String> resultList = new ArrayList<String>();

        if (attributeMap == null) {
            try {
                service.initAndWait();
            } catch (Exception e) {
                LOG.error("Error while initializing the feature service.", e);
            }
            attributeMap = service.getFeatureServiceAttributes();
        }

        for (final String name : attributeMap.keySet()) {
            final FeatureServiceAttribute attr = attributeMap.get(name);

            if (cl.isAssignableFrom(FeatureTools.getClass(attr))) {
                resultList.add(name);
            }
        }

        return resultList;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        cbFrom = new javax.swing.JComboBox();
        cbTill = new javax.swing.JComboBox();
        lblFrom = new javax.swing.JLabel();
        lblTill = new javax.swing.JLabel();
        butApply = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                PointReferencingDialog.class,
                "PointReferencingDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        lblTitle.setText(org.openide.util.NbBundle.getMessage(
                PointReferencingDialog.class,
                "PointReferencingDialog.lblTitle.text",
                new Object[] {}));                            // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 0);
        getContentPane().add(lblTitle, gridBagConstraints);

        cbFrom.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbFrom.setMinimumSize(new java.awt.Dimension(80, 27));
        cbFrom.setPreferredSize(new java.awt.Dimension(180, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(cbFrom, gridBagConstraints);

        cbTill.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbTill.setMinimumSize(new java.awt.Dimension(80, 27));
        cbTill.setPreferredSize(new java.awt.Dimension(180, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(cbTill, gridBagConstraints);

        lblFrom.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblFrom.setText(org.openide.util.NbBundle.getMessage(
                PointReferencingDialog.class,
                "PointReferencingDialog.lblFrom.text",
                new Object[] {}));                           // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblFrom, gridBagConstraints);

        lblTill.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblTill.setText(org.openide.util.NbBundle.getMessage(
                PointReferencingDialog.class,
                "PointReferencingDialog.lblTill.text",
                new Object[] {}));                           // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblTill, gridBagConstraints);

        butApply.setText(org.openide.util.NbBundle.getMessage(
                PointReferencingDialog.class,
                "PointReferencingDialog.butApply.text",
                new Object[] {})); // NOI18N
        butApply.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butApplyActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(butApply, gridBagConstraints);

        butCancel.setText(org.openide.util.NbBundle.getMessage(
                PointReferencingDialog.class,
                "PointReferencingDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(butCancel, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butApplyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butApplyActionPerformed
        final String fromField = String.valueOf(cbFrom.getSelectedItem());
        final String tillField = (cbTill.isEnabled() ? String.valueOf(cbTill.getSelectedItem()) : null);

        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(
                    getParent()),
                true,
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed().text"),
                null,
                200) {

                @Override
                protected Void doInBackground() throws Exception {
                    service.setPointGeometryInformation(
                        fromField,
                        tillField);

                    return null;
                }

                @Override
                protected void done() {
                    final MappingModel model = CismapBroker.getInstance().getMappingComponent().getMappingModel();

                    final TreeMap<Integer, MapService> map = model.getRasterServices();

                    if (map != null) {
                        for (final Integer key : map.keySet()) {
                            final MapService mapService = map.get(key);

                            if (mapService instanceof H2FeatureService) {
                                final H2FeatureService other = (H2FeatureService)mapService;
                                if (service.getTableName().equals(other.getTableName())) {
                                    try {
                                        other.initAndWait();
                                    } catch (Exception ex) {
                                        LOG.error("Error while reinitialise layer.", ex);
                                    }
                                    other.retrieve(true);
                                }
                            }
                        }
                    }
                }
            };

        setVisible(false);
        wdt.start();
    } //GEN-LAST:event_butApplyActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PointReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PointReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PointReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PointReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
    }
}
