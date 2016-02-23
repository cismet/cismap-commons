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

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class XPImage extends PImage {

    //~ Instance fields --------------------------------------------------------

    XPImage crossfadingTheOldPart = null;
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new XPImage object.
     */
    public XPImage() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  newImage           DOCUMENT ME!
     * @param  animationDuration  DOCUMENT ME!
     */
    public void setImage(final Image newImage, int animationDuration) {
        if (!getVisible()) {
            animationDuration = 0;
        }
        if ((getImage() != null) && (this.getParent() != null)) {
            final float transparencyOfLayer = getParent().getTransparency();
            if (crossfadingTheOldPart == null) {
                crossfadingTheOldPart = new XPImage();
                this.getParent().addChild(crossfadingTheOldPart);
                crossfadingTheOldPart.moveInFrontOf(this);
                crossfadingTheOldPart.setTransparency(0f);
            }
            if (crossfadingTheOldPart.getTransparency() == 0.0) {
                crossfadingTheOldPart.setImage(getImage());
                crossfadingTheOldPart.setBounds(this.getBounds());
                crossfadingTheOldPart.setOffset(getOffset());
                crossfadingTheOldPart.setScale(getScale());
                crossfadingTheOldPart.setTransparency(getTransparency());
                setTransparency(0);
                setImage(newImage);
                animateToTransparency(transparencyOfLayer, animationDuration);
                crossfadingTheOldPart.animateToTransparency(0, animationDuration);
            } else {
                setImage(newImage);
                // the existing animation will care about it
            }
        } else {
            // no parent or no image >> no animation
            setImage(newImage);
        }
    }
}
