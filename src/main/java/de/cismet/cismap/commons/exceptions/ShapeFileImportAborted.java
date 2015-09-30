/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.exceptions;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShapeFileImportAborted extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFileImportAborted object.
     */
    public ShapeFileImportAborted() {
    }

    /**
     * Creates a new ShapeFileImportAborted object.
     *
     * @param  message  DOCUMENT ME!
     */
    public ShapeFileImportAborted(final String message) {
        super(message);
    }
}
