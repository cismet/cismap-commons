package de.cismet.cismap.commons.capabilities;

import de.cismet.security.WebAccessManager;
import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
import de.cismet.security.exceptions.MissingArgumentException;
import de.cismet.security.exceptions.NoHandlerForURLException;
import de.cismet.security.exceptions.RequestFailedException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 *
 * @author therter
 */
public abstract class AbstractVersionNegotiator {
    private static Logger logger = Logger.getLogger(AbstractVersionNegotiator.class);
    private final static String VERSION_STRING = "version";//NOI18N
    // the constant SUPPORTED_VERSIONS contains all supported service versions. The array should
    // be sorted and the first elements of the array should contain the oldest version.
    protected String[] supportedVersions;//NOI18N
    protected String currentVersion = null;
    protected String serviceName;

    
    public AbstractVersionNegotiator() {
        initVersion();
    }


    /**
     * This m,ethod must be overidden by the sub classes and should initialize the constant
     * SUPPORTED_VERSIONS.
     *
     */
    protected abstract void initVersion();

    /**
     * Invokes the GetCapabilities operation of the server with an older version string
     * @param link
     * @return
     */
    protected String getOlderCapabilitiesDocument(String link) throws MalformedURLException, MissingArgumentException,
            AccessMethodIsNotSupportedException, RequestFailedException, NoHandlerForURLException, Exception {
        StringBuilder document;
        boolean olderVersionFound = false;

        logger.debug("try to use an older version number");//NOI18N
        for (int i = 1; i < supportedVersions.length; ++i) {
            if (supportedVersions[i].equals( currentVersion ) ) {
                currentVersion = supportedVersions[i-1];
                olderVersionFound = true;
            }
        }

        if (olderVersionFound) {
            logger.debug("Older version found " + currentVersion);//NOI18N
            if ( link.toLowerCase().indexOf("?") != -1 ) {//NOI18N
                link = link.substring(0, link.toLowerCase().indexOf("?"));//NOI18N
            }

            link += "?SERVICE=" + serviceName + "&REQUEST=GetCapabilities&VERSION=" + currentVersion;//NOI18N
            document = readStringFromlink(link);

            return document.toString();
        }

        return null;
    }

    /**
     *
     * @param link
     * @return the capabilities document of the service with the given link. If the link already  contains
     * a version, then this version will be used as start version for the version negotation. Otherwise, the
     * most recent version, that is supported by the client will be used as start version.
     * @throws MalformedURLException
     * @throws MissingArgumentException
     * @throws AccessMethodIsNotSupportedException
     * @throws RequestFailedException
     * @throws NoHandlerForURLException
     * @throws Exception
     */
    protected String getCapabilitiesDocument(String link) throws MalformedURLException, MissingArgumentException,
            AccessMethodIsNotSupportedException, RequestFailedException, NoHandlerForURLException, Exception {
        String startVersion = getVersionFromLink(link);
        StringBuilder document;

        if (startVersion == null) {
            logger.debug("No version string found in the link. Add parameter");//NOI18N
            if (link.indexOf("?") != -1) {//NOI18N
                link = link.substring(0, link.indexOf("?"));//NOI18N
            }

            //set the version to the latest one
            startVersion = supportedVersions[supportedVersions.length - 1];
            link += "?SERVICE=" + serviceName + "&REQUEST=GetCapabilities&VERSION=" + startVersion;//NOI18N
        }

        logger.debug("start version = " + startVersion);//NOI18N
        document = readStringFromlink(link);
        currentVersion = getDocumentVersion(document);

        if (currentVersion == null) {
            logger.error("No version string found in the GetCapabilities document from location " + link);//NOI18N
            // try to parse the document
            return document.toString();
        }

        if ( !isVersionSupported(currentVersion) ) {
            logger.error("The client does not support the version of the received Getcapabilities document." +//NOI18N
                    "\nLink: " + link + "\nresponse version " + currentVersion);//NOI18N
        } else {
            logger.debug("Version negotation successfully. \nLink: " //NOI18N
                    + link + "\nresponse version " + currentVersion);//NOI18N
        }

        return document.toString();
    }

    /**
     *
     * @param link
     * @return the version string of the given link or null, if the given link
     * does not contain any version string
     */
    private String getVersionFromLink(String link) {
        String version = null;

        if ( link.toLowerCase().indexOf(VERSION_STRING) != -1 ) {
            version = link.substring( link.toLowerCase().indexOf(VERSION_STRING) + VERSION_STRING.length() + 1 );

            if (version.indexOf("&") != -1) {//NOI18N
                version = version.substring(0, version.indexOf("&"));//NOI18N
            }
        }

        return version;
    }



    /**
     *
     * @param version the version that should be checked
     * @return true, if the given version is contained in the array constant <code>SUPPORTED_VERSIONS</code>
     */
    private boolean isVersionSupported(String version) {
        for (int i = 0; i < supportedVersions.length; ++i) {
            if ( supportedVersions[i].equals(version) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param document the GetCapabilities document
     * @return the version of the given document or null, if no version string found
     */
    private String getDocumentVersion(StringBuilder document) {
        String documentVersion = null;
        int startIndexOfVersion = -1;

        if (document.indexOf("?>") != -1) {//NOI18N
            startIndexOfVersion = document.indexOf( VERSION_STRING + "=\"", document.indexOf("?>") )//NOI18N
                    + VERSION_STRING.length() + 2;//NOI18N
        } else {
            startIndexOfVersion = document.indexOf( VERSION_STRING + "=\"" ) + VERSION_STRING.length() + 2;//NOI18N
        }

        if ( startIndexOfVersion != (VERSION_STRING.length() + 1) ) {
            //version string in document found
            int endIndexOfVersion = document.indexOf("\"", startIndexOfVersion );//NOI18N
            documentVersion = document.substring(startIndexOfVersion, endIndexOfVersion);
        }

        logger.debug("version of received GetCapabilities document: " + documentVersion);//NOI18N
        return documentVersion;
    }

    /**
     * read the document from the given link
     * @param url
     *
     * @return a StringBuilder that contains the content of the given url
     */
    private StringBuilder readStringFromlink(String url)  throws MalformedURLException,
            MissingArgumentException, AccessMethodIsNotSupportedException, RequestFailedException,
            NoHandlerForURLException, Exception {
        StringBuilder sb = new StringBuilder("");//NOI18N
        logger.debug("send Getcapabilities request to the service: " + url);//NOI18N
        URL getCapURL = new URL(url);
        InputStream is = WebAccessManager.getInstance().doRequest(getCapURL);
        BufferedReader br = new BufferedReader( new InputStreamReader(is));
        String buffer = br.readLine();

        while (buffer != null) {
            sb.append(buffer + "\n");//NOI18N
            buffer = br.readLine();
        }

        is.close();

        return sb;
    }
}
