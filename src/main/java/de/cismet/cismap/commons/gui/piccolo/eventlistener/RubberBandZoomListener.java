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
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author Hell
 */
public class RubberBandZoomListener extends RectangleRubberBandListener {

    public RubberBandZoomListener() {
        timer = new Timer(500, null);
        timer.setRepeats(false);
    }
    public static final int ANIMATION_DURATION = 750;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final Timer timer;
    private ActionListener zoomListener = null;

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == 1) { //Linke Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            final PBounds b = new PBounds(rectangle.getBounds());
            PBounds bb = (PBounds) b.clone();
            e.getCamera().viewToLocal(bb);
            if (bb.width > 20 && bb.height > 20) {
                if (e.getComponent() instanceof MappingComponent) {
                    MappingComponent map = (MappingComponent) e.getComponent();
//                    map.getHandleLayer().removeAllChildren();
                    e.getCamera().animateViewToCenterBounds(b, true, map.getAnimationDuration());
                    map.setNewViewBounds(b);
                    map.queryServices();
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
//        if (e.getComponent() instanceof MappingComponent) {
//            if (((MappingComponent) e.getComponent()).getAnimating()) {
                final PBounds b = new PBounds();
                double scale = factor;
                double oldWidth = e.getCamera().getViewBounds().getWidth();
                double newWidth = oldWidth * (1 - scale + 1);
                double oldHeight = e.getCamera().getViewBounds().getHeight();
                double newHeight = oldHeight * (1 - scale + 1);

                double offsetX = (newWidth - oldWidth) / 2;
                double offsetY = (newHeight - oldHeight) / 2;
//        if (e.getComponent() instanceof MappingComponent) {
//            MappingComponent map = (MappingComponent) e.getComponent();
//            map.getHandleLayer().removeAllChildren();
//        }
                //Offsetverschiebung sorgt daf\u00FCr das der Punkt auf den man geclickt hat an der gleichen Stelle bleibt
                b.setOrigin(e.getCamera().getViewBounds().getOrigin().getX() - offsetX, e.getCamera().getViewBounds().getOrigin().getY() - offsetY);//);
                b.setSize(newWidth, newHeight);
                e.getCamera().animateViewToCenterBounds(b, true, localAnimationDuration);
                if (localAnimationDuration == 0) {
                    CismapBroker.getInstance().fireMapBoundsChanged();
                }
                if (!timer.isRunning()) {
                    if (zoomListener != null) {
                        timer.removeActionListener(zoomListener);
                    }
                    zoomListener = new ZoomAction(b, e);
                    timer.addActionListener(zoomListener);
                    timer.start();
                }
            }
//        }
//    }
}
