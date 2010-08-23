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

import de.cismet.cismap.commons.wms.capabilities.Operation;
import java.net.URL;
import java.util.List;
import org.deegree.owscommon_new.HTTP;


/**
 *
 * @author therter
 */
public class DeegreeOperation implements Operation {
    private org.deegree.owscommon_new.Operation op;


    public DeegreeOperation(org.deegree.owscommon_new.Operation op) {
        this.op = op;
    }


    @Override
    public URL getGet() {
        return getOnlineResource(true);
    }


    @Override
    public URL getPost() {
        return getOnlineResource(false);
    }


    private URL getOnlineResource(boolean get) {
        Object o = op.getDCP().get(0);

        if (o instanceof HTTP) {
            HTTP http = (HTTP)o;
            List<URL> urlList;

            if (get) {
                urlList = http.getGetOnlineResources();
            } else {
                urlList = http.getPostOnlineResources();
            }

            if ( urlList.size() > 0 ) {
                return urlList.get(0);
            }
        }

        return null;
    }
}
