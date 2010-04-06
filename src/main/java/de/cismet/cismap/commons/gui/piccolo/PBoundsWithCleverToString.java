/*
 * PBoundsWithCleverToString.java
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
 * Created on 21. M\u00E4rz 2006, 15:00
 *
 */

package de.cismet.cismap.commons.gui.piccolo;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.tools.StaticDecimalTools;
import edu.umd.cs.piccolo.util.PBounds;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class PBoundsWithCleverToString extends PBounds{
    WorldToScreenTransform wtst;
    /** Creates a new instance of PBoundsWithCleverToString */
    public PBoundsWithCleverToString(PBounds aBounds,WorldToScreenTransform wtst) {
        super(aBounds);
        this.wtst=wtst;
    }
    public String toString() {
        //x,y ist der Punkt links oben
        double x1=wtst.getWorldX(x); 
        double y2=wtst.getWorldY(y);
        double x2=x1+width;
        double y1=y2-height;
        return StaticDecimalTools.round("0.00",x1)+","+StaticDecimalTools.round("0.00",y1)+","+StaticDecimalTools.round("0.00",x2)+","+StaticDecimalTools.round("0.00",y2);//NOI18N
        
    }
    
}
