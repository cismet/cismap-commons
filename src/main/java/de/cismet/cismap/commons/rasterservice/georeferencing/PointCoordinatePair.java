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
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.Point;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointCoordinatePair {

    //~ Instance fields --------------------------------------------------------

    private Point point;
    private Coordinate coordinate;

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Object clone() {
        return new PointCoordinatePair((getPoint() != null) ? (Point)getPoint().clone() : null,
                (getCoordinate() != null) ? (Coordinate)getCoordinate().clone() : null);
    }
}
