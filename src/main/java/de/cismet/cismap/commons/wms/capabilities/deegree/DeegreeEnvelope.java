package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.exceptions.ConvertException;
import de.cismet.cismap.commons.wms.capabilities.CoordinateSystem;
import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Position;
import java.security.InvalidParameterException;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;

/**
 *
 * @author therter
 */
public class DeegreeEnvelope implements Envelope {
    private org.deegree.model.spatialschema.Envelope envelope;


    public DeegreeEnvelope(org.deegree.model.spatialschema.Envelope envelope) {
        this.envelope = envelope;
    }


    @Override
    public Position getMax() {
        if (envelope.getMax() == null) {
            return null;
        } else {
            return new DeegreePosition(envelope.getMax());
        }
    }


    @Override
    public Position getMin() {
        if (envelope.getMin() == null) {
            return null;
        } else {
            return new DeegreePosition(envelope.getMin());
        }
    }


    @Override
    public double getWidth() {
        return envelope.getWidth();
    }


    @Override
    public double getHeight() {
        return envelope.getHeight();
    }


    @Override
    public CoordinateSystem getCoordinateSystem() {
        if (envelope.getCoordinateSystem() == null) {
            return null;
        } else {
            return new DeegreeCoordinateSystem(envelope.getCoordinateSystem());
        }
    }

    @Override
    public Envelope transform(String destCrs, String sourceCrs) throws ConvertException {
        try {
            GeoTransformer transformer = new GeoTransformer(destCrs);
            return new DeegreeEnvelope(transformer.transform(envelope, sourceCrs));
        } catch (Exception e) {
            throw new ConvertException(e.getMessage(), e);
        }
    }
}
