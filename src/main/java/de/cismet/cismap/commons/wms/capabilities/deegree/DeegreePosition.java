package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.wms.capabilities.Position;

/**
 *
 * @author therter
 */
public class DeegreePosition implements Position {
    private org.deegree.model.spatialschema.Position pos;

    public DeegreePosition(org.deegree.model.spatialschema.Position pos) {
        this.pos = pos;
    }


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
