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
package de.cismet.cismap.commons.retrieval;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class RepaintEvent {

    //~ Instance fields --------------------------------------------------------

    private Object source;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RepaintEvent object.
     *
     * @param  source  DOCUMENT ME!
     */
    public RepaintEvent(final Object source) {
        this.source = source;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getSource() {
        return source;
    }
}
