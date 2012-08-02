/*
 * ServiceLayer.java
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
 * Created on 15. Juni 2005, 16:42
 *
 */

package de.cismet.cismap.commons;

import java.util.HashMap;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public interface ServiceLayer {    
    public static final int LAYER_ENABLED_VISIBLE=0;
    public static final int LAYER_DISABLED_VISIBLE=1;
    public static final int LAYER_ENABLED_INVISIBLE=2;
    public static final int LAYER_DISABLED_INVISIBLE=3;
    public boolean isEnabled();
    public void setEnabled(boolean enabled);
    public boolean canBeDisabled();
    public int getLayerPosition();
    public void setLayerPosition(int layerPosition);
    public float getTranslucency();
    public void setTranslucency(float t);
    public String getName();
    public void setName(String name);    
}
