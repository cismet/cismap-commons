package de.cismet.cismap.commons.wfs.capabilities.deegree;

import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.wfs.FeatureTypeDescription;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.OperationType;
import de.cismet.cismap.commons.wfs.capabilities.OutputFormatType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.jdom.Element;

/**
 *
 * @author therter
 */
public class DeegreeFeatureType implements FeatureType {
    private final static Logger logger = Logger.getLogger(DeegreeFeatureType.class);
    private org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType feature;
    private WFSCapabilities caps;
    private Element query;
    private Vector<FeatureServiceAttribute> attributes;
    private String geometryName;

    public DeegreeFeatureType(org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType feature, WFSCapabilities caps) 
            throws IOException, BadHttpStatusCodeException {
        this.feature = feature;
        this.caps = caps;
        analyseStructure();
    }


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
        Keywords[] words = feature.getKeywords();
        ArrayList<String> keywords = new ArrayList<String>();

        for (Keywords tmp : words) {
            for ( String s : tmp.getKeywords()) {
                keywords.add(s);
            }
        }

        return keywords.toArray( new String[keywords.size()] );
    }


    @Override
    public String[] getSupportedSRS() {
        URI[] uris = feature.getOtherSrs();
        String[] srs = new String[uris.length];

        for (int i = 0; i < uris.length; ++i) {
            srs[i] = uris[i].toString();
        }

        return srs;
    }

    @Override
    public OperationType[] getOperations() {
        Operation[] operationsOrig = feature.getOperations();
        OperationType[] operations = new OperationType[operationsOrig.length];

        for (int i = 0; i < operationsOrig.length; ++i) {
            operations[i] = new DeegreeOperation(operationsOrig[i]);
        }

        return operations;
    }

    
    @Override
    public OutputFormatType[] getOutputFormats() {
        FormatType[] formatsOrig = feature.getOutputFormats();
        OutputFormatType[] formats = new OutputFormatType[formatsOrig.length];

        for (int i = 0; i < formatsOrig.length; ++i) {
            formats[i] = new DeegreeOutputFormatType(formatsOrig[i]);
        }

        return formats;
    }

    @Override
    public OutputFormatType getOutputFormat(String name) {
        return new DeegreeOutputFormatType( feature.getOutputFormat(name) );
    }

    
    @Override
    public Element getWFSQuery() {
        return query;
    }

    @Override
    public void setWFSQuery(Element query) {
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


    private void analyseStructure() throws IOException, BadHttpStatusCodeException {
        logger.debug("analyseStructure " + getName().toString());//NOI18N
        FeatureTypeDescription featTypeDesc = caps.getServiceFacade().describeFeatureType(this);

        attributes = featTypeDesc.getAllFeatureAttributes(caps);
        geometryName = featTypeDesc.getFirstGeometryName();
        query = caps.getServiceFacade().getGetFeatureQuery(this);
    }

    @Override
    public String getPrefixedNameString() {
        QName qname = getName();
        String name;

        if (qname.getPrefix() != null) {
            name = qname.getPrefix() + ":" + qname.getLocalPart();
        } else {
            name = qname.getLocalPart();
        }

        return name;
    }
}
