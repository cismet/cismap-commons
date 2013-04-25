/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.commons.converter.ConversionException;

/**
 * Creates a geometry from a WKB that is provided in form of a hex string.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class GeomFromWkbAsHexTextConverter implements TextToGeometryConverter {

    //~ Instance fields --------------------------------------------------------

    private final transient GeomFromWkbConverter wkbConverter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeomFromWkbAsHexTextConverter object.
     */
    public GeomFromWkbAsHexTextConverter() {
        wkbConverter = new GeomFromWkbConverter();
    }

    //~ Methods ----------------------------------------------------------------

    // this is because of jalopy as for some reason it generates a javadoc template for this method although overridden
    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry convertForward(final String from, final String... params) throws ConversionException {
        if ((from == null) || from.isEmpty()) {
            throw new IllegalArgumentException("'from' must not be null or empty"); // NOI18N
        }

        final byte[] wkb;

        try {
            wkb = WKBReader.hexToBytes(from);
        } catch (final IllegalArgumentException e) {
            throw new ConversionException("cannot convert hex string to bytes", e); // NOI18N
        }

        return wkbConverter.convertForward(wkb, params);
    }

    @Override
    public String convertBackward(final Geometry to, final String... params) throws ConversionException {
        if (to == null) {
            throw new IllegalArgumentException("'to' must not be null"); // NOI18N
        }

        final byte[] wkb = wkbConverter.convertBackward(to, params);

        return WKBWriter.toHex(wkb);
    }

    @Override
    public String getFormatName() {
        return "GeomFromWkbAsTextConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return "Geometry from (E)WKB as hex text converter";
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return
            "- Assumes WKB format as hex text\n- Supports PostGIS EWKB format\n- Uses SRID of EWKB instead of current one if data is EWKB";
    }

    @Override
    public String getFormatHtmlDescription() {
        return
            "<html>- Assumes WKB format as hex text<br/>- Supports PostGIS EWKB format<br/>- Uses SRID of EWKB instead of current one if data is EWKB</html>";
    }

    @Override
    public Object getFormatExample() {
        return "<html>0101000020cd0b000014ae47e17a14f23f0000000000000040</html>";
    }
}
