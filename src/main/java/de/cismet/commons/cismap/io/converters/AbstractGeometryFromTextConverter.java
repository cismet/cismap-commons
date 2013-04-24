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
import com.vividsolutions.jts.geom.PrecisionModel;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Locale;

import de.cismet.cismap.commons.CrsTransformer;

import de.cismet.commons.converter.ConversionException;

/**
 * Basic <code>TextToGeometryConverter</code> implementation that expects the given text to be separated by white space
 * characters, ';' or ':'. Additionally it requires a parameter: the EPSG code, e.g. EPSG:4326. The code is used to
 * interpret the given coordinates. The coordinates are expected to be provided as follows:<br/>
 * <br/>
 * northing&lt;separator&gt;easting&lt;separator&gt;northing&lt;separator&gt;easting&lt;separator&gt; ...<br/>
 * <br/>
 *
 * <p>The coordinates are parsed using the currently active {@link Locale} and thus the corresponding
 * {@link NumberFormat}. Three-dimensional coordinates are not supported.</p>
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public abstract class AbstractGeometryFromTextConverter implements TextToGeometryConverter {

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a geometry from the given coordinate array and geometry factory. The coordinate array and the geomeetry
     * factory shall never be <code>null</code>.
     *
     * @param   coordinates  the coordinates to create a geometry from
     * @param   geomFactory  the geometry factory that may be used to create the geometry
     *
     * @return  a geometry created using the given parameters
     *
     * @throws  ConversionException  if any error occurs during creation of the geometry
     */
    protected abstract Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException;

    // this is because of jalopy as for some reason it generates a javadoc template for this method although overridden
    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry convertForward(final String from, final String... params) throws ConversionException {
        if ((from == null) || from.isEmpty()) {
            throw new IllegalArgumentException("from must not be null or empty");                         // NOI18N
        }
        if ((params == null) || (params.length < 1)) {
            throw new IllegalArgumentException("no parameters provided, epsgcode is required parameter"); // NOI18N
        }

        final String[] tokens = from.split("([\\s;:])+"); // NOI18N

        if ((tokens.length % 2) == 0) {
            final int srid;
            try {
                srid = CrsTransformer.extractSridFromCrs(params[0]);
            } catch (final Exception e) {
                throw new ConversionException("unsupported epsg parameter: " + params[0], e); // NOI18Ny
            }

            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);

            final Coordinate[] coordinates = new Coordinate[tokens.length / 2];

            for (int i = 0; i < tokens.length; i += 2) {
                try {
                    final NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
                    final double easting = format.parse(tokens[i]).doubleValue();
                    final double northing = format.parse(tokens[i + 1]).doubleValue();

                    coordinates[i / 2] = new Coordinate(easting, northing);
                } catch (final ParseException ex) {
                    throw new ConversionException("cannot parse convert data into valid double", ex); // NOI18N
                }
            }

            return createGeometry(coordinates, geomFactory);
        } else {
            throw new ConversionException("uneven number of tokens illegal, only two dimensional coordinates allowed"); // NOI18N
        }
    }

    @Override
    public String convertBackward(final Geometry to, final String... params) throws ConversionException {
        if (to == null) {
            throw new IllegalArgumentException("'to' must not be null"); // NOI18N
        }

        final StringBuilder sb = new StringBuilder();
        final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        for (final Coordinate coord : to.getCoordinates()) {
            sb.append(nf.format(coord.x));
            sb.append(' ');
            sb.append(nf.format(coord.y));
            sb.append('\n');
        }

        return sb.toString();
    }

    // TODO: can provide generic format description
}
