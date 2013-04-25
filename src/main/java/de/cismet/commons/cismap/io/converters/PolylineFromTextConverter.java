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
 * Creates a line string geometry from the provided coordinates. At least two coordinates are expected.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class PolylineFromTextConverter extends AbstractGeometryFromTextConverter {

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
        return "Polyline from text converter";
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return
            "- Requires at least two coordinates\n- Assumes 'easting<sep>northing<sep>easting<sep>...' pattern\n- <sep> may be white-spaces (space, tab, enter, etc.), colons or semi-colons\n- Numbers are parsed dependent on the language settings";
    }

    @Override
    public String getFormatHtmlDescription() {
        return
            "<html>- Requires at least two coordinates<br/>- Assumes 'easting&lt;sep&gt;northing&lt;sep&gt;easting&lt;sep&gt;...' pattern<br/>- &lt;sep&gt; may be white-spaces (space, tab, enter, etc.), colons or semi-colons<br/>- Numbers are parsed dependent on the language settings</html>";
    }

    @Override
    public Object getFormatExample() {
        return
            "<html>2581629.99 5683116<br/>2581706.999 5683180.99<br/>2581780.9 5683220.99<br/>2581852 5683282.9<br/>2581793.0 5683366.7</html>";
    }
}
