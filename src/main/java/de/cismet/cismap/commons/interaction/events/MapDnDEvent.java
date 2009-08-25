/*
 * MapDnDEvent.java
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
 * Created on 25. April 2006, 11:14
 *
 */

package de.cismet.cismap.commons.interaction.events;

import java.awt.dnd.DropTargetEvent;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class MapDnDEvent {
    private DropTargetEvent dte;
    private double xPos;
    private double yPos;
    
    /** Creates a new instance of MapDnDEvent */
    public MapDnDEvent() {
    }

    public DropTargetEvent getDte() {
        return dte;
    }

    public void setDte(DropTargetEvent dte) {
        this.dte = dte;
    }

    public double getXPos() {
        return xPos;
    }

    public void setXPos(double xPos) {
        this.xPos = xPos;
    }

    public double getYPos() {
        return yPos;
    }

    public void setYPos(double yPos) {
        this.yPos = yPos;
    }
    
}
