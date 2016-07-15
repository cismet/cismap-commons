/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * PrintingSettingsWidget.java
 *
 * Created on 10. Juli 2006, 14:06
 */
package de.cismet.cismap.commons.gui.printing;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintTemplateFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.configuration.Configurable;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PrintingSettingsWidget extends javax.swing.JDialog implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    public static final double FEATURE_RESOLUTION_FACTOR = 125.0d;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Element configuration = null;
    private Vector<Scale> scales = new Vector<Scale>();
    private Vector<Resolution> resolutions = new Vector<Resolution>();
    private Vector<Template> templates = new Vector<Template>();
    private Vector<Action> actions = new Vector<Action>();
    private MappingComponent mappingComponent = null;
    private boolean chooseFileName = false;
    private boolean oldOverlappingCheck = true;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboAction;
    private javax.swing.JComboBox cboResolution;
    private javax.swing.JComboBox cboScales;
    private javax.swing.JComboBox cboTemplates;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPanel panDesc;
    private javax.swing.JPanel panSettings;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form PrintingSettingsWidget.
     *
     * @param  modal             DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PrintingSettingsWidget(final boolean modal, final MappingComponent mappingComponent) {
        super(StaticSwingTools.getParentFrame(mappingComponent), modal);
        initComponents();
        getRootPane().setDefaultButton(cmdOk);
        this.mappingComponent = mappingComponent;
        oldOverlappingCheck = CismapBroker.getInstance().isCheckForOverlappingGeometriesAfterFeatureRotation();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   modal             DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PrintingSettingsWidget cloneWithNewParent(final boolean modal, final MappingComponent mappingComponent) {
        final PrintingSettingsWidget newWidget = new PrintingSettingsWidget(modal, mappingComponent);
        newWidget.configuration = configuration;
        newWidget.scales = scales;
        newWidget.resolutions = resolutions;
        newWidget.templates = templates;
        newWidget.actions = actions;
        newWidget.cboScales.setModel(cboScales.getModel());
        newWidget.cboResolution.setModel(cboResolution.getModel());
        newWidget.cboTemplates.setModel(cboTemplates.getModel());
        newWidget.cboAction.setModel(new DefaultComboBoxModel(actions));
        newWidget.cboScales.setSelectedItem(cboScales.getSelectedItem());
        newWidget.cboResolution.setSelectedItem(cboResolution.getSelectedItem());
        newWidget.cboTemplates.setSelectedItem(cboTemplates.getSelectedItem());
        newWidget.cboAction.setSelectedItem(cboAction.getSelectedItem());
        return newWidget;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        panDesc = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        cmdOk = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();
        panSettings = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cboTemplates = new javax.swing.JComboBox();
        cboScales = new javax.swing.JComboBox();
        cboResolution = new javax.swing.JComboBox();
        cboAction = new javax.swing.JComboBox();
        jSeparator4 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(PrintingSettingsWidget.class, "PrintingSettingsWidget.title")); // NOI18N

        panDesc.setBackground(new java.awt.Color(216, 228, 248));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel4.text")); // NOI18N

        jLabel5.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/frameprint.png"))); // NOI18N

        final org.jdesktop.layout.GroupLayout panDescLayout = new org.jdesktop.layout.GroupLayout(panDesc);
        panDesc.setLayout(panDescLayout);
        panDescLayout.setHorizontalGroup(
            panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                jSeparator3,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                254,
                Short.MAX_VALUE).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                panDescLayout.createSequentialGroup().addContainerGap(116, Short.MAX_VALUE).add(jLabel5)
                            .addContainerGap()).add(
                panDescLayout.createSequentialGroup().addContainerGap().add(
                    panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        panDescLayout.createSequentialGroup().add(
                            jSeparator2,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            234,
                            Short.MAX_VALUE).addContainerGap()).add(
                        panDescLayout.createSequentialGroup().add(
                            panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                jLabel1).add(jLabel2).add(jLabel3).add(jLabel4)).add(83, 83, 83)))));
        panDescLayout.setVerticalGroup(
            panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                panDescLayout.createSequentialGroup().addContainerGap().add(jLabel1).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator2,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    2,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel2).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel3).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel4).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    26,
                    Short.MAX_VALUE).add(jLabel5).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator3,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        cmdOk.setMnemonic('O');
        cmdOk.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.cmdOk.text")); // NOI18N
        cmdOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOkActionPerformed(evt);
                }
            });

        cmdCancel.setMnemonic('A');
        cmdCancel.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.cmdCancel.text")); // NOI18N
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCancelActionPerformed(evt);
                }
            });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel6.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel6.text")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel7.text")); // NOI18N

        jLabel8.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel8.text")); // NOI18N

        jLabel9.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel9.text")); // NOI18N

        jLabel10.setText(org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel10.text")); // NOI18N

        cboTemplates.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cboScales.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboScales.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboScalesActionPerformed(evt);
                }
            });

        cboResolution.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cboAction.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        final org.jdesktop.layout.GroupLayout panSettingsLayout = new org.jdesktop.layout.GroupLayout(panSettings);
        panSettings.setLayout(panSettingsLayout);
        panSettingsLayout.setHorizontalGroup(
            panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                panSettingsLayout.createSequentialGroup().addContainerGap().add(
                    panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        panSettingsLayout.createSequentialGroup().add(jLabel6).add(14, 14, 14)).add(
                        panSettingsLayout.createSequentialGroup().add(
                            panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                jLabel7).add(jLabel8).add(jLabel9).add(jLabel10)).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                cboAction,
                                0,
                                302,
                                Short.MAX_VALUE).add(cboResolution, 0, 302, Short.MAX_VALUE).add(
                                cboScales,
                                0,
                                302,
                                Short.MAX_VALUE).add(cboTemplates, 0, 302, Short.MAX_VALUE)))).addContainerGap()).add(
                jSeparator4,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                376,
                Short.MAX_VALUE).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                panSettingsLayout.createSequentialGroup().addContainerGap().add(
                    jSeparator1,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    366,
                    Short.MAX_VALUE)));
        panSettingsLayout.setVerticalGroup(
            panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                panSettingsLayout.createSequentialGroup().addContainerGap().add(jLabel6).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    10,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel7).add(
                        cboTemplates,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel8).add(
                        cboScales,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel9).add(
                        cboResolution,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel10).add(
                        cboAction,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    108,
                    Short.MAX_VALUE).add(
                    jSeparator4,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
                        layout.createSequentialGroup().addContainerGap().add(
                            cmdCancel,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            110,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            cmdOk,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            107,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(
                        org.jdesktop.layout.GroupLayout.LEADING,
                        layout.createSequentialGroup().add(
                            panDesc,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(18, 18, 18).add(
                            panSettings,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        panSettings,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        panDesc,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(cmdOk).add(cmdCancel))
                            .addContainerGap()));

        pack();
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOkActionPerformed
        try {
            final Scale selectedScale = (Scale)cboScales.getSelectedItem();
            final Resolution selectedResolution = (Resolution)cboResolution.getSelectedItem();
            mappingComponent.setPrintingResolution(selectedResolution.getResolution()
                        / mappingComponent.getFeaturePrintingDpi());
            final Template selectedTemplate = (Template)cboTemplates.getSelectedItem();
            final PrintTemplateFeature printTemplateStyledFeature = new PrintTemplateFeature(
                    selectedTemplate,
                    selectedResolution,
                    selectedScale,
                    mappingComponent);
            final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection)
                mappingComponent.getFeatureCollection();
            mapFeatureCol.holdFeature(printTemplateStyledFeature);
            mapFeatureCol.addFeature(printTemplateStyledFeature);
            mappingComponent.adjustMapForPrintingTemplates();
            mapFeatureCol.select(printTemplateStyledFeature);
            mappingComponent.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
            mappingComponent.showHandles(false);
            CismapBroker.getInstance().setCheckForOverlappingGeometriesAfterFeatureRotation(false);
            dispose();
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten der Druckeinstellungen", e);   // NOI18N
        }
    }                                                                         //GEN-LAST:event_cmdOkActionPerformed
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdCancelActionPerformed
        if (mappingComponent.getPrintFeatureCollection().size() == 0) {
            CismapBroker.getInstance().setCheckForOverlappingGeometriesAfterFeatureRotation(oldOverlappingCheck);
        }
        dispose();
    }                                                                             //GEN-LAST:event_cmdCancelActionPerformed
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboScalesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboScalesActionPerformed
    }                                                                             //GEN-LAST:event_cboScalesActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getOldOverlappingCheckEnabled() {
        return oldOverlappingCheck;
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
                    // new PrintingSettingsWidget(new javax.swing.JFrame(), true).setVisible(true);
                }
            });
    }

    @Override
    public Element getConfiguration() {
        final Element ret = new Element("printing"); // NOI18N
        for (final Scale elem : scales) {
            ret.addContent(elem.getElement(elem.equals(cboScales.getSelectedItem())));
        }
        for (final Resolution elem : resolutions) {
            ret.addContent(elem.getElement(elem.equals(cboResolution.getSelectedItem())));
        }
        for (final Template elem : templates) {
            ret.addContent(elem.getElement(elem.equals(cboTemplates.getSelectedItem())));
        }
        for (final Action elem : actions) {
            ret.addContent(elem.getElement(elem.equals(cboAction.getSelectedItem())));
        }
        return ret;
    }

    @Override
    public void masterConfigure(final Element parent) {
        try {
            final Element prefs = parent.getChild("printing");                             // NOI18N
            configuration = (Element)prefs.clone();
            final List scalesList = prefs.getChildren("scale");                            // NOI18N
            final List resolutionsList = prefs.getChildren("resolution");                  // NOI18N
            final List templatesList = prefs.getChildren("template");                      // NOI18N
            final List actionList = prefs.getChildren("action");                           // NOI18N
            Scale selectedScale = null;
            Resolution selectedResolution = null;
            Template selectedTemplate = null;
            Action selectedAction = null;
            scales.removeAllElements();
            resolutions.removeAllElements();
            templates.removeAllElements();
            actions.removeAllElements();
            try {
                for (final Object elem : scalesList) {
                    if (elem instanceof Element) {
                        final Scale s = new Scale((Element)elem);
                        scales.add(s);
                        if (((Element)elem).getAttribute("selected").getBooleanValue()) {  // NOI18N
                            selectedScale = s;
                        }
                    }
                }
                for (final Object elem : resolutionsList) {
                    if (elem instanceof Element) {
                        final Resolution r = new Resolution((Element)elem);
                        resolutions.add(r);
                        if (((Element)elem).getAttribute("selected").getBooleanValue()) {  // NOI18N
                            selectedResolution = r;
                        }
                    }
                }
                for (final Object elem : templatesList) {
                    if (elem instanceof Element) {
                        final Template t = new Template((Element)elem);
                        templates.add(t);
                        if (((Element)elem).getAttribute("selected").getBooleanValue()) {  // NOI18N
                            selectedTemplate = t;
                        }
                    }
                }
                for (final Object elem : actionList) {
                    if (elem instanceof Element) {
                        final Action a = new Action((Element)elem);
                        actions.add(a);
                        if (((Element)elem).getAttribute("selected").getBooleanValue()) {  // NOI18N
                            selectedAction = a;
                        }
                    }
                }
                cboScales.setModel(new DefaultComboBoxModel(scales));
                cboResolution.setModel(new DefaultComboBoxModel(resolutions));
                cboTemplates.setModel(new DefaultComboBoxModel(templates));
                cboAction.setModel(new DefaultComboBoxModel(actions));
                cboScales.setSelectedItem(selectedScale);
                cboResolution.setSelectedItem(selectedResolution);
                cboTemplates.setSelectedItem(selectedTemplate);
                cboAction.setSelectedItem(selectedAction);
            } catch (Exception e) {
                log.error("Error during initialization of the printingDialog", e);         // NOI18N
            }
        } catch (Exception ex) {
            log.error("Error during initialization of the PrintingWidgets. catched.", ex); // NOI18N
        }
    }

    @Override
    public void configure(final Element parent) {
        if (parent != null) {
            final Element prefs = parent.getChild("printing");                // NOI18N
            if (prefs != null) {
                configuration = (Element)prefs.clone();
                final List scalesList = prefs.getChildren("scale");           // NOI18N
                final List resolutionsList = prefs.getChildren("resolution"); // NOI18N
                final List templatesList = prefs.getChildren("template");     // NOI18N
                final List actionList = prefs.getChildren("action");          // NOI18N
                Scale selectedScale = null;
                Resolution selectedResolution = null;
                Template selectedTemplate = null;
                Action selectedAction = null;
                // scales.removeAllElements();
                // resolutions.removeAllElements();
                // templates.removeAllElements();
                // actions.removeAllElements();
                try {
                    for (final Object elem : scalesList) {
                        if (elem instanceof Element) {
                            final Scale s = new Scale((Element)elem);
                            // scales.add(s);
                            if (((Element)elem).getAttribute("selected").getBooleanValue()) { // NOI18N
                                selectedScale = s;
                            }
                        }
                    }
                    for (final Object elem : resolutionsList) {
                        if (elem instanceof Element) {
                            final Resolution r = new Resolution((Element)elem);
                            // resolutions.add(r);
                            if (((Element)elem).getAttribute("selected").getBooleanValue()) { // NOI18N
                                selectedResolution = r;
                            }
                        }
                    }
                    for (final Object elem : templatesList) {
                        if (elem instanceof Element) {
                            final Template t = new Template((Element)elem);
                            // templates.add(t);
                            if (((Element)elem).getAttribute("selected").getBooleanValue()) { // NOI18N
                                selectedTemplate = t;
                            }
                        }
                    }
                    for (final Object elem : actionList) {
                        if (elem instanceof Element) {
                            final Action a = new Action((Element)elem);
                            // actions.add(a);
                            if (((Element)elem).getAttribute("selected").getBooleanValue()) { // NOI18N
                                selectedAction = a;
                            }
                        }
                    }
                    if (scales.contains(selectedScale)) {
                        cboScales.setSelectedItem(selectedScale);
                    }
                    if (resolutions.contains(selectedResolution)) {
                        cboResolution.setSelectedItem(selectedResolution);
                    }
                    if (templates.contains(selectedTemplate)) {
                        cboTemplates.setSelectedItem(selectedTemplate);
                    }
                    if (actions.contains(selectedAction)) {
                        cboAction.setSelectedItem(selectedAction);
                    }
                } catch (Exception e) {
                    log.error("Error during initialization of the printingDialog", e);        // NOI18N
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Scale getSelectedScale() {
        return (Scale)cboScales.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Template getSelectedTemplate() {
        return (Template)cboTemplates.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Resolution getSelectedResolution() {
        return (Resolution)cboResolution.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Action getSelectedAction() {
        return (Action)cboAction.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  chooseFileName  DOCUMENT ME!
     */
    public void setChooseFileName(final boolean chooseFileName) {
        this.chooseFileName = chooseFileName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isChooseFileName() {
        return chooseFileName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Scale> getScales() {
        return new ArrayList<Scale>(scales);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Template> getTemplates() {
        return new ArrayList<Template>(templates);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Resolution> getResolutions() {
        return new ArrayList<Resolution>(resolutions);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTemplateString() {
        return org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel7.text");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getScaleString() {
        return org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel8.text");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getResolutionString() {
        return org.openide.util.NbBundle.getMessage(
                PrintingSettingsWidget.class,
                "PrintingSettingsWidget.jLabel9.text");
    }
}
