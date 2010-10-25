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

import de.cismet.cismap.commons.wms.capabilities.CoordinateSystem;
import de.cismet.cismap.commons.wms.capabilities.Unit;

/**
 *
 * @author therter
 */
public class DeegreeCoordinateSystem implements CoordinateSystem {
    private org.deegree.model.crs.CoordinateSystem coordinateSytem;

    public DeegreeCoordinateSystem(org.deegree.model.crs.CoordinateSystem coordinateSytem) {
        this.coordinateSytem = coordinateSytem;
    }


    @Override
    public Unit[] getAxisUnits() {
        if ( this.coordinateSytem == null ) {
            return null;
        }
        org.deegree.crs.components.Unit[] origUnits = this.coordinateSytem.getAxisUnits();
        Unit[] units = new Unit[origUnits.length];

        for (int i = 0; i < origUnits.length; ++i) {
            units[i] = new DeegreeUnit(origUnits[i]);
        }

        return units;
    }


    @Override
    public String getIdentifier() {
        return coordinateSytem.getIdentifier();
    }

    @Override
    public String getPrefixedName() {
        return coordinateSytem.getPrefixedName();
    }

    @Override
    public int getDimension() {
        return coordinateSytem.getDimension();
    }
}
