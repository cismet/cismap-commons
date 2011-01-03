/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AuthenticationCanceledException.java
 *
 * Created on 19. Oktober 2006, 09:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.exceptions;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class AuthenticationCanceledException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AuthenticationCanceledException.
     */
    public AuthenticationCanceledException() {
        super(org.openide.util.NbBundle.getMessage(
                AuthenticationCanceledException.class,
                "AuthenticationCanceledException.AuthenticationCanceledException()")); // NOI18N
    }

    /**
     * Creates a new AuthenticationCanceledException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public AuthenticationCanceledException(final String message) {
        super(message);
    }
}
