/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;

import java.net.MalformedURLException;

import de.cismet.cismap.commons.capabilities.AbstractVersionNegotiator;
import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeWMSCapabilities;

import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
import de.cismet.security.exceptions.MissingArgumentException;
import de.cismet.security.exceptions.NoHandlerForURLException;
import de.cismet.security.exceptions.RequestFailedException;

/**
 * This class provides an implementation of a WMS GetCapabilities response parser.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WMSCapabilitiesFactory extends AbstractVersionNegotiator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(WMSCapabilitiesFactory.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   link  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  MalformedURLException                DOCUMENT ME!
     * @throws  MissingArgumentException             DOCUMENT ME!
     * @throws  AccessMethodIsNotSupportedException  DOCUMENT ME!
     * @throws  RequestFailedException               DOCUMENT ME!
     * @throws  NoHandlerForURLException             DOCUMENT ME!
     * @throws  ParserException                      DOCUMENT ME!
     * @throws  Exception                            DOCUMENT ME!
     */
    public synchronized WMSCapabilities createCapabilities(final String link) throws MalformedURLException,
        MissingArgumentException,
        AccessMethodIsNotSupportedException,
        RequestFailedException,
        NoHandlerForURLException,
        ParserException,
        Exception {
        String document = getCapabilitiesDocument(link);
        ByteArrayInputStream docStream = new ByteArrayInputStream(document.getBytes());
        WMSCapabilities result = null;
        String errorMsg = "";

        do {
            try {
                final StringBuilder builder = new StringBuilder(document);
                final String version = getDocumentVersion(builder);
                result = new DeegreeWMSCapabilities(docStream, link, version);
            } catch (Throwable th) {
                logger.warn("cannot parse the GetCapabilities document. Try to use an other version.", th); // NOI18N
                errorMsg = th.getMessage();
                // try to parse an older version of the GetCapabilities Document
                docStream.close();
                document = getOlderCapabilitiesDocument(link);
                if (document != null) {
                    docStream = new ByteArrayInputStream(document.getBytes());
                }
            }
        } while ((result == null) && (currentVersion != null) && (document != null));

        if (result == null) {
            logger.error("cannot parse the GetCapabilities document of the wms" + link); // NOI18N
            throw new ParserException(errorMsg);
        }
        docStream.close();
        return result;
    }

    @Override
    protected void initVersion() {
        supportedVersions = new String[5];
        supportedVersions[0] = "1.0";
        supportedVersions[1] = "1.1";
        supportedVersions[2] = "1.1.0";
        supportedVersions[3] = "1.1.1";
        supportedVersions[4] = "1.3.0";
        serviceName = "WMS";
    }
}
