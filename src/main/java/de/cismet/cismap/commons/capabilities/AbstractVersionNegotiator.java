/***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
package de.cismet.cismap.commons.capabilities;

import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
import de.cismet.security.exceptions.MissingArgumentException;
import de.cismet.security.exceptions.NoHandlerForURLException;
import de.cismet.security.exceptions.RequestFailedException;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractVersionNegotiator extends de.cismet.commons.capabilities.AbstractVersionNegotiator {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractVersionNegotiator object.
     */
    public AbstractVersionNegotiator() {
        super(null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * read the document from the given link.
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  a StringBuilder that contains the content of the given url
     *
     * @throws  MalformedURLException                DOCUMENT ME!
     * @throws  MissingArgumentException             DOCUMENT ME!
     * @throws  AccessMethodIsNotSupportedException  DOCUMENT ME!
     * @throws  RequestFailedException               DOCUMENT ME!
     * @throws  NoHandlerForURLException             DOCUMENT ME!
     * @throws  Exception                            DOCUMENT ME!
     */
    @Override
    protected StringBuilder readStringFromlink(final String url)
        throws MalformedURLException, MissingArgumentException, AccessMethodIsNotSupportedException, RequestFailedException, NoHandlerForURLException, Exception {
        final CapabilitiesCache cache = CapabilitiesCache.getInstance();

        return new StringBuilder(cache.calcValue(url));
    }
}
