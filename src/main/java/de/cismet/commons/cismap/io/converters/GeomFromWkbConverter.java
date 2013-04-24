/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.cismap.commons.CrsTransformer;

import de.cismet.commons.converter.ConversionException;

/**
 * Creates a geometry from (E)WKB. However, the conversion back from a geometry produces WKB only. If the input is EWKB
 * a possibly provided EPSG is ignored. Additionally the backwards conversion can take an additional parameter, the
 * endian. By default the result of the conversion is a byte array with little endian encoding. Iff the first parameter
 * equals 'XDR' (ignoring case) the big endian encoding will be used. This behaviour is equal to the ST_AsBinary
 * function of PostGIS. The result of the backwards conversion is always two-dimensional.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = GeometryConverter.class)
public final class GeomFromWkbConverter implements BinaryToGeometryConverter {

    //~ Methods ----------------------------------------------------------------

    // this is because of jalopy as for some reason it generates a javadoc template for this method although overridden
    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry convertForward(final byte[] from, final String... params) throws ConversionException {
        if ((from == null) || (from.length == 0)) {
            throw new IllegalArgumentException("'from' must not be null or empty");                       // NOI18N
        }
        if ((params == null) || (params.length < 1)) {
            throw new IllegalArgumentException("no parameters provided, epsgcode is required parameter"); // NOI18N
        }

        final int srid;
        try {
            srid = CrsTransformer.extractSridFromCrs(params[0]);
        } catch (final Exception e) {
            throw new ConversionException("unsupported epsg parameter: " + params[0], e); // NOI18Ny
        }

        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        final WKBReader wkbReader = new WKBReader(geomFactory);
        try {
            return wkbReader.read(from);
        } catch (final ParseException ex) {
            throw new ConversionException("cannot create geometry from WKT: " + from, ex); // NOI18N
        }
    }

    @Override
    public byte[] convertBackward(final Geometry to, final String... params) throws ConversionException {
        if (to == null) {
            throw new IllegalArgumentException("'to' must not be null"); // NOI18N
        }

        int endian = ByteOrderValues.LITTLE_ENDIAN;
        if ((params != null)
                    && (params.length > 0)
                    && (params[0] != null)
                    && "XDR".equalsIgnoreCase(params[0])) {
            endian = ByteOrderValues.BIG_ENDIAN;
        }

        final WKBWriter wkbWriter = new WKBWriter(2, endian);

        return wkbWriter.write(to);
    }

    @Override
    public String getFormatName() {
        return "GeomFromWkbConverter";
    }

    @Override
    public String getFormatDisplayName() {
        return "Geometry from WKB converter";
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
