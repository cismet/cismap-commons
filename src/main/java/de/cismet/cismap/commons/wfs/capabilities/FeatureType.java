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

import org.jdom.Element;

import java.util.Vector;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.wms.capabilities.Envelope;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureType {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPrefixedNameString();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    QName getName();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getAbstract();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDefaultSRS();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getTitle();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getKeywords();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getSupportedSRS();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    OperationType[] getOperations();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    OutputFormatType[] getOutputFormats();
    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    OutputFormatType getOutputFormat(String name);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Element getWFSQuery();
    /**
     * DOCUMENT ME!
     *
     * @param  query  DOCUMENT ME!
     */
    void setWFSQuery(Element query);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Vector<FeatureServiceAttribute> getFeatureAttributes();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getNameOfGeometryAtrtibute();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    WFSCapabilities getWFSCapabilities();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Envelope[] getWgs84BoundingBoxes();
}
