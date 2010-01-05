/*
 * KeyboardListener.java
 *
 * Created on 29. August 2007, 13:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.tools.CismetThreadPool;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.event.KeyEvent;

/**
 *
 * @author hell
 */
public class KeyboardListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    MappingComponent viewer;
    public final static String X_PAN = "X_PAN";
    public final static String Y_PAN = "Y_PAN";

    public KeyboardListener(MappingComponent map) {
        viewer = map;
    }

    @Override
    public void keyPressed(PInputEvent event) {
        super.keyPressed(event);
        log.debug("keyPressed " + event);
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
            CreateGeometryListener listener = (CreateGeometryListener) viewer.getInputListener(MappingComponent.NEW_POLYGON);
            log.debug("Event an CreateGeometryListener weitergeleitet:" + listener);
            listener.keyPressed(event);
        } else {
            log.debug("kein treffer:" + event.getKeyCode() + "     (" + KeyEvent.VK_DOWN);
        }
    }

    public void pan(String direction, float factor, int localAnimationDuration, int delayTime) {
        final PBounds b = viewer.getCamera().getViewBounds();
        if (direction.equals(X_PAN)) {
            b.setOrigin(b.getX() + b.getWidth() * factor, b.getY());
            viewer.getHandleLayer().removeAllChildren();
            viewer.getCamera().animateViewToCenterBounds(b, false, localAnimationDuration);
            viewer.showHandles(true);

        } else if (direction.equals(Y_PAN)) {
            b.setOrigin(b.getX(), b.getY() + b.getHeight() * factor);
            viewer.getHandleLayer().removeAllChildren();
            viewer.getCamera().animateViewToCenterBounds(b, false, localAnimationDuration);
            viewer.showHandles(true);
        }
        if (localAnimationDuration == 0) {
            CismapBroker.getInstance().fireMapBoundsChanged();
        }
        try {
            refreshTime = System.currentTimeMillis() + delayTime;
            if (refreshThread == null || !refreshThread.isAlive()) {
                refreshThread = new Thread() {
                    public void run() {
                        while (System.currentTimeMillis() < refreshTime) {
                            try {
                                sleep(100);
                            //log.debug("WAIT");
                            } catch (InterruptedException iex) {
                            }
                        }
                        //log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                        viewer.setNewViewBounds(b);
                        viewer.queryServices();
                    }
                };
                CismetThreadPool.execute(refreshThread);
            //log.fatal("Breite src:"+((MappingComponent)e.getComponent()).getWtst().getSourceRect().getWidth());
            }
        } catch (Exception ex) {
            log.debug("fehler in pan", ex);
        }
    }

    public void zoom(float factor, final PInputEvent e, int localAnimationDuration, int delayTime) {
        try {
            log.debug("zoom");
            final PBounds b = new PBounds();
            double h = viewer.getCamera().getViewBounds().getHeight();
            double w = viewer.getCamera().getViewBounds().getWidth();
            double scale = factor;
            
            double oldWidth = viewer.getCamera().getViewBounds().getWidth();
            double newWidth = oldWidth * (1 - scale + 1);
            double oldHeight = viewer.getCamera().getViewBounds().getHeight();
            double newHeight = oldHeight * (1 - scale + 1);
            
            double offsetX = (newWidth - oldWidth) / 2;
            double offsetY = (newHeight - oldHeight) / 2;
            
            //Offsetverschiebung sorgt daf\u00FCr das der Punkt auf den man geclickt hat an der gleichen Stelle bleibt
//        double xR=e.getPosition().getX()-offsetX;
//        double yR=e.getPosition().getY()-offsetY;
            b.setOrigin(viewer.getCamera().getViewBounds().getOrigin().getX() - offsetX, viewer.getCamera().getViewBounds().getOrigin().getY() - offsetY);//);
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
            if (refreshThread == null || !refreshThread.isAlive()) {
                refreshThread = new Thread() {
                    public void run() {
                        while (System.currentTimeMillis() < refreshTime) {
                            try {
                                sleep(100);
                            //log.debug("WAIT");
                            } catch (InterruptedException iex) {
                            }
                        }
                        //log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                        viewer.setNewViewBounds(b);
                        viewer.queryServices();
                    }
                };
                refreshThread.setPriority(Thread.NORM_PRIORITY);
                CismetThreadPool.execute(refreshThread);
            //log.fatal("Breite src:"+((MappingComponent)e.getComponent()).getWtst().getSourceRect().getWidth());
            }
        } catch (Exception ex) {
            log.debug("fehler in zoom", ex);
        }
    }
    Thread refreshThread;
    long refreshTime;
}
