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
package de.cismet.cismap.commons.rasterservice;

import com.sun.javafx.scene.control.skin.VirtualFlow;

import com.vividsolutions.jts.geom.Envelope;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import java.util.Collection;
import java.util.List;

import de.cismet.cismap.commons.rasterservice.georeferencing.PointCoordinatePair;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ImageFileMetaData {

    //~ Instance fields --------------------------------------------------------

    private Rectangle imageBounds;
    private Envelope imageEnvelope;
    private AffineTransform transform;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageMetaData object.
     *
     * @param  imageBounds    DOCUMENT ME!
     * @param  imageEnvelope  DOCUMENT ME!
     * @param  transform      DOCUMENT ME!
     */
    public ImageFileMetaData(final Rectangle imageBounds,
            final Envelope imageEnvelope,
            final AffineTransform transform) {
        this.imageBounds = imageBounds;
        this.imageEnvelope = imageEnvelope;
        this.transform = transform;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the imageBounds
     */
    public Rectangle getImageBounds() {
        return imageBounds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  imageBounds  the imageBounds to set
     */
    public void setImageBounds(final Rectangle imageBounds) {
        this.imageBounds = imageBounds;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the imageEnvelope
     */
    public Envelope getImageEnvelope() {
        return imageEnvelope;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  imageEnvelope  the imageEnvelope to set
     */
    public void setImageEnvelope(final Envelope imageEnvelope) {
        this.imageEnvelope = imageEnvelope;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AffineTransform getTransform() {
        return transform;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  transform  DOCUMENT ME!
     */
    public void setTransform(final AffineTransform transform) {
        this.transform = transform;
    }
}
