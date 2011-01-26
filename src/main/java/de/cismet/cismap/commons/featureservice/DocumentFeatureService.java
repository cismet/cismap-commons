/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import org.jdom.Element;

import java.net.URI;

import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * Base class for document-based feature services.
 *
 * @author   Sebastian Puhl
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public abstract class DocumentFeatureService<FT extends FeatureServiceFeature, QT>
        extends AbstractFeatureService<FT, QT> {

    //~ Static fields/initializers ---------------------------------------------

    // public static String DOCUMENT_FEATURELAYER_TYPE = "DocumentFeatureServiceLayer";

    // TODO where are the deegree Constants
    public static final int GML_GEOMETRY_TYPE = 10012;

    //~ Instance fields --------------------------------------------------------

    /** URI of the feature document. */
    protected URI documentURI;

    /**
     * Max number of featurews the underlying factory will parse. If the document contains more features they will be
     * ignored.
     */
    protected int maxSupportedFeatureCount = 150000;

    /** Document Size in Kilobytes. */
    protected long documentSize = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DocumentFeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public DocumentFeatureService(final Element e) throws Exception {
        super(e);
    }

    /**
     * Creates a new DocumentFeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   documentURI   DOCUMENT ME!
     * @param   documentSize  DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public DocumentFeatureService(final String name,
            final URI documentURI,
            final long documentSize,
            final List<FeatureServiceAttribute> attributes) throws Exception {
        super(name, attributes);
        this.documentURI = documentURI;
        this.documentSize = documentSize;
        if (LOG.isInfoEnabled()) {
            LOG.info("creating new DocumentFeatureService from URI: " + documentURI); // NOI18N
        }
    }

    /**
     * Creates a new DocumentFeatureService object.
     *
     * @param  dfs  DOCUMENT ME!
     */
    protected DocumentFeatureService(final DocumentFeatureService dfs) {
        super(dfs);
        this.documentURI = dfs.getDocumentURI();
        this.documentSize = dfs.getDocumentSize();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Element toElement() {
        final Element parentElement = super.toElement();
        final Element docURI = new Element("documentURI");                                                     // NOI18N
        docURI.setText(documentURI.toString());
        parentElement.addContent(docURI);
        parentElement.setAttribute("maxSupportedFeatureCount", String.valueOf(this.maxSupportedFeatureCount)); // NOI18N
        parentElement.setAttribute("documentSize", String.valueOf(this.documentSize));                         // NOI18N
        return parentElement;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        super.initFromElement(element);
        this.setDocumentURI(new URI(element.getChildText("documentURI").trim()));                           // NOI18N
        if (element.getAttribute("maxSupportedFeatureCount") != null) {                                     // NOI18N
            this.maxSupportedFeatureCount = element.getAttribute("maxSupportedFeatureCount").getIntValue(); // NOI18N
        }

        if (element.getAttribute("documentSize") != null) {                         // NOI18N
            this.documentSize = element.getAttribute("documentSize").getIntValue(); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public URI getDocumentURI() {
        return documentURI;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  documentURI  DOCUMENT ME!
     */
    public void setDocumentURI(final URI documentURI) {
        this.documentURI = documentURI;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getDocumentSize() {
        return this.documentSize;
    }
}
