/*
 * FeatureCollectionListener.java
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
 * Created on 22. Juni 2005, 13:47
 *
 */

package de.cismet.cismap.commons.features;

import de.cismet.cismap.commons.MappingModelEvent;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public interface FeatureCollectionListener {
    public void featuresAdded(FeatureCollectionEvent fce);
    public void allFeaturesRemoved(FeatureCollectionEvent fce);
    public void featuresRemoved(FeatureCollectionEvent fce);
    public void featuresChanged(FeatureCollectionEvent fce);
    public void featureSelectionChanged(FeatureCollectionEvent fce);
    public void featureReconsiderationRequested(FeatureCollectionEvent fce);
    public void featureCollectionChanged();
}
