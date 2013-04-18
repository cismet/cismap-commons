/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.commons.converter.Converter;
import de.cismet.commons.converter.FormatHint;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface GeometryConverter<FROM extends Object> extends Converter<FROM, Geometry>, FormatHint {
}
