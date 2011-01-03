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
public class FileExtensionContentMissmatchException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FileExtensionContentMissmatchException object.
     */
    public FileExtensionContentMissmatchException() {
    }

    /**
     * Creates a new FileExtensionContentMissmatchException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public FileExtensionContentMissmatchException(final String message) {
        super(message);
    }
}
