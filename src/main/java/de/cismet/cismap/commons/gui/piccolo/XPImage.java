/*
 * XPImage.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 20. April 2006, 17:13
 *
 */
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.nodes.PImage;
import java.awt.EventQueue;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class XPImage extends PImage {
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int animationDuration;

    public void setImage(String fileName, int animationDuration) {
        this.animationDuration = animationDuration;
        if (getImage() != null) {
            float t = super.getTransparency();
            final XPImage old = (XPImage) this.clone();
            old.setImage(getImage());
            this.getParent().addChild(old);
            old.moveInFrontOf(this);
            this.setTransparency(0);
            super.setImage(fileName);
            animateToTransparency(t,animationDuration);
            old.animateToTransparency(0,animationDuration);
            TimerTask task = new TimerTask() {
                public void run() {
                //XPImage.this.getParent().removeChild(old);
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, (long) (animationDuration * 1.5));
        } else {
            float t = super.getTransparency();
            setTransparency(0);
            setImage(fileName);
            animateToTransparency(t, animationDuration);
        }
    }

    public void setImage(final Image newImage, int animationDuration) {
        if (!getVisible()) {
            animationDuration = 0;
        }
        if (getImage() != null&&this.getParent()!=null) {
            float t = super.getTransparency();
            XPImage oldX = new XPImage();
            final XPImage old = (XPImage) this.clone();
            old.setImage(getImage());
            log.debug("this.getParent() in setImage():" + this.getParent());
            this.getParent().addChild(old);
            old.moveInFrontOf(this);
            this.setTransparency(0);
            super.setImage(newImage);
            animateToTransparency(t, animationDuration);
            old.animateToTransparency(0, animationDuration);
            TimerTask task = new TimerTask() {
                public void run() {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                XPImage.this.getParent().removeChild(old);
                            } catch (Exception e) {
                                log.info("Entfernen des Tempor\u00E4ren Bildes ging schief. War schon weg.", e);
                            }
                        }
                    });
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, (long) (animationDuration * 4.5));
        } else {
            float t = super.getTransparency();
            setTransparency(0);
            setImage(newImage);
            animateToTransparency(t, animationDuration);
        }
    }

    @Override
    public Object clone() {
        XPImage cl = new XPImage();
        cl.setImage(this.getImage());
        cl.setBounds(this.getBounds());
        cl.setOffset(getOffset());
        cl.setScale(getScale());
        cl.setTransparency(getTransparency());
        return cl;
    }
}
