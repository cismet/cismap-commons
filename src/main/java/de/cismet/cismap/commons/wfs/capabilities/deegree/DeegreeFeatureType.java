/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfs.capabilities.deegree;

import org.apache.log4j.Logger;

import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;

import org.jdom.Element;

import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.Vector;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.OperationType;
import de.cismet.cismap.commons.wfs.capabilities.OutputFormatType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeEnvelope;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeFeatureType implements FeatureType {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger logger = Logger.getLogger(DeegreeFeatureType.class);

    //~ Instance fields --------------------------------------------------------

    private org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType feature;
    private WFSCapabilities caps;
    private Element query;
    private Vector<FeatureServiceAttribute> attributes;
    private String geometryName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeFeatureType object.
     *
     * @param   feature  DOCUMENT ME!
     * @param   caps     DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    public DeegreeFeatureType(final org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType feature,
            final WFSCapabilities caps) throws IOException, Exception {
        this.feature = feature;
        this.caps = caps;
        analyseStructure();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public QName getName() {
        return new QName(feature.getName().getNamespace().toString(),
                feature.getName().getLocalName(),
                feature.getName().getPrefix());
    }

    @Override
    public String getAbstract() {
        return feature.getAbstract();
    }

    @Override
    public String getDefaultSRS() {
        return feature.getDefaultSRS().toString();
    }

    @Override
    public String getTitle() {
        return feature.getTitle();
    }

    @Override
    public String[] getKeywords() {
        final Keywords[] words = feature.getKeywords();
        final ArrayList<String> keywords = new ArrayList<String>();

        for (final Keywords tmp : words) {
            for (final String s : tmp.getKeywords()) {
                keywords.add(s);
            }
        }

        return keywords.toArray(new String[keywords.size()]);
    }

    @Override
    public String[] getSupportedSRS() {
        URI[] uris = feature.getOtherSrs();
        if (uris == null) {
            uris = new URI[0];
        }
        final String[] srs = new String[uris.length];

        for (int i = 0; i < uris.length; ++i) {
            srs[i] = uris[i].toString();
        }

        return srs;
    }

    @Override
    public OperationType[] getOperations() {
        final Operation[] operationsOrig = feature.getOperations();
        final OperationType[] operations = new OperationType[operationsOrig.length];

        for (int i = 0; i < operationsOrig.length; ++i) {
            operations[i] = new DeegreeOperation(operationsOrig[i]);
        }

        return operations;
    }

    @Override
    public OutputFormatType[] getOutputFormats() {
        final FormatType[] formatsOrig = feature.getOutputFormats();
        final OutputFormatType[] formats = new OutputFormatType[formatsOrig.length];

        for (int i = 0; i < formatsOrig.length; ++i) {
            formats[i] = new DeegreeOutputFormatType(formatsOrig[i]);
        }

        return formats;
    }

    @Override
    public OutputFormatType getOutputFormat(final String name) {
        return new DeegreeOutputFormatType(feature.getOutputFormat(name));
    }

    @Override
    public Element getWFSQuery() {
        return query;
    }

    @Override
    public void setWFSQuery(final Element query) {
        this.query = query;
    }

    @Override
    public Vector<FeatureServiceAttribute> getFeatureAttributes() {
        return attributes;
    }

    @Override
    public String getNameOfGeometryAtrtibute() {
        return geometryName;
    }

    @Override
    public WFSCapabilities getWFSCapabilities() {
        return caps;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     * @throws  Exception    DOCUMENT ME!
     */
    private void analyseStructure() throws IOException, Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("analyseStructure " + getName().toString()); // NOI18N
        }
        final FeatureTypeDescription featTypeDesc = caps.getServiceFacade().describeFeatureType(this);

        attributes = featTypeDesc.getAllFeatureAttributes(caps);
        geometryName = featTypeDesc.getFirstGeometryName();
        query = caps.getServiceFacade().getGetFeatureQuery(this);
    }

    @Override
    public String getPrefixedNameString() {
        final QName qname = getName();
        String name;

        if (qname.getPrefix() != null) {
            name = qname.getPrefix() + ":" + qname.getLocalPart();
        } else {
            name = qname.getLocalPart();
        }

        return name;
    }

    @Override
    public Envelope[] getWgs84BoundingBoxes() {
        final org.deegree.model.spatialschema.Envelope[] envelopeOrig = feature.getWgs84BoundingBoxes();
        final Envelope[] envelopes = new Envelope[envelopeOrig.length];

        for (int i = 0; i < envelopeOrig.length; ++i) {
            envelopes[i] = new DeegreeEnvelope(envelopeOrig[i]);
        }

        return envelopes;
    }
}
