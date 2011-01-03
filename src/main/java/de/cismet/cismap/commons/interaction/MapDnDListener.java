/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction;

import de.cismet.cismap.commons.interaction.events.MapDnDEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface MapDnDListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mde  DOCUMENT ME!
     */
    void dropOnMap(MapDnDEvent mde);
    /**
     * DOCUMENT ME!
     *
     * @param  mde  DOCUMENT ME!
     */
    void dragOverMap(MapDnDEvent mde);
}
