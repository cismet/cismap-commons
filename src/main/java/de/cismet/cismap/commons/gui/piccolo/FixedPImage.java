/*
 * FixedPImage.java
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
 * Created on 9. Mai 2006, 11:13
 *
 */
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.nodes.PImage;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.net.URL;

/**
 * 
 * @author thorsten.hell@cismet.de
 */
public class FixedPImage extends PImage implements PSticky {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private double sweetSpotX = 0d;
    private double sweetSpotY = 0d;
    private double originalOffsetX = 0;
    private double originalOffsetY = 0;

    /** Creates a new instance of FixedPImage */
    public FixedPImage() {
        super();
    }

    public FixedPImage(Image newImage) {
        super(newImage);
    }

    public FixedPImage(String fileName) {
        super(fileName);
    }

    public FixedPImage(URL url) {
        super(url);
    }

    @Override
    public void setOffset(double x, double y) {
        originalOffsetX = x;
        originalOffsetY = y;
        setOffsetWithoutTouchingOriginalOffset(x, y);
    }

    @Override
    public void setOffset(Point2D point) {
        setOffset(point.getX(), point.getY());
    }

    public double getSweetSpotX() {
        return sweetSpotX;
    }

    public void setSweetSpotX(double sweetSpotX) {
        this.sweetSpotX = sweetSpotX;
    }

    public double getSweetSpotY() {
        return sweetSpotY;
    }

    public void setSweetSpotY(double sweetSpotY) {
        this.sweetSpotY = sweetSpotY;
    }

    @Override
    public void setScale(double scale) {
        super.setScale(scale);
        setOffsetWithoutTouchingOriginalOffset(originalOffsetX, originalOffsetY);
    }

    private void setOffsetWithoutTouchingOriginalOffset(double x, double y) {
        super.setOffset(x - getGlobalBounds().getWidth() * sweetSpotX, y - getGlobalBounds().getHeight() * sweetSpotY);
    }
}
