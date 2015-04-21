/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.layerwidget;

import java.util.EventObject;

import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ThemeLayerEvent extends EventObject {

    //~ Instance fields --------------------------------------------------------

    private MapService layer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ThemeLayerEvent object.
     *
     * @param  source  DOCUMENT ME!
     */
    public ThemeLayerEvent(final Object source) {
        super(source);
    }

    /**
     * Creates a new ThemeLayerEvent object.
     *
     * @param  layer   DOCUMENT ME!
     * @param  source  DOCUMENT ME!
     */
    public ThemeLayerEvent(final MapService layer, final Object source) {
        super(source);
        this.layer = layer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MapService getLayer() {
        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public void setLayer(final MapService layer) {
        this.layer = layer;
    }
}
