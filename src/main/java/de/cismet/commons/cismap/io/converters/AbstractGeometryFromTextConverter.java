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

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
public abstract class AbstractGeometryFromTextConverter extends AbstractRatingConverter<String, Geometry>
        implements TextToGeometryConverter {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AbstractGeometryFromTextConverter.class);

    public static final String SYS_PROP_DECIMAL_SEP;

    private static final char[] DEFAULT_TOKEN_SEPARATORS;
    private static final char[] WHITE_SPACE_CHARS;

    static {
        SYS_PROP_DECIMAL_SEP =
            "de.cismet.commons.cismap.io.convertes.AbstractGeometryFromTextConverter.decimalSeparator"; // NOI18N

        // in line with Character.isWithespace()
        WHITE_SPACE_CHARS = new char[] {
                // white space characters
                0x20,   // space
                0x2028, // line sep
                0x2029, // paragraph sep
                0xA0,   // no-break space
                0x2007, // figure space
                0x202F, // narrow no-break space
                0x09,   // tab
                0x0A,   // LF
                0x0B,   // vertical tab
                0x0C,   // form feed
                0x0D,   // CR
                0x1C,   // file sep
                0x1D,   // group sep
                0x1E,   // record sep
                0x1F    // unit sep
            };

        DEFAULT_TOKEN_SEPARATORS = new char[WHITE_SPACE_CHARS.length + 3];
        DEFAULT_TOKEN_SEPARATORS[0] = 0x3A; // colon
        DEFAULT_TOKEN_SEPARATORS[1] = 0x3B; // semicolon
        DEFAULT_TOKEN_SEPARATORS[2] = 0x2C; // comma

        System.arraycopy(WHITE_SPACE_CHARS, 0, DEFAULT_TOKEN_SEPARATORS, 3, WHITE_SPACE_CHARS.length);
    }

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

        final String[] tokens = from.split(getTokenRegex()); // NOI18N

        if ((tokens.length % 2) == 0) {
            final int srid;
            try {
                srid = CrsTransformer.extractSridFromCrs(params[0]);
            } catch (final Exception e) {
                throw new ConversionException("unsupported epsg parameter: " + params[0], e); // NOI18Ny
            }

            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);

            final Coordinate[] coordinates = new Coordinate[tokens.length / 2];
            final NumberFormat format = getDecimalFormat();

            for (int i = 0; i < tokens.length; i += 2) {
                try {
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
        final NumberFormat nf = getDecimalFormat();
        for (final Coordinate coord : to.getCoordinates()) {
            sb.append(nf.format(coord.x));
            sb.append(' ');
            sb.append(nf.format(coord.y));
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Gets the decimal separator that is used by this converter. The separator is read from the system property
     * {@link #SYS_PROP_DECIMAL_SEP}. If the property is not set or is empty or is an invalid character (any white
     * space) or an invalid format the default separator of the default locale is returned. Supported formats are:<br/>
     *
     * <ul>
     *   <li>those of {@link Integer#decode(java.lang.String)}</li>
     *   <li>a string starting with '&#92;u' or '&#92;U' followed by a hexadecimal character code</li>
     * </ul>
     * <br/>
     * If none of these formats is used simply the first char of the given string is returned.
     *
     * @return  the decimal separator to be used by this converter
     *
     * @see     Character#isWhitespace(char)
     * @see     Integer#decode(java.lang.String)
     */
    protected char getDecimalSeparator() {
        final String systemSep = System.getProperty(SYS_PROP_DECIMAL_SEP);

        char c;
        if ((systemSep == null) || (systemSep.length() == 0)) {
            c = ((DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault())).getDecimalFormatSymbols()
                        .getDecimalSeparator();
        } else {
            try {
                c = (char)Integer.decode(systemSep).intValue();
            } catch (final NumberFormatException e) {
                // not encoded according to §3.10.1 Java language sepc
                if (systemSep.startsWith("\\u") || systemSep.startsWith("\\U")) { // NOI18N
                    try {
                        // only hex is accepted when using this notation
                        c = (char)Integer.parseInt(systemSep.substring(2), 16);
                    } catch (final NumberFormatException ex) {
                        LOG.warn("unrecognized separator format '" + systemSep + "', using locale default", ex); // NOI18N

                        c = ((DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault()))
                                    .getDecimalFormatSymbols().getDecimalSeparator();
                    }
                } else {
                    c = systemSep.charAt(0);
                }
            }

            if (Character.isWhitespace(c)) {
                LOG.warn("white space chars not accepted as decimal separator, using locale default"); // NOI18N
                c = ((DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault())).getDecimalFormatSymbols()
                            .getDecimalSeparator();
            }
        }

        return c;
    }

    /**
     * Creates a decimal format that uses the decimal separator of returned from {@link #getDecimalSeparator()} and
     * disabled grouping.
     *
     * @return  the decimal format used by this converter
     */
    protected DecimalFormat getDecimalFormat() {
        final DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault());
        final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        final char decimalSep = getDecimalSeparator();

        symbols.setDecimalSeparator(decimalSep);
        symbols.setGroupingSeparator((char)0);

        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);

        return format;
    }

    /**
     * Get the token regex that is used to split the data to convert. This regex never contains the <code>char</code>
     * returned from {@link #getDecimalSeparator()}.
     *
     * @return  the token regex that is used to split the data to convert
     */
    protected String getTokenRegex() {
        final char decimalSep = getDecimalSeparator();

        final StringBuilder sb = new StringBuilder("["); // NOI18N
        for (final char sep : DEFAULT_TOKEN_SEPARATORS) {
            if (sep != decimalSep) {
                sb.append(sep);
            }
        }
        sb.append("]+");                                 // NOI18N

        return sb.toString();
    }
}
