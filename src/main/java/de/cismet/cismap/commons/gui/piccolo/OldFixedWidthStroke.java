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

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class OldFixedWidthStroke extends BasicStroke {

    //~ Instance fields --------------------------------------------------------

    protected float multiplyer = 1.0f;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public float getLineWidth() {
        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
            // log.fatal("LineWidth:"+super.getLineWidth() / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale());
            return super.getLineWidth() * multiplyer / (float)PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
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
