/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

/**
 * Simple interface to further specify that the respective <code>GeometryConverter</code> will take a <code>
 * byte[]</code> as input.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public interface BinaryToGeometryConverter extends GeometryConverter<byte[]> {
}
