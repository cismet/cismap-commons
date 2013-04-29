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
import com.vividsolutions.jts.geom.LinearRing;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Locale;

import de.cismet.commons.converter.ConversionException;
import de.cismet.commons.converter.Converter.MatchRating;

/**
 * Creates a rectangular polygon geometry (a bounding box) from the provided coordinates. At least two coordinates are
 * expected. If there are more they are ignored. The first coordinate is expected to be the lower-left corner of the
 * bounding box and the second one is expected to be the upper-right corner.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class BoundingBoxFromTextConverter extends AbstractGeometryFromTextConverter
        implements MatchRating<String> {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        if (coordinates.length < 2) {
            throw new ConversionException("too few coordinates for bounding box: " + coordinates.length); // NOI18N
        }

        final Coordinate[] coords = new Coordinate[5];
        final double llx = coordinates[0].x;
        final double lly = coordinates[0].y;
        final double urx = coordinates[1].x;
        final double ury = coordinates[1].y;

        coords[0] = coordinates[0];
        coords[1] = new Coordinate(llx, ury);
        coords[2] = coordinates[1];
        coords[3] = new Coordinate(urx, lly);
        coords[4] = coordinates[0];

        final LinearRing ring = geomFactory.createLinearRing(coords);

        return geomFactory.createPolygon(ring, null);
    }

    @Override
    public String getFormatName() {
        return "BoundingBoxFromTextConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return NbBundle.getMessage(
                BoundingBoxFromTextConverter.class,
                "BoundingBoxFromTextConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return NbBundle.getMessage(
                BoundingBoxFromTextConverter.class,
                "BoundingBoxFromTextConverter.getFormatDescription().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlDescription() {
        return NbBundle.getMessage(
                BoundingBoxFromTextConverter.class,
                "BoundingBoxFromTextConverter.getFormatHtmlDescription().returnValue"); // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                BoundingBoxFromTextConverter.class,
                "BoundingBoxFromTextConverter.getFormatExample().returnValue"); // NOI18N
    }

    @Override
    public int rate(final String from) {
        final int superRating = super.rate(from);
        if (superRating == 0) {
            return 0;
        }

        final String[] tokens = from.split(getTokenRegex());

        if (tokens.length < 4) {
            return 0;
        } else if (tokens.length > 4) {
            return 50;
        } else {
            // 4 tokens = 2 coordinates
            return 100;
        }
    }
}
