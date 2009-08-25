/*
 * RubberBandZoomListener.java
 *
 * Created on 5. M\u00E4rz 2005, 15:05
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 *
 * @author Hell
 */
public class RubberBandZoomListener extends RectangleRubberBandListener {
    public static final int ANIMATION_DURATION = 750;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == 1) { //Linke Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            final PBounds b = new PBounds(rectangle.getBounds());
            PBounds bb = (PBounds) b.clone();
            e.getCamera().viewToLocal(bb);
            if (bb.width > 20 && bb.height > 20) {
                if (e.getComponent() instanceof MappingComponent) {
                    e.getCamera().animateViewToCenterBounds(b, true, ((MappingComponent) e.getComponent()).getAnimationDuration());
                    ((MappingComponent) e.getComponent()).setNewViewBounds(b);
                    ((MappingComponent) e.getComponent()).queryServices();
                }
            }
        }
    }

    @Override
    public void mouseClicked(PInputEvent e) {
        super.mouseClicked(e);
        if (e.getButton() == 3) { //Rechte Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            if (e.getComponent() instanceof MappingComponent) {
                zoom(0.5f, e, ((MappingComponent) e.getComponent()).getAnimationDuration(), 200);
            }
        }
    }

    @Override
    public void mouseWheelRotated(PInputEvent pInputEvent) {
        super.mouseWheelRotated(pInputEvent);
        if (pInputEvent.getWheelRotation() < 0) {
            zoom(0.9f, pInputEvent, 0, 800);
        } else {
            zoom(1.1f, pInputEvent, 0, 800);
        }
    }

    public void zoom(float factor, PInputEvent e, int localAnimationDuration) {
        zoom(factor, e, localAnimationDuration, 0);
    }

    public void zoom(float factor, final PInputEvent e, int localAnimationDuration, int delayTime) {
        final PBounds b = new PBounds();
        double h = e.getCamera().getViewBounds().getHeight();
        double w = e.getCamera().getViewBounds().getWidth();
        double scale = factor;
        
        double oldWidth = e.getCamera().getViewBounds().getWidth();
        double newWidth = oldWidth * (1 - scale + 1);
        double oldHeight = e.getCamera().getViewBounds().getHeight();
        double newHeight = oldHeight * (1 - scale + 1);
        
        double offsetX = (newWidth - oldWidth) / 2;
        double offsetY = (newHeight - oldHeight) / 2;

        //Offsetverschiebung sorgt daf\u00FCr das der Punkt auf den man geclickt hat an der gleichen Stelle bleibt        
        double xR = e.getPosition().getX() - offsetX;
        double yR = e.getPosition().getY() - offsetY;
        
        b.setOrigin(e.getCamera().getViewBounds().getOrigin().getX() - offsetX, e.getCamera().getViewBounds().getOrigin().getY() - offsetY);//);
        b.setSize(newWidth, newHeight);
        e.getCamera().animateViewToCenterBounds(b, true, localAnimationDuration);
        if (localAnimationDuration == 0) {
            CismapBroker.getInstance().fireMapBoundsChanged();
        }
//        if (e.getComponent() instanceof SimpleFeatureViewer) {
//            ((SimpleFeatureViewer)e.getComponent()).refreshBackground();                            
//        }
        if (e.getComponent() instanceof MappingComponent) {
            zoomTime = System.currentTimeMillis() + delayTime;
            if (zoomThread == null || !zoomThread.isAlive()) {
                zoomThread = new Thread() {
                    public void run() {
                        while (System.currentTimeMillis() < zoomTime) {
                            try {
                                sleep(100);
                            //log.debug("WAIT");
                            } catch (InterruptedException iex) {
                            }
                        }
                        //log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                        ((MappingComponent) e.getComponent()).setNewViewBounds(b);
                        ((MappingComponent) e.getComponent()).queryServices();
                    }
                };
                zoomThread.setPriority(Thread.NORM_PRIORITY);
                zoomThread.start();
            }
        //log.fatal("Breite src:"+((MappingComponent)e.getComponent()).getWtst().getSourceRect().getWidth());
        }
    }
    Thread zoomThread;
    long zoomTime;
}
