/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import java.net.URI;
import java.util.Vector;
import org.jdom.Element;

/**
 *
 * @author spuhl
 */

public abstract class DocumentFeatureService extends AbstractFeatureService implements StaticFeatureService {
    // <editor-fold defaultstate="collapsed" desc="Declaration ">
    /**
     * The static logger variable
     */
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private URI documentURI;
    //private FeatureCollection featuresCollection = null;
    
    // </editor-fold>
    //TODO where are the deegree Constants
    public static final int GML_GEOMETRY_TYPE = 10012;

    /**
     * Standard constructor, needed for cloning a SimpleWebFeatureService
     */
    public DocumentFeatureService(Element e) throws Exception {
        super(e);
        if (getLayerConf() != null) {
            setDocumentURI(new URI(getLayerConf().getChildText("documentURI").trim()));
        }
    }

    //TODO flat or deep copy
    protected DocumentFeatureService(DocumentFeatureService afs) {
        super(afs);
        setDocumentURI(afs.getDocumentURI());
        setFeatures(afs.getFeatures());
    }

    public DocumentFeatureService(String name, URI documentURI, Vector<FeatureServiceAttribute> attributes) throws Exception {
        super(name, attributes);
        setDocumentURI(documentURI);
        log.info("DocumentUri: " + getDocumentURI());
    }

    @Override
    protected void initConcreteInstance() throws Exception {
        
    }

    @Override
    protected void addConcreteElement(Element e) {
        Element docURI = new Element("documentURI");
        docURI.setText(documentURI.toString());
        e.addContent(docURI);
    }

    
    public static String DOCUMENT_FEATURELAYER_TYPE = "DocumentFeatureServiceLayer";

    @Override
    protected String getFeatureLayerType() {
        return DOCUMENT_FEATURELAYER_TYPE;
    }
       
    public URI getDocumentURI() {
        return documentURI;
    }

    public void setDocumentURI(URI documentURI) {
        this.documentURI = documentURI;
    }
    
     /**
     * This method creates an one-to-one hard copy of the SimpleWebFeatureService
     * @return the copy of the SimpleWebFeatureService
     */
    abstract public Object clone();

}
