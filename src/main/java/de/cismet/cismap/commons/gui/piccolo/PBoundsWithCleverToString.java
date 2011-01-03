/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.util.PBounds;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.XBoundingBox;

import de.cismet.tools.StaticDecimalTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PBoundsWithCleverToString extends PBounds {

    //~ Instance fields --------------------------------------------------------

    private WorldToScreenTransform wtst;
    private String crsCode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PBoundsWithCleverToString.
     *
     * @param  aBounds  DOCUMENT ME!
     * @param  wtst     DOCUMENT ME!
     * @param  crsCode  DOCUMENT ME!
     */
    public PBoundsWithCleverToString(final PBounds aBounds, final WorldToScreenTransform wtst, final String crsCode) {
        super(aBounds);
        this.wtst = wtst;
        this.crsCode = crsCode;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        // x,y ist der Punkt links oben
        final double x1 = wtst.getWorldX(x);
        final double y2 = wtst.getWorldY(y);
        final double x2 = x1 + width;
        final double y1 = y2 - height;
        return StaticDecimalTools.round("0.00", x1) + "," + StaticDecimalTools.round("0.00", y1) + ","
                    + StaticDecimalTools.round("0.00", x2) + "," + StaticDecimalTools.round("0.00", y2) + " (" + crsCode
                    + ")"; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCrsCode() {
        return crsCode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WorldToScreenTransform getWtst() {
        return wtst;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  a bounding box with the world coordinates of this PBounds object. The metric value of the resulting
     *          bounding box is not correct
     */
    public XBoundingBox getWorldCoordinates() {
        final double x1 = wtst.getWorldX(x);
        final double y2 = wtst.getWorldY(y);
        final double x2 = x1 + width;
        final double y1 = y2 - height;

        return new XBoundingBox(x1, y1, x2, y2, crsCode, false);
    }
}
