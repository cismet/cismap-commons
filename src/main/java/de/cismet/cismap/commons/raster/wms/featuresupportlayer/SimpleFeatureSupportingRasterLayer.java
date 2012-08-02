/*
 * SimpleFeatureSupportingRasterLayer.java
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
 * Created on 18. Juli 2006, 15:50
 *
 */

package de.cismet.cismap.commons.raster.wms.featuresupportlayer;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimpleFeatureSupportingRasterLayer extends SimpleWMS implements FeatureAwareRasterService{
    FeatureCollection featureCollection;
    SimpleFeatureSupporterRasterServiceUrl sfu;
    /**
     * Creates a new instance of SimpleFeatureSupportingRasterLayer
     */
    public SimpleFeatureSupportingRasterLayer(SimpleFeatureSupporterRasterServiceUrl sfu) {
        super(sfu);
        this.sfu=sfu;
    }
    
    public SimpleFeatureSupportingRasterLayer(SimpleFeatureSupportingRasterLayer s) {
        super(s);
        featureCollection=s.featureCollection;
        sfu=s.sfu;
    }

    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureCollection=featureCollection;
    }

    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }
    
    public boolean equals( Object o) {
        return o instanceof SimpleFeatureSupportingRasterLayer && ((SimpleFeatureSupportingRasterLayer)o).sfu.equals(sfu);
    }

    public int hashCode() {
        return sfu.hashCode();
    }
    
    public Object clone() {
         return new SimpleFeatureSupportingRasterLayer(this);
    }
    
    
    
    
    
    
}
