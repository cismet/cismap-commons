/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Point2D;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.collections.HashArrayList;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class AngleMeasurementDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            AngleMeasurementDialog.class);
    private static AngleMeasurementDialog INSTANCE = null;
    private static DecimalFormat FORMAT = new DecimalFormat("#0.00");

    //~ Instance fields --------------------------------------------------------

    private boolean mirrorAAllowed = false;
    private boolean mirrorBAllowed = false;
    private boolean mirrorA = false;
    private boolean mirrorB = false;
    private boolean showFeature = true;
    private boolean listSelectionListenerEnabled = true;
    private boolean featureCollectionListenerEnabled = true;
    private boolean initied = false;

    private final FeatureCollectionListModel featureCollectionListModel = new FeatureCollectionListModel();
    private final ListSelectionListener listSelectionListener = new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && isListSelectionListenerEnabled()) {
                    applyListSelectionToMap();
                    doCalculateAngle();
                }
            }
        };

    private PPath tempFeature = null;

    private boolean initialMcReadonly = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JPanel panButtons;
    private javax.swing.JPanel panParams;
    private javax.swing.JPanel panSide;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form RectangleFromLineDialog.
     */
    private AngleMeasurementDialog() {
        super(StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent()), false);
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isListSelectionListenerEnabled() {
        return listSelectionListenerEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listSelectionListenerEnabled  DOCUMENT ME!
     */
    public void setListSelectionListenerEnabled(final boolean listSelectionListenerEnabled) {
        this.listSelectionListenerEnabled = listSelectionListenerEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return CismapBroker.getInstance().getMappingComponent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFeatureCollectionListenerEnabled() {
        return featureCollectionListenerEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureCollectionListenerEnabled  DOCUMENT ME!
     */
    public void setFeatureCollectionListenerEnabled(final boolean featureCollectionListenerEnabled) {
        this.featureCollectionListenerEnabled = featureCollectionListenerEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AngleMeasurementDialog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AngleMeasurementDialog();
        }
        return INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMirrorAAllowed() {
        return mirrorAAllowed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mirrorAAllowed  DOCUMENT ME!
     */
    public void setMirrorAAllowed(final boolean mirrorAAllowed) {
        this.mirrorAAllowed = mirrorAAllowed;
        if (!mirrorAAllowed) {
            setMirrorA(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMirrorBAllowed() {
        return mirrorBAllowed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mirrorBAllowed  DOCUMENT ME!
     */
    public void setMirrorBAllowed(final boolean mirrorBAllowed) {
        this.mirrorBAllowed = mirrorBAllowed;
        if (!mirrorBAllowed) {
            setMirrorB(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isShowFeature() {
        return showFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  showFeature  DOCUMENT ME!
     */
    public void setShowFeature(final boolean showFeature) {
        this.showFeature = showFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMirrorA() {
        return isMirrorAAllowed() && mirrorA;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mirrorA  DOCUMENT ME!
     */
    public void setMirrorA(final boolean mirrorA) {
        this.mirrorA = mirrorA;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMirrorB() {
        return isMirrorBAllowed() && mirrorB;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mirrorB  DOCUMENT ME!
     */
    public void setMirrorB(final boolean mirrorB) {
        this.mirrorB = mirrorB;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panParams = new javax.swing.JPanel();
        panSide = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        panButtons = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(AngleMeasurementDialog.class, "AngleMeasurementDialog.title")); // NOI18N
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(450, 250));
        setPreferredSize(new java.awt.Dimension(450, 250));
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        panParams.setLayout(new java.awt.GridBagLayout());

        panSide.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(panSide, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jLabel4.text"));     // NOI18N
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel4, gridBagConstraints);

        jList1.setModel(featureCollectionListModel);
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setCellRenderer(new FeatureCollectionListCellRenderer());
        jScrollPane1.setViewportView(jList1);
        jList1.getSelectionModel().addListSelectionListener(listSelectionListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jScrollPane1, gridBagConstraints);

        jList2.setModel(featureCollectionListModel);
        jList2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList2.setCellRenderer(new FeatureCollectionListCellRenderer());
        jScrollPane2.setViewportView(jList2);
        jList2.getSelectionModel().addListSelectionListener(listSelectionListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jScrollPane2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jToggleButton2.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/Angle-Thingy-icon.png"))); // NOI18N
        jToggleButton2.setSelected(true);
        jToggleButton2.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jToggleButton2.text"));                                          // NOI18N
        jToggleButton2.setFocusPainted(false);
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jToggleButton2ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel2.add(jToggleButton2, gridBagConstraints);

        jToggleButton1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/layer-flip.png"))); // NOI18N
        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jToggleButton1.text"));                                   // NOI18N
        jToggleButton1.setToolTipText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jToggleButton1.toolTipText"));                            // NOI18N
        jToggleButton1.setDisabledIcon(null);
        jToggleButton1.setFocusPainted(false);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jToggleButton1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel2.add(jToggleButton1, gridBagConstraints);

        jToggleButton3.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/layer-flip-vertical.png"))); // NOI18N
        jToggleButton3.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jToggleButton3.text"));                                            // NOI18N
        jToggleButton3.setToolTipText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jToggleButton3.toolTipText"));                                     // NOI18N
        jToggleButton3.setDisabledIcon(null);
        jToggleButton3.setFocusPainted(false);
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jToggleButton3ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel2.add(jToggleButton3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel3.add(jLabel1, gridBagConstraints);

        jButton2.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/layer-shape-line-crossed.png"))); // NOI18N
        jButton2.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jButton2.text"));                                                       // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jButton2.toolTipText"));                                                // NOI18N
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setDisabledIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/layer-shape-line.png")));         // NOI18N
        jButton2.setEnabled(false);
        jButton2.setFocusPainted(false);
        jButton2.setFocusable(false);
        jButton2.setRequestFocusEnabled(false);
        jButton2.setRolloverEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel3.add(jButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panParams.add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel4.add(jLabel2, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/layer-shape-line-crossed.png"))); // NOI18N
        jButton1.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jButton1.text"));                                                       // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.jButton1.toolTipText"));                                                // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setDisabledIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/layer-shape-line.png")));         // NOI18N
        jButton1.setEnabled(false);
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setRequestFocusEnabled(false);
        jButton1.setRolloverEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel4.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panParams.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(panParams, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        btnCancel.setText(org.openide.util.NbBundle.getMessage(
                AngleMeasurementDialog.class,
                "AngleMeasurementDialog.btnCancel.text_2")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnCancelActionPerformed(evt);
                }
            });
        jPanel1.add(btnCancel);

        panButtons.add(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(panButtons, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }                                                                             //GEN-LAST:event_btnCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jToggleButton2ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jToggleButton2ActionPerformed
        setShowFeature(jToggleButton2.isSelected());
        if (tempFeature != null) {
            if (isShowFeature()) {
                getMappingComponent().getTmpFeatureLayer().addChild(tempFeature);
            } else {
                try {
                    getMappingComponent().getTmpFeatureLayer().removeChild(tempFeature);
                } catch (final Exception ex) {
                }
            }
        }
        refreshButtons();
    }                                                                                  //GEN-LAST:event_jToggleButton2ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jToggleButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jToggleButton1ActionPerformed
        setMirrorA(jToggleButton1.isSelected());
        doCalculateAngle();
    }                                                                                  //GEN-LAST:event_jToggleButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jToggleButton3ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jToggleButton3ActionPerformed
        setMirrorB(jToggleButton3.isSelected());
        doCalculateAngle();
    }                                                                                  //GEN-LAST:event_jToggleButton3ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        final Feature feature = (Feature)jList1.getSelectedValue();
        getMappingComponent().getFeatureCollection().removeFeature(feature);
    }                                                                            //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton2ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton2ActionPerformed
        final Feature feature = (Feature)jList2.getSelectedValue();
        getMappingComponent().getFeatureCollection().removeFeature(feature);
    }                                                                            //GEN-LAST:event_jButton2ActionPerformed

    @Override
    public void dispose() {
        setFeatureCollectionListenerEnabled(false);
        try {
            getMappingComponent().getTmpFeatureLayer().removeChild(tempFeature);
        } catch (final Exception ex) {
        }
        featureCollectionListModel.clear();
        getMappingComponent().setReadOnly(initialMcReadonly);
        tempFeature = null;
        super.dispose();
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshButtons() {
        jToggleButton2.setSelected(isShowFeature());
        jToggleButton1.setSelected(isShowFeature() && isMirrorA());
        jToggleButton1.setEnabled(isMirrorAAllowed() && isShowFeature() && (tempFeature != null));
        jToggleButton3.setSelected(isShowFeature() && isMirrorB());
        jToggleButton3.setEnabled(isMirrorBAllowed() && isShowFeature() && (tempFeature != null));
        jButton1.setEnabled(jList1.getSelectedValue() instanceof PureNewFeature);
        jButton2.setEnabled(jList2.getSelectedValue() instanceof PureNewFeature);
    }

    @Override
    public void setVisible(final boolean b) {
        initialMcReadonly = getMappingComponent().isReadOnly();
        getMappingComponent().setReadOnly(false);

        super.setVisible(b);
        if (b) {
            setFeatureCollectionListenerEnabled(true);
            featureCollectionListModel.refresh();
        }
        if (isShowFeature() && (tempFeature != null)) {
            getMappingComponent().getTmpFeatureLayer().addChild(tempFeature);
        }
        refreshButtons();
    }

    /**
     * DOCUMENT ME!
     */
    private void doCalculateAngle() {
        final Feature featureA = (Feature)jList1.getSelectedValue();
        final Feature featureB = (Feature)jList2.getSelectedValue();

        try {
            getMappingComponent().getTmpFeatureLayer().removeChild(tempFeature);
        } catch (final Exception ex) {
        }
        if ((featureA == null) || !checkForSegment(featureA.getGeometry()) || (featureB == null)
                    || !checkForSegment(featureB.getGeometry())) {
            return;
        }
        final LineString geomA = (LineString)featureA.getGeometry();
        final LineString geomB = (LineString)featureB.getGeometry();

        final LineSegment segA = new LineSegment(geomA.getCoordinateN(0), geomA.getCoordinateN(1));
        final LineSegment segB = new LineSegment(geomB.getCoordinateN(0), geomB.getCoordinateN(1));
        final Coordinate intersection = segA.lineIntersection(segB);

        if (intersection != null) {
            final boolean intersectsA = segA.distance(intersection) < 0.01;
            final boolean intersectsB = segB.distance(intersection) < 0.01;

            setMirrorAAllowed(intersectsA);
            setMirrorBAllowed(intersectsB);

            final Coordinate cA0 =
                (intersection.distance(segA.getCoordinate(0)) > intersection.distance(segA.getCoordinate(1)))
                ? segA.getCoordinate(0) : segA.getCoordinate(1);
            final Coordinate cA1 =
                (intersection.distance(segA.getCoordinate(0)) > intersection.distance(segA.getCoordinate(1)))
                ? segA.getCoordinate(1) : segA.getCoordinate(0);
            final Coordinate cB0 =
                (intersection.distance(segB.getCoordinate(0)) > intersection.distance(segB.getCoordinate(1)))
                ? segB.getCoordinate(0) : segB.getCoordinate(1);
            final Coordinate cB1 =
                (intersection.distance(segB.getCoordinate(0)) > intersection.distance(segB.getCoordinate(1)))
                ? segB.getCoordinate(1) : segB.getCoordinate(0);

            final Coordinate cA;
            final Coordinate cB;
            if (!isMirrorA()) {
                cA = cA0;
            } else {
                cA = cA1;
            }
            if (!isMirrorB()) {
                cB = cB0;
            } else {
                cB = cB1;
            }
            final LineSegment interSegA = new LineSegment(intersection, cA);
            final LineSegment interSegB = new LineSegment(intersection, cB);
            double angle;
            if (interSegA.angle() > interSegB.angle()) {
                angle = Math.toDegrees(interSegA.angle())
                            - Math.toDegrees(interSegB.angle());
            } else {
                angle = Math.toDegrees(interSegB.angle()) - Math.toDegrees(interSegA.angle());
            }
            if (angle > 180) {
                angle = 360 - angle;
            }
            tempFeature = createNewTempFeature();
            tempFeature.setPathToPolyline(
                new Point2D[] {
                    new Point2D.Double(
                        getMappingComponent().getWtst().getScreenX(cA.x),
                        getMappingComponent().getWtst().getScreenY(cA.y)),
                    new Point2D.Double(
                        getMappingComponent().getWtst().getScreenX(intersection.x),
                        getMappingComponent().getWtst().getScreenY(intersection.y)),
                    new Point2D.Double(
                        getMappingComponent().getWtst().getScreenX(cB.x),
                        getMappingComponent().getWtst().getScreenY(cB.y))
                });
            if (isShowFeature()) {
                getMappingComponent().getTmpFeatureLayer().addChild(tempFeature);
            }

            jLabel4.setText(FORMAT.format(angle) + " °");
        } else {
            setMirrorAAllowed(false);
            setMirrorBAllowed(false);
            tempFeature = null;
            jLabel4.setText("∞ °");
        }
        refreshButtons();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final AngleMeasurementDialog dialog = new AngleMeasurementDialog();
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    dialog.setVisible(true);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean checkForSegment(final Geometry geom) {
        return ((geom instanceof LineString) && (geom.getNumPoints() == 2));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected PPath createNewTempFeature() {
        final PPath newTempFeaturePath = new PPath();
        newTempFeaturePath.setStroke(new FixedWidthStroke());
        final Color fillingColor = Color.BLUE;
        newTempFeaturePath.setStrokePaint(fillingColor);
        newTempFeaturePath.setPaint(fillingColor);
        newTempFeaturePath.setTransparency(0.2f);
        return newTempFeaturePath;
    }

    /**
     * DOCUMENT ME!
     */
    private void applyListSelectionToMap() {
        final Feature feature1 = (Feature)jList1.getSelectedValue();
        final Feature feature2 = (Feature)jList2.getSelectedValue();
        final Collection<Feature> featuresToSelect = new ArrayList<Feature>();
        if ((feature1 != null) && getMappingComponent().getFeatureCollection().contains(feature1)) {
            featuresToSelect.add(feature1);
        }
        if ((feature2 != null) && getMappingComponent().getFeatureCollection().contains(feature2)) {
            featuresToSelect.add(feature2);
        }
        setFeatureCollectionListenerEnabled(false);
        try {
            getMappingComponent().getFeatureCollection().select(featuresToSelect);
        } finally {
            setFeatureCollectionListenerEnabled(true);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class FeatureCollectionListModel extends DefaultListModel<Feature> {

        //~ Instance fields ----------------------------------------------------

        private final List<Feature> segmentFeatures = new HashArrayList<Feature>();
        private final FeatureCollectionListener featureCollectionListener = new FeatureCollectionListener() {

                @Override
                public void featuresAdded(final FeatureCollectionEvent fce) {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                            boolean changed = false;
                            for (final Feature feature : filterSegments(fce.getEventFeatures())) {
                                if (!segmentFeatures.contains(feature)) {
                                    segmentFeatures.add(feature);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                fireContentChanged();
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while featuresAdded", ex);
                    }
                }

                @Override
                public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                            if (!segmentFeatures.isEmpty()) {
                                jList1.clearSelection();
                                jList2.clearSelection();
                                segmentFeatures.clear();
                                fireContentChanged();
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while allFeaturesRemoved", ex);
                    }
                }

                @Override
                public void featuresRemoved(final FeatureCollectionEvent fce) {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                            boolean changed = false;
                            for (final Feature segment : filterSegments(fce.getEventFeatures())) {
                                if (segmentFeatures.contains(segment)) {
                                    if (segment.equals(jList1.getSelectedValue())) {
                                        jList1.clearSelection();
                                    }
                                    if (segment.equals(jList2.getSelectedValue())) {
                                        jList2.clearSelection();
                                    }
                                    segmentFeatures.remove(segment);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                fireContentChanged();
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while featuresRemoved", ex);
                    }
                }

                @Override
                public void featuresChanged(final FeatureCollectionEvent fce) {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                            boolean changed = false;
                            for (final Feature feature : fce.getEventFeatures()) {
                                if (fce.getFeatureCollection().contains(feature)
                                            && fce.getEventFeatures().contains(feature)) {
                                    if (segmentFeatures.contains(feature) && !checkForSegment(feature.getGeometry())) {
                                        if (feature.equals(jList1.getSelectedValue())) {
                                            jList1.clearSelection();
                                        }
                                        if (feature.equals(jList2.getSelectedValue())) {
                                            jList2.clearSelection();
                                        }
                                        segmentFeatures.remove(feature);
                                        changed = true;
                                    } else if (!segmentFeatures.contains(feature)
                                                && checkForSegment(feature.getGeometry())) {
                                        segmentFeatures.add(feature);
                                        changed = true;
                                    } else if (feature.equals(jList1.getSelectedValue())
                                                || feature.equals(jList2.getSelectedValue())) {
                                        doCalculateAngle();
                                    }
                                }
                            }
                            if (changed) {
                                SwingUtilities.invokeLater(new Thread() {

                                        @Override
                                        public void run() {
                                            fireContentChanged();
                                        }
                                    });
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while featuresChanged", ex);
                    }
                }

                @Override
                public void featureSelectionChanged(final FeatureCollectionEvent fce) {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                            selectListFromFeatureCollection(getMappingComponent().getFeatureCollection());
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while featureSelectionChanged", ex);
                    }
                }

                @Override
                public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while featureReconsiderationRequested", ex);
                    }
                }

                @Override
                public void featureCollectionChanged() {
                    try {
                        if (isFeatureCollectionListenerEnabled() && isVisible()) {
                        }
                    } catch (final Exception ex) {
                        LOG.warn("error while featureCollectionChanged", ex);
                    }
                }
            };

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureCollectionComboBoxModel object.
         */
        public FeatureCollectionListModel() {
            getMappingComponent().getFeatureCollection().addFeatureCollectionListener(featureCollectionListener);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        public void refresh() {
            segmentFeatures.clear();
            segmentFeatures.addAll(filterSegments(getMappingComponent().getFeatureCollection().getAllFeatures()));
            fireContentChanged();
            selectListFromFeatureCollection(getMappingComponent().getFeatureCollection());
        }

        /**
         * DOCUMENT ME!
         *
         * @param   allFeatures  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public final Collection<Feature> filterSegments(final Collection<Feature> allFeatures) {
            final Collection<Feature> filtredFeatures = new ArrayList<Feature>();
            for (final Feature potentialSegment : allFeatures) {
                final Geometry geom = potentialSegment.getGeometry();
                if (checkForSegment(geom)) {
                    filtredFeatures.add(potentialSegment);
                }
            }
            return filtredFeatures;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  featureCollection  DOCUMENT ME!
         */
        private void selectListFromFeatureCollection(final FeatureCollection featureCollection) {
            final Collection<Feature> features = featureCollection.getSelectedFeatures();
            final List<Feature> selectedSegmentFeatures = new ArrayList<Feature>();
            for (final Feature feature : features) {
                if (segmentFeatures.contains(feature)) {
                    selectedSegmentFeatures.add(feature);
                }
            }
            if (selectedSegmentFeatures.size() == 1) {
                setListSelectionListenerEnabled(false);
                try {
                    jList1.setSelectedValue(selectedSegmentFeatures.get(0), true);
                    jList2.clearSelection();
                } finally {
                    setListSelectionListenerEnabled(true);
                }
            } else if (selectedSegmentFeatures.size() == 2) {
                final Feature featureA = selectedSegmentFeatures.get(0);
                final Feature featureB = selectedSegmentFeatures.get(1);
                setListSelectionListenerEnabled(false);
                try {
                    jList1.setSelectedValue(featureA, true);
                    jList2.setSelectedValue(featureB, true);
                } finally {
                    setListSelectionListenerEnabled(true);
                }
            } else {
                setListSelectionListenerEnabled(false);
                try {
                    jList1.clearSelection();
                    jList2.clearSelection();
                } finally {
                    setListSelectionListenerEnabled(true);
                }
            }
            doCalculateAngle();
        }

        /**
         * DOCUMENT ME!
         */
        private void fireContentChanged() {
            fireContentsChanged(this, 0, getSize() - 1);
            if (getSize() == 2) {
                setListSelectionListenerEnabled(false);
                try {
                    jList1.setSelectedIndex(0);
                    jList2.setSelectedIndex(1);
                } finally {
                    setListSelectionListenerEnabled(true);
                }
                applyListSelectionToMap();
                doCalculateAngle();
            }
        }

        @Override
        public Feature getElementAt(final int index) {
            if ((index < 0) || (index >= getSize())) {
                return null;
            }
            return segmentFeatures.get(index);
        }

        @Override
        public int getSize() {
            return segmentFeatures.size();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class FeatureCollectionListCellRenderer extends DefaultListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final Component superComp = super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);
            if ((value instanceof XStyledFeature) && (superComp instanceof JLabel)) {
                ((JLabel)superComp).setText(((XStyledFeature)value).getName());
            }
            return superComp;
        }
    }
}
