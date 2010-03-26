/*
 * WFSFormTester.java
 *
 * Created on 25. Juli 2006, 17:38
 */
package de.cismet.cismap.commons.wfsforms;

import com.vividsolutions.jts.geom.Point;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.deegree2.datatypes.QualifiedName;
import org.deegree2.model.feature.DefaultFeature;
import org.deegree2.model.feature.FeatureProperty;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class WFSFormPOISearch extends AbstractWFSForm implements ActionListener {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());    private WFSFormFeature strasse = null;
    private WFSFormFeature poi = null;

    /** Creates new form WFSFormTester */
    public WFSFormPOISearch() {
        log.debug("new WFSFormPOISearch");//NOI18N
        try {
            initComponents();
//        cboStreets.setEditable(true);
//        cboNr.setEditable(true);
            AutoCompleteDecorator.decorate(cboPois);
//        prbLocationtypes.setPreferredSize(new java.awt.Dimension(1,5));
            prbPois.setPreferredSize(new java.awt.Dimension(1, 5));

            cboPois.setRenderer(new ListCellRenderer() {

                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                    DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
                    JLabel lbl = (JLabel) (dlcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus));
                    String additionalInfo = "";//NOI18N
                    try {
                       
                        FeatureProperty[] fpa = ((WFSFormFeature) value).getRawFeatureArray("app", "alternativeGeographicIdentifier", "http://www.deegree.org/app");//NOI18N

                        for (int i = 0; i < fpa.length; ++i) {
                            if (i>0){
                                additionalInfo+=", " ;//NOI18N
                            }
                            
                            additionalInfo+=((DefaultFeature) fpa[i].getValue()).getProperties(new QualifiedName("app", "alternativeGeographicIdentifier", new URI("http://www.deegree.org/app")))[0].getValue().toString();//NOI18N

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


//        listComponents.put("cboAllLocationtypes",cboLocationtypes);
//        listComponents.put("cboAllLocationtypesProgress",prbLocationtypes);
            listComponents.put("cboPois", cboPois);//NOI18N
            listComponents.put("cboPoisProgress", prbPois);//NOI18N

            pMark.setVisible(false);
            pMark.setSweetSpotX(0.5d);
            pMark.setSweetSpotY(1d);
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {

                public void changedUpdate(DocumentEvent e) {
                    doSearch();
                }

                public void insertUpdate(DocumentEvent e) {
                    doSearch();
                }

                public void removeUpdate(DocumentEvent e) {
                    doSearch();
                }
            });

            lblBehind.setMinimumSize(new Dimension(94, 16));
            lblBehind.setMaximumSize(new Dimension(94, 16));
            lblBehind.setPreferredSize(new Dimension(94, 16));
            super.addActionListener(this);

        //CismapBroker.getInstance().getMappingComponent().getHighlightingLayer().addChild(pMark);
        } catch (Exception e) {
            log.error("Could not Create WFForm", e);//NOI18N
        }
    }

    public void garbageDuringAutoCompletion(JComboBox box){

    }
    private void doSearch() {
        if (txtSearch.getText().length() >= 3) {
            log.debug("doSearch");//NOI18N
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("@@search_text@@", txtSearch.getText());//NOI18N
            requestRefresh("cboPois", hm);//NOI18N
        } else {
            lblBehind.setText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.lblBehind.text"));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cmdOk = new javax.swing.JButton();
        panPois = new javax.swing.JPanel();
        cboPois = new javax.swing.JComboBox();
        prbPois = new javax.swing.JProgressBar();
        chkVisualize = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        chkLockScale = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblBehind = new javax.swing.JLabel();
        panFill = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setMinimumSize(new java.awt.Dimension(373, 1));
        setLayout(new java.awt.GridBagLayout());

        cmdOk.setMnemonic('P');
        cmdOk.setText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.cmdOk.text")); // NOI18N
        cmdOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(cmdOk, gridBagConstraints);

        panPois.setMinimumSize(new java.awt.Dimension(220, 23));
        panPois.setPreferredSize(new java.awt.Dimension(220, 24));
        panPois.setLayout(new java.awt.GridBagLayout());

        cboPois.setEnabled(false);
        cboPois.setMaximumSize(new java.awt.Dimension(32767, 19));
        cboPois.setMinimumSize(new java.awt.Dimension(10, 18));
        cboPois.setPreferredSize(new java.awt.Dimension(27, 19));
        cboPois.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboPoisActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 30.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        panPois.add(cboPois, gridBagConstraints);

        prbPois.setBorderPainted(false);
        prbPois.setMaximumSize(new java.awt.Dimension(32767, 5));
        prbPois.setMinimumSize(new java.awt.Dimension(10, 5));
        prbPois.setPreferredSize(new java.awt.Dimension(100, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 10);
        panPois.add(prbPois, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 0, 0);
        add(panPois, gridBagConstraints);

        chkVisualize.setSelected(true);
        chkVisualize.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.chkVisualize.toolTipText")); // NOI18N
        chkVisualize.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkVisualize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVisualizeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        add(chkVisualize, gridBagConstraints);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/markPoint.png"))); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.jLabel1.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        add(jLabel1, gridBagConstraints);

        chkLockScale.setSelected(true);
        chkLockScale.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.chkLockScale.toolTipText")); // NOI18N
        chkLockScale.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 14, 0, 0);
        add(chkLockScale, gridBagConstraints);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png"))); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.jLabel2.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 7, 0, 0);
        add(jLabel2, gridBagConstraints);

        txtSearch.setMinimumSize(new java.awt.Dimension(80, 20));
        txtSearch.setPreferredSize(new java.awt.Dimension(80, 20));
        txtSearch.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtSearchInputMethodTextChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(txtSearch, gridBagConstraints);

        lblBehind.setText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.lblBehind.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 0);
        add(lblBehind, gridBagConstraints);

        panFill.setMinimumSize(new java.awt.Dimension(1, 1));
        panFill.setPreferredSize(new java.awt.Dimension(1, 1));

        org.jdesktop.layout.GroupLayout panFillLayout = new org.jdesktop.layout.GroupLayout(panFill);
        panFill.setLayout(panFillLayout);
        panFillLayout.setHorizontalGroup(
            panFillLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 129, Short.MAX_VALUE)
        );
        panFillLayout.setVerticalGroup(
            panFillLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 34, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panFill, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    private void txtSearchInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtSearchInputMethodTextChanged
        log.fatal("kik");//NOI18N
    }//GEN-LAST:event_txtSearchInputMethodTextChanged

    private void chkVisualizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVisualizeActionPerformed
        MappingComponent mc = getMappingComponent();
        if (mc == null) {
            mc = CismapBroker.getInstance().getMappingComponent();
        }

        if (!mc.getHighlightingLayer().getChildrenReference().contains(pMark)) {
            mc.getHighlightingLayer().addChild(pMark);
        }
        if (poi != null) {
            Point p = poi.getPosition();
            double x = mc.getWtst().getScreenX(p.getCoordinate().x);
            double y = mc.getWtst().getScreenY(p.getCoordinate().y);
            pMark.setOffset(x, y);
            pMark.setVisible(chkVisualize.isSelected());
        }
    }//GEN-LAST:event_chkVisualizeActionPerformed

    private void cmdOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOkActionPerformed
        boolean history = true;
        MappingComponent mc = getMappingComponent();
        if (mc == null) {
            mc = CismapBroker.getInstance().getMappingComponent();
        }
        boolean scaling = !(mc.isFixedMapScale()) && !(chkLockScale.isSelected());
        BoundingBox bb = null;
        int animation = mc.getAnimationDuration();
        if (poi != null) {
            bb = new BoundingBox(poi.getJTSGeometry());
        } else {
            return;
        }
        mc.gotoBoundingBox(bb, history, scaling, animation);
        chkVisualizeActionPerformed(null);
    }//GEN-LAST:event_cmdOkActionPerformed

    private void cboPoisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboPoisActionPerformed
        log.debug("cboPoisActionPerformed()");//NOI18N
        if (cboPois.getSelectedItem() instanceof WFSFormFeature) {
            poi = (WFSFormFeature) cboPois.getSelectedItem();
        }
    }//GEN-LAST:event_cboPoisActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboPois;
    private javax.swing.JCheckBox chkLockScale;
    private javax.swing.JCheckBox chkVisualize;
    private javax.swing.JButton cmdOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblBehind;
    private javax.swing.JPanel panFill;
    private javax.swing.JPanel panPois;
    private javax.swing.JProgressBar prbPois;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
   

    public void actionPerformed(ActionEvent e) {
        lblBehind.setText(org.openide.util.NbBundle.getMessage(WFSFormPOISearch.class, "WFSFormPOISearch.lblBehind.text2", new Object[]{cboPois.getItemCount()}));
        log.debug("cboPois.getItemAt(0):" + cboPois.getItemAt(0));//NOI18N
        if (cboPois.getItemCount() == 1) {
            cboPois.setEditable(false);
            cboPois.setSelectedItem(cboPois.getItemAt(0));
            cboPois.setEditable(true);
        }
    }
}
