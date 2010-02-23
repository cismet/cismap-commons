/*
 *  Copyright (C) 2010 thorsten
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.features;

import java.util.Collection;

/**
 *
 * @author thorsten
 */
public interface FeatureGroup extends Feature, Iterable<Feature> {

    /**
     *
     * @return read-only view of all contained features
     */
    public Collection<Feature> getFeatures();

    public boolean addFeature(Feature toAdd);

    public boolean addFeatures(Collection<Feature> toAdd);

    public boolean removeFeature(Feature toRemove);

    public boolean removeFeatures(Collection<Feature> toRemove);
}
