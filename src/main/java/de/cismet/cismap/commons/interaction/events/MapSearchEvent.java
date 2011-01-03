/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.util.PBounds;

import de.cismet.cismap.commons.BoundingBox;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class MapSearchEvent {

    //~ Instance fields --------------------------------------------------------

    private PBounds bounds;
    private BoundingBox bb;
    private Geometry geometry;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MapSearchEvent.
     */
    public MapSearchEvent() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geometry  DOCUMENT ME!
     */
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    public PBounds getBounds() {
        return bounds;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  bounds  DOCUMENT ME!
     */
    @Deprecated
    public void setBounds(final PBounds bounds) {
        this.bounds = bounds;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    public BoundingBox getBb() {
        return bb;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  bb  DOCUMENT ME!
     */
    @Deprecated
    public void setBb(final BoundingBox bb) {
        this.bb = bb;
    }
}
