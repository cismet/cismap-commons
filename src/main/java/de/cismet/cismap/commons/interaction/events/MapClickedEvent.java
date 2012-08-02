/*
 * MapClickedEvent.java
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
 * Created on 7. April 2006, 11:36
 *
 */

package de.cismet.cismap.commons.interaction.events;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class MapClickedEvent {
    private PInputEvent pInputEvent=null;
    private Object objectUnderClick=null;
    private String mode=null;
    /** Creates a new instance of MapClickedEvent */
    public MapClickedEvent(String mode, PInputEvent pInputEvent) {
        this.pInputEvent=pInputEvent;
        this.mode=mode;
    }

    public double getX() {
        return pInputEvent.getCanvasPosition().getX();
    }


    public double getY() {
        return pInputEvent.getCanvasPosition().getY();
    }


    public int getClickCount() {
        return pInputEvent.getClickCount();
    }


    public Object getObjectUnderClick() {
        return objectUnderClick;
    }

    public void setObjectUnderClick(Object objectUnderClick) {
        this.objectUnderClick = objectUnderClick;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
    
}
