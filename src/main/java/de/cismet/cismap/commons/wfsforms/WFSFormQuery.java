/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfsforms;

import org.jdom.Element;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WFSFormQuery {

    //~ Static fields/initializers ---------------------------------------------

    public static final String INITIAL = "INITIAL"; // NOI18N
    public static final String FOLLOWUP = "FOLLOWUP"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private String serverUrl;
    private String filename;
    private String wfsQueryString;
    private String title;
    private String id;
    private String displayTextProperty;

    private String propertyPrefix;
    private String propertyNamespace;
    private String idProperty;
    private String extentProperty;
    private String positionProperty;
    private String type;
    private String componentName;
    private String queryPlaceholder;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WFSFormQuery.
     */
    public WFSFormQuery() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFilename() {
        return filename;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  filename  DOCUMENT ME!
     */
    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDisplayTextProperty() {
        return displayTextProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  displayTextProperty  DOCUMENT ME!
     */
    public void setDisplayTextProperty(final String displayTextProperty) {
        this.displayTextProperty = displayTextProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIdProperty() {
        return idProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  idProperty  DOCUMENT ME!
     */
    public void setIdProperty(final String idProperty) {
        this.idProperty = idProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getExtentProperty() {
        return extentProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  positionProperty  DOCUMENT ME!
     */
    public void setExtentProperty(final String positionProperty) {
        this.extentProperty = extentProperty;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPositionProperty() {
        return positionProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  positionProperty  DOCUMENT ME!
     */
    public void setPositionProperty(final String positionProperty) {
        this.positionProperty = positionProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  type  DOCUMENT ME!
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  componentName  DOCUMENT ME!
     */
    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getQueryPlaceholder() {
        return queryPlaceholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  queryPlaceholder  DOCUMENT ME!
     */
    public void setQueryPlaceholder(final String queryPlaceholder) {
        this.queryPlaceholder = queryPlaceholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  serverUrl  DOCUMENT ME!
     */
    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getWfsQueryString() {
        return wfsQueryString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wfsQueryString  DOCUMENT ME!
     */
    public void setWfsQueryString(final String wfsQueryString) {
        this.wfsQueryString = wfsQueryString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        final Element ret = new Element("wfsFormQuery");                   // NOI18N
        ret.setAttribute("id", getId());                                   // NOI18N
        ret.setAttribute("title", getTitle());                             // NOI18N
        ret.setAttribute("server", getServerUrl());                        // NOI18N
        ret.setAttribute("queryFile", getFilename());                      // NOI18N
        ret.setAttribute("propertyPrefix", getFilename());                 // NOI18N
        ret.setAttribute("propertyNamespace", getFilename());              // NOI18N
        ret.setAttribute("displayTextProperty", getDisplayTextProperty()); // NOI18N
        ret.setAttribute("extentProperty", getExtentProperty());           // NOI18N
        ret.setAttribute("positionProperty", getExtentProperty());         // NOI18N
        ret.setAttribute("idProperty", getIdProperty());                   // NOI18N
        ret.setAttribute("type", getType());                               // NOI18N
        ret.setAttribute("componentName", getComponentName());             // NOI18N
        if (getQueryPlaceholder() != null) {
            ret.setAttribute("queryPlaceholder", getQueryPlaceholder());   // NOI18N
        }
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyPrefix  DOCUMENT ME!
     */
    public void setPropertyPrefix(final String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPropertyNamespace() {
        return propertyNamespace;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyNamespace  DOCUMENT ME!
     */
    public void setPropertyNamespace(final String propertyNamespace) {
        this.propertyNamespace = propertyNamespace;
    }
}
