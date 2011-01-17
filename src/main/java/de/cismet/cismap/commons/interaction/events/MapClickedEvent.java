/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

import edu.umd.cs.piccolo.event.PInputEvent;

import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class MapClickedEvent {

    //~ Instance fields --------------------------------------------------------

    double xCoord = -1d;
    double yCoord = -1d;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
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
        final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();
        xCoord = mc.getWtst().getSourceX(pInputEvent.getPosition().getX() - mc.getClip_offset_x());
        yCoord = mc.getWtst().getSourceY(pInputEvent.getPosition().getY() - mc.getClip_offset_y());
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getxCoord() {
        return xCoord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getyCoord() {
        return yCoord;
    }
}
