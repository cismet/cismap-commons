/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;
import edu.umd.cs.piccolo.nodes.PImage;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.Iterator;
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
     * @param  fileName           DOCUMENT ME!
     * @param  animationDuration  DOCUMENT ME!
     */
    public void setImage(final String fileName, final int animationDuration) {
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
                log.fatal("TargetTransparency: " + transparencyOfLayer);
                animateToTransparency(transparencyOfLayer, animationDuration);
                crossfadingTheOldPart.animateToTransparency(0, animationDuration);
            } else {
                setImage(newImage);
                // the existing animation will care about it
            }
//            final TimerTask task = new TimerTask() {
//
//                    @Override
//                    public void run() {
//                        EventQueue.invokeLater(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    try {
//                                        XPImage.this.getParent().removeChild(old);
//                                    } catch (Exception e) {
//                                        log.info("Removal of the temporary image failed. It was already removed.", e); // NOI18N
//                                    }
//                                }
//                            });
//                    }
//                };
//
//            final Timer timer = new Timer();
            // timer.schedule(task, (long)(animationDuration * 1.1));
        } else {
            log.fatal("How can this happen???");
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

//    @Override
//    protected void layoutChildren() {
//        Iterator i = getChildrenIterator();
//        while (i.hasNext()) {
//            PNode each = (PNode) i.next();
//            each.setOffset(getOffset());
//            each.setScale(getScale());
//        }
//
//    }
}
