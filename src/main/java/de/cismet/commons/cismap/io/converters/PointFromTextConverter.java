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
        final String desc = NbBundle.getMessage(
                PointFromTextConverter.class,
                "PointFromTextConverter.getFormatDescription().returnValue"); // NOI18N
        final String superDesc = super.getFormatDescription();

        return desc + "\n" + superDesc;
    }

    @Override
    public String getFormatHtmlDescription() {
        final String desc = NbBundle.getMessage(
                    PointFromTextConverter.class,
                    "PointFromTextConverter.getFormatHtmlDescription().returnValue")
                    .replaceAll("<[/]?html>", "");                                              // NOI18N
        final String superDesc = super.getFormatHtmlDescription().replaceAll("<[/]?html>", ""); // NOI18N;

        return "<html>" + desc + "<br/>" + superDesc + "</html>"; // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "PointFromTextConverter.getFormatExample().returnValue",
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

        if (tokens.length < 2) {
            return 0;
        } else if (tokens.length > 2) {
            return 50;
        } else {
            // 2 tokens = 1 coordinates
            return 100;
        }
    }
}
