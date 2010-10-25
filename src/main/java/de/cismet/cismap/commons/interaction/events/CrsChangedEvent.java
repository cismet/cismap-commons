package de.cismet.cismap.commons.interaction.events;

import de.cismet.cismap.commons.Crs;

/**
 *
 * @author therter
 */
public class CrsChangedEvent {
    private Crs formerCrs;
    private Crs currentCrs;

    public CrsChangedEvent(Crs formerCrs, Crs currentCrs) {
        this.formerCrs = formerCrs;
        this.currentCrs = currentCrs;
    }


    /**
     * @return the formerCrs
     */
    public Crs getFormerCrs() {
        return formerCrs;
    }

    /**
     * @return the currentCrs
     */
    public Crs getCurrentCrs() {
        return currentCrs;
    }
}