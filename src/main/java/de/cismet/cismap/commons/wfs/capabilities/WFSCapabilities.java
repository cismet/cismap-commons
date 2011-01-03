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

import java.io.IOException;

import java.net.URL;

import de.cismet.cismap.commons.capabilities.Service;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.cismap.commons.wms.capabilities.FeatureTypeList;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface WFSCapabilities {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    FeatureTypeList getFeatureTypeList() throws IOException, Exception;
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Service getService();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVersion();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    URL getURL();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    WFSFacade getServiceFacade();
}
