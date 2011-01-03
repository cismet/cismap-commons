/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CantReadFromURLException.java
 *
 * Created on 19. Oktober 2006, 10:12
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
public class CannotReadFromURLException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CantReadFromURLException.
     */
    public CannotReadFromURLException() {
        super();
    }

    /**
     * Creates a new CannotReadFromURLException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public CannotReadFromURLException(final String message) {
        super(message);
    }
}
