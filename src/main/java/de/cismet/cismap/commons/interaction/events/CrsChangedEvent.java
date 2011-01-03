/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

import de.cismet.cismap.commons.Crs;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CrsChangedEvent {

    //~ Instance fields --------------------------------------------------------

    private Crs formerCrs;
    private Crs currentCrs;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CrsChangedEvent object.
     *
     * @param  formerCrs   DOCUMENT ME!
     * @param  currentCrs  DOCUMENT ME!
     */
    public CrsChangedEvent(final Crs formerCrs, final Crs currentCrs) {
        this.formerCrs = formerCrs;
        this.currentCrs = currentCrs;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the formerCrs
     */
    public Crs getFormerCrs() {
        return formerCrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the currentCrs
     */
    public Crs getCurrentCrs() {
        return currentCrs;
    }
}
