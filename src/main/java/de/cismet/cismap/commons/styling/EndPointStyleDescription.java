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
package de.cismet.cismap.commons.styling;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class EndPointStyleDescription {

    //~ Instance fields --------------------------------------------------------

    private final String className;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new EndPointStyleDescription object.
     *
     * @param  className  DOCUMENT ME!
     */
    public EndPointStyleDescription(final String className) {
        this.className = className;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClassName() {
        return className;
    }
}
