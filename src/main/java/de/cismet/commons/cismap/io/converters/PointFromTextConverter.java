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
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "PointFromTextConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "PointFromTextConverter.getFormatDescription().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlDescription() {
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "PointFromTextConverter.getFormatHtmlDescription().returnValue"); // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "PointFromTextConverter.getFormatExample().returnValue"); // NOI18N
    }
}
