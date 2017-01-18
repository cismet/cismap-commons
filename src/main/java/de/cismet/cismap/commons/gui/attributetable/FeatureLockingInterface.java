/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.attributetable;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * This interface is used to lock and unlock features. See also {@link FeatureLockerFactory}
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureLockingInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * locks the given feature.
     *
     * @param   feature                      the feature that should be locked
     * @param   multiLockForSameUserAllowed  true, iff it is allowed to lock an object that is already locked by this
     *                                       user
     *
     * @return  The unlock object for the given feature. This can be used with {@link unlock(Object)}
     *
     * @throws  LockAlreadyExistsException  if the given feature is already locked
     * @throws  Exception                   if the locking failed for some reason
     */
    Object lock(final Feature feature, boolean multiLockForSameUserAllowed) throws LockAlreadyExistsException,
        Exception;

    /**
     * locks the given feature service.
     *
     * @param   service                      the service that should be locked
     * @param   multiLockForSameUserAllowed  true, iff it is allowed to lock an object that is already locked by this
     *                                       user
     *
     * @return  The unlock object for the given service. This can be used with {@link unlock(Object)}
     *
     * @throws  LockAlreadyExistsException  if at least one feature of the given service is already locked
     * @throws  Exception                   if the locking failed for some reason
     */
    Object lock(final AbstractFeatureService service, boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception;

    /**
     * Unlocks the feature, that is associated with the given unlock-object.
     *
     * @param   unlockObject  the unlock object from {@link lock(Feature)}
     *
     * @throws  Exception  if the unlock process failed
     */
    void unlock(final Object unlockObject) throws Exception;

    /**
     * Returns the supported feature services.
     *
     * @return  the supported feature services
     */
    Class[] getSupportedFeatureServiceClasses();
}
