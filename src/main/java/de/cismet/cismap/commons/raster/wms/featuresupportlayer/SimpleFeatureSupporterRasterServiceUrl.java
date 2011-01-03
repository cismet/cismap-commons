/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms.featuresupportlayer;

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

    //~ Instance fields --------------------------------------------------------

    private String filterToken;
    private String filter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleFeatureSupporterRasterServiceUrl object.
     *
     * @param  urlTemplate  DOCUMENT ME!
     */
    public SimpleFeatureSupporterRasterServiceUrl(final String urlTemplate) {
        super(urlTemplate);
        filterToken = FILTER_TOKEN;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        String retValue;
        retValue = super.toString();
        retValue = retValue.replaceAll(filterToken, filter);
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

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final SimpleFeatureSupporterRasterServiceUrl u = new SimpleFeatureSupporterRasterServiceUrl(
                "http://s102w2k1.wuppertal-intra.de/wunda_dk_v61/isserver/ims/scripts/ShowMap.pl?datasource=erhebungsflaechen&VERSION=1.1.1&REQUEST=GetMap&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_inimage&LAYERS=09_2&STYLES=farbe_altabl&<cismap:filterString>"); // NOI18N
        u.setFilter("Testfilter");                                                                                                                                                                                                                                                                                                                                                                    // NOI18N
        u.setX1(0.1);
        u.setX2(0.2);
        u.setY1(0.3);
        u.setY2(0.4);
        u.setHeight(1);
        u.setWidth(1000);
        System.out.println(u);
    }

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
