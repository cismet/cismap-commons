/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleMappingClient.java
 *
 * Created on 23. Juni 2005, 10:12
 */
package de.cismet.cismap.commons.demo;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.event.PNotification;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import org.postgis.PGgeometry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ResourceBundle;
import java.util.Vector;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
import de.cismet.cismap.commons.preferences.CismapPreferences;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleMappingClient extends javax.swing.JFrame implements RetrievalListener {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JComboBox cboMode;
    private javax.swing.JButton cmdBack;
    private javax.swing.JButton cmdFwd;
    private javax.swing.JButton cmdShowFeatureCollection;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCoord;
    private de.cismet.cismap.commons.gui.MappingComponent mapC;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel panStatus;
    private javax.swing.JPanel panToolbar;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JTextField txtKZ;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form SimpleMappingClient.
     */
    public SimpleMappingClient() {
        try {
            org.apache.log4j.PropertyConfigurator.configure(ClassLoader.getSystemResource(
                    "de/cismet/cismap/commons/demo/log4j.properties")); // NOI18N
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Simple Mapping Client started");                      // NOI18N
        // ClearLookManager.setMode(ClearLookMode.ON);
        // PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
        try {
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
            javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            // javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel()); javax.swing.UIManager.setLookAndFeel(new
            // com.jgoodies.plaf.plastic.PlasticXPLookAndFeel()); UIManager.setLookAndFeel(new
            // com.sun.java.swing.plaf.windows.WindowsLookAndFeel()); UIManager.setLookAndFeel(new
            // PlasticLookAndFeel());
        } catch (Exception e) {
            log.warn("Error during the configuration of the Look&Feel!", e); // NOI18N
        }

        initComponents();

        final CismapPreferences cismapPrefs = new CismapPreferences(getClass().getResource("/cismapPreferences.xml")); // NOI18N

        validateTree();

        mapC.setPreferences(cismapPrefs);

        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "coordinatesChanged", // NOI18N
                    SimpleMoveListener.COORDINATES_CHANGED,
                    mapC.getInputListener(MappingComponent.MOTION));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  notification  DOCUMENT ME!
     */
    public void coordinatesChanged(final PNotification notification) {
        final Object o = notification.getObject();
        if (o instanceof SimpleMoveListener) {
            final double x = ((SimpleMoveListener)o).getXCoord();
            final double y = ((SimpleMoveListener)o).getYCoord();
            lblCoord.setText(MappingComponent.getCoordinateString(x, y));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panToolbar = new javax.swing.JPanel();
        panStatus = new javax.swing.JPanel();
        cboMode = new javax.swing.JComboBox();
        cmdShowFeatureCollection = new javax.swing.JButton();
        txtKZ = new javax.swing.JTextField();
        lblCoord = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        cmdBack = new javax.swing.JButton();
        cmdFwd = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        mapC = new de.cismet.cismap.commons.gui.MappingComponent();
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
        getContentPane().add(panToolbar, java.awt.BorderLayout.NORTH);

        panStatus.setLayout(new java.awt.GridBagLayout());

        cboMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Zoom", "Pan" }));
        cboMode.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboModeActionPerformed(evt);
                    jComboBox1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        panStatus.add(cboMode, gridBagConstraints);

        cmdShowFeatureCollection.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.cmdShowFeatureCollection.text")); // NOI18N
        cmdShowFeatureCollection.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdShowFeatureCollectionActionPerformed(evt);
                }
            });
        panStatus.add(cmdShowFeatureCollection, new java.awt.GridBagConstraints());

        txtKZ.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.txtKZ.text")); // NOI18N
        txtKZ.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtKZActionPerformed(evt);
                }
            });
        panStatus.add(txtKZ, new java.awt.GridBagConstraints());

        lblCoord.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panStatus.add(lblCoord, gridBagConstraints);

        jButton1.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
        panStatus.add(jButton1, new java.awt.GridBagConstraints());

        cmdBack.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.cmdBack.text")); // NOI18N
        cmdBack.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdBackActionPerformed(evt);
                }
            });
        panStatus.add(cmdBack, new java.awt.GridBagConstraints());

        cmdFwd.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.cmdFwd.text")); // NOI18N
        cmdFwd.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdFwdActionPerformed(evt);
                }
            });
        panStatus.add(cmdFwd, new java.awt.GridBagConstraints());

        getContentPane().add(panStatus, java.awt.BorderLayout.SOUTH);

        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
                javax.swing.BorderFactory.createEtchedBorder()));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        mapC.setBackground(new java.awt.Color(236, 233, 216));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(mapC, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        fileMenu.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.fileMenu.text")); // NOI18N

        openMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.openMenuItem.text")); // NOI18N
        fileMenu.add(openMenuItem);

        saveMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.saveMenuItem.text")); // NOI18N
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.saveAsMenuItem.text")); // NOI18N
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.exitMenuItem.text")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    exitMenuItemActionPerformed(evt);
                }
            });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.editMenu.text")); // NOI18N

        cutMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.cutMenuItem.text")); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.copyMenuItem.text")); // NOI18N
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.pasteMenuItem.text")); // NOI18N
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.deleteMenuItem.text")); // NOI18N
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.helpMenu.text")); // NOI18N

        contentsMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.contentsMenuItem.text")); // NOI18N
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText(org.openide.util.NbBundle.getMessage(
                SimpleMappingClient.class,
                "SimpleMappingClient.aboutMenuItem.text")); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 800) / 2, (screenSize.height - 600) / 2, 800, 600);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdFwdActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdFwdActionPerformed
        final Object o = mapC.forward(true);
        if ((o != null) && (o instanceof PBounds)) {
            mapC.gotoBoundsWithoutHistory((PBounds)o);
        }
    }                                                                          //GEN-LAST:event_cmdFwdActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdBackActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdBackActionPerformed
        final Object o = mapC.back(true);
        if ((o != null) && (o instanceof PBounds)) {
            mapC.gotoBoundsWithoutHistory((PBounds)o);
        }
    }                                                                           //GEN-LAST:event_cmdBackActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        mapC.showInternalLayerWidget(!mapC.isInternalLayerWidgetVisible(), 500);
    }                                                                            //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtKZActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtKZActionPerformed
