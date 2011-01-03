/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities.deegree;

import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;

import java.security.InvalidParameterException;

import de.cismet.cismap.commons.exceptions.ConvertException;
import de.cismet.cismap.commons.wms.capabilities.CoordinateSystem;
import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Position;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeEnvelope implements Envelope {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.model.spatialschema.Envelope envelope;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeEnvelope object.
     *
     * @param  envelope  DOCUMENT ME!
     */
    public DeegreeEnvelope(final org.deegree.model.spatialschema.Envelope envelope) {
        this.envelope = envelope;
    }

    //~ Methods ----------------------------------------------------------------

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
    public Envelope transform(final String destCrs, final String sourceCrs) throws ConvertException {
        try {
            final GeoTransformer transformer = new GeoTransformer(destCrs);
            return new DeegreeEnvelope(transformer.transform(envelope, sourceCrs));
        } catch (Exception e) {
            throw new ConvertException(e.getMessage(), e);
        }
    }
}
