/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.attributetable;

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * Uses the lookup netbeans mechanism to identify all available FeatureLockingInterface implementaions.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureLockerFactory {

    //~ Instance fields --------------------------------------------------------

    private final Map<Class, FeatureLockingInterface> locker = new HashMap<Class, FeatureLockingInterface>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureLockerFactory object.
     */
    private FeatureLockerFactory() {
        final Collection<? extends FeatureLockingInterface> lockerList = Lookup.getDefault()
                    .lookupAll(FeatureLockingInterface.class);

        for (final FeatureLockingInterface tmp : lockerList) {
            final Class[] c = tmp.getSupportedFeatureServiceClasses();

            for (final Class featureServiceClass : c) {
                locker.put(featureServiceClass, tmp);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * returns a new instance.
     *
     * @return  a new instance
     */
    public static FeatureLockerFactory getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * Returns a locker that supports the given feature service.
     *
     * @param   featureService  the feature service, the locker should support
     *
     * @return  a locker that supports the given feature service
     */
    public FeatureLockingInterface getLockerForFeatureService(final AbstractFeatureService featureService) {
        return locker.get(featureService.getClass());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * To realise the lazy initialised singleton implementation.
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        static final FeatureLockerFactory INSTANCE = new FeatureLockerFactory();
    }
}
