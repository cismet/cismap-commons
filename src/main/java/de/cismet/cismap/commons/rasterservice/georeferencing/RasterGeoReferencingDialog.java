/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;

import com.vividsolutions.jts.geom.Coordinate;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.Rectangle;

import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RasterGeoReferencingDialog.class);

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel panButtons;
    private de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingPanel rasterGeoReferencingPanel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form RasterGeoReferencingDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private RasterGeoReferencingDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        getRootPane().setDefaultButton(jButton4);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        final java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        rasterGeoReferencingPanel1 =
            new de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingPanel();
        panButtons = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setTitle(org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingDialog.class,
                "RasterGeoReferencingDialog.title")); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        buttonGroup1.add(jToggleButton3);
        jToggleButton3.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/georef.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jToggleButton3,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingDialog.class,
                "RasterGeoReferencingDialog.jToggleButton3.text"));                       // NOI18N
        jToggleButton3.setFocusable(false);
        jToggleButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton3);

        buttonGroup1.add(jToggleButton1);
        jToggleButton1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/zoom.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jToggleButton1,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingDialog.class,
                "RasterGeoReferencingDialog.jToggleButton1.text"));                     // NOI18N
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton1);

        buttonGroup1.add(jToggleButton2);
        jToggleButton2.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/pan.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jToggleButton2,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingDialog.class,
                "RasterGeoReferencingDialog.jToggleButton2.text"));                    // NOI18N
        jToggleButton2.setFocusable(false);
        jToggleButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jToggleButton2);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(rasterGeoReferencingPanel1, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        panButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));

        org.openide.awt.Mnemonics.setLocalizedText(
            jButton4,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingDialog.class,
                "RasterGeoReferencingDialog.jButton4.text")); // NOI18N
        panButtons.add(jButton4);

        org.openide.awt.Mnemonics.setLocalizedText(
            jButton5,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingDialog.class,
                "RasterGeoReferencingDialog.jButton5.text")); // NOI18N
        panButtons.add(jButton5);

        getContentPane().add(panButtons, java.awt.BorderLayout.SOUTH);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RasterGeoReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RasterGeoReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RasterGeoReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RasterGeoReferencingDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        Log4JQuickConfig.configure4LumbermillOnLocalhost();
        try {
            javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(final Thread thread, final Throwable error) {
                    LOG.error("uncaught exception in thread: " + thread, error);
                }
            });

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final RasterGeoReferencingDialog dialog = new RasterGeoReferencingDialog(
                            new javax.swing.JFrame(),
                            true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);

                                final ImageFileMetaData metaData = new ImageFileMetaData(
                                        new Rectangle(0, 0, 900, 600),
                                        null,
                                        null);
                                final RasterGeoReferencingHandler handler = new RasterGeoReferencingHandler(metaData);

                                RasterGeoReferencingWizard.getInstance().setHandler(handler);

                                Thread.sleep(1000);
                                handler.addPair(new Point(294, 674), new Coordinate(374492.74, 5681564.39));
                                Thread.sleep(200);
                                handler.addPair(new Point(15, 23), new Coordinate(374358.41, 5681716.48));
                                Thread.sleep(200);
                                handler.addPair(new Point(286, 108), new Coordinate(374439.75, 5681717.62));
                                Thread.sleep(200);
                                handler.addPair(new Point(159, 409), new Coordinate(374432.30, 5681624.54));
                                Thread.sleep(200);
                                handler.addPair(new Point(199, 94), new Coordinate(374414.83, 5681713.61));
                                Thread.sleep(200);
                                handler.addPair(new Point(17, 680), new Coordinate(374417.98, 5681538.04));
                            } catch (final InterruptedException ex) {
                                LOG.error(ex, ex);
                            }
                        }
                    }.start();
                    dialog.setVisible(true);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RasterGeoReferencingDialog getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final RasterGeoReferencingDialog INSTANCE = new RasterGeoReferencingDialog(
                StaticSwingTools.getFirstParentFrame(CismapBroker.getInstance().getMappingComponent()),
                false);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
