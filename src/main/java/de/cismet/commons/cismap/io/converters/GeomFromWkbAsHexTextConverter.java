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

import org.openide.util.NbBundle;
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
        return NbBundle.getMessage(
                GeomFromWkbAsHexTextConverter.class,
                "GeomFromWkbAsHexTextConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return NbBundle.getMessage(
                GeomFromWkbAsHexTextConverter.class,
                "GeomFromWkbAsHexTextConverter.getFormatDescription().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlDescription() {
        return NbBundle.getMessage(
                GeomFromWkbAsHexTextConverter.class,
                "GeomFromWkbAsHexTextConverter.getFormatHtmlDescription().returnValue"); // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                GeomFromWkbAsHexTextConverter.class,
                "GeomFromWkbAsHexTextConverter.getFormatExample().returnValue"); // NOI18N
    }
}
