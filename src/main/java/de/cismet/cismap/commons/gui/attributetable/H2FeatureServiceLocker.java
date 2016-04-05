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
package de.cismet.cismap.commons.gui.attributetable;

import org.apache.log4j.Logger;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = FeatureLockingInterface.class)
public class H2FeatureServiceLocker implements FeatureLockingInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(H2FeatureServiceLocker.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object lock(final Feature feature, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
        if ((feature instanceof JDBCFeature)
                    && (((JDBCFeature)feature).getLayerProperties().getFeatureService() instanceof H2FeatureService)) {
            final JDBCFeature jdbcFeature = (JDBCFeature)feature;
            try {
                final H2FeatureService service = (H2FeatureService)jdbcFeature.getLayerProperties().getFeatureService();

                try {
                    H2FeatureService.lockFeature(jdbcFeature.getId(), service.getTableName());
                } catch (LockFromSameUserAlreadyExistsException e) {
                    if (!multiLockForSameUserAllowed) {
                        throw e;
                    }
                }

                return new Lock(jdbcFeature.getId(), service.getTableName());
            } catch (LockAlreadyExistsException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("Error while creating lock object", e);
                throw new Exception("Cannot lock object");
            }
        }

        throw new IllegalArgumentException("Only JDBCFeature from H2FeatureServices are supported");
    }

    @Override
    public Object lock(final AbstractFeatureService service, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
        if (service instanceof H2FeatureService) {
            try {
                final H2FeatureService h2Service = (H2FeatureService)service;

                try {
                    H2FeatureService.lockFeature(null, h2Service.getTableName());
                } catch (LockFromSameUserAlreadyExistsException e) {
                    if (!multiLockForSameUserAllowed) {
                        throw e;
                    }
                }

                return new Lock(null, h2Service.getTableName());
            } catch (LockAlreadyExistsException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("Error while creating lock object", e);
                throw new Exception("Cannot lock object");
            }
        }

        throw new IllegalArgumentException("Only H2FeatureServices are supported");
    }

    @Override
    public void unlock(final Object unlockObject) throws Exception {
        if (unlockObject instanceof Lock) {
            try {
                final Lock lock = (Lock)unlockObject;

                H2FeatureService.unlockFeature(lock.getFeatureId(), lock.getTableName());
            } catch (Exception e) {
                LOG.error("Error while creating lock object", e);
                throw new Exception("Cannot lock object");
            }
        } else {
            throw new IllegalArgumentException("No supported unlock object");
        }
    }

    @Override
    public Class[] getSupportedFeatureServiceClasses() {
        return new Class[] { H2FeatureService.class };
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Lock {

        //~ Instance fields ----------------------------------------------------

        private final String tableName;
        private final Integer featureId;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Lock object.
         *
         * @param  featureId  DOCUMENT ME!
         * @param  tableName  DOCUMENT ME!
         */
        public Lock(final Integer featureId, final String tableName) {
            this.featureId = featureId;
            this.tableName = tableName;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the tableName
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the featureId
         */
        public Integer getFeatureId() {
            return featureId;
        }
    }
}
