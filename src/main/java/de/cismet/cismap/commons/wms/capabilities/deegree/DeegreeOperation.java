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
package de.cismet.cismap.commons.wms.capabilities.deegree;

import org.deegree.owscommon_new.HTTP;

import java.net.URL;

import java.util.List;

import de.cismet.cismap.commons.wms.capabilities.Operation;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeOperation implements Operation {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.owscommon_new.Operation op;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeOperation object.
     *
     * @param  op  DOCUMENT ME!
     */
    public DeegreeOperation(final org.deegree.owscommon_new.Operation op) {
        this.op = op;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public URL getGet() {
        return getOnlineResource(true);
    }

    @Override
    public URL getPost() {
        return getOnlineResource(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   get  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private URL getOnlineResource(final boolean get) {
        final Object o = op.getDCP().get(0);

        if (o instanceof HTTP) {
            final HTTP http = (HTTP)o;
            List<URL> urlList;

            if (get) {
                urlList = http.getGetOnlineResources();
            } else {
                urlList = http.getPostOnlineResources();
            }

            if (urlList.size() > 0) {
                return urlList.get(0);
            }
        }

        return null;
    }
}
