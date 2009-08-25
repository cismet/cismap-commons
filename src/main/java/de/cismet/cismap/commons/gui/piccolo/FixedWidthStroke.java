/*
 * FixedWidthStroke.java
 *
 * Created on 16. M\u00E4rz 2005, 15:55
 */
package de.cismet.cismap.commons.gui.piccolo;

import java.awt.BasicStroke;

/**
 *
 * @author hell
 */
public class FixedWidthStroke extends BasicStroke {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected float multiplyer = 1.0f;

    @Override
    public float getLineWidth() {
        return 0.001f;
//        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
//            //log.fatal("LineWidth:"+super.getLineWidth() / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale());
//            return super.getLineWidth()*multiplyer / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
//        }
//        else {
//            return super.getLineWidth()*multiplyer;
//        }
    }

    public void setMultiplyer(float multiplyer) {
        this.multiplyer = multiplyer;
    }
}
