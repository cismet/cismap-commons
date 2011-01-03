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
package de.cismet.cismap.commons.wfs.capabilities.deegree;

import org.deegree.ogcwebservices.wfs.capabilities.Operation;

import de.cismet.cismap.commons.wfs.capabilities.OperationType;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeOperation implements OperationType {

    //~ Instance fields --------------------------------------------------------

    private Operation op;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeOperation object.
     *
     * @param  op  DOCUMENT ME!
     */
    public DeegreeOperation(final Operation op) {
        this.op = op;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getOperation() {
        return op.getOperation();
    }
}