// TODO add your handling code here:
    } //GEN-LAST:event_txtKZActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdShowFeatureCollectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdShowFeatureCollectionActionPerformed
//       try {
//            Connection conn;
//            System.out.println("Creating JDBC connection...");
//            Class.forName("org.postgresql.Driver");
//            String url = "jdbc:postgresql://134.96.211.29:5432/verdis_beta";
//            conn = DriverManager.getConnection(url, "postgres","x");
//            System.out.println("Adding geometric type entries...");
//
//            ((org.postgresql.PGConnection)conn).addDataType("geometry","org.postgis.PGgeometry");
//            ((org.postgresql.PGConnection)conn).addDataType("box3d","org.postgis.PGbox3d");
//
//            Statement s = conn.createStatement();
//            System.out.println("Creating table with geometric types...");
//
//            System.out.println("Querying table...");
//            String kassenzeichen=txtKZ.getText();
////            if (kassenzeichen.trim().length()==0) kassenzeichen="6007025";
//            if (kassenzeichen.trim().length()==0) kassenzeichen="6011373";
//            ResultSet r = s.executeQuery("select kassenzeichen_reference,flaechenart,geo_field,geom.id,flaechenbezeichnung from flaechen,flaeche,flaecheninfo,geom where flaechen.flaeche=flaeche.id and flaeche.flaecheninfo=flaecheninfo.id and flaecheninfo.geometrie=geom.id and kassenzeichen_reference in ("+kassenzeichen+")");
//            //ResultSet r = s.executeQuery("select kassenzeichen_reference,flaechenart,geo_field,geom.id,flaechenbezeichnung from flaechen,flaeche,flaecheninfo,geom where flaechen.flaeche=flaeche.id and flaeche.flaecheninfo=flaecheninfo.id and flaecheninfo.geometrie=geom.id and geom.id<10000");
//            Vector v=new Vector();
//            while( r.next() )
//            {
//                FeatureExample fe=new FeatureExample();
//                fe.setName(r.getString(5));
//                fe.setArt(r.getInt(2));
//                PGgeometry postgresGeom=(PGgeometry)r.getObject(3);
//                org.postgis.Geometry postgisGeom=postgresGeom.getGeometry();
//                fe.setGeom(PostGisGeometryFactory.createJtsGeometry(postgisGeom));
//                v.add(fe);
//            }
//            Feature[] fa=new Feature[v.size()];
//            fa=(Feature[])v.toArray(fa);
//            mapC.showFeatureCollection(fa);
//            System.out.println("...ready");
//            s.close();
//            conn.close();
//        }
//        catch( Exception e ) {
//                System.out.println("Keine DB Verbindung: Beispiel Feature....");
//                e.printStackTrace();
//                Feature[] fa=new Feature[1];
//                FeatureExample f=new FeatureExample();
//                com.vividsolutions.jts.geom.Coordinate[] coordArr=new com.vividsolutions.jts.geom.Coordinate[17];
//
//                for (int i=1;i<9;++i) {
//                    coordArr[i-1]=new com.vividsolutions.jts.geom.Coordinate(i*10,i*i);
//                    System.out.println("f["+new Integer(i-1).toString()+"]:(x,y):("+i+","+i*i+")");
//                    coordArr[16-i]=new com.vividsolutions.jts.geom.Coordinate(i*10,i*i+10);
//                 }
//                coordArr[16]=new com.vividsolutions.jts.geom.Coordinate(10,1);
//                com.vividsolutions.jts.geom.LinearRing lr=new com.vividsolutions.jts.geom.LinearRing(com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory.instance().create(coordArr), new com.vividsolutions.jts.geom.GeometryFactory());
//                com.vividsolutions.jts.geom.Polygon p=new com.vividsolutions.jts.geom.Polygon(lr,null,new com.vividsolutions.jts.geom.GeometryFactory());
//                f.setGeom(p);
//                f.setName("Test");
//                f.setArt(-5);
//                fa[0]=f;
//                mapC.showFeatureCollection(fa);
//        }

    } //GEN-LAST:event_cmdShowFeatureCollectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jComboBox1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jComboBox1ActionPerformed
// TODO add your handling code here:
    } //GEN-LAST:event_jComboBox1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboModeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboModeActionPerformed
        if (cboMode.getSelectedItem().equals("Zoom")) {                         // NOI18N
            mapC.setInteractionMode("ZOOM");                                    // NOI18N
        } else if (cboMode.getSelectedItem().equals("Pan")) {                   // NOI18N
            mapC.setInteractionMode("PAN");                                     // NOI18N
        }
    }                                                                           //GEN-LAST:event_cboModeActionPerformed

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
                    new SimpleMappingClient().setVisible(true);
                }
            });
    }

    @Override
    public void retrievalStarted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }

    @Override
    public void retrievalProgress(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }

    @Override
    public void retrievalError(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        log.error("Retrieve error message\n" + e.getRetrievedObject()); // NOI18N
    }

    @Override
    public void retrievalComplete(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }

    @Override
    public void retrievalAborted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }
}
