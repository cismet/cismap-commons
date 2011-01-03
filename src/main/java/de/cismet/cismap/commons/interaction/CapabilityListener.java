/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction;

import de.cismet.cismap.commons.interaction.events.CapabilityEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface CapabilityListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void serverChanged(CapabilityEvent e);
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    void layerChanged(CapabilityEvent e);
}
