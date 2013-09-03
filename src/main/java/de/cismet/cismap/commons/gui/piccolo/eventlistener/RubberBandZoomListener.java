/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RubberBandZoomListener.java
 *
 * Created on 5. M\u00E4rz 2005, 15:05
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Hell
 * @version  $Revision$, $Date$
 */
public class RubberBandZoomListener extends RectangleRubberBandListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final int ANIMATION_DURATION = 750;

    //~ Instance fields --------------------------------------------------------

    private final transient Timer timer;
    private ActionListener zoomListener = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RubberBandZoomListener object.
     */
    public RubberBandZoomListener() {
        timer = new Timer(1, null);
        timer.setRepeats(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        if (e.getButton() == 1) { // Linke Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            final PBounds b = new PBounds(rectangle.getBounds());
            final PBounds bb = (PBounds)b.clone();
            e.getCamera().viewToLocal(bb);
            if ((bb.width > 20) && (bb.height > 20)) {
                if (e.getComponent() instanceof MappingComponent) {
                    final MappingComponent map = (MappingComponent)e.getComponent();
                    e.getCamera().animateViewToCenterBounds(b, true, map.getAnimationDuration());
                    map.setNewViewBounds(b);
                    map.queryServices();
                }
            }
        }
    }

    @Override
    public void mouseClicked(final PInputEvent e) {
        super.mouseClicked(e);
        if (e.getButton() == 3) { // Rechte Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            if (e.getComponent() instanceof MappingComponent) {
                zoom(0.5f, e, ((MappingComponent)e.getComponent()).getAnimationDuration(), 200);
            }
        }
    }

    @Override
    public void mouseWheelRotated(final PInputEvent pInputEvent) {
        super.mouseWheelRotated(pInputEvent);
        if (pInputEvent.getWheelRotation() < 0) {
            zoom(0.9f, pInputEvent, 0, 800);
        } else {
            zoom(1.1f, pInputEvent, 0, 800);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  factor                  DOCUMENT ME!
     * @param  e                       DOCUMENT ME!
     * @param  localAnimationDuration  DOCUMENT ME!
     */
    public void zoom(final float factor, final PInputEvent e, final int localAnimationDuration) {
        zoom(factor, e, localAnimationDuration, 0);
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
        zoom(factor, e.getCamera(), localAnimationDuration, delayTime);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  factor                  DOCUMENT ME!
     * @param  pc                      DOCUMENT ME!
     * @param  localAnimationDuration  DOCUMENT ME!
     * @param  delayTime               DOCUMENT ME!
     */
    public void zoom(final float factor, final PCamera pc, final int localAnimationDuration, final int delayTime) {
        final PBounds b = new PBounds();
        final double scale = factor;
        final double oldWidth = pc.getViewBounds().getWidth();
        final double newWidth = oldWidth * (1 - scale + 1);
        final double oldHeight = pc.getViewBounds().getHeight();
        final double newHeight = oldHeight * (1 - scale + 1);

        final double offsetX = (newWidth - oldWidth) / 2;
        final double offsetY = (newHeight - oldHeight) / 2;

        final Point2D origin = pc.getViewBounds().getOrigin();
        final double originX = origin.getX() - offsetX;
        final double originY = origin.getY() - offsetY;

        b.setOrigin(originX, originY);
        b.setSize(newWidth, newHeight);
        pc.animateViewToCenterBounds(b, true, localAnimationDuration);

        if (localAnimationDuration == 0) {
            CismapBroker.getInstance().fireMapBoundsChanged();
        }

        if (zoomListener != null) {
            timer.removeActionListener(zoomListener);
        }
        zoomListener = new ZoomAction(b, (MappingComponent)pc.getComponent());
        timer.addActionListener(zoomListener);

        timer.setInitialDelay(delayTime);
        if (timer.isRunning()) {
            timer.restart();
        } else {
            timer.start();
        }
    }
}
