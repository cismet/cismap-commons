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
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = GeometryConverter.class)
public final class PolygonFromTextConverter extends AbstractGeometryFromTextConverter {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        final LinearRing ring = geomFactory.createLinearRing(coordinates);

        return geomFactory.createPolygon(ring, null);
    }

    @Override
    public String getFormatName() {
        return "PolygonFromTextConverter";
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
