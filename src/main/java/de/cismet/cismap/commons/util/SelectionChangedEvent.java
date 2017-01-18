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
package de.cismet.cismap.commons.util;

import java.util.EventObject;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectionChangedEvent extends EventObject {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionChangedEvent object.
     *
     * @param  source  DOCUMENT ME!
     */
    public SelectionChangedEvent(final Object source) {
        super(source);
    }
}
