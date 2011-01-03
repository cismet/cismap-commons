/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
package de.cismet.cismap.commons.util;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public class FormatToRealWordCalculator {

    //~ Static fields/initializers ---------------------------------------------

    public static final double DEFAULT_JAVA_RESOLUTION_IN_DPI = 72d;
    public static final double CM_TO_INCH = 2.54d;
    public static final double MM_TO_INCH = CM_TO_INCH * 10d;
//    public static final double INCH_OF_A_MILLIMETER = 0.03937008d;
    public static final double MILLIMETER_OF_A_METER = 1000d;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   value     DOCUMENT ME!
     * @param   massstab  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static double toRealWorldValue(final double value, final double massstab) {
        return value * CM_TO_INCH / DEFAULT_JAVA_RESOLUTION_IN_DPI * MM_TO_INCH / MILLIMETER_OF_A_METER * massstab;
    }
}
