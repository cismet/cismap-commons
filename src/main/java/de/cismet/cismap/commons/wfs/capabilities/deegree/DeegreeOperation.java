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

package de.cismet.cismap.commons.wfs.capabilities.deegree;

import de.cismet.cismap.commons.wfs.capabilities.OperationType;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;

/**
 *
 * @author therter
 */
public class DeegreeOperation implements OperationType {
    private Operation op;


    public DeegreeOperation(Operation op) {
        this.op = op;
    }

    public String getOperation() {
        return op.getOperation();
    }
}
