/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice.georeferencing;

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
public interface RasterGeoReferencingHandlerListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    void positionAdded(final int position);

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    void positionRemoved(final int position);

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    void positionChanged(final int position);

    /**
     * DOCUMENT ME!
     */
    void transformationChanged();
}
