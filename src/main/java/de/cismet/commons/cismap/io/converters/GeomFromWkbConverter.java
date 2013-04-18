
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.commons.converter.ConversionException;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class GeomFromWkbConverter implements BinaryToGeometryConverter
{

    @Override
    public Geometry convertForward(byte[] from, String... params) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] convertBackward(Geometry to, String... params) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFormatName()
    {
        return "GeomFromWkbConverter";
    }

    @Override
    public String getFormatDisplayName()
    {
        return "Geometry from WKB converter";
    }

    @Override
    public String getFormatHtmlName()
    {
        return null;
    }

    @Override
    public String getFormatDescription()
    {
        return null;
    }

    @Override
    public String getFormatHtmlDescription()
    {
        return null;
    }

    @Override
    public Object getFormatExample()
    {
        return null;
    }
}
