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

import de.cismet.commons.converter.ConversionException;

/**
 * Creates a polygon geometry from the provided coordinates. At least three coordinates are expected. If the first and
 * the last coordinate of the given array do not match an additional coordinate is added to close the polygon.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class PolygonFromTextConverter extends AbstractGeometryFromTextConverter {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        if (coordinates.length < 3) {
            throw new ConversionException("too few coordinates for polygon: " + coordinates.length); // NOI18N
        }

        final Coordinate[] coords;
        final Coordinate lastCoord = coordinates[coordinates.length - 1];
        if (coordinates[0].equals(lastCoord)) {
            coords = coordinates;
        } else {
            // adding closing coordinate for convenience
            coords = new Coordinate[coordinates.length + 1];
            System.arraycopy(coordinates, 0, coords, 0, coordinates.length);
            coords[coordinates.length] = coordinates[0];
        }

        final LinearRing ring = geomFactory.createLinearRing(coords);

        return geomFactory.createPolygon(ring, null);
    }

    @Override
    public String getFormatName() {
        return "PolygonFromTextConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return NbBundle.getMessage(
                PolygonFromTextConverter.class,
                "PolygonFromTextConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        final String desc = NbBundle.getMessage(
                PointFromTextConverter.class,
                "PolygonFromTextConverter.getFormatDescription().returnValue"); // NOI18N
        final String superDesc = super.getFormatDescription();

        return desc + "\n" + superDesc;
    }

    @Override
    public String getFormatHtmlDescription() {
        final String desc = NbBundle.getMessage(
                    PointFromTextConverter.class,
                    "PolygonFromTextConverter.getFormatHtmlDescription().returnValue")
                    .replaceAll("<[/]?html>", "");                                              // NOI18N
        final String superDesc = super.getFormatHtmlDescription().replaceAll("<[/]?html>", ""); // NOI18N;

        return "<html>" + desc + "<br/>" + superDesc + "</html>"; // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "PolygonFromTextConverter.getFormatExample().returnValue",
                getDecimalSeparator()); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
                return 100;
            } else {
                // could be polyline
                return 80;
            }
        }
    }
}
