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
package de.cismet.cismap.commons;

import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class SimpleGetFeatureInfoUrl extends SimpleWmsGetMapUrl {

    //~ Static fields/initializers ---------------------------------------------

    public static final String X_TOKEN = "<cismap:x>"; // NOI18N
    public static final String Y_TOKEN = "<cismap:y>"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    int x = 0;
    int y = 0;
    private String xToken;
    private String yToken;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleGetFeatureInfoUrl object.
     *
     * @param  urlTemplate  DOCUMENT ME!
     */
    public SimpleGetFeatureInfoUrl(final String urlTemplate) {
        super(urlTemplate);
        xToken = X_TOKEN;
        yToken = Y_TOKEN;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getX() {
        return x;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x  DOCUMENT ME!
     */
    public void setX(final int x) {
        this.x = x;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getY() {
        return y;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y  DOCUMENT ME!
     */
    public void setY(final int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        String url = super.toString();
        url = url.replaceAll(xToken, x + ""); // NOI18N
        url = url.replaceAll(yToken, y + ""); // NOI18N
        return url;
    }
}
