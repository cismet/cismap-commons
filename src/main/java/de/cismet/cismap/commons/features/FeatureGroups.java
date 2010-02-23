/*
 *  Copyright (C) 2010 srichter
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

import de.cismet.tools.collections.TypeSafeCollections;
import java.util.Collection;

/**
 *
 * @author srichter
 */
public final class FeatureGroups {

    private FeatureGroups() {
        throw new AssertionError();
    }

    public static Collection<Feature> expand(FeatureGroup fg) {
        final Collection<Feature> result = TypeSafeCollections.newArrayList();
        result.add(fg);
        final Collection<Feature> subFeatures = fg.getFeatures();
        if (subFeatures != null) {
            for (Feature f : fg.getFeatures()) {
                if (f instanceof FeatureGroup) {
                    result.addAll(expand((FeatureGroup) f));
                } else {
                    result.add(f);
                }
            }
        }
        return result;
    }
}
