/*
 * SimpleBackgroundedJPanel.java
 *
 * Created on 4. April 2005, 18:09
 */

package de.cismet.cismap.commons.gui;

import edu.umd.cs.piccolo.PCanvas;
import javax.swing.JPanel;

/**
 *
 * @author hell
 */
public class SimpleBackgroundedJPanel extends JPanel implements java.beans.PropertyChangeListener{
    PCanvas viewer;
    private boolean backgroundEnabled;
    /** Creates a new instance of SimpleBackgroundedJPanel */
    public SimpleBackgroundedJPanel() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        
    }
    
    public void formComponentResized(java.awt.event.ComponentEvent evt){
        if (viewer!=null) {
            viewer.setSize(evt.getComponent().getSize());
        }
    }
    
    public void setPCanvas(PCanvas v) {
        viewer=v;
        //viewer.addPropertyChangeListener(this);
        viewer.getCamera().addPropertyChangeListener(this);
        
    }

    public void paint(java.awt.Graphics g) {
        super.paintComponent(g);
        if (viewer!=null){//&&backgroundEnabled==true) {
            try {
                viewer.paint(g);
            }
            catch (Exception e) {}
        }
        //g.setColor(new Color(g.getColor().getRed(),g.getColor().getGreen(),g.getColor().getBlue(),200));
        super.paintChildren(g);
        //super.paint(g);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt){
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                repaint();
            }
        });
    }

    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    public void setBackgroundEnabled(boolean enabled) {
        if (viewer!=null) {
//            if (enabled!=backgroundEnabled) {
                if (!enabled) {
                    viewer.getCamera().animateToTransparency(0,1000);
                }
                else {
                    viewer.getCamera().animateToTransparency(0.3f,1000);
                }
//            }
        }
        backgroundEnabled=enabled;
//        this.backgroundEnabled = enabled;
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                repaint();
//            }
//        });
    }
    
    
}
