package de.cismet.cismap.commons.wms.capabilities;

import de.cismet.cismap.commons.capabilities.AbstractVersionNegotiator;
import de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeWMSCapabilities;
import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
import de.cismet.security.exceptions.MissingArgumentException;
import de.cismet.security.exceptions.NoHandlerForURLException;
import de.cismet.security.exceptions.RequestFailedException;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

/**
 * This class provides an implementation of a WMS GetCapabilities response parser.
 * @author therter
 */
public class WMSCapabilitiesFactory extends AbstractVersionNegotiator {
    private static final Logger logger = Logger.getLogger(WMSCapabilitiesFactory.class);

    public synchronized WMSCapabilities createCapabilities(String link) throws MalformedURLException,
            MissingArgumentException, AccessMethodIsNotSupportedException, RequestFailedException,
            NoHandlerForURLException, Exception {
        String document = getCapabilitiesDocument(link);
        ByteArrayInputStream docStream = new ByteArrayInputStream(document.getBytes());
        WMSCapabilities result = null;

        do {
            try {
                result = new DeegreeWMSCapabilities(docStream, link);
            } catch (Throwable th) {
                logger.warn("cannot parse the GetCapabilities document. Try to use an other version.", th);//NOI18N
                // try to parse an older version of the GetCapabilities Document
                docStream.close();
                document = getOlderCapabilitiesDocument(link);
                if (document != null) {
                    docStream = new ByteArrayInputStream(document.getBytes());
                }
            }
        } while (result == null && currentVersion != null && document != null);

        if (result == null) {
            logger.error("cannot parse the GetCapabilities document of the wms" + link);//NOI18N
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
