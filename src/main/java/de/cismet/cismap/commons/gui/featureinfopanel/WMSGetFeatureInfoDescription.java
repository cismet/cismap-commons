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
package de.cismet.cismap.commons.gui.featureinfopanel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import edu.umd.cs.piccolo.event.PInputEvent;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WMSGetFeatureInfoDescription implements Feature {

    //~ Instance fields --------------------------------------------------------

    private WMSLayer layer;
    private WMSServiceLayer service;
    private Geometry geometry;
    private PInputEvent pInputEvent;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WMSGetFeatureInfoDescription object.
     */
    public WMSGetFeatureInfoDescription() {
    }

    /**
     * Creates a new WMSGetFeatureInfoDescription object.
     *
     * @param  geometry     DOCUMENT ME!
     * @param  pInputEvent  DOCUMENT ME!
     * @param  layer        DOCUMENT ME!
     * @param  service      DOCUMENT ME!
     */
    public WMSGetFeatureInfoDescription(final Geometry geometry,
            final PInputEvent pInputEvent,
            final WMSLayer layer,
            final WMSServiceLayer service) {
        this.layer = layer;
        this.service = service;
        this.geometry = geometry;
        this.pInputEvent = pInputEvent;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the x
     */
    public int getX() {
        return (int)getpInputEvent().getCanvasPosition().getX();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the y
     */
    public int getY() {
        return (int)getpInputEvent().getCanvasPosition().getY();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the layer
     */
    public WMSLayer getLayer() {
        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  the layer to set
     */
    public void setLayer(final WMSLayer layer) {
        this.layer = layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the service
     */
    public WMSServiceLayer getService() {
        return service;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  the service to set
     */
    public void setService(final WMSServiceLayer service) {
        this.service = service;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Geometry geom) {
        this.geometry = geometry;
    }

    @Override
    public boolean canBeSelected() {
        return true;
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(final boolean editable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void hide(final boolean hiding) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the pInputEvent
     */
    public PInputEvent getpInputEvent() {
        return pInputEvent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  the pInputEvent to set
     */
    public void setpInputEvent(final PInputEvent pInputEvent) {
        this.pInputEvent = pInputEvent;
    }

    @Override
    public String toString() {
        String label = service.getName();
        
        if (geometry != null && geometry instanceof Point) {
            label += round(((Point)geometry).getX()) + ", " + round(((Point)geometry).getY());
        }
        
        return label;
    }
    
    private String round(double value) {
        int precision = (int)Math.log10(value) + 3;
        return new BigDecimal(value).round(new MathContext(precision, RoundingMode.HALF_EVEN)).toPlainString();
    }
}
