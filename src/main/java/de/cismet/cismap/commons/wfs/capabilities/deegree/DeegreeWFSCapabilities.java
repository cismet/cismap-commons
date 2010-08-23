package de.cismet.cismap.commons.wfs.capabilities.deegree;

import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.cismap.commons.capabilities.Service;
import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.ResponseParserFactory;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wms.capabilities.FeatureTypeList;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import de.cismet.cismap.commons.wfs.deegree.DeegreeFeatureTypeDescription;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.xml.sax.SAXException;


/**
 * Parses WFS Capabilities documents.
 * @author therter
 */
public class DeegreeWFSCapabilities implements WFSCapabilities {
    private static final Logger logger = Logger.getLogger(DeegreeWFSCapabilities.class);
    private org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities cap;
    private URL url;
    private Service service;
    private WFSFacade facade;
    private FeatureTypeList list;

    
    public DeegreeWFSCapabilities(InputStream in, String link) throws InvalidCapabilitiesException,
            IOException, SAXException {
        String urlString = link;
        WFSCapabilitiesDocument parser = new WFSCapabilitiesDocument();

        if (urlString.indexOf("?") != -1) {
            urlString = urlString.substring(0, urlString.indexOf("?"));
        }
        this.url = new URL(urlString);

        parser.load(in, link);
        cap = (org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities)parser.parseCapabilities();
    }


    @Override
    public FeatureTypeList getFeatureTypeList() throws IOException, BadHttpStatusCodeException {
        if (list == null) {
            list = new FeatureTypeList();
            org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList tmpList = cap.getFeatureTypeList();

            for (WFSFeatureType type : tmpList.getFeatureTypes()) {
                DeegreeFeatureType tmpType = new DeegreeFeatureType(type, this);
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

    private class DeegreeResponseParserFactory implements ResponseParserFactory {
        @Override
        public FeatureTypeDescription getFeatureTypeDescription(String featureTypeDescription, FeatureType feature) throws ParserException {
            return new DeegreeFeatureTypeDescription(featureTypeDescription, feature);
        }
    }
}
