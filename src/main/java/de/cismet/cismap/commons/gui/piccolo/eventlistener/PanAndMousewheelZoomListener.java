/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Pan listener with mousewheel zoom.
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class PanAndMousewheelZoomListener extends BackgroundRefreshingPanEventListener {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final RubberBandZoomListener zoomDelegate;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PanAndMousewheelZoomListener object.
     */
    public PanAndMousewheelZoomListener() {
        zoomDelegate = new RubberBandZoomListener();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseWheelRotated(final PInputEvent pInputEvent) {
        zoomDelegate.mouseWheelRotated(pInputEvent);
    }
}
