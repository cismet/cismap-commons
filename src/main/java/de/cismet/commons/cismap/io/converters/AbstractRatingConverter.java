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
 * Abstract implementation of a match rating converter that uses the <code>convertForward</code> operation to determine
 * if the entry is feasible or not. However, it can only provide a boolean rating, thus any successfully converted
 * non-null result of <code>convertForward</code> will result in a score of 100. Any other case will result in a score
 * of 0.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
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
