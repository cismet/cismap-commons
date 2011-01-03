/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.simplelayerwidget;
//import de.cismet.cismap.commons.DefaultMappingModel;
import edu.umd.cs.piccolo.PNode;

import java.awt.GridBagConstraints;
import java.awt.event.MouseListener;

import java.util.Iterator;
import java.util.Vector;

import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleInternalLayerWidget extends javax.swing.JInternalFrame implements MouseListener,
    LayerControlSelectionChangedListener {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Vector<LayerControl> rasterLayerControls = new Vector<LayerControl>();
    private Vector<LayerControl> featureLayerControls = new Vector<LayerControl>();
    private LayerControl featureCollectionControl;
    private MappingComponent mappingComponent;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel panFeatureCollection;
    private javax.swing.JPanel panFeatureServices;
    private javax.swing.JPanel panRasterServices;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form SimpleInternalLayerWidget.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public SimpleInternalLayerWidget(final MappingComponent mappingComponent) {
        initComponents();
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE); // NOI18N
        this.mappingComponent = mappingComponent;
//setClosable(true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   errorAbolitionTime  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LayerControl showFeatureCollection(final int errorAbolitionTime) {
        final LayerControl control = new LayerControl(
                mappingComponent,
                LayerControl.FEATURE_COLLECTION,
                errorAbolitionTime);
        control.addLayerSelectionChangedListener(this);
        final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        panFeatureCollection.add(control, gridBagConstraints);
        featureCollectionControl = control;
        control.addMouseListener(this);
        return control;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position            DOCUMENT ME!
     * @param   layer               DOCUMENT ME!
     * @param   errorAbolitionTime  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LayerControl addRasterService(final int position, final ServiceLayer layer, final int errorAbolitionTime) {
        final LayerControl control = new LayerControl(
                mappingComponent,
                LayerControl.RASTER_SERVICE,
                errorAbolitionTime);
        control.setLayer(layer);
        control.addLayerSelectionChangedListener(this);
        final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
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
    /**
     * DOCUMENT ME!
     *
     * @param   position            DOCUMENT ME!
     * @param   layer               DOCUMENT ME!
     * @param   errorAbolitionTime  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LayerControl addFeatureService(final int position, final ServiceLayer layer, final int errorAbolitionTime) {
        final LayerControl control = new LayerControl(
                mappingComponent,
                LayerControl.FEATURE_SERVICE,
                errorAbolitionTime);
        control.setLayer(layer);
        control.addLayerSelectionChangedListener(this);
        final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
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

                @Override
                public void keyPressed(final java.awt.event.KeyEvent evt) {
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
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void formKeyPressed(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_formKeyPressed
    }                                                                //GEN-LAST:event_formKeyPressed

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
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mousePressed(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseExited(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseEntered(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final java.awt.event.MouseEvent e) {
        if ((e.getSource() instanceof LayerControl) && (e.getClickCount() > 1) && (e.getButton() == e.BUTTON1)) {
            final LayerControl flashControl = (LayerControl)e.getSource();
            final Vector allControls = new Vector();
            allControls.addAll(rasterLayerControls);
            allControls.addAll(featureLayerControls);
            allControls.remove(flashControl);
            flashControl.flashObject(true, 1000, 3000);
            final Iterator it = allControls.iterator();
            final java.awt.event.ActionListener timerAction = new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed(final java.awt.event.ActionEvent event) {
                        while (it.hasNext()) {
                            final LayerControl lc = (LayerControl)it.next();
                            lc.flashObject(false, 0, 4000);
                        }
                    }
                };

            final javax.swing.Timer timer = new javax.swing.Timer(1000, timerAction);
            timer.setRepeats(false);
            timer.start();
        }
    }

    @Override
    public void layerControlSelectionChanged(final LayerControl lc) {
//        boolean sel=lc.isSelected();
//        for (LayerControl lac:rasterLayerControls) {
//            lac.setSelected(false);
//        }
//        for (LayerControl lac:featureLayerControls) {
//            lac.setSelected(false);
//        }
//        lc.setSelected(sel);
    }

    @Override
    public void layerWantsUp(final LayerControl lc) {
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

    @Override
    public void layerWantsDown(final LayerControl lc) {
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
