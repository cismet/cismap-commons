/*
 * PFeatureCoordinatePosition.java
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
 * Created on 18. August 2006, 16:17
 *
 */

package de.cismet.cismap.commons.gui.piccolo;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class PFeatureCoordinatePosition {
    private PFeature pFeature;
    private int position;
    /** Creates a new instance of PFeatureCoordinatePosition */
    public PFeatureCoordinatePosition(PFeature pFeature,int position) {
        this.pFeature=pFeature;
        this.position=position;
    }

    public PFeature getPFeature() {
        return pFeature;
    }

    public void setPFeature(PFeature pFeature) {
        this.pFeature = pFeature;
    }

 
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
}
