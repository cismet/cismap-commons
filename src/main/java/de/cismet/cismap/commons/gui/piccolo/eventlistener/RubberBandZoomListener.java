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

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.event.ActionListener;

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

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final Timer timer;
    private ActionListener zoomListener = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RubberBandZoomListener object.
     */
    public RubberBandZoomListener() {
        timer = new Timer(500, null);
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
//                    map.getHandleLayer().removeAllChildren();
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
//        if (e.getComponent() instanceof MappingComponent) {
//            if (((MappingComponent) e.getComponent()).getAnimating()) {
        final PBounds b = new PBounds();
        final double scale = factor;
        final double oldWidth = e.getCamera().getViewBounds().getWidth();
        final double newWidth = oldWidth * (1 - scale + 1);
        final double oldHeight = e.getCamera().getViewBounds().getHeight();
        final double newHeight = oldHeight * (1 - scale + 1);

        final double offsetX = (newWidth - oldWidth) / 2;
        final double offsetY = (newHeight - oldHeight) / 2;
//        if (e.getComponent() instanceof MappingComponent) {
//            MappingComponent map = (MappingComponent) e.getComponent();
//            map.getHandleLayer().removeAllChildren();
//        }
        // Offsetverschiebung sorgt daf\u00FCr das der Punkt auf den man geclickt hat an der gleichen Stelle bleibt
        b.setOrigin(e.getCamera().getViewBounds().getOrigin().getX() - offsetX,
            e.getCamera().getViewBounds().getOrigin().getY()
                    - offsetY); // );
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
