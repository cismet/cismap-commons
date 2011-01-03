/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.wms.capabilities.Position;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreePosition implements Position {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.model.spatialschema.Position pos;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreePosition object.
     *
     * @param  pos  DOCUMENT ME!
     */
    public DeegreePosition(final org.deegree.model.spatialschema.Position pos) {
        this.pos = pos;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public double getX() {
        return pos.getX();
    }

    @Override
    public double getY() {
        return pos.getY();
    }

    @Override
    public double getZ() {
        return pos.getZ();
    }

    @Override
    public double getCoordinateDimension() {
        return pos.getCoordinateDimension();
    }
}
