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

import org.deegree.ogcwebservices.wfs.capabilities.FormatType;

import java.net.URI;

import de.cismet.cismap.commons.wfs.capabilities.OutputFormatType;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeOutputFormatType implements OutputFormatType {

    //~ Instance fields --------------------------------------------------------

    FormatType format;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeOutputFormatType object.
     *
     * @param  format  DOCUMENT ME!
     */
    public DeegreeOutputFormatType(final FormatType format) {
        this.format = format;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public URI getInFilter() {
        return format.getInFilter();
    }

    @Override
    public URI getOutFilter() {
        return format.getOutFilter();
    }

    @Override
    public URI getSchemaLocation() {
        return format.getSchemaLocation();
    }

    @Override
    public String getValue() {
        return format.getValue();
    }
}
