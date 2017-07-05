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
package de.cismet.cismap.commons.styling;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface EndPointStyle {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   coordArray  shaftTail DOCUMENT ME!
     * @param   scale       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    GeneralPath arrowhead(Coordinate[] coordArray, double scale);
}
