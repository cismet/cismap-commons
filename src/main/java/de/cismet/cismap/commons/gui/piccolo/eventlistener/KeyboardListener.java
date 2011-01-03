/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * KeyboardListener.java
 *
 * Created on 29. August 2007, 13:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.event.KeyEvent;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.CismetThreadPool;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class KeyboardListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String X_PAN = "X_PAN"; // NOI18N
    public static final String Y_PAN = "Y_PAN"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    MappingComponent viewer;
    Thread refreshThread;
    long refreshTime;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KeyboardListener object.
     *
     * @param  map  DOCUMENT ME!
     */
    public KeyboardListener(final MappingComponent map) {
        viewer = map;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void keyPressed(final PInputEvent event) {
        super.keyPressed(event);
        if (log.isDebugEnabled()) {
            log.debug("keyPressed " + event);                                                  // NOI18N
        }
        if (event.getKeyChar() == 'a') {
            zoom(0.95f, event, 0, 800);
        } else if (event.getKeyChar() == 'y') {
            zoom(1.05f, event, 0, 800);
        } else if (event.getKeyCode() == KeyEvent.VK_UP) {
            pan(Y_PAN, 0.05f, 0, 800);
        } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            pan(Y_PAN, -0.05f, 0, 800);
        } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            pan(X_PAN, -0.05f, 0, 800);
        } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            pan(X_PAN, 0.05f, 0, 800);
        } else if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            final CreateNewGeometryListener listener = (CreateNewGeometryListener)viewer.getInputListener(
                    MappingComponent.NEW_POLYGON);
            if (log.isDebugEnabled()) {
                log.debug("Event an CreateGeometryListener weitergeleitet:" + listener);       // NOI18N
            }
            listener.keyPressed(event);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("kein treffer:" + event.getKeyCode() + "     (" + KeyEvent.VK_DOWN); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  direction               DOCUMENT ME!
     * @param  factor                  DOCUMENT ME!
     * @param  localAnimationDuration  DOCUMENT ME!
     * @param  delayTime               DOCUMENT ME!
     */
    public void pan(final String direction, final float factor, final int localAnimationDuration, final int delayTime) {
        final PBounds b = viewer.getCamera().getViewBounds();
        if (direction.equals(X_PAN)) {
            b.setOrigin(b.getX() + (b.getWidth() * factor), b.getY());
            viewer.getHandleLayer().removeAllChildren();
            viewer.getCamera().animateViewToCenterBounds(b, false, localAnimationDuration);
            viewer.showHandles(true);
        } else if (direction.equals(Y_PAN)) {
            b.setOrigin(b.getX(), b.getY() + (b.getHeight() * factor));
            viewer.getHandleLayer().removeAllChildren();
            viewer.getCamera().animateViewToCenterBounds(b, false, localAnimationDuration);
            viewer.showHandles(true);
        }
        if (localAnimationDuration == 0) {
            CismapBroker.getInstance().fireMapBoundsChanged();
        }
        try {
            refreshTime = System.currentTimeMillis() + delayTime;
            if ((refreshThread == null) || !refreshThread.isAlive()) {
                refreshThread = new Thread() {

                        @Override
                        public void run() {
                            while (System.currentTimeMillis() < refreshTime) {
                                try {
                                    sleep(100);
                                    // log.debug("WAIT");
                                } catch (InterruptedException iex) {
                                }
                            }
                            // log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                            viewer.setNewViewBounds(b);
                            viewer.queryServices();
                        }
                    };
                CismetThreadPool.execute(refreshThread);
                // log.fatal("Breite src:"+((MappingComponent)e.getComponent()).getWtst().getSourceRect().getWidth());
            }
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("error in pan", ex); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  factor                  DOCUMENT ME!
     * @param  e                       DOCUMENT ME!
     * @param  localAnimationDuration  DOCUMENT ME!
     * @param  delayTime               DOCUMENT ME!
     */
    public void zoom(final float factor, final PInputEvent e, final int localAnimationDuration, final int delayTime) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("zoom"); // NOI18N
            }
            final PBounds b = new PBounds();
            final double h = viewer.getCamera().getViewBounds().getHeight();
            final double w = viewer.getCamera().getViewBounds().getWidth();
            final double scale = factor;

            final double oldWidth = viewer.getCamera().getViewBounds().getWidth();
            final double newWidth = oldWidth * (1 - scale + 1);
            final double oldHeight = viewer.getCamera().getViewBounds().getHeight();
            final double newHeight = oldHeight * (1 - scale + 1);

            final double offsetX = (newWidth - oldWidth) / 2;
            final double offsetY = (newHeight - oldHeight) / 2;

            // Offsetverschiebung sorgt daf\u00FCr das der Punkt auf den man geclickt hat an der gleichen Stelle bleibt
// double xR=e.getPosition().getX()-offsetX;
// double yR=e.getPosition().getY()-offsetY;
            b.setOrigin(viewer.getCamera().getViewBounds().getOrigin().getX() - offsetX,
                viewer.getCamera().getViewBounds().getOrigin().getY()
                        - offsetY); // );
            b.setSize(newWidth, newHeight);
            viewer.getHandleLayer().removeAllChildren();
            viewer.getCamera().animateViewToCenterBounds(b, true, localAnimationDuration);
            viewer.showHandles(true);
            if (localAnimationDuration == 0) {
                CismapBroker.getInstance().fireMapBoundsChanged();
            }
//        if (e.getComponent() instanceof SimpleFeatureViewer) {
//            ((SimpleFeatureViewer)e.getComponent()).refreshBackground();
//        }
            refreshTime = System.currentTimeMillis() + delayTime;
            if ((refreshThread == null) || !refreshThread.isAlive()) {
                refreshThread = new Thread() {

                        @Override
                        public void run() {
                            while (System.currentTimeMillis() < refreshTime) {
                                try {
                                    sleep(100);
                                    // log.debug("WAIT");
                                } catch (InterruptedException iex) {
                                }
                            }
                            // log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                            viewer.setNewViewBounds(b);
                            viewer.queryServices();
                        }
                    };
                refreshThread.setPriority(Thread.NORM_PRIORITY);
                CismetThreadPool.execute(refreshThread);
                // log.fatal("Breite src:"+((MappingComponent)e.getComponent()).getWtst().getSourceRect().getWidth());
            }
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("error in zoom", ex); // NOI18N
            }
        }
    }
}
