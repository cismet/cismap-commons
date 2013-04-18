/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.commons.converter.ConversionException;
import org.openide.util.lookup.ServiceProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service=GeometryConverter.class)
public final class GeomFromWkbConverter implements BinaryToGeometryConverter {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   from    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConversionException            DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public Geometry convertForward(final byte[] from, final String... params) throws ConversionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   to      DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConversionException            DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public byte[] convertBackward(final Geometry to, final String... params) throws ConversionException {
        throw new UnsupportedOperationException("Not supported yet.");
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
