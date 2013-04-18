
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.commons.converter.ConversionException;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class PolygonFromTextConverter implements TextToGeometryConverter
{

    @Override
    public Geometry convertForward(String from, String... params) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String convertBackward(Geometry to, String... params) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFormatName()
    {
        return "PolygonFromTextConverter";
    }

    @Override
    public String getFormatDisplayName()
    {
        return "Polygon from text converter";
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
