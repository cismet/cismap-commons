/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.exceptions;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
public class UnknownDocumentException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UnknownDocumentException object.
     */
    public UnknownDocumentException() {
    }

    /**
     * Creates a new UnknownDocumentException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public UnknownDocumentException(final String message) {
        super(message);
    }
}
