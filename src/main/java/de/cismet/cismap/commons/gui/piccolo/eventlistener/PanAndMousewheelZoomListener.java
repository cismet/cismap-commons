package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Pan listener with mousewheel zoom.
 * @author srichter
 */
public final class PanAndMousewheelZoomListener extends BackgroundRefreshingPanEventListener {

    public PanAndMousewheelZoomListener() {
        zoomDelegate = new RubberBandZoomListener();
    }
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final RubberBandZoomListener zoomDelegate;

    @Override
    public void mouseWheelRotated(PInputEvent pInputEvent) {
        zoomDelegate.mouseWheelRotated(pInputEvent);
    }
}

