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

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.util.Map;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.XMLObjectFactory;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;
import de.cismet.cismap.commons.featureservice.style.Style;

/**
 * Default implementation of the LayerProperties Interface.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class DefaultLayerProperties implements LayerProperties {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger logger = Logger.getLogger(DefaultLayerProperties.class);

    //~ Instance fields --------------------------------------------------------

    private Style style;

    private int idExpressionType = EXPRESSIONTYPE_UNDEFINED;
    private int primaryAnnotationExpressionType = EXPRESSIONTYPE_UNDEFINED;
    private int secondaryAnnotationExpressionType = EXPRESSIONTYPE_UNDEFINED;
    private int queryType = QUERYTYPE_UNDEFINED;

    private String idExpression;
    private String primaryAnnotationExpression;
    private String secondaryAnnotationExpression;
    // private String queryTemplate;

    private boolean idExpressionEnabled = true;
    private AbstractFeatureService featureService;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultLayerProperties instance and initialises the style property with a preconfigured BasicStyle
     * object. All expression types are set to undefined.
     */
    public DefaultLayerProperties() {
        this.style = new BasicStyle();
    }

    /**
     * Creates a new DefaultLayerProperties instance and initialises the style property from the style parameter.
     *
     * @param  style  Style object to be used
     */
    public DefaultLayerProperties(final Style style) {
        this.style = style;
    }

    /**
     * Initialises a new DefaultLayerProperties instance from an xml document.
     *
     * @param   element  the xml serialized DefaultLayerProperties object
     *
     * @throws  Exception  if the initilaisation failed
     *
     * @see     LayerProperties#initFromElement(Element)
     */
    public DefaultLayerProperties(final Element element) throws Exception {
        this.initFromElement(element);
    }

    /**
     * Initialises a new DefaultLayerProperties instance from an existing LayerProperties object. The properties of the
     * LayerProperties will be cloned.
     *
     * @param  layerProperties  LayerProperties to be used for initialisation
     *
     * @see    LayerProperties#clone(Object)
     */
    public DefaultLayerProperties(final LayerProperties layerProperties) {
        this.assign(layerProperties);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public LayerProperties clone() {
        return new DefaultLayerProperties(this);
    }

    @Override
    public void setStyle(final Style featureStyle) {
        this.style = featureStyle;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * Get the value of idExpression.
     *
     * @return  the value of idExpression
     */
    @Override
    public String getIdExpression() {
        return idExpression;
    }

    @Override
    public void setIdExpression(final String idExpression, final int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("setIdExpression: '" + idExpression + "', " + type);
        }
        this.idExpression = idExpression;
        this.idExpressionType = type;
    }

    @Override
    public int getIdExpressionType() {
        return this.idExpressionType;
    }

    @Override
    public String getPrimaryAnnotationExpression() {
        return primaryAnnotationExpression;
    }

    @Override
    public int getPrimaryAnnotationExpressionType() {
        return this.primaryAnnotationExpressionType;
    }

    @Override
    public void setPrimaryAnnotationExpression(final String primaryAnnotationExpression, final int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("setPrimaryAnnotationExpression: '" + primaryAnnotationExpression + "', " + type);
        }
        this.primaryAnnotationExpression = primaryAnnotationExpression;
        this.primaryAnnotationExpressionType = type;
        if (this.style != null) {
            this.style.setLabel(primaryAnnotationExpression);
        }
    }

    @Override
    public String getSecondaryAnnotationExpression() {
        return this.secondaryAnnotationExpression;
    }

    @Override
    public int getSecondaryAnnotationExpressionType() {
        return this.secondaryAnnotationExpressionType;
    }

    @Override
    public void setSecondaryAnnotationExpression(final String secondaryAnnotationExpression, final int type) {
        if (logger.isDebugEnabled()) {
            logger.debug("setSecondaryAnnotationExpression: '" + secondaryAnnotationExpression + "', " + type);
        }
        this.secondaryAnnotationExpression = secondaryAnnotationExpression;
        this.secondaryAnnotationExpressionType = type;
    }

    @Override
    public int getQueryType() {
        return this.queryType;
    }

