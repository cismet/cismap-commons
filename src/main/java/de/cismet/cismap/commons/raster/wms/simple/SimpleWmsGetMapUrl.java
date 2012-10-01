/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleWmsGetMapUrl.java
 *
 * Created on 14. M\u00E4rz 2005, 16:00
 */
package de.cismet.cismap.commons.raster.wms.simple;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * Einfache Klasse um einen WMS Aufruf zu parametrisieren nur eine WMS String mitschneiden und dann einfach die Breite,
 * Hoehe und<br>
 * die Bounding Box durch eindeutige (und nicht zu kurze) Token ersetzen. Fertig :-)<br>
 * <br>
 * Bsp:<br>
 * <code>
 * http://geoportal.wuppertal.de/wms/wms?null&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&WIDTH=<cids:width>&HEIGHT=<cids:height>&BBOX=<cids:boundingBox>&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=false&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=R102:DGK5</code>
 * 
 * <br/><br/><b>WARNING: if you use the SRS_TOKEN be aware that the SRS is always fetched from the main mapping component!</b>
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class SimpleWmsGetMapUrl {

    //~ Static fields/initializers ---------------------------------------------

    public static final String WIDTH_TOKEN = "<cismap:width>";              // NOI18N
    public static final String HEIGHT_TOKEN = "<cismap:height>";            // NOI18N
    public static final String BOUNDING_BOX_TOKEN = "<cismap:boundingBox>"; // NOI18N

    // NOTE: not configurable
    public static final String BOUNDING_BOX_TOKEN_LL_X = "<cismap:boundingBox_ll_x>"; // NOI18N
    public static final String BOUNDING_BOX_TOKEN_LL_Y = "<cismap:boundingBox_ll_y>"; // NOI18N
    public static final String BOUNDING_BOX_TOKEN_UR_X = "<cismap:boundingBox_ur_x>"; // NOI18N
    public static final String BOUNDING_BOX_TOKEN_UR_Y = "<cismap:boundingBox_ur_y>"; // NOI18N
    public static final String SRS_TOKEN = "<cismap:srs>";                            // NOI18N

    public static final String EPSG_NAMESPACE = "http://www.opengis.net/gml/srs/epsg.xml"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected String urlTemplate;

    private double x1 = 0.0;
    private double y1 = 0.0;
    private double x2 = 0.0;
    private double y2 = 0.0;

    private String widthToken;
    private String heightToken;
    private String boundingBoxToken;
    private String payloadTemplate;

    private int width = 0;
    private int height = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleWmsGetMapUrl object.
     *
     * @param  urlTemplate  DOCUMENT ME!
     */
    public SimpleWmsGetMapUrl(final String urlTemplate) {
        this(urlTemplate, null);
    }

    /**
     * Creates a new SimpleWmsGetMapUrl object.
     *
     * @param  urlTemplate      DOCUMENT ME!
     * @param  payloadTemplate  DOCUMENT ME!
     */
    public SimpleWmsGetMapUrl(final String urlTemplate, final String payloadTemplate) {
        this(urlTemplate, WIDTH_TOKEN, HEIGHT_TOKEN, BOUNDING_BOX_TOKEN, payloadTemplate);
    }

    /**
     * Creates a new instance of SimpleWmsGetMapUrl.
     *
     * @param  urlTemplate       DOCUMENT ME!
     * @param  widthToken        DOCUMENT ME!
     * @param  heightToken       DOCUMENT ME!
     * @param  boundingBoxToken  DOCUMENT ME!
     */
    public SimpleWmsGetMapUrl(final String urlTemplate,
            final String widthToken,
            final String heightToken,
            final String boundingBoxToken) {
        this(urlTemplate, widthToken, heightToken, boundingBoxToken, null);
    }

    /**
     * Creates a new SimpleWmsGetMapUrl object.
     *
     * @param  urlTemplate       DOCUMENT ME!
     * @param  widthToken        DOCUMENT ME!
     * @param  heightToken       DOCUMENT ME!
     * @param  boundingBoxToken  DOCUMENT ME!
     * @param  payloadTemplate   DOCUMENT ME!
     */
    public SimpleWmsGetMapUrl(final String urlTemplate,
            final String widthToken,
            final String heightToken,
            final String boundingBoxToken,
            final String payloadTemplate) {
        this.urlTemplate = urlTemplate;
        this.widthToken = widthToken;
        this.heightToken = heightToken;
        this.boundingBoxToken = boundingBoxToken;
        this.payloadTemplate = payloadTemplate;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPayloadTemplate() {
        return payloadTemplate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  payloadTemplate  DOCUMENT ME!
     */
    public void setPayloadTemplate(final String payloadTemplate) {
        this.payloadTemplate = payloadTemplate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String createPayload() {
        if (payloadTemplate == null) {
            return null;
        }

        String payload = payloadTemplate;
        payload = payload.replaceAll(heightToken, String.valueOf(height));
        payload = payload.replaceAll(widthToken, String.valueOf(width));
        payload = payload.replaceAll(BOUNDING_BOX_TOKEN_LL_X, String.valueOf(x1));
        payload = payload.replaceAll(BOUNDING_BOX_TOKEN_LL_Y, String.valueOf(y1));
        payload = payload.replaceAll(BOUNDING_BOX_TOKEN_UR_X, String.valueOf(x2));
        payload = payload.replaceAll(BOUNDING_BOX_TOKEN_UR_Y, String.valueOf(y2));

        final String srsCode = CismapBroker.getInstance().getSrs().getCode();
        final String srs = srsCode.substring(srsCode.indexOf(':') + 1);
        payload = payload.replaceAll(SRS_TOKEN, EPSG_NAMESPACE + "#" + srs); // NOI18N

        return payload;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  width  DOCUMENT ME!
     */
    public void setWidth(final int width) {
        this.width = width;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  height  DOCUMENT ME!
     */
    public void setHeight(final int height) {
        this.height = height;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coord  DOCUMENT ME!
     */
    public void setX1(final double coord) {
        x1 = coord;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  coord  DOCUMENT ME!
     */
    public void setY1(final double coord) {
        y1 = coord;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  coord  DOCUMENT ME!
     */
    public void setX2(final double coord) {
        x2 = coord;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  coord  DOCUMENT ME!
     */
    public void setY2(final double coord) {
        y2 = coord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUrlTemplate() {
        return urlTemplate;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  urlTemplate  DOCUMENT ME!
     */
    public void setUrlTemplate(final String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public String toString() {
        // NOTE: the payload will not be inserted here, since this is the place where the getMap GET url is built
        String url = urlTemplate.replaceAll(widthToken, new Integer(width).toString());
        url = url.replaceAll(heightToken, new Integer(height).toString());
        url = url.replaceAll(
                boundingBoxToken,
                new Double(x1).toString()
                        + "," // NOI18N
                        + new Double(y1).toString()
                        + "," // NOI18N
                        + new Double(x2).toString()
                        + "," // NOI18N
                        + new Double(y2).toString());

        // we can always replace all since the code is always present, requests without SRS_TOKEN won't be affected
        url = url.replaceAll(SRS_TOKEN, CismapBroker.getInstance().getSrs().getCode());

        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getWidth() {
        return width;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getHeight() {
        return height;
    }
}
