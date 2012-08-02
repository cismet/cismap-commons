/*
 * FixedWidthStroke.java
 *
 * Created on 16. M\u00E4rz 2005, 15:55
 */
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.util.PPaintContext;
import java.awt.BasicStroke;

/**
 *
 * @author hell
 */
public class CustomFixedWidthStroke extends BasicStroke {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected float multiplyer = 1.0f;
    
    
    
    /**
     * Privater Defaultkonstruktor damit FixedWidthStroke in diesem Fall verwendet wird
     */
    private CustomFixedWidthStroke(){
        
    }
    
    public CustomFixedWidthStroke(float thickness){
        setMultiplyer(thickness);
    }
    
    @Override
    public float getLineWidth() {
        if (PPaintContext.CURRENT_PAINT_CONTEXT != null) {
            //log.fatal("LineWidth:"+super.getLineWidth() / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale());
            return super.getLineWidth() * multiplyer / (float) PPaintContext.CURRENT_PAINT_CONTEXT.getScale();
        } else {
            return super.getLineWidth() * multiplyer;
        }
    }

    public void setMultiplyer(float multiplyer) {
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
