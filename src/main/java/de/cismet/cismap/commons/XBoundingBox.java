/*
 * XBoundingBox.java
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
 * Created on 21. M\u00E4rz 2006, 10:36
 *
 */

package de.cismet.cismap.commons;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class XBoundingBox extends BoundingBox{
    private String srs;
    private boolean metric=true;
    /** Creates a new instance of XBoundingBox */
    public XBoundingBox(double x1,double y1,double x2,double y2,String srs, boolean metric) {
        super(x1,y1,x2,y2);
        this.srs=srs;
        this.metric=metric;
    }
    
    public XBoundingBox(Element boundingBoxElementParent,String srs, boolean metric) throws DataConversionException{
        super(boundingBoxElementParent);
        this.srs=srs;
        this.metric=metric;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    public boolean isMetric() {
        return metric;
    }

    public void setMetric(boolean metric) {
        this.metric = metric;
    }
    
}
