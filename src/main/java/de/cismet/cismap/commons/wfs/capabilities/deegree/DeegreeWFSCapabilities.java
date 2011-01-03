/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfs.capabilities.deegree;

import org.apache.log4j.Logger;

import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import de.cismet.cismap.commons.capabilities.Service;
import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.ResponseParserFactory;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import de.cismet.cismap.commons.wfs.deegree.DeegreeFeatureTypeDescription;
import de.cismet.cismap.commons.wms.capabilities.FeatureTypeList;

/**
 * Parses WFS Capabilities documents.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeWFSCapabilities implements WFSCapabilities {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(DeegreeWFSCapabilities.class);

    //~ Instance fields --------------------------------------------------------

    private org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities cap;
    private URL url;
    private Service service;
    private WFSFacade facade;
    private FeatureTypeList list;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeWFSCapabilities object.
     *
     * @param   in    DOCUMENT ME!
     * @param   link  DOCUMENT ME!
     *
     * @throws  InvalidCapabilitiesException  DOCUMENT ME!
     * @throws  IOException                   DOCUMENT ME!
     * @throws  SAXException                  DOCUMENT ME!
     */
    public DeegreeWFSCapabilities(final InputStream in, final String link) throws InvalidCapabilitiesException,
        IOException,
        SAXException {
        String urlString = link;
        final WFSCapabilitiesDocument parser = new WFSCapabilitiesDocument();

        if (urlString.indexOf("?") != -1) {
            urlString = urlString.substring(0, urlString.indexOf("?"));
        }
        this.url = new URL(urlString);

        parser.load(in, link);
        cap = (org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities)parser.parseCapabilities();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FeatureTypeList getFeatureTypeList() throws IOException, Exception {
        if (list == null) {
            list = new FeatureTypeList();
            final org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList tmpList = cap.getFeatureTypeList();

            for (final WFSFeatureType type : tmpList.getFeatureTypes()) {
                final DeegreeFeatureType tmpType = new DeegreeFeatureType(type, this);
                list.put(tmpType.getName(), tmpType);
            }
        }

        return list;
    }

    @Override
    public Service getService() {
        if (service == null) {
            service = new DeegreeService(cap.getServiceProvider(), cap.getServiceIdentification());
        }

        return service;
    }

    @Override
    public String getVersion() {
        return cap.getVersion();
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public WFSFacade getServiceFacade() {
        if (facade == null) {
            facade = new WFSFacade(this, new DeegreeResponseParserFactory());
        }

        return facade;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DeegreeResponseParserFactory implements ResponseParserFactory {

        //~ Methods ------------------------------------------------------------

        @Override
        public FeatureTypeDescription getFeatureTypeDescription(final String featureTypeDescription,
                final FeatureType feature) throws ParserException {
            return new DeegreeFeatureTypeDescription(featureTypeDescription, feature);
        }
    }
}
