/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.nodes.PImage;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.Timer;
import java.util.TimerTask;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class XPImage extends PImage {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int animationDuration;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  fileName           DOCUMENT ME!
     * @param  animationDuration  DOCUMENT ME!
     */
    public void setImage(final String fileName, final int animationDuration) {
        this.animationDuration = animationDuration;
        if (getImage() != null) {
            final float t = super.getTransparency();
            final XPImage old = (XPImage)this.clone();
            old.setImage(getImage());
            this.getParent().addChild(old);
            old.moveInFrontOf(this);
            this.setTransparency(0);
            super.setImage(fileName);
            animateToTransparency(t, animationDuration);
            old.animateToTransparency(0, animationDuration);
            final TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        // XPImage.this.getParent().removeChild(old);
                    }
                };

            final Timer timer = new Timer();
            timer.schedule(task, (long)(animationDuration * 1.5));
        } else {
            final float t = super.getTransparency();
            setTransparency(0);
            setImage(fileName);
            animateToTransparency(t, animationDuration);
        }
    }

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
            final float t = super.getTransparency();
            final XPImage oldX = new XPImage();
            final XPImage old = (XPImage)this.clone();
            old.setImage(getImage());
            if (log.isDebugEnabled()) {
                log.debug("this.getParent() in setImage():" + this.getParent()); // NOI18N
            }
            this.getParent().addChild(old);
            old.moveInFrontOf(this);
            this.setTransparency(0);
            super.setImage(newImage);
            animateToTransparency(t, animationDuration);
            old.animateToTransparency(0, animationDuration);
            final TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        XPImage.this.getParent().removeChild(old);
                                    } catch (Exception e) {
                                        log.info("Removal of the temporary image failed. It was already removed.", e); // NOI18N
                                    }
                                }
                            });
                    }
                };

            final Timer timer = new Timer();
            timer.schedule(task, (long)(animationDuration * 4.5));
        } else {
            final float t = super.getTransparency();
            setTransparency(0);
            setImage(newImage);
            animateToTransparency(t, animationDuration);
        }
    }

    @Override
    public Object clone() {
        final XPImage cl = new XPImage();
        cl.setImage(this.getImage());
        cl.setBounds(this.getBounds());
        cl.setOffset(getOffset());
        cl.setScale(getScale());
        cl.setTransparency(getTransparency());
        return cl;
    }
}
