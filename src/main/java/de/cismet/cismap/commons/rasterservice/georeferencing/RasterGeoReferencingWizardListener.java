/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;

import java.awt.Point;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface RasterGeoReferencingWizardListener extends RasterGeoReferencingHandlerListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    void pointSelected(final int position);

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    void coordinateSelected(final int position);

    /**
     * DOCUMENT ME!
     *
     * @param  handler  DOCUMENT ME!
     */
    void handlerChanged(final RasterGeoReferencingHandler handler);
}
