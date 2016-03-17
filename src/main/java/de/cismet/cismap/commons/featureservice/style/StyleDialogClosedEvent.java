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
package de.cismet.cismap.commons.featureservice.style;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StyleDialogClosedEvent {

    //~ Instance fields --------------------------------------------------------

    private StyleDialogInterface styleDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StyleDialogClosedEvent object.
     *
     * @param  styleDialog  DOCUMENT ME!
     */
    public StyleDialogClosedEvent(final StyleDialogInterface styleDialog) {
        this.styleDialog = styleDialog;
    }
}
