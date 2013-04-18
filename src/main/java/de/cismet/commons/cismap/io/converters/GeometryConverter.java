
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.commons.converter.Converter;
import de.cismet.commons.converter.FormatHint;

/**
 *
 * @author martin.scholl@cismet.de
 */
public interface GeometryConverter<FROM extends Object> extends Converter<FROM, Geometry>, FormatHint
{

}
