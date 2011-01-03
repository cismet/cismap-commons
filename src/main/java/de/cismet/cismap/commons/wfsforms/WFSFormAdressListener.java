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
package de.cismet.cismap.commons.wfsforms;

import de.cismet.cismap.commons.BoundingBox;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
public interface WFSFormAdressListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void wfsFormAdressStreetSelected();
    /**
     * DOCUMENT ME!
     */
    void wfsFormAdressNrSelected();
    /**
     * DOCUMENT ME!
     *
     * @param  addressBB  DOCUMENT ME!
     */
    void wfsFormAddressPositioned(BoundingBox addressBB);
}
