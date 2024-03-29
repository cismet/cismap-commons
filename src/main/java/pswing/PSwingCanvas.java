/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/* Copyright 2004, Sam Reid */
package pswing;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PBounds;

import org.jdesktop.swingx.RepaintManagerX;
import org.jdesktop.swingx.TranslucentRepaintManager;

import java.awt.*;

import java.util.Vector;

import javax.swing.*;

/**
 * User: Sam Reid Date: Jul 12, 2005 Time: 8:47:08 AM Copyright (c) Jul 12, 2005 by Sam Reid
 *
 * @version  $Revision$, $Date$
 */
public class PSwingCanvas extends PCanvas {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SWING_WRAPPER_KEY = "Swing Wrapper"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private JComponent swingWrapper = new SwingWrapper();
    private ZBasicRepaintManager zBasicRepaintManager = new ZBasicRepaintManager();
    private PSwingEventHandler swingEventHandler;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PSwingCanvas object.
     */
    public PSwingCanvas() {
        add(swingWrapper);
        RepaintManager.setCurrentManager(zBasicRepaintManager);

        swingEventHandler = new PSwingEventHandler(this, getCamera()); // todo or maybe getCameraLayer()?
        swingEventHandler.setActive(true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JComponent getSwingWrapper() {
        return swingWrapper;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class SwingWrapper extends JComponent {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SwingWrapper object.
         */
        public SwingWrapper() {
            setSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(0, 0));
            putClientProperty(SWING_WRAPPER_KEY, SWING_WRAPPER_KEY);
        }
    }

    /**
     * This is an internal class used by Jazz to support Swing components in Jazz. This should not be instantiated,
     * though all the public methods of javax.swing.RepaintManager may still be called and perform in the expected
     * manner.
     *
     * <p/>ZBasicRepaint Manager is an extension of RepaintManager that traps those repaints called by the Swing
     * components that have been added to the ZCanvas and passes these repaints to the SwingVisualComponent rather than
     * up the component hierarchy as usually happens.</p>
     *
     * <p>Also traps revalidate calls made by the Swing components added to the ZCanvas to reshape the applicable Visual
     * Component.</p>
     *
     * <p>Also keeps a list of ZSwings that are painting. This disables repaint until the component has finished
     * painting. This is to address a problem introduced by Swing's CellRendererPane which is itself a work-around. The
     * problem is that JTable's, JTree's, and JList's cell renderers need to be validated before repaint. Since we have
     * to repaint the entire Swing component hierarchy (in the case of a Swing component group used as a Jazz visual
     * component). This causes an infinite loop. So we introduce the restriction that no repaints can be triggered by a
     * call to paint.</p>
     *
     * @version  $Revision$, $Date$
     */
    @TranslucentRepaintManager
    public class ZBasicRepaintManager extends RepaintManagerX {

        //~ Instance fields ----------------------------------------------------

        // The components that are currently painting
        // This needs to be a vector for thread safety
        Vector paintingComponents = new Vector();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ZBasicRepaintManager object.
         */
        public ZBasicRepaintManager() {
            super(new RepaintManager());
        }

        /**
         * Creates a new ZBasicRepaintManager object.
         *
         * @param  delegate  DOCUMENT ME!
         */
        public ZBasicRepaintManager(final RepaintManager delegate) {
            super(delegate);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Locks repaint for a particular (Swing) component displayed by ZCanvas.
         *
         * @param  c  The component for which the repaint is to be locked
         */
        public void lockRepaint(final JComponent c) {
            paintingComponents.addElement(c);
        }

        /**
         * Unlocks repaint for a particular (Swing) component displayed by ZCanvas.
         *
         * @param  c  The component for which the repaint is to be unlocked
         */
        public void unlockRepaint(final JComponent c) {
            synchronized (paintingComponents) {
                if (paintingComponents.contains(c)) {
                    paintingComponents.removeElementAt(paintingComponents.lastIndexOf(c));
                }
            }
        }

        /**
         * Returns true if repaint is currently locked for a component and false otherwise.
         *
         * @param   c  The component for which the repaint status is desired
         *
         * @return  Whether the component is currently painting
         */
        public boolean isPainting(final JComponent c) {
            return paintingComponents.contains(c);
        }

        /**
         * This is the method "repaint" now calls in the Swing components. Overridden to capture repaint calls from
         * those Swing components which are being used as Jazz visual components and to call the Jazz repaint mechanism
         * rather than the traditional Component hierarchy repaint mechanism. Otherwise, behaves like the superclass.
         *
         * @param  c  Component to be repainted
         * @param  x  X coordinate of the dirty region in the component
         * @param  y  Y coordinate of the dirty region in the component
         * @param  w  Width of the dirty region in the component
         * @param  h  Height of the dirty region in the component
         */
        @Override
        public synchronized void addDirtyRegion(final JComponent c,
                final int x,
                final int y,
                final int w,
                final int h) {
            boolean captureRepaint = false;
            JComponent capturedComponent = null;
            int captureX = x;
            int captureY = y;

            // We have to check to see if the ZCanvas
            // (ie. the SwingWrapper) is in the components ancestry.  If so,
            // we will want to capture that repaint.  However, we also will
            // need to translate the repaint request since the component may
            // be offset inside another component.
            for (Component comp = c; (comp != null) && isLightweight(comp) && !captureRepaint;
                        comp = comp.getParent()) {
                if (comp.getParent() == swingWrapper) {
                    if (comp instanceof JComponent) {
                        captureRepaint = true;
                        capturedComponent = (JComponent)comp;
                    }
                } else {
                    // Adds to the offset since the component is nested
                    captureX += comp.getLocation().getX();
                    captureY += comp.getLocation().getY();
                }
            }

            // Now we check to see if we should capture the repaint and act
            // accordingly
            if (captureRepaint) {
                if (!isPainting(capturedComponent)) {
                    final PSwing vis = (PSwing)capturedComponent.getClientProperty(PSwing.VISUAL_COMPONENT_KEY);

                    if (vis != null) {
                        final int repaintX = captureX;
                        final int repaintY = captureY;
                        final Runnable repainter = new Runnable() {

                                @Override
                                public void run() {
                                    vis.repaint(new PBounds((double)repaintX, (double)repaintY, (double)w, (double)h));
                                }
                            };
                        SwingUtilities.invokeLater(repainter);
                    }
                }
            } else {
                super.addDirtyRegion(c, x, y, w, h);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   comp  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private boolean isLightweight(final Component comp) {
            return comp.isDisplayable() || comp.isLightweight();
        }

        /**
         * This is the method "revalidate" calls in the Swing components. Overridden to capture revalidate calls from
         * those Swing components being used as Jazz visual components and to update Jazz's visual component wrapper
         * bounds (these are stored separately from the Swing component). Otherwise, behaves like the superclass.
         *
         * @param  invalidComponent  The Swing component that needs validation
         */
        @Override
        public synchronized void addInvalidComponent(final JComponent invalidComponent) {
            final JComponent capturedComponent = invalidComponent;

            if ((capturedComponent.getParent() != null)
                        && (capturedComponent.getParent() instanceof JComponent)
                        && (((JComponent)capturedComponent.getParent()).getClientProperty(SWING_WRAPPER_KEY) != null)) {
                final Runnable validater = new Runnable() {

                        @Override
                        public void run() {
                            capturedComponent.validate();
                            final PSwing swing = (PSwing)capturedComponent.getClientProperty(
                                    PSwing.VISUAL_COMPONENT_KEY);
                            swing.reshape();
                        }
                    };
                SwingUtilities.invokeLater(validater);
            } else {
                super.addInvalidComponent(invalidComponent);
            }
        }
    }
}
