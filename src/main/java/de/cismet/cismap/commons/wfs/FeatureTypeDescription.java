/*
 *  Copyright (C) 2010 therter
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

package de.cismet.cismap.commons.wfs;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import java.util.Vector;
import org.w3c.dom.Element;

/**
 * This interface provides the response of a describeFeatureRequest
 * @author therter
 */
public interface FeatureTypeDescription {
    public Element getSchemaRootElement();
    public Vector<FeatureServiceAttribute> getAllFeatureAttributes(WFSCapabilities caps);
    public String getFirstGeometryName();
}
