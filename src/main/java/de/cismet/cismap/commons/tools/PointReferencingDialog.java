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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.interaction.CismapBroker;

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
    private static Object lastFromProperty = null;
    private static Object lastTillProperty = null;
    private static Double MIN_X = null;
    private static Double MAX_X = null;
    private static Double MIN_Y = null;
    private static Double MAX_Y = null;

    //~ Instance fields --------------------------------------------------------

    private H2FeatureService service;
    private boolean geometry = false;
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
        lblTitle.setVisible(false);
        final List<String> fields = getAllFieldNames(Number.class);
        cbFrom.setModel(new DefaultComboBoxModel(fields.toArray()));
        cbTill.setModel(new DefaultComboBoxModel(fields.toArray()));

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
     * @return  the MIN_X
     */
    public static Double getMIN_X() {
        return MIN_X;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aMIN_X  the MIN_X to set
     */
    public static void setMIN_X(final Double aMIN_X) {
        MIN_X = aMIN_X;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the MAX_X
     */
    public static Double getMAX_X() {
        return MAX_X;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aMAX_X  the MAX_X to set
     */
    public static void setMAX_X(final Double aMAX_X) {
        MAX_X = aMAX_X;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the MIN_Y
     */
    public static Double getMIN_Y() {
        return MIN_Y;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aMIN_Y  the MIN_Y to set
     */
    public static void setMIN_Y(final Double aMIN_Y) {
        MIN_Y = aMIN_Y;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the MAX_Y
     */
    public static Double getMAX_Y() {
        return MAX_Y;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aMAX_Y  the MAX_Y to set
     */
    public static void setMAX_Y(final Double aMAX_Y) {
        MAX_Y = aMAX_Y;
    }

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
                if (cl.isAssignableFrom(FeatureTools.getClass(attr))) {
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
        final String tillField = String.valueOf(cbTill.getSelectedItem());

        if (fromField.equals(tillField)) {
            JOptionPane.showConfirmDialog(CismapBroker.getInstance().getMappingComponent(),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed.xEqualsY"),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed.xEqualsY.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);

            return;
        }

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
                    "PointReferencingDialog.butApplyActionPerformed.tableAlreadyExists"),
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed.tableAlreadyExists.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);

            return;
        }

        final WaitingDialogThread<ServiceWithHint> wdt = new WaitingDialogThread<ServiceWithHint>(StaticSwingTools
                        .getParentFrame(
                            getParent()),
                true,
                NbBundle.getMessage(
                    PointReferencingDialog.class,
                    "PointReferencingDialog.butApplyActionPerformed().text"),
                null,
                200) {

                @Override
                protected ServiceWithHint doInBackground() throws Exception {
//                    return service.createPointGeometryInformation(
//                            fromField,
//                            tillField,
//                            tableName);

                    service.initAndWait();
                    final Map<String, FeatureServiceAttribute> attributes =
                        new HashMap<String, FeatureServiceAttribute>(service.getFeatureServiceAttributes());
                    String geometryField = null;
                    final List<String> attributeOrder = new ArrayList<String>(
                            service.getOrderedFeatureServiceAttributes());

                    for (final String key : attributes.keySet()) {
                        final FeatureServiceAttribute attr = attributes.get(key);

                        if (attr.isGeometry()) {
                            geometryField = key;
                            break;
                        }
                    }

                    if (geometryField != null) {
                        attributes.remove(geometryField);
                        attributeOrder.remove(geometryField);
                    }

                    attributes.put("geom", new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true));
                    attributeOrder.add(0, "geom");

                    final List<FeatureServiceAttribute> featureServiceAttributes =
                        new ArrayList<FeatureServiceAttribute>();

                    for (final String key : attributeOrder) {
                        final FeatureServiceAttribute attr = attributes.get(key);

                        featureServiceAttributes.add(attr);
                    }

                    final List<FeatureServiceFeature> featureList = new ArrayList<FeatureServiceFeature>();
                    final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getDefaultCrs()));
                    final LayerProperties layerProperties = new DefaultLayerProperties();
                    // add a dummy service, that contains the feature service attributes
                    layerProperties.setFeatureService(new H2FeatureService(
                            "dummy",
                            "dummy",
                            null,
                            featureServiceAttributes));

                    final List<FeatureServiceFeature> features = service.getFeatureFactory()
                                .createFeatures(service.getQuery(), null, null, 0, 0, null);
                    final List<FeatureServiceFeature> newFeatures = new ArrayList<FeatureServiceFeature>();
                    int invalidCount = 0;

                    for (final FeatureServiceFeature tmp : features) {
                        final HashMap<String, Object> properties = new HashMap<String, Object>(
                                featureServiceAttributes.size());
                        final Double x;
                        final Double y;

                        try {
                            if (tmp.getProperty(fromField) != null) {
                                x = Double.parseDouble(tmp.getProperty(fromField).toString());
                            } else {
                                ++invalidCount;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            ++invalidCount;
                            continue;
                        }

                        try {
                            if (tmp.getProperty(tillField) != null) {
                                y = Double.parseDouble(tmp.getProperty(tillField).toString());
                            } else {
                                ++invalidCount;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            ++invalidCount;
                            continue;
                        }

                        if (((MIN_X != null) && (x < MIN_X)) || ((MAX_X != null) && (x > MAX_X))
                                    || ((MIN_Y != null) && (y < MIN_Y))
                                    || ((MAX_Y != null) && (y > MAX_Y))) {
                            ++invalidCount;
                            continue;
                        }

                        final Geometry g = geomFactory.createPoint(new Coordinate(x, y));

                        for (final String propName : attributeOrder) {
                            if (propName.equals("geom")) {
                                properties.put(propName, g);
                            } else {
                                properties.put(propName, tmp.getProperty(propName));
                            }
                        }

                        final DefaultFeatureServiceFeature lastFeature = new DefaultFeatureServiceFeature(
                                tmp.getId(),
                                g,
                                layerProperties);
                        lastFeature.setProperties(properties);
                        featureList.add(lastFeature);
                    }

                    H2FeatureService internalService = null;
                    String hint = null;

                    if (featureList.size() > 0) {
                        internalService = new H2FeatureService(
                                tableName,
                                H2FeatureServiceFactory.DB_NAME,
                                tableName,
                                featureServiceAttributes,
                                featureList);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("create the new data source");
                        }
                        internalService.initAndWait();
                    } else {
                        hint = NbBundle.getMessage(
                                PointReferencingDialog.class,
                                "PointReferencingDialog.butApplyActionPerformed.themeNotCreated");
                    }

                    if ((hint == null) && (invalidCount > 0)) {
                        hint = NbBundle.getMessage(
                                PointReferencingDialog.class,
                                "PointReferencingDialog.butApplyActionPerformed.themePartiallyCreated",
                                featureList.size(),
                                (featureList.size() + invalidCount));
                    }

                    return new ServiceWithHint(hint, internalService);
                }

                @Override
                protected void done() {
                    try {
                        final ServiceWithHint serviceWithHint = get();

                        final String hint = serviceWithHint.getHint();

                        if (hint != null) {
                            JOptionPane.showMessageDialog(
                                PointReferencingDialog.this,
                                hint,
                                NbBundle.getMessage(
                                    PointReferencingDialog.class,
                                    "PointReferencingDialog.butApplyActionPerformed.title"),
                                JOptionPane.WARNING_MESSAGE);
                        }

                        final H2FeatureService service = serviceWithHint.getService();

                        if (service != null) {
                            final CapabilityWidget cap = CapabilityWidgetOptionsPanel.getCapabilityWidget();

                            if (cap != null) {
                                cap.refreshJdbcTrees();
                            }
                        }
                        lastFromProperty = cbFrom.getSelectedItem();
                        lastTillProperty = cbTill.getSelectedItem();
                    } catch (H2FeatureServiceFactory.NegativeValueException ex) {
                        if (ex.isBoth()) {
                            JOptionPane.showConfirmDialog(CismapBroker.getInstance().getMappingComponent(),
                                NbBundle.getMessage(
                                    PointReferencingDialog.class,
                                    "PointReferencingDialog.butApplyActionPerformed.negativeValuesInBothAttributes"),
                                NbBundle.getMessage(
                                    PointReferencingDialog.class,
                                    "PointReferencingDialog.butApplyActionPerformed.negativeValuesInBothAttributes.title"),
                                JOptionPane.CANCEL_OPTION,
                                JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showConfirmDialog(CismapBroker.getInstance().getMappingComponent(),
                                NbBundle.getMessage(
                                    PointReferencingDialog.class,
                                    "PointReferencingDialog.butApplyActionPerformed.negativeValuesInAttribute",
                                    ex.getAttributeName()),
                                NbBundle.getMessage(
                                    PointReferencingDialog.class,
                                    "PointReferencingDialog.butApplyActionPerformed.negativeValuesInAttribute.title"),
                                JOptionPane.CANCEL_OPTION,
                                JOptionPane.ERROR_MESSAGE);
                        }
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class ServiceWithHint {

        //~ Instance fields ----------------------------------------------------

        private final String hint;
        private final H2FeatureService service;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ServiceWithHint object.
         *
         * @param  hint     DOCUMENT ME!
         * @param  service  DOCUMENT ME!
         */
        public ServiceWithHint(final String hint, final H2FeatureService service) {
            this.hint = hint;
            this.service = service;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the hint
         */
        public String getHint() {
            return hint;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the service
         */
        public H2FeatureService getService() {
            return service;
        }
    }
}
