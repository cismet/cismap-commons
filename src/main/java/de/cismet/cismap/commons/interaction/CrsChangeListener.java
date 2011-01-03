/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction;

import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface CrsChangeListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * This method will be invoked, after the crs was changed.
     *
     * @param  event  contains the old crs and the new crs. The old crs is null, if the new CRS is the first used crs
     *                (at the first invocation of this method).
     */
    void crsChanged(CrsChangedEvent event);
}
