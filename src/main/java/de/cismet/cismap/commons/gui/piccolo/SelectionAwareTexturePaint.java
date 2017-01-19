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
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * This class can be used as a TexturePaint that considers the selection status of the corresponding feature.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectionAwareTexturePaint implements Paint, Cloneable {
    private final double MIN_SIDE_LENGTH = 0.5;
    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum SelectionMode {

        //~ Enum constants -----------------------------------------------------

        HIGHLIGHTED, SELECTED, UNSELECTED
    }

    //~ Instance fields --------------------------------------------------------

    private final BufferedImage defaultImage;
    private final BufferedImage highlightedImage;
    private final BufferedImage selectedImage;
    private final Rectangle2D rec;
    private TexturePaint paint;
    private BufferedImage currentImage;
    private Rectangle2D currentRec;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionAwareTexturePaint object.
     *
     * @param  defaultImage      this image is used, if the corresponding feature is unselected and not highlighted
     * @param  highlightedImage  this image is used, if the corresponding feature is highlighted
     * @param  selectedImage     this image is used, if the corresponding feature is selected
     * @param  rec               the Rectangle2D in user space used to anchor and replicate the texture
     */
    public SelectionAwareTexturePaint(final BufferedImage defaultImage,
            final BufferedImage highlightedImage,
            final BufferedImage selectedImage,
            final Rectangle2D rec) {
        paint = new TexturePaint(defaultImage, rec);
        this.rec = rec;
        this.currentRec = rec;
        this.defaultImage = defaultImage;
        this.highlightedImage = highlightedImage;
        this.selectedImage = selectedImage;
        this.currentImage = defaultImage;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TexturePaint getTexturePaint() {
        return paint;
    }

    /**
     * Set the selection mode to highlighted, selected or unselected.
     *
     * @param  mode  the new selection mode
     */
    public void setMode(final SelectionMode mode) {
        switch (mode) {
            case HIGHLIGHTED: {
                paint = new TexturePaint(highlightedImage, currentRec);
                this.currentImage = highlightedImage;
                break;
            }
            case SELECTED: {
                paint = new TexturePaint(selectedImage, currentRec);
                this.currentImage = selectedImage;
                break;
            }
            case UNSELECTED: {
                paint = new TexturePaint(defaultImage, currentRec);
                this.currentImage = defaultImage;
                break;
            }
        }
    }

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
    public Object clone() {
        final SelectionAwareTexturePaint clone = new SelectionAwareTexturePaint(
                defaultImage,
                highlightedImage,
                selectedImage,
                rec);
        clone.currentRec = currentRec;
        clone.currentImage = currentImage;
        return clone;
    }

    /**
     * Set the scale of the map that shows the corresponding feature.
     *
     * @param  scale  the current scale
     * @param  geom   the current feature geometry (a very small feature geometry is assumed to be in a geographic crs
     *                and causes very small rectangle height/width)
     */
    public void setScale(final double scale, final Geometry geom) {
        double factor = (1 / scale);

        double minSide = Math.min(rec.getWidth() * factor, rec.getHeight() * factor);
        
        if (minSide < MIN_SIDE_LENGTH) {
            //if the side is smaller than MIN_SIDE_LENGTH, display errors will be occur
            factor = MIN_SIDE_LENGTH / Math.min(rec.getWidth(), rec.getHeight());
        }

        if (geom.getArea() < 0.0001d) {
            //wgs 84 is assumed
            factor *= Math.sqrt(geom.getArea());
        }

        currentRec = new Rectangle2D.Double(
                rec.getMinX(),
                rec.getMinY(),
                rec.getWidth() * factor,
                rec.getHeight()
                        * factor);
        paint = new TexturePaint(
                defaultImage,
                currentRec);
    }
}