//  @Override
//  public String getQueryTemplate()
//  {
//    return this.queryTemplate;
//  }
//
//  @Override
//  public void setQueryTemplate(String queryTemplate, int queryType)
//  {
//    this.queryTemplate = queryTemplate;
//    this.queryType = queryType;
//  }

    @Override
    public Element toElement() {
        final Element element = new Element(LAYER_PROPERTIES_ELEMENT);
        element.setAttribute(ConvertableToXML.TYPE_ATTRIBUTE, this.getClass().getCanonicalName());

        if (this.getIdExpression() != null) {
            element.setAttribute("idExpression", this.getIdExpression());
            element.setAttribute("idExpressionType", String.valueOf(this.getIdExpressionType()));
        }

        element.setAttribute("idExpressionEnabled", String.valueOf(this.isIdExpressionEnabled()));

        if (this.getPrimaryAnnotationExpression() != null) {
            element.setAttribute("primaryAnnotationExpression", this.getPrimaryAnnotationExpression());
            element.setAttribute(
                "primaryAnnotationExpressionType",
                String.valueOf(this.getPrimaryAnnotationExpressionType()));
        }

        if (this.getSecondaryAnnotationExpression() != null) {
            element.setAttribute("secondaryAnnotationExpression", this.getSecondaryAnnotationExpression());
            element.setAttribute(
                "secondaryAnnotationExpressionType",
                String.valueOf(this.getSecondaryAnnotationExpressionType()));
        }

//    if(this.getQueryTemplate() != null)
//    {
//      element.setAttribute("queryTemplate", this.getQueryTemplate());
//      element.setAttribute("queryType", String.valueOf(this.getQueryType()));
//    }

        element.setAttribute("queryType", String.valueOf(this.getQueryType()));

        if (this.getStyle() != null) {
            try {
                element.addContent(this.getStyle().toElement());
            } catch (Throwable t) {
                logger.error("style element could not be created", t);
            }
        } else {
            logger.warn("style element could not be created (is null), setting default basic style");
            element.addContent(new BasicStyle().toElement());
        }

        return element;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        final String type = element.getAttributeValue("type");
        if (type == null) {
            throw new ClassNotFoundException("madatory attribute 'type' missing in xml element");
        }
        final Class typeClass = Class.forName(type);
        if (!typeClass.isAssignableFrom(this.getClass())) {
            throw new ClassNotFoundException("the XML element type '" + type
                        + "'does not match the layer properties class '" + this.getClass().getCanonicalName() + "'");
        }

        if (element.getAttribute("idExpression") != null) {
            this.setIdExpression(element.getAttributeValue("idExpression"),
                element.getAttribute("idExpressionType").getIntValue());
        }

        if (element.getAttribute("primaryAnnotationExpression") != null) {
            this.setPrimaryAnnotationExpression(element.getAttributeValue("primaryAnnotationExpression"),
                element.getAttribute("primaryAnnotationExpressionType").getIntValue());
        }

        if (element.getAttribute("idExpressionEnablesd") != null) {
            this.setIdExpressionEnabled(element.getAttribute("idExpressionEnabled").getBooleanValue());
        }

        if (element.getAttribute("secondaryAnnotationExpression") != null) {
            this.setSecondaryAnnotationExpression(element.getAttributeValue("secondaryAnnotationExpression"),
                element.getAttribute("secondaryAnnotationExpressionType").getIntValue());
        }

//    if(element.getAttribute("queryTemplate") != null)
//      this.setQueryTemplate(element.getAttributeValue("queryTemplate"), element.getAttribute("queryType").getIntValue());

        if (element.getAttribute("queryType") != null) {
            this.setQueryType(element.getAttribute("queryType").getIntValue());
        }

        final Element styleElement = element.getChild(Style.STYLE_ELEMENT);

        // FIXME: remove support for old style elements
        if ((styleElement != null) && (styleElement.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) == null)) {
            logger.warn("initFromElement: restoring object from deprecated xml element");
            this.setStyle(new BasicStyle());
            this.setPrimaryAnnotationExpression(this.getStyle().getLabel(), EXPRESSIONTYPE_PROPERTYNAME);
        } else if (styleElement != null) {
            try {
                final Style restoredStyle = (Style)XMLObjectFactory.restoreObjectfromElement(styleElement);
                this.setStyle(restoredStyle);
            } catch (Throwable t) {
                logger.error("could not restore generic style element '"
                            + styleElement.getAttribute(ConvertableToXML.TYPE_ATTRIBUTE) + "': \n" + t.getMessage(),
                    t);
                this.setStyle(new BasicStyle());
            }
        } else {
            logger.warn("no style found in XML Element, setting default random Basic Style");
            this.setStyle(new BasicStyle());
        }
    }

    @Override
    public void assign(final LayerProperties layerProperties) {
        if (layerProperties.getIdExpression() != null) {
            this.setIdExpression(new String(layerProperties.getIdExpression()), layerProperties.getIdExpressionType());
        } else {
            this.idExpressionType = EXPRESSIONTYPE_UNDEFINED;
        }

        if (layerProperties.getPrimaryAnnotationExpression() != null) {
            this.setPrimaryAnnotationExpression(new String(layerProperties.getPrimaryAnnotationExpression()),
                layerProperties.getPrimaryAnnotationExpressionType());
        } else {
            this.primaryAnnotationExpressionType = EXPRESSIONTYPE_UNDEFINED;
        }

        if (layerProperties.getSecondaryAnnotationExpression() != null) {
            this.setSecondaryAnnotationExpression(new String(layerProperties.getSecondaryAnnotationExpression()),
                layerProperties.getSecondaryAnnotationExpressionType());
        } else {
            this.secondaryAnnotationExpressionType = EXPRESSIONTYPE_UNDEFINED;
        }

        this.setIdExpressionEnabled(layerProperties.isIdExpressionEnabled());

        this.setQueryType(layerProperties.getQueryType());

        try {
            this.setStyle((Style)((layerProperties.getStyle() != null) ? layerProperties.getStyle().clone()
                                                                       : new BasicStyle()));
        } catch (CloneNotSupportedException ex) {
            logger.warn(
                "unexpected CloneNotSupportedException while cloning Style object, setting default Basic Style",
                ex);
            this.setStyle(new BasicStyle());
        }
    }

    @Override
    public boolean isIdExpressionEnabled() {
        return this.idExpressionEnabled;
    }

    @Override
    public void setIdExpressionEnabled(final boolean idExpressionEnabled) {
        this.idExpressionEnabled = idExpressionEnabled;
    }

    @Override
    public void setQueryType(final int queryType) {
        this.queryType = queryType;
    }

    @Override
    public AbstractFeatureService getFeatureService() {
        return featureService;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureService  DOCUMENT ME!
     */
    public void setFeatureService(final AbstractFeatureService featureService) {
        this.featureService = featureService;
    }
}
