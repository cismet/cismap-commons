/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.PNode;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 *
 * @author mroncoroni
 */
public class PFixedTexturePaint implements java.awt.Paint, PSticky{

    java.awt.TexturePaint paint;
    BufferedImage img;
    Rectangle2D anchor;
    PNode parent;
    
    public PFixedTexturePaint(BufferedImage img, Rectangle2D anchor, PNode parent) {
        this.img = img;
        this.anchor = anchor;
        paint = new TexturePaint(img, anchor);
    }
    
    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        return paint.createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    @Override
    public int getTransparency() {
        return paint.getTransparency();
    }

    @Override
    public boolean getVisible() {
        if(parent == null)
            return false;
        return parent.getVisible();
    }

    @Override
    public PNode getParent() {
        return parent;
    }

    @Override
    public void setScale(double scale) {
        paint = new TexturePaint(img, new Rectangle2D.Double(anchor.getX() * scale, anchor.getY() * scale, anchor.getWidth() * scale, anchor.getHeight()));
    }
    
}
