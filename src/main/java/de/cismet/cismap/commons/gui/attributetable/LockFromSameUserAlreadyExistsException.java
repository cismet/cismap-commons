/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.attributetable;

/**
 * This exception will be thrown, when an object that is already locked, should be locked again.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LockFromSameUserAlreadyExistsException extends LockAlreadyExistsException {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LockAlreadyExistsException object.
     *
     * @param  message      a general description
     * @param  lockMessage  the user who contains the already existing lock, for example
     */
    public LockFromSameUserAlreadyExistsException(final String message, final String lockMessage) {
        super(message, lockMessage);
    }
}
