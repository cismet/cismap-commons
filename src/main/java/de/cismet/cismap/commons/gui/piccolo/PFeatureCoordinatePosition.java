/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PFeatureCoordinatePosition {

    //~ Instance fields --------------------------------------------------------

    private PFeature pFeature;
    private int position;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PFeatureCoordinatePosition.
     *
     * @param  pFeature  DOCUMENT ME!
     * @param  position  DOCUMENT ME!
     */
    public PFeatureCoordinatePosition(final PFeature pFeature, final int position) {
        this.pFeature = pFeature;
        this.position = position;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getPFeature() {
        return pFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pFeature  DOCUMENT ME!
     */
    public void setPFeature(final PFeature pFeature) {
        this.pFeature = pFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getPosition() {
        return position;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    public void setPosition(final int position) {
        this.position = position;
    }
}
