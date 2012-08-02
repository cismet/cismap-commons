/*
 * SimpleInternalLayerWidget.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 22. August 2005, 12:13
 *
 */
package de.cismet.cismap.commons.gui.simplelayerwidget;
//import de.cismet.cismap.commons.DefaultMappingModel;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.rasterservice.MapService;
import edu.umd.cs.piccolo.PNode;
import java.awt.GridBagConstraints;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class SimpleInternalLayerWidget extends javax.swing.JInternalFrame  implements MouseListener,LayerControlSelectionChangedListener{
    
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());private Vector<LayerControl> rasterLayerControls=new Vector<LayerControl>();
    private Vector<LayerControl> featureLayerControls=new Vector<LayerControl>();
    private LayerControl featureCollectionControl;
    private MappingComponent mappingComponent;
    
    /** Creates new form SimpleInternalLayerWidget */
    public SimpleInternalLayerWidget(MappingComponent mappingComponent) {
        initComponents();
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);//NOI18N
        this.mappingComponent=mappingComponent;
//setClosable(true);
        
    }
    
    
    
    public LayerControl showFeatureCollection(int errorAbolitionTime)    {
        LayerControl control=new LayerControl(mappingComponent,LayerControl.FEATURE_COLLECTION, errorAbolitionTime);
        control.addLayerSelectionChangedListener(this);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        panFeatureCollection.add(control, gridBagConstraints);
        featureCollectionControl=control;
        control.addMouseListener(this);
        return control;
        
    }
    
    public LayerControl addRasterService(int position,ServiceLayer layer,int errorAbolitionTime) {
        LayerControl control=new LayerControl(mappingComponent,LayerControl.RASTER_SERVICE,errorAbolitionTime);
        control.setLayer(layer);
        control.addLayerSelectionChangedListener(this);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = position;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        panRasterServices.add(control, gridBagConstraints);
        pack();
        rasterLayerControls.add(control);
        control.addMouseListener(this);
        control.syncIconWithEnabledState();
        return control;
    }
    public LayerControl addFeatureService(int position,ServiceLayer layer,int errorAbolitionTime) {
        LayerControl control=new LayerControl(mappingComponent,LayerControl.FEATURE_SERVICE,errorAbolitionTime);
        control.setLayer(layer);
        control.addLayerSelectionChangedListener(this);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = position;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        panFeatureServices.add(control, gridBagConstraints);
        pack();
        featureLayerControls.add(control);
        control.addMouseListener(this);
        control.syncIconWithEnabledState();
        return control;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panRasterServices = new javax.swing.JPanel();
        panFeatureCollection = new javax.swing.JPanel();
        panFeatureServices = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setResizable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        panRasterServices.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(panRasterServices, gridBagConstraints);

        panFeatureCollection.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(panFeatureCollection, gridBagConstraints);

        panFeatureServices.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(panFeatureServices, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jSeparator2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        
    }//GEN-LAST:event_formKeyPressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel panFeatureCollection;
    private javax.swing.JPanel panFeatureServices;
    private javax.swing.JPanel panRasterServices;
    // End of variables declaration//GEN-END:variables
    
//    public static void main(String[] args) {
//        try {
//            //ClearLookManager.setMode(ClearLookMode.DEBUG);
//            PlasticLookAndFeel.setMyCurrentTheme(new ExperienceBlue());
//            //com.jgoodies.plaf.plastic.PlasticLookAndFeel.setMyCurrentTheme(new com.jgoodies.plaf.plastic.theme.DesertBlue());
//            javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
//        } catch (Exception e) {
//        }
//       JFrame frame = new JFrame("Example");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JDesktopPane jdp=new JDesktopPane();
//        frame.setContentPane(jdp);
//        SimpleInternalLayerWidget sil= new SimpleInternalLayerWidget();
//        jdp.add(sil);
//        sil.show();
//
//        frame.setSize(300, 300);
//        frame.setVisible(true);
//
//    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(java.awt.event.MouseEvent e) {
    }
    
    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    
    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    
    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getSource() instanceof LayerControl && e.getClickCount()>1&&e.getButton()==e.BUTTON1) {
            LayerControl flashControl=(LayerControl)e.getSource();
            Vector allControls=new Vector();
            allControls.addAll(rasterLayerControls);
            allControls.addAll(featureLayerControls);
            allControls.remove(flashControl);
            flashControl.flashObject(true, 1000, 3000);
            final Iterator it=allControls.iterator();
            java.awt.event.ActionListener timerAction = new java.awt.event.ActionListener() {
                public void actionPerformed( java.awt.event.ActionEvent event ) {
                    while (it.hasNext()) {
                        LayerControl lc=(LayerControl)it.next();
                        lc.flashObject(false, 0, 4000);
                    }
                }
            };
            javax.swing.Timer timer = new javax.swing.Timer(1000, timerAction );
            timer.setRepeats(false);
            timer.start();
            
        }
    }
    
    public void layerControlSelectionChanged(LayerControl lc) {
//        boolean sel=lc.isSelected();
//        for (LayerControl lac:rasterLayerControls) {
//            lac.setSelected(false);
//        }
//        for (LayerControl lac:featureLayerControls) {
//            lac.setSelected(false);
//        }
//        lc.setSelected(sel);
        
    }
    
    public void layerWantsUp(LayerControl lc) {
//        log.debug("layerWantsUp");
//        if (rasterLayerControls.contains(lc)) {
//            if (mappingComponent.getMappingModel() instanceof DefaultMappingModel) {
//                DefaultMappingModel dmm=((DefaultMappingModel)(mappingComponent.getMappingModel()));
//                int newP=dmm.moveRasterServiceUp((RasterService)lc.getLayer());
//                PNode p=((RasterService)(dmm.getRasterServices().get(newP+1))).getPImage();
//                lc.getPNode().moveInBackOf(p);
//                
//                
//            }
//        } else if (featureLayerControls.contains(lc)) {
//            if (mappingComponent.getMappingModel() instanceof DefaultMappingModel) {
//                DefaultMappingModel dmm=((DefaultMappingModel)(mappingComponent.getMappingModel()));
//                int newP=dmm.moveFeatureServiceUp((FeatureService)lc.getLayer());
//                PNode p=((FeatureService)(dmm.getFeatureServices().get(newP+1))).getPNode();
//                lc.getPNode().moveInBackOf(p);
//                
//            }
//               
//        }
        
    }
    
    public void layerWantsDown(LayerControl lc) {
//        log.debug("layerWantsDown");
//        if (rasterLayerControls.contains(lc)) {
//            if (mappingComponent.getMappingModel() instanceof DefaultMappingModel) {
//                ((DefaultMappingModel)(mappingComponent.getMappingModel())).moveRasterServiceDown((RasterService)lc.getLayer());
//            }
//        } else if (featureLayerControls.contains(lc)) {
//            if (mappingComponent.getMappingModel() instanceof DefaultMappingModel) {
//                ((DefaultMappingModel)(mappingComponent.getMappingModel())).moveFeatureServiceDown((FeatureService)lc.getLayer());
//            }
//        }
    }
    
    
    
}
