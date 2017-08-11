/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

/**
 * Will be used, if the interaction mode of the map has changed and the old interaction mode will be removed.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface DeregistrationListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * Can be used to cleanup.
     */
    void deregistration();
}
