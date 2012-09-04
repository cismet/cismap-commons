/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * WFSFormTester.java
 *
 * Created on 25. Juli 2006, 17:38
 */
package de.cismet.cismap.commons.wfsforms;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import com.vividsolutions.jts.geom.Point;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.DefaultFeature;
import org.deegree.model.feature.FeatureProperty;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.net.URI;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WFSFormBPlanSearch extends AbstractWFSForm implements ActionListener {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private WFSFormFeature strasse = null;
    private WFSFormFeature hit = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboHits;
    private javax.swing.JCheckBox chkLockScale;
    private javax.swing.JCheckBox chkVisualize;
    private javax.swing.JButton cmdOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblBehind;
    private javax.swing.JPanel panFill;
    private javax.swing.JProgressBar prbHits;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WFSFormTester.
     */
    public WFSFormBPlanSearch() {
        if (log.isDebugEnabled()) {
            log.debug("new WFSFormBPlanSearch");            // NOI18N
        }
        try {
            initComponents();
            listComponents.put("cboHits", cboHits);         // NOI18N
            listComponents.put("cboHitsProgress", prbHits); // NOI18N
//        cboStreets.setEditable(true);
//        cboNr.setEditable(true);
            StaticSwingTools.decorateWithFixedAutoCompleteDecorator(cboHits);
//        prbLocationtypes.setPreferredSize(new java.awt.Dimension(1,5));
            prbHits.setPreferredSize(new java.awt.Dimension(1, 5));

            cboHits.setRenderer(new ListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(final JList list,
                            final Object value,
                            final int index,
                            final boolean isSelected,
                            final boolean cellHasFocus) {
                        final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
                        final JLabel lbl = (JLabel)(dlcr.getListCellRendererComponent(
                                    list,
                                    value,
                                    index,
                                    isSelected,
                                    cellHasFocus));
                        String additionalInfo = "";                // NOI18N
                        try {
                            final FeatureProperty[] fpa = ((WFSFormFeature)value).getRawFeatureArray(
                                    "app",
                                    "alternativeGeographicIdentifier",
                                    "http://www.deegree.org/app"); // NOI18N
                            if (fpa != null) {
                                for (int i = 0; i < fpa.length; ++i) {
                                    if (i > 0) {
                                        additionalInfo += ", ";    // NOI18N
                                    }

                                    additionalInfo += ((DefaultFeature)fpa[i].getValue())
                                                .getProperties(
                                                    new QualifiedName(
                                                        "app",
                                                        "alternativeGeographicIdentifier",
                                                        new URI("http://www.deegree.org/app")))[0].getValue()
                                                .toString(); // NOI18N
                                }
                            }
                        } catch (Exception ex) {
                            log.error(ex, ex);
                        }

                        if (additionalInfo != null) {
                            lbl.setToolTipText(additionalInfo);
                        }
                        return lbl;
                    }
                });

            pMark.setVisible(false);
            pMark.setSweetSpotX(0.5d);
            pMark.setSweetSpotY(1d);
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        doSearch();
                    }

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        doSearch();
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        doSearch();
                    }
                });

            lblBehind.setMinimumSize(new Dimension(94, 16));
            lblBehind.setMaximumSize(new Dimension(94, 16));
            lblBehind.setPreferredSize(new Dimension(94, 16));
            super.addActionListener(this);

            // CismapBroker.getInstance().getMappingComponent().getHighlightingLayer().addChild(pMark);
        } catch (Exception e) {
            log.error("Could not Create WFForm", e); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void garbageDuringAutoCompletion(final JComboBox box) {
    }

    /**
     * DOCUMENT ME!
     */
    private void doSearch() {
        if (txtSearch.getText().length() >= 2) {
            if (log.isDebugEnabled()) {
                log.debug("doSearch");                      // NOI18N
            }
            final HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("@@search_text@@", txtSearch.getText()); // NOI18N
            requestRefresh("cboHits", hm);                  // NOI18N
        } else {
            lblBehind.setText(org.openide.util.NbBundle.getMessage(
                    WFSFormBPlanSearch.class,
                    "WFSFormBPlanSearch.lblBehind.text"));  // NOI18N
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cmdOk = new javax.swing.JButton();
        chkVisualize = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        chkLockScale = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblBehind = new javax.swing.JLabel();
        panFill = new javax.swing.JPanel();
        cboHits = new javax.swing.JComboBox();
        prbHits = new javax.swing.JProgressBar();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setMinimumSize(new java.awt.Dimension(373, 1));
        setLayout(new java.awt.GridBagLayout());

        cmdOk.setMnemonic('P');
        cmdOk.setText(org.openide.util.NbBundle.getMessage(WFSFormBPlanSearch.class, "WFSFormBPlanSearch.cmdOk.text")); // NOI18N
        cmdOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(cmdOk, gridBagConstraints);

        chkVisualize.setSelected(true);
        chkVisualize.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormBPlanSearch.class,
                "WFSFormBPlanSearch.chkVisualize.toolTipText")); // NOI18N
        chkVisualize.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkVisualize.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkVisualizeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 0);
        add(chkVisualize, gridBagConstraints);

        jLabel1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/markPoint.png"))); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormBPlanSearch.class,
                "WFSFormBPlanSearch.jLabel1.toolTipText"));                                  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        add(jLabel1, gridBagConstraints);

        chkLockScale.setSelected(true);
        chkLockScale.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormBPlanSearch.class,
                "WFSFormBPlanSearch.chkLockScale.toolTipText")); // NOI18N
        chkLockScale.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 14, 0, 0);
        add(chkLockScale, gridBagConstraints);

        jLabel2.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png"))); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormBPlanSearch.class,
                "WFSFormBPlanSearch.jLabel2.toolTipText"));                                    // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 0, 0);
        add(jLabel2, gridBagConstraints);

        txtSearch.setMaximumSize(new java.awt.Dimension(100, 19));
        txtSearch.setMinimumSize(new java.awt.Dimension(100, 19));
        txtSearch.setPreferredSize(new java.awt.Dimension(100, 19));
        txtSearch.addInputMethodListener(new java.awt.event.InputMethodListener() {

                @Override
                public void caretPositionChanged(final java.awt.event.InputMethodEvent evt) {
                }
                @Override
                public void inputMethodTextChanged(final java.awt.event.InputMethodEvent evt) {
                    txtSearchInputMethodTextChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(txtSearch, gridBagConstraints);

        lblBehind.setText(org.openide.util.NbBundle.getMessage(
                WFSFormBPlanSearch.class,
                "WFSFormBPlanSearch.lblBehind.text")); // NOI18N
        lblBehind.setMaximumSize(new java.awt.Dimension(120, 14));
        lblBehind.setMinimumSize(new java.awt.Dimension(120, 14));
        lblBehind.setPreferredSize(new java.awt.Dimension(120, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
        add(lblBehind, gridBagConstraints);

        panFill.setMinimumSize(new java.awt.Dimension(1, 1));
        panFill.setPreferredSize(new java.awt.Dimension(1, 1));

        final org.jdesktop.layout.GroupLayout panFillLayout = new org.jdesktop.layout.GroupLayout(panFill);
        panFill.setLayout(panFillLayout);
        panFillLayout.setHorizontalGroup(
            panFillLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 131, Short.MAX_VALUE));
        panFillLayout.setVerticalGroup(
            panFillLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 30, Short.MAX_VALUE));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panFill, gridBagConstraints);

        cboHits.setEnabled(false);
        cboHits.setMaximumSize(new java.awt.Dimension(100, 19));
        cboHits.setMinimumSize(new java.awt.Dimension(100, 19));
        cboHits.setPreferredSize(new java.awt.Dimension(100, 19));
        cboHits.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboHitsActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 0);
        add(cboHits, gridBagConstraints);

        prbHits.setBorderPainted(false);
        prbHits.setMaximumSize(new java.awt.Dimension(100, 5));
        prbHits.setMinimumSize(new java.awt.Dimension(100, 5));
        prbHits.setPreferredSize(new java.awt.Dimension(100, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 10);
        add(prbHits, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtSearchInputMethodTextChanged(final java.awt.event.InputMethodEvent evt) { //GEN-FIRST:event_txtSearchInputMethodTextChanged
    }                                                                                         //GEN-LAST:event_txtSearchInputMethodTextChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkVisualizeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkVisualizeActionPerformed
        MappingComponent mc = getMappingComponent();
        if (mc == null) {
            mc = CismapBroker.getInstance().getMappingComponent();
        }

        if (hit != null) {
            visualizePosition(hit, chkVisualize.isSelected());
        }
    } //GEN-LAST:event_chkVisualizeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOkActionPerformed
        final boolean history = true;
        MappingComponent mc = getMappingComponent();
        if (mc == null) {
            mc = CismapBroker.getInstance().getMappingComponent();
        }
        final boolean scaling = !(mc.isFixedMapScale()) && !(chkLockScale.isSelected());
        XBoundingBox bb = null;
        final int animation = mc.getAnimationDuration();
        if (hit != null) {
            bb = new XBoundingBox(hit.getJTSGeometry());
        } else {
            return;
        }
        mc.gotoBoundingBox(bb, history, scaling, animation);
        chkVisualizeActionPerformed(null);
        mc.rescaleStickyNodes();
    }                                                                         //GEN-LAST:event_cmdOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboHitsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboHitsActionPerformed
        if (log.isDebugEnabled()) {
            log.debug("cboHitssActionPerformed()");                             // NOI18N
        }
        if (cboHits.getSelectedItem() instanceof WFSFormFeature) {
            hit = (WFSFormFeature)cboHits.getSelectedItem();
        }
    }                                                                           //GEN-LAST:event_cboHitsActionPerformed

    @Override
    public void actionPerformed(final ActionEvent e) {
        lblBehind.setText(org.openide.util.NbBundle.getMessage(
                WFSFormBPlanSearch.class,
                "WFSFormBPlanSearch.lblBehind.text2",
                new Object[] { cboHits.getItemCount() }));             // NOI18N
        if (log.isDebugEnabled()) {
            log.debug("cboPois.getItemAt(0):" + cboHits.getItemAt(0)); // NOI18N
        }
        if (cboHits.getItemCount() == 1) {
            cboHits.setEditable(false);
            cboHits.setSelectedItem(cboHits.getItemAt(0));
            cboHits.setEditable(true);
        }
    }
}
