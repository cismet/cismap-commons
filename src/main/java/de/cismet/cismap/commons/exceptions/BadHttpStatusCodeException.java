/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * BadHttpStatusCodeException.java
 *
 * Created on 19. Oktober 2006, 10:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.exceptions;

import java.awt.EventQueue;

import javax.swing.SwingUtilities;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class BadHttpStatusCodeException extends Exception {

    //~ Instance fields --------------------------------------------------------

    int statuscode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of BadHttpStatusCodeException.
     */
    public BadHttpStatusCodeException() {
        super();
    }

    /**
     * Creates a new BadHttpStatusCodeException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public BadHttpStatusCodeException(final String message) {
        super(message);
    }

    /**
     * Creates a new BadHttpStatusCodeException object.
     *
     * @param  message     DOCUMENT ME!
     * @param  statuscode  DOCUMENT ME!
     */
    public BadHttpStatusCodeException(final String message, final int statuscode) {
        super(message);
        this.statuscode = statuscode;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getHttpStatuscode() {
        return statuscode;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": " + statuscode; // NOI18N
    }
}
