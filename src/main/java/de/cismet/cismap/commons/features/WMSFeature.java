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
package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WMSFeature extends WFSFeature {

    //~ Instance fields --------------------------------------------------------

    private WMSServiceLayer WMSServiceLayer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WMSFeature object.
     *
     * @param  WMSServiceLayer  DOCUMENT ME!
     */
    public WMSFeature(final WMSServiceLayer WMSServiceLayer) {
        super();
        this.WMSServiceLayer = WMSServiceLayer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the WMSServiceLayer
     */
    public WMSServiceLayer getWMSServiceLayer() {
        return WMSServiceLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  WMSServiceLayer  the WMSServiceLayer to set
     */
    public void setWMSServiceLayer(final WMSServiceLayer WMSServiceLayer) {
        this.WMSServiceLayer = WMSServiceLayer;
    }
}
