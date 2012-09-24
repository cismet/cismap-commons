/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * WFSFormGemarkungFlurFlurstueck.java
 *
 * Created on 2. August 2006, 14:45
 */
package de.cismet.cismap.commons.wfsforms;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WFSFormGemarkungFlurFlurstueck extends AbstractWFSForm {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private WFSFormFeature gemarkung = null;
    private WFSFormFeature flur = null;
    private WFSFormFeature flurstueck = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboFlur;
    private javax.swing.JComboBox cboFlurstueck;
    private javax.swing.JComboBox cboGem;
    private javax.swing.JCheckBox chkLockScale;
    private javax.swing.JCheckBox chkVisualize;
    private javax.swing.JButton cmdPos;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel panEmpty;
    private javax.swing.JProgressBar prbFlur;
    private javax.swing.JProgressBar prbFlurstueck;
    private javax.swing.JProgressBar prbGem;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WFSFormGemarkungFlurFlurstueck.
     */
    public WFSFormGemarkungFlurFlurstueck() {
        try {
            initComponents();
//        prbFlur.setPreferredSize(new Dimension(1,5));
//        prbFlurstueck.setPreferredSize(new Dimension(1,5));
//        prbGem.setPreferredSize(new Dimension(1,5));
            StaticSwingTools.decorateWithFixedAutoCompleteDecorator(cboGem);
            StaticSwingTools.decorateWithFixedAutoCompleteDecorator(cboFlur);
            StaticSwingTools.decorateWithFixedAutoCompleteDecorator(cboFlurstueck);
            listComponents.put("cboGem", cboGem);                       // NOI18N
            listComponents.put("cboGemProgress", prbGem);               // NOI18N
            listComponents.put("cboFlur", cboFlur);                     // NOI18N
            listComponents.put("cboFlurProgress", prbFlur);             // NOI18N
            listComponents.put("cboFlurstueck", cboFlurstueck);         // NOI18N
            listComponents.put("cboFlurstueckProgress", prbFlurstueck); // NOI18N

            final JTextField flurEditor = (JTextField)cboFlur.getEditor().getEditorComponent();
            flurEditor.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        // log.fatal(cboFlur.getSelectedIndex());
                        checkCboCorrectness(cboFlur);
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        checkCboCorrectness(cboFlur);
                    }

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        checkCboCorrectness(cboFlur);
                    }
                });

            final JTextField flurstueckEditor = (JTextField)cboFlurstueck.getEditor().getEditorComponent();
            flurstueckEditor.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        // log.fatal(cboFlurstueck.getSelectedIndex());
                        checkCboCorrectness(cboFlurstueck);
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        checkCboCorrectness(cboFlurstueck);
                    }

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        checkCboCorrectness(cboFlurstueck);
                    }
                });
        } catch (Exception e) {
            log.error("Could not Create WFForm", e); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void garbageDuringAutoCompletion(final JComboBox box) {
        if (box == cboFlur) {
            flur = null;
        } else if (box == cboFlurstueck) {
            flurstueck = null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cmdPos = new javax.swing.JButton();
        chkVisualize = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        chkLockScale = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        panEmpty = new javax.swing.JPanel();
        cboGem = new javax.swing.JComboBox();
        prbGem = new javax.swing.JProgressBar();
        cboFlurstueck = new javax.swing.JComboBox();
        prbFlurstueck = new javax.swing.JProgressBar();
        cboFlur = new javax.swing.JComboBox();
        prbFlur = new javax.swing.JProgressBar();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setPreferredSize(new java.awt.Dimension(400, 38));
        setLayout(new java.awt.GridBagLayout());

        cmdPos.setMnemonic('P');
        cmdPos.setText(org.openide.util.NbBundle.getMessage(
                WFSFormGemarkungFlurFlurstueck.class,
                "WFSFormGemarkungFlurFlurstueck.cmdPos.text")); // NOI18N
        cmdPos.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdPosActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(cmdPos, gridBagConstraints);

        chkVisualize.setSelected(true);
        chkVisualize.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormGemarkungFlurFlurstueck.class,
                "WFSFormGemarkungFlurFlurstueck.chkVisualize.toolTipText")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 0, 0);
        add(chkVisualize, gridBagConstraints);

        jLabel2.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/markPoint.png"))); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormGemarkungFlurFlurstueck.class,
                "WFSFormGemarkungFlurFlurstueck.jLabel2.toolTipText"));                      // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 7, 0, 0);
        add(jLabel2, gridBagConstraints);

        chkLockScale.setSelected(true);
        chkLockScale.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormGemarkungFlurFlurstueck.class,
                "WFSFormGemarkungFlurFlurstueck.chkLockScale.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 14, 0, 0);
        add(chkLockScale, gridBagConstraints);

        jLabel3.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png"))); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(
                WFSFormGemarkungFlurFlurstueck.class,
                "WFSFormGemarkungFlurFlurstueck.jLabel3.toolTipText"));                        // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 7, 3, 7);
        add(jLabel3, gridBagConstraints);

        panEmpty.setPreferredSize(new java.awt.Dimension(1, 1));

        final org.jdesktop.layout.GroupLayout panEmptyLayout = new org.jdesktop.layout.GroupLayout(panEmpty);
        panEmpty.setLayout(panEmptyLayout);
        panEmptyLayout.setHorizontalGroup(
            panEmptyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 32, Short.MAX_VALUE));
        panEmptyLayout.setVerticalGroup(
            panEmptyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 29, Short.MAX_VALUE));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(panEmpty, gridBagConstraints);

        cboGem.setEnabled(false);
        cboGem.setMaximumSize(new java.awt.Dimension(100, 19));
        cboGem.setMinimumSize(new java.awt.Dimension(100, 19));
        cboGem.setPreferredSize(new java.awt.Dimension(100, 19));
        cboGem.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboGemActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 3);
        add(cboGem, gridBagConstraints);

        prbGem.setBorderPainted(false);
        prbGem.setEnabled(false);
        prbGem.setMaximumSize(new java.awt.Dimension(100, 5));
        prbGem.setMinimumSize(new java.awt.Dimension(100, 5));
        prbGem.setPreferredSize(new java.awt.Dimension(100, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(prbGem, gridBagConstraints);

        cboFlurstueck.setEditable(true);
        cboFlurstueck.setEnabled(false);
        cboFlurstueck.setMaximumSize(new java.awt.Dimension(27, 19));
        cboFlurstueck.setMinimumSize(new java.awt.Dimension(27, 19));
        cboFlurstueck.setPreferredSize(new java.awt.Dimension(27, 19));
        cboFlurstueck.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboFlurstueckActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 25.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 3);
        add(cboFlurstueck, gridBagConstraints);

        prbFlurstueck.setBorderPainted(false);
        prbFlurstueck.setEnabled(false);
        prbFlurstueck.setMaximumSize(new java.awt.Dimension(1, 5));
        prbFlurstueck.setMinimumSize(new java.awt.Dimension(1, 5));
        prbFlurstueck.setPreferredSize(new java.awt.Dimension(1, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(prbFlurstueck, gridBagConstraints);

        cboFlur.setEditable(true);
        cboFlur.setEnabled(false);
        cboFlur.setMaximumSize(new java.awt.Dimension(27, 19));
        cboFlur.setMinimumSize(new java.awt.Dimension(27, 19));
        cboFlur.setPreferredSize(new java.awt.Dimension(27, 19));
        cboFlur.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboFlurActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 25.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 3);
        add(cboFlur, gridBagConstraints);

        prbFlur.setBorderPainted(false);
        prbFlur.setEnabled(false);
        prbFlur.setMaximumSize(new java.awt.Dimension(1, 5));
        prbFlur.setMinimumSize(new java.awt.Dimension(1, 5));
        prbFlur.setPreferredSize(new java.awt.Dimension(1, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(prbFlur, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdPosActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdPosActionPerformed
        final boolean history = true;
        MappingComponent mc = getMappingComponent();
        if (mc == null) {
            mc = CismapBroker.getInstance().getMappingComponent();
        }

        final boolean scaling = !(mc.isFixedMapScale()) && !(chkLockScale.isSelected());

        XBoundingBox bb = null;
        final int animation = mc.getAnimationDuration();
        if (flurstueck != null) {
            if (scaling) {
                bb = new XBoundingBox(flurstueck.getJTSGeometry());
            } else {
                bb = new XBoundingBox(flurstueck.getPosition().buffer(AbstractWFSForm.FEATURE_BORDER));
            }
        } else if (flur != null) {
            if (scaling) {
                bb = new XBoundingBox(flur.getJTSGeometry());
            } else {
                bb = new XBoundingBox(flur.getPosition().buffer(AbstractWFSForm.FEATURE_BORDER));
            }
        } else if (gemarkung != null) {
            if (scaling) {
                bb = new XBoundingBox(gemarkung.getJTSGeometry());
            } else {
                bb = new XBoundingBox(gemarkung.getPosition().buffer(AbstractWFSForm.FEATURE_BORDER));
            }
        } else {
            return;
        }
        mc.gotoBoundingBox(bb, history, scaling, animation);
        chkVisualizeActionPerformed(null);
    } //GEN-LAST:event_cmdPosActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboFlurstueckActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboFlurstueckActionPerformed
        final Object selected = cboFlurstueck.getSelectedItem();
        if (selected instanceof WFSFormFeature) {
            flurstueck = (WFSFormFeature)selected;
        }
    }                                                                                 //GEN-LAST:event_cboFlurstueckActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboFlurActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboFlurActionPerformed

        final Object selected = cboFlur.getSelectedItem();
        if (log.isDebugEnabled()) {
            log.debug("cboFlurActionPerformed selected=" + selected);  // NOI18N
        }
        if (selected instanceof WFSFormFeature) {
            flur = (WFSFormFeature)selected;
            flurstueck = null;
            requestRefresh("cboFlurstueck", (WFSFormFeature)selected); // NOI18N
        }
    }                                                                  //GEN-LAST:event_cboFlurActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboGemActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboGemActionPerformed
        final Object selected = cboGem.getSelectedItem();
        if (selected instanceof WFSFormFeature) {
            gemarkung = (WFSFormFeature)selected;
            flur = null;
            flurstueck = null;
            requestRefresh("cboFlur", (WFSFormFeature)selected);               // NOI18N
            cboFlurstueck.setEnabled(false);
            cboFlurstueck.setModel(new DefaultComboBoxModel(new Vector()));
        }
    }                                                                          //GEN-LAST:event_cboGemActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkVisualizeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkVisualizeActionPerformed
        if (mappingComponent == null) {
            mappingComponent = CismapBroker.getInstance().getMappingComponent();
        }

        if (flurstueck != null) {
            visualizePosition(flurstueck, chkVisualize.isSelected());
        } else if (flur != null) {
            visualizePosition(flur, chkVisualize.isSelected());
        } else if (gemarkung != null) {
            visualizePosition(gemarkung, chkVisualize.isSelected());
        }
    } //GEN-LAST:event_chkVisualizeActionPerformed
}
