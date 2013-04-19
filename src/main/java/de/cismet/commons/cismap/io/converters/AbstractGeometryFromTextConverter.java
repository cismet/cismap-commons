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

import java.util.Arrays;
import java.util.Locale;

import de.cismet.cismap.commons.CrsTransformer;

import de.cismet.commons.converter.ConversionException;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractGeometryFromTextConverter implements TextToGeometryConverter {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   from    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConversionException  DOCUMENT ME!
     */
    @Override
    public Geometry convertForward(final String from, final String... params) throws ConversionException {
        if (params.length < 1) {
            throw new ConversionException("no parameters provided, epsg is required parameter"); // NOI18N
        }

        final String[] tokens = from.split("([\\s;:])+"); // NOI18N

        System.out.println(Arrays.toString(tokens));

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

    /**
     * DOCUMENT ME!
     *
     * @param   coordinates  DOCUMENT ME!
     * @param   geomFactory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConversionException  DOCUMENT ME!
     */
    public abstract Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException;

    @Override
    public String convertBackward(final Geometry to, final String... params) throws ConversionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO: can provide generic format description
}
