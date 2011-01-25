/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class InitialisationException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of <code>InitialisationException</code> without detail message.
     */
    public InitialisationException() {
    }

    /**
     * Constructs an instance of <code>InitialisationException</code> with the specified detail message.
     *
     * @param  msg  the detail message.
     */
    public InitialisationException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>InitialisationException</code> with the specified detail message and the
     * specified cause.
     *
     * @param  msg    the detail message.
     * @param  cause  the exception cause
     */
    public InitialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
