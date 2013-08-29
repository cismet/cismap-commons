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

    private MappingComponent mc = null;

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
        super(1.0f, CAP_ROUND, JOIN_ROUND);
        setMultiplyer(thickness);
        this.mc = mc;
    }

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  width       DOCUMENT ME!
     * @param  lineCap     DOCUMENT ME!
     * @param  lineJoin    DOCUMENT ME!
     * @param  miterlimit  DOCUMENT ME!
     * @param  dash        DOCUMENT ME!
     * @param  dash_phase  DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float width,
            final int lineCap,
            final int lineJoin,
            final float miterlimit,
            final float[] dash,
            final float dash_phase) {
        super(width, lineCap, lineJoin, miterlimit, dash, dash_phase);
    }

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  width       DOCUMENT ME!
     * @param  lineCap     DOCUMENT ME!
     * @param  lineJoin    DOCUMENT ME!
     * @param  miterlimit  DOCUMENT ME!
     * @param  dash        DOCUMENT ME!
     * @param  dash_phase  DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float width,
            final int lineCap,
            final int lineJoin,
            final float miterlimit,
            final float[] dash,
            final float dash_phase) {
        super(width, lineCap, lineJoin, miterlimit, dash, dash_phase);
    }

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  width       DOCUMENT ME!
     * @param  lineCap     DOCUMENT ME!
     * @param  lineJoin    DOCUMENT ME!
     * @param  miterlimit  DOCUMENT ME!
     * @param  dash        DOCUMENT ME!
     * @param  dash_phase  DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float width,
            final int lineCap,
            final int lineJoin,
            final float miterlimit,
            final float[] dash,
            final float dash_phase) {
        super(width, lineCap, lineJoin, miterlimit, dash, dash_phase);
    }

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  width       DOCUMENT ME!
     * @param  lineCap     DOCUMENT ME!
     * @param  lineJoin    DOCUMENT ME!
     * @param  miterlimit  DOCUMENT ME!
     * @param  dash        DOCUMENT ME!
     * @param  dash_phase  DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float width,
            final int lineCap,
            final int lineJoin,
            final float miterlimit,
            final float[] dash,
            final float dash_phase) {
        super(width, lineCap, lineJoin, miterlimit, dash, dash_phase);
    }

    /**
     * Creates a new CustomFixedWidthStroke object.
     *
     * @param  width       DOCUMENT ME!
     * @param  lineCap     DOCUMENT ME!
     * @param  lineJoin    DOCUMENT ME!
     * @param  miterlimit  DOCUMENT ME!
     * @param  dash        DOCUMENT ME!
     * @param  dash_phase  DOCUMENT ME!
     */
    public CustomFixedWidthStroke(final float width,
            final int lineCap,
            final int lineJoin,
            final float miterlimit,
            final float[] dash,
            final float dash_phase) {
        super(width, lineCap, lineJoin, miterlimit, dash, dash_phase);
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

    @Override
    public float getMiterLimit() {
        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
            final float ml = super.getMiterLimit() / (float)PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
            if (ml < 1.0f) {
                return 1.0f;
            } else {
                return ml;
            }
        } else {
            return 1.0f;
        }
    }

    @Override
    public float[] getDashArray() {
        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
            final float[] dash = super.getDashArray();
            if ((dash == null) || (dash.length == 0)) {
                return null;
            }
            final float scale = (float)PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
            final float[] temp = new float[dash.length];
            for (int i = dash.length - 1; i >= 0; i--) {
                temp[i] = dash[i] / scale;
            }
            return temp;
        } else {
            return super.getDashArray();
        }
    }

    @Override
    public float getDashPhase() {
        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
            return super.getDashPhase() / (float)PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
        } else {
            return super.getDashPhase(); // To change body of generated methods, choose Tools | Templates.
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

    /*@Override
     * public int getEndCap() { return this.CAP_ROUND; }
     *
     * @Override public int getLineJoin() { return this.JOIN_ROUND;}*/
}
