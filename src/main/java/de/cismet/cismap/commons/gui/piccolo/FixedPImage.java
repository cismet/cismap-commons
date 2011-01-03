/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.nodes.PImage;

import java.awt.Image;
import java.awt.geom.Point2D;

import java.net.URL;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class FixedPImage extends PImage implements PSticky {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private double sweetSpotX = 0d;
    private double sweetSpotY = 0d;
    private double originalOffsetX = 0;
    private double originalOffsetY = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FixedPImage.
     */
    public FixedPImage() {
        super();
    }

    /**
     * Creates a new FixedPImage object.
     *
     * @param  newImage  DOCUMENT ME!
     */
    public FixedPImage(final Image newImage) {
        super(newImage);
    }

    /**
     * Creates a new FixedPImage object.
     *
     * @param  fileName  DOCUMENT ME!
     */
    public FixedPImage(final String fileName) {
        super(fileName);
    }

    /**
     * Creates a new FixedPImage object.
     *
     * @param  url  DOCUMENT ME!
     */
    public FixedPImage(final URL url) {
        super(url);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setOffset(final double x, final double y) {
        originalOffsetX = x;
        originalOffsetY = y;
        setOffsetWithoutTouchingOriginalOffset(x, y);
    }

    @Override
    public void setOffset(final Point2D point) {
        setOffset(point.getX(), point.getY());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSweetSpotX() {
        return sweetSpotX;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sweetSpotX  DOCUMENT ME!
     */
    public void setSweetSpotX(final double sweetSpotX) {
        this.sweetSpotX = sweetSpotX;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSweetSpotY() {
        return sweetSpotY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sweetSpotY  DOCUMENT ME!
     */
    public void setSweetSpotY(final double sweetSpotY) {
        this.sweetSpotY = sweetSpotY;
    }

    @Override
    public void setScale(final double scale) {
        super.setScale(scale);
        setOffsetWithoutTouchingOriginalOffset(originalOffsetX, originalOffsetY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x  DOCUMENT ME!
     * @param  y  DOCUMENT ME!
     */
    private void setOffsetWithoutTouchingOriginalOffset(final double x, final double y) {
        super.setOffset(x - (getGlobalBounds().getWidth() * sweetSpotX),
            y
                    - (getGlobalBounds().getHeight() * sweetSpotY));
    }
}
