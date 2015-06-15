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
package de.cismet.cismap.commons.features;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface InfoNodeAwareFeature {

    //~ Methods ----------------------------------------------------------------

    /**
     * the info node panel will not be shown, if false this method returns false.
     *
     * @return  true, iff the info node panel should be shown for the feature.
     */
    boolean hasInfoNode();
}
