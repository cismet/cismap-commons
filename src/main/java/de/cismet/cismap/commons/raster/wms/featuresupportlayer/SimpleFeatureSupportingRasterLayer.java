/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms.featuresupportlayer;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleFeatureSupportingRasterLayer extends SimpleWMS implements FeatureAwareRasterService {

    //~ Instance fields --------------------------------------------------------

    FeatureCollection featureCollection;
    SimpleFeatureSupporterRasterServiceUrl sfu;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleFeatureSupportingRasterLayer object.
     *
     * @param  s  DOCUMENT ME!
     */
    public SimpleFeatureSupportingRasterLayer(final SimpleFeatureSupportingRasterLayer s) {
        super(s);
        featureCollection = s.featureCollection;
        sfu = s.sfu;
    }
    /**
     * Creates a new instance of SimpleFeatureSupportingRasterLayer.
     *
     * @param  sfu  DOCUMENT ME!
     */
    public SimpleFeatureSupportingRasterLayer(final SimpleFeatureSupporterRasterServiceUrl sfu) {
        super(sfu);
        this.sfu = sfu;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setFeatureCollection(final FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }

    @Override
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof SimpleFeatureSupportingRasterLayer)
                    && ((SimpleFeatureSupportingRasterLayer)o).sfu.equals(sfu);
    }

    @Override
    public int hashCode() {
        return sfu.hashCode();
    }

    @Override
    public Object clone() {
        return new SimpleFeatureSupportingRasterLayer(this);
    }
}
