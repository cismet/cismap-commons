/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;
import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface RasterLayerSupportedFeature {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureAwareRasterService getSupportingRasterService();
    /**
     * DOCUMENT ME!
     *
     * @param  featureAwareRasterService  DOCUMENT ME!
     */
    void setSupportingRasterService(FeatureAwareRasterService featureAwareRasterService);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getFilterPart();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getSpecialLayerName();
}
