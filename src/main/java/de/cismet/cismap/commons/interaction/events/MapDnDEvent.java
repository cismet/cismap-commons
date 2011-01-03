/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

import java.awt.dnd.DropTargetEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class MapDnDEvent {

    //~ Instance fields --------------------------------------------------------

    private DropTargetEvent dte;
    private double xPos;
    private double yPos;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MapDnDEvent.
     */
    public MapDnDEvent() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DropTargetEvent getDte() {
        return dte;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dte  DOCUMENT ME!
     */
    public void setDte(final DropTargetEvent dte) {
        this.dte = dte;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getXPos() {
        return xPos;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  xPos  DOCUMENT ME!
     */
    public void setXPos(final double xPos) {
        this.xPos = xPos;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getYPos() {
        return yPos;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  yPos  DOCUMENT ME!
     */
    public void setYPos(final double yPos) {
        this.yPos = yPos;
    }
}
