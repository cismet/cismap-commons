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

import org.openide.util.lookup.ServiceProvider;

import de.cismet.commons.converter.ConversionException;

/**
 * Creates a rectangular polygon geometry (a bounding box) from the provided coordinates. At least two coordinates are
 * expected. If there are more they are ignored. The first coordinate is expected to be the lower-left corner of the
 * bounding box and the second one is expected to be the upper-right corner.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class BoundingBoxFromTextConverter extends AbstractGeometryFromTextConverter {

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
        return "BoundingBoxFromTextConverter";
    }

    @Override
    public String getFormatDisplayName() {
        return "Bounding Box from text converter";
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return null;
    }

    @Override
    public String getFormatHtmlDescription() {
        return null;
    }

    @Override
    public Object getFormatExample() {
        return null;
    }
}
