/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.image.BufferedImage;

/**
 * DOCUMENT ME!
 *
 * @author   dmeiers
 * @version  $Revision$, $Date$
 */
public class SignaturedFeature implements Feature {

    //~ Instance fields --------------------------------------------------------

    private Geometry geom = null;
    private boolean canBeSelected = false;
    private boolean isEditable = false;
    private boolean hidden = false;
    private BufferedImage overlayIcon = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SignaturedFeature object.
     *
     * @param  g  DOCUMENT ME!
     */
    public SignaturedFeature(final Geometry g) {
        geom = g;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry getGeometry() {
        return geom;
    }

    @Override
    public void setGeometry(final Geometry g) {
        this.geom = g;
    }

    @Override
    public boolean canBeSelected() {
        return canBeSelected;
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    @Override
    public boolean isEditable() {
        return this.isEditable;
    }

    @Override
    public void setEditable(final boolean editable) {
        this.isEditable = editable;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public void hide(final boolean hiding) {
        this.hidden = hiding;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public BufferedImage getOverlayIcon() {
        return overlayIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  overlayIcon  DOCUMENT ME!
     */
    public void setOverlayIcon(final BufferedImage overlayIcon) {
        this.overlayIcon = overlayIcon;
    }
}
