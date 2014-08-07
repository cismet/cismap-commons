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
public class LockAlreadyExistsException extends Exception {

    //~ Instance fields --------------------------------------------------------

    private String lockMessage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LockAlreadyExistsException object.
     *
     * @param  message      a general description
     * @param  lockMessage  the user who contains the already existing lock, for example
     */
    public LockAlreadyExistsException(final String message, final String lockMessage) {
        super(message);
        this.lockMessage = lockMessage;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  lockMessage  DOCUMENT ME!
     */
    public void setLockMessage(final String lockMessage) {
        this.lockMessage = lockMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getLockMessage() {
        return lockMessage;
    }
}
