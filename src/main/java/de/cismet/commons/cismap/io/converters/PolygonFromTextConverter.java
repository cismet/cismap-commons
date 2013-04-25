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
        return "Polygon from text converter";
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return
            "- Requires at least three coordinates\n- Adds closing coordinate if last coordinate does not match the first one\n- Assumes 'easting<sep>northing<sep>easting<sep>...' pattern\n- <sep> may be white-spaces (space, tab, enter, etc.), colons or semi-colons\n- Numbers are parsed dependent on the language settings";
    }

    @Override
    public String getFormatHtmlDescription() {
        return
            "<html>- Requires at least three coordinates<br/>- Adds closing coordinate if last coordinate does not match the first one<br/>- Assumes 'easting&lt;sep&gt;northing&lt;sep&gt;easting&lt;sep&gt;...' pattern<br/>- &lt;sep&gt; may be white-spaces (space, tab, enter, etc.), colons or semi-colons<br/>- Numbers are parsed dependent on the language settings</html>";
    }

    @Override
    public Object getFormatExample() {
        return
            "<html>2581629.99 5683116<br/>2581706.999 5683180.99<br/>2581780.9 5683220.99<br/>2581852 5683282.9<br/>2581629.99 5683116</html>";
    }
}
