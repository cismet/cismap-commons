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
 * StyledFeature.java
 *
 * Created on 4. M\u00E4rz 2005, 14:31
 */
package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import java.awt.Font;
import java.awt.Paint;

/**
 *
 * @author hell
 */
public interface StyledFeature extends Feature {

    //public java.awt.Stroke getLineStyle();
    public java.awt.Paint getLinePaint();

    public void setLinePaint(Paint linePaint);

    public int getLineWidth();

    public void setLineWidth(int width);

    public java.awt.Paint getFillingPaint();

    public void setFillingPaint(Paint fillingStyle);

    public float getTransparency();

    public void setTransparency(float transparrency);

    public FeatureAnnotationSymbol getPointAnnotationSymbol();

    public void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol);

    public boolean isHighlightingEnabled();

    public void setHighlightingEnabled(boolean enabled);
}
