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
package de.cismet.cismap.linearreferencing.tools;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LinearReferencingDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(LinearReferencingDialog.class);
    private static String station = NbBundle.getMessage(
            LinearReferencingDialog.class,
            "LinearRefeerencingDialog.station");
    private static String stationLine = NbBundle.getMessage(
            LinearReferencingDialog.class,
            "LinearRefeerencingDialog.stationLine");
    private static List<AbstractFeatureService> ROUTE_CLASSES;

    //~ Instance fields --------------------------------------------------------

    private H2FeatureService service;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butApply;
    private javax.swing.JButton butCancel;
    private javax.swing.JComboBox cbFrom;
    private javax.swing.JComboBox cbKind;
    private javax.swing.JComboBox cbRoute;
    private javax.swing.JComboBox cbRouteField;
    private javax.swing.JComboBox cbRouteTargetField;
    private javax.swing.JComboBox cbTill;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JLabel lblKind;
    private javax.swing.JLabel lblRoute;
    private javax.swing.JLabel lblRouteField;
    private javax.swing.JLabel lblRouteTargetField;
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
    public LinearReferencingDialog(final java.awt.Frame parent,
            final boolean modal,
            final H2FeatureService service) {
        super(parent, modal);
        this.service = service;
        initComponents();
        cbKind.setModel(new DefaultComboBoxModel(new Object[] { station, stationLine }));
        final List<String> fields = getAllFieldNames(Number.class);
        cbFrom.setModel(new DefaultComboBoxModel(fields.toArray()));
        cbTill.setModel(new DefaultComboBoxModel(fields.toArray()));
        cbRouteField.setModel(new DefaultComboBoxModel(getAllFieldNames(null).toArray()));

        if (ROUTE_CLASSES == null) {
            final Collection<? extends LinearReferencedGeomProvider> linRefGeomProvider = Lookup.getDefault()
                        .lookupAll(LinearReferencedGeomProvider.class);
            ROUTE_CLASSES = new ArrayList<AbstractFeatureService>();

            for (final LinearReferencedGeomProvider prov : linRefGeomProvider) {
                final List<AbstractFeatureService> services = prov.getLinearReferencedGeomServices();

                if (services != null) {
                    ROUTE_CLASSES.addAll(services);
                }
            }
        }
        cbRoute.setModel(new DefaultComboBoxModel(ROUTE_CLASSES.toArray()));
        cbTill.setEnabled(cbKind.getSelectedItem().equals(stationLine));
        lblTill.setEnabled(cbKind.getSelectedItem().equals(stationLine));
        cbRouteItemStateChanged(null);
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

            resultList.add(name);
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
        cbKind = new javax.swing.JComboBox();
        cbRoute = new javax.swing.JComboBox();
        cbFrom = new javax.swing.JComboBox();
        cbTill = new javax.swing.JComboBox();
        lblKind = new javax.swing.JLabel();
        lblFrom = new javax.swing.JLabel();
        lblTill = new javax.swing.JLabel();
        lblRoute = new javax.swing.JLabel();
        butApply = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        lblRouteField = new javax.swing.JLabel();
        cbRouteField = new javax.swing.JComboBox();
        lblRouteTargetField = new javax.swing.JLabel();
        cbRouteTargetField = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        lblTitle.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblTitle.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 0);
        getContentPane().add(lblTitle, gridBagConstraints);

        cbKind.setMinimumSize(new java.awt.Dimension(80, 27));
        cbKind.setPreferredSize(new java.awt.Dimension(180, 27));
        cbKind.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbKindItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(cbKind, gridBagConstraints);

        cbRoute.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbRoute.setMinimumSize(new java.awt.Dimension(80, 27));
        cbRoute.setPreferredSize(new java.awt.Dimension(180, 27));
        cbRoute.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbRouteItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbRoute, gridBagConstraints);

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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbTill, gridBagConstraints);

        lblKind.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblKind.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblKind.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblKind, gridBagConstraints);

        lblFrom.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblFrom.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblFrom.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblFrom, gridBagConstraints);

        lblTill.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblTill.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblTill.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(lblTill, gridBagConstraints);

        lblRoute.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblRoute.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblRoute.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(lblRoute, gridBagConstraints);

        butApply.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.butApply.text", new Object[] {})); // NOI18N
        butApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        butCancel.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.butCancel.text", new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(butCancel, gridBagConstraints);

        lblRouteField.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblRouteField.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblRouteField.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblRouteField, gridBagConstraints);

        cbRouteField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbRouteField.setMinimumSize(new java.awt.Dimension(80, 27));
        cbRouteField.setPreferredSize(new java.awt.Dimension(180, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(cbRouteField, gridBagConstraints);

        lblRouteTargetField.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblRouteTargetField.setText(org.openide.util.NbBundle.getMessage(LinearReferencingDialog.class, "LinearReferencingDialog.lblRouteTargetField.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 10, 5);
        getContentPane().add(lblRouteTargetField, gridBagConstraints);

        cbRouteTargetField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbRouteTargetField.setMinimumSize(new java.awt.Dimension(80, 27));
        cbRouteTargetField.setPreferredSize(new java.awt.Dimension(180, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbRouteTargetField, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbKindItemStateChanged(final java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbKindItemStateChanged
        cbTill.setEnabled(evt.getItem().equals(stationLine));
        lblTill.setEnabled(evt.getItem().equals(stationLine));
    }//GEN-LAST:event_cbKindItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbRouteItemStateChanged(final java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbRouteItemStateChanged
        final AbstractFeatureService routeService = (AbstractFeatureService)cbRoute.getSelectedItem();
        final List<String> allFields = new ArrayList<String>();

        if (routeService.getFeatureServiceAttributes() == null) {
            try {
                routeService.initAndWait();
            } catch (Exception ex) {
                LOG.error("Error while initializing the route service", ex);
            }
        }

        for (final Object attrName : routeService.getFeatureServiceAttributes().keySet()) {
            allFields.add(String.valueOf(attrName));
        }

        cbRouteTargetField.setModel(new DefaultComboBoxModel(allFields.toArray()));
    }//GEN-LAST:event_cbRouteItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butApplyActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butApplyActionPerformed
        final String fromField = String.valueOf(cbFrom.getSelectedItem());
        final String tillField = (cbTill.isEnabled() ? String.valueOf(cbTill.getSelectedItem()) : null);
        final String routeField = String.valueOf(cbRouteField.getSelectedItem());
        final String routeJoinField = String.valueOf(cbRouteTargetField.getSelectedItem());
        final AbstractFeatureService routeService = (AbstractFeatureService)cbRoute.getSelectedItem();

        final String name = routeService.getName();

        final StringTokenizer st = new StringTokenizer(name, ":");

        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(
                    getParent()),
                true,
                name,
                null,
                200) {

                @Override
                protected Void doInBackground() throws Exception {
                    String layerName = null;
                    String domainName = null;

                    if (st.countTokens() == 2) {
                        domainName = st.nextToken();
                        layerName = st.nextToken();
                    }

                    service.setLinearReferencingInformation(
                        fromField,
                        tillField,
                        routeField,
                        routeJoinField,
                        routeService,
                        layerName,
                        domainName);

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
    }//GEN-LAST:event_butApplyActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_butCancelActionPerformed

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
            java.util.logging.Logger.getLogger(LinearReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LinearReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LinearReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LinearReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
    }
}
