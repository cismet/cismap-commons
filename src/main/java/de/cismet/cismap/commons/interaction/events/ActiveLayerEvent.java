/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ActiveLayerEvent {

    //~ Instance fields --------------------------------------------------------

    private int oldPosition;
    private int absolutePosition;
    private Object layer;
    private WMSCapabilities wmsCapabilities;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ActiveLayerEvent.
     */
    public ActiveLayerEvent() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getOldPosition() {
        return oldPosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  oldPosition  DOCUMENT ME!
     */
    public void setOldPosition(final int oldPosition) {
        this.oldPosition = oldPosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getAbsolutePosition() {
        return absolutePosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  absolutePosition  DOCUMENT ME!
     */
    public void setAbsolutePosition(final int absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getLayer() {
        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public void setLayer(final Object layer) {
        this.layer = layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WMSCapabilities getCapabilities() {
        return wmsCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilities  DOCUMENT ME!
     */
    public void setCapabilities(final WMSCapabilities capabilities) {
        this.wmsCapabilities = capabilities;
    }
}
