/*
 * FeatureCollectionEvent.java
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
 * Created on 17. Mai 2006, 14:13
 *
 */

package de.cismet.cismap.commons.features;

import java.util.Collection;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class FeatureCollectionEvent {
    private Collection<Feature> eventFeatures;
    private FeatureCollection featureCollection;
    /** Creates a new instance of FeatureCollectionEvent */
    public FeatureCollectionEvent(FeatureCollection fc,Collection<Feature> features) {
        this.setEventFeatures(features);
        this.setFeatureCollection(fc);
    }

    public Collection<Feature> getEventFeatures() {
        return eventFeatures;
    }

    public void setEventFeatures(Collection<Feature> features) {
        this.eventFeatures = features;
    }

    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }
}
