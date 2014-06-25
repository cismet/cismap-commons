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

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * DOCUMENT ME!
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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureLockerFactory getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureService  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureLockingInterface getLockerForFeatureService(final AbstractFeatureService featureService) {
        return locker.get(featureService.getClass());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        static final FeatureLockerFactory INSTANCE = new FeatureLockerFactory();
    }
}
