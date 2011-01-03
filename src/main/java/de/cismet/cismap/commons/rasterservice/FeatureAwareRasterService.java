/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.features.FeatureCollection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FeatureAwareRasterService extends RetrievalServiceLayer, MapService {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  featureCollection  DOCUMENT ME!
     */
    void setFeatureCollection(FeatureCollection featureCollection);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureCollection getFeatureCollection();
}
