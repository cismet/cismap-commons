/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import de.cismet.commons.converter.ConversionException;
import de.cismet.commons.converter.Converter.MatchRating;

/**
 * Creates a line string geometry from the provided coordinates. At least two coordinates are expected.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class PolylineFromTextConverter extends AbstractGeometryFromTextConverter implements MatchRating<String> {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        if (coordinates.length < 2) {
            throw new ConversionException("cannot create linestring lnfrom empty coordinate array"); // NOI18N
        }

        return geomFactory.createLineString(coordinates);
    }

    @Override
    public String getFormatName() {
        return "PolylineFromTextConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return NbBundle.getMessage(
                PolylineFromTextConverter.class,
                "PolylineFromTextConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return NbBundle.getMessage(
                PolylineFromTextConverter.class,
                "PolylineFromTextConverter.getFormatDescription().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlDescription() {
        return NbBundle.getMessage(
                PolylineFromTextConverter.class,
                "PolylineFromTextConverter.getFormatHtmlDescription().returnValue"); // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                PolylineFromTextConverter.class,
                "PolylineFromTextConverter.getFormatExample().returnValue"); // NOI18N
    }

    @Override
    public int rate(final String from, final String... params) {
        final int superRating = super.rate(from, params);
        if (superRating == 0) {
            return 0;
        }

        final String[] tokens = from.split(getTokenRegex());

        if (tokens.length < 6) {
            return 0;
        } else {
            if (tokens[0].equals(tokens[tokens.length - 2]) && tokens[1].equals(tokens[tokens.length - 1])) {
                // could be polygon
                return 80;
            } else {
                return 100;
            }
        }
    }
}
