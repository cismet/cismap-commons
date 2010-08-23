/*
 *  Copyright (C) 2010 therter
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Request;
import de.cismet.cismap.commons.capabilities.Service;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.xml.sax.SAXException;

/**
 *
 * @author therter
 */
public class DeegreeWMSCapabilities implements WMSCapabilities {
    private static final Logger logger = Logger.getLogger(DeegreeWMSCapabilities.class);
    private org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities cap;
    private URL url;

    
    public DeegreeWMSCapabilities(InputStream in, String nameID) 
            throws InvalidCapabilitiesException, IOException, SAXException {
        String urlString = nameID;
        WMSCapabilitiesDocument parser = new WMSCapabilitiesDocument();

        if (urlString.indexOf("?") != -1) {
            urlString = urlString.substring(0, urlString.indexOf("?"));
        }
        this.url = new URL(urlString);

        parser.load(in, XMLFragment.DEFAULT_URL);
        cap = (org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities)parser.parseCapabilities();
    }


    @Override
    public Service getService() {
        return new DeegreeService(cap.getServiceProvider(), cap.getServiceIdentification());
    }


    @Override
    public Request getRequest() {
        return new DeegreeRequest( cap.getOperationMetadata() );
    }


    @Override
    public Layer getLayer() {
        return new DeegreeLayer(cap.getLayer());
    }


    @Override
    public String getVersion() {
        return cap.getVersion();
    }

    @Override
    public URL getURL() {
        return url;
    }
}
