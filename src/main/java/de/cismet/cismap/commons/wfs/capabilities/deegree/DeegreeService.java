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
package de.cismet.cismap.commons.wfs.capabilities.deegree;

import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;

import java.util.ArrayList;

import de.cismet.cismap.commons.capabilities.Service;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeService implements Service {

    //~ Instance fields --------------------------------------------------------

    private ServiceProvider service;
    private ServiceIdentification ident;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeService object.
     *
     * @param  service  DOCUMENT ME!
     * @param  ident    DOCUMENT ME!
     */
    public DeegreeService(final ServiceProvider service, final ServiceIdentification ident) {
        this.service = service;
        this.ident = ident;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String[] getKeywordList() {
        final Keywords[] list = ident.getKeywords();
        final ArrayList<String> resultList = new ArrayList<String>();

        for (final Keywords word : list) {
            for (final String tmp : word.getKeywords()) {
                resultList.add(tmp);
            }
        }

        return resultList.toArray(new String[resultList.size()]);
    }

    @Override
    public String getAbstract() {
        return ident.getAbstract();
    }

    @Override
    public String getTitle() {
        return ident.getTitle();
    }

    @Override
    public String getName() {
        return ident.getName();
    }

    @Override
    public String getContactPerson() {
        return service.getIndividualName();
    }

    @Override
    public String getContactOrganization() {
        return service.getProviderName();
    }

    @Override
    public String getFees() {
        return ident.getFees();
    }

    @Override
    public String getAccessConstraints() {
        return stringArrayToString(ident.getAccessConstraints());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   array  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String stringArrayToString(final String[] array) {
        final StringBuffer buffer = new StringBuffer("");

        for (final String tmp : array) {
            if (buffer.length() != 0) {
                buffer.append(", " + tmp);
            } else {
                buffer.append(tmp);
            }
        }

        return buffer.toString();
    }
}
