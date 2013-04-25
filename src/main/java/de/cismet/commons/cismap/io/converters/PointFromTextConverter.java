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

import org.openide.util.lookup.ServiceProvider;

import de.cismet.commons.converter.ConversionException;

/**
 * Creates a point geometry from the provided coordinates. At least one coordinate is expected. If there are more they
 * are ignored.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class PointFromTextConverter extends AbstractGeometryFromTextConverter {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        if (coordinates.length < 1) {
            throw new ConversionException("cannot create point from empty coordinate array"); // NOI18N
        }

        return geomFactory.createPoint(coordinates[0]);
    }

    @Override
    public String getFormatName() {
        return "PointFromTextConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return "Point from text converter";
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return
            "- Requires one coordinate\n- Surplus coordinates are ignored\n- Assumes 'easting<sep>northing<sep>easting<sep>...' pattern\n- <sep> may be white-spaces (space, tab, enter, etc.), colons or semi-colons\n- Numbers are parsed dependent on the language settings";
    }

    @Override
    public String getFormatHtmlDescription() {
        return
            "<html>- Requires one coordinate<br/>- Surplus coordinates are ignored<br/>- Assumes 'easting&lt;sep&gt;northing&lt;sep&gt;easting&lt;sep&gt;...' pattern<br/>- &lt;sep&gt; may be white-spaces (space, tab, enter, etc.), colons or semi-colons<br/>- Numbers are parsed dependent on the language settings</html>";
    }

    @Override
    public Object getFormatExample() {
        return "<html>2581629.99 5683116</html>";
    }
}
