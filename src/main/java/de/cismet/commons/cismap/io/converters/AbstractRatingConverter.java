/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import de.cismet.commons.converter.Converter;
import de.cismet.commons.converter.Converter.MatchRating;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractRatingConverter<FROM, TO> implements Converter<FROM, TO>, MatchRating<FROM> {

    //~ Methods ----------------------------------------------------------------

    @Override
    public int rate(final FROM from, final String... params) {
        if (from == null) {
            return 0;
        }

        try {
            final TO result = convertForward(from, params);

            return (result == null) ? 0 : 100;
        } catch (final Exception e) {
            // conversion failed
            return 0;
        }
    }
}
