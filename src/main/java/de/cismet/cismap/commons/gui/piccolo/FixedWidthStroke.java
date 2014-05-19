/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FixedWidthStroke.java
 *
 * Created on 16. M\u00E4rz 2005, 15:55
 */
package de.cismet.cismap.commons.gui.piccolo;

import java.awt.BasicStroke;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class FixedWidthStroke extends BasicStroke implements Serializable {

    //~ Instance fields --------------------------------------------------------

    protected float multiplyer = 1.0f;
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public float getLineWidth() {
        return 0.0000000001f; // wegen wgs84
//        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
//            //log.fatal("LineWidth:"+super.getLineWidth() / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale());
//            return super.getLineWidth()*multiplyer / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
//        }
//        else {
//            return super.getLineWidth()*multiplyer;
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  multiplyer  DOCUMENT ME!
     */
    public void setMultiplyer(final float multiplyer) {
        this.multiplyer = multiplyer;
    }
}
