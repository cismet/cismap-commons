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
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class PFixedTexturePaint implements java.awt.Paint, PSticky {

    //~ Instance fields --------------------------------------------------------

    java.awt.TexturePaint paint;
    BufferedImage img;
    Rectangle2D anchor;
    PNode parent;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PFixedTexturePaint object.
     *
     * @param  img     DOCUMENT ME!
     * @param  anchor  DOCUMENT ME!
     * @param  parent  DOCUMENT ME!
     */
    public PFixedTexturePaint(final BufferedImage img, final Rectangle2D anchor, final PNode parent) {
        this.img = img;
        this.anchor = anchor;
        paint = new TexturePaint(img, anchor);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public PaintContext createContext(final ColorModel cm,
            final Rectangle deviceBounds,
            final Rectangle2D userBounds,
            final AffineTransform xform,
            final RenderingHints hints) {
        return paint.createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    @Override
    public int getTransparency() {
        return paint.getTransparency();
    }

    @Override
    public boolean getVisible() {
        if (parent == null) {
            return false;
        }
        return parent.getVisible();
    }

    @Override
    public PNode getParent() {
        return parent;
    }

    @Override
    public void setScale(final double scale) {
        paint = new TexturePaint(
                img,
                new Rectangle2D.Double(
                    anchor.getX()
                            * scale,
                    anchor.getY()
                            * scale,
                    anchor.getWidth()
                            * scale,
                    anchor.getHeight()));
    }
}
