/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction;

import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface ActiveLayerListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerAdded(ActiveLayerEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerRemoved(ActiveLayerEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerPositionChanged(ActiveLayerEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerVisibilityChanged(ActiveLayerEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerAvailabilityChanged(ActiveLayerEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerInformationStatusChanged(ActiveLayerEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerSelectionChanged(ActiveLayerEvent e);
}
