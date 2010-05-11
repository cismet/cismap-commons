/*
 * WFSFormTester.java
 *
 * Created on 25. Juli 2006, 17:38
 */
package de.cismet.cismap.commons.wfsforms;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class WFSFormAdress extends AbstractWFSForm {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private WFSFormFeature strasse = null;
    private WFSFormFeature nr = null;
    private final ArrayList<WFSFormAdressListener> listeners = new ArrayList<WFSFormAdressListener>();

    /** Creates new form WFSFormTester */
    public WFSFormAdress() {
        log.debug("new WFSFormAddress");//NOI18N
        try {
            initComponents();
//        cboStreets.setEditable(true);
//        cboNr.setEditable(true);
            AutoCompleteDecorator.decorate(cboStreets);

            AutoCompleteDecorator.decorate(cboNr);
            prbStreets.setPreferredSize(new java.awt.Dimension(1, 5));
            prbNr.setPreferredSize(new java.awt.Dimension(1, 5));

            listComponents.put("cboAllStreets", cboStreets);//NOI18N
            listComponents.put("cboAllStreetsProgress", prbStreets);//NOI18N
            listComponents.put("cboNumbersOfAStreet", cboNr);//NOI18N
            listComponents.put("cboNumbersOfAStreetProgress", prbNr);//NOI18N

            pMark.setVisible(false);
            pMark.setSweetSpotX(0.5d);
            pMark.setSweetSpotY(1d);

            //log.fatal(cboNr.getEditor().getEditorComponent());
            JTextField nrEditor = (JTextField) cboNr.getEditor().getEditorComponent();
            nrEditor.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(DocumentEvent e) {
                    checkCboCorrectness(cboNr);
                }

                public void removeUpdate(DocumentEvent e) {
                    checkCboCorrectness(cboNr);
                }

                public void changedUpdate(DocumentEvent e) {
                    checkCboCorrectness(cboNr);
                }
            });
            //CismapBroker.getInstance().getMappingComponent().getHighlightingLayer().addChild(pMark);
        } catch (Exception e) {
            log.error("Could not Create WFForm", e);//NOI18N
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

        jPanel3 = new javax.swing.JPanel();
        cmdOk = new javax.swing.JButton();
        chkVisualize = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        chkLockScale = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        panEmpty = new javax.swing.JPanel();
        cboNr = new javax.swing.JComboBox();
        prbNr = new javax.swing.JProgressBar();
        cboStreets = new javax.swing.JComboBox();
        prbStreets = new javax.swing.JProgressBar();

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setMaximumSize(new java.awt.Dimension(498, 35));
        setMinimumSize(new java.awt.Dimension(498, 35));
        setLayout(new java.awt.GridBagLayout());

        cmdOk.setMnemonic('P');
        cmdOk.setText(org.openide.util.NbBundle.getMessage(WFSFormAdress.class, "WFSFormAdress.cmdOk.text")); // NOI18N
        cmdOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(cmdOk, gridBagConstraints);

        chkVisualize.setSelected(true);
        chkVisualize.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormAdress.class, "WFSFormAdress.chkVisualize.toolTipText")); // NOI18N
        chkVisualize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkVisualizeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 0, 0);
        add(chkVisualize, gridBagConstraints);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/markPoint.png"))); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormAdress.class, "WFSFormAdress.jLabel1.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 0, 0);
        add(jLabel1, gridBagConstraints);

        chkLockScale.setSelected(true);
        chkLockScale.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormAdress.class, "WFSFormAdress.chkLockScale.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 14, 0, 0);
        add(chkLockScale, gridBagConstraints);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png"))); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(WFSFormAdress.class, "WFSFormAdress.jLabel2.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 7, 4, 0);
        add(jLabel2, gridBagConstraints);

        panEmpty.setMinimumSize(new java.awt.Dimension(1, 1));
        panEmpty.setPreferredSize(new java.awt.Dimension(1, 1));

        org.jdesktop.layout.GroupLayout panEmptyLayout = new org.jdesktop.layout.GroupLayout(panEmpty);
        panEmpty.setLayout(panEmptyLayout);
        panEmptyLayout.setHorizontalGroup(
            panEmptyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 8, Short.MAX_VALUE)
        );
        panEmptyLayout.setVerticalGroup(
            panEmptyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 33, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 1.0;
        add(panEmpty, gridBagConstraints);

        cboNr.setEditable(true);
        cboNr.setEnabled(false);
        cboNr.setMaximumSize(new java.awt.Dimension(70, 19));
        cboNr.setMinimumSize(new java.awt.Dimension(70, 19));
        cboNr.setPreferredSize(new java.awt.Dimension(70, 19));
        cboNr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboNrActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 30.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 0);
        add(cboNr, gridBagConstraints);

        prbNr.setBorderPainted(false);
        prbNr.setMaximumSize(new java.awt.Dimension(100, 5));
        prbNr.setMinimumSize(new java.awt.Dimension(100, 5));
        prbNr.setPreferredSize(new java.awt.Dimension(100, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 10);
        add(prbNr, gridBagConstraints);

        cboStreets.setEnabled(false);
        cboStreets.setMaximumSize(new java.awt.Dimension(200, 19));
        cboStreets.setMinimumSize(new java.awt.Dimension(200, 19));
        cboStreets.setPreferredSize(new java.awt.Dimension(200, 19));
        cboStreets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboStreetsActionPerformed(evt);
            }
        });
        cboStreets.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cboStreetsKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 60.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(cboStreets, gridBagConstraints);

        prbStreets.setBorderPainted(false);
        prbStreets.setMaximumSize(new java.awt.Dimension(100, 5));
        prbStreets.setMinimumSize(new java.awt.Dimension(100, 5));
        prbStreets.setPreferredSize(new java.awt.Dimension(100, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
        add(prbStreets, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void chkVisualizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkVisualizeActionPerformed
        if (mappingComponent == null) {
            mappingComponent = CismapBroker.getInstance().getMappingComponent();
        }

        if (!mappingComponent.getHighlightingLayer().getChildrenReference().contains(pMark)) {
            mappingComponent.getHighlightingLayer().addChild(pMark);
        }
        if (nr != null) {
            visualizePosition(nr, chkVisualize.isSelected());
        } else if (strasse != null) {
            visualizePosition(strasse, chkVisualize.isSelected());
        }
    }//GEN-LAST:event_chkVisualizeActionPerformed


    private void cboStreetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboStreetsActionPerformed
        log.debug("cboStreetsActionPerformed()");//NOI18N
        if (cboStreets.getSelectedItem() instanceof WFSFormFeature) {
            strasse = (WFSFormFeature) cboStreets.getSelectedItem();
            nr = null;
            requestRefresh("cboNumbersOfAStreet", (WFSFormFeature) cboStreets.getSelectedItem());//NOI18N
            fireWfsFormAddressStreetSelected();
        }
    }//GEN-LAST:event_cboStreetsActionPerformed

    private void cboStreetsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cboStreetsKeyTyped
    }//GEN-LAST:event_cboStreetsKeyTyped

    private void cmdOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOkActionPerformed
        boolean history = true;
        MappingComponent mc = getMappingComponent();
        if (mc == null) {
            mc = CismapBroker.getInstance().getMappingComponent();
        }
        boolean scaling = !(mc.isFixedMapScale()) && !(chkLockScale.isSelected());
        BoundingBox bb = null;
        int animation = mc.getAnimationDuration();
        if (nr != null) {
            if (scaling) {
                bb = new BoundingBox(nr.getJTSGeometry());
            } else {
                bb = new BoundingBox(nr.getPosition().buffer(AbstractWFSForm.FEATURE_BORDER));
            }
        }
        else if (strasse != null) {
            if (scaling) {
                bb = new BoundingBox(strasse.getJTSGeometry());
            } else {
                bb = new BoundingBox(strasse.getPosition().buffer(AbstractWFSForm.FEATURE_BORDER));
            }
        } else {
            return;
        }
        mc.gotoBoundingBox(bb, history, scaling, animation);
        chkVisualizeActionPerformed(null);
        fireWfsFormAddressPositioned(bb);
    }//GEN-LAST:event_cmdOkActionPerformed


    public void garbageDuringAutoCompletion(JComboBox box){
        nr=null;
    }

    private void cboNrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboNrActionPerformed
        log.debug("cboNrActionPerformed()");//NOI18N
        if (cboNr.getSelectedItem() instanceof WFSFormFeature) {
            nr = (WFSFormFeature) cboNr.getSelectedItem();
            fireWfsFormAddressNrSelected();
        }
    }//GEN-LAST:event_cboNrActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboNr;
    private javax.swing.JComboBox cboStreets;
    protected javax.swing.JCheckBox chkLockScale;
    protected javax.swing.JCheckBox chkVisualize;
    protected javax.swing.JButton cmdOk;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel3;
    protected javax.swing.JPanel panEmpty;
    private javax.swing.JProgressBar prbNr;
    private javax.swing.JProgressBar prbStreets;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) throws Exception {

        try {
            //javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
            //javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            //javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
            javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            //javax.swing.UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
            // UIManager.setLookAndFeel(new PlasticLookAndFeel());
            //javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        WFSFormQuery allStreets = new WFSFormQuery();
        allStreets.setComponentName("cboAllStreets");//NOI18N
        allStreets.setServerUrl("http://s103bdc-a1/deegreewfs/wfs");//NOI18N
        allStreets.setDisplayTextProperty("geographicIdentifier");//NOI18N
        allStreets.setExtentProperty("geographicExtent");//NOI18N
        allStreets.setFilename("/request_all_streets.xml");//NOI18N
        allStreets.setId("all_streets");//NOI18N
        allStreets.setIdProperty("identifier");//NOI18N
        allStreets.setTitle("Strassen");//NOI18N
        allStreets.setType(WFSFormQuery.INITIAL);
        allStreets.setWfsQueryString(readFileAsString(new File("C:\\request_alle_strassen_extent.xml")));//NOI18N

        WFSFormQuery numbers = new WFSFormQuery();
        numbers.setComponentName("cboNumbersOfAStreet");//NOI18N
        numbers.setServerUrl("http://s103bdc-a1/deegreewfs/wfs");//NOI18N
        numbers.setDisplayTextProperty("geographicIdentifier");//NOI18N
        numbers.setExtentProperty("geographicExtent");//NOI18N
        numbers.setFilename("/request_all_numbers.xml");//NOI18N
        numbers.setId("numbers");//NOI18N
        numbers.setIdProperty("identifier");//NOI18N
        numbers.setTitle("Nr");//NOI18N
        numbers.setType(WFSFormQuery.FOLLOWUP);
        numbers.setQueryPlaceholder("@@strasse_id@@");//NOI18N
        numbers.setWfsQueryString(readFileAsString(new File("C:\\request_hausnummern_from_strasse_extent.xml")));//NOI18N
        Vector<WFSFormQuery> v = new Vector<WFSFormQuery>();
        v.add(allStreets);
        v.add(numbers);

        final WFSFormAdress tester = new WFSFormAdress();
        tester.setQueries(v);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().setLayout(new BorderLayout());
                f.getContentPane().add(tester, BorderLayout.CENTER);
                f.setVisible(true);
                f.setSize(365, 65);
            }
        });

    }

    private void fireWfsFormAddressStreetSelected() {
        for (WFSFormAdressListener curListener : listeners) {
            curListener.wfsFormAdressStreetSelected();
        }
    }

    private void fireWfsFormAddressNrSelected() {
        for (WFSFormAdressListener curListener : listeners) {
            curListener.wfsFormAdressNrSelected();
        }
    }

    private void fireWfsFormAddressPositioned(BoundingBox addressBB) {
        for (WFSFormAdressListener curListener : listeners) {
            curListener.wfsFormAddressPositioned(addressBB);
        }
    }

    public void addWFSFormAddressListner(WFSFormAdressListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeWFSFormAddressListner(WFSFormAdressListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private static String readFileAsString(File file) throws java.io.IOException {
        Log4JQuickConfig.configure4LumbermillOnLocalhost();
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(file));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
