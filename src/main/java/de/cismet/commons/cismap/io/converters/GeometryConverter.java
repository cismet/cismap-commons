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
import de.cismet.commons.converter.Converter;
import de.cismet.commons.converter.FormatHint;

/**
 * Converts a given object to a geometry using a specific EPSG.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public interface GeometryConverter<FROM extends Object> extends Converter<FROM, Geometry>, FormatHint {

    //~ Methods ----------------------------------------------------------------

    /**
     * Converts a given instance <code>FROM</code> to a <code>Geometry</code>. The first parameter must be the EPSG
     * code, e.g. EPSG:4326.
     *
     * @param   from    the originating object that shall be converted to a <code>Geometry</code>
     * @param   params  first parameter must be the EPSG code
     *
     * @return  an instance of <code>Geometry</code> which was create from the <code>FROM</code> instance
     *
     * @throws  ConversionException  if any error occurs during conversion
     */
    @Override
    Geometry convertForward(FROM from, final String... params) throws ConversionException;

    /**
     * Converts a given instance of <code>Geometry</code> back to the origin format <code>FROM</code>. The first
     * parameter must be the EPSG code, e.g. EPSG:4326.
     *
     * @param   to      the geometry that shall be converted back to an instance of <code>FROM</code>
     * @param   params  no parameters needed
     *
     * @return  an instance of <code>FROM</code> which was create from the <code>Geometry</code> instance
     *
     * @throws  ConversionException  if any error occurs during conversion
     */
    @Override
    FROM convertBackward(Geometry to, final String... params) throws ConversionException;
}
