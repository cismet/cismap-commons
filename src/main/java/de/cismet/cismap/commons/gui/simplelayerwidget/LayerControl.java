/*
 * LayerControl.java
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
 * Created on 22. August 2005, 10:31
 *
 */

package de.cismet.cismap.commons.gui.simplelayerwidget;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;

import edu.umd.cs.piccolo.PNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.JToolTip;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class LayerControl extends javax.swing.JPanel implements RetrievalListener{

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private boolean selected=false;
    private ServiceLayer layer;
    private ImageIcon visibleIcon=null;
    private ImageIcon invisibleIcon=null;
    private ImageIcon visibleRasterIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/rasterLayerVisible.png"));//NOI18N
    private ImageIcon invisibleRasterIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/rasterLayerNotVisible.png"));//NOI18N
    private ImageIcon visibleFeatureIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/featureLayerVisible.png"));//NOI18N
    private ImageIcon invisibleFeatureIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/featureLayerNotVisible.png"));//NOI18N
    private ImageIcon visibleCollectionIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/featureCollectionLayerVisible.png"));//NOI18N
    private ImageIcon invisibleCollectionIcon=new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/featureCollectionLayerNotVisible.png"));//NOI18N
    private PNode transparentable;
    public static final int RASTER_SERVICE=1;
    public static final int FEATURE_SERVICE=2;
    public static final int FEATURE_COLLECTION=4;
    private int type;
    private Image errorImage=null;
    private int errorAbolitionTime=1000;
    private MappingComponent mappingComponent;
    private Vector <LayerControlSelectionChangedListener> listener=new Vector <LayerControlSelectionChangedListener>();
    /**
     * Creates new form LayerControl
     */
    //SliderMenuItem slider=new SliderMenuItem();
    JSlider slider=new JSlider(){
        public JToolTip createToolTip() {
            if (errorImage!=null) {
                return new ImageToolTip(errorImage);
            } else {
                return super.createToolTip();
            }
        }
    };
    public LayerControl(MappingComponent mappingComponent,int type,int errorAbolitionTime) {
        this.mappingComponent=mappingComponent;
        this.errorAbolitionTime=errorAbolitionTime;
        this.type=type;
        switch (type) {
            case RASTER_SERVICE:
                visibleIcon=visibleRasterIcon;
                invisibleIcon=invisibleRasterIcon;
                break;
            case FEATURE_SERVICE:
                visibleIcon=visibleFeatureIcon;
                invisibleIcon=invisibleFeatureIcon;
                break;
            case FEATURE_COLLECTION:
                visibleIcon=visibleCollectionIcon;
                invisibleIcon=invisibleCollectionIcon;
                break;
        }
        initComponents();
        lblIcon.setIcon(visibleIcon);
        slider.setBorder(new EmptyBorder(2,2,2,2));
        slider.setFocusable(false);
        prbLayer.setLayout(new BorderLayout(2,2));
        prbLayer.add(slider,BorderLayout.CENTER);
        slider.setRequestFocusEnabled(false);
        
        slider.setOpaque(false);
        //pmenProps.add(slider);
        addMouseListener(new MousePopupListener(  ));
        //slider.setForeground(pmenProps.getBackground());
        
//        prbLayer.setBorderPainted(false);
        slider.addChangeListener(new ChangeListener(){
            public void stateChanged(final ChangeEvent e) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        setTransparency((float)((int)((JSlider)e.getSource()).getValue()/100.0));
                    }
                });
            }
        });
        
    }
    
    
    public void addLayerSelectionChangedListener(LayerControlSelectionChangedListener lc) {
        listener.add(lc);
    }
    public void removeLayerSelectionChangedListener(LayerControlSelectionChangedListener lc) {
        listener.remove(lc);
    }
    private void fireLayerSelectionChanged() {
        for (LayerControlSelectionChangedListener lc:listener){
            lc.layerControlSelectionChanged(this);
        }
    }
    private void fireLayerWantsUp() {
        for (LayerControlSelectionChangedListener lc:listener){
            lc.layerWantsUp(this);
        }
    }
    private void fireLayerWantsDown() {
        for (LayerControlSelectionChangedListener lc:listener){
            lc.layerWantsDown(this);
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

        pmenProps = new javax.swing.JPopupMenu();
        mitLayerName = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mitGridEnabled = new javax.swing.JCheckBoxMenuItem();
        lblIndent = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();
        lblLayerName = new javax.swing.JLabel();
        prbLayer = new javax.swing.JProgressBar();

        mitLayerName.setText(org.openide.util.NbBundle.getMessage(LayerControl.class, "LayerControl.mitLayerName.text")); // NOI18N
        mitLayerName.setEnabled(false);
        pmenProps.add(mitLayerName);
        pmenProps.add(jSeparator1);

        mitGridEnabled.setText(org.openide.util.NbBundle.getMessage(LayerControl.class, "LayerControl.mitGridEnabled.text")); // NOI18N
        pmenProps.add(mitGridEnabled);

        setToolTipText("");
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(lblIndent, gridBagConstraints);

        lblIcon.setText(org.openide.util.NbBundle.getMessage(LayerControl.class, "LayerControl.lblIcon.text")); // NOI18N
        lblIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblIconMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(lblIcon, gridBagConstraints);

        lblLayerName.setText(org.openide.util.NbBundle.getMessage(LayerControl.class, "LayerControl.lblLayerName.text")); // NOI18N
        lblLayerName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblLayerNameMouseClicked(evt);
            }
        });
        lblLayerName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lblLayerNameKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(lblLayerName, gridBagConstraints);

        prbLayer.setPreferredSize(new java.awt.Dimension(80, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(prbLayer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void lblLayerNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lblLayerNameKeyPressed
        log.debug("LBL Key");//NOI18N
    }//GEN-LAST:event_lblLayerNameKeyPressed
    
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        log.debug("Key");//NOI18N
        if (evt.getKeyCode()==evt.VK_UP) {
            fireLayerWantsUp();
        } else if (evt.getKeyCode()==evt.VK_DOWN) {
            fireLayerWantsDown();
        }
        
    }//GEN-LAST:event_formKeyPressed
    
    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        setSelected(!isSelected());
        fireLayerSelectionChanged();
        this.requestFocus();
    }//GEN-LAST:event_formMouseClicked
    
    private void lblLayerNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLayerNameMouseClicked
        setSelected(!isSelected());
        fireLayerSelectionChanged();
        this.requestFocus();
    }//GEN-LAST:event_lblLayerNameMouseClicked
    
    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        
    }//GEN-LAST:event_formMouseEntered
    
    private void lblIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblIconMouseClicked
        if (getLayer()!=null&&evt.getButton()==evt.BUTTON1) {
            getLayer().setEnabled(!getLayer().isEnabled());
            transparentable.setVisible(getLayer().isEnabled());
            transparentable.repaint();
        }
        syncIconWithEnabledState();
    }//GEN-LAST:event_lblIconMouseClicked
    
    public void syncIconWithEnabledState() {
        if (getLayer().isEnabled()) {
            lblIcon.setIcon(this.visibleIcon);
            if (getLayer() instanceof MapService) {
//                mappingComponent.handleMapService(-1,(MapService)getLayer(),true);
            }
            
        } else {
            lblIcon.setIcon(this.invisibleIcon);
            ((AbstractRetrievalService)getLayer()).fireRetrievalAborted(new RetrievalEvent());
        }
        setObjectVisible(getLayer().isEnabled());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblIndent;
    private javax.swing.JLabel lblLayerName;
    private javax.swing.JCheckBoxMenuItem mitGridEnabled;
    private javax.swing.JMenuItem mitLayerName;
    private javax.swing.JPopupMenu pmenProps;
    private javax.swing.JProgressBar prbLayer;
    // End of variables declaration//GEN-END:variables
    
    class MousePopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) { checkPopup(e); }
        public void mouseClicked(MouseEvent e) { checkPopup(e); }
        public void mouseReleased(MouseEvent e) { checkPopup(e); }
        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger(  )) {
                //pmenProps.show(LayerControl.this, e.getX(  ), e.getY(  ));
            }
        }
    }
    class SliderMenuItem extends JSlider implements MenuElement {
        public SliderMenuItem(  ) {
//            setBorder(new CompoundBorder(new TitledBorder("Control"),
//                                  new EmptyBorder(1, 1, 1, 1)));
            
            setMajorTickSpacing(25);
            setMinorTickSpacing(20);
            
        }
        public void processMouseEvent(MouseEvent e, MenuElement path[ ],
                MenuSelectionManager manager) {}
        public void processKeyEvent(KeyEvent e, MenuElement path[ ],
                MenuSelectionManager manager) {}
        public void menuSelectionChanged(boolean isIncluded) {}
        public MenuElement[ ] getSubElements(  ) {return new MenuElement[0];}
        public Component getComponent(  ) {return this;}
    }
    
    
    
    public void retrievalStarted(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (getLayer().isEnabled()) {
                    resetProgressbarColor();
                    prbLayer.setIndeterminate(true);
                }
            }
        });
    }
    
    public void retrievalProgress(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (getLayer().isEnabled()) {
                    prbLayer.setIndeterminate(false);
                    prbLayer.setValue((int)(100*e.getPercentageDone()));
                }
            }
        });
        
    }
    
    public void retrievalError(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        Runnable t=new Runnable(){
            public void run() {
                if (getLayer().isEnabled()) {
                    if (e.getRetrievedObject() instanceof Image) {
                        
                        Image i=(Image)e.getRetrievedObject();
                        int newWidth=(int)(i.getWidth(null)*0.7);
                        int newHeight=(int)(i.getHeight(null)*0.7);
                        
                        log.debug("w,h:"+newWidth+","+newHeight);//NOI18N
                        Image ii=i.getScaledInstance(newWidth,newHeight , i.SCALE_SMOOTH);
                        BufferedImage bi=new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g=(Graphics2D)bi.getGraphics();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
                        g.drawImage(i,0,0,newWidth,newHeight,null);
                        g.drawLine(10, 0, 20,0);
                        
                        int maxX=0;
                        int maxY=0;
                        int minX=newWidth;
                        int minY=newHeight;
                        int white=-1;
                        
                        for (int x=0;x<newWidth;++x) {
                            for (int y=0;y<newHeight;++y) {
                                int val=bi.getRGB(x,y);
                                if (val!=white) {
                                    if (x>maxX)maxX=x;
                                    if (x<minX)minX=x;
                                    if (y>maxY)maxY=y;
                                    if (y<minY)minY=y;
                                }
                            }
                        }
                        int border=5;
                        if (minX-border<0)minX=0; else minX-=border;
                        if (minY-border<0)minY=0; else minY-=border;
                        if (maxX+border>newWidth)maxX=newWidth; else maxX+=border;
                        if (maxY+border>newHeight)maxY=newHeight; else maxY+=border;
                        try {
                            
                            bi=bi.getSubimage(minX, minY,maxX,maxY);
                        } catch (Exception e) {
                        }
                        errorImage=bi;
                        slider.setToolTipText(org.openide.util.NbBundle.getMessage(LayerControl.class, "LayerControl.slider.toolTipText1"));
                    } else if (e.getRetrievedObject() instanceof String) {
                        errorImage=null;
                        final String message=(String)e.getRetrievedObject();
//                  slider.setToolTipText("<html><b>Der Server lieferte folgende Fehlermeldung zur�ck:<br></b><body>"+message+"</body></html>");
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                slider.setToolTipText(org.openide.util.NbBundle.getMessage(LayerControl.class, "LayerControl.slider.toolTipText2", new Object[]{message}));
                            }
                        });
                        
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            prbLayer.setForeground(java.awt.Color.RED);
                            prbLayer.setIndeterminate(false);
                            prbLayer.setValue(100);
                        }
                    });
                    if (errorAbolitionTime>0) {
                        java.awt.event.ActionListener timerAction = new java.awt.event.ActionListener() {
                            public void actionPerformed( java.awt.event.ActionEvent event ) {
                                resetProgressbarColor();
                            }
                        };
                        javax.swing.Timer timer = new javax.swing.Timer(errorAbolitionTime, timerAction );
                        timer.setRepeats(false);
                        timer.start();
                    }
                    
                }
            }
            
        };
        CismetThreadPool.execute(t);
        //prbLayer.setForeground(new Color(255,0,0));
    }
    
    
    public void retrievalComplete(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (getLayer().isEnabled()) {
                    if (!e.isHasErrors()) {
                        errorImage=null;
                        slider.setToolTipText("");//NOI18N
                        prbLayer.setIndeterminate(false);
                        prbLayer.setValue(100);
                    } else {
                        retrievalError(e);
                    }
                }
            }
        });
    }
    
    public void retrievalAborted(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                prbLayer.setIndeterminate(false);
                prbLayer.setValue(0);
            }
        });
    }
    
    public void setIndent(String s) {
        lblIndent.setText(s);
    }
    public void setLayer(ServiceLayer layer) {
        this.layer=layer;
        lblLayerName.setText(layer.getName());
        mitLayerName.setText(layer.getName());
    }
    public void setTransparentable(PNode t) {
        transparentable=t;
        slider.setValue((int)(100*transparentable.getTransparency()));
//        if (transparentable.getVisible()) {
//            lblIcon.setIcon(visibleIcon);
//        }
//        else {
//            lblIcon.setIcon(invisibleIcon);
//        }
    }
    public PNode getPNode() {
        return transparentable;
    }
    private float getTransparency() {
        return transparentable.getTransparency();
    }
    
    private void setTransparency(final float f) {
        transparentable.setTransparency(f);
        transparentable.repaint();
    }
    private void setObjectVisible(boolean b) {
        if (transparentable!=null) {
            transparentable.setVisible(b);
        } else {
            log.warn("transparentable was null");//NOI18N
        }
    }
    
    public void flashObject(final boolean positive,final int fadeDuration,int flashDuration) {
        final float oldTrans=transparentable.getTransparency();
        if (positive) {
            transparentable.animateToTransparency(1f, fadeDuration);
        } else {
            transparentable.setTransparency(0.1f);
        }
        java.awt.event.ActionListener timerAction = new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent event ) {
                if (positive) {
                    transparentable.animateToTransparency(oldTrans, fadeDuration);
                } else {
                    transparentable.setTransparency(oldTrans);
                }
            }
        };
        javax.swing.Timer timer = new javax.swing.Timer(flashDuration, timerAction );
        timer.setRepeats(false);
        timer.start();
    }
    
    
    public boolean isRunning() {
        if (prbLayer.isIndeterminate()||prbLayer.getValue()<100) {
            return true;
        } else {
            return false;
        }
    }
    
    private void resetProgressbarColor() {
        prbLayer.setForeground(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"));//NOI18N
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
//        if (selected) {
//            setBackground(java.awt.SystemColor.textHighlight);
//            //setBackground(new java.awt.Color(51, 255, 51));
//            //setBackground(javax.swing.UIManager.getDefaults().getColor("PropSheet.selectedSetBackground"));
//            repaint();
//        } else {
//            setBackground(new Color(236,233,216));
//            repaint();
//        }
    }

    public ServiceLayer getLayer() {
        return layer;
    }
}
