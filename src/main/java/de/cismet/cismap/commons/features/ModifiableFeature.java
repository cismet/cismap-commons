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
package de.cismet.cismap.commons.features;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface ModifiableFeature extends Feature {

    //~ Methods ----------------------------------------------------------------

    /**
     * Saves the feature
     *
     * @return  the reloaded feature
     *
     * @throws  Exception  DOCUMENT ME!
     */
    FeatureServiceFeature saveChanges() throws Exception;

    /**
     * Deletes the feature
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void delete() throws Exception;

    /**
     * undo all changes since the last save
     */
    void undoAll();
}
