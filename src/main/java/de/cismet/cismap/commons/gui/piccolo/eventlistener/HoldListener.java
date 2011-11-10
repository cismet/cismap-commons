/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import java.util.Collection;

import de.cismet.cismap.commons.features.SignaturedFeature;

/**
 * DOCUMENT ME!
 *
 * @author   dmeiers
 * @version  $Revision$, $Date$
 */
public interface HoldListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  event  c DOCUMENT ME!
     */
    void holdFeaturesChanged(HoldFeatureChangeEvent event);
}
