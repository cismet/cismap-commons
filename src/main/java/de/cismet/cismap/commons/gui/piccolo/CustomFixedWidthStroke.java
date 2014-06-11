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

import edu.umd.cs.piccolo.util.PPaintContext;

import java.awt.BasicStroke;

import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class CustomFixedWidthStroke extends BasicStroke {

    //~ Instance fields --------------------------------------------------------

    protected float multiplyer = 1.0f;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private final MappingComponent mc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  thickness  DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float thickness) {
        this(thickness, null);
    }

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  thickness  DOCUMENT ME!
     * @param  mc         DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float thickness, final MappingComponent mc) {
        setMultiplyer(thickness);
        this.mc = mc;
    }

    /**
     * Privater Defaultkonstruktor damit FixedWidthStroke in diesem Fall verwendet wird.
     */
    private CustomFixedWidthStroke() {
        this.mc = null;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public float getLineWidth() {
        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
            // log.fatal("LineWidth:"+super.getLineWidth() / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale());
            if (mc != null) {
                return super.getLineWidth() * multiplyer * (float)mc.getStickyFeatureCorrectionFactor()
                            / (float)mc.getCamera().getViewScale();
            } else {
                return super.getLineWidth() * multiplyer / (float)PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
            }
        } else {
            return super.getLineWidth() * multiplyer;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  multiplyer  DOCUMENT ME!
     */
    public void setMultiplyer(final float multiplyer) {
        this.multiplyer = multiplyer;
    }

    @Override
    public int getEndCap() {
        return this.CAP_ROUND;
    }

    @Override
    public int getLineJoin() {
        return this.JOIN_ROUND;
    }
}
