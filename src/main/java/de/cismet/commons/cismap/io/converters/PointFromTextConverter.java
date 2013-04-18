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
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = GeometryConverter.class)
public final class PointFromTextConverter extends AbstractGeometryFromTextConverter {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        if (coordinates.length < 1) {
            throw new ConversionException("cannot create point from empty coordinate array");
        }

        return geomFactory.createPoint(coordinates[0]);
    }

    @Override
    public String getFormatName() {
        return "PointFromTextConverter";
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
