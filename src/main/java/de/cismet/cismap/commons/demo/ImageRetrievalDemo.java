/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.demo;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import java.awt.Image;

import java.util.Locale;

import javax.swing.UIManager;

import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import de.cismet.cismap.commons.rasterservice.HTTPImageRetrieval;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ImageRetrievalDemo extends javax.swing.JFrame implements RetrievalListener {

    //~ Instance fields --------------------------------------------------------

    HTTPImageRetrieval ir;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel panMain;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JProgressBar prBar;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form Test.
     */
    public ImageRetrievalDemo() {
        Log4JQuickConfig.configure4LumbermillOnLocalhost();
        log.info("Simple Mapping Client started"); // NOI18N
        // ClearLookManager.setMode(ClearLookMode.ON);
        // PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
        try {
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
            // javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            // javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
            // javax.swing.UIManager.setLookAndFeel(new com.jgoodies.plaf.plastic.PlasticXPLookAndFeel());
            // UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
            // UIManager.setLookAndFeel(new PlasticLookAndFeel());
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        } catch (Exception e) {
            log.warn("Fehler beim Einstellen des Look&Feels's!", e); // NOI18N
        }

        initComponents();

        prBar.setMaximum(100);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        prBar = new javax.swing.JProgressBar();
        panMain = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(prBar, java.awt.BorderLayout.SOUTH);

        panMain.setMinimumSize(new java.awt.Dimension(100, 100));
        getContentPane().add(panMain, java.awt.BorderLayout.CENTER);

        jButton1.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
        getContentPane().add(jButton1, java.awt.BorderLayout.NORTH);

        fileMenu.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.fileMenu.text")); // NOI18N

        openMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.openMenuItem.text")); // NOI18N
        fileMenu.add(openMenuItem);

        saveMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.saveMenuItem.text")); // NOI18N
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.saveAsMenuItem.text")); // NOI18N
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.exitMenuItem.text")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    exitMenuItemActionPerformed(evt);
                }
            });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.editMenu.text")); // NOI18N

        cutMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.cutMenuItem.text")); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.copyMenuItem.text")); // NOI18N
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.pasteMenuItem.text")); // NOI18N
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.deleteMenuItem.text")); // NOI18N
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.helpMenu.text")); // NOI18N

        contentsMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.contentsMenuItem.text")); // NOI18N
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText(org.openide.util.NbBundle.getMessage(
                ImageRetrievalDemo.class,
                "ImageRetrievalDemo.aboutMenuItem.text")); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 400) / 2, (screenSize.height - 300) / 2, 400, 300);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        prBar.setValue(0);
        prBar.setIndeterminate(true);

        if (ir == null) {
            ir = new HTTPImageRetrieval(this);
            ir.setUrl(
                "http://s102w2k1.wuppertal-intra.de/wunda_dk_v61/isserver/ims/scripts/ShowMap.pl?datasource=grundlkarten&VERSION=1.1.1&REQUEST=GetMap&BBOX=2581794.0773859876,5684502.5686845,2581948.756619977,5684588.15786064&WIDTH=750&HEIGHT=415&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_inimage&LAYERS=02_11&STYLES=farbig"); // NOI18N
            ir.start();
        } else {
            ir.endRetrieval();
            ir = new HTTPImageRetrieval(this);
            ir.setUrl(
                "http://s102w2k1.wuppertal-intra.de/wunda_dk_v61/isserver/ims/scripts/ShowMap.pl?datasource=grundlkarten&VERSION=1.1.1&REQUEST=GetMap&BBOX=2581794.0773859876,5684502.5686845,2581948.756619977,5684588.15786064&WIDTH=750&HEIGHT=415&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_inimage&LAYERS=02_11&STYLES=farbig"); // NOI18N
            ir.start();
        }
    }                                                                                                                                                                                                                                                                                                                                                                                           //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void exitMenuItemActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }                                                                                //GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new ImageRetrievalDemo().setVisible(true);
                }
            });
    }

    @Override
    public void retrievalStarted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }

    @Override
    public void retrievalProgress(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        final double p = e.getPercentageDone();
        System.out.println(p);
        if (true || (p > prBar.getValue())) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (prBar.isIndeterminate()) {
                            prBar.setIndeterminate(false);
                        }
                        prBar.setValue((int)p);
                        // prBar.setString(new Double(p).toString());
                    }
                });
        }
    }

    @Override
    public void retrievalError(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        log.error("Error:" + e.getRetrievedObject()); // NOI18N
    }

    @Override
    public void retrievalComplete(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        final Object o = e.getRetrievedObject();
        Image i = null;
        if (o instanceof Image) {
            i = (Image)o;
        }
        panMain.getGraphics().drawImage(i, 0, 0, this);
    }

    @Override
    public void retrievalAborted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }
}
