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

import de.cismet.cismap.commons.exceptions.ParserException;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.XMLSchema;
import org.deegree.framework.xml.schema.XSDocument;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class parses DescribeFeatureType responses.
 * @author therter
 */
public class DeegreeFeatureTypeDescription implements FeatureTypeDescription {
    private static final Logger logger = Logger.getLogger(DeegreeFeatureTypeDescription.class);
    private org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription desc;
    private FeatureType feature;

    /**
     *
     * @param featureTypeDescription a describeFeatureType response as string
     * @param feature the feature taht is described in the first parameter
     * @throws ParserException
     * @throws IOException
     */
    public DeegreeFeatureTypeDescription(String featureTypeDescription, FeatureType feature) throws ParserException {
        this.feature = feature;
        try {
            XMLFragment frag = new XMLFragment();
            frag.load(new ByteArrayInputStream(featureTypeDescription.getBytes()), "http:/fake.de");//NOI18N
            desc = new org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription(frag);
        } catch (Exception e) {
            throw new ParserException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Element getSchemaRootElement() {
        return desc.getFeatureTypeSchema().getRootElement();
    }

    @Override
    public String toString() {
        return desc.toString();
    }

    /**
     *
     * @param caps the capabilities object of the corresponding wfs
     * @return all attributes of the described feature
     */
    @Override
    public Vector<FeatureServiceAttribute> getAllFeatureAttributes(WFSCapabilities caps) {
        try {
            logger.debug("get complextypes for " + feature.getName().toString());//NOI18N

            XMLFragment xmlFrag = desc.getFeatureTypeSchema();
            if (xmlFrag.hasSchema()) {
                XSDocument xsDoc = new XSDocument();
                xsDoc.setRootElement(xmlFrag.getRootElement());
                XMLSchema xmlSchema = xsDoc.parseXMLSchema();

                // check if the FeatureType-name is in the current FeatureTypeDescription
                ElementDeclaration requestedElement = xmlSchema.getElementDeclaration(new QualifiedName( feature.getName() ));
                if (requestedElement == null) {
                    logger.fatal("Error requestedElement == null " + feature.getName() + " " + caps.getURL().toString() + "\n\n" + desc.toString() );
                }
                if (requestedElement != null && requestedElement.getName().getNamespace() != null) { // if FeatureType-name found
                    QualifiedName typeName = xmlSchema.getElementDeclaration( new QualifiedName( feature.getName() ) )
                            .getType().getName();
                    ComplexTypeDeclaration compTypeDec = xmlSchema.getComplexTypeDeclaration(typeName);

                    if (getFirstGeometryName(compTypeDec.getElements()) != null) {
                        Vector<FeatureServiceAttribute> fsaVector = new Vector<FeatureServiceAttribute>(compTypeDec.getElements().length);

                        for (ElementDeclaration e : compTypeDec.getElements()) {
                            fsaVector.add(new FeatureServiceAttribute(feature.getName().getPrefix() + ":" + e.getName().getLocalName(), e.getType().getName().getPrefixedName(), true));//NOI18N
                        }

                        return fsaVector;
                    }
                    logger.debug("complextypes found: " + compTypeDec.getElements());//NOI18N
                }
            }
        } catch (Throwable ex) {
            logger.fatal("Error in getElementDeclarations", ex);//NOI18N
        }

        return null;
    }


    /**
     * @return the name of the first geometry that was found in the feature description or
     * null, if no geometry was found
     */
    @Override
    public String getFirstGeometryName() {
        try {
            logger.debug("get first geometry name for " + feature.getName().toString());//NOI18N

            XMLFragment xmlFrag = desc.getFeatureTypeSchema();
            if (xmlFrag.hasSchema()) {
                XSDocument xsDoc = new XSDocument();
                xsDoc.setRootElement(xmlFrag.getRootElement());
                XMLSchema xmlSchema = xsDoc.parseXMLSchema();

                // check if the FeatureType-name is in the current FeatureTypeDescription
                ElementDeclaration requestedElement = xmlSchema.getElementDeclaration(new QualifiedName( feature.getName() ));

                if (requestedElement != null && requestedElement.getName().getNamespace() != null) { // if FeatureType-name found
                    QualifiedName typeName = xmlSchema.getElementDeclaration( new QualifiedName( feature.getName() ) )
                            .getType().getName();
                    ComplexTypeDeclaration compTypeDec = xmlSchema.getComplexTypeDeclaration(typeName);

                    return getFirstGeometryName(compTypeDec.getElements());
                }
            }
        } catch (Throwable ex) {
            logger.fatal("Error in getFirstGeometryName", ex);//NOI18N
        }

        return null;
    }

  /**
   * Returns the name of the first geometry that was found in the ElementDeclaration-array.
   * @param elements ElementDeclaration-array that will be searched
   * @return ElementDeclaration-name or null
   */
  public String getFirstGeometryName(ElementDeclaration[] elements)
  {
    for (ElementDeclaration e : elements)
    {
      if (logger.isDebugEnabled()) {
        logger.debug("getFirstGeometryName e: " + e.getType().getName().getLocalName());
      }
      if ( FeatureServiceUtilities.isElementOfGeometryType(e.getType().getName().getLocalName()) )
      {
          //TODO: warum wird hier das Praefix des Features genutzt?? Siehe auch Methode getAllFeatureAttributes
        return feature.getName().getPrefix() + ":" + e.getName().getLocalName();
      }
    }
    return null;
  }
}
