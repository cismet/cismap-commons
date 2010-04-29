/*
 * MapSearchEvent.java
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
 * Created on 24. April 2006, 16:50
 *
 */

package de.cismet.cismap.commons.interaction.events;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.BoundingBox;
import edu.umd.cs.piccolo.util.PBounds;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class MapSearchEvent {
    private PBounds bounds;
    private BoundingBox bb;
    private Geometry geometry;
    /** Creates a new instance of MapSearchEvent */
    public MapSearchEvent() {
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Deprecated
    public PBounds getBounds() {
        return bounds;
    }
    @Deprecated
    public void setBounds(PBounds bounds) {
        this.bounds = bounds;
    }
    @Deprecated
    public BoundingBox getBb() {
        return bb;
    }
    @Deprecated
    public void setBb(BoundingBox bb) {
        this.bb = bb;
    }
    
    
}
