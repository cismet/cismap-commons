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
 * LabeledStyledFeature.java
 *
 * Created on 4. M\u00E4rz 2005, 14:33
 */

package de.cismet.cismap.commons.features;

import java.awt.Font;
import java.awt.Paint;

/**
 *
 * @author hell
 */
public interface AnnotatedFeature{

    public String getPrimaryAnnotation();
    
    public boolean isPrimaryAnnotationVisible();
    
    public void setPrimaryAnnotationVisible(boolean visible);

    public Font getPrimaryAnnotationFont();
    
    public Paint getPrimaryAnnotationPaint();

    public double getPrimaryAnnotationScaling();

    public float getPrimaryAnnotationJustification();
    
    public void setPrimaryAnnotationJustification(float just);
    
    public String getSecondaryAnnotation();

    public void setPrimaryAnnotation(String primaryAnnotation);
    
    public void setPrimaryAnnotationFont(Font primaryAnnotationFont);

    public void setPrimaryAnnotationPaint(Paint primaryAnnotationPaint);
    
    public void setPrimaryAnnotationScaling(double primaryAnnotationScaling);

    public void setSecondaryAnnotation(String secondaryAnnotation);
    
    public boolean isAutoscale();
    public void setAutoScale(boolean autoScale);
    
    public Integer getMinScaleDenominator();
    public Integer getMaxScaleDenominator();
    
    public void setMinScaleDenominator(Integer min);
    public void setMaxScaleDenominator(Integer max);
    
    
}
