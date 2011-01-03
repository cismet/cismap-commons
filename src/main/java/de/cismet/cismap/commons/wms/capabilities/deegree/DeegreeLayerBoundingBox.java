/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.wms.capabilities.LayerBoundingBox;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeLayerBoundingBox extends DeegreeEnvelope implements LayerBoundingBox {

    //~ Instance fields --------------------------------------------------------

    org.deegree.ogcwebservices.wms.capabilities.LayerBoundingBox layerBoundingBox;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeLayerBoundingBox object.
     *
     * @param  layerBoundingBox  DOCUMENT ME!
     */
    public DeegreeLayerBoundingBox(
            final org.deegree.ogcwebservices.wms.capabilities.LayerBoundingBox layerBoundingBox) {
        super(layerBoundingBox);
        this.layerBoundingBox = layerBoundingBox;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public double getResX() {
        return this.layerBoundingBox.getResx();
    }

    @Override
    public double getResY() {
        return this.layerBoundingBox.getResy();
    }

    @Override
    public String getSRS() {
        return this.layerBoundingBox.getSRS();
    }
}
