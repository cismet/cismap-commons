/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
package de.cismet.cismap.commons.wfs.capabilities;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;

import de.cismet.cismap.commons.capabilities.AbstractVersionNegotiator;
import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.wfs.capabilities.deegree.DeegreeWFSCapabilities;

import de.cismet.tools.CalculationCache;
import de.cismet.tools.Calculator;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WFSCapabilitiesFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(WFSCapabilitiesFactory.class);
    private static final CalculationCache<String, WFSCapabilities> cache;

    static {
        cache = new CalculationCache<String, WFSCapabilities>(new InternalWFSCapabilitiesFactory());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   link  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public WFSCapabilities createCapabilities(final String link) throws Exception {
        return cache.calcValue(link);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class InternalWFSCapabilitiesFactory extends AbstractVersionNegotiator
            implements Calculator<String, WFSCapabilities> {

        //~ Methods ------------------------------------------------------------

        @Override
        public WFSCapabilities calculate(final String link) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("create capabilities from link: " + link);
            }
            String document = getCapabilitiesDocument(link);
            document = document.replace("xlink:href=\"\"", "xlink:href=\"http://test.de\"");
            ByteArrayInputStream docStream = new ByteArrayInputStream(document.getBytes());
            WFSCapabilities result = null;
            String errorMsg = "";

            do {
                try {
                    result = new DeegreeWFSCapabilities(docStream, link);
                } catch (Throwable th) {
                    logger.warn("cannot parse the Getcapabilities document try to use an other version.", th); // NOI18N
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
                logger.error("cannot parse the GetCapabilities document of the wfs" + link); // NOI18N
                throw new ParserException(errorMsg);
            }
            docStream.close();

            return result;
        }

        @Override
        protected void initVersion() {
            supportedVersions = new String[2];
            supportedVersions[0] = "1.0.0";
            supportedVersions[1] = "1.1.0";
            serviceName = "WFS";
        }
    }
}
