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

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.tools.PointReferencingDialog;

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

    private static final Logger LOG = Logger.getLogger(LinearReferencingDialog.class);
    private static final String station = NbBundle.getMessage(
            LinearReferencingDialog.class,
            "LinearRefeerencingDialog.station");
    private static final String stationLine = NbBundle.getMessage(
            LinearReferencingDialog.class,
            "LinearRefeerencingDialog.stationLine");
    private static List<AbstractFeatureService> ROUTE_CLASSES;
    private static Collection<? extends LinearReferencedGeomProvider> linRefGeomProvider;
    private static Object lastKindProperty = null;
    private static Object lastRouteProperty = null;
    private static Object lastRouteFieldProperty = null;
    private static Object lastRouteTargetFieldProperty = null;
    private static Object lastFromProperty = null;
    private static Object lastTillProperty = null;

    //~ Instance fields --------------------------------------------------------

    private final H2FeatureService service;
    private boolean geometry = false;
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
        lblTitle.setVisible(false);
        cbKind.setModel(new DefaultComboBoxModel(new Object[] { station, stationLine }));
        final List<String> fields = getAllFieldNames(Number.class);
        cbFrom.setModel(new DefaultComboBoxModel(fields.toArray()));
        cbTill.setModel(new DefaultComboBoxModel(fields.toArray()));
        cbRouteField.setModel(new DefaultComboBoxModel(getAllFieldNames(null).toArray()));

        if (ROUTE_CLASSES == null) {
            linRefGeomProvider = Lookup.getDefault().lookupAll(LinearReferencedGeomProvider.class);
            ROUTE_CLASSES = new ArrayList<AbstractFeatureService>();

            for (final LinearReferencedGeomProvider prov : linRefGeomProvider) {
                final List<AbstractFeatureService> services = prov.getLinearReferencedGeomServices();

                if (services != null) {
                    ROUTE_CLASSES.addAll(services);
                    break;
                }
            }
        }
        cbRoute.setModel(new DefaultComboBoxModel(ROUTE_CLASSES.toArray()));
        cbTill.setEnabled(cbKind.getSelectedItem().equals(stationLine));
        lblTill.setEnabled(cbKind.getSelectedItem().equals(stationLine));
        cbRouteItemStateChanged(null);
        if (lastKindProperty != null) {
            cbKind.setSelectedItem(lastKindProperty);
        }
        if (lastRouteProperty != null) {
            cbRoute.setSelectedItem(lastRouteProperty);
        }
        if (lastRouteFieldProperty != null) {
            cbRouteField.setSelectedItem(lastRouteFieldProperty);
        }
        if (lastRouteTargetFieldProperty != null) {
            cbRouteTargetField.setSelectedItem(lastRouteTargetFieldProperty);
        }
        if (lastFromProperty != null) {
            cbFrom.setSelectedItem(lastFromProperty);
        }
        if (lastTillProperty != null) {
            cbTill.setSelectedItem(lastTillProperty);
        }
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
        List<String> names = service.getOrderedFeatureServiceAttributes();
        final List<String> resultList = new ArrayList<String>();

        if ((attributeMap == null) || (names == null)) {
            try {
                service.initAndWait();
            } catch (Exception e) {
                LOG.error("Error while initializing the feature service.", e);
            }
            attributeMap = service.getFeatureServiceAttributes();
            names = service.getOrderedFeatureServiceAttributes();
        }

        for (final String name : names) {
            final FeatureServiceAttribute attr = attributeMap.get(name);

            if (attr != null) {
                if ((cl == null) || cl.isAssignableFrom(FeatureTools.getClass(attr))) {
                    resultList.add(name);
                }

                if (attr.isGeometry()) {
                    geometry = true;
                }
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
        setTitle(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        lblTitle.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblTitle.text",
                new Object[] {}));                            // NOI18N
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

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbKindItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbKind, gridBagConstraints);

        cbRoute.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbRoute.setMinimumSize(new java.awt.Dimension(80, 27));
        cbRoute.setPreferredSize(new java.awt.Dimension(180, 27));
        cbRoute.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbRouteItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbFrom, gridBagConstraints);

        cbTill.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbTill.setMinimumSize(new java.awt.Dimension(80, 27));
        cbTill.setPreferredSize(new java.awt.Dimension(180, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbTill, gridBagConstraints);

        lblKind.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblKind.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblKind.text",
                new Object[] {}));                           // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblKind, gridBagConstraints);

        lblFrom.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblFrom.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblFrom.text",
                new Object[] {}));                           // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblFrom, gridBagConstraints);

        lblTill.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblTill.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblTill.text",
                new Object[] {}));                           // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblTill, gridBagConstraints);

        lblRoute.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblRoute.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblRoute.text",
                new Object[] {}));                            // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblRoute, gridBagConstraints);

        butApply.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.butApply.text",
                new Object[] {})); // NOI18N
        butApply.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butApplyActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(butApply, gridBagConstraints);

        butCancel.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(butCancel, gridBagConstraints);

        lblRouteField.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblRouteField.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblRouteField.text",
                new Object[] {}));                                 // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblRouteField, gridBagConstraints);

        cbRouteField.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbRouteField.setMinimumSize(new java.awt.Dimension(80, 27));
        cbRouteField.setPreferredSize(new java.awt.Dimension(210, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbRouteField, gridBagConstraints);

        lblRouteTargetField.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblRouteTargetField.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencingDialog.class,
                "LinearReferencingDialog.lblRouteTargetField.text",
                new Object[] {}));                                       // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(lblRouteTargetField, gridBagConstraints);

        cbRouteTargetField.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbRouteTargetField.setMinimumSize(new java.awt.Dimension(80, 27));
        cbRouteTargetField.setPreferredSize(new java.awt.Dimension(180, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 5);
        getContentPane().add(cbRouteTargetField, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbKindItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbKindItemStateChanged
        cbTill.setEnabled(evt.getItem().equals(stationLine));
        lblTill.setEnabled(evt.getItem().equals(stationLine));
    }                                                                         //GEN-LAST:event_cbKindItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbRouteItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbRouteItemStateChanged
        final AbstractFeatureService routeService = (AbstractFeatureService)cbRoute.getSelectedItem();
        final List<String> allFields = new ArrayList<String>();

        try {
            routeService.initAndWait();
        } catch (Exception ex) {
            LOG.error("Error while initializing the route service", ex);
        }

        for (final Object attrName : routeService.getOrderedFeatureServiceAttributes()) {
            allFields.add(String.valueOf(attrName));
        }

        cbRouteTargetField.setModel(new DefaultComboBoxModel(allFields.toArray()));
    } //GEN-LAST:event_cbRouteItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butApplyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butApplyActionPerformed
        final String fromField = String.valueOf(cbFrom.getSelectedItem());
        final String tillField = (cbTill.isEnabled() ? String.valueOf(cbTill.getSelectedItem()) : null);
        final String routeField = String.valueOf(cbRouteField.getSelectedItem());
        final String routeJoinField = String.valueOf(cbRouteTargetField.getSelectedItem());
        final AbstractFeatureService routeService = (AbstractFeatureService)cbRoute.getSelectedItem();
        String tmpName = null;
        String tmpDomain = null;

        for (final LinearReferencedGeomProvider provider : linRefGeomProvider) {
            tmpName = provider.getInternalServiceName(routeService);
            tmpDomain = provider.getServiceDomain(routeService);

            if ((tmpName != null) && (tmpDomain != null)) {
                break;
            }
        }
        final String name = tmpName;
        final String domain = tmpDomain;

