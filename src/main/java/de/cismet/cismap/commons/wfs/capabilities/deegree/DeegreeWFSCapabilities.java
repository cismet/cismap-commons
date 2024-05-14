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

import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.ResponseParserFactory;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.FeatureTypeList;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import de.cismet.cismap.commons.wfs.deegree.DeegreeFeatureTypeDescription;

import de.cismet.commons.capabilities.Service;

import de.cismet.tools.CalculationCache;
import de.cismet.tools.Calculator;

/**
 * Parses WFS Capabilities documents.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeWFSCapabilities implements WFSCapabilities {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DeegreeWFSCapabilities.class);

    //~ Instance fields --------------------------------------------------------

    private final org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities cap;
    private final CalculationCache<String, FeatureTypeList> cache = new CalculationCache<String, FeatureTypeList>(
            new FeatureTypeListRetriever());
    private final URL url;
    private Service service;
    private WFSFacade facade;
    private final String originalLink;

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
        originalLink = link;
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

    /**
     * DOCUMENT ME!
     *
     * @return  the originalLink
     */
    @Override
    public String getOriginalLink() {
        return originalLink;
    }

    @Override
    public FeatureTypeList getFeatureTypeList() throws IOException, Exception {
        return cache.calcValue(originalLink);
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
    private class FeatureTypeListRetriever implements Calculator<String, FeatureTypeList> {

        //~ Methods ------------------------------------------------------------

        @Override
        public FeatureTypeList calculate(final String input) throws Exception {
            final FeatureTypeList list = new FeatureTypeList();
            final org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList tmpList = cap.getFeatureTypeList();

            for (final WFSFeatureType type : tmpList.getFeatureTypes()) {
                if (type == null) {
                    LOG.error("TEST feature type == null " + type);
                }
                final DeegreeFeatureType tmpType = new DeegreeFeatureType(type, DeegreeWFSCapabilities.this, input);
                list.put(tmpType.getName(), tmpType);
            }

            return list;
        }
    }

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
