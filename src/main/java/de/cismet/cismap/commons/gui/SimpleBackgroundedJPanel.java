/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleBackgroundedJPanel.java
 *
 * Created on 4. April 2005, 18:09
 */
package de.cismet.cismap.commons.gui;

import edu.umd.cs.piccolo.PCanvas;

import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class SimpleBackgroundedJPanel extends JPanel implements java.beans.PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    PCanvas viewer;
    private boolean backgroundEnabled;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimpleBackgroundedJPanel.
     */
    public SimpleBackgroundedJPanel() {
        addComponentListener(new java.awt.event.ComponentAdapter() {

                @Override
                public void componentResized(final java.awt.event.ComponentEvent evt) {
                    formComponentResized(evt);
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    public void formComponentResized(final java.awt.event.ComponentEvent evt) {
        if (viewer != null) {
            viewer.setSize(evt.getComponent().getSize());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  v  DOCUMENT ME!
     */
    public void setPCanvas(final PCanvas v) {
        viewer = v;
        // viewer.addPropertyChangeListener(this);
        viewer.getCamera().addPropertyChangeListener(this);
    }

    @Override
    public void paint(final java.awt.Graphics g) {
        super.paintComponent(g);
        if (viewer != null) { // &&backgroundEnabled==true) {
            try {
                viewer.paint(g);
            } catch (Exception e) {
            }
        }
        // g.setColor(new Color(g.getColor().getRed(),g.getColor().getGreen(),g.getColor().getBlue(),200));
        super.paintChildren(g);
        // super.paint(g);
    }

    @Override
    public void propertyChange(final java.beans.PropertyChangeEvent evt) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    repaint();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    public void setBackgroundEnabled(final boolean enabled) {
        if (viewer != null) {
//            if (enabled!=backgroundEnabled) {
            if (!enabled) {
                viewer.getCamera().animateToTransparency(0, 1000);
            } else {
                viewer.getCamera().animateToTransparency(0.3f, 1000);
            }
//            }
        }
        backgroundEnabled = enabled;
//        this.backgroundEnabled = enabled;
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                repaint();
//            }
//        });
    }
}
