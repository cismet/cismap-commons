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
package de.cismet.cismap.commons.wfs.deegree;

import org.apache.log4j.Logger;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.XMLSchema;
import org.deegree.framework.xml.schema.XSDocument;

import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.Vector;

import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;

/**
 * This class parses DescribeFeatureType responses.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeFeatureTypeDescription implements FeatureTypeDescription {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(DeegreeFeatureTypeDescription.class);

    //~ Instance fields --------------------------------------------------------

    private org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription desc;
    private FeatureType feature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeFeatureTypeDescription object.
     *
     * @param   featureTypeDescription  a describeFeatureType response as string
     * @param   feature                 the feature taht is described in the first parameter
     *
     * @throws  ParserException  DOCUMENT ME!
     */
    public DeegreeFeatureTypeDescription(final String featureTypeDescription, final FeatureType feature)
            throws ParserException {
        this.feature = feature;
        try {
            final XMLFragment frag = new XMLFragment();
            frag.load(new ByteArrayInputStream(featureTypeDescription.getBytes()), "http:/fake.de"); // NOI18N
            desc = new org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription(frag);
        } catch (Exception e) {
            throw new ParserException(e.getMessage(), e.getCause());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Element getSchemaRootElement() {
        return desc.getFeatureTypeSchema().getRootElement();
    }

    @Override
    public String toString() {
        return desc.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   caps  the capabilities object of the corresponding wfs
     *
     * @return  all attributes of the described feature
     */
    @Override
    public Vector<FeatureServiceAttribute> getAllFeatureAttributes(final WFSCapabilities caps) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("get complextypes for " + feature.getName().toString()); // NOI18N
            }

            final XMLFragment xmlFrag = desc.getFeatureTypeSchema();
            if (xmlFrag.hasSchema()) {
                final XSDocument xsDoc = new XSDocument();
                xsDoc.setRootElement(xmlFrag.getRootElement());
                final XMLSchema xmlSchema = xsDoc.parseXMLSchema();

                // check if the FeatureType-name is in the current FeatureTypeDescription
                final ElementDeclaration requestedElement = xmlSchema.getElementDeclaration(new QualifiedName(
                            feature.getName()));
                if (requestedElement == null) {
                    logger.fatal("Error requestedElement == null " + feature.getName() + " " + caps.getURL().toString()
                                + "\n\n" + desc.toString());
                }
                if ((requestedElement != null) && (requestedElement.getName().getNamespace() != null)) { // if FeatureType-name found
                    QualifiedName typeName = xmlSchema.getElementDeclaration(new QualifiedName(feature.getName()))
                                .getType()
                                .getName();
                    final ComplexTypeDeclaration compTypeDec = xmlSchema.getComplexTypeDeclaration(typeName);
                    ElementDeclaration[] elementDeclaration;

                    if (compTypeDec == null) {
                        typeName = xmlSchema.getElementDeclaration(new QualifiedName(feature.getName())).getName();

                        if (xmlSchema.getElementDeclaration(typeName).getType().getTypeDeclaration()
                                    instanceof ComplexTypeDeclaration) {
                            final ComplexTypeDeclaration type = (ComplexTypeDeclaration)xmlSchema.getElementDeclaration(
                                    typeName).getType().getTypeDeclaration();
                            elementDeclaration = type.getElements();
                        } else {
                            elementDeclaration = new ElementDeclaration[0];
                        }
                    } else {
                        elementDeclaration = compTypeDec.getElements();
                    }

                    if (getFirstGeometryName(elementDeclaration) != null) {
                        final Vector<FeatureServiceAttribute> fsaVector = new Vector<FeatureServiceAttribute>(
                                elementDeclaration.length);

                        for (final ElementDeclaration e : elementDeclaration) {
                            fsaVector.add(new FeatureServiceAttribute(
                                    feature.getName().getPrefix()
                                            + ":"
                                            + e.getName().getLocalName(),
                                    e.getType().getName().getPrefixedName(),
                                    true)); // NOI18N
                        }

                        return fsaVector;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("complextypes found: " + elementDeclaration); // NOI18N
                    }
                }
            }
        } catch (Throwable ex) {
            logger.fatal("Error in getElementDeclarations", ex);                   // NOI18N
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the name of the first geometry that was found in the feature description or null, if no geometry was
     *          found
     */
    @Override
    public String getFirstGeometryName() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("get first geometry name for " + feature.getName().toString()); // NOI18N
            }

            final XMLFragment xmlFrag = desc.getFeatureTypeSchema();
            if (xmlFrag.hasSchema()) {
                final XSDocument xsDoc = new XSDocument();
                xsDoc.setRootElement(xmlFrag.getRootElement());
                final XMLSchema xmlSchema = xsDoc.parseXMLSchema();

                // check if the FeatureType-name is in the current FeatureTypeDescription
                final ElementDeclaration requestedElement = xmlSchema.getElementDeclaration(new QualifiedName(
                            feature.getName()));

                if ((requestedElement != null) && (requestedElement.getName().getNamespace() != null)) { // if FeatureType-name found
                    QualifiedName typeName = xmlSchema.getElementDeclaration(new QualifiedName(feature.getName()))
                                .getType()
                                .getName();
                    final ComplexTypeDeclaration compTypeDec = xmlSchema.getComplexTypeDeclaration(typeName);

                    if (compTypeDec == null) {
                        typeName = xmlSchema.getElementDeclaration(new QualifiedName(feature.getName())).getName();

                        if (xmlSchema.getElementDeclaration(typeName).getType().getTypeDeclaration()
                                    instanceof ComplexTypeDeclaration) {
                            final ComplexTypeDeclaration type = (ComplexTypeDeclaration)xmlSchema.getElementDeclaration(
                                    typeName).getType().getTypeDeclaration();
                            return getFirstGeometryName(type.getElements());
                        } else {
                            return getFirstGeometryName(new ElementDeclaration[0]);
                        }
                    } else {
                        return getFirstGeometryName(compTypeDec.getElements());
                    }
                }
            }
        } catch (Throwable ex) {
            logger.fatal("Error in getFirstGeometryName", ex); // NOI18N
        }

        return null;
    }

    /**
     * Returns the name of the first geometry that was found in the ElementDeclaration-array.
     *
     * @param   elements  ElementDeclaration-array that will be searched
     *
     * @return  ElementDeclaration-name or null
     */
    public String getFirstGeometryName(final ElementDeclaration[] elements) {
        for (final ElementDeclaration e : elements) {
            if (logger.isDebugEnabled()) {
                logger.debug("getFirstGeometryName e: " + e.getType().getName().getLocalName());
            }
            if (FeatureServiceUtilities.isElementOfGeometryType(e.getType().getName().getLocalName())) {
                // TODO: warum wird hier das Praefix des Features genutzt?? Siehe auch Methode getAllFeatureAttributes
                return feature.getName().getPrefix() + ":" + e.getName().getLocalName();
            }
        }
        return null;
    }
}
