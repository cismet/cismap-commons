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
package de.cismet.cismap.commons.gui.attributetable;

import de.cismet.cismap.commons.features.Feature;

import de.cismet.locking.exception.LockAlreadyExistsException;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureLockingInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   bean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  LockAlreadyExistsException  DOCUMENT ME!
     * @throws  Exception                   DOCUMENT ME!
     */
    Object lock(final Feature bean) throws LockAlreadyExistsException, Exception;
    /**
     * DOCUMENT ME!
     *
     * @param   bean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void unlock(final Object bean) throws Exception;
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Class[] getSupportedFeatureServiceClasses();
}
