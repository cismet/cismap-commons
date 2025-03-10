/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import java.awt.dnd.DnDConstants;

import java.io.InputStream;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.configuration.ConfigurationManager;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class Tester extends javax.swing.JFrame {

    //~ Instance fields --------------------------------------------------------

    LayerWidget layerWidget;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    // added by therter to fix some errors.
    private MappingComponent mapC = new MappingComponent();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panCaps;
    private javax.swing.JPanel panLayerWidget;
    private javax.swing.JPanel panMap;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form Tester.
     */
    public Tester() {
        try {
            try(final InputStream configStream = ClassLoader.getSystemResourceAsStream(
                                "de/cismet/cismap/commons/demo/log4j.xml")) {
                final ConfigurationSource source = new ConfigurationSource(configStream);
                final LoggerContext context = (LoggerContext)LogManager.getContext(false);
                context.start(new XmlConfiguration(context, source)); // Apply new configuration
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
            // javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            // javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
            // javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            javax.swing.UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
            // UIManager.setLookAndFeel(new PlasticLookAndFeel());
            // javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (Exception e) {
            log.warn("Error while configuring the Look&Feel!", e); // NOI18N
        }
        initComponents();
        final CapabilityWidget capWidget = new CapabilityWidget();
        panCaps.add(capWidget);
        layerWidget = new LayerWidget(mapC);
        panLayerWidget.add(layerWidget);
        synchronized (getTreeLock()) {
            validateTree();
        }
        CismapBroker.getInstance().setMappingComponent(mapC);
        mapC.setMappingModel(layerWidget.getMappingModel());
        final ConfigurationManager cm = new ConfigurationManager();
        cm.addConfigurable(capWidget);
        cm.addConfigurable(layerWidget);
        cm.addConfigurable(mapC);
        cm.setFileName("configuration.xml");                       // NOI18N
        cm.setFolder(".cismap");                                   // NOI18N
        cm.configure();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panLayerWidget = new javax.swing.JPanel();
        panCaps = new javax.swing.JPanel();
        panMap = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        panLayerWidget.setMinimumSize(new java.awt.Dimension(0, 200));
        panLayerWidget.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(panLayerWidget, gridBagConstraints);

        panCaps.setBackground(new java.awt.Color(153, 255, 153));
        panCaps.setMinimumSize(new java.awt.Dimension(200, 100));
        panCaps.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(panCaps, gridBagConstraints);

        panMap.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(panMap, gridBagConstraints);

        jButton2.setText(org.openide.util.NbBundle.getMessage(Tester.class, "Tester.jButton2.text")); // NOI18N
        getContentPane().add(jButton2, new java.awt.GridBagConstraints());

        jButton1.setText(org.openide.util.NbBundle.getMessage(Tester.class, "Tester.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
        jPanel1.add(jButton1);

        jButton3.setText(org.openide.util.NbBundle.getMessage(Tester.class, "Tester.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton3ActionPerformed(evt);
                }
            });
        jPanel1.add(jButton3);

        jButton4.setText(org.openide.util.NbBundle.getMessage(Tester.class, "Tester.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton4ActionPerformed(evt);
                }
            });
        jPanel1.add(jButton4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 910) / 2, (screenSize.height - 606) / 2, 910, 606);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton4ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton4ActionPerformed
        mapC.getRasterServiceLayer().removeAllChildren();                        // TODO add your handling code here:
    }                                                                            //GEN-LAST:event_jButton4ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton3ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton3ActionPerformed
        synchronized (getTreeLock()) {
            validateTree();
        }
        mapC.showInternalLayerWidget(!mapC.isInternalLayerWidgetVisible(), 500);
    }                                                                            //GEN-LAST:event_jButton3ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        mapC.setMappingModel(layerWidget.getMappingModel());
    }                                                                            //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new Tester().setVisible(true);
                }
            });
    }
}
