package de.cismet.cismap.commons.interaction;

import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;

/**
 *
 * @author therter
 */
public interface CrsChangeListener {
    /**
     * This method will be invoked, after the crs was changed.
     * @param event contains the old crs and the new crs. The old crs is null, if
     *              the new CRS is the first used crs (at the first invocation of this method).
     */
    public void crsChanged(CrsChangedEvent event);
}