/*
 * MappingModel.java
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
 * Created on 15. Juni 2005, 16:22
 *
 */

package de.cismet.cismap.commons;
import java.util.TreeMap;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public interface MappingModel {
    public TreeMap getRasterServices();
//    public void putRasterService(int position,RasterService rasterService);
//    public void moveRasterService(int step);
//    public void removeRasterService(RasterService rasterService);
    
    public TreeMap getFeatureServices();
//    public void putFeatureService(int position,FeatureService featureService);
//    public void moveFeatureService(int step);
//    public void removeFeatureService(FeatureService featureService);

    public void addMappingModelListener(MappingModelListener mml);
    public void removeMappingModelListener(MappingModelListener mml);
    
    public BoundingBox getInitialBoundingBox();
//    public void setInitialBoundingBox(BoundingBox bb);
    public void addLayer(RetrievalServiceLayer layer);
    public void removeLayer(RetrievalServiceLayer layer);
}
