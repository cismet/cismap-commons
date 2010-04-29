/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import java.net.URI;
import java.util.Vector;
import org.jdom.Element;

/**
 * Base class for document-based feature services
 *
 * @author Sebastian Puhl
 * @author Pascal Dihé
 */
public abstract class DocumentFeatureService<FT extends FeatureServiceFeature, QT>  extends AbstractFeatureService<FT, QT>
{
  // <editor-fold defaultstate="collapsed" desc="Declaration ">

  /**
   * URI of the feature document
   */
  protected URI documentURI;

  /**
   * Max number of featurews the underlying factory will parse. If the document
   * contains more features they will be ignored.
   */
  protected int maxSupportedFeatureCount = 150000;
  

  /**
   * Document Size in Kilobytes
   */
  protected long documentSize = -1;

  //public static String DOCUMENT_FEATURELAYER_TYPE = "DocumentFeatureServiceLayer";

  //TODO where are the deegree Constants
  public static final int GML_GEOMETRY_TYPE = 10012;

  public DocumentFeatureService(Element e) throws Exception
  {
    super(e);
  }

  protected DocumentFeatureService(DocumentFeatureService dfs)
  {
    super(dfs);
    this.setDocumentURI(dfs.getDocumentURI());
    this.documentSize = dfs.getDocumentSize();
  }

  public DocumentFeatureService(String name, URI documentURI, long documentSize, Vector<FeatureServiceAttribute> attributes) throws Exception
  {
    super(name, attributes);
    this.setDocumentURI(documentURI);
    this.documentSize = documentSize;
    logger.info("creating new DocumentFeatureService from URI: " + this.getDocumentURI());//NOI18N
  }

  @Override
  public Element toElement()
  {
    Element parentElement = super.toElement();
    Element docURI = new Element("documentURI");//NOI18N
    docURI.setText(documentURI.toString());
    parentElement.addContent(docURI);
    parentElement.setAttribute("maxSupportedFeatureCount", String.valueOf(this.maxSupportedFeatureCount));//NOI18N
    parentElement.setAttribute("documentSize", String.valueOf(this.documentSize));//NOI18N
    return parentElement;
  }


  @Override
  public void initFromElement(Element element) throws Exception
  {
    super.initFromElement(element);
    this.setDocumentURI(new URI(element.getChildText("documentURI").trim()));//NOI18N
    if(element.getAttribute("maxSupportedFeatureCount") != null){//NOI18N
      this.maxSupportedFeatureCount = element.getAttribute("maxSupportedFeatureCount").getIntValue();//NOI18N
    }

    if(element.getAttribute("documentSize") != null){//NOI18N
      this.documentSize = element.getAttribute("documentSize").getIntValue();//NOI18N
    }
  }

  public URI getDocumentURI()
  {
    return documentURI;
  }

  public void setDocumentURI(URI documentURI)
  {
    this.documentURI = documentURI;
  }

  public long getDocumentSize()
  {
    return this.documentSize;
  }
}
