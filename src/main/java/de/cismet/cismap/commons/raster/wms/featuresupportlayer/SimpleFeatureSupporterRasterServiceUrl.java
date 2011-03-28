/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms.featuresupportlayer;

import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleFeatureSupporterRasterServiceUrl extends SimpleWmsGetMapUrl {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FILTER_TOKEN = "<cismap:filterString>"; // NOI18N
    public static final String SRS_TOKEN = "<cismap:srs>";             // NOI18N

    //~ Instance fields --------------------------------------------------------

    private String filter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleFeatureSupporterRasterServiceUrl object.
     *
     * @param  urlTemplate  DOCUMENT ME!
     */
    public SimpleFeatureSupporterRasterServiceUrl(final String urlTemplate) {
        super(urlTemplate);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        String retValue;
        retValue = super.toString();
        if (filter != null) {
            retValue = retValue.replaceAll(FILTER_TOKEN, filter);
        }

        // we can always replace all since the code is always present, requests without SRS_TOKEN won't be affected
        retValue = retValue.replaceAll(SRS_TOKEN, CismapBroker.getInstance().getSrs().getCode());

        return retValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFilter() {
        return filter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  filter  DOCUMENT ME!
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    // TODO: these two method should probably be available in the super implementation
    @Override
    public boolean equals(final Object o) {
        return (o instanceof SimpleFeatureSupporterRasterServiceUrl)
                    && ((SimpleFeatureSupporterRasterServiceUrl)o).urlTemplate.equals(this.urlTemplate);
    }

    @Override
    public int hashCode() {
        return this.urlTemplate.hashCode();
    }
}
