/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class MapClickedEvent {

    //~ Instance fields --------------------------------------------------------

    private PInputEvent pInputEvent = null;
    private Object objectUnderClick = null;
    private String mode = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MapClickedEvent.
     *
     * @param  mode         DOCUMENT ME!
     * @param  pInputEvent  DOCUMENT ME!
     */
    public MapClickedEvent(final String mode, final PInputEvent pInputEvent) {
        this.pInputEvent = pInputEvent;
        this.mode = mode;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getX() {
        return pInputEvent.getCanvasPosition().getX();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getY() {
        return pInputEvent.getCanvasPosition().getY();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClickCount() {
        return pInputEvent.getClickCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getObjectUnderClick() {
        return objectUnderClick;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectUnderClick  DOCUMENT ME!
     */
    public void setObjectUnderClick(final Object objectUnderClick) {
        this.objectUnderClick = objectUnderClick;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMode() {
        return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    public void setMode(final String mode) {
        this.mode = mode;
    }
}
