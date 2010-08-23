/*
 * ActiveLayerEvent.java
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
 * Created on 23. Februar 2006, 12:46
 *
 */
package de.cismet.cismap.commons.interaction.events;

import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerEvent {
    private int oldPosition;
    private int absolutePosition;
    private Object layer;
    private WMSCapabilities wmsCapabilities;

    /**
     * Creates a new instance of ActiveLayerEvent
     */
    public ActiveLayerEvent() {
    }

    public int getOldPosition() {
        return oldPosition;
    }

    public void setOldPosition(int oldPosition) {
        this.oldPosition = oldPosition;
    }

    public int getAbsolutePosition() {
        return absolutePosition;
    }

    public void setAbsolutePosition(int absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    public Object getLayer() {
        return layer;
    }

    public void setLayer(Object layer) {
        this.layer = layer;
    }

    public WMSCapabilities getCapabilities() {
        return wmsCapabilities;
    }

    public void setCapabilities(WMSCapabilities capabilities) {
        this.wmsCapabilities = capabilities;
    }
}
