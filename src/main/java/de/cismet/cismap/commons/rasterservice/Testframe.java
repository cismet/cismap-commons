/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.awt.BorderLayout;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class Testframe extends javax.swing.JFrame {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form Testframe.
     */
    public Testframe() {
        initComponents();

        initMap();
    }
    
    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initMap() {
        CismapBroker.getInstance().setMappingComponent(new MappingComponent(true));
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 32633);
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(336220.125, 4645346);
        coordinates[1] = new Coordinate(336220.125, 4724596.5);
        coordinates[2] = new Coordinate(406720.15625, 4724596.5);
        coordinates[3] = new Coordinate(406720.15625, 4645346);
        coordinates[4] = new Coordinate(336220.125, 4645346);
        final LinearRing lr = gf.createLinearRing(coordinates);
        final Geometry gt = gf.createPolygon(lr, null);
        final XBoundingBox bbox = new XBoundingBox(gt);
        final ActiveLayerModel mappingModel = new ActiveLayerModel();
        mappingModel.setSrs(new Crs("EPSG:32633", "EPSG:32633", "EPSG:32633", true, true));
        mappingModel.addHome(bbox);

        final SimpleWMS layer = new SimpleWMS(new SimpleWmsGetMapUrl(
                    "http://crisma.cismet.de/geoserver/crisma/wms?service=WMS&version=1.1.0&request=GetMap&layers="
                            + "shakemap2"
                            + "&bbox=<cismap:boundingBox>&width=<cismap:width>&height=<cismap:height>&srs=EPSG:32633&format=image/png&transparent=true"));
        final SimpleWMS ortho = new SimpleWMS(new SimpleWmsGetMapUrl(
                    "http://geoportale2.regione.abruzzo.it/erdas-iws/ogc/wms/?&VERSION=1.1.1&REQUEST=GetMap&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS=EPSG:32633&FORMAT=image/png&EXCEPTIONS=application/vnd.ogc.se_xml&transparent=true&LAYERS="
                            + "IMAGES_ORTO_AQUILA_2010.ECW"));
        
        layer.setCallback(new ImageCallback() {

            @Override
            public void call(final Image image)
            {
                final Runnable r = new Runnable() {

                    @Override
                    public void run()
                    {
                final ImagePanel p = new ImagePanel(image);
                        try
                        {
                            jPanel2.remove(0);
                        }catch(Exception e)
                        {
                        }
                jPanel2.add(p, 0);
                jPanel2.invalidate();
                jPanel2.validate();
                    }
                };
                
                if(EventQueue.isDispatchThread()){
                    r.run();
                } else {
                    EventQueue.invokeLater(r);
                }
            }
        });
        ortho.setCallback(new ImageCallback() {

            @Override
            public void call(final Image image)
            {
                final Runnable r = new Runnable() {

                    @Override
                    public void run()
                    {
                final ImagePanel p = new ImagePanel(image);
                        try
                        {
                            jPanel2.remove(1);
                        }catch(Exception e)
                        {
                        }
                jPanel2.add(p, 1);
                jPanel2.invalidate();
                jPanel2.validate();
                    }
                };
                
                if(EventQueue.isDispatchThread()){
                    r.run();
                } else {
                    EventQueue.invokeLater(r);
                }
            }
        });
        mappingModel.addLayer(ortho);
        layer.setTranslucency(0.7f);
        mappingModel.addLayer(layer);

        final MappingComponent mappingComponent1 = new MappingComponent(false);
        mappingComponent1.setMappingModel(mappingModel);
        mappingComponent1.setInteractionMode("PAN");
        mappingComponent1.unlock();
        mappingComponent1.gotoInitialBoundingBox();

        jPanel1.add(mappingComponent1, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1024, 768));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridLayout(1, 0, 5, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        Log4JQuickConfig.configure4LumbermillOnLocalhost();
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
            java.util.logging.Logger.getLogger(Testframe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Testframe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Testframe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Testframe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new Testframe().setVisible(true);
                }
            });
    }
    
    public interface ImageCallback {
        void call(Image image);
    }
    
    public static final class ImagePanel extends JPanel{

        private final Image image;

        public ImagePanel(Image image)
        {
            this.image = image;
            this.setSize(image.getWidth(this), image.getHeight(this));
        }
        
        @Override
        public void paint(Graphics g)
        {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g.dispose();
        }
        
    }
}
