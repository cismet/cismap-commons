/*
 * StatusEvent.java
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
 * Created on 20. Februar 2006, 10:53
 *
 */

package de.cismet.cismap.commons.interaction.events;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class StatusEvent {
    public static final String ACTIVE_STATUS="active_status";//NOI18N
    public static final String COORDINATE_STRING="coordinate_string";//NOI18N
    public static final String ERROR_STATUS="error_status";//NOI18N
    public static final String MAPPING_MODE="mode";//NOI18N
    public static final String MEASUREMENT_INFOS="measurement";//NOI18N
    public static final String OBJECT_INFOS="object_infos";//NOI18N
    public static final String SCALE="scale";//NOI18N
    public static final String CRS="crs";//NOI18N
    
    private String name;
    private Object value;
    /** Creates a new instance of StatusEvent */
    public StatusEvent(String name,Object value) {
        this.name=name;
        this.setValue(value);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
