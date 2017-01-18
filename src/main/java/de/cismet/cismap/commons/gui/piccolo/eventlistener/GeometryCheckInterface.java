/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This interface can be used for checks, which should be performed during the creation of new geometries.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface GeometryCheckInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * Checks, if the given geomety is valid.
     *
     * @param   g                             the geometry to check
     * @param   lastCoordinate                the coordinate of the last mouse event
     * @param   ignoreLastGeometryCoordinate  the last coordinate of the geometry is not confirmed, yet, if this is true
     *                                        (i.e. this coordinate results from a move action)
     *
     * @return  true, iff the given geomety is valid
     */
    boolean check(Geometry g, Coordinate lastCoordinate, boolean ignoreLastGeometryCoordinate);

    /**
     * The error text, that should be shown, if the geometry is not valid.
     *
     * @return  An array with the individual rows of the error text.
     */
    String[] getErrorText();
}