//        final StringTokenizer st = new StringTokenizer(name, ":");

        final String tableName = JOptionPane.showInputDialog(CismapBroker.getInstance().getMappingComponent(),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed.tableName"),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed.tableName.title"),
                JOptionPane.QUESTION_MESSAGE);

        if ((tableName == null) || tableName.equals("")) {
            return;
        }

        if (H2FeatureService.tableAlreadyExists(tableName)) {
            JOptionPane.showConfirmDialog(CismapBroker.getInstance().getMappingComponent(),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "LinearReferencingDialog.butApplyActionPerformed.tableAlreadyExists"),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "LinearReferencingDialog.butApplyActionPerformed.tableAlreadyExists.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);

            return;
        }

        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(StaticSwingTools
                        .getParentFrame(
                            getParent()),
                true,
                routeService.getName(),
                null,
                200) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    return service.createLinearReferencingLayer(
                            fromField,
                            tillField,
                            routeField,
                            routeJoinField,
                            routeService,
                            name,
                            domain,
                            tableName);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        final CapabilityWidget cap = CapabilityWidgetOptionsPanel.getCapabilityWidget();

                        if (cap != null) {
                            cap.refreshJdbcTrees();
                        }

                        lastKindProperty = cbKind.getSelectedItem();
                        lastRouteProperty = cbRoute.getSelectedItem();
                        lastRouteFieldProperty = cbRouteField.getSelectedItem();
                        lastRouteTargetFieldProperty = cbRouteTargetField.getSelectedItem();
                        lastFromProperty = cbFrom.getSelectedItem();
                        lastTillProperty = cbTill.getSelectedItem();
                    } catch (Exception e) {
                        LOG.error("Error while adding point references", e);
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
