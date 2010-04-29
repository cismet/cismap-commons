/*
 * FeatureCollection.java
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
 * Created on 15. Juni 2005, 16:23
 *
 */

package de.cismet.cismap.commons.features;
import de.cismet.cismap.commons.*;
import java.util.Collection;
import java.util.Vector;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public interface FeatureCollection extends ServiceLayer{
    public void addFeatureCollectionListener(FeatureCollectionListener l);
    public void removeFeatureCollectionListener(FeatureCollectionListener l);
    
    public int getFeatureCount();
    public Vector<Feature> getAllFeatures();
    public Feature getFeature(int index);
    public boolean areFeaturesEditable();
    
    
    public void select(Feature f);
    public void select(Collection<Feature> cf);
    public void addToSelection(Feature f);
    public void addToSelection(Collection<Feature> cf);
     
    public void unselect(Feature f);
    public void unselect(Collection<Feature> cf);
    public void unselectAll();
    public void unselectAll(boolean quiet);
    public Collection getSelectedFeatures();
    public boolean isSelected(Feature f);
    
    public void addFeature(Feature f);
    public void addFeatures(Collection<Feature> cf);
    public void removeFeatures(Collection<Feature> cf);
    public void removeFeature(Feature f);
    public void substituteFeatures(Collection<Feature> cf);
    public void removeAllFeatures();
    
    public void holdFeature(Feature f);
    public void unholdFeature(Feature f);
    public boolean isHoldFeature(Feature f);
    public void setHoldAll(boolean holdAll);
    
    public void reconsiderFeature(Feature f);
    
}
