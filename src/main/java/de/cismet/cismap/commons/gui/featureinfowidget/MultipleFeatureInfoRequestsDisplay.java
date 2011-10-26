/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

import java.util.Collection;

import de.cismet.cismap.commons.features.SignaturedFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.HoldListener;

/**
 * DOCUMENT ME!
 *
 * @author   dmeiers
 * @version  $Revision$, $Date$
 */
public interface MultipleFeatureInfoRequestsDisplay {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isOnHold();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<SignaturedFeature> getHoldFeatures();

    /**
     * DOCUMENT ME!
     *
     * @param  o  DOCUMENT ME!
     */
    void addHoldListener(HoldListener o);

    /**
     * DOCUMENT ME!
     *
     * @param  o  DOCUMENT ME!
     */
    void removeHoldListener(HoldListener o);

    /**
     * DOCUMENT ME!
     *
     * @param  aFlag  DOCUMENT ME!
     */
    void setDisplayVisble(boolean aFlag);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isDisplayVisible();
}
