/*----------------    FILE HEADER  ------------------------------------------
 * This file is part of cismap (http://cismap.sourceforge.net)
 *
 * Copyright (C) 2004 by:
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
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 4. M\u00E4rz 2005, 14:24
 */

package de.cismet.cismap.commons.features;

/**
 * A Feature is "something" that has a geometry
 *
 * @author thorsten.hell@cismet.de
 */
public interface Feature {
    public com.vividsolutions.jts.geom.Geometry getGeometry(); 
    public void setGeometry(com.vividsolutions.jts.geom.Geometry geom); 
    public boolean canBeSelected();
    public void setCanBeSelected(boolean canBeSelected);
    public boolean isEditable();
    public void setEditable(boolean editable);
    public boolean isHidden();
    public void hide(boolean hiding);
}
